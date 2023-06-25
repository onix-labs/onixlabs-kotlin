package io.onixlabs.kotlin.core.specifications.comparable

import io.onixlabs.kotlin.core.isWithinRange
import io.onixlabs.kotlin.core.specifications.Specification

class ComparableRangeSpecification<T : Comparable<T>>(private val minimum: T, private val maximum: T) : Specification<T?>() {

    constructor(range: ClosedRange<T>) : this(range.start, range.endInclusive)

    override fun isSatisfiedBy(value: T?) = value?.isWithinRange(minimum, maximum) ?: false
}
