package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.isRegexMatch
import io.onixlabs.kotlin.core.specifications.Specification

class StringMatchRegexSpecification(private val regex: Regex) : Specification<String?>() {

    constructor(pattern: String) : this(Regex(pattern))

    override fun isSatisfiedBy(value: String?) = value?.isRegexMatch(regex) ?: false
}
