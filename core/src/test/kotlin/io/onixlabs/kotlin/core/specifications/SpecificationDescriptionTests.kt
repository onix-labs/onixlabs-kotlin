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
package io.onixlabs.kotlin.core.specifications

import io.onixlabs.kotlin.core.specifications.generic.GenericPredicateSpecification
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SpecificationDescriptionTests {

    private class CustomerAgeSpecification : Specification<Int>() {
        override fun isSatisfiedBy(value: Int): Boolean = value >= 18
    }

    private object IsBusinessCustomer : Specification<Int>() {
        override val description: String = "is a business customer"
        override fun isSatisfiedBy(value: Int): Boolean = value > 100
    }

    @Test
    fun `Specification description should be derived from the class name by default`() {

        // Assert
        assertEquals("customer age", CustomerAgeSpecification().description)
    }

    @Test
    fun `Specification description should be overridable`() {

        // Assert
        assertEquals("is a business customer", IsBusinessCustomer.description)
    }

    @Test
    fun `Logical specifications should describe their operands, grouping where needed`() {

        // Arrange
        val age = CustomerAgeSpecification()

        // Assert
        assertEquals("customer age and is a business customer", (age and IsBusinessCustomer).description)
        assertEquals("customer age or is a business customer", (age or IsBusinessCustomer).description)
        assertEquals("(customer age or is a business customer) and customer age", ((age or IsBusinessCustomer) and age).description)
        assertEquals("not customer age", age.not().description)
        assertEquals("not (customer age and is a business customer)", (age and IsBusinessCustomer).not().description)
    }

    @Test
    fun `Logical specifications composed with infix operators should evaluate correctly`() {

        // Arrange
        val specification = CustomerAgeSpecification() and IsBusinessCustomer

        // Assert
        assertTrue(specification.isSatisfiedBy(200))
        assertFalse(specification.isSatisfiedBy(50))
    }

    @Test
    fun `GenericPredicateSpecification should use a custom description when specified`() {

        // Arrange
        val named = GenericPredicateSpecification<Int>({ it > 0 }, "is positive")
        val unnamed = GenericPredicateSpecification<Int>({ it > 0 })

        // Assert
        assertEquals("is positive", named.description)
        assertEquals("generic predicate", unnamed.description)
    }
}
