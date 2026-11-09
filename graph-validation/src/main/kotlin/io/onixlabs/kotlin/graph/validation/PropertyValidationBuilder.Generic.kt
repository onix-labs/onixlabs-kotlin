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

import io.onixlabs.kotlin.core.specifications.generic.GenericEqualAnySpecification
import io.onixlabs.kotlin.core.specifications.generic.GenericEqualSpecification
import io.onixlabs.kotlin.core.specifications.generic.GenericNullSpecification
import io.onixlabs.kotlin.core.specifications.generic.GenericPredicateSpecification
import io.onixlabs.kotlin.graph.GraphDslMarker

@GraphDslMarker
public fun <T> Receiver<T>.mustBeNull(
    message: String = "must be null."
): Unit = addAssertion(GenericNullSpecification(), message)


@GraphDslMarker
public fun <T> Receiver<T>.mustNotBeNull(
    message: String = "must not be null."
): Unit = addAssertion(GenericNullSpecification<Any?>().not(), message)

@GraphDslMarker
public fun <T> Receiver<T>.mustBeEqualTo(
    value: T,
    message: String = "must be equal to the specified value: $value."
): Unit = addAssertion(GenericEqualSpecification(value), message)

@GraphDslMarker
public fun <T> Receiver<T>.mustNotBeEqualTo(
    value: T,
    message: String = "must not be equal to the specified value: $value."
): Unit = addAssertion(GenericEqualSpecification(value).not(), message)

@GraphDslMarker
public fun <T> Receiver<T>.mustBeEqualToAny(
    values: Iterable<T>,
    message: String = "must be equal to the any of the specified values: ${values.joinToString()}."
): Unit = addAssertion(GenericEqualAnySpecification(values), message)

@GraphDslMarker
public fun <T> Receiver<T>.mustNotBeEqualToAny(
    values: Iterable<T>,
    message: String = "must not be equal to any of the specified values: ${values.joinToString()}."
): Unit = addAssertion(GenericEqualAnySpecification(values).not(), message)

@GraphDslMarker
public fun <T> Receiver<T>.mustSatisfy(
    predicate: (T) -> Boolean,
    message: String = "must satisfy the specified predicate."
): Unit = addAssertion(GenericPredicateSpecification(predicate), message)

@GraphDslMarker
public fun <T> Receiver<T>.mustNotSatisfy(
    predicate: (T) -> Boolean,
    message: String = "must not satisfy the specified predicate."
): Unit = addAssertion(GenericPredicateSpecification(predicate).not(), message)
