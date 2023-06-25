package io.onixlabs.kotlin.core.specifications.iterable

import io.onixlabs.kotlin.core.collections.isSingle
import io.onixlabs.kotlin.core.specifications.Specification

class IterableSingleSpecification<T> : Specification<Iterable<T>?>() {
    override fun isSatisfiedBy(value: Iterable<T>?) = value?.isSingle() ?: false
}
