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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StringRuleTests {

    private val path = "StringSubject.property"

    private fun assertFails(rule: String, value: String?, rules: ValidationBuilder<StringSubject>.() -> Unit): ValidationFailure {
        val failure = firstFailureOf(StringSubject(value), rules)
        assertEquals(path, failure.path.toString())
        assertEquals(rule, failure.rule)
        return failure
    }

    private fun assertPasses(vararg values: String?, rules: ValidationBuilder<StringSubject>.() -> Unit) {
        for (value in values) assertTrue(isValid(StringSubject(value), rules), "Expected \"$value\" to pass.")
    }

    @Test
    fun `mustBeNullOrBlank should fail when the value has content and pass when it is null or blank`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustBeNullOrBlank() } }

        // Act
        val failure = assertFails("string.nullOrBlank", "Hello World", rules)

        // Assert
        assertEquals("must be null or blank.", failure.message)
        assertPasses(null, "", " ", rules = rules)
    }

    @Test
    fun `mustNotBeNullOrBlank should fail when the value is null or blank and pass when it has content`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustNotBeNullOrBlank() } }

        // Act
        val failure = assertFails("string.notNullOrBlank", null, rules)

        // Assert
        assertEquals("must not be null or blank.", failure.message)
        assertFails("string.notNullOrBlank", " ", rules)
        assertPasses("Hello World", rules = rules)
    }

    @Test
    fun `mustBeNullOrEmpty should fail when the value has content and pass when it is null or empty`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustBeNullOrEmpty() } }

        // Act
        val failure = assertFails("string.nullOrEmpty", "Hello World", rules)

        // Assert
        assertEquals("must be null or empty.", failure.message)
        assertFails("string.nullOrEmpty", " ", rules)
        assertPasses(null, "", rules = rules)
    }

    @Test
    fun `mustNotBeNullOrEmpty should fail when the value is null or empty and pass when it has content`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustNotBeNullOrEmpty() } }

        // Act
        val failure = assertFails("string.notNullOrEmpty", null, rules)

        // Assert
        assertEquals("must not be null or empty.", failure.message)
        assertFails("string.notNullOrEmpty", "", rules)
        assertPasses(" ", "Hello World", rules = rules)
    }

    @Test
    fun `mustBeBlank should fail when the value has content and pass when it is blank`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustBeBlank() } }

        // Act
        val failure = assertFails("string.blank", "Hello World", rules)

        // Assert
        assertEquals("must be blank.", failure.message)
        assertPasses("", " ", rules = rules)
    }

    @Test
    fun `mustNotBeBlank should fail when the value is blank and pass when it has content`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustNotBeBlank() } }

        // Act
        val failure = assertFails("string.notBlank", "", rules)

        // Assert
        assertEquals("must not be blank.", failure.message)
        assertFails("string.notBlank", " ", rules)
        assertPasses("Hello World", rules = rules)
    }

    @Test
    fun `mustBeEmpty should fail when the value is not empty and pass when it is empty`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustBeEmpty() } }

        // Act
        val failure = assertFails("string.empty", " ", rules)

        // Assert
        assertEquals("must be empty.", failure.message)
        assertPasses("", rules = rules)
    }

    @Test
    fun `mustNotBeEmpty should fail when the value is empty and pass when it is not empty`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustNotBeEmpty() } }

        // Act
        val failure = assertFails("string.notEmpty", "", rules)

        // Assert
        assertEquals("must not be empty.", failure.message)
        assertPasses(" ", "Hello World", rules = rules)
    }

    @Test
    fun `mustContain should fail when the value does not contain the substring and pass when it does`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustContain("Awesome") } }

        // Act
        val failure = assertFails("string.contains", "Hello World", rules)

        // Assert
        assertEquals(mapOf("value" to "Awesome"), failure.parameters)
        assertPasses("Hello Awesome World", rules = rules)
    }

    @Test
    fun `mustNotContain should fail when the value contains the substring and pass when it does not`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustNotContain("World") } }

        // Act
        val failure = assertFails("string.notContains", "Hello World", rules)

        // Assert
        assertEquals(mapOf("value" to "World"), failure.parameters)
        assertPasses("Hello Mars", rules = rules)
    }

    @Test
    fun `mustContainAll should fail when any substring is missing and pass when all are present`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustContainAll(listOf("Cat", "Dog", "Fox")) } }

        // Act
        val failure = assertFails("string.containsAll", "Cat Dog", rules)

        // Assert
        assertEquals(mapOf("values" to listOf("Cat", "Dog", "Fox")), failure.parameters)
        assertPasses("Fox Cat Dog", rules = rules)
    }

    @Test
    fun `mustContainAny should fail when no substring is present and pass when one is present`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustContainAny(listOf("Dog", "Fox")) } }

        // Act
        val failure = assertFails("string.containsAny", "Cat", rules)

        // Assert
        assertEquals(mapOf("values" to listOf("Dog", "Fox")), failure.parameters)
        assertPasses("Cat Fox", rules = rules)
    }

    @Test
    fun `mustEndWith should fail when the value has a different suffix and pass when it has the suffix`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustEndWith("World") } }

        // Act
        val failure = assertFails("string.endsWith", "Hello Mars", rules)

        // Assert
        assertEquals(mapOf("value" to "World"), failure.parameters)
        assertPasses("Hello World", rules = rules)
    }

    @Test
    fun `mustNotEndWith should fail when the value has the suffix and pass when it does not`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustNotEndWith("World") } }

        // Act
        val failure = assertFails("string.notEndsWith", "Hello World", rules)

        // Assert
        assertEquals(mapOf("value" to "World"), failure.parameters)
        assertPasses("Hello Mars", rules = rules)
    }

    @Test
    fun `mustStartWith should fail when the value has a different prefix and pass when it has the prefix`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustStartWith("Goodbye") } }

        // Act
        val failure = assertFails("string.startsWith", "Hello World", rules)

        // Assert
        assertEquals(mapOf("value" to "Goodbye"), failure.parameters)
        assertPasses("Goodbye World", rules = rules)
    }

    @Test
    fun `mustNotStartWith should fail when the value has the prefix and pass when it does not`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustNotStartWith("Hello") } }

        // Act
        val failure = assertFails("string.notStartsWith", "Hello World", rules)

        // Assert
        assertEquals(mapOf("value" to "Hello"), failure.parameters)
        assertPasses("Goodbye World", rules = rules)
    }

    @Test
    fun `mustHaveLength should fail when the length differs and pass when it matches`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustHaveLength(100) } }

        // Act
        val failure = assertFails("string.length", "Hello World", rules)

        // Assert
        assertEquals(mapOf("length" to 100), failure.parameters)
        assertPasses("x".repeat(100), rules = rules)
    }

    @Test
    fun `mustHaveLengthWithinRange should fail when the value is shorter than the minimum`() {

        // The old library spelled this rule mustHaveLength(10..20); the new library calls it mustHaveLengthWithinRange.

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustHaveLengthWithinRange(10..20) } }

        // Act
        val failure = assertFails("string.lengthWithinRange", "Hello", rules)

        // Assert
        assertEquals(mapOf("minimum" to 10, "maximum" to 20), failure.parameters)
        assertPasses("x".repeat(10), "Hello World", "x".repeat(20), rules = rules)
    }

    @Test
    fun `mustHaveLengthWithinRange should fail when the value is longer than the maximum`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustHaveLengthWithinRange(10, 20) } }

        // Act
        val failure = assertFails("string.lengthWithinRange", "The quick brown fox jumps over the lazy dog", rules)

        // Assert
        assertEquals(mapOf("minimum" to 10, "maximum" to 20), failure.parameters)
        assertFalse(isValid(StringSubject("x".repeat(21)), rules))
    }

    @Test
    fun `mustHaveMaximumLength should fail when the value is too long and pass when it is short enough`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustHaveMaximumLength(5) } }

        // Act
        val failure = assertFails("string.maximumLength", "Hello World", rules)

        // Assert
        assertEquals(mapOf("length" to 5), failure.parameters)
        assertPasses("", "Hello", rules = rules)
    }

    @Test
    fun `mustHaveMinimumLength should fail when the value is too short and pass when it is long enough`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustHaveMinimumLength(100) } }

        // Act
        val failure = assertFails("string.minimumLength", "Hello World", rules)

        // Assert
        assertEquals(mapOf("length" to 100), failure.parameters)
        assertPasses("x".repeat(100), "x".repeat(101), rules = rules)
    }

    @Test
    fun `mustBeValidLuhnChecksum should fail for an invalid checksum and pass for a valid one`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustBeValidLuhnChecksum() } }

        // Act
        val failure = assertFails("string.mod10Checksum", "Hello World", rules)

        // Assert
        assertEquals("must be a valid Luhn (Mod10) checksum.", failure.message)
        assertFails("string.mod10Checksum", "79927398710", rules)
        assertPasses("79927398713", "4111111111111111", rules = rules)
    }

    @Test
    fun `mustBeValidMod10Checksum should behave like mustBeValidLuhnChecksum`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustBeValidMod10Checksum() } }

        // Act
        val failure = assertFails("string.mod10Checksum", "79927398710", rules)

        // Assert
        assertEquals("must be a valid Mod10 (Luhn) checksum.", failure.message)
        assertPasses("79927398713", rules = rules)
    }

    @Test
    fun `mustMatchRegex should fail when the value does not match and pass when it does`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustMatchRegex(Regex("[A-Z]")) } }

        // Act
        val failure = assertFails("string.matches", "Hello World", rules)

        // Assert
        assertEquals(mapOf("pattern" to "[A-Z]"), failure.parameters)
        assertPasses("H", rules = rules)
    }

    @Test
    fun `mustMatchRegularExpression should behave like mustMatchRegex`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustMatchRegularExpression("[A-Z][a-z]+") } }

        // Act
        val failure = assertFails("string.matches", "Hello World", rules)

        // Assert
        assertEquals(mapOf("pattern" to "[A-Z][a-z]+"), failure.parameters)
        assertPasses("Hello", rules = rules)
    }

    @Test
    fun `mustBeValidUUID should fail for an invalid UUID and pass for a valid one`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = { StringSubject::property { mustBeValidUUID() } }

        // Act
        val failure = assertFails("string.uuid", "Hello World", rules)

        // Assert
        assertEquals("must be a valid UUID.", failure.message)
        assertPasses("f1758a23-c14f-4397-957d-c63af8ca6273", rules = rules)
    }

    @Test
    fun `string rules other than null checks should pass on null`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = {
            StringSubject::property {
                mustBeBlank()
                mustNotBeBlank()
                mustBeEmpty()
                mustNotBeEmpty()
                mustContain("x")
                mustNotContain("x")
                mustContainAll(listOf("x"))
                mustContainAny(listOf("x"))
                mustStartWith("x")
                mustNotStartWith("x")
                mustEndWith("x")
                mustNotEndWith("x")
                mustHaveLength(1)
                mustHaveLengthWithinRange(1, 2)
                mustHaveMaximumLength(1)
                mustHaveMinimumLength(1)
                mustBeValidLuhnChecksum()
                mustMatchRegex(Regex("x"))
                mustBeValidUUID()
            }
        }

        // Act
        val result = validate(StringSubject(null), block = rules)

        // Assert
        assertTrue(result.isValid, result.toString())
    }

    @Test
    fun `string rules should report every failure in ALL_FAILURES mode in declaration order`() {

        // Arrange
        val rules: ValidationBuilder<StringSubject>.() -> Unit = {
            StringSubject::property {
                mustStartWith("Goodbye")
                mustEndWith("Mars")
                mustHaveMaximumLength(5)
            }
        }

        // Act
        val result = validate(StringSubject("Hello World"), block = rules)

        // Assert
        assertEquals(ValidationMode.ALL_FAILURES, result.mode)
        assertEquals(listOf("string.startsWith", "string.endsWith", "string.maximumLength"), result.failures.map { it.rule })
    }
}
