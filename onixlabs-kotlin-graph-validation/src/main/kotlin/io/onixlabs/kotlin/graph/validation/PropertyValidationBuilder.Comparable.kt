package io.onixlabs.kotlin.graph.validation

import io.onixlabs.kotlin.core.specifications.comparable.ComparableGreaterThanOrEqualSpecification
import io.onixlabs.kotlin.core.specifications.comparable.ComparableGreaterThanSpecification
import io.onixlabs.kotlin.core.specifications.comparable.ComparableLessThanOrEqualSpecification
import io.onixlabs.kotlin.core.specifications.comparable.ComparableLessThanSpecification
import io.onixlabs.kotlin.core.specifications.comparable.ComparableRangeSpecification
import io.onixlabs.kotlin.graph.GraphDslMarker

@GraphDslMarker
fun <T : Comparable<T>> Receiver<T>.mustBeWithinRange(
    minimum: T,
    maximum: T,
    message: String = "must be within the specified range: $minimum to $maximum."
) = addAssertion(ComparableRangeSpecification(minimum, maximum), message)

@GraphDslMarker
fun <T : Comparable<T>> Receiver<T>.mustBeWithinRange(
    range: ClosedRange<T>,
    message: String = "must be within the specified range: ${range.start} to ${range.endInclusive}."
) = addAssertion(ComparableRangeSpecification(range), message)


@GraphDslMarker
fun <T : Comparable<T>> Receiver<T>.mustBeGreaterThan(
    value: T,
    message: String = "must be greater than the specified value: $value."
) = addAssertion(ComparableGreaterThanSpecification(value), message)

@GraphDslMarker
fun <T : Comparable<T>> Receiver<T>.mustBeGreaterThanOrEqualTo(
    value: T,
    message: String = "must be greater than or equal to the specified value: $value."
) = addAssertion(ComparableGreaterThanOrEqualSpecification(value), message)

@GraphDslMarker
fun <T : Comparable<T>> Receiver<T>.mustBeLessThan(
    value: T,
    message: String = "must be less than the specified value: $value."
) = addAssertion(ComparableLessThanSpecification(value), message)

@GraphDslMarker
fun <T : Comparable<T>> Receiver<T>.mustBeLessThanOrEqualTo(
    value: T,
    message: String = "must be less than or equal to the specified value: $value."
) = addAssertion(ComparableLessThanOrEqualSpecification(value), message)
