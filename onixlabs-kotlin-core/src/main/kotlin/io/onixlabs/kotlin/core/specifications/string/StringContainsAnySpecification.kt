package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.containsAll
import io.onixlabs.kotlin.core.specifications.Specification

class StringContainsAnySpecification(private val values: Iterable<String>) : Specification<String?>() {

    constructor(vararg values: String) : this(values.toList())

    override fun isSatisfiedBy(value: String?) = value?.containsAll(values) ?: false
}
