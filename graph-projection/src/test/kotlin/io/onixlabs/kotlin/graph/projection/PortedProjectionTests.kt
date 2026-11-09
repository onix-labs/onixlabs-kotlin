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

import io.onixlabs.kotlin.core.EMPTY
import io.onixlabs.kotlin.core.reflection.kotlinClass
import io.onixlabs.kotlin.core.typeconverters.IllegalTypeConversionException
import io.onixlabs.kotlin.core.typeconverters.TypeConverter
import io.onixlabs.kotlin.graph.graphs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigInteger
import java.util.UUID

/**
 * Scenarios ported from the old onixlabs-kotlin-projection ProjectionTests.
 */
class PortedProjectionTests {

    data class NullableSource(val a: Int?)
    data class NonNullableSource(val a: Int)
    data class NullableTarget(val a: Int?)
    data class NonNullableTarget(val a: Int)
    data class NonNullableTargetWithUnmatchedPropertyName(val b: Int)
    data class NullableOptionalTarget(val a: Int? = 123)
    data class NonNullableOptionalTarget(val a: Int = 123)
    data class CompatibleNullableTarget(val a: Long?)
    data class CompatibleNonNullableTarget(val a: Long)
    data class CompatibleNullableOptionalTarget(val a: Long? = 123)
    data class CompatibleNonNullableOptionalTarget(val a: Long = 123)
    data class IncompatibleMultiArgSource(val value: Int, val power: Int)
    data class IncompatibleSingleArgSource(val value: UUID)
    data class IncompatibleSingleArgTarget(val value: BigInteger)
    data class ParentSource(val a: Child)
    data class ParentTarget(val a: Child)
    data class Child(val a: Int, val b: String)

    private val uuidToBigInteger = object : TypeConverter<BigInteger>() {
        override fun convert(value: Any): BigInteger = when (value) {
            is UUID -> BigInteger(value.toString().replace("-", String.EMPTY), 16)
            else -> throw IllegalTypeConversionException(value.kotlinClass, BigInteger::class)
        }
    }

    @Test
    fun `projection should project identical non-nullable parameters`() {

        // Arrange
        val source = NonNullableSource(123)

        // Act
        val result = graphs { }.project<NonNullableTarget>(source)

        // Assert
        assertEquals(NonNullableTarget(123), result)
    }

    @Test
    fun `projection should project identical nullable parameters - with a value`() {

        // Arrange
        val source = NullableSource(123)

        // Act
        val result = graphs { }.project<NullableTarget>(source)

        // Assert
        assertEquals(NullableTarget(123), result)
    }

    @Test
    fun `projection should project identical nullable parameters - without a value`() {

        // Arrange
        val source = NullableSource(null)

        // Act
        val result = graphs { }.project<NullableTarget>(source)

        // Assert
        assertEquals(NullableTarget(null), result)
    }

    @Test
    fun `projection should project non-nullable parameter to nullable parameter`() {

        // Arrange
        val source = NonNullableSource(123)

        // Act
        val result = graphs { }.project<NullableTarget>(source)

        // Assert
        assertEquals(NullableTarget(123), result)
    }

    @Test
    fun `projection should project nullable parameter to non-nullable optional parameter - with a value`() {

        // Arrange
        val source = NullableSource(456)

        // Act
        val result = graphs { }.project<NonNullableOptionalTarget>(source)

        // Assert
        assertEquals(NonNullableOptionalTarget(456), result)
    }

    @Test
    fun `projection should project nullable parameter to non-nullable optional parameter - without a value`() {

        // Arrange
        val source = NullableSource(null)

        // Act
        val result = graphs { }.project<NonNullableOptionalTarget>(source)

        // Assert
        assertEquals(NonNullableOptionalTarget(123), result)
    }

    @Test
    fun `projection should project nullable parameter to nullable optional parameter - with a value`() {

        // Arrange
        val source = NullableSource(456)

        // Act
        val result = graphs { }.project<NullableOptionalTarget>(source)

        // Assert
        assertEquals(NullableOptionalTarget(456), result)
    }

    @Test
    fun `projection should project nullable parameter to nullable optional parameter - without a value`() {

        // Arrange
        val source = NullableSource(null)

        // Act
        val result = graphs { }.project<NullableOptionalTarget>(source)

        // Assert
        assertEquals(NullableOptionalTarget(123), result)
    }

    @Test
    fun `projection should fail when projecting nullable parameter to non-nullable parameter`() {

        // Arrange
        val source = NullableSource(null)

        // Act
        val exception = assertThrows<ProjectionException> { graphs { }.project<NonNullableTarget>(source) }

        // Assert
        val failure = exception.failures.single()
        assertEquals("NonNullableTarget.a", failure.path.toString())
        assertEquals(true, failure.message.contains("null"))
    }

    @Test
    fun `projection should project compatible non-nullable parameters`() {

        // Arrange
        val source = NonNullableSource(123)

        // Act
        val result = graphs { }.project<CompatibleNonNullableTarget>(source)

        // Assert
        assertEquals(CompatibleNonNullableTarget(123), result)
    }

    @Test
    fun `projection should project compatible nullable parameters - with a value`() {

        // Arrange
        val source = NullableSource(123)

        // Act
        val result = graphs { }.project<CompatibleNullableTarget>(source)

        // Assert
        assertEquals(CompatibleNullableTarget(123), result)
    }

    @Test
    fun `projection should project compatible nullable parameters - without a value`() {

        // Arrange
        val source = NullableSource(null)

        // Act
        val result = graphs { }.project<CompatibleNullableTarget>(source)

        // Assert
        assertEquals(CompatibleNullableTarget(null), result)
    }

    @Test
    fun `projection should project compatible non-nullable parameter to nullable parameter`() {

        // Arrange
        val source = NonNullableSource(123)

        // Act
        val result = graphs { }.project<CompatibleNullableTarget>(source)

        // Assert
        assertEquals(CompatibleNullableTarget(123), result)
    }

    @Test
    fun `projection should project compatible nullable parameter to non-nullable optional parameter - with a value`() {

        // Arrange
        val source = NullableSource(456)

        // Act
        val result = graphs { }.project<CompatibleNonNullableOptionalTarget>(source)

        // Assert
        assertEquals(CompatibleNonNullableOptionalTarget(456), result)
    }

    @Test
    fun `projection should project compatible nullable parameter to non-nullable optional parameter - without a value`() {

        // Arrange
        val source = NullableSource(null)

        // Act
        val result = graphs { }.project<CompatibleNonNullableOptionalTarget>(source)

        // Assert
        assertEquals(CompatibleNonNullableOptionalTarget(123), result)
    }

    @Test
    fun `projection should project compatible nullable parameter to nullable optional parameter - with a value`() {

        // Arrange
        val source = NullableSource(456)

        // Act
        val result = graphs { }.project<CompatibleNullableOptionalTarget>(source)

        // Assert
        assertEquals(CompatibleNullableOptionalTarget(456), result)
    }

    @Test
    fun `projection should project compatible nullable parameter to nullable optional parameter - without a value`() {

        // Arrange
        val source = NullableSource(null)

        // Act
        val result = graphs { }.project<CompatibleNullableOptionalTarget>(source)

        // Assert
        assertEquals(CompatibleNullableOptionalTarget(123), result)
    }

    @Test
    fun `projection should fail when projecting compatible nullable parameter to non-nullable parameter`() {

        // Arrange
        val source = NullableSource(null)

        // Act
        val exception = assertThrows<ProjectionException> { graphs { }.project<CompatibleNonNullableTarget>(source) }

        // Assert
        val failure = exception.failures.single()
        assertEquals("CompatibleNonNullableTarget.a", failure.path.toString())
        assertEquals(true, failure.message.contains("null"))
    }

    @Test
    fun `projection should project incompatible parameters with custom projection value`() {

        // Arrange
        val source = IncompatibleMultiArgSource(10, 10)

        // Act
        val result = project(source, IncompatibleSingleArgTarget::class) {
            parameter(IncompatibleSingleArgTarget::value, BigInteger.valueOf(source.value.toLong()).pow(source.power))
        }

        // Assert
        assertEquals(IncompatibleSingleArgTarget(BigInteger.valueOf(10000000000)), result)
    }

    @Test
    fun `projection should project incompatible parameters with custom projection value in the reference form`() {

        // Arrange
        val source = IncompatibleMultiArgSource(10, 10)

        // Act
        val result = project(source, IncompatibleSingleArgTarget::class) {
            IncompatibleSingleArgTarget::value from BigInteger.valueOf(source.value.toLong()).pow(source.power)
        }

        // Assert
        assertEquals(IncompatibleSingleArgTarget(BigInteger.valueOf(10000000000)), result)
    }

    @Test
    fun `projection should project incompatible parameters with custom projection action`() {

        // Arrange
        val source = IncompatibleMultiArgSource(10, 10)

        // Act
        val result = project(source, IncompatibleSingleArgTarget::class) {
            parameter(IncompatibleSingleArgTarget::value) { BigInteger.valueOf(it.value.toLong()).pow(it.power) }
        }

        // Assert
        assertEquals(IncompatibleSingleArgTarget(BigInteger.valueOf(10000000000)), result)
    }

    @Test
    fun `projection should project incompatible parameters with custom projection action declared on a graph`() {

        // Arrange
        val registry = graphs {
            graph<IncompatibleMultiArgSource> {
                project(IncompatibleSingleArgTarget::class) {
                    IncompatibleSingleArgTarget::value from { BigInteger.valueOf(it.value.toLong()).pow(it.power) }
                }
            }
        }

        // Act
        val result = registry.project<IncompatibleSingleArgTarget>(IncompatibleMultiArgSource(10, 10))

        // Assert
        assertEquals(IncompatibleSingleArgTarget(BigInteger.valueOf(10000000000)), result)
    }

    @Test
    fun `projection should project incompatible parameters with custom type converter`() {

        // Arrange
        val source = IncompatibleSingleArgSource(UUID.fromString("00000000-0000-0000-0000-00000000000A"))

        // Act
        val result = project(source, IncompatibleSingleArgTarget::class) {
            parameter(IncompatibleSingleArgTarget::value, uuidToBigInteger)
        }

        // Assert
        assertEquals(IncompatibleSingleArgTarget(BigInteger.TEN), result)
    }

    @Test
    fun `projection should project incompatible parameters with custom type converter in the reference form`() {

        // Arrange
        val source = IncompatibleSingleArgSource(UUID.fromString("00000000-0000-0000-0000-00000000000A"))

        // Act
        val result = project(source, IncompatibleSingleArgTarget::class) {
            IncompatibleSingleArgTarget::value using uuidToBigInteger
        }

        // Assert
        assertEquals(IncompatibleSingleArgTarget(BigInteger.TEN), result)
    }

    @Test
    fun `projection should project incompatible parameters with custom projection value using string name`() {

        // Arrange
        val source = IncompatibleMultiArgSource(10, 10)

        // Act
        val result = project(source, IncompatibleSingleArgTarget::class) {
            parameter("value", BigInteger.valueOf(source.value.toLong()).pow(source.power))
        }

        // Assert
        assertEquals(IncompatibleSingleArgTarget(BigInteger.valueOf(10000000000)), result)
    }

    @Test
    fun `projection should project incompatible parameters with custom projection action using string name`() {

        // Arrange
        val source = IncompatibleMultiArgSource(10, 10)

        // Act
        val result = project(source, IncompatibleSingleArgTarget::class) {
            parameter("value") { BigInteger.valueOf(it.value.toLong()).pow(it.power) }
        }

        // Assert
        assertEquals(IncompatibleSingleArgTarget(BigInteger.valueOf(10000000000)), result)
    }

    @Test
    fun `projection should project incompatible parameters with custom type converter and source property using string name`() {

        // Arrange
        val source = IncompatibleSingleArgSource(UUID.fromString("00000000-0000-0000-0000-00000000000A"))

        // Act
        val result = project(source, IncompatibleSingleArgTarget::class) {
            parameter("value", IncompatibleSingleArgSource::value, uuidToBigInteger)
        }

        // Assert
        assertEquals(IncompatibleSingleArgTarget(BigInteger.TEN), result)
    }

    @Test
    fun `projection should project incompatible parameters with custom type converter using string name`() {

        // Arrange
        val source = IncompatibleSingleArgSource(UUID.fromString("00000000-0000-0000-0000-00000000000A"))

        // Act
        val result = project(source, IncompatibleSingleArgTarget::class) {
            parameter("value", uuidToBigInteger)
        }

        // Assert
        assertEquals(IncompatibleSingleArgTarget(BigInteger.TEN), result)
    }

    @Test
    fun `projection should project parameters with different names`() {

        // Arrange
        val source = NonNullableSource(456)

        // Act
        val result = project(source, NonNullableTargetWithUnmatchedPropertyName::class) {
            parameter(NonNullableTargetWithUnmatchedPropertyName::b, NonNullableSource::a)
        }

        // Assert
        assertEquals(NonNullableTargetWithUnmatchedPropertyName(456), result)
    }

    @Test
    fun `projection should project parameters with different names in the reference form`() {

        // Arrange
        val source = NonNullableSource(456)

        // Act
        val result = project(source, NonNullableTargetWithUnmatchedPropertyName::class) {
            NonNullableTargetWithUnmatchedPropertyName::b from NonNullableSource::a
        }

        // Assert
        assertEquals(NonNullableTargetWithUnmatchedPropertyName(456), result)
    }

    @Test
    fun `projection should project parameters with different names using string name`() {

        // Arrange
        val source = NonNullableSource(456)

        // Act
        val result = project(source, NonNullableTargetWithUnmatchedPropertyName::class) {
            parameter("b", NonNullableSource::a)
        }

        // Assert
        assertEquals(NonNullableTargetWithUnmatchedPropertyName(456), result)
    }

    @Test
    fun `projection should project identical parameters when there is no registered type converter`() {

        // Arrange
        val source = ParentSource(Child(123, "Hello, world!"))

        // Act
        val result = graphs { withoutDefaultTypeConverters() }.project<ParentTarget>(source)

        // Assert
        assertEquals(ParentTarget(Child(123, "Hello, world!")), result)
        assertEquals(true, result.a === source.a)
    }
}
