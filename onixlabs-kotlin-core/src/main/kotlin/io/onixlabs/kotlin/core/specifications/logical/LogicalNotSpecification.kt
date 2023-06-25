package io.onixlabs.kotlin.core.specifications.logical

import io.onixlabs.kotlin.core.specifications.Specification

class LogicalNotSpecification<T>(private val specification: Specification<T>) : Specification<T>() {
    override fun isSatisfiedBy(value: T) = !specification.isSatisfiedBy(value)
}
