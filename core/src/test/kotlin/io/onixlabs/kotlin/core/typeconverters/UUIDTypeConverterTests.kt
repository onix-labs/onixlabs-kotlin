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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UUIDTypeConverterTests {

    @Test
    fun `UUIDTypeConverter should return the expected value for a String`() {

        // Arrange
        val value = "3ab57c8d-ab84-4db0-be2b-db9551dd4af6"
        val converter = UUIDTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(4, result.version())
    }

    @Test
    fun `UUIDTypeConverter should return the expected value for a UUID`() {

        // Arrange
        val value = UUID.fromString("3ab57c8d-ab84-4db0-be2b-db9551dd4af6")
        val converter = UUIDTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(value, result)
    }

    @Test
    fun `UUIDTypeConverter should throw an exception for an unsupported type`() {

        // Arrange
        val value = Int.ONE
        val converter = UUIDTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Cannot convert from 'kotlin.Int' to 'java.util.UUID'.", result.message)
    }
}
