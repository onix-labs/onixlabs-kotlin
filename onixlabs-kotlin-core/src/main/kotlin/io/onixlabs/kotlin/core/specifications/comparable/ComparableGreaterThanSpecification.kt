package io.onixlabs.kotlin.core.specifications.comparable

import io.onixlabs.kotlin.core.isGreaterThan
import io.onixlabs.kotlin.core.specifications.Specification

class ComparableGreaterThanSpecification<T : Comparable<T>>(private val value: T) : Specification<T?>() {
    override fun isSatisfiedBy(value: T?) = value?.isGreaterThan(this.value) ?: false
}
