package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.specifications.Specification

class StringContainsSpecification(private val value: String) : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value?.contains(this.value) ?: false
}
