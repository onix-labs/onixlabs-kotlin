package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.specifications.Specification

class StringStartsWithSpecification(private val value: String) : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value?.startsWith(this.value) ?: false
}
