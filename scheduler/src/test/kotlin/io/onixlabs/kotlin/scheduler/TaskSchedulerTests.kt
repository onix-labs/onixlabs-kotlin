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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TaskSchedulerTests {

    /** A clock whose time the test sets. */
    private class TestClock(var now: Instant) : Clock() {
        override fun instant(): Instant = now
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
    }

    /** An executor that holds submitted runs until the test runs them. */
    private class ManualExecutor : Executor {
        val pending = ArrayDeque<Runnable>()
        override fun execute(command: Runnable) {
            pending += command
        }
        fun runNext() = pending.removeFirst().run()
    }

    /** A listener that records every outcome as a line. */
    private class RecordingListener : TaskListener {
        val events = mutableListOf<String>()
        override fun onStarted(run: TaskRun) { events += "started ${run.task} ${run.scheduledFor}" }
        override fun onSucceeded(run: TaskRun, duration: Duration) { events += "succeeded ${run.task} ${run.scheduledFor}" }
        override fun onFailed(run: TaskRun, exception: Throwable, duration: Duration) { events += "failed ${run.task} ${run.scheduledFor} ${exception.message}" }
        override fun onSkipped(run: TaskRun, reason: SkipReason) { events += "skipped ${run.task} ${run.scheduledFor} $reason" }
    }

    private val start = Instant.parse("2026-01-01T00:00:00Z")
    private val everyMinute = schedule { every(1.minutes) }
    private val inline = Executor { it.run() }

    @Test
    fun `a due task should run once, and its next occurrence should be calculated`() {

        // Arrange
        val clock = TestClock(start)
        val listener = RecordingListener()
        val scheduler = TaskScheduler(inline, listener, clock) { task("tick", everyMinute) { } }
        scheduler.activate()

        // Act
        clock.now = start.plusSeconds(60)
        val next = scheduler.runDue(clock.now)

        // Assert
        assertEquals(listOf("started tick 2026-01-01T00:01:00Z", "succeeded tick 2026-01-01T00:01:00Z"), listener.events)
        assertEquals(Instant.parse("2026-01-01T00:02:00Z"), next)
        assertEquals(next, scheduler.tasks.single().nextRun)
    }

    @Test
    fun `a failing task should be reported, stay scheduled, and not affect other tasks`() {

        // Arrange
        val clock = TestClock(start)
        val listener = RecordingListener()
        var healthyRuns = 0
        val scheduler = TaskScheduler(inline, listener, clock) {
            task("broken", everyMinute) { throw IllegalStateException("disk full") }
            task("healthy", everyMinute) { healthyRuns++ }
        }
        scheduler.activate()

        // Act
        repeat(2) {
            clock.now = clock.now.plusSeconds(60)
            scheduler.runDue(clock.now)
        }

        // Assert
        assertEquals(2, healthyRuns)
        assertEquals(2, listener.events.count { it.startsWith("failed broken") && it.endsWith("disk full") })
        val broken = scheduler.tasks.first()
        assertTrue(broken.lastOutcome is TaskOutcome.Failed)
        assertEquals(Instant.parse("2026-01-01T00:03:00Z"), broken.nextRun)
    }

    @Test
    fun `missed occurrences should follow each task's misfire policy`() {

        // Arrange
        val clock = TestClock(start)
        val listener = RecordingListener()
        val runs = mutableMapOf("once" to 0, "skip" to 0, "all" to 0)
        val scheduler = TaskScheduler(inline, listener, clock) {
            task("once", everyMinute) { runs.merge("once", 1, Int::plus) }
            task("skip", everyMinute, misfire = Misfire.SKIP) { runs.merge("skip", 1, Int::plus) }
            task("all", everyMinute, misfire = Misfire.RUN_ALL) { runs.merge("all", 1, Int::plus) }
        }
        scheduler.activate()

        // Act: the machine sleeps through three occurrences.
        clock.now = start.plusSeconds(3 * 60 + 30)
        scheduler.runDue(clock.now)

        // Assert
        assertEquals(mapOf("once" to 1, "skip" to 0, "all" to 3), runs)
        assertEquals(2, listener.events.count { it.startsWith("skipped once") && it.endsWith("MISFIRE") })
        assertTrue(listener.events.contains("started once 2026-01-01T00:03:00Z"))
        assertEquals(3, listener.events.count { it.startsWith("skipped skip") })
        assertEquals(Instant.parse("2026-01-01T00:04:00Z"), scheduler.tasks.first().nextRun)
    }

    @Test
    fun `a slightly late occurrence should run normally`() {

        // Arrange
        val clock = TestClock(start)
        var runs = 0
        val scheduler = TaskScheduler(inline, RecordingListener(), clock) { task("tick", everyMinute, misfire = Misfire.SKIP) { runs++ } }
        scheduler.activate()

        // Act: 30 seconds late is within the default one-minute misfire threshold.
        clock.now = start.plusSeconds(90)
        scheduler.runDue(clock.now)

        // Assert
        assertEquals(1, runs)
    }

    @Test
    fun `overlapping occurrences should follow each task's overlap policy`() {

        // Arrange
        val clock = TestClock(start)
        val listener = RecordingListener()
        val executor = ManualExecutor()
        val scheduler = TaskScheduler(executor, listener, clock) {
            task("skip", everyMinute) { }
            task("queue", everyMinute, overlap = Overlap.QUEUE) { }
            task("concurrent", everyMinute, overlap = Overlap.CONCURRENT) { }
        }
        scheduler.activate()

        // Act: two occurrences fall due while the first runs have not finished.
        clock.now = start.plusSeconds(60)
        scheduler.runDue(clock.now)
        clock.now = start.plusSeconds(120)
        scheduler.runDue(clock.now)

        // Assert
        assertEquals(4, executor.pending.size)
        assertTrue(listener.events.contains("skipped skip 2026-01-01T00:02:00Z OVERLAP"))
        assertTrue(scheduler.tasks.all { it.isRunning })

        // Act: finishing the queued task's first run starts its queued run.
        while (executor.pending.isNotEmpty()) executor.runNext()

        // Assert
        assertTrue(listener.events.contains("succeeded queue 2026-01-01T00:02:00Z"))
        assertEquals(2, listener.events.count { it.startsWith("succeeded concurrent") })
        assertEquals(1, listener.events.count { it.startsWith("succeeded skip") })
        assertFalse(scheduler.tasks.any { it.isRunning })
    }

    @Test
    fun `tasks should be added and removed while the scheduler is running`() {

        // Arrange
        val clock = TestClock(start)
        val listener = RecordingListener()
        val scheduler = TaskScheduler(inline, listener, clock)
        scheduler.activate()

        // Act
        scheduler.add("late", everyMinute) { }
        val duplicate = assertThrows<IllegalArgumentException> { scheduler.add("late", everyMinute) { } }
        clock.now = start.plusSeconds(60)
        scheduler.runDue(clock.now)
        val removed = scheduler.remove("late")
        clock.now = start.plusSeconds(120)
        scheduler.runDue(clock.now)

        // Assert
        assertEquals("A task named 'late' already exists.", duplicate.message)
        assertTrue(removed)
        assertFalse(scheduler.remove("late"))
        assertEquals(1, listener.events.count { it.startsWith("succeeded late") })
        assertTrue(scheduler.tasks.isEmpty())
    }

    @Test
    fun `a listener that throws should not affect scheduling`() {

        // Arrange
        val clock = TestClock(start)
        var runs = 0
        val throwing = object : TaskListener {
            override fun onStarted(run: TaskRun): Unit = throw IllegalStateException("listener broke")
        }
        val scheduler = TaskScheduler(inline, throwing, clock) { task("tick", everyMinute) { runs++ } }
        scheduler.activate()

        // Act
        clock.now = start.plusSeconds(60)
        scheduler.runDue(clock.now)

        // Assert
        assertEquals(1, runs)
        assertTrue(scheduler.tasks.single().lastOutcome is TaskOutcome.Succeeded)
    }

    @Test
    fun `a closed scheduler should reject new tasks and a second start`() {

        // Arrange
        val scheduler = TaskScheduler(inline, RecordingListener())

        // Act
        scheduler.close()

        // Assert
        assertThrows<IllegalStateException> { scheduler.add("tick", everyMinute) { } }
        assertThrows<IllegalStateException> { scheduler.start() }
    }

    @Test
    fun `the scheduling thread should run tasks on a real clock and close cleanly`() {

        // Arrange
        val latch = CountDownLatch(3)
        val scheduler = TaskScheduler(listener = RecordingListener()) {
            task("fast", schedule { every(50.milliseconds) }) { latch.countDown() }
        }

        // Act
        scheduler.start()
        val ran = latch.await(5, TimeUnit.SECONDS)
        val closed = scheduler.close(5.seconds)

        // Assert
        assertTrue(ran)
        assertTrue(closed)
    }
}
