package io.onixlabs.kotlin.playground

import io.onixlabs.kotlin.graph.Graph
import io.onixlabs.kotlin.graph.validation.mustBeGreaterThanOrEqualTo
import io.onixlabs.kotlin.graph.validation.mustContainAny
import io.onixlabs.kotlin.graph.validation.mustHaveCountWithinRange
import io.onixlabs.kotlin.graph.validation.mustNotBeNullOrBlank
import io.onixlabs.kotlin.graph.validation.validator

object Configuration : Graph({

    configure<Request> {
        validator(RequestValidator)
    }


})
