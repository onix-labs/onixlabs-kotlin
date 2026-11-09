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
import io.onixlabs.kotlin.graph.GraphDslMarker

/**
 * Represents the builder for the rules applied to the value of a member, such as `Customer::name { mustNotBeBlank() }`.
 * Rules are evaluated in declaration order. Every rule except the null checks passes when the value is null, so a
 * required member declares `mustNotBeNull()` explicitly.
 *
 * @param V The type of the member's value.
 */
@GraphDslMarker
public class MemberValidationBuilder<V> internal constructor() {

    private val items: MutableList<() -> MemberItem> = mutableListOf()
    private var requireGraph: Boolean = false
    private var skipGraph: Boolean = false

    /**
     * Requires a graph to be registered for the member's type. If none is, building the registry fails with a
     * [io.onixlabs.kotlin.graph.GraphConfigurationException]. Use this where silently skipping nested validation
     * would be dangerous.
     */
    public fun requireGraph() {
        requireGraph = true
    }

    /**
     * Stops validation from descending into the registered graphs of the member's value, for example for
     * back-references or expensive subtrees. Rules declared on the member itself still apply.
     */
    public fun skipGraph() {
        skipGraph = true
    }

    /**
     * Applies the specified rules only when the value satisfies the specified condition.
     *
     * @param condition The condition that the value must satisfy.
     * @param block The block that declares the rules that apply when the condition is satisfied.
     * @return Returns a handle with which to declare the rules that apply when the condition is not satisfied.
     */
    @Suppress("UNCHECKED_CAST")
    public fun whenever(condition: Specification<in V>, block: MemberValidationBuilder<V>.() -> Unit): MemberOtherwise<V> {
        return whenever(condition.description, { value -> (condition as Specification<Any?>).isSatisfiedBy(value) }, block)
    }

    /**
     * Applies the specified rules only when the value satisfies the specified named predicate.
     *
     * @param description The description of the condition, used when describing rules.
     * @param predicate The predicate that the value must satisfy.
     * @param block The block that declares the rules that apply when the condition is satisfied.
     * @return Returns a handle with which to declare the rules that apply when the condition is not satisfied.
     */
    @Suppress("UNCHECKED_CAST")
    public fun whenever(description: String, predicate: (V) -> Boolean, block: MemberValidationBuilder<V>.() -> Unit): MemberOtherwise<V> {
        val then = MemberValidationBuilder<V>().apply(block).buildItems()
        val handle = MemberOtherwise<V>()
        items += { MemberConditionalItem(description, { predicate(it as V) }, then, handle.items) }
        return handle
    }

    /**
     * Adds a rule to the current member.
     *
     * @param id The namespaced identifier of the rule.
     * @param message The message reported when the rule fails.
     * @param parameters The parameters of the rule.
     * @param judgesNull Determines whether the rule judges null values; otherwise, the rule passes on null.
     * @param test The test applied to the value.
     */
    internal fun rule(id: String, message: String, parameters: Map<String, Any?> = emptyMap(), judgesNull: Boolean = false, test: (Any?) -> Boolean) {
        val rule = ValidationRule(id, parameters, message, judgesNull, test)
        items += { RuleItem(rule) }
    }

    /**
     * Adds a rule backed by the specified specification to the current member.
     *
     * @param id The namespaced identifier of the rule.
     * @param message The message reported when the rule fails.
     * @param specification The specification that the value must satisfy.
     * @param parameters The parameters of the rule.
     * @param judgesNull Determines whether the rule judges null values; otherwise, the rule passes on null.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun rule(id: String, message: String, specification: Specification<*>, parameters: Map<String, Any?> = emptyMap(), judgesNull: Boolean = false) {
        rule(id, message, parameters, judgesNull) { (specification as Specification<Any?>).isSatisfiedBy(it) }
    }

    /**
     * Builds the items of the current builder.
     *
     * @return Returns the items of the current builder.
     */
    internal fun buildItems(): List<MemberItem> = items.map { it() }

    /**
     * Builds the body of the current builder.
     *
     * @return Returns the body of the current builder.
     */
    internal fun build(): MemberBody = MemberBody(buildItems(), requireGraph, skipGraph)
}

/**
 * Represents a handle with which to declare the rules that apply to a member when a `whenever` condition is not satisfied.
 *
 * @param V The type of the member's value.
 */
@GraphDslMarker
public class MemberOtherwise<V> internal constructor() {

    internal var items: List<MemberItem> = emptyList()
        private set

    /**
     * Declares the rules that apply when the condition is not satisfied.
     *
     * @param block The block that declares the rules that apply when the condition is not satisfied.
     */
    public infix fun otherwise(block: MemberValidationBuilder<V>.() -> Unit) {
        items = MemberValidationBuilder<V>().apply(block).buildItems()
    }
}

/**
 * Adds a custom rule backed by the specified specification.
 *
 * @param S The type of value tested by the specification.
 * @param specification The specification that the value must satisfy.
 * @param id The namespaced identifier of the rule, such as `customer.vatNumber`.
 * @param message The message reported when the rule fails.
 * @param parameters The parameters of the rule.
 */
@Suppress("UNCHECKED_CAST")
public fun <S : Any> MemberValidationBuilder<out S?>.must(
    specification: Specification<S>,
    id: String,
    message: String = "must satisfy ${specification.description}.",
    parameters: Map<String, Any?> = emptyMap(),
) {
    rule(id, message, parameters) { specification.isSatisfiedBy(it as S) }
}

/**
 * Adds a custom rule backed by the specified predicate.
 *
 * @param S The type of value tested by the predicate.
 * @param id The namespaced identifier of the rule, such as `customer.vatNumber`.
 * @param message The message reported when the rule fails.
 * @param parameters The parameters of the rule.
 * @param predicate The predicate that the value must satisfy.
 */
@Suppress("UNCHECKED_CAST")
public fun <S : Any> MemberValidationBuilder<out S?>.must(
    id: String,
    message: String,
    parameters: Map<String, Any?> = emptyMap(),
    predicate: (S) -> Boolean,
) {
    rule(id, message, parameters) { predicate(it as S) }
}
