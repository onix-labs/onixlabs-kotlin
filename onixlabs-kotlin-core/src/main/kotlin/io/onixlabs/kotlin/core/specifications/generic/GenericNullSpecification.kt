package io.onixlabs.kotlin.core.specifications.generic

import io.onixlabs.kotlin.core.isNull
import io.onixlabs.kotlin.core.specifications.Specification

class GenericNullSpecification<T> : Specification<T?>() {
    override fun isSatisfiedBy(value: T?) = value.isNull()
}
