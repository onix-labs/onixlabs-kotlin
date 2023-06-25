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
fun Receiver<String>.mustBeNullOrBlank(
    message: String = "must be null or blank."
) = addAssertion(StringNullOrBlankSpecification(), message)

@GraphDslMarker
fun Receiver<String>.mustNotBeNullOrBlank(
    message: String = "must not be null or blank."
) = addAssertion(StringNullOrBlankSpecification().not(), message)

@GraphDslMarker
fun Receiver<String>.mustBeNullOrEmpty(
    message: String = "must be null or empty."
) = addAssertion(StringNullOrEmptySpecification(), message)

@GraphDslMarker
fun Receiver<String>.mustNotBeNullOrEmpty(
    message: String = "must not be mull or empty."
) = addAssertion(StringNullOrEmptySpecification().not(), message)

@GraphDslMarker
fun Receiver<String>.mustBeBlank(
    message: String = "must be blank."
) = addAssertion(StringBlankSpecification(), message)

@GraphDslMarker
fun Receiver<String>.mustNotBeBlank(
    message: String = "must not be blank."
) = addAssertion(StringBlankSpecification().not(), message)

@GraphDslMarker
fun Receiver<String>.mustBeEmpty(
    message: String = "must be empty."
) = addAssertion(StringEmptySpecification(), message)

@GraphDslMarker
fun Receiver<String>.mustNotBeEmpty(
    message: String = "must not be empty."
) = addAssertion(StringEmptySpecification().not(), message)

@GraphDslMarker
fun Receiver<String>.mustContain(
    value: String,
    message: String = "must contain the specified value: $value."
) = addAssertion(StringContainsSpecification(value), message)

@GraphDslMarker
fun Receiver<String>.mustNotContain(
    value: String,
    message: String = "must not contain the specified value: $value."
) = addAssertion(StringContainsSpecification(value).not(), message)

@GraphDslMarker
fun Receiver<String>.mustContainAll(
    values: Iterable<String>,
    message: String = "must contain all of the specified values: ${values.joinToString()}."
) = addAssertion(StringContainsAllSpecification(values), message)

@GraphDslMarker
fun Receiver<String>.mustContainAny(
    values: Iterable<String>,
    message: String = "must contain any of the specified values: ${values.joinToString()}."
) = addAssertion(StringContainsAnySpecification(values), message)

@GraphDslMarker
fun Receiver<String>.mustEndWith(
    value: String,
    message: String = "must end with the specified value: $value."
) = addAssertion(StringEndsWithSpecification(value), message)

@GraphDslMarker
fun Receiver<String>.mustNotEndWith(
    value: String,
    message: String = "must not end with the specified value: $value."
) = addAssertion(StringEndsWithSpecification(value).not(), message)

@GraphDslMarker
fun Receiver<String>.mustStartWith(
    value: String,
    message: String = "must start with the specified value: $value."
) = addAssertion(StringStartsWithSpecification(value), message)

@GraphDslMarker
fun Receiver<String>.mustNotStartWith(
    value: String,
    message: String = "must not start with the specified value: $value."
) = addAssertion(StringStartsWithSpecification(value).not(), message)

@GraphDslMarker
fun Receiver<String>.mustHaveLength(
    length: Int,
    message: String = "must have the specified length: $length"
) = addAssertion(StringExactLengthSpecification(length), message)

@GraphDslMarker
fun Receiver<String>.mustHaveLengthWithinRange(
    minimum: Int,
    maximum: Int,
    message: String = "must have a length between the specified range: $minimum to $maximum."
) = addAssertion(StringRangeLengthSpecification(minimum, maximum), message)

@GraphDslMarker
fun Receiver<String>.mustHaveLengthWithinRange(
    range: IntRange,
    message: String = "must have a length between the specified range: ${range.first} to ${range.last}."
) = addAssertion(StringRangeLengthSpecification(range), message)

@GraphDslMarker
fun Receiver<String>.mustHaveMaximumLength(
    length: Int,
    message: String = "must have the specified maximum length: $length"
) = addAssertion(StringMaximumLengthSpecification(length), message)

@GraphDslMarker
fun Receiver<String>.mustHaveMinimumLength(
    length: Int,
    message: String = "must have the specified minimum length: $length"
) = addAssertion(StringMinimumLengthSpecification(length), message)

@GraphDslMarker
fun Receiver<String>.mustBeValidMod10Checksum(
    message: String = "must be a valid Mod10 (Luhn) checksum."
) = addAssertion(StringMod10ChecksumSpecification(), message)

@GraphDslMarker
fun Receiver<String>.mustMatchRegularExpression(
    pattern: String,
    message: String = "must match the specified regular expression: $pattern."
) = addAssertion(StringMatchRegexSpecification(pattern), message)

@GraphDslMarker
fun Receiver<String>.mustBeValidUUID(
    message: String = "must be a valid UUID."
) = addAssertion(StringValidUUIDSpecification(), message)
