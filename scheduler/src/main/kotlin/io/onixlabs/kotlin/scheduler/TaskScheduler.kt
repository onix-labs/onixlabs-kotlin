/*
 * Copyright 2020-2026 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.onixlabs.kotlin.scheduler

import java.time.Clock
import java.time.Instant
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

/**
 * Represents a scheduler that runs tasks at the occurrences of their schedules.
 *
 * One scheduling thread calculates each task's next occurrence and waits until the earliest is due; tasks run on the
 * executor. A run that throws is reported to the listener and the task stays scheduled, so one failing task never
 * affects another. Tasks can be added and removed while the scheduler is running.
 *
 * The scheduler holds its state in memory: occurrences that fall while the process is not running are not caught up.
 * Occurrences missed while it is running, for example because the machine slept, are handled by each task's [Misfire] policy.
 *
 * @param executor The executor on which tasks run. If null, the scheduler creates a cached thread pool of daemon threads,
 * which it shuts down when it is closed; an executor that is specified is not shut down.
 * @param listener The listener that receives the outcomes of runs. The default logs them.
 * @param clock The clock from which the current time is obtained.
 * @param misfireThreshold How late an occurrence may run before it counts as missed.
 * @param block The block that declares the scheduler's initial tasks.
 */
public class TaskScheduler(
    executor: Executor? = null,
    private val listener: TaskListener = TaskListener.Logging,
    private val clock: Clock = Clock.systemUTC(),
    private val misfireThreshold: Duration = 1.minutes,
    block: TaskSchedulerBuilder.() -> Unit = {},
) : AutoCloseable {

    private companion object {
        val MAXIMUM_WAIT: Duration = 1.minutes
        const val MAXIMUM_MISSED = 10_000
        val logger: System.Logger = System.getLogger("io.onixlabs.kotlin.scheduler")
    }

    private enum class State { CREATED, STARTED, CLOSED }

    private class Entry(
        val name: String,
        var schedule: Schedule,
        val overlap: Overlap,
        val misfire: Misfire,
        val action: () -> Unit,
    ) {
        var nextRun: Instant? = null
        var lastOutcome: TaskOutcome? = null
        var running: Int = 0
        var queued: TaskRun? = null
        var removed: Boolean = false
    }

    private val ownedExecutor: ExecutorService? = if (executor == null) {
        Executors.newCachedThreadPool { runnable -> Thread(runnable, "onixlabs-scheduler-task").apply { isDaemon = true } }
    } else {
        null
    }
    private val executor: Executor = executor ?: ownedExecutor!!
    private val lock = ReentrantLock()
    private val changed = lock.newCondition()
    private val idle = lock.newCondition()
    private val entries: MutableMap<String, Entry> = linkedMapOf()
    private var state = State.CREATED
    private var thread: Thread? = null
    private var activeRuns = 0
    private val notifications: MutableList<() -> Unit> = mutableListOf()

    init {
        TaskSchedulerBuilder(this).apply(block)
    }

    /**
     * Gets the status of every task, in the order in which the tasks were added.
     */
    public val tasks: List<TaskStatus>
        get() = lock.withLock { entries.values.map { TaskStatus(it.name, it.schedule, it.nextRun, it.lastOutcome, it.running > 0) } }

    /**
     * Adds a task. If the scheduler is running, the task's first occurrence is the first one after now.
     *
     * @param name The unique name of the task.
     * @param schedule The schedule of the task.
     * @param overlap What happens when an occurrence is due while a previous run is still running.
     * @param misfire What happens when occurrences were missed.
     * @param action The action that the task runs.
     * @throws IllegalArgumentException if a task with the specified name already exists.
     * @throws IllegalStateException if the scheduler is closed.
     */
    public fun add(name: String, schedule: Schedule, overlap: Overlap = Overlap.SKIP, misfire: Misfire = Misfire.RUN_ONCE, action: () -> Unit) {
        lock.withLock {
            check(state != State.CLOSED) { "Cannot add a task to a closed scheduler." }
            require(name !in entries) { "A task named '$name' already exists." }
            val entry = Entry(name, schedule, overlap, misfire, action)
            if (state == State.STARTED) entry.nextRun = schedule.nextAfter(clock.instant())
            entries[name] = entry
            changed.signalAll()
        }
    }

    /**
     * Removes a task. A run that is in progress completes, but the task does not run again.
     *
     * @param name The name of the task.
     * @return Returns true if the task was removed; otherwise, false if there was no task with the specified name.
     */
    public fun remove(name: String): Boolean = lock.withLock {
        val entry = entries.remove(name) ?: return false
        entry.removed = true
        entry.queued = null
        changed.signalAll()
        true
    }

    /**
     * Replaces the schedule of a task. If the scheduler is running, the task's next occurrence is the first one of the
     * new schedule after now. To add or remove rules, combine schedules: `schedule { include(a); include(b) }`.
     *
     * @param name The name of the task.
     * @param schedule The new schedule of the task.
     * @return Returns true if the task was rescheduled; otherwise, false if there was no task with the specified name.
     */
    public fun reschedule(name: String, schedule: Schedule): Boolean = lock.withLock {
        val entry = entries[name] ?: return false
        entry.schedule = schedule
        if (state == State.STARTED) entry.nextRun = schedule.nextAfter(clock.instant())
        changed.signalAll()
        true
    }

    /**
     * Starts the scheduler. Each task's first occurrence is the first one after now.
     *
     * @throws IllegalStateException if the scheduler has already been started or closed.
     */
    public fun start() {
        activate()
        thread = Thread(::loop, "onixlabs-scheduler").apply {
            isDaemon = true
            start()
        }
    }

    /**
     * Stops the scheduler, and waits for runs in progress to complete.
     *
     * @param timeout How long to wait for runs in progress to complete.
     * @return Returns true if every run in progress completed within the timeout; otherwise, false.
     */
    public fun close(timeout: Duration): Boolean {
        val thread = lock.withLock {
            if (state == State.CLOSED) return activeRuns == 0
            state = State.CLOSED
            changed.signalAll()
            this.thread
        }
        thread?.join()
        val completed = lock.withLock {
            var remaining = timeout.inWholeNanoseconds
            while (activeRuns > 0 && remaining > 0) remaining = idle.awaitNanos(remaining)
            activeRuns == 0
        }
        ownedExecutor?.shutdown()
        ownedExecutor?.awaitTermination(1, TimeUnit.SECONDS)
        return completed
    }

    /**
     * Stops the scheduler, and waits up to 30 seconds for runs in progress to complete.
     */
    override fun close() {
        close(30.seconds)
    }

    /**
     * Marks the scheduler as started, and calculates each task's first occurrence, without starting the scheduling
     * thread. [start] calls this; tests call it directly and drive the scheduler with [runDue].
     *
     * @throws IllegalStateException if the scheduler has already been started or closed.
     */
    internal fun activate() {
        lock.withLock {
            check(state == State.CREATED) { "The scheduler has already been ${state.name.lowercase()}." }
            state = State.STARTED
            val now = clock.instant()
            for (entry in entries.values) entry.nextRun = entry.schedule.nextAfter(now)
        }
    }

    /**
     * Runs every task whose occurrence is due at the specified instant, applying each task's misfire policy to
     * occurrences that are later than the misfire threshold, and calculates each task's next occurrence.
     *
     * @param now The current instant.
     * @return Returns the earliest next occurrence of any task, or null if no task has a further occurrence.
     */
    internal fun runDue(now: Instant): Instant? = lock.withLock {
        dueRuns(now)
    }.also { flush() }

    private fun dueRuns(now: Instant): Instant? {
        for (entry in entries.values.toList()) {
            val due = entry.nextRun ?: continue
            if (due > now) continue
            val occurrences = generateSequence(due) { entry.schedule.nextAfter(it) }.takeWhile { it <= now }.take(MAXIMUM_MISSED).toList()
            entry.nextRun = entry.schedule.nextAfter(now)
            val latest = occurrences.last()
            val late = occurrences.size > 1 || java.time.Duration.between(latest, now).toKotlinDuration() > misfireThreshold
            if (!late) {
                dispatch(entry, listOf(TaskRun(entry.name, latest)))
                continue
            }
            val runs = occurrences.map { TaskRun(entry.name, it) }
            when (entry.misfire) {
                Misfire.SKIP -> runs.forEach { skip(entry, it, SkipReason.MISFIRE) }
                Misfire.RUN_ONCE -> {
                    runs.dropLast(1).forEach { skip(entry, it, SkipReason.MISFIRE) }
                    dispatch(entry, listOf(runs.last()))
                }
                Misfire.RUN_ALL -> dispatch(entry, runs)
            }
        }
        return entries.values.mapNotNull { it.nextRun }.minOrNull()
    }

    private fun loop() {
        while (true) {
            if (lock.withLock { state == State.CLOSED }) return
            val now = clock.instant()
            val next = runDue(now)
            lock.withLock {
                if (state == State.CLOSED) return
                val untilNext = next?.let { java.time.Duration.between(now, it).toNanos() } ?: Long.MAX_VALUE
                changed.awaitNanos(minOf(untilNext, MAXIMUM_WAIT.inWholeNanoseconds).coerceAtLeast(1))
            }
        }
    }

    private fun dispatch(entry: Entry, runs: List<TaskRun>) {
        if (entry.running > 0) {
            when (entry.overlap) {
                Overlap.SKIP -> return runs.forEach { skip(entry, it, SkipReason.OVERLAP) }
                Overlap.QUEUE -> {
                    entry.queued?.let { skip(entry, it, SkipReason.OVERLAP) }
                    runs.dropLast(1).forEach { skip(entry, it, SkipReason.OVERLAP) }
                    entry.queued = runs.last()
                    return
                }
                Overlap.CONCURRENT -> Unit
            }
        }
        submit(entry, runs)
    }

    private fun submit(entry: Entry, runs: List<TaskRun>) {
        entry.running++
        activeRuns++
        try {
            executor.execute { execute(entry, runs) }
        } catch (exception: RejectedExecutionException) {
            finish(entry)
            for (run in runs) record(entry, TaskOutcome.Failed(run, Duration.ZERO, exception)) { listener.onFailed(run, exception, Duration.ZERO) }
        }
    }

    private fun execute(entry: Entry, runs: List<TaskRun>) {
        var error: Error? = null
        try {
            for (run in runs) {
                notify { listener.onStarted(run) }
                val started = System.nanoTime()
                try {
                    entry.action()
                    val duration = elapsedSince(started)
                    lock.withLock { record(entry, TaskOutcome.Succeeded(run, duration)) { listener.onSucceeded(run, duration) } }
                    flush()
                } catch (exception: Exception) {
                    val duration = elapsedSince(started)
                    lock.withLock { record(entry, TaskOutcome.Failed(run, duration, exception)) { listener.onFailed(run, exception, duration) } }
                    flush()
                } catch (thrown: Error) {
                    val duration = elapsedSince(started)
                    lock.withLock { record(entry, TaskOutcome.Failed(run, duration, thrown)) { listener.onFailed(run, thrown, duration) } }
                    flush()
                    error = thrown
                    break
                }
            }
        } finally {
            lock.withLock {
                finish(entry)
                val queued = entry.queued
                if (queued != null && !entry.removed && state != State.CLOSED) {
                    entry.queued = null
                    submit(entry, listOf(queued))
                }
            }
            flush()
        }
        error?.let { throw it }
    }

    private fun finish(entry: Entry) {
        entry.running--
        activeRuns--
        if (activeRuns == 0) idle.signalAll()
    }

    private fun skip(entry: Entry, run: TaskRun, reason: SkipReason) {
        record(entry, TaskOutcome.Skipped(run, reason)) { listener.onSkipped(run, reason) }
    }

    private fun record(entry: Entry, outcome: TaskOutcome, notification: () -> Unit) {
        entry.lastOutcome = outcome
        notifications += notification
    }

    /** Notifies the listener of recorded outcomes. It must be called without holding the lock, so that a slow listener cannot block scheduling. */
    private fun flush() {
        val pending = lock.withLock { notifications.toList().also { notifications.clear() } }
        pending.forEach(::notify)
    }

    private fun notify(notification: () -> Unit) {
        try {
            notification()
        } catch (exception: Exception) {
            logger.log(System.Logger.Level.WARNING, "A task listener threw.", exception)
        }
    }

    private fun elapsedSince(started: Long): Duration = (System.nanoTime() - started).nanoseconds
}

/**
 * Represents the builder for the initial tasks of a [TaskScheduler].
 */
@ScheduleDslMarker
public class TaskSchedulerBuilder internal constructor(private val scheduler: TaskScheduler) {

    /**
     * Adds a task.
     *
     * @param name The unique name of the task.
     * @param schedule The schedule of the task.
     * @param overlap What happens when an occurrence is due while a previous run is still running.
     * @param misfire What happens when occurrences were missed.
     * @param action The action that the task runs.
     */
    public fun task(name: String, schedule: Schedule, overlap: Overlap = Overlap.SKIP, misfire: Misfire = Misfire.RUN_ONCE, action: () -> Unit) {
        scheduler.add(name, schedule, overlap, misfire, action)
    }
}
