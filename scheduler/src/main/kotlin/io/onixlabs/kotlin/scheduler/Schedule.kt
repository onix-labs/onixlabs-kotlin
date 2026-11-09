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

import io.onixlabs.kotlin.core.time.DayOfMonth
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.time.Duration

/**
 * Represents a schedule: a rule that determines when something occurs.
 *
 * Implement this interface for rules that the built-in schedules cannot express, such as business-day calendars.
 */
public fun interface Schedule {

    /**
     * Gets the first occurrence of the current schedule strictly after the specified instant.
     *
     * @param instant The instant after which to find the next occurrence.
     * @return Returns the first occurrence strictly after the specified instant, or null if the schedule has no further occurrences.
     */
    public fun nextAfter(instant: Instant): Instant?
}

/**
 * Gets the occurrences of the current schedule strictly after the specified instant, in order.
 *
 * @param instant The instant after which to obtain occurrences.
 * @return Returns a lazy sequence of the occurrences strictly after the specified instant.
 */
public fun Schedule.occurrencesAfter(instant: Instant): Sequence<Instant> = generateSequence(nextAfter(instant)) { nextAfter(it) }

/**
 * Represents a schedule that occurs at a fixed interval of elapsed time, aligned to an anchor instant. Interval
 * schedules count real elapsed time, so they are not affected by time zones or daylight saving.
 *
 * @property interval The interval between occurrences.
 * @property anchor The instant to which occurrences are aligned; the default aligns occurrences to the epoch, so that
 * an interval of 15 minutes occurs on the hour and at quarter past, half past, and quarter to.
 */
public class IntervalSchedule(public val interval: Duration, public val anchor: Instant = Instant.EPOCH) : Schedule {

    init {
        require(interval.isPositive()) { "The interval of a schedule must be positive: $interval." }
    }

    private val intervalNanos: Long = interval.inWholeNanoseconds

    override fun nextAfter(instant: Instant): Instant {
        if (instant < anchor) return anchor
        val elapsed = java.time.Duration.between(anchor, instant).toNanos()
        return anchor.plusNanos(Math.multiplyExact(Math.floorDiv(elapsed, intervalNanos) + 1, intervalNanos))
    }

    override fun toString(): String = "every $interval"
}

/**
 * Represents a schedule that occurs at times of day on the dates that it matches, in a time zone.
 *
 * Times of day are converted to instants in the schedule's time zone. When a time falls in a daylight-saving gap, it
 * is shifted forward by the length of the gap; when it falls in an overlap, it occurs once, at the earlier offset.
 *
 * @property zone The time zone of the schedule.
 * @property times The times of day at which the schedule occurs, on each matching date.
 * @property every The frequency of the schedule's period: every day, week, month, or year when 1; every other when 2; and so on.
 * @property from The date from which the schedule's periods are counted, and before which it does not occur. It is
 * required when [every] is greater than 1.
 */
public abstract class CalendarSchedule internal constructor(
    public val zone: ZoneId,
    times: Set<LocalTime>,
    public val every: Int,
    public val from: LocalDate?,
) : Schedule {

    private companion object {
        const val MAXIMUM_DAYS = 366 * 8
    }

    /**
     * Gets the times of day at which the schedule occurs, in ascending order.
     */
    public val times: List<LocalTime> = times.sorted()

    init {
        require(times.isNotEmpty()) { "A schedule must occur at one or more times of day." }
        require(every >= 1) { "The frequency of a schedule must be at least 1: $every." }
        require(every == 1 || from != null) { "A schedule that occurs every $every periods requires a date from which to count them." }
    }

    /**
     * Determines whether the schedule occurs on the specified date, ignoring [from] and [every].
     *
     * @param date The date to check.
     * @return Returns true if the schedule occurs on the specified date; otherwise, false.
     */
    protected abstract fun matchesDate(date: LocalDate): Boolean

    /**
     * Gets the number of the schedule's periods between the [from] date and the specified date.
     *
     * @param from The date from which periods are counted.
     * @param date The date.
     * @return Returns the number of the schedule's periods between the dates.
     */
    protected abstract fun periodsBetween(from: LocalDate, date: LocalDate): Long

    override fun nextAfter(instant: Instant): Instant? {
        var date = instant.atZone(zone).toLocalDate().minusDays(1)
        repeat(MAXIMUM_DAYS) {
            if (matches(date)) {
                for (time in times) {
                    val occurrence = ZonedDateTime.ofLocal(date.atTime(time), zone, null).toInstant()
                    if (occurrence > instant) return occurrence
                }
            }
            date = date.plusDays(1)
        }
        return null
    }

    private fun matches(date: LocalDate): Boolean {
        if (from != null && date < from) return false
        if (every > 1 && periodsBetween(from!!, date) % every != 0L) return false
        return matchesDate(date)
    }
}

/**
 * Represents a schedule that occurs at times of day, every day or every [every] days.
 */
public class DailySchedule(
    zone: ZoneId,
    times: Set<LocalTime>,
    every: Int = 1,
    from: LocalDate? = null,
) : CalendarSchedule(zone, times, every, from) {

    override fun matchesDate(date: LocalDate): Boolean = true

    override fun periodsBetween(from: LocalDate, date: LocalDate): Long = ChronoUnit.DAYS.between(from, date)

    override fun toString(): String = "every ${if (every == 1) "day" else "$every days"} at ${times.joinToString()} ($zone)"
}

/**
 * Represents a schedule that occurs at times of day on days of the week, every week or every [every] weeks. Weeks
 * start on Monday.
 *
 * @property days The days of the week on which the schedule occurs.
 */
public class WeeklySchedule(
    zone: ZoneId,
    public val days: Set<DayOfWeek>,
    times: Set<LocalTime>,
    every: Int = 1,
    from: LocalDate? = null,
) : CalendarSchedule(zone, times, every, from) {

    init {
        require(days.isNotEmpty()) { "A weekly schedule must occur on one or more days of the week." }
    }

    override fun matchesDate(date: LocalDate): Boolean = date.dayOfWeek in days

    override fun periodsBetween(from: LocalDate, date: LocalDate): Long = ChronoUnit.WEEKS.between(from.weekStart(), date.weekStart())

    override fun toString(): String = "every ${if (every == 1) "week" else "$every weeks"} on ${days.sorted().joinToString()} at ${times.joinToString()} ($zone)"

    private fun LocalDate.weekStart(): LocalDate = with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
}

/**
 * Represents a schedule that occurs at times of day on days of the month, every month or every [every] months.
 *
 * @property days The ordinal days of the month on which the schedule occurs, such as the second Tuesday or the last Friday.
 * @property dates The dates of the month on which the schedule occurs, from 1 to 31, or from -1 (the last day) to -31.
 * A date that a month does not have, such as the 31st of April, does not occur in that month.
 */
public class MonthlySchedule(
    zone: ZoneId,
    times: Set<LocalTime>,
    public val days: Set<DayOfMonth> = emptySet(),
    public val dates: Set<Int> = emptySet(),
    every: Int = 1,
    from: LocalDate? = null,
) : CalendarSchedule(zone, times, every, from) {

    init {
        requireDays(days, dates)
    }

    override fun matchesDate(date: LocalDate): Boolean = matchesDayOfMonth(date, days, dates)

    override fun periodsBetween(from: LocalDate, date: LocalDate): Long = ChronoUnit.MONTHS.between(YearMonth.from(from), YearMonth.from(date))

    override fun toString(): String = "every ${if (every == 1) "month" else "$every months"} on ${describeDays(days, dates)} at ${times.joinToString()} ($zone)"
}

/**
 * Represents a schedule that occurs at times of day on days of the month, in months of the year, every year or every
 * [every] years.
 *
 * @property months The months in which the schedule occurs.
 * @property days The ordinal days of the month on which the schedule occurs, such as the last Friday.
 * @property dates The dates of the month on which the schedule occurs, from 1 to 31, or from -1 (the last day) to -31.
 */
public class YearlySchedule(
    zone: ZoneId,
    public val months: Set<Month>,
    times: Set<LocalTime>,
    public val days: Set<DayOfMonth> = emptySet(),
    public val dates: Set<Int> = emptySet(),
    every: Int = 1,
    from: LocalDate? = null,
) : CalendarSchedule(zone, times, every, from) {

    init {
        require(months.isNotEmpty()) { "A yearly schedule must occur in one or more months." }
        requireDays(days, dates)
    }

    override fun matchesDate(date: LocalDate): Boolean = date.month in months && matchesDayOfMonth(date, days, dates)

    override fun periodsBetween(from: LocalDate, date: LocalDate): Long = (date.year - from.year).toLong()

    override fun toString(): String {
        val frequency = if (every == 1) "year" else "$every years"
        return "every $frequency in ${months.sorted().joinToString()} on ${describeDays(days, dates)} at ${times.joinToString()} ($zone)"
    }
}

/**
 * Represents a schedule that occurs whenever any of its schedules occurs.
 *
 * @property schedules The schedules.
 */
public class CompositeSchedule(public val schedules: List<Schedule>) : Schedule {

    init {
        require(schedules.isNotEmpty()) { "A composite schedule requires one or more schedules." }
    }

    override fun nextAfter(instant: Instant): Instant? = schedules.mapNotNull { it.nextAfter(instant) }.minOrNull()

    override fun toString(): String = schedules.joinToString(" or ")
}

private fun requireDays(days: Set<DayOfMonth>, dates: Set<Int>) {
    require(days.isNotEmpty() || dates.isNotEmpty()) { "A schedule must occur on one or more days or dates of the month." }
    require(dates.all { it in 1..31 || it in -31..-1 }) { "Dates of the month must be from 1 to 31, or from -1 to -31: $dates." }
}

private fun matchesDayOfMonth(date: LocalDate, days: Set<DayOfMonth>, dates: Set<Int>): Boolean {
    val fromEnd = date.dayOfMonth - date.lengthOfMonth() - 1
    return date.dayOfMonth in dates || fromEnd in dates || days.any { it.matches(date) }
}

private fun describeDays(days: Set<DayOfMonth>, dates: Set<Int>): String = (days.map { it.toString() } + dates.map { it.toString() }).joinToString()
