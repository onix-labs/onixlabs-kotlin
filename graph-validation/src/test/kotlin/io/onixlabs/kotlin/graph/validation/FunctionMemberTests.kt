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

import io.onixlabs.kotlin.graph.GraphPathFormat
import io.onixlabs.kotlin.graph.graphs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FunctionMemberTests {

    data class Person(val first: String, val last: String) {
        fun fullName(): String = "$first $last".trim()
    }

    @Test
    fun `rules should apply to the result of a member function, in both forms`() {

        // Arrange
        val reference = graphs { graph<Person> { validate { Person::fullName { mustHaveMinimumLength(3) } } } }
        val verb = graphs { graph<Person> { validate { function(Person::fullName) { mustHaveMinimumLength(3) } } } }

        // Act
        val result = reference.validate(Person("A", ""))

        // Assert
        assertEquals(listOf("Person.fullName() must have a length of at least 3."), result.failures.map { it.toString() })
        assertEquals("/fullName", result.failures.single().path.format(GraphPathFormat.JsonPointer))
        assertEquals(result.failures, verb.validate(Person("A", "")).failures)
        assertTrue(reference.validate(Person("Ada", "Lovelace")).isValid)
    }

    @Test
    fun `describe should include function members`() {

        // Arrange
        val registry = graphs { graph<Person> { validate { Person::fullName { mustNotBeBlank() } } } }

        // Assert
        assertEquals(listOf("Person.fullName() must not be blank."), registry.describe<Person>().failures.map { it.toString() })
    }

    @Test
    fun `validate should apply one-off rules without a graph or registry`() {

        // Act
        val result = validate(Person(" ", "Lovelace")) {
            Person::first { mustNotBeBlank() }
            Person::last { mustHaveMaximumLength(3) }
        }

        // Assert
        assertEquals(listOf("string.notBlank", "string.maximumLength"), result.failures.map { it.rule })
    }
}
