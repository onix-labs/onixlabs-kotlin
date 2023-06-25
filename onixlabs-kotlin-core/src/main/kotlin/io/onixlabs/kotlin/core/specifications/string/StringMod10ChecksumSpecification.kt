package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.isValidMod10Checksum
import io.onixlabs.kotlin.core.specifications.Specification

class StringMod10ChecksumSpecification : Specification<String?>() {
    override fun isSatisfiedBy(value: String?) = value?.isValidMod10Checksum() ?: false
}
