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

import io.onixlabs.kotlin.core.specifications.comparable.ComparableGreaterThanOrEqualSpecification
import io.onixlabs.kotlin.core.specifications.comparable.ComparableGreaterThanSpecification
import io.onixlabs.kotlin.core.specifications.comparable.ComparableLessThanOrEqualSpecification
import io.onixlabs.kotlin.core.specifications.comparable.ComparableLessThanSpecification
import io.onixlabs.kotlin.core.specifications.comparable.ComparableRangeSpecification

/**
 * Requires the value to be within the specified inclusive range. Rule `comparable.withinRange` with parameters `minimum` and `maximum`.
 *
 * @param minimum The inclusive minimum.
 * @param maximum The inclusive maximum.
 * @param message The message reported when the rule fails.
 */
public fun <C : Comparable<C>> MemberValidationBuilder<out C?>.mustBeWithinRange(
    minimum: C,
    maximum: C,
    message: String = "must be within the range $minimum to $maximum.",
) {
    rule("comparable.withinRange", message, ComparableRangeSpecification(minimum, maximum), mapOf("minimum" to minimum, "maximum" to maximum))
}

/**
 * Requires the value to be within the specified inclusive range. Rule `comparable.withinRange` with parameters `minimum` and `maximum`.
 *
 * @param range The inclusive range.
 * @param message The message reported when the rule fails.
 */
public fun <C : Comparable<C>> MemberValidationBuilder<out C?>.mustBeWithinRange(
    range: ClosedRange<C>,
    message: String = "must be within the range ${range.start} to ${range.endInclusive}.",
) {
    mustBeWithinRange(range.start, range.endInclusive, message)
}

/**
 * Requires the value to be greater than the specified value. Rule `comparable.greaterThan` with parameter `value`.
 *
 * @param value The exclusive lower bound.
 * @param message The message reported when the rule fails.
 */
public fun <C : Comparable<C>> MemberValidationBuilder<out C?>.mustBeGreaterThan(value: C, message: String = "must be greater than $value.") {
    rule("comparable.greaterThan", message, ComparableGreaterThanSpecification(value), mapOf("value" to value))
}

/**
 * Requires the value to be greater than or equal to the specified value. Rule `comparable.greaterThanOrEqualTo` with parameter `value`.
 *
 * @param value The inclusive lower bound.
 * @param message The message reported when the rule fails.
 */
public fun <C : Comparable<C>> MemberValidationBuilder<out C?>.mustBeGreaterThanOrEqualTo(
    value: C,
    message: String = "must be greater than or equal to $value.",
) {
    rule("comparable.greaterThanOrEqualTo", message, ComparableGreaterThanOrEqualSpecification(value), mapOf("value" to value))
}

/**
 * Requires the value to be less than the specified value. Rule `comparable.lessThan` with parameter `value`.
 *
 * @param value The exclusive upper bound.
 * @param message The message reported when the rule fails.
 */
public fun <C : Comparable<C>> MemberValidationBuilder<out C?>.mustBeLessThan(value: C, message: String = "must be less than $value.") {
    rule("comparable.lessThan", message, ComparableLessThanSpecification(value), mapOf("value" to value))
}

/**
 * Requires the value to be less than or equal to the specified value. Rule `comparable.lessThanOrEqualTo` with parameter `value`.
 *
 * @param value The inclusive upper bound.
 * @param message The message reported when the rule fails.
 */
public fun <C : Comparable<C>> MemberValidationBuilder<out C?>.mustBeLessThanOrEqualTo(
    value: C,
    message: String = "must be less than or equal to $value.",
) {
    rule("comparable.lessThanOrEqualTo", message, ComparableLessThanOrEqualSpecification(value), mapOf("value" to value))
}
