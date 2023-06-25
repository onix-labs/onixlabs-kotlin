package io.onixlabs.kotlin.core.specifications.generic

import io.onixlabs.kotlin.core.isEqualTo
import io.onixlabs.kotlin.core.specifications.Specification

class GenericEqualSpecification<T>(private val value: T) : Specification<T?>() {
    override fun isSatisfiedBy(value: T?) = value.isEqualTo(this.value)
}
