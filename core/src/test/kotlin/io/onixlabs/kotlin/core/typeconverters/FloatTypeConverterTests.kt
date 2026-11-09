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

import io.onixlabs.kotlin.core.ONE
import io.onixlabs.kotlin.core.ZERO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class FloatTypeConverterTests {

    @Test
    fun `FloatTypeConverter should return ONE when Boolean is true`() {

        // Arrange
        val value = true
        val converter = FloatTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(Float.ONE, result)
    }

    @Test
    fun `FloatTypeConverter should return ZERO when Boolean is false`() {

        // Arrange
        val value = false
        val converter = FloatTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(Float.ZERO, result)
    }

    @Test
    fun `FloatTypeConverter should return the expected value for a Byte`() {

        // Arrange
        val value = Byte.ONE
        val converter = FloatTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(Float.ONE, result)
    }

    @Test
    fun `FloatTypeConverter should return the expected value for a Short`() {

        // Arrange
        val value = Short.ONE
        val converter = FloatTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(Float.ONE, result)
    }

    @Test
    fun `FloatTypeConverter should return the expected value for an Int`() {

        // Arrange
        val value = Int.ONE
        val converter = FloatTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(Float.ONE, result)
    }

    @Test
    fun `FloatTypeConverter should return the expected value for a Float`() {

        // Arrange
        val value = Float.ONE
        val converter = FloatTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(Float.ONE, result)
    }

    @Test
    fun `FloatTypeConverter should return the expected value for a String`() {

        // Arrange
        val value = "1"
        val converter = FloatTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(Float.ONE, result)
    }

    @Test
    fun `FloatTypeConverter should return the expected value for a Char`() {

        // Arrange
        val value = ' '
        val converter = FloatTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(32.0F, result)
    }

    @Test
    fun `FloatTypeConverter should throw an exception for an unsupported type`() {

        // Arrange
        val value = UUID.randomUUID()
        val converter = FloatTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Cannot convert from 'java.util.UUID' to 'kotlin.Float'.", result.message)
    }
}
