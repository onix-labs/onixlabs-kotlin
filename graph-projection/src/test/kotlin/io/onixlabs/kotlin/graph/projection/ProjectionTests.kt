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

import io.onixlabs.kotlin.core.typeconverters.TypeConverter
import io.onixlabs.kotlin.graph.graph
import io.onixlabs.kotlin.graph.graphs
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant

enum class Status { ACTIVE, CLOSED }
enum class StatusDto { ACTIVE, CLOSED }

data class Customer(
    val first: String,
    val last: String,
    val emailAddress: String,
    val orders: List<Order> = emptyList(),
    val createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
    val status: Status = Status.ACTIVE,
    val referrer: Customer? = null,
)

data class Order(val total: BigDecimal, val tags: Set<String> = emptySet())

data class OrderDto(val total: String, val tags: List<String>)

data class CustomerDto(
    val displayName: String,
    val email: String,
    val orderCount: Int,
    val orders: List<OrderDto>,
    val createdAt: String,
    val status: StatusDto,
)

data class Summary(val first: String, val nickname: String = "none", val referrer: Summary? = null)

class CustomerEntity(var id: Int = 0, var first: String = "") {
    var last: String = ""
}

interface CustomerRecord {
    var first: String
    var last: String
}

class CustomerRecordImpl : CustomerRecord {
    override var first: String = ""
    override var last: String = ""
}

object IsoInstantConverter : TypeConverter<String>() {
    override fun convert(value: Any): String = (value as Instant).toString().removeSuffix("Z") + "+00:00"
}

class ProjectionTests {

    private val customer = Customer(
        first = "Ada",
        last = "Lovelace",
        emailAddress = "ada@example.com",
        orders = listOf(Order(BigDecimal("10.50"), setOf("a")), Order(BigDecimal("3"))),
    )

    private val customerGraph = graph<Customer> {
        project(CustomerDto::class) {
            CustomerDto::displayName from { "${it.first} ${it.last}" }
            CustomerDto::email from Customer::emailAddress
            CustomerDto::orderCount from { it.orders.size }
            CustomerDto::createdAt using IsoInstantConverter
        }
    }

    @Test
    fun `projection should apply mappings, conventions, collections, converters, and enums`() {

        // Arrange
        val registry = graphs { include(customerGraph) }

        // Act
        val dto = registry.project<CustomerDto>(customer)

        // Assert
        assertEquals(
            CustomerDto(
                displayName = "Ada Lovelace",
                email = "ada@example.com",
                orderCount = 2,
                orders = listOf(OrderDto("10.50", listOf("a")), OrderDto("3", emptyList())),
                createdAt = "2026-01-01T00:00:00+00:00",
                status = StatusDto.ACTIVE,
            ),
            dto,
        )
    }

    @Test
    fun `the verb form should produce the same projection as the reference form`() {

        // Arrange
        val registry = graphs {
            graph<Customer> {
                project(CustomerDto::class) {
                    parameter(CustomerDto::displayName) { "${it.first} ${it.last}" }
                    parameter(CustomerDto::email, Customer::emailAddress)
                    parameter("orderCount") { it.orders.size }
                    parameter(CustomerDto::createdAt, IsoInstantConverter)
                }
            }
        }

        // Assert
        assertEquals(graphs { include(customerGraph) }.project<CustomerDto>(customer), registry.project<CustomerDto>(customer))
    }

    @Test
    fun `a projection registered on a nested type's graph should take precedence over the convention`() {

        // Arrange
        val registry = graphs {
            include(customerGraph)
            graph<Order> {
                project<_, OrderDto> { OrderDto::total from { "£${it.total}" } }
            }
        }

        // Act
        val dto = registry.project<CustomerDto>(customer)

        // Assert
        assertEquals(listOf("£10.50", "£3"), dto.orders.map { it.total })
    }

    @Test
    fun `a registered type converter should replace the default converter`() {

        // Arrange
        val registry = graphs {
            include(customerGraph)
            typeConverter(object : TypeConverter<String>() {
                override fun convert(value: Any): String = "<$value>"
            })
        }

        // Act
        val dto = registry.project<CustomerDto>(customer)

        // Assert
        assertEquals("<10.50>", dto.orders.first().total)
    }

    @Test
    fun `projection should need no configuration for a conventional target`() {

        // Act
        val summary = graphs { }.project<Summary>(customer)

        // Assert
        assertEquals(Summary("Ada", "none", null), summary)
    }

    @Test
    fun `projection should set remaining mutable properties after construction`() {

        // Act
        val entity = graphs { }.project<CustomerEntity>(customer)

        // Assert
        assertEquals(listOf(0, "Ada", "Lovelace"), listOf(entity.id, entity.first, entity.last))
    }

    @Test
    fun `a registered factory should create targets without a usable constructor, including subtypes`() {

        // Arrange
        val registry = graphs { projectionTarget<CustomerRecord> { CustomerRecordImpl() } }

        // Act
        val record = registry.project<CustomerRecord>(customer)

        // Assert
        assertEquals(listOf("Ada", "Lovelace"), listOf(record.first, record.last))
    }

    @Test
    fun `projection should convert arrays and maps element by element`() {

        // Arrange
        data class Source(val values: List<String>, val totals: Map<String, BigDecimal>)
        class Target(val values: Array<Int>, val totals: Map<String, String>)

        // Act
        val target = graphs { }.project<Target>(Source(listOf("1", "2"), mapOf("a" to BigDecimal.ONE)))

        // Assert
        assertArrayEquals(arrayOf(1, 2), target.values)
        assertEquals(mapOf("a" to "1"), target.totals)
    }

    @Test
    fun `projection should collect every failure with target and source paths`() {

        // Arrange
        data class Source(val total: String, val items: List<String>)
        data class Target(val total: BigDecimal, val email: String, val items: List<Int>)

        // Act
        val exception = assertThrows<ProjectionException> {
            graphs { }.project<Target>(Source("abc", listOf("1", "x")))
        }

        // Assert
        assertEquals(
            listOf("Target.total", "Target.email", "Target.items[1]"),
            exception.failures.map { it.path.toString() },
        )
        assertEquals("Source.items[1]", exception.failures[2].sourcePath.toString())
        assertEquals(true, exception.message!!.startsWith("3 projection failures:"))
    }

    @Test
    fun `projection should report a cycle in the source as a failure`() {

        // Arrange
        val cyclic = Customer("Ada", "Lovelace", "ada@example.com").let { it.copy(referrer = it) }
        val selfReferencing = object : Any() {
            val first = "Ada"
            val referrer: Any get() = this
        }

        // Act
        val acyclic = graphs { }.project<Summary>(cyclic)
        val exception = assertThrows<ProjectionException> { graphs { }.project<Summary>(selfReferencing) }

        // Assert
        assertEquals(Summary("Ada", "none", Summary("Ada", "none", null)), acyclic)
        assertEquals("Summary.referrer", exception.failures.single().path.toString())
    }

    @Test
    fun `a mapping for an unknown target parameter should be reported`() {

        // Arrange
        val registry = graphs {
            graph<Customer> { project(Summary::class) { parameter("unknown") { "x" } } }
        }

        // Act
        val exception = assertThrows<ProjectionException> { registry.project<Summary>(customer) }

        // Assert
        assertEquals("Summary.unknown: Summary has no constructor parameter or mutable property named 'unknown'.", exception.message)
    }
}
