package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.isWithinRange
import io.onixlabs.kotlin.core.specifications.Specification

class StringRangeLengthSpecification(private val minimum: Int, private val maximum: Int) : Specification<String?>() {

    constructor(range: IntRange) : this(range.first, range.last)

    override fun isSatisfiedBy(value: String?) = value?.length?.isWithinRange(minimum, maximum) ?: false
}
