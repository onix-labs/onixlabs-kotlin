package io.onixlabs.kotlin.core.specifications.iterable

import io.onixlabs.kotlin.core.collections.hasCount
import io.onixlabs.kotlin.core.specifications.Specification

class IterableExactCountSpecification<T>(private val count: Int) : Specification<Iterable<T>?>() {
    override fun isSatisfiedBy(value: Iterable<T>?) = value?.hasCount(count) ?: false
}
