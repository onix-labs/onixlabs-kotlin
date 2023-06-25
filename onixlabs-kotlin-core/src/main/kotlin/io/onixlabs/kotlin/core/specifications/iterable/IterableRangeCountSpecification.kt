package io.onixlabs.kotlin.core.specifications.iterable

import io.onixlabs.kotlin.core.isWithinRange
import io.onixlabs.kotlin.core.specifications.Specification

class IterableRangeCountSpecification<T>(private val minimum: Int, private val maximum: Int) : Specification<Iterable<T>?>() {

    constructor(range: IntRange) : this(range.first, range.last)

    override fun isSatisfiedBy(value: Iterable<T>?) = value?.count()?.isWithinRange(minimum, maximum) ?: false
}
