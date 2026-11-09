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
package io.onixlabs.kotlin.core.time

import io.onixlabs.kotlin.core.time.DayOfMonthOccurrence.FIFTH
import io.onixlabs.kotlin.core.time.DayOfMonthOccurrence.FIRST
import io.onixlabs.kotlin.core.time.DayOfMonthOccurrence.FOURTH
import io.onixlabs.kotlin.core.time.DayOfMonthOccurrence.LAST
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.DayOfWeek
import java.time.LocalDate

class DayOfMonthTests {

    @Test
    fun `DayOfMonthOccurrence from should return the ordinal occurrence`() {

        // Arrange
        val first = LocalDate.of(2026, 1, 1)
        val fifth = LocalDate.of(2026, 1, 29)

        // Act
        val firstResult = DayOfMonthOccurrence.from(first)
        val fifthResult = DayOfMonthOccurrence.from(fifth)

        // Assert
        assertEquals(FIRST, firstResult)
        assertEquals(FIFTH, fifthResult)
    }

    @Test
    fun `DayOfMonthOccurrence occurrencesOf should include LAST for the last occurrence`() {

        // Arrange
        val fourthAndLast = LocalDate.of(2026, 1, 26)
        val fourthOnly = LocalDate.of(2026, 1, 22)

        // Act
        val fourthAndLastResult = DayOfMonthOccurrence.occurrencesOf(fourthAndLast)
        val fourthOnlyResult = DayOfMonthOccurrence.occurrencesOf(fourthOnly)

        // Assert
        assertEquals(setOf(FOURTH, LAST), fourthAndLastResult)
        assertEquals(setOf(FOURTH), fourthOnlyResult)
    }

    @Test
    fun `DayOfMonthOccurrence LAST should match only the last occurrence`() {

        // Arrange
        val last = LocalDate.of(2026, 2, 28)
        val notLast = LocalDate.of(2026, 2, 21)

        // Act
        val lastResult = LAST.matches(last)
        val notLastResult = LAST.matches(notLast)

        // Assert
        assertTrue(lastResult)
        assertFalse(notLastResult)
    }

    @Test
    fun `DayOfMonthOccurrence valueOf should throw for an invalid position`() {

        // Act and Assert
        assertThrows<IllegalArgumentException> { DayOfMonthOccurrence.valueOf(6) }
    }

    @Test
    fun `DayOfMonth last should match the last occurrence of the day in the month`() {

        // Arrange
        val lastMonday = DayOfMonth.last(DayOfWeek.MONDAY)

        // Act
        val lastMondayResult = lastMonday.matches(LocalDate.of(2026, 1, 26))
        val earlierMondayResult = lastMonday.matches(LocalDate.of(2026, 1, 19))
        val lastTuesdayResult = lastMonday.matches(LocalDate.of(2026, 1, 27))

        // Assert
        assertTrue(lastMondayResult)
        assertFalse(earlierMondayResult)
        assertFalse(lastTuesdayResult)
    }

    @Test
    fun `DayOfMonth from should match the date it was created from`() {

        // Arrange
        val date = LocalDate.of(2026, 1, 29)

        // Act
        val result = DayOfMonth.from(date)

        // Assert
        assertEquals(DayOfMonth(FIFTH, DayOfWeek.THURSDAY), result)
        assertTrue(result.matches(date))
    }
}
