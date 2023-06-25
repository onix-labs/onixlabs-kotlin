package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.specifications.Specification

class StringNullOrEmptySpecification : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value.isNullOrEmpty()
}
