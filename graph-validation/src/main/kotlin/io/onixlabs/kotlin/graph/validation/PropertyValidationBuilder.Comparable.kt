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
import io.onixlabs.kotlin.graph.GraphDslMarker

@GraphDslMarker
public fun <T : Comparable<T>> Receiver<T>.mustBeWithinRange(
    minimum: T,
    maximum: T,
    message: String = "must be within the specified range: $minimum to $maximum."
): Unit = addAssertion(ComparableRangeSpecification(minimum, maximum), message)

@GraphDslMarker
public fun <T : Comparable<T>> Receiver<T>.mustBeWithinRange(
    range: ClosedRange<T>,
    message: String = "must be within the specified range: ${range.start} to ${range.endInclusive}."
): Unit = addAssertion(ComparableRangeSpecification(range), message)


@GraphDslMarker
public fun <T : Comparable<T>> Receiver<T>.mustBeGreaterThan(
    value: T,
    message: String = "must be greater than the specified value: $value."
): Unit = addAssertion(ComparableGreaterThanSpecification(value), message)

@GraphDslMarker
public fun <T : Comparable<T>> Receiver<T>.mustBeGreaterThanOrEqualTo(
    value: T,
    message: String = "must be greater than or equal to the specified value: $value."
): Unit = addAssertion(ComparableGreaterThanOrEqualSpecification(value), message)

@GraphDslMarker
public fun <T : Comparable<T>> Receiver<T>.mustBeLessThan(
    value: T,
    message: String = "must be less than the specified value: $value."
): Unit = addAssertion(ComparableLessThanSpecification(value), message)

@GraphDslMarker
public fun <T : Comparable<T>> Receiver<T>.mustBeLessThanOrEqualTo(
    value: T,
    message: String = "must be less than or equal to the specified value: $value."
): Unit = addAssertion(ComparableLessThanOrEqualSpecification(value), message)
