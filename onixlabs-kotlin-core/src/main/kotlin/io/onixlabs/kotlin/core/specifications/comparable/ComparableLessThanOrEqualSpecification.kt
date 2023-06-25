package io.onixlabs.kotlin.core.specifications.comparable

import io.onixlabs.kotlin.core.isLessThanOrEqualTo
import io.onixlabs.kotlin.core.specifications.Specification

class ComparableLessThanOrEqualSpecification<T : Comparable<T>>(private val value: T) : Specification<T?>() {
    override fun isSatisfiedBy(value: T?) = value?.isLessThanOrEqualTo(this.value) ?: false
}
