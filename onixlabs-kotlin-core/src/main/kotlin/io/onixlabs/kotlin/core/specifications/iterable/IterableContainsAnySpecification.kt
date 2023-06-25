package io.onixlabs.kotlin.core.specifications.iterable

import io.onixlabs.kotlin.core.collections.containsAny
import io.onixlabs.kotlin.core.specifications.Specification

class IterableContainsAnySpecification<T>(private val values: Iterable<T>) : Specification<Iterable<T>?>() {

    constructor(vararg values: T) : this(values.toList())

    override fun isSatisfiedBy(value: Iterable<T>?) = value?.containsAny(values) ?: false
}
