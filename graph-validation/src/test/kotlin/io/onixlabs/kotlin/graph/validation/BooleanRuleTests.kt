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

class BooleanRuleTests {

    private val path = "BooleanSubject.property"

    @Test
    fun `mustBeTrue should fail when the value is false and pass when it is true`() {

        // Arrange
        val rules: ValidationBuilder<BooleanSubject>.() -> Unit = { BooleanSubject::property { mustBeTrue() } }

        // Act
        val failure = firstFailureOf(BooleanSubject(false), rules)
        val valid = isValid(BooleanSubject(true), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("boolean.true", failure.rule)
        assertEquals("must be true.", failure.message)
        assertTrue(valid)
    }

    @Test
    fun `mustBeFalse should fail when the value is true and pass when it is false`() {

        // Arrange
        val rules: ValidationBuilder<BooleanSubject>.() -> Unit = { BooleanSubject::property { mustBeFalse() } }

        // Act
        val failure = firstFailureOf(BooleanSubject(true), rules)
        val valid = isValid(BooleanSubject(false), rules)

        // Assert
        assertEquals(path, failure.path.toString())
        assertEquals("boolean.false", failure.rule)
        assertEquals("must be false.", failure.message)
        assertTrue(valid)
    }

    @Test
    fun `boolean rules should pass on null`() {

        // Arrange
        val rules: ValidationBuilder<BooleanSubject>.() -> Unit = {
            BooleanSubject::property {
                mustBeTrue()
                mustBeFalse()
            }
        }

        // Act
        val valid = isValid(BooleanSubject(null), rules)

        // Assert
        assertTrue(valid)
    }

    @Test
    fun `boolean rules should report custom messages`() {

        // Arrange
        val rules: ValidationBuilder<BooleanSubject>.() -> Unit = { BooleanSubject::property { mustBeTrue("must be accepted.") } }

        // Act
        val failure = firstFailureOf(BooleanSubject(false), rules)

        // Assert
        assertEquals("must be accepted.", failure.message)
        assertEquals("$path must be accepted.", failure.toString())
    }
}
