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

import io.onixlabs.kotlin.core.isEqualTo
import io.onixlabs.kotlin.core.isEqualToAny

/**
 * Requires the value to be null. Rule `generic.null`.
 *
 * @param message The message reported when the rule fails.
 */
public fun <V> MemberValidationBuilder<V>.mustBeNull(message: String = "must be null.") {
    rule("generic.null", message, judgesNull = true) { it == null }
}

/**
 * Requires the value not to be null. Rule `generic.notNull`.
 *
 * @param message The message reported when the rule fails.
 */
public fun <V> MemberValidationBuilder<V>.mustNotBeNull(message: String = "must not be null.") {
    rule("generic.notNull", message, judgesNull = true) { it != null }
}

/**
 * Requires the value to be equal to the specified value. Rule `generic.equalTo` with parameter `value`.
 *
 * @param value The value that the member must equal.
 * @param message The message reported when the rule fails.
 */
public fun <V> MemberValidationBuilder<V>.mustBeEqualTo(value: V, message: String = "must be equal to $value.") {
    rule("generic.equalTo", message, mapOf("value" to value)) { it.isEqualTo(value) }
}

/**
 * Requires the value not to be equal to the specified value. Rule `generic.notEqualTo` with parameter `value`.
 *
 * @param value The value that the member must not equal.
 * @param message The message reported when the rule fails.
 */
public fun <V> MemberValidationBuilder<V>.mustNotBeEqualTo(value: V, message: String = "must not be equal to $value.") {
    rule("generic.notEqualTo", message, mapOf("value" to value)) { !it.isEqualTo(value) }
}

/**
 * Requires the value to be equal to any of the specified values. Rule `generic.equalToAny` with parameter `values`.
 *
 * @param values The values, one of which the member must equal.
 * @param message The message reported when the rule fails.
 */
public fun <V> MemberValidationBuilder<V>.mustBeEqualToAny(
    values: Iterable<V>,
    message: String = "must be equal to any of: ${values.joinToString()}.",
) {
    rule("generic.equalToAny", message, mapOf("values" to values.toList())) { it.isEqualToAny(values) }
}

/**
 * Requires the value not to be equal to any of the specified values. Rule `generic.notEqualToAny` with parameter `values`.
 *
 * @param values The values, none of which the member may equal.
 * @param message The message reported when the rule fails.
 */
public fun <V> MemberValidationBuilder<V>.mustNotBeEqualToAny(
    values: Iterable<V>,
    message: String = "must not be equal to any of: ${values.joinToString()}.",
) {
    rule("generic.notEqualToAny", message, mapOf("values" to values.toList())) { !it.isEqualToAny(values) }
}

/**
 * Requires the value to satisfy the specified predicate. Rule `generic.satisfies`; use `must` for a rule with its own identifier.
 *
 * @param predicate The predicate that the value must satisfy.
 * @param message The message reported when the rule fails.
 */
@Suppress("UNCHECKED_CAST")
public fun <V : Any> MemberValidationBuilder<out V?>.mustSatisfy(predicate: (V) -> Boolean, message: String = "must satisfy the specified predicate.") {
    rule("generic.satisfies", message) { predicate(it as V) }
}

/**
 * Requires the value not to satisfy the specified predicate. Rule `generic.notSatisfies`; use `must` for a rule with its own identifier.
 *
 * @param predicate The predicate that the value must not satisfy.
 * @param message The message reported when the rule fails.
 */
@Suppress("UNCHECKED_CAST")
public fun <V : Any> MemberValidationBuilder<out V?>.mustNotSatisfy(predicate: (V) -> Boolean, message: String = "must not satisfy the specified predicate.") {
    rule("generic.notSatisfies", message) { !predicate(it as V) }
}

/**
 * Requires the value to be the same instance as the specified value (`===`). Rule `generic.sameAs` with parameter `value`.
 *
 * @param value The instance that the member must be.
 * @param message The message reported when the rule fails.
 */
public fun <V> MemberValidationBuilder<V>.mustBeEqualByReferenceTo(value: V, message: String = "must be the same instance as $value.") {
    rule("generic.sameAs", message, mapOf("value" to value)) { it === value }
}

/**
 * Requires the value not to be the same instance as the specified value (`!==`). Rule `generic.notSameAs` with parameter `value`.
 *
 * @param value The instance that the member must not be.
 * @param message The message reported when the rule fails.
 */
public fun <V> MemberValidationBuilder<V>.mustNotBeEqualByReferenceTo(value: V, message: String = "must not be the same instance as $value.") {
    rule("generic.notSameAs", message, mapOf("value" to value)) { it !== value }
}
