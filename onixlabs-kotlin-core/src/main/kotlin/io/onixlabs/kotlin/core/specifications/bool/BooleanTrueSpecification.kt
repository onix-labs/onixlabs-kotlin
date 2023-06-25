package io.onixlabs.kotlin.core.specifications.bool

import io.onixlabs.kotlin.core.isTrue
import io.onixlabs.kotlin.core.specifications.Specification

class BooleanTrueSpecification : Specification<Boolean?>() {
    override fun isSatisfiedBy(value: Boolean?) = value?.isTrue() ?: false
}
