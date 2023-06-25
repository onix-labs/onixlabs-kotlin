package io.onixlabs.kotlin.core.specifications.generic

import io.onixlabs.kotlin.core.isEqualToAny
import io.onixlabs.kotlin.core.specifications.Specification

class GenericEqualAnySpecification<T>(private val values: Iterable<T?>) : Specification<T?>() {

    constructor(vararg values: T?) : this(values.toList())

    override fun isSatisfiedBy(value: T?) = value.isEqualToAny(values)
}
