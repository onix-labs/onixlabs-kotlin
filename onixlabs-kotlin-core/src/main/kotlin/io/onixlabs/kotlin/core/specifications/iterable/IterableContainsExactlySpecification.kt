package io.onixlabs.kotlin.core.specifications.iterable

import io.onixlabs.kotlin.core.collections.unorderedContentEquals
import io.onixlabs.kotlin.core.specifications.Specification

class IterableContainsExactlySpecification<T>(private val values: Iterable<T>) : Specification<Iterable<T>?>() {
    override fun isSatisfiedBy(value: Iterable<T>?) = value?.unorderedContentEquals(values) ?: false
}
