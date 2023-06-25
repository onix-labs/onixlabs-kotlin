package io.onixlabs.kotlin.core.specifications.comparable

import io.onixlabs.kotlin.core.isLessThan
import io.onixlabs.kotlin.core.specifications.Specification

class ComparableLessThanSpecification<T : Comparable<T>>(private val value: T) : Specification<T?>() {
    override fun isSatisfiedBy(value: T?) = value?.isLessThan(this.value) ?: false
}
