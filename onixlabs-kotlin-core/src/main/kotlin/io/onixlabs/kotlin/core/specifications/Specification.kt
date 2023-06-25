package io.onixlabs.kotlin.core.specifications

import io.onixlabs.kotlin.core.collections.isNotEmpty
import io.onixlabs.kotlin.core.specifications.logical.LogicalAndSpecification
import io.onixlabs.kotlin.core.specifications.logical.LogicalNotSpecification
import io.onixlabs.kotlin.core.specifications.logical.LogicalOrSpecification

/**
 * Represents the base class for implementing the specification pattern.
 *
 * @param T The underlying type of of the value to be satisfied by the specification.
 */
abstract class Specification<T> {

    companion object {

        /**
         * Creates an [EmptySpecification] of the specified type.
         *
         * @param T The underlying type of of the value to be satisfied by the specification.
         * @return Returns an [EmptySpecification] of the specified type.
         */
        fun <T> empty(value: Boolean) = EmptySpecification<T>(value)

        /**
         * Creates a logical AND specification from all of the specified specifications.
         *
         * @param T The underlying type of of the value to be satisfied by the specification.
         * @param specifications The specifications from which to create a logical AND specification.
         * @return Returns a logical AND specification from all of the specified specifications.
         */
        fun <T> and(specifications: Iterable<Specification<T>>): Specification<T> {
            check(specifications.isNotEmpty()) { "Cannot create a logical AND specification from empty expressions collection." }
            return specifications.reduce { left, right -> left.and(right) }
        }

        /**
         * Creates a logical AND specification from all of the specified specifications.
         *
         * @param T The underlying type of of the value to be satisfied by the specification.
         * @param specifications The specifications from which to create a logical AND specification.
         * @return Returns a logical AND specification from all of the specified specifications.
         */
        fun <T> and(vararg specifications: Specification<T>): Specification<T> {
            return and(specifications.toList())
        }

        /**
         * Creates a logical OR specification from all of the specified specifications.
         *
         * @param T The underlying type of of the value to be satisfied by the specification.
         * @param specifications The specifications from which to create a logical OR specification.
         * @return Returns a logical OR specification from all of the specified specifications.
         */
        fun <T> or(specifications: Iterable<Specification<T>>): Specification<T> {
            check(specifications.isNotEmpty()) { "Cannot create a logical OR specification from empty expressions collection." }
            return specifications.reduce { left, right -> left.or(right) }
        }

        /**
         * Creates a logical OR specification from all of the specified specifications.
         *
         * @param T The underlying type of of the value to be satisfied by the specification.
         * @param specifications The specifications from which to create a logical OR specification.
         * @return Returns a logical OR specification from all of the specified specifications.
         */
        fun <T> or(vararg specifications: Specification<T>): Specification<T> {
            return or(specifications.toList())
        }
    }

    /**
     * Determines whether specified value satisfies the current [Specification].
     *
     * @param value The value to test using the the current [Specification].
     * @return Returns true if the specified value satisfies the current [Specification].
     */
    abstract fun isSatisfiedBy(value: T): Boolean

    /**
     * Joins the current [Specification] with the specified other [Specification] using logical AND.
     *
     * @param other The other [Specification] to join with the current [Specification].
     * @return Returns the current [Specification] joined with the specified other [Specification] using logical AND.
     */
    fun and(other: Specification<T>) = LogicalAndSpecification(this, other)

    /**
     * Joins the current [Specification] with the specified other [Specification] using logical OR.
     *
     * @param other The other [Specification] to join with the current [Specification].
     * @return Returns the current [Specification] joined with the specified other [Specification] using logical OR.
     */
    fun or(other: Specification<T>) = LogicalOrSpecification(this, other)

    /**
     * Negates the current [Specification] using logical NOT.
     *
     * @return Returns the current [Specification] negated using logical NOT.
     */
    fun not() = LogicalNotSpecification(this)
}
