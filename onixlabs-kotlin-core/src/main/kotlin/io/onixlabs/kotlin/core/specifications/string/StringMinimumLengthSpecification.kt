package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.isGreaterThanOrEqualTo
import io.onixlabs.kotlin.core.specifications.Specification

class StringMinimumLengthSpecification(private val length: Int) : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value?.length?.isGreaterThanOrEqualTo(length) ?: false
}
