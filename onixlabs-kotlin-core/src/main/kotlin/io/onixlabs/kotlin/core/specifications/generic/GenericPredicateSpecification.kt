package io.onixlabs.kotlin.core.specifications.generic

import io.onixlabs.kotlin.core.specifications.Specification

class GenericPredicateSpecification<T>(private val predicate: (T) -> Boolean) : Specification<T?>() {
    override fun isSatisfiedBy(value: T?) = value?.let(predicate) ?: false
}
