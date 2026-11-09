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

import io.onixlabs.kotlin.core.specifications.Specification
import io.onixlabs.kotlin.graph.GraphConfigurationException
import io.onixlabs.kotlin.graph.GraphPath
import io.onixlabs.kotlin.graph.graph
import io.onixlabs.kotlin.graph.graphs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

enum class CustomerType { PERSONAL, BUSINESS }

data class Customer(
    val name: String?,
    val type: CustomerType = CustomerType.PERSONAL,
    val vatNumber: String? = null,
    val orders: List<Order> = emptyList(),
    val attributes: Map<String, String> = emptyMap(),
    val tags: Array<String> = emptyArray(),
    val referrer: Customer? = null,
)

data class Order(val total: BigDecimal, val payment: Payment? = null)

sealed interface Payment {
    val amount: BigDecimal
}

data class Card(override val amount: BigDecimal, val pan: String) : Payment

data class BankTransfer(override val amount: BigDecimal, val iban: String) : Payment

object IsBusinessCustomer : Specification<Customer>() {
    override val description: String = "is a business customer"
    override fun isSatisfiedBy(value: Customer): Boolean = value.type == CustomerType.BUSINESS
}

class ValidationTests {

    private val orderGraph = graph<Order> {
        validate { Order::total { mustBeGreaterThan(BigDecimal.ZERO) } }
    }

    private val paymentGraph = graph<Payment> {
        validate { Payment::amount { mustBeGreaterThan(BigDecimal.ZERO) } }
    }

    private val cardGraph = graph<Card> {
        validate { Card::pan { mustBeValidMod10Checksum() } }
    }

    private fun lines(result: ValidationResult): List<String> = result.failures.map { it.toString() }

    @Test
    fun `reference form and verb form should produce the same failures`() {

        // Arrange
        val referenceForm = graphs {
            graph<Customer> {
                validate {
                    Customer::name { mustNotBeBlank() }
                    Customer::tags.each { mustHaveMinimumLength(2) }
                    Customer::attributes.keys { mustNotBeBlank() }
                    Customer::attributes.values { mustHaveMaximumLength(3) }
                }
            }
        }
        val verbForm = graphs {
            graph<Customer> {
                validate {
                    property(Customer::name) { mustNotBeBlank() }
                    each(Customer::tags) { mustHaveMinimumLength(2) }
                    keys(Customer::attributes) { mustNotBeBlank() }
                    values(Customer::attributes) { mustHaveMaximumLength(3) }
                }
            }
        }
        val customer = Customer(name = " ", tags = arrayOf("ok", "x"), attributes = mapOf(" " to "long", "a" to "ok"))

        // Act
        val reference = referenceForm.validate(customer)
        val verb = verbForm.validate(customer)

        // Assert
        assertEquals(
            listOf(
                "Customer.name must not be blank.",
                "Customer.tags[1] must have a length of at least 2.",
                "Customer.attributes.keys[\" \"] must not be blank.",
                "Customer.attributes[\" \"] must have a length of at most 3.",
            ),
            lines(reference),
        )
        assertEquals(reference.failures, verb.failures)
    }

    @Test
    fun `rules other than null checks should pass on null`() {

        // Arrange
        val registry = graphs {
            graph<Customer> {
                validate {
                    Customer::name {
                        mustNotBeNull()
                        mustNotBeBlank()
                        mustHaveMaximumLength(50)
                    }
                    Customer::vatNumber { mustHaveLength(9) }
                }
            }
        }

        // Act
        val result = registry.validate(Customer(name = null))

        // Assert
        assertEquals(listOf("Customer.name must not be null."), lines(result))
        assertEquals("generic.notNull", result.failures.single().rule)
    }

    @Test
    fun `validation should descend automatically into members whose types have graphs`() {

        // Arrange
        val registry = graphs {
            include(orderGraph)
            graph<Customer> { validate { Customer::name { mustNotBeBlank() } } }
        }
        val customer = Customer("Ada", orders = listOf(Order(BigDecimal.ONE), Order(BigDecimal("-5"))))

        // Act
        val result = registry.validate(customer)

        // Assert
        assertEquals(listOf("Customer.orders[1].total must be greater than 0."), lines(result))
        assertEquals(mapOf("value" to BigDecimal.ZERO), result.failures.single().parameters)
    }

    @Test
    fun `validation should compose the graphs of a type hierarchy, from general to specific`() {

        // Arrange
        val registry = graphs {
            include(orderGraph, paymentGraph, cardGraph)
            graph<Customer>()
        }
        val order = Order(BigDecimal.ONE, Card(BigDecimal("-1"), "1234"))

        // Act
        val result = registry.validate(Customer("Ada", orders = listOf(order)))

        // Assert
        assertEquals(
            listOf(
                "Customer.orders[0].payment.amount must be greater than 0.",
                "Customer.orders[0].payment.pan must be a valid Mod10 (Luhn) checksum.",
            ),
            lines(result),
        )
    }

    @Test
    fun `validation should not loop on cycles, and skipGraph should stop descent`() {

        // Arrange
        val rules: io.onixlabs.kotlin.graph.GraphBuilder<Customer>.() -> Unit = { validate { Customer::name { mustNotBeBlank() } } }
        val descending = graphs { graph<Customer>(rules) }
        val skipping = graphs {
            graph<Customer> {
                rules()
                validate { Customer::referrer { skipGraph() } }
            }
        }
        val referrer = Customer(" ")
        val customer = Customer("Ada", referrer = referrer)
        val cyclic = Customer(" ").let { it.copy(referrer = it) }

        // Assert
        assertEquals(listOf("Customer.referrer.name must not be blank."), lines(descending.validate(customer)))
        assertTrue(skipping.validate(customer).isValid)
        assertEquals(listOf("Customer.name must not be blank.", "Customer.referrer.name must not be blank."), lines(descending.validate(cyclic)))
    }

    @Test
    fun `requireGraph should fail the registry build when the member's graph is missing`() {

        // Arrange
        val customerGraph = graph<Customer> {
            validate { Customer::orders.each { requireGraph() } }
        }

        // Act
        val exception = assertThrows<GraphConfigurationException> { graphs { include(customerGraph) } }

        // Assert
        assertEquals(
            "Customer.orders[*] requires a graph, but no graph is registered for io.onixlabs.kotlin.graph.validation.Order.",
            exception.message,
        )
        assertEquals(2, graphs { include(customerGraph, orderGraph) }.graphs.size)
    }

    @Test
    fun `FIRST_FAILURE should stop at the first failure and return it`() {

        // Arrange
        val registry = graphs {
            include(orderGraph)
            graph<Customer> { validate { Customer::name { mustNotBeBlank() } } }
        }
        val customer = Customer(" ", orders = listOf(Order(BigDecimal("-1"))))

        // Act
        val result = registry.validate(customer, ValidationMode.FIRST_FAILURE)

        // Assert
        assertEquals(listOf("Customer.name must not be blank."), lines(result))
        assertEquals(ValidationMode.FIRST_FAILURE, result.mode)
    }

    @Test
    fun `whenever and otherwise should apply rules depending on the subject`() {

        // Arrange
        val registry = graphs {
            graph<Customer> {
                validate {
                    whenever(IsBusinessCustomer) {
                        Customer::vatNumber { mustNotBeNull() }
                    } otherwise {
                        Customer::vatNumber { mustBeNull() }
                    }
                }
            }
        }

        // Act
        val business = registry.validate(Customer("Ada", type = CustomerType.BUSINESS))
        val personal = registry.validate(Customer("Ada", vatNumber = "GB123"))
        val valid = registry.validate(Customer("Ada", type = CustomerType.BUSINESS, vatNumber = "GB123"))

        // Assert
        assertEquals(listOf("Customer.vatNumber must not be null. (when is a business customer)"), lines(business))
        assertEquals(listOf("Customer.vatNumber must be null. (when not is a business customer)"), lines(personal))
        assertTrue(valid.isValid)
    }

    @Test
    fun `member-level whenever should apply rules depending on the value`() {

        // Arrange
        val registry = graphs {
            graph<Customer> {
                validate {
                    Customer::vatNumber {
                        whenever("is a UK number", { it?.startsWith("GB") == true }) {
                            mustHaveLength(11)
                        }
                    }
                }
            }
        }

        // Assert
        assertEquals(
            listOf("Customer.vatNumber must have a length of 11. (when is a UK number)"),
            lines(registry.validate(Customer("Ada", vatNumber = "GB1"))),
        )
        assertTrue(registry.validate(Customer("Ada", vatNumber = "FR1")).isValid)
    }

    @Test
    fun `TEST mode should report every rule and both branches of every condition`() {

        // Arrange
        val registry = graphs {
            graph<Customer> {
                validate {
                    Customer::name { mustNotBeBlank() }
                    whenever(IsBusinessCustomer) {
                        Customer::vatNumber { mustNotBeNull() }
                    } otherwise {
                        Customer::vatNumber { mustBeNull() }
                    }
                }
            }
        }

        // Act
        val result = registry.validate(Customer("Ada"), ValidationMode.TEST)

        // Assert
        assertEquals(
            listOf(
                "Customer.name must not be blank.",
                "Customer.vatNumber must not be null. (when is a business customer)",
                "Customer.vatNumber must be null. (when not is a business customer)",
            ),
            lines(result),
        )
    }

    @Test
    fun `describe should list every rule with wildcard paths, nested graphs, and subtypes`() {

        // Arrange
        val registry = graphs {
            include(orderGraph, paymentGraph, cardGraph)
            graph<Customer> {
                validate {
                    Customer::name { mustNotBeBlank() }
                    Customer::tags.each { mustNotBeBlank() }
                }
            }
        }

        // Act
        val result = registry.describe<Customer>()

        // Assert
        assertEquals(ValidationMode.TEST, result.mode)
        assertEquals(
            listOf(
                "Customer.name must not be blank.",
                "Customer.tags[*] must not be blank.",
                "Customer.orders[*].total must be greater than 0.",
                "Customer.orders[*].payment.amount must be greater than 0.",
                "Customer.orders[*].payment.pan must be a valid Mod10 (Luhn) checksum. (when Card)",
            ),
            lines(result),
        )
    }

    @Test
    fun `all and allExcept should apply rules to every member except those excluded`() {

        // Arrange
        val registry = graphs {
            graph<Order> {
                validate { all { mustNotBeNull() } }
            }
            graph<Card> {
                validate { allExcept(Card::pan) { mustNotBeNull() } }
            }
        }

        // Act
        val order = registry.describe<Order>()
        val card = registry.describe<Card>()

        // Assert
        assertEquals(
            listOf("Order.total must not be null.", "Order.payment must not be null.", "Order.payment.amount must not be null. (when Card)"),
            lines(order),
        )
        assertEquals(listOf("Card.amount must not be null."), lines(card))
    }

    @Test
    fun `custom rules should carry their identifier, message, and parameters`() {

        // Arrange
        val registry = graphs {
            graph<Customer> {
                validate {
                    Customer::vatNumber {
                        must("customer.vatNumber", "must be a valid VAT number.", mapOf("country" to "GB")) { it.length == 11 }
                    }
                }
            }
        }

        // Act
        val failure = registry.validate(Customer("Ada", vatNumber = "GB1")).failures.single()

        // Assert
        assertEquals("customer.vatNumber", failure.rule)
        assertEquals("must be a valid VAT number.", failure.message)
        assertEquals(mapOf("country" to "GB"), failure.parameters)
    }

    @Test
    fun `results should support path queries and orThrow`() {

        // Arrange
        val registry = graphs {
            include(orderGraph)
            graph<Customer> { validate { Customer::name { mustNotBeBlank() } } }
        }
        val customer = Customer(" ", orders = listOf(Order(BigDecimal("-1")), Order(BigDecimal("-2"))))

        // Act
        val result = registry.validate(customer)

        // Assert
        assertEquals(1, result.at(GraphPath.of(Customer::name)).size)
        assertEquals(1, result.under(GraphPath.of(Customer::orders).index(1)).size)
        assertEquals(2, result.matching(GraphPath.of(Customer::orders).anyIndex().property(Order::total)).size)
        assertEquals(listOf("name", "orders"), result.root.children.map { (it.path.segments.last() as io.onixlabs.kotlin.graph.GraphPathSegment.Property).name })
        val exception = assertThrows<ValidationException> { result.orThrow() }
        assertSame(result, exception.result)
        assertTrue(registry.validate(Customer("Ada")).let { it.orThrow() === it })
        assertFalse(result.isValid)
    }

    @Test
    fun `results should render as flat JSON and as JSON shaped like the subject`() {

        // Arrange
        val registry = graphs {
            include(orderGraph)
            graph<Customer> {
                validate {
                    Customer::name { mustNotBeBlank() }
                    Customer::orders { mustHaveCountWithinRange(1, 1) }
                    Customer::attributes.keys { mustNotBeBlank() }
                }
            }
        }
        val customer = Customer("", orders = listOf(Order(BigDecimal.ONE), Order(BigDecimal("-5"))), attributes = mapOf(" " to "x"))

        // Act
        val result = registry.validate(customer)

        // Assert
        assertEquals(
            """{"valid":false,"failures":[""" +
                """{"path":"/name","rule":"string.notBlank","parameters":{},"message":"must not be blank."},""" +
                """{"path":"/orders","rule":"iterable.countWithinRange","parameters":{"minimum":1,"maximum":1},"message":"must contain between 1 and 1 elements."},""" +
                """{"path":"/orders/1/total","rule":"comparable.greaterThan","parameters":{"value":0},"message":"must be greater than 0."},""" +
                """{"path":"/attributes/ ","rule":"string.notBlank","parameters":{},"message":"must not be blank."}]}""",
            result.toFlatJson(),
        )
        assertEquals(
            """{"valid":false,"errors":{""" +
                """"name":{"${'$'}failures":[{"rule":"string.notBlank","parameters":{},"message":"must not be blank."}]},""" +
                """"orders":{"${'$'}failures":[{"rule":"iterable.countWithinRange","parameters":{"minimum":1,"maximum":1},"message":"must contain between 1 and 1 elements."}],""" +
                """"1":{"total":{"${'$'}failures":[{"rule":"comparable.greaterThan","parameters":{"value":0},"message":"must be greater than 0."}]}}},""" +
                """"attributes":{"${'$'}keys":{" ":{"${'$'}failures":[{"rule":"string.notBlank","parameters":{},"message":"must not be blank."}]}}}}}""",
            result.toTreeJson(),
        )
    }

    @Test
    fun `members declined by the access policy should be neither validated nor descended into`() {

        // Arrange
        val registry = graphs {
            include(orderGraph)
            memberAccess { _, property -> property.name != "orders" }
            graph<Customer> { validate { Customer::orders { mustBeEmpty() } } }
        }

        // Act
        val result = registry.validate(Customer("Ada", orders = listOf(Order(BigDecimal("-1")))))

        // Assert
        assertTrue(result.isValid)
    }
}
