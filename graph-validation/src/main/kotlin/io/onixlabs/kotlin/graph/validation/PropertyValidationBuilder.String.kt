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

import io.onixlabs.kotlin.core.specifications.string.StringBlankSpecification
import io.onixlabs.kotlin.core.specifications.string.StringContainsAllSpecification
import io.onixlabs.kotlin.core.specifications.string.StringContainsAnySpecification
import io.onixlabs.kotlin.core.specifications.string.StringContainsSpecification
import io.onixlabs.kotlin.core.specifications.string.StringEmptySpecification
import io.onixlabs.kotlin.core.specifications.string.StringEndsWithSpecification
import io.onixlabs.kotlin.core.specifications.string.StringExactLengthSpecification
import io.onixlabs.kotlin.core.specifications.string.StringMatchRegexSpecification
import io.onixlabs.kotlin.core.specifications.string.StringMaximumLengthSpecification
import io.onixlabs.kotlin.core.specifications.string.StringMinimumLengthSpecification
import io.onixlabs.kotlin.core.specifications.string.StringMod10ChecksumSpecification
import io.onixlabs.kotlin.core.specifications.string.StringNullOrBlankSpecification
import io.onixlabs.kotlin.core.specifications.string.StringNullOrEmptySpecification
import io.onixlabs.kotlin.core.specifications.string.StringRangeLengthSpecification
import io.onixlabs.kotlin.core.specifications.string.StringStartsWithSpecification
import io.onixlabs.kotlin.core.specifications.string.StringValidUUIDSpecification
import io.onixlabs.kotlin.graph.GraphDslMarker

@GraphDslMarker
public fun Receiver<String>.mustBeNullOrBlank(
    message: String = "must be null or blank."
): Unit = addAssertion(StringNullOrBlankSpecification(), message)

@GraphDslMarker
public fun Receiver<String>.mustNotBeNullOrBlank(
    message: String = "must not be null or blank."
): Unit = addAssertion(StringNullOrBlankSpecification().not(), message)

@GraphDslMarker
public fun Receiver<String>.mustBeNullOrEmpty(
    message: String = "must be null or empty."
): Unit = addAssertion(StringNullOrEmptySpecification(), message)

@GraphDslMarker
public fun Receiver<String>.mustNotBeNullOrEmpty(
    message: String = "must not be mull or empty."
): Unit = addAssertion(StringNullOrEmptySpecification().not(), message)

@GraphDslMarker
public fun Receiver<String>.mustBeBlank(
    message: String = "must be blank."
): Unit = addAssertion(StringBlankSpecification(), message)

@GraphDslMarker
public fun Receiver<String>.mustNotBeBlank(
    message: String = "must not be blank."
): Unit = addAssertion(StringBlankSpecification().not(), message)

@GraphDslMarker
public fun Receiver<String>.mustBeEmpty(
    message: String = "must be empty."
): Unit = addAssertion(StringEmptySpecification(), message)

@GraphDslMarker
public fun Receiver<String>.mustNotBeEmpty(
    message: String = "must not be empty."
): Unit = addAssertion(StringEmptySpecification().not(), message)

@GraphDslMarker
public fun Receiver<String>.mustContain(
    value: String,
    message: String = "must contain the specified value: $value."
): Unit = addAssertion(StringContainsSpecification(value), message)

@GraphDslMarker
public fun Receiver<String>.mustNotContain(
    value: String,
    message: String = "must not contain the specified value: $value."
): Unit = addAssertion(StringContainsSpecification(value).not(), message)

@GraphDslMarker
public fun Receiver<String>.mustContainAll(
    values: Iterable<String>,
    message: String = "must contain all of the specified values: ${values.joinToString()}."
): Unit = addAssertion(StringContainsAllSpecification(values), message)

@GraphDslMarker
public fun Receiver<String>.mustContainAny(
    values: Iterable<String>,
    message: String = "must contain any of the specified values: ${values.joinToString()}."
): Unit = addAssertion(StringContainsAnySpecification(values), message)

@GraphDslMarker
public fun Receiver<String>.mustEndWith(
    value: String,
    message: String = "must end with the specified value: $value."
): Unit = addAssertion(StringEndsWithSpecification(value), message)

@GraphDslMarker
public fun Receiver<String>.mustNotEndWith(
    value: String,
    message: String = "must not end with the specified value: $value."
): Unit = addAssertion(StringEndsWithSpecification(value).not(), message)

@GraphDslMarker
public fun Receiver<String>.mustStartWith(
    value: String,
    message: String = "must start with the specified value: $value."
): Unit = addAssertion(StringStartsWithSpecification(value), message)

@GraphDslMarker
public fun Receiver<String>.mustNotStartWith(
    value: String,
    message: String = "must not start with the specified value: $value."
): Unit = addAssertion(StringStartsWithSpecification(value).not(), message)

@GraphDslMarker
public fun Receiver<String>.mustHaveLength(
    length: Int,
    message: String = "must have the specified length: $length"
): Unit = addAssertion(StringExactLengthSpecification(length), message)

@GraphDslMarker
public fun Receiver<String>.mustHaveLengthWithinRange(
    minimum: Int,
    maximum: Int,
    message: String = "must have a length between the specified range: $minimum to $maximum."
): Unit = addAssertion(StringRangeLengthSpecification(minimum, maximum), message)

@GraphDslMarker
public fun Receiver<String>.mustHaveLengthWithinRange(
    range: IntRange,
    message: String = "must have a length between the specified range: ${range.first} to ${range.last}."
): Unit = addAssertion(StringRangeLengthSpecification(range), message)

@GraphDslMarker
public fun Receiver<String>.mustHaveMaximumLength(
    length: Int,
    message: String = "must have the specified maximum length: $length"
): Unit = addAssertion(StringMaximumLengthSpecification(length), message)

@GraphDslMarker
public fun Receiver<String>.mustHaveMinimumLength(
    length: Int,
    message: String = "must have the specified minimum length: $length"
): Unit = addAssertion(StringMinimumLengthSpecification(length), message)

@GraphDslMarker
public fun Receiver<String>.mustBeValidMod10Checksum(
    message: String = "must be a valid Mod10 (Luhn) checksum."
): Unit = addAssertion(StringMod10ChecksumSpecification(), message)

@GraphDslMarker
public fun Receiver<String>.mustMatchRegularExpression(
    pattern: String,
    message: String = "must match the specified regular expression: $pattern."
): Unit = addAssertion(StringMatchRegexSpecification(pattern), message)

@GraphDslMarker
public fun Receiver<String>.mustBeValidUUID(
    message: String = "must be a valid UUID."
): Unit = addAssertion(StringValidUUIDSpecification(), message)
