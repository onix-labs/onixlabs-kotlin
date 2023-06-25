package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.specifications.Specification

class StringNullOrBlankSpecification : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value.isNullOrBlank()
}
