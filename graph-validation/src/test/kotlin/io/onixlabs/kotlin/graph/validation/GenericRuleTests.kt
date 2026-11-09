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
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class GenericRuleTests {

    private val path = "GenericSubject.property"

    @Test
    fun `mustBeNull should fail when the value is not null and pass when it is null`() {

        // Arrange
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustBeNull() } }

        // Act
        val failure = firstFailureOf(GenericSubject(Unit), rules)
        val valid = isValid(GenericSubject(null), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("generic.null", failure.rule)
        assertEquals("must be null.", failure.message)
        assertTrue(valid)
    }

    @Test
    fun `mustNotBeNull should fail when the value is null and pass when it is not null`() {

        // Arrange
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustNotBeNull() } }

        // Act
        val failure = firstFailureOf(GenericSubject(null), rules)
        val valid = isValid(GenericSubject(Unit), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("generic.notNull", failure.rule)
        assertEquals("must not be null.", failure.message)
        assertTrue(valid)
    }

    @Test
    fun `mustBeEqualTo should fail when the value is not equal and pass when it is equal`() {

        // Arrange
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustBeEqualTo(123) } }

        // Act
        val failure = firstFailureOf(GenericSubject(456), rules)
        val valid = isValid(GenericSubject(123), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("generic.equalTo", failure.rule)
        assertEquals(mapOf("value" to 123), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustBeEqualTo should pass on null`() {

        // The old library failed mustBeEqualTo(123) on a null value; every non-null rule now passes on null.

        // Arrange
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustBeEqualTo(123) } }

        // Act
        val valid = isValid(GenericSubject(null), rules)

        // Assert
        assertTrue(valid)
    }

    @Test
    fun `mustNotBeEqualTo should fail when the value is equal and pass when it is not equal`() {

        // Arrange
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustNotBeEqualTo(123) } }

        // Act
        val failure = firstFailureOf(GenericSubject(123), rules)
        val valid = isValid(GenericSubject(456), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("generic.notEqualTo", failure.rule)
        assertEquals(mapOf("value" to 123), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustBeEqualByReferenceTo should fail for an equal but distinct instance and pass for the same instance`() {

        // Arrange
        val reference = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val other = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustBeEqualByReferenceTo(other) } }

        // Act
        val failure = firstFailureOf(GenericSubject(reference), rules)
        val valid = isValid(GenericSubject(other), rules)

        // Assert
        assertEquals(reference, other)
        assertEquals(path, failure.path.toString())
        assertEquals("generic.sameAs", failure.rule)
        assertSame(other, failure.parameters["value"])
        assertTrue(valid)
    }

    @Test
    fun `mustNotBeEqualByReferenceTo should fail for the same instance and pass for an equal but distinct instance`() {

        // Arrange
        val reference = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val other = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustNotBeEqualByReferenceTo(reference) } }

        // Act
        val failure = firstFailureOf(GenericSubject(reference), rules)
        val valid = isValid(GenericSubject(other), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("generic.notSameAs", failure.rule)
        assertTrue(valid)
    }

    @Test
    fun `mustBeEqualToAny should fail when the value equals none and pass when it equals one`() {

        // Arrange
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustBeEqualToAny(listOf(456, 789)) } }

        // Act
        val failure = firstFailureOf(GenericSubject(123), rules)
        val valid = isValid(GenericSubject(789), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("generic.equalToAny", failure.rule)
        assertEquals(mapOf("values" to listOf(456, 789)), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustNotBeEqualToAny should fail when the value equals one and pass when it equals none`() {

        // Arrange
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustNotBeEqualToAny(listOf(123, 456)) } }

        // Act
        val failure = firstFailureOf(GenericSubject(123), rules)
        val valid = isValid(GenericSubject(789), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("generic.notEqualToAny", failure.rule)
        assertEquals(mapOf("values" to listOf(123, 456)), failure.parameters)
        assertTrue(valid)
    }

    @Test
    fun `mustSatisfy should fail when the predicate is not satisfied and pass when it is`() {

        // Arrange
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustSatisfy({ value -> value as Int > 200 }) } }

        // Act
        val failure = firstFailureOf(GenericSubject(123), rules)
        val valid = isValid(GenericSubject(201), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("generic.satisfies", failure.rule)
        assertEquals("must satisfy the specified predicate.", failure.message)
        assertTrue(valid)
    }

    @Test
    fun `mustNotSatisfy should fail when the predicate is satisfied and pass when it is not`() {

        // Arrange
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustNotSatisfy({ value -> value as Int > 100 }) } }

        // Act
        val failure = firstFailureOf(GenericSubject(123), rules)
        val valid = isValid(GenericSubject(100), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("generic.notSatisfies", failure.rule)
        assertEquals("must not satisfy the specified predicate.", failure.message)
        assertTrue(valid)
    }

    @Test
    fun `generic rules other than null checks should pass on null`() {

        // Arrange
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = {
            GenericSubject::property {
                mustBeEqualTo(123)
                mustNotBeEqualTo(123)
                mustBeEqualToAny(listOf(456, 789))
                mustNotBeEqualToAny(listOf(456, 789))
                mustSatisfy({ false })
                mustNotSatisfy({ true })
                mustBeEqualByReferenceTo(Unit)
                mustNotBeEqualByReferenceTo(Unit)
            }
        }

        // Act
        val result = validate(GenericSubject(null), block = rules)

        // Assert
        assertTrue(result.isValid, result.toString())
    }

    @Test
    fun `generic rules should report custom messages`() {

        // Arrange
        val rules: ValidationBuilder<GenericSubject>.() -> Unit = { GenericSubject::property { mustNotBeNull("is required.") } }

        // Act
        val failure = firstFailureOf(GenericSubject(null), rules)

        // Assert
        assertEquals("is required.", failure.message)
        assertFalse(isValid(GenericSubject(null), rules))
    }
}
