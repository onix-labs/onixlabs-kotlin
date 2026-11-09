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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.TUESDAY
import java.time.Instant
import java.time.LocalDate
import java.time.Month.DECEMBER
import java.time.Month.JUNE
import java.time.Month.MARCH
import java.time.Month.SEPTEMBER
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.minutes

class ScheduleTests {

    private val london = ZoneId.of("Europe/London")
    private val newYork = ZoneId.of("America/New_York")

    private fun Schedule.next(count: Int, after: String): List<String> = occurrencesAfter(Instant.parse(after)).take(count).map { it.toString() }.toList()

    @Test
    fun `an interval schedule should be aligned to its anchor`() {

        // Arrange
        val schedule = schedule { every(15.minutes) }

        // Assert
        assertEquals(listOf("2026-01-01T10:15:00Z", "2026-01-01T10:30:00Z"), schedule.next(2, "2026-01-01T10:07:12Z"))
        assertEquals("2026-01-01T10:30:00Z", schedule.nextAfter(Instant.parse("2026-01-01T10:15:00Z")).toString())
    }

    @Test
    fun `a monthly schedule should occur on ordinal days, in the schedule's zone`() {

        // Arrange
        val schedule = schedule(london) { monthly { on(second(TUESDAY)); at(9, 0) } }

        // Assert
        assertEquals(listOf("2026-01-13T09:00:00Z", "2026-02-10T09:00:00Z"), schedule.next(2, "2026-01-01T00:00:00Z"))
        assertEquals("2026-06-09T08:00:00Z", schedule.nextAfter(Instant.parse("2026-06-01T00:00:00Z")).toString())
    }

    @Test
    fun `a weekly schedule should occur every other week from its anchor`() {

        // Arrange
        val schedule = schedule(london) { weekly(every = 2, from = LocalDate.of(2026, 1, 6)) { on(TUESDAY); at(9, 0) } }

        // Assert
        assertEquals(listOf("2026-01-06T09:00:00Z", "2026-01-20T09:00:00Z", "2026-02-03T09:00:00Z"), schedule.next(3, "2026-01-01T00:00:00Z"))
    }

    @Test
    fun `a yearly schedule should occur on the last Friday of the specified months`() {

        // Arrange
        val schedule = schedule(newYork) { yearly { months(MARCH, JUNE, SEPTEMBER, DECEMBER); on(last(FRIDAY)); at(17, 30) } }

        // Assert
        assertEquals(listOf("2026-03-27T21:30:00Z", "2026-06-26T21:30:00Z"), schedule.next(2, "2026-01-01T00:00:00Z"))
    }

    @Test
    fun `a monthly schedule should count months correctly across a year boundary`() {

        // Arrange
        val schedule = schedule(ZoneOffset.UTC) { monthly(every = 2, from = LocalDate.of(2025, 11, 1)) { onDates(1); at(0, 0) } }

        // Assert
        assertEquals(
            listOf("2025-11-01T00:00:00Z", "2026-01-01T00:00:00Z", "2026-03-01T00:00:00Z"),
            schedule.next(3, "2025-10-15T00:00:00Z"),
        )
    }

    @Test
    fun `dates of the month should count from the end when negative, and skip months without the date`() {

        // Arrange
        val lastDay = schedule(ZoneOffset.UTC) { monthly { onDates(-1); at(0, 0) } }
        val thirtyFirst = schedule(ZoneOffset.UTC) { monthly { onDates(31); at(0, 0) } }

        // Assert
        assertEquals("2026-02-28T00:00:00Z", lastDay.nextAfter(Instant.parse("2026-02-01T00:00:00Z")).toString())
        assertEquals("2026-05-31T00:00:00Z", thirtyFirst.nextAfter(Instant.parse("2026-04-01T00:00:00Z")).toString())
    }

    @Test
    fun `a time in a daylight-saving gap should be shifted forward`() {

        // Arrange
        val schedule = schedule(london) { daily { at(1, 30) } }

        // Assert: 01:30 does not exist on 29 March 2026, so it runs at 02:30 BST (01:30Z).
        assertEquals(listOf("2026-03-29T01:30:00Z", "2026-03-30T00:30:00Z"), schedule.next(2, "2026-03-28T12:00:00Z"))
    }

    @Test
    fun `a time in a daylight-saving overlap should occur once, at the earlier offset`() {

        // Arrange
        val schedule = schedule(london) { daily { at(1, 30) } }

        // Assert: 01:30 happens twice on 25 October 2026; only the first (BST, 00:30Z) occurs.
        assertEquals(listOf("2026-10-25T00:30:00Z", "2026-10-26T01:30:00Z"), schedule.next(2, "2026-10-24T12:00:00Z"))
    }

    @Test
    fun `a schedule with several rules should occur whenever any of them occurs`() {

        // Arrange
        val schedule = schedule(ZoneOffset.UTC) {
            daily { at(9, 0) }
            weekly { on(FRIDAY); at(17, 0) }
        }

        // Assert: 2 January 2026 is a Friday.
        assertEquals(listOf("2026-01-02T09:00:00Z", "2026-01-02T17:00:00Z", "2026-01-03T09:00:00Z"), schedule.next(3, "2026-01-01T12:00:00Z"))
    }

    @Test
    fun `a schedule that can never occur should have no next occurrence`() {

        // Arrange
        val schedule = schedule(ZoneOffset.UTC) { yearly { months(java.time.Month.FEBRUARY); onDates(30); at(0, 0) } }

        // Assert
        assertNull(schedule.nextAfter(Instant.parse("2026-01-01T00:00:00Z")))
    }

    @Test
    fun `schedules should reject invalid declarations`() {

        // Assert
        assertThrows<IllegalArgumentException> { schedule { daily { at(9, 0) } } }
        assertThrows<IllegalArgumentException> { schedule(london) { weekly(every = 2) { on(TUESDAY); at(9, 0) } } }
        assertThrows<IllegalArgumentException> { schedule(london) { monthly { at(9, 0) } } }
        assertThrows<IllegalArgumentException> { schedule(london) { daily { } } }
        assertThrows<IllegalArgumentException> { schedule { } }
    }
}
