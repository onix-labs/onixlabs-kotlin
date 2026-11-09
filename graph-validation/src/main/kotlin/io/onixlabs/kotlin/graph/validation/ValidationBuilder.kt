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
import io.onixlabs.kotlin.graph.GraphMember
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Represents the builder for the validation rules of a graph, declared by `validate { }`.
 *
 * Members are selected in either of two equivalent forms: the reference form (`Customer::name { }`,
 * `Customer::orders.each { }`, `Customer::attributes.keys { }`, `Customer::attributes.values { }`), or the verb form
 * (`property(Customer::name) { }`, `each(...)`, `keys(...)`, `values(...)`). Groups of members are selected with
 * `properties(...)`, `all { }`, and `allExcept(...)`.
 *
 * @param T The type of the subject.
 */
@GraphDslMarker
public class ValidationBuilder<T : Any> internal constructor(private val type: KClass<T>) {

    private val nodes: MutableList<() -> SubjectNode> = mutableListOf()

    /**
     * Declares rules for the value of the current property.
     *
     * @param R The type of the property.
     * @param block The block that declares the rules.
     */
    public operator fun <R> KProperty1<T, R>.invoke(block: MemberValidationBuilder<R>.() -> Unit) {
        property(this, block)
    }

    /**
     * Declares rules for each element of the current collection property.
     *
     * @param E The type of the elements.
     * @param block The block that declares the rules.
     */
    @JvmName("eachOfIterableReference")
    public fun <E> KProperty1<T, Iterable<E>?>.each(block: MemberValidationBuilder<E>.() -> Unit) {
        each(this, block)
    }

    /**
     * Declares rules for each element of the current array property.
     *
     * @param E The type of the elements.
     * @param block The block that declares the rules.
     */
    @JvmName("eachOfArrayReference")
    public fun <E> KProperty1<T, Array<out E>?>.each(block: MemberValidationBuilder<E>.() -> Unit) {
        each(this, block)
    }

    /**
     * Declares rules for each key of the current map property.
     *
     * @param K The type of the keys.
     * @param block The block that declares the rules.
     */
    @JvmName("keysOfReference")
    public fun <K> KProperty1<T, Map<K, *>?>.keys(block: MemberValidationBuilder<K>.() -> Unit) {
        keys(this, block)
    }

    /**
     * Declares rules for each value of the current map property.
     *
     * @param V The type of the values.
     * @param block The block that declares the rules.
     */
    @JvmName("valuesOfReference")
    public fun <V> KProperty1<T, Map<*, V>?>.values(block: MemberValidationBuilder<V>.() -> Unit) {
        values(this, block)
    }

    /**
     * Declares rules for the value of the specified property.
     *
     * @param R The type of the property.
     * @param property The property.
     * @param block The block that declares the rules.
     */
    public fun <R> property(property: KProperty1<T, R>, block: MemberValidationBuilder<R>.() -> Unit) {
        member(GraphMember.Property(property), block)
    }

    /**
     * Declares rules for each element of the specified collection property.
     *
     * @param E The type of the elements.
     * @param property The collection property.
     * @param block The block that declares the rules.
     */
    @JvmName("eachOfIterable")
    public fun <E> each(property: KProperty1<T, Iterable<E>?>, block: MemberValidationBuilder<E>.() -> Unit) {
        member(GraphMember.Elements(property), block)
    }

    /**
     * Declares rules for each element of the specified array property.
     *
     * @param E The type of the elements.
     * @param property The array property.
     * @param block The block that declares the rules.
     */
    @JvmName("eachOfArray")
    public fun <E> each(property: KProperty1<T, Array<out E>?>, block: MemberValidationBuilder<E>.() -> Unit) {
        member(GraphMember.Elements(property), block)
    }

    /**
     * Declares rules for each key of the specified map property.
     *
     * @param K The type of the keys.
     * @param property The map property.
     * @param block The block that declares the rules.
     */
    public fun <K> keys(property: KProperty1<T, Map<K, *>?>, block: MemberValidationBuilder<K>.() -> Unit) {
        member(GraphMember.Keys(property), block)
    }

    /**
     * Declares rules for each value of the specified map property.
     *
     * @param V The type of the values.
     * @param property The map property.
     * @param block The block that declares the rules.
     */
    public fun <V> values(property: KProperty1<T, Map<*, V>?>, block: MemberValidationBuilder<V>.() -> Unit) {
        member(GraphMember.Values(property), block)
    }

    /**
     * Declares the same rules for the values of each of the specified properties.
     *
     * @param R The common type of the properties.
     * @param properties The properties.
     * @param block The block that declares the rules.
     */
    public fun <R> properties(vararg properties: KProperty1<T, R>, block: MemberValidationBuilder<R>.() -> Unit) {
        for (selected in properties) property(selected, block)
    }

    /**
     * Declares the same rules for the values of every member of the subject.
     *
     * @param block The block that declares the rules.
     */
    public fun all(block: MemberValidationBuilder<Any?>.() -> Unit) {
        allExcept(block = block)
    }

    /**
     * Declares the same rules for the values of every member of the subject, except the specified properties.
     *
     * @param properties The properties to exclude.
     * @param block The block that declares the rules.
     */
    public fun allExcept(vararg properties: KProperty1<T, *>, block: MemberValidationBuilder<Any?>.() -> Unit) {
        val body = MemberValidationBuilder<Any?>().apply(block).build()
        val excluded = properties.mapTo(mutableSetOf()) { it.name }
        nodes += { AllMembersNode(excluded, body) }
    }

    /**
     * Applies the specified rules only when the subject satisfies the specified condition.
     *
     * @param condition The condition that the subject must satisfy.
     * @param block The block that declares the rules that apply when the condition is satisfied.
     * @return Returns a handle with which to declare the rules that apply when the condition is not satisfied.
     */
    @Suppress("UNCHECKED_CAST")
    public fun whenever(condition: Specification<in T>, block: ValidationBuilder<T>.() -> Unit): ValidationOtherwise<T> {
        return whenever(condition.description, { subject -> (condition as Specification<Any?>).isSatisfiedBy(subject) }, block)
    }

    /**
     * Applies the specified rules only when the subject satisfies the specified named predicate.
     *
     * @param description The description of the condition, used when describing rules.
     * @param predicate The predicate that the subject must satisfy.
     * @param block The block that declares the rules that apply when the condition is satisfied.
     * @return Returns a handle with which to declare the rules that apply when the condition is not satisfied.
     */
    @Suppress("UNCHECKED_CAST")
    public fun whenever(description: String, predicate: (T) -> Boolean, block: ValidationBuilder<T>.() -> Unit): ValidationOtherwise<T> {
        val then = ValidationBuilder(type).apply(block).build()
        val handle = ValidationOtherwise(type)
        nodes += { SubjectConditionalNode(description, { predicate(it as T) }, then, handle.nodes) }
        return handle
    }

    /**
     * Builds the nodes of the current builder.
     *
     * @return Returns the nodes of the current builder.
     */
    internal fun build(): List<SubjectNode> = nodes.map { it() }

    private fun <R> member(member: GraphMember, block: MemberValidationBuilder<R>.() -> Unit) {
        val body = MemberValidationBuilder<R>().apply(block).build()
        nodes += { MemberNode(member, body) }
    }
}

/**
 * Represents a handle with which to declare the rules that apply when a `whenever` condition is not satisfied.
 *
 * @param T The type of the subject.
 */
@GraphDslMarker
public class ValidationOtherwise<T : Any> internal constructor(private val type: KClass<T>) {

    internal var nodes: List<SubjectNode> = emptyList()
        private set

    /**
     * Declares the rules that apply when the condition is not satisfied.
     *
     * @param block The block that declares the rules that apply when the condition is not satisfied.
     */
    public infix fun otherwise(block: ValidationBuilder<T>.() -> Unit) {
        nodes = ValidationBuilder(type).apply(block).build()
    }
}
