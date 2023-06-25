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
fun <T> Receiver<Iterable<T>>.mustContain(
    value: T,
    message: String = "must contain the specified value: $value."
) = addAssertion(IterableContainsSpecification(value), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustNotContain(
    value: T,
    message: String = "must not contain the specified value: $value."
) = addAssertion(IterableContainsSpecification(value).not(), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustContainAll(
    values: Iterable<T>,
    message: String = "must contain all of the specified values: ${values.joinToString()}."
) = addAssertion(IterableContainsAllSpecification(values), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustContainAny(
    values: Iterable<T>,
    message: String = "must contain any of the specified values: ${values.joinToString()}."
) = addAssertion(IterableContainsAnySpecification(values), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustNotContainAny(
    values: Iterable<T>,
    message: String = "must not contain any of the specified values: ${values.joinToString()}."
) = addAssertion(IterableContainsAnySpecification(values).not(), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustContainExactly(
    values: Iterable<T>,
    message: String = "must contain exactly the specified values: ${values.joinToString()}."
) = addAssertion(IterableContainsExactlySpecification(values), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustContainOrderedExactly(
    values: Iterable<T>,
    message: String = "must contain exactly the specified values in the specified order: ${values.joinToString()}."
) = addAssertion(IterableContainsOrderedExactlySpecification(values), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustHaveCount(
    count: Int,
    message: String = "must contain exactly the specified number of elements: $count."
) = addAssertion(IterableExactCountSpecification(count), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustHaveCountWithinRange(
    minimum: Int,
    maximum: Int,
    message: String = "must contain the number of elements within the specified range: $minimum to $maximum."
) = addAssertion(IterableRangeCountSpecification(minimum, maximum), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustHaveCountWithinRange(
    range: IntRange,
    message: String = "must contain the specified number of elements within range: ${range.first} to ${range.last}."
) = addAssertion(IterableRangeCountSpecification(range), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustBeEmpty(
    message: String = "must be empty."
) = addAssertion(IterableEmptySpecification(), message)


@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustNotBeEmpty(
    message: String = "must not be empty."
) = addAssertion(IterableEmptySpecification<T>().not(), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustBeSingle(
    message: String = "must contain a single element."
) = addAssertion(IterableSingleSpecification(), message)

@GraphDslMarker
fun <T> Receiver<Iterable<T>>.mustBeDistinct(
    message: String = "must contain distinct elements."
) = addAssertion(IterableDistinctSpecification(), message)
