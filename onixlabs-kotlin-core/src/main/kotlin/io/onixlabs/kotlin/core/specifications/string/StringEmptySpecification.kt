package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.specifications.Specification

class StringEmptySpecification : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value?.isEmpty() ?: false
}
