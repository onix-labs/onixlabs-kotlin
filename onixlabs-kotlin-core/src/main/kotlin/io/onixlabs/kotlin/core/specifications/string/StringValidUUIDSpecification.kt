package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.isValidUUID
import io.onixlabs.kotlin.core.specifications.Specification

class StringValidUUIDSpecification : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value?.isValidUUID() ?: false
}
