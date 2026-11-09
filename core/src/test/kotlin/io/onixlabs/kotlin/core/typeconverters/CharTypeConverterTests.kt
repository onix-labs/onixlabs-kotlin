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
import org.junit.jupiter.api.assertThrows
import java.util.*

class CharTypeConverterTests {

    @Test
    fun `CharTypeConverter should return ONE when Boolean is true`() {

        // Arrange
        val value = true
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals('1', result)
    }

    @Test
    fun `CharTypeConverter should return ZERO when Boolean is false`() {

        // Arrange
        val value = false
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals('0', result)
    }

    @Test
    fun `CharTypeConverter should return the expected value for a Byte`() {

        // Arrange
        val value = 32.toByte()
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(' ', result)
    }

    @Test
    fun `CharTypeConverter should return the expected value for a Short`() {

        // Arrange
        val value = 32.toShort()
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(' ', result)
    }

    @Test
    fun `CharTypeConverter should return the expected value for an Int`() {

        // Arrange
        val value = 32
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(' ', result)
    }

    @Test
    fun `CharTypeConverter should throw an exception when an Int overflows`() {

        // Arrange
        val value = Int.MAX_VALUE
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Numeric overflow.", result.message)
    }

    @Test
    fun `CharTypeConverter should return the expected value for a Long`() {

        // Arrange
        val value = 32.toLong()
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(' ', result)
    }

    @Test
    fun `CharTypeConverter should throw an exception when an Long overflows`() {

        // Arrange
        val value = Long.MAX_VALUE
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Numeric overflow.", result.message)
    }

    @Test
    fun `CharTypeConverter should return the expected value for a BigInteger`() {

        // Arrange
        val value = 32.toBigInteger()
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(' ', result)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a BigInteger overflows`() {

        // Arrange
        val value = Long.MAX_VALUE.toBigInteger()
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Numeric overflow.", result.message)
    }

    @Test
    fun `CharTypeConverter should return the expected value for a Float`() {

        // Arrange
        val value = 32.toFloat()
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(' ', result)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a Float overflows`() {

        // Arrange
        val value = Float.MAX_VALUE
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Numeric overflow.", result.message)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a Float loses precision`() {

        // Arrange
        val value = 123.456F
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Loss of precision.", result.message)
    }

    @Test
    fun `CharTypeConverter should return the expected value for a Double`() {

        // Arrange
        val value = 32.toDouble()
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(' ', result)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a Double overflows`() {

        // Arrange
        val value = Double.MAX_VALUE
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Numeric overflow.", result.message)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a Double loses precision`() {

        // Arrange
        val value = 123.456
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Loss of precision.", result.message)
    }

    @Test
    fun `CharTypeConverter should return the expected value for a BigDecimal`() {

        // Arrange
        val value = 32.toBigDecimal()
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(' ', result)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a BigDecimal overflows`() {

        // Arrange
        val value = Double.MAX_VALUE.toBigDecimal()
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Numeric overflow.", result.message)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a BigDecimal loses precision`() {

        // Arrange
        val value = (123.456).toBigDecimal()
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Loss of precision.", result.message)
    }

    @Test
    fun `CharTypeConverter should return the expected value for a String`() {

        // Arrange
        val value = " "
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(' ', result)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a String contains more than a single Char`() {

        // Arrange
        val value = "ab"
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. String value cannot be converted to Char.", result.message)
    }

    @Test
    fun `CharTypeConverter should return the expected value for a Char`() {

        // Arrange
        val value = ' '
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(' ', result)
    }

    @Test
    fun `CharTypeConverter should throw an exception for an unsupported type`() {

        // Arrange
        val value = UUID.randomUUID()
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Cannot convert from 'java.util.UUID' to 'kotlin.Char'.", result.message)
    }

    @Test
    fun `CharTypeConverter should convert an Int at the upper bound of the Char range`() {

        // Arrange
        val value = 0xFFFF
        val converter = CharTypeConverter()

        // Act
        val result = converter.convert(value)

        // Assert
        assertEquals(Char.MAX_VALUE, result)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a negative Byte underflows`() {

        // Arrange
        val value = (-1).toByte()
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Numeric overflow.", result.message)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a negative Short underflows`() {

        // Arrange
        val value = (-1).toShort()
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Numeric overflow.", result.message)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a negative Int underflows`() {

        // Arrange
        val value = -1
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Numeric overflow.", result.message)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a negative Long underflows`() {

        // Arrange
        val value = -1L
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Numeric overflow.", result.message)
    }

    @Test
    fun `CharTypeConverter should throw an exception when a negative Double underflows`() {

        // Arrange
        val value = -1.0
        val converter = CharTypeConverter()

        // Act
        val result = assertThrows<IllegalTypeConversionException> {
            converter.convert(value)
        }

        // Assert
        assertEquals("Illegal type conversion. Numeric overflow.", result.message)
    }
}
