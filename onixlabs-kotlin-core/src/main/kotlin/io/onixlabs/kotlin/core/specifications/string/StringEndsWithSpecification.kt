package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.specifications.Specification

class StringEndsWithSpecification(private val value: String) : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value?.endsWith(this.value) ?: false
}
