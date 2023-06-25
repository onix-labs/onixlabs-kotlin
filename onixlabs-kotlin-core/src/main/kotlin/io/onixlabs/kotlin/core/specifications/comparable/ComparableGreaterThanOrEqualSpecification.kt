package io.onixlabs.kotlin.core.specifications.comparable

import io.onixlabs.kotlin.core.isGreaterThanOrEqualTo
import io.onixlabs.kotlin.core.specifications.Specification

class ComparableGreaterThanOrEqualSpecification<T : Comparable<T>>(private val value: T) : Specification<T?>() {
    override fun isSatisfiedBy(value: T?) = value?.isGreaterThanOrEqualTo(this.value) ?: false
}
