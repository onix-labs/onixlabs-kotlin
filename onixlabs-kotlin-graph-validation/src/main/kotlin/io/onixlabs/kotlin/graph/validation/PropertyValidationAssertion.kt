package io.onixlabs.kotlin.graph.validation

import io.onixlabs.kotlin.core.specifications.Specification

internal data class PropertyValidationAssertion<T>(private val specification: Specification<T>, internal val message: String)
