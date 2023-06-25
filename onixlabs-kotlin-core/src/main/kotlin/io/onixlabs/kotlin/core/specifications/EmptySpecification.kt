package io.onixlabs.kotlin.core.specifications

/**
 * Represents an empty specification.
 *
 * @param T The underlying type of of the value to be satisfied by the specification.
 */
class EmptySpecification<T>(private val value: Boolean) : Specification<T>() {

    /**
     * Determines whether specified value satisfies the current [Specification].
     *
     * @param value The value to test using the the current [Specification].
     * @return Returns the value of the current specification.
     */
    override fun isSatisfiedBy(value: T): Boolean = this.value
}
