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

import io.onixlabs.kotlin.core.specifications.iterable.IterableContainsAllSpecification
import io.onixlabs.kotlin.core.specifications.iterable.IterableContainsAnySpecification
import io.onixlabs.kotlin.core.specifications.iterable.IterableContainsExactlySpecification
import io.onixlabs.kotlin.core.specifications.iterable.IterableContainsOrderedExactlySpecification
import io.onixlabs.kotlin.core.specifications.iterable.IterableContainsSpecification
import io.onixlabs.kotlin.core.specifications.iterable.IterableDistinctSpecification
import io.onixlabs.kotlin.core.specifications.iterable.IterableEmptySpecification
import io.onixlabs.kotlin.core.specifications.iterable.IterableExactCountSpecification
import io.onixlabs.kotlin.core.specifications.iterable.IterableRangeCountSpecification
import io.onixlabs.kotlin.core.specifications.iterable.IterableSingleSpecification

/**
 * Requires the collection to contain the specified value. Rule `iterable.contains` with parameter `value`.
 *
 * @param value The value that the collection must contain.
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustContain(value: E, message: String = "must contain $value.") {
    rule("iterable.contains", message, IterableContainsSpecification(value), mapOf("value" to value))
}

/**
 * Requires the collection not to contain the specified value. Rule `iterable.notContains` with parameter `value`.
 *
 * @param value The value that the collection must not contain.
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustNotContain(value: E, message: String = "must not contain $value.") {
    rule("iterable.notContains", message, IterableContainsSpecification(value).not(), mapOf("value" to value))
}

/**
 * Requires the collection to contain all of the specified values. Rule `iterable.containsAll` with parameter `values`.
 *
 * @param values The values that the collection must contain.
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustContainAll(
    values: Iterable<E>,
    message: String = "must contain all of: ${values.joinToString()}.",
) {
    rule("iterable.containsAll", message, IterableContainsAllSpecification(values), mapOf("values" to values.toList()))
}

/**
 * Requires the collection to contain any of the specified values. Rule `iterable.containsAny` with parameter `values`.
 *
 * @param values The values, one of which the collection must contain.
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustContainAny(
    values: Iterable<E>,
    message: String = "must contain any of: ${values.joinToString()}.",
) {
    rule("iterable.containsAny", message, IterableContainsAnySpecification(values), mapOf("values" to values.toList()))
}

/**
 * Requires the collection not to contain any of the specified values. Rule `iterable.notContainsAny` with parameter `values`.
 *
 * @param values The values, none of which the collection may contain.
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustNotContainAny(
    values: Iterable<E>,
    message: String = "must not contain any of: ${values.joinToString()}.",
) {
    rule("iterable.notContainsAny", message, IterableContainsAnySpecification(values).not(), mapOf("values" to values.toList()))
}

/**
 * Requires the collection to contain exactly the specified values, in any order. Rule `iterable.containsExactly` with parameter `values`.
 *
 * @param values The values that the collection must contain exactly.
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustContainExactly(
    values: Iterable<E>,
    message: String = "must contain exactly: ${values.joinToString()}.",
) {
    rule("iterable.containsExactly", message, IterableContainsExactlySpecification(values), mapOf("values" to values.toList()))
}

/**
 * Requires the collection to contain exactly the specified values, in order. Rule `iterable.containsOrderedExactly` with parameter `values`.
 *
 * @param values The values that the collection must contain exactly, in order.
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustContainOrderedExactly(
    values: Iterable<E>,
    message: String = "must contain exactly, in order: ${values.joinToString()}.",
) {
    rule("iterable.containsOrderedExactly", message, IterableContainsOrderedExactlySpecification(values), mapOf("values" to values.toList()))
}

/**
 * Requires the collection to contain exactly the specified number of elements. Rule `iterable.count` with parameter `count`.
 *
 * @param count The number of elements that the collection must contain.
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustHaveCount(count: Int, message: String = "must contain exactly $count elements.") {
    rule("iterable.count", message, IterableExactCountSpecification<E>(count), mapOf("count" to count))
}

/**
 * Requires the number of elements to be within the specified inclusive range. Rule `iterable.countWithinRange` with parameters `minimum` and `maximum`.
 *
 * @param minimum The inclusive minimum number of elements.
 * @param maximum The inclusive maximum number of elements.
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustHaveCountWithinRange(
    minimum: Int,
    maximum: Int,
    message: String = "must contain between $minimum and $maximum elements.",
) {
    rule("iterable.countWithinRange", message, IterableRangeCountSpecification<E>(minimum, maximum), mapOf("minimum" to minimum, "maximum" to maximum))
}

/**
 * Requires the number of elements to be within the specified inclusive range. Rule `iterable.countWithinRange` with parameters `minimum` and `maximum`.
 *
 * @param range The inclusive range of the number of elements.
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustHaveCountWithinRange(
    range: IntRange,
    message: String = "must contain between ${range.first} and ${range.last} elements.",
) {
    mustHaveCountWithinRange(range.first, range.last, message)
}

/**
 * Requires the collection to be empty. Rule `iterable.empty`.
 *
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustBeEmpty(message: String = "must be empty.") {
    rule("iterable.empty", message, IterableEmptySpecification<E>())
}

/**
 * Requires the collection not to be empty. Rule `iterable.notEmpty`.
 *
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustNotBeEmpty(message: String = "must not be empty.") {
    rule("iterable.notEmpty", message, IterableEmptySpecification<E>().not())
}

/**
 * Requires the collection to contain exactly one element. Rule `iterable.single`.
 *
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustBeSingle(message: String = "must contain a single element.") {
    rule("iterable.single", message, IterableSingleSpecification<E>())
}

/**
 * Requires the elements of the collection to be distinct. Rule `iterable.distinct`.
 *
 * @param message The message reported when the rule fails.
 */
public fun <E> MemberValidationBuilder<out Iterable<E>?>.mustBeDistinct(message: String = "must contain distinct elements.") {
    rule("iterable.distinct", message, IterableDistinctSpecification<E>())
}
