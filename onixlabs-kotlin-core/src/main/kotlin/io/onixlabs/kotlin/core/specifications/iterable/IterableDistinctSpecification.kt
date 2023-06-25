package io.onixlabs.kotlin.core.specifications.iterable

import io.onixlabs.kotlin.core.collections.isDistinct
import io.onixlabs.kotlin.core.specifications.Specification

class IterableDistinctSpecification<T> : Specification<Iterable<T>?>() {
    override fun isSatisfiedBy(value: Iterable<T>?) = value?.isDistinct() ?: false
}
