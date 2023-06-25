package io.onixlabs.kotlin.core.specifications.iterable

import io.onixlabs.kotlin.core.collections.containsAll
import io.onixlabs.kotlin.core.specifications.Specification

class IterableContainsAllSpecification<T>(private val values: Iterable<T>) : Specification<Iterable<T>?>() {

    constructor(vararg values: T) : this(values.toList())

    override fun isSatisfiedBy(value: Iterable<T>?) = value?.containsAll(values) ?: false
}
