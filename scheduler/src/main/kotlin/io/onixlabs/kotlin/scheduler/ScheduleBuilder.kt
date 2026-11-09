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
import java.time.ZoneId
import kotlin.time.Duration

/**
 * Marks the scheduler DSL, so that the builders of enclosing blocks cannot be called implicitly.
 */
@DslMarker
public annotation class ScheduleDslMarker

/**
 * Creates a schedule; for example, `schedule(ZoneId.of("Europe/London")) { monthly { on(second(TUESDAY)); at(9, 0) } }`.
 * A block that declares several rules creates a schedule that occurs whenever any of them occurs.
 *
 * @param zone The time zone in which times of day are interpreted. It is required by daily, weekly, monthly, and
 * yearly rules, and not by interval rules.
 * @param block The block that declares the schedule's rules.
 * @return Returns the schedule.
 */
public fun schedule(zone: ZoneId? = null, block: ScheduleBuilder.() -> Unit): Schedule {
    val schedules = ScheduleBuilder(zone).apply(block).build()
    return schedules.singleOrNull() ?: CompositeSchedule(schedules)
}

/**
 * Represents the builder for the rules of a schedule.
 *
 * @property zone The time zone in which times of day are interpreted.
 */
@ScheduleDslMarker
public class ScheduleBuilder internal constructor(private val zone: ZoneId?) {

    private val schedules: MutableList<Schedule> = mutableListOf()

    /**
     * Occurs at a fixed interval of elapsed time; for example, `every(15.minutes)`.
     *
     * @param interval The interval between occurrences.
     * @param from The instant to which occurrences are aligned; the default aligns them to the epoch.
     */
    public fun every(interval: Duration, from: Instant = Instant.EPOCH) {
        schedules += IntervalSchedule(interval, from)
    }

    /**
     * Occurs at times of day, every day or every [every] days.
     *
     * @param every The number of days between occurrences.
     * @param from The date from which days are counted, which is required when [every] is greater than 1.
     * @param block The block that declares the times of day.
     */
    public fun daily(every: Int = 1, from: LocalDate? = null, block: DailyScheduleBuilder.() -> Unit) {
        val builder = DailyScheduleBuilder().apply(block)
        schedules += DailySchedule(requireZone("daily"), builder.times, every, from)
    }

    /**
     * Occurs at times of day on days of the week, every week or every [every] weeks.
     *
     * @param every The number of weeks between occurrences.
     * @param from The date from which weeks are counted, which is required when [every] is greater than 1.
     * @param block The block that declares the days of the week and times of day.
     */
    public fun weekly(every: Int = 1, from: LocalDate? = null, block: WeeklyScheduleBuilder.() -> Unit) {
        val builder = WeeklyScheduleBuilder().apply(block)
        schedules += WeeklySchedule(requireZone("weekly"), builder.days, builder.times, every, from)
    }

    /**
     * Occurs at times of day on days of the month, every month or every [every] months.
     *
     * @param every The number of months between occurrences.
     * @param from The date from which months are counted, which is required when [every] is greater than 1.
     * @param block The block that declares the days of the month and times of day.
     */
    public fun monthly(every: Int = 1, from: LocalDate? = null, block: MonthlyScheduleBuilder.() -> Unit) {
        val builder = MonthlyScheduleBuilder().apply(block)
        schedules += MonthlySchedule(requireZone("monthly"), builder.times, builder.days, builder.dates, every, from)
    }

    /**
     * Occurs at times of day on days of the month, in months of the year, every year or every [every] years.
     *
     * @param every The number of years between occurrences.
     * @param from The date from which years are counted, which is required when [every] is greater than 1.
     * @param block The block that declares the months, days of the month, and times of day.
     */
    public fun yearly(every: Int = 1, from: LocalDate? = null, block: YearlyScheduleBuilder.() -> Unit) {
        val builder = YearlyScheduleBuilder().apply(block)
        schedules += YearlySchedule(requireZone("yearly"), builder.months, builder.times, builder.days, builder.dates, every, from)
    }

    /**
     * Adds an existing schedule to the current schedule.
     *
     * @param schedule The schedule to add.
     */
    public fun include(schedule: Schedule) {
        schedules += schedule
    }

    internal fun build(): List<Schedule> {
        require(schedules.isNotEmpty()) { "A schedule must declare one or more rules." }
        return schedules.toList()
    }

    private fun requireZone(rule: String): ZoneId {
        return requireNotNull(zone) { "A $rule schedule requires a time zone; for example, schedule(ZoneId.of(\"Europe/London\")) { ... }." }
    }
}

/**
 * Represents the builder for the times of day of a schedule.
 */
@ScheduleDslMarker
public abstract class TimesOfDayBuilder internal constructor() {

    internal val times: MutableSet<LocalTime> = linkedSetOf()

    /**
     * Occurs at the specified time of day.
     *
     * @param hour The hour, from 0 to 23.
     * @param minute The minute, from 0 to 59.
     * @param second The second, from 0 to 59.
     */
    public fun at(hour: Int, minute: Int = 0, second: Int = 0) {
        times += LocalTime.of(hour, minute, second)
    }

    /**
     * Occurs at the specified times of day.
     *
     * @param times The times of day.
     */
    public fun at(vararg times: LocalTime) {
        this.times += times
    }
}

/**
 * Represents the builder for a daily schedule.
 */
public class DailyScheduleBuilder internal constructor() : TimesOfDayBuilder()

/**
 * Represents the builder for a weekly schedule.
 */
public class WeeklyScheduleBuilder internal constructor() : TimesOfDayBuilder() {

    internal val days: MutableSet<DayOfWeek> = linkedSetOf()

    /**
     * Occurs on the specified days of the week.
     *
     * @param days The days of the week.
     */
    public fun on(vararg days: DayOfWeek) {
        this.days += days
    }
}

/**
 * Represents the builder for the days of the month of a schedule, by ordinal day (such as `second(TUESDAY)`) or by date.
 */
public abstract class DaysOfMonthBuilder internal constructor() : TimesOfDayBuilder() {

    internal val days: MutableSet<DayOfMonth> = linkedSetOf()
    internal val dates: MutableSet<Int> = linkedSetOf()

    /**
     * Occurs on the specified ordinal days of the month.
     *
     * @param days The ordinal days of the month, such as `second(TUESDAY)` or `last(FRIDAY)`.
     */
    public fun on(vararg days: DayOfMonth) {
        this.days += days
    }

    /**
     * Occurs on the specified dates of the month, from 1 to 31, or from -1 (the last day) to -31. A date that a month
     * does not have, such as the 31st of April, does not occur in that month.
     *
     * @param dates The dates of the month.
     */
    public fun onDates(vararg dates: Int) {
        this.dates += dates.toList()
    }

    /** Gets the first occurrence of the specified day of the week in the month. */
    public fun first(day: DayOfWeek): DayOfMonth = DayOfMonth.first(day)

    /** Gets the second occurrence of the specified day of the week in the month. */
    public fun second(day: DayOfWeek): DayOfMonth = DayOfMonth.second(day)

    /** Gets the third occurrence of the specified day of the week in the month. */
    public fun third(day: DayOfWeek): DayOfMonth = DayOfMonth.third(day)

    /** Gets the fourth occurrence of the specified day of the week in the month. */
    public fun fourth(day: DayOfWeek): DayOfMonth = DayOfMonth.fourth(day)

    /** Gets the fifth occurrence of the specified day of the week in the month, which not every month has. */
    public fun fifth(day: DayOfWeek): DayOfMonth = DayOfMonth.fifth(day)

    /** Gets the last occurrence of the specified day of the week in the month. */
    public fun last(day: DayOfWeek): DayOfMonth = DayOfMonth.last(day)
}

/**
 * Represents the builder for a monthly schedule.
 */
public class MonthlyScheduleBuilder internal constructor() : DaysOfMonthBuilder()

/**
 * Represents the builder for a yearly schedule.
 */
public class YearlyScheduleBuilder internal constructor() : DaysOfMonthBuilder() {

    internal val months: MutableSet<Month> = linkedSetOf()

    /**
     * Occurs in the specified months.
     *
     * @param months The months.
     */
    public fun months(vararg months: Month) {
        this.months += months
    }
}
