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

class MapRuleTests {

    private val path = "MapSubject.property"

    @Test
    fun `mustContainKey should fail when the key is missing and pass when it is present`() {

        // Arrange
        val rules: ValidationBuilder<MapSubject>.() -> Unit = { MapSubject::property { mustContainKey("goodbye") } }

        // Act
        val failure = firstFailureOf(MapSubject(mapOf("hello" to 123)), rules)
        val valid = isValid(MapSubject(mapOf("goodbye" to 123)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("map.containsKey", failure.rule)
        assertEquals(mapOf("key" to "goodbye"), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustNotContainKey should fail when the key is present and pass when it is missing`() {

        // Arrange
        val rules: ValidationBuilder<MapSubject>.() -> Unit = { MapSubject::property { mustNotContainKey("hello") } }

        // Act
        val failure = firstFailureOf(MapSubject(mapOf("hello" to 123)), rules)
        val valid = isValid(MapSubject(mapOf("goodbye" to 123)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("map.notContainsKey", failure.rule)
        assertEquals(mapOf("key" to "hello"), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustContainValue should fail when the value is missing and pass when it is present`() {

        // Arrange
        val rules: ValidationBuilder<MapSubject>.() -> Unit = { MapSubject::property { mustContainValue(456) } }

        // Act
        val failure = firstFailureOf(MapSubject(mapOf("hello" to 123)), rules)
        val valid = isValid(MapSubject(mapOf("hello" to 456)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("map.containsValue", failure.rule)
        assertEquals(mapOf("value" to 456), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustNotContainValue should fail when the value is present and pass when it is missing`() {

        // Arrange
        val rules: ValidationBuilder<MapSubject>.() -> Unit = { MapSubject::property { mustNotContainValue(123) } }

        // Act
        val failure = firstFailureOf(MapSubject(mapOf("hello" to 123)), rules)
        val valid = isValid(MapSubject(mapOf("hello" to 456)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("map.notContainsValue", failure.rule)
        assertEquals(mapOf("value" to 123), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustContainEntry should fail when the entry is missing and pass when it is present`() {

        // Arrange
        val rules: ValidationBuilder<MapSubject>.() -> Unit = { MapSubject::property { mustContainEntry("goodbye", 456) } }

        // Act
        val failure = firstFailureOf(MapSubject(mapOf("hello" to 123)), rules)
        val wrongValue = isValid(MapSubject(mapOf("goodbye" to 123)), rules)
        val valid = isValid(MapSubject(mapOf("hello" to 123, "goodbye" to 456)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("map.containsEntry", failure.rule)
        assertEquals(mapOf("key" to "goodbye", "value" to 456), failure.parameters)
        assertEquals(false, wrongValue)
        assertTrue(valid)
    }

    @Test
    fun `mustNotContainEntry should fail when the entry is present and pass when it is missing`() {

        // Arrange
        val rules: ValidationBuilder<MapSubject>.() -> Unit = { MapSubject::property { mustNotContainEntry("hello", 123) } }

        // Act
        val failure = firstFailureOf(MapSubject(mapOf("hello" to 123)), rules)
        val valid = isValid(MapSubject(mapOf("hello" to 456)), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("map.notContainsEntry", failure.rule)
        assertEquals(mapOf("key" to "hello", "value" to 123), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `map size rules should fail and pass on the number of entries`() {

        // Arrange
        val empty: ValidationBuilder<MapSubject>.() -> Unit = { MapSubject::property { mustBeEmpty() } }
        val notEmpty: ValidationBuilder<MapSubject>.() -> Unit = { MapSubject::property { mustNotBeEmpty() } }
        val count: ValidationBuilder<MapSubject>.() -> Unit = { MapSubject::property { mustHaveCountWithinRange(1, 2) } }

        // Act
        val emptyFailure = firstFailureOf(MapSubject(mapOf("a" to 1)), empty)
        val notEmptyFailure = firstFailureOf(MapSubject(emptyMap()), notEmpty)
        val countFailure = firstFailureOf(MapSubject(mapOf("a" to 1, "b" to 2, "c" to 3)), count)

        // Assert
        assertEquals("map.empty", emptyFailure.rule)
        assertEquals("map.notEmpty", notEmptyFailure.rule)
        assertEquals("map.countWithinRange", countFailure.rule)
        assertTrue(isValid(MapSubject(emptyMap()), empty))
        assertTrue(isValid(MapSubject(mapOf("a" to 1)), notEmpty))
        assertTrue(isValid(MapSubject(mapOf("a" to 1, "b" to 2)), count))
    }

    @Test
    fun `keys and values should apply rules to every key and value at entry paths`() {

        // Arrange
        val rules: ValidationBuilder<MapSubject>.() -> Unit = {
            MapSubject::property.keys { mustNotStartWith("$") }
            MapSubject::property.values { mustSatisfy({ it is Int }) }
        }

        // Act
        val result = validate(MapSubject(mapOf("\$bad" to 1, "good" to "text")), block = rules)

        // Assert
        assertEquals(
            listOf("$path.keys[\"\$bad\"] string.notStartsWith", "$path[\"good\"] generic.satisfies"),
            result.failures.map { "${it.path} ${it.rule}" },
        )
    }

    @Test
    fun `map rules should pass on null`() {

        // Arrange
        val rules: ValidationBuilder<MapSubject>.() -> Unit = {
            MapSubject::property {
                mustContainKey("hello")
                mustNotContainKey("hello")
                mustContainValue(123)
                mustNotContainValue(123)
                mustContainEntry("hello", 123)
                mustNotContainEntry("hello", 123)
                mustBeEmpty()
                mustNotBeEmpty()
                mustHaveCountWithinRange(1, 2)
            }
        }

        // Act
        val result = validate(MapSubject(null), block = rules)

        // Assert
        assertTrue(result.isValid, result.toString())
    }
}
