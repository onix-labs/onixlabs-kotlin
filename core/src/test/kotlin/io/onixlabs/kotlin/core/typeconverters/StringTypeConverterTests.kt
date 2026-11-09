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
package io.onixlabs.kotlin.core.typeconverters

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringTypeConverterTests {

    @Test
    fun `StringTypeConverter should return the expected value for a Boolean value true`() {

        // Arrange
        val value = true
        val converter = StringTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals("True", result)
    }

    @Test
    fun `StringTypeConverter should return the expected value for a Boolean value false`() {

        // Arrange
        val value = false
        val converter = StringTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals("False", result)
    }

    @Test
    fun `StringTypeConverter should return the string representation of all types other than Boolean`() {

        // Arrange
        val value = 123456
        val converter = StringTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals("123456", result)
    }
}
