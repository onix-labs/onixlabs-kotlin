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

import java.math.BigDecimal

data class Customer(
    val name: String,
    val orders: List<Order> = emptyList(),
    val attributes: Map<String, String> = emptyMap(),
    val tags: Array<String> = emptyArray(),
    val notes: String? = null,
)

data class Order(val total: BigDecimal, val payment: Payment)

interface Auditable {
    val createdBy: String
}

sealed interface Payment {
    val amount: BigDecimal
}

data class Card(override val amount: BigDecimal, val pan: String, override val createdBy: String = "system") : Payment, Auditable

data class BankTransfer(override val amount: BigDecimal, val iban: String) : Payment

abstract class FrameworkEntity {
    val database: String = "db"
    val dirty: Boolean = false
}

class PersistentCustomer(val name: String, val email: String) : FrameworkEntity()

/**
 * A test plugin whose definitions record a label and optionally require the graph of a type.
 */
object TestPlugin : GraphPlugin<TestDefinition>("test")

data class TestDefinition(val label: String, val requires: kotlin.reflect.KClass<*>? = null) : GraphVerifiable {
    override fun verify(graph: Graph<*>, registry: GraphRegistry): List<String> {
        return if (requires == null || registry.hasGraphFor(requires)) emptyList()
        else listOf("$graph requires a graph for ${requires.simpleName}.")
    }
}

fun GraphBuilder<*>.test(label: String, requires: kotlin.reflect.KClass<*>? = null) {
    define(TestPlugin, TestDefinition(label, requires))
}

object NamedCustomerGraph : Graph<Customer> by graph({ test("named") })
