package io.onixlabs.kotlin.playground

import io.onixlabs.kotlin.graph.validation.Validator
import io.onixlabs.kotlin.graph.validation.mustBeGreaterThanOrEqualTo
import io.onixlabs.kotlin.graph.validation.mustContainAny
import io.onixlabs.kotlin.graph.validation.mustHaveCountWithinRange
import io.onixlabs.kotlin.graph.validation.mustNotBeNull
import io.onixlabs.kotlin.graph.validation.mustNotBeNullOrBlank
import kotlin.reflect.jvm.internal.impl.load.java.JavaClassFinder

object RequestValidator : Validator<Request>({
    scalar(Request::text) {
        mustNotBeNullOrBlank()
        mustContainAny(listOf("Alice", "Bob", "Charlie"))
    }

    scalar(Request::number) {
        mustBeGreaterThanOrEqualTo(10)
    }

    scalar(Request::numberElements) {
        mustHaveCountWithinRange(1, 100)
    }

    iterable(Request::numberElements) {
        mustBeGreaterThanOrEqualTo(123)
    }
})
