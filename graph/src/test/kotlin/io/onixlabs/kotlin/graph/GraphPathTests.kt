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

import io.onixlabs.kotlin.core.toLowerSnakeCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GraphPathTests {

    private val orderTotal = GraphPath.root<Customer>().property(Customer::orders).index(2).property(Order::total)
    private val attributeValue = GraphPath.root<Customer>().property(Customer::attributes).entry("colour")
    private val attributeKey = GraphPath.root<Customer>().property(Customer::attributes).entryKey("colour")

    @Test
    fun `GraphPath should format in the Kotlin style by default`() {

        // Assert
        assertEquals("Customer.orders[2].total", orderTotal.toString())
        assertEquals("Customer.attributes[\"colour\"]", attributeValue.toString())
        assertEquals("Customer.attributes.keys[\"colour\"]", attributeKey.toString())
        assertEquals("Customer.orders[*].total", GraphPath.of(Customer::orders).anyIndex().property(Order::total).toString())
    }

    @Test
    fun `GraphPath should escape string keys in the Kotlin style`() {

        // Arrange
        val path = GraphPath.root<Customer>().property(Customer::attributes).entry("say \"hi\"")

        // Assert
        assertEquals("Customer.attributes[\"say \\\"hi\\\"\"]", path.toString())
    }

    @Test
    fun `GraphPath should format as a JSON Pointer relative to the document root`() {

        // Arrange
        val path = GraphPath.root<Customer>().property(Customer::attributes).entry("a/b~c")

        // Assert
        assertEquals("/orders/2/total", orderTotal.format(GraphPathFormat.JsonPointer))
        assertEquals("/attributes/a~1b~0c", path.format(GraphPathFormat.JsonPointer))
        assertEquals("", GraphPath.root<Customer>().format(GraphPathFormat.JsonPointer))
    }

    @Test
    fun `GraphPath should format as a JSONPath relative to the document root`() {

        // Assert
        assertEquals("$.orders[2].total", orderTotal.format(GraphPathFormat.JsonPath))
        assertEquals("$.attributes['colour']", attributeValue.format(GraphPathFormat.JsonPath))
        assertEquals("$.orders[*]", GraphPath.of(Customer::orders).anyIndex().format(GraphPathFormat.JsonPath))
    }

    @Test
    fun `GraphPathFormat withNames should transform property names after existing transformations`() {

        // Arrange
        val path = GraphPath.root<Customer>().property("orderTotal")
        val snakeCase = GraphPathFormat.JsonPointer.withNames { it.toLowerSnakeCase() }
        val prefixed = snakeCase.withNames { "x_$it" }

        // Assert
        assertEquals("/order_total", path.format(snakeCase))
        assertEquals("/x_order_total", path.format(prefixed))
        assertEquals("/orderTotal", path.format(GraphPathFormat.JsonPointer))
    }

    @Test
    fun `GraphPath of should start at the type that declares the first property`() {

        // Act
        val path = GraphPath.of(Customer::orders, Order::total)

        // Assert
        assertEquals(Customer::class, path.root)
        assertEquals("Customer.orders.total", path.toString())
    }

    @Test
    fun `GraphPath should support equality, prefixes, parents, and wildcard matching`() {

        // Arrange
        val pattern = GraphPath.of(Customer::orders).anyIndex().property(Order::total)

        // Assert
        assertEquals(orderTotal, GraphPath.of(Customer::orders).index(2).property(Order::total))
        assertEquals(orderTotal.hashCode(), GraphPath.of(Customer::orders).index(2).property(Order::total).hashCode())
        assertTrue(orderTotal.startsWith(GraphPath.of(Customer::orders)))
        assertFalse(orderTotal.startsWith(GraphPath.of(Customer::attributes)))
        assertEquals(GraphPath.of(Customer::orders).index(2), orderTotal.parent)
        assertNull(GraphPath.root<Customer>().parent)
        assertTrue(orderTotal.matches(pattern))
        assertFalse(attributeValue.matches(pattern))
        assertTrue(orderTotal.isConcrete)
        assertFalse(pattern.isConcrete)
    }

    @Test
    fun `GraphPath should sort segment by segment`() {

        // Arrange
        val first = GraphPath.of(Customer::orders).index(2)
        val second = GraphPath.of(Customer::orders).index(10)
        val third = GraphPath.of(Customer::tags)

        // Act
        val sorted = listOf(third, second, orderTotal, first).sorted()

        // Assert
        assertEquals(listOf(first, orderTotal, second, third), sorted)
    }

    @Test
    fun `GraphPath index should reject negative indexes`() {

        // Act and Assert
        assertThrows<IllegalArgumentException> { GraphPath.root<Customer>().index(-1) }
    }
}
