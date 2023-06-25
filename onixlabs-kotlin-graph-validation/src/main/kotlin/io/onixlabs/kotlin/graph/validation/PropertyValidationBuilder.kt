package io.onixlabs.kotlin.graph.validation

import io.onixlabs.kotlin.core.specifications.Specification

typealias Receiver<T> = PropertyValidationBuilder<out T?>

class PropertyValidationBuilder<T> internal constructor(private val value: T?, private val stopOnError: Boolean) {

    private val mutableAssertions: MutableList<PropertyValidationAssertion<in T>> = mutableListOf()
    internal val assertions: List<PropertyValidationAssertion<in T>> get() = mutableAssertions


    fun addAssertion(specification: Specification<in T?>, message: String) {
        mutableAssertions.add(PropertyValidationAssertion(specification, message))
        if (stopOnError && !specification.isSatisfiedBy(value)) throw StopOnErrorException(this)
    }
}
