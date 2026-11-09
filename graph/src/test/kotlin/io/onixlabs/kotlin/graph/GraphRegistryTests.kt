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
package io.onixlabs.kotlin.graph

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class GraphRegistryTests {

    private val paymentGraph = graph<Payment> { test("payment") }
    private val auditableGraph = graph<Auditable> { test("auditable") }
    private val cardGraph = graph<Card> { test("card") }

    @Test
    fun `graph should hold plugin definitions in declaration order`() {

        // Act
        val graph = graph<Customer> {
            test("first")
            test("second")
        }

        // Assert
        assertEquals(Customer::class, graph.type)
        assertEquals(setOf(TestPlugin), graph.plugins)
        assertEquals(listOf("first", "second"), graph.definitionsOf(TestPlugin).map { it.label })
        assertEquals("Graph<Customer>", graph.toString())
    }

    @Test
    fun `graph should be usable as a named type through delegation`() {

        // Act
        val registry = graphs { include(NamedCustomerGraph) }

        // Assert
        assertSame(NamedCustomerGraph, registry.graphFor(Customer::class))
        assertEquals("named", registry.graphFor(Customer::class)?.definitionsOf(TestPlugin)?.single()?.label)
    }

    @Test
    fun `graphsFor should order graphs from the most general to the most specific, including interfaces`() {

        // Arrange
        val registry = graphs { include(cardGraph, auditableGraph, paymentGraph) }

        // Act
        val result = registry.graphsFor(Card::class)

        // Assert
        assertEquals(listOf(auditableGraph, paymentGraph, cardGraph), result)
        assertSame(cardGraph, registry.mostSpecificGraphFor(Card::class))
        assertEquals(listOf(paymentGraph), registry.graphsFor(BankTransfer::class))
        assertTrue(registry.graphsFor(Customer::class).isEmpty())
    }

    @Test
    fun `graphFor should only return a graph for exactly the specified type`() {

        // Arrange
        val registry = graphs { include(paymentGraph) }

        // Assert
        assertSame(paymentGraph, registry.graphFor(Payment::class))
        assertNull(registry.graphFor(Card::class))
        assertTrue(registry.hasGraphFor(Card::class))
    }

    @Test
    fun `graphs should accept inline graphs and ignore the same graph included twice`() {

        // Act
        val registry = graphs {
            include(paymentGraph, paymentGraph)
            graph<Customer> { test("inline") }
        }

        // Assert
        assertEquals(listOf(Payment::class, Customer::class), registry.graphs.map { it.type })
    }

    @Test
    fun `graphs should reject more than one graph for a type`() {

        // Act
        val exception = assertThrows<GraphConfigurationException> {
            graphs { include(paymentGraph, graph<Payment>()) }
        }

        // Assert
        assertEquals("More than one graph is registered for io.onixlabs.kotlin.graph.Payment.", exception.message)
    }

    @Test
    fun `graphs should report every problem found by verifiable definitions`() {

        // Act
        val exception = assertThrows<GraphConfigurationException> {
            graphs {
                graph<Customer> { test("auditing", requires = Auditable::class) }
                graph<Order> { test("payment", requires = Payment::class) }
            }
        }

        // Assert
        assertEquals(
            listOf("Graph<Customer> requires a graph for Auditable.", "Graph<Order> requires a graph for Payment."),
            exception.problems,
        )
        assertTrue(exception.message!!.startsWith("Found 2 graph configuration problems:"))
    }

    @Test
    fun `graphs should pass verification when required graphs are registered`() {

        // Act
        val registry = graphs {
            graph<Customer> { test("orders", requires = Order::class) }
            graph<Order>()
        }

        // Assert
        assertEquals(2, registry.graphs.size)
    }

    @Test
    fun `membersOf should order members by constructor position and exclude infrastructure members`() {

        // Arrange
        val plain = graphs { }
        val excluding = graphs { excludeMembersDeclaredBy(FrameworkEntity::class) }

        // Assert
        assertEquals(listOf("name", "orders", "attributes", "tags", "notes"), plain.membersOf(Customer::class).map { it.name })
        assertEquals(listOf("name", "email", "database", "dirty"), plain.membersOf(PersistentCustomer::class).map { it.name })
        assertEquals(listOf("name", "email"), excluding.membersOf(PersistentCustomer::class).map { it.name })
    }

    @Test
    fun `membersOf should include members inherited from domain supertypes`() {

        // Arrange
        val registry = graphs { }

        // Assert
        assertEquals(listOf("amount", "pan", "createdBy"), registry.membersOf(Card::class).map { it.name })
    }

    @Test
    fun `read should honour the member access policy`() {

        // Arrange
        val customer = Customer("Ada", notes = "secret")
        val registry = graphs { memberAccess { _, property -> property.name != "notes" } }

        // Assert
        assertEquals(GraphMemberRead.Value("Ada"), registry.read(customer, Customer::name))
        assertEquals(GraphMemberRead.Unavailable, registry.read(customer, Customer::notes))
        assertTrue(registry.valuesOf(customer, GraphMember.Property(Customer::notes), GraphPath.root<Customer>()).isEmpty())
    }

    @Test
    fun `valuesOf should read properties, elements, keys, and values with concrete paths`() {

        // Arrange
        val order = Order(BigDecimal.TEN, BankTransfer(BigDecimal.TEN, "GB00"))
        val customer = Customer("Ada", listOf(order, order), mapOf("colour" to "red"), arrayOf("a", "b"))
        val registry = graphs { }
        val root = GraphPath.root<Customer>()

        // Act
        val name = registry.valuesOf(customer, GraphMember.Property(Customer::name), root)
        val orders = registry.valuesOf(customer, GraphMember.Elements(Customer::orders), root)
        val tags = registry.valuesOf(customer, GraphMember.Elements(Customer::tags), root)
        val keys = registry.valuesOf(customer, GraphMember.Keys(Customer::attributes), root)
        val values = registry.valuesOf(customer, GraphMember.Values(Customer::attributes), root)

        // Assert
        assertEquals(listOf(GraphMemberValue(root.property("name"), "Ada")), name)
        assertEquals(listOf("Customer.orders[0]", "Customer.orders[1]"), orders.map { it.path.toString() })
        assertEquals(listOf("a", "b"), tags.map { it.value })
        assertEquals(listOf(GraphMemberValue(root.property("attributes").entryKey("colour"), "colour")), keys)
        assertEquals(listOf(GraphMemberValue(root.property("attributes").entry("colour"), "red")), values)
    }

    @Test
    fun `valuesOf should yield nothing for a null collection and reject a non-collection`() {

        // Arrange
        val customer = Customer("Ada")
        val registry = graphs { }
        val root = GraphPath.root<Customer>()

        // Assert
        assertTrue(registry.valuesOf(customer, GraphMember.Elements(Customer::notes), root).isEmpty())
        assertThrows<IllegalArgumentException> { registry.valuesOf(customer, GraphMember.Elements(Customer::name), root) }
    }

    @Test
    fun `GraphMember pathFrom should produce wildcard paths for elements and entries`() {

        // Arrange
        val root = GraphPath.root<Customer>()

        // Assert
        assertEquals("Customer.name", GraphMember.Property(Customer::name).pathFrom(root).toString())
        assertEquals("Customer.orders[*]", GraphMember.Elements(Customer::orders).pathFrom(root).toString())
        assertEquals("Customer.attributes.keys[*]", GraphMember.Keys(Customer::attributes).pathFrom(root).toString())
        assertEquals("Customer.attributes[*]", GraphMember.Values(Customer::attributes).pathFrom(root).toString())
    }
}
