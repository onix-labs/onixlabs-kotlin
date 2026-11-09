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

import java.time.Instant
import kotlin.time.Duration

/**
 * Specifies what happens when a task's next occurrence is due while a previous run is still running.
 */
public enum class Overlap {

    /**
     * The occurrence does not run, and is reported as skipped.
     */
    SKIP,

    /**
     * The occurrence runs as soon as the previous run finishes. At most one occurrence waits; further occurrences are skipped.
     */
    QUEUE,

    /**
     * The occurrence runs alongside the previous run.
     */
    CONCURRENT,
}

/**
 * Specifies what happens when the scheduler finds that occurrences were missed, for example after the machine slept.
 */
public enum class Misfire {

    /**
     * Missed occurrences do not run, and are reported as skipped.
     */
    SKIP,

    /**
     * The task runs once, however many occurrences were missed.
     */
    RUN_ONCE,

    /**
     * The task runs once for each missed occurrence, one after another.
     */
    RUN_ALL,
}

/**
 * Specifies why an occurrence was skipped.
 */
public enum class SkipReason {

    /**
     * A previous run was still running; see [Overlap].
     */
    OVERLAP,

    /**
     * The occurrence was missed; see [Misfire].
     */
    MISFIRE,
}

/**
 * Represents a run of a task.
 *
 * @property task The name of the task.
 * @property scheduledFor The occurrence for which the task runs.
 */
public data class TaskRun(public val task: String, public val scheduledFor: Instant)

/**
 * Represents the outcome of a run of a task.
 */
public sealed interface TaskOutcome {

    /**
     * Represents a run that completed.
     *
     * @property run The run.
     * @property duration The duration of the run.
     */
    public data class Succeeded(public val run: TaskRun, public val duration: Duration) : TaskOutcome

    /**
     * Represents a run that threw.
     *
     * @property run The run.
     * @property duration The duration of the run.
     * @property exception The exception that the run threw.
     */
    public data class Failed(public val run: TaskRun, public val duration: Duration, public val exception: Throwable) : TaskOutcome

    /**
     * Represents an occurrence that did not run.
     *
     * @property run The run that did not happen.
     * @property reason The reason the occurrence did not run.
     */
    public data class Skipped(public val run: TaskRun, public val reason: SkipReason) : TaskOutcome
}

/**
 * Represents the status of a scheduled task.
 *
 * @property name The name of the task.
 * @property schedule The schedule of the task.
 * @property nextRun The next occurrence of the task, or null if it has no further occurrences or the scheduler is not running.
 * @property lastOutcome The outcome of the task's most recent run or skipped occurrence, or null if there has been none.
 * @property isRunning Determines whether the task is running.
 */
public data class TaskStatus(
    public val name: String,
    public val schedule: Schedule,
    public val nextRun: Instant?,
    public val lastOutcome: TaskOutcome?,
    public val isRunning: Boolean,
)

/**
 * Receives the outcomes of task runs, for example to record metrics or raise alerts. Every method has an empty
 * default, so a listener implements only what it needs. Exceptions thrown by a listener are logged and otherwise ignored.
 */
public interface TaskListener {

    /**
     * Called when a run starts.
     *
     * @param run The run.
     */
    public fun onStarted(run: TaskRun): Unit = Unit

    /**
     * Called when a run completes.
     *
     * @param run The run.
     * @param duration The duration of the run.
     */
    public fun onSucceeded(run: TaskRun, duration: Duration): Unit = Unit

    /**
     * Called when a run throws. The task remains scheduled.
     *
     * @param run The run.
     * @param exception The exception that the run threw.
     * @param duration The duration of the run.
     */
    public fun onFailed(run: TaskRun, exception: Throwable, duration: Duration): Unit = Unit

    /**
     * Called when an occurrence does not run.
     *
     * @param run The run that did not happen.
     * @param reason The reason the occurrence did not run.
     */
    public fun onSkipped(run: TaskRun, reason: SkipReason): Unit = Unit

    public companion object {

        /**
         * Gets a listener that logs outcomes through [System.Logger]: failures as warnings, skipped occurrences as
         * information, and starts and completions as debug messages.
         */
        public val Logging: TaskListener = object : TaskListener {
            private val logger = System.getLogger("io.onixlabs.kotlin.scheduler")

            override fun onStarted(run: TaskRun) {
                logger.log(System.Logger.Level.DEBUG, "Task '${run.task}' started for ${run.scheduledFor}.")
            }

            override fun onSucceeded(run: TaskRun, duration: Duration) {
                logger.log(System.Logger.Level.DEBUG, "Task '${run.task}' succeeded for ${run.scheduledFor} in $duration.")
            }

            override fun onFailed(run: TaskRun, exception: Throwable, duration: Duration) {
                logger.log(System.Logger.Level.WARNING, "Task '${run.task}' failed for ${run.scheduledFor} after $duration.", exception)
            }

            override fun onSkipped(run: TaskRun, reason: SkipReason) {
                logger.log(System.Logger.Level.INFO, "Task '${run.task}' skipped ${run.scheduledFor}: ${reason.name.lowercase()}.")
            }
        }
    }
}
