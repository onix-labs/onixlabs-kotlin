package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.specifications.Specification

class StringBlankSpecification : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value?.isBlank() ?: false
}
