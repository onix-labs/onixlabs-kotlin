package io.onixlabs.kotlin.core.specifications.iterable

import io.onixlabs.kotlin.core.collections.orderedContentEquals
import io.onixlabs.kotlin.core.specifications.Specification

class IterableContainsOrderedExactlySpecification<T>(private val values: Iterable<T>) : Specification<Iterable<T>?>() {
    override fun isSatisfiedBy(value: Iterable<T>?) = value?.orderedContentEquals(values) ?: false
}
