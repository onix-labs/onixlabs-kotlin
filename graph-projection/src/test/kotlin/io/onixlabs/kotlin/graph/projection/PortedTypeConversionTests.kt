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
package io.onixlabs.kotlin.graph.projection

import io.onixlabs.kotlin.core.typeconverters.BigDecimalTypeConverter
import io.onixlabs.kotlin.core.typeconverters.TypeConverter
import io.onixlabs.kotlin.graph.graphs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.util.UUID

/**
 * Scenarios ported from the old onixlabs-kotlin-projection TypeConverterConfigurationTests. The old configuration API
 * no longer exists, so each scenario asserts that projection uses the corresponding default converter, or fails when
 * there is none. The converters' own behaviour is covered by core's type converter tests.
 */
class PortedTypeConversionTests {

    data class TextSource(val value: String)
    data class NumberSource(val value: Int)

    data class BigDecimalTarget(val value: BigDecimal)
    data class BigIntegerTarget(val value: BigInteger)
    data class BooleanTarget(val value: Boolean)
    data class ByteTarget(val value: Byte)
    data class CharTarget(val value: Char)
    data class DoubleTarget(val value: Double)
    data class FloatTarget(val value: Float)
    data class IntTarget(val value: Int)
    data class LongTarget(val value: Long)
    data class ShortTarget(val value: Short)
    data class StringTarget(val value: String)
    data class UUIDTarget(val value: UUID)
    data class LocalDateTarget(val value: LocalDate)
    data class ListTarget(val value: List<Int>)

    @Test
    fun `projection should use the default BigDecimal type converter`() {

        // Act
        val result = graphs { }.project<BigDecimalTarget>(TextSource("1.5"))

        // Assert
        assertEquals(BigDecimalTarget(BigDecimal("1.5")), result)
    }

    @Test
    fun `projection should use the default BigInteger type converter`() {

        // Act
        val result = graphs { }.project<BigIntegerTarget>(TextSource("10"))

        // Assert
        assertEquals(BigIntegerTarget(BigInteger.TEN), result)
    }

    @Test
    fun `projection should use the default Boolean type converter`() {

        // Act
        val result = graphs { }.project<BooleanTarget>(TextSource("true"))

        // Assert
        assertEquals(BooleanTarget(true), result)
    }

    @Test
    fun `projection should use the default Byte type converter`() {

        // Act
        val result = graphs { }.project<ByteTarget>(TextSource("1"))

        // Assert
        assertEquals(ByteTarget(1), result)
    }

    @Test
    fun `projection should use the default Char type converter`() {

        // Act
        val result = graphs { }.project<CharTarget>(TextSource("A"))

        // Assert
        assertEquals(CharTarget('A'), result)
    }

    @Test
    fun `projection should use the default Double type converter`() {

        // Act
        val result = graphs { }.project<DoubleTarget>(TextSource("1.5"))

        // Assert
        assertEquals(DoubleTarget(1.5), result)
    }

    @Test
    fun `projection should use the default Float type converter`() {

        // Act
        val result = graphs { }.project<FloatTarget>(TextSource("1.5"))

        // Assert
        assertEquals(FloatTarget(1.5F), result)
    }

    @Test
    fun `projection should use the default Int type converter`() {

        // Act
        val result = graphs { }.project<IntTarget>(TextSource("1"))

        // Assert
        assertEquals(IntTarget(1), result)
    }

    @Test
    fun `projection should use the default Long type converter`() {

        // Act
        val result = graphs { }.project<LongTarget>(TextSource("1"))

        // Assert
        assertEquals(LongTarget(1), result)
    }

    @Test
    fun `projection should use the default Short type converter`() {

        // Act
        val result = graphs { }.project<ShortTarget>(TextSource("1"))

        // Assert
        assertEquals(ShortTarget(1), result)
    }

    @Test
    fun `projection should use the default String type converter`() {

        // Act
        val result = graphs { }.project<StringTarget>(NumberSource(123))

        // Assert
        assertEquals(StringTarget("123"), result)
    }

    @Test
    fun `projection should use the default UUID type converter`() {

        // Arrange
        val id = "079861fc-97f2-4af5-a47b-ece5a433e623"

        // Act
        val result = graphs { }.project<UUIDTarget>(TextSource(id))

        // Assert
        assertEquals(UUIDTarget(UUID.fromString(id)), result)
    }

    @Test
    fun `projection should fail when there is no available type converter`() {

        // Act
        val exception = assertThrows<ProjectionException> { graphs { }.project<LocalDateTarget>(TextSource("2020-01-01")) }

        // Assert
        val failure = exception.failures.single()
        assertEquals("LocalDateTarget.value", failure.path.toString())
        assertEquals("TextSource.value", failure.sourcePath.toString())
    }

    @Test
    fun `projection should fail when a non-collection value is projected to a list`() {

        // Act
        val exception = assertThrows<ProjectionException> { graphs { }.project<ListTarget>(NumberSource(1)) }

        // Assert
        assertEquals("ListTarget.value", exception.failures.single().path.toString())
    }

    @Test
    fun `projection should fail without default type converters`() {

        // Act
        val exception = assertThrows<ProjectionException> {
            graphs { withoutDefaultTypeConverters() }.project<BigDecimalTarget>(TextSource("1.5"))
        }

        // Assert
        assertEquals("BigDecimalTarget.value", exception.failures.single().path.toString())
    }

    @Test
    fun `projection should use a registered type converter without default type converters`() {

        // Arrange
        val registry = graphs {
            withoutDefaultTypeConverters()
            typeConverter(BigDecimalTypeConverter())
        }

        // Act
        val result = registry.project<BigDecimalTarget>(TextSource("1.5"))

        // Assert
        assertEquals(BigDecimalTarget(BigDecimal("1.5")), result)
    }

    @Test
    fun `projection should use a registered type converter for a type that has no default converter`() {

        // Arrange
        val registry = graphs {
            typeConverter(object : TypeConverter<LocalDate>() {
                override fun convert(value: Any): LocalDate = LocalDate.parse(value.toString())
            })
        }

        // Act
        val result = registry.project<LocalDateTarget>(TextSource("2020-01-01"))

        // Assert
        assertEquals(LocalDateTarget(LocalDate.of(2020, 1, 1)), result)
    }
}
