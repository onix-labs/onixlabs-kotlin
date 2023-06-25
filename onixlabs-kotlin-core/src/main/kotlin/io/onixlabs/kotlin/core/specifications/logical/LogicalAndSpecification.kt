package io.onixlabs.kotlin.core.specifications.logical

import io.onixlabs.kotlin.core.specifications.Specification

class LogicalAndSpecification<T>(private val left: Specification<T>, private val right: Specification<T>) : Specification<T>() {
    override fun isSatisfiedBy(value: T) = left.isSatisfiedBy(value) && right.isSatisfiedBy(value)
}
