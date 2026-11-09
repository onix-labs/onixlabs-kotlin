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
package io.onixlabs.kotlin.graph.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ComparableRuleTests {

    private val path = "ComparableSubject.property"

    @Test
    fun `mustBeWithinRange should fail when the value is outside the range and pass when it is inside`() {

        // Arrange
        val rules: ValidationBuilder<ComparableSubject>.() -> Unit = { ComparableSubject::property { mustBeWithinRange(123, 456) } }

        // Act
        val failure = firstFailureOf(ComparableSubject(100), rules)
        val above = validate(ComparableSubject(457), block = rules)
        val valid = listOf(123, 200, 456).all { isValid(ComparableSubject(it), rules) }

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("comparable.withinRange", failure.rule)
        assertEquals(mapOf("minimum" to 123, "maximum" to 456), failure.parameters)
        assertEquals(listOf("comparable.withinRange"), above.failures.map { it.rule })
        assertTrue(valid)
    }

    @Test
    fun `mustBeWithinRange with a range should behave like mustBeWithinRange with bounds`() {

        // Arrange
        val rules: ValidationBuilder<ComparableSubject>.() -> Unit = { ComparableSubject::property { mustBeWithinRange(123..456) } }

        // Act
        val failure = firstFailureOf(ComparableSubject(100), rules)
        val valid = isValid(ComparableSubject(200), rules)

        // Assert
        assertEquals("comparable.withinRange", failure.rule)
        assertEquals(mapOf("minimum" to 123, "maximum" to 456), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustNotBeWithinRange should fail when the value is inside the range and pass when it is outside`() {

        // Arrange
        val rules: ValidationBuilder<ComparableSubject>.() -> Unit = { ComparableSubject::property { mustNotBeWithinRange(123, 456) } }

        // Act
        val failure = firstFailureOf(ComparableSubject(200), rules)
        val valid = listOf(122, 457).all { isValid(ComparableSubject(it), rules) }

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("comparable.notWithinRange", failure.rule)
        assertEquals(mapOf("minimum" to 123, "maximum" to 456), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustBeGreaterThan should fail when the value is less than or equal and pass when it is greater`() {

        // Arrange
        val rules: ValidationBuilder<ComparableSubject>.() -> Unit = { ComparableSubject::property { mustBeGreaterThan(123) } }

        // Act
        val failure = firstFailureOf(ComparableSubject(100), rules)
        val equal = isValid(ComparableSubject(123), rules)
        val valid = isValid(ComparableSubject(124), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("comparable.greaterThan", failure.rule)
        assertEquals(mapOf("value" to 123), failure.parameters)
        assertEquals(false, equal)
        assertTrue(valid)
    }

    @Test
    fun `mustBeGreaterThanOrEqualTo should fail when the value is less and pass when it is equal or greater`() {

        // Arrange
        val rules: ValidationBuilder<ComparableSubject>.() -> Unit = { ComparableSubject::property { mustBeGreaterThanOrEqualTo(123) } }

        // Act
        val failure = firstFailureOf(ComparableSubject(100), rules)
        val valid = listOf(123, 124).all { isValid(ComparableSubject(it), rules) }

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("comparable.greaterThanOrEqualTo", failure.rule)
        assertEquals(mapOf("value" to 123), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustBeLessThan should fail when the value is greater than or equal and pass when it is less`() {

        // Arrange
        val rules: ValidationBuilder<ComparableSubject>.() -> Unit = { ComparableSubject::property { mustBeLessThan(123) } }

        // Act
        val failure = firstFailureOf(ComparableSubject(200), rules)
        val equal = isValid(ComparableSubject(123), rules)
        val valid = isValid(ComparableSubject(122), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("comparable.lessThan", failure.rule)
        assertEquals(mapOf("value" to 123), failure.parameters)
        assertEquals(false, equal)
        assertTrue(valid)
    }

    @Test
    fun `mustBeLessThanOrEqualTo should fail when the value is greater and pass when it is equal or less`() {

        // Arrange
        val rules: ValidationBuilder<ComparableSubject>.() -> Unit = { ComparableSubject::property { mustBeLessThanOrEqualTo(123) } }

        // Act
        val failure = firstFailureOf(ComparableSubject(200), rules)
        val valid = listOf(122, 123).all { isValid(ComparableSubject(it), rules) }

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("comparable.lessThanOrEqualTo", failure.rule)
        assertEquals(mapOf("value" to 123), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `comparable rules should apply to non-numeric comparables such as dates`() {

        // Arrange
        data class DateWindow(val start: LocalDate)
        val boundary = LocalDate.of(2000, 1, 1)

        // Act
        val failure = firstFailureOf(DateWindow(LocalDate.of(2000, 1, 2))) { DateWindow::start { mustBeLessThanOrEqualTo(boundary) } }
        val valid = isValid(DateWindow(boundary)) { DateWindow::start { mustBeLessThanOrEqualTo(boundary) } }

        // Assert
        assertEquals("comparable.lessThanOrEqualTo", failure.rule)
        assertEquals(mapOf("value" to boundary), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `comparable rules should pass on null`() {

        // Arrange
        val rules: ValidationBuilder<ComparableSubject>.() -> Unit = {
            ComparableSubject::property {
                mustBeWithinRange(123, 456)
                mustNotBeWithinRange(0, 1000)
                mustBeGreaterThan(123)
                mustBeGreaterThanOrEqualTo(123)
                mustBeLessThan(123)
                mustBeLessThanOrEqualTo(123)
            }
        }

        // Act
        val valid = isValid(ComparableSubject(null), rules)

        // Assert
        assertTrue(valid)
    }
}
