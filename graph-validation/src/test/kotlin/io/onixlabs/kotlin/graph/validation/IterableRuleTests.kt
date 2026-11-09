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

class IterableRuleTests {

    private val path = "IterableSubject.property"

    @Test
    fun `mustContain should fail when the element is missing and pass when it is present`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustContain(789) } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(123, 456)), rules)
        val valid = isValid(IterableSubject(listOf(123, 789)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("iterable.contains", failure.rule)
        assertEquals(mapOf("value" to 789), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustNotContain should fail when the element is present and pass when it is missing`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustNotContain(123) } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(123, 456)), rules)
        val valid = isValid(IterableSubject(listOf(456)), rules)

        // Assert
        assertEquals("iterable.notContains", failure.rule)
        assertTrue(valid)
    }

    @Test
    fun `mustContainAll should fail when any element is missing and pass when all are present`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustContainAll(listOf(123, 456, 789)) } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(123, 456)), rules)
        val valid = isValid(IterableSubject(listOf(789, 456, 123, 0)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("iterable.containsAll", failure.rule)
        assertEquals(mapOf("values" to listOf(123, 456, 789)), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustContainAny should fail when no element is present and pass when one is present`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustContainAny(listOf(1, 2, 3)) } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(123, 456)), rules)
        val valid = isValid(IterableSubject(listOf(123, 2)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("iterable.containsAny", failure.rule)
        assertEquals(mapOf("values" to listOf(1, 2, 3)), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustNotContainAny should fail when an element is present and pass when none is present`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustNotContainAny(listOf(1, 2, 3)) } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(123, 2)), rules)
        val valid = isValid(IterableSubject(listOf(123, 456)), rules)

        // Assert
        assertEquals("iterable.notContainsAny", failure.rule)
        assertTrue(valid)
    }

    @Test
    fun `mustContainExactly should fail when the elements differ and pass when they match in any order`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustContainExactly(listOf(123, 456, 789)) } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(123, 456)), rules)
        val valid = isValid(IterableSubject(listOf(789, 123, 456)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("iterable.containsExactly", failure.rule)
        assertEquals(mapOf("values" to listOf(123, 456, 789)), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustContainOrderedExactly should fail when the order differs and pass when the elements match in order`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustContainOrderedExactly(listOf(456, 123)) } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(123, 456)), rules)
        val valid = isValid(IterableSubject(listOf(456, 123)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("iterable.containsOrderedExactly", failure.rule)
        assertEquals(mapOf("values" to listOf(456, 123)), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustHaveCount should fail when the count differs and pass when it matches`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustHaveCount(3) } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(123, 456)), rules)
        val valid = isValid(IterableSubject(listOf(1, 2, 3)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("iterable.count", failure.rule)
        assertEquals(mapOf("count" to 3), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustHaveCountWithinRange should fail when the count is outside the range and pass when it is inside`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustHaveCountWithinRange(2..3) } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(1)), rules)
        val tooMany = isValid(IterableSubject(listOf(1, 2, 3, 4)), rules)
        val valid = isValid(IterableSubject(listOf(1, 2)), rules)

        // Assert
        assertEquals("iterable.countWithinRange", failure.rule)
        assertEquals(mapOf("minimum" to 2, "maximum" to 3), failure.parameters)
        assertEquals(false, tooMany)
        assertTrue(valid)
    }

    @Test
    fun `mustBeEmpty should fail when the iterable has elements and pass when it is empty`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustBeEmpty() } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(123, 456)), rules)
        val valid = isValid(IterableSubject(emptyList()), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("iterable.empty", failure.rule)
        assertTrue(valid)
    }

    @Test
    fun `mustNotBeEmpty should fail when the iterable is empty and pass when it has elements`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustNotBeEmpty() } }

        // Act
        val failure = firstFailureOf(IterableSubject(emptyList()), rules)
        val valid = isValid(IterableSubject(listOf(123)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("iterable.notEmpty", failure.rule)
        assertTrue(valid)
    }

    @Test
    fun `mustBeSingle should fail when the iterable has more than one element and pass when it has one`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustBeSingle() } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(123, 456)), rules)
        val empty = isValid(IterableSubject(emptyList()), rules)
        val valid = isValid(IterableSubject(listOf(123)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("iterable.single", failure.rule)
        assertEquals(false, empty)
        assertTrue(valid)
    }

    @Test
    fun `mustBeDistinct should fail when the iterable has duplicates and pass when it does not`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property { mustBeDistinct() } }

        // Act
        val failure = firstFailureOf(IterableSubject(listOf(123, 123, 456)), rules)
        val valid = isValid(IterableSubject(listOf(123, 456)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("iterable.distinct", failure.rule)
        assertTrue(valid)
    }

    @Test
    fun `each should apply rules to every element at an indexed path`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = { IterableSubject::property.each { mustBeGreaterThan(0) } }

        // Act
        val result = validate(IterableSubject(listOf(1, -1, 2, 0)), block = rules)

        // Assert
        assertEquals(listOf("$path[1]", "$path[3]"), result.failures.map { it.path.toString() })
        assertEquals(listOf("comparable.greaterThan", "comparable.greaterThan"), result.failures.map { it.rule })
    }

    @Test
    fun `iterable rules should pass on null`() {

        // Arrange
        val rules: ValidationBuilder<IterableSubject>.() -> Unit = {
            IterableSubject::property {
                mustContain(789)
                mustNotContain(789)
                mustContainAll(listOf(1, 2))
                mustContainAny(listOf(1, 2))
                mustNotContainAny(listOf(1, 2))
                mustContainExactly(listOf(1, 2))
                mustContainOrderedExactly(listOf(1, 2))
                mustHaveCount(3)
                mustHaveCountWithinRange(1, 2)
                mustBeEmpty()
                mustNotBeEmpty()
                mustBeSingle()
                mustBeDistinct()
            }
        }

        // Act
        val result = validate(IterableSubject(null), block = rules)

        // Assert
        assertTrue(result.isValid, result.toString())
    }
}
