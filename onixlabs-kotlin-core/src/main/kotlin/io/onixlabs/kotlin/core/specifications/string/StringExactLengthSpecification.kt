package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.specifications.Specification

class StringExactLengthSpecification(private val length: Int) : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value?.length == length
}
