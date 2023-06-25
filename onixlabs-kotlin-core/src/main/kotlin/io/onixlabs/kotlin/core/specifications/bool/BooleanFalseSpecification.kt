package io.onixlabs.kotlin.core.specifications.bool

import io.onixlabs.kotlin.core.isFalse
import io.onixlabs.kotlin.core.specifications.Specification

class BooleanFalseSpecification : Specification<Boolean?>() {
    override fun isSatisfiedBy(value: Boolean?) = value?.isFalse() ?: false
}
