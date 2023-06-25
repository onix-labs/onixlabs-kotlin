package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.isLessThanOrEqualTo
import io.onixlabs.kotlin.core.specifications.Specification

class StringMaximumLengthSpecification(private val length: Int) : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value?.length?.isLessThanOrEqualTo(length) ?: false
}
