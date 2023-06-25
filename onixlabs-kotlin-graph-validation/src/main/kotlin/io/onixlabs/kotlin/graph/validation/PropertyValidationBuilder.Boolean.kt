package io.onixlabs.kotlin.graph.validation

import io.onixlabs.kotlin.core.specifications.bool.BooleanFalseSpecification
import io.onixlabs.kotlin.core.specifications.bool.BooleanTrueSpecification
import io.onixlabs.kotlin.graph.GraphDslMarker

@GraphDslMarker
fun Receiver<Boolean>.mustBeTrue(
    message: String = "must be true."
) = addAssertion(BooleanTrueSpecification(), message)


@GraphDslMarker
fun Receiver<Boolean>.mustBeFalse(
    message: String = "must be false."
) = addAssertion(BooleanFalseSpecification(), message)
