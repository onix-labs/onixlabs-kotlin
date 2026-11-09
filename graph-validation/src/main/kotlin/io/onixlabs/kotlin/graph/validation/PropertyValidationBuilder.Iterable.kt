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
import io.onixlabs.kotlin.graph.GraphDslMarker

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustContain(
    value: T,
    message: String = "must contain the specified value: $value."
): Unit = addAssertion(IterableContainsSpecification(value), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustNotContain(
    value: T,
    message: String = "must not contain the specified value: $value."
): Unit = addAssertion(IterableContainsSpecification(value).not(), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustContainAll(
    values: Iterable<T>,
    message: String = "must contain all of the specified values: ${values.joinToString()}."
): Unit = addAssertion(IterableContainsAllSpecification(values), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustContainAny(
    values: Iterable<T>,
    message: String = "must contain any of the specified values: ${values.joinToString()}."
): Unit = addAssertion(IterableContainsAnySpecification(values), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustNotContainAny(
    values: Iterable<T>,
    message: String = "must not contain any of the specified values: ${values.joinToString()}."
): Unit = addAssertion(IterableContainsAnySpecification(values).not(), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustContainExactly(
    values: Iterable<T>,
    message: String = "must contain exactly the specified values: ${values.joinToString()}."
): Unit = addAssertion(IterableContainsExactlySpecification(values), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustContainOrderedExactly(
    values: Iterable<T>,
    message: String = "must contain exactly the specified values in the specified order: ${values.joinToString()}."
): Unit = addAssertion(IterableContainsOrderedExactlySpecification(values), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustHaveCount(
    count: Int,
    message: String = "must contain exactly the specified number of elements: $count."
): Unit = addAssertion(IterableExactCountSpecification(count), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustHaveCountWithinRange(
    minimum: Int,
    maximum: Int,
    message: String = "must contain the number of elements within the specified range: $minimum to $maximum."
): Unit = addAssertion(IterableRangeCountSpecification(minimum, maximum), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustHaveCountWithinRange(
    range: IntRange,
    message: String = "must contain the specified number of elements within range: ${range.first} to ${range.last}."
): Unit = addAssertion(IterableRangeCountSpecification(range), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustBeEmpty(
    message: String = "must be empty."
): Unit = addAssertion(IterableEmptySpecification(), message)


@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustNotBeEmpty(
    message: String = "must not be empty."
): Unit = addAssertion(IterableEmptySpecification<T>().not(), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustBeSingle(
    message: String = "must contain a single element."
): Unit = addAssertion(IterableSingleSpecification(), message)

@GraphDslMarker
public fun <T> Receiver<Iterable<T>>.mustBeDistinct(
    message: String = "must contain distinct elements."
): Unit = addAssertion(IterableDistinctSpecification(), message)
