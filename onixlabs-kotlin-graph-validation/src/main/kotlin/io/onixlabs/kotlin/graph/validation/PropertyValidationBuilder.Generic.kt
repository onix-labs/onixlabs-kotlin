package io.onixlabs.kotlin.graph.validation

import io.onixlabs.kotlin.core.specifications.generic.GenericEqualAnySpecification
import io.onixlabs.kotlin.core.specifications.generic.GenericEqualSpecification
import io.onixlabs.kotlin.core.specifications.generic.GenericNullSpecification
import io.onixlabs.kotlin.core.specifications.generic.GenericPredicateSpecification
import io.onixlabs.kotlin.graph.GraphDslMarker

@GraphDslMarker
fun <T> Receiver<T>.mustBeNull(
    message: String = "must be null."
) = addAssertion(GenericNullSpecification(), message)


@GraphDslMarker
fun <T> Receiver<T>.mustNotBeNull(
    message: String = "must not be null."
) = addAssertion(GenericNullSpecification<Any?>().not(), message)

@GraphDslMarker
fun <T> Receiver<T>.mustBeEqualTo(
    value: T,
    message: String = "must be equal to the specified value: $value."
) = addAssertion(GenericEqualSpecification(value), message)

@GraphDslMarker
fun <T> Receiver<T>.mustNotBeEqualTo(
    value: T,
    message: String = "must not be equal to the specified value: $value."
) = addAssertion(GenericEqualSpecification(value).not(), message)

@GraphDslMarker
fun <T> Receiver<T>.mustBeEqualToAny(
    values: Iterable<T>,
    message: String = "must be equal to the any of the specified values: ${values.joinToString()}."
) = addAssertion(GenericEqualAnySpecification(values), message)

@GraphDslMarker
fun <T> Receiver<T>.mustNotBeEqualToAny(
    values: Iterable<T>,
    message: String = "must not be equal to any of the specified values: ${values.joinToString()}."
) = addAssertion(GenericEqualAnySpecification(values).not(), message)

@GraphDslMarker
fun <T> Receiver<T>.mustSatisfy(
    predicate: (T) -> Boolean,
    message: String = "must satisfy the specified predicate."
) = addAssertion(GenericPredicateSpecification(predicate), message)

@GraphDslMarker
fun <T> Receiver<T>.mustNotSatisfy(
    predicate: (T) -> Boolean,
    message: String = "must not satisfy the specified predicate."
) = addAssertion(GenericPredicateSpecification(predicate).not(), message)
