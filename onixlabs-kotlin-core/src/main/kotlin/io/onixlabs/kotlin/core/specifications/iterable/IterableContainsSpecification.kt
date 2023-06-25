package io.onixlabs.kotlin.core.specifications.iterable

import io.onixlabs.kotlin.core.specifications.Specification

class IterableContainsSpecification<T>(private val value: T) : Specification<Iterable<T>?>() {
    override fun isSatisfiedBy(value: Iterable<T>?) = value?.contains(this.value) ?: false
}
