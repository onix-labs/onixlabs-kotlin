package io.onixlabs.kotlin.core.specifications.iterable

import io.onixlabs.kotlin.core.collections.isEmpty
import io.onixlabs.kotlin.core.specifications.Specification

class IterableEmptySpecification<T> : Specification<Iterable<T>?>() {
    override fun isSatisfiedBy(value: Iterable<T>?) = value?.isEmpty() ?: false
}
