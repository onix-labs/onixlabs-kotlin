/*
 * Copyright 2020-2026 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
public abstract class Specification<T> {

    public companion object {

        /**
         * Creates an [EmptySpecification] of the specified type.
         *
         * @param T The underlying type of of the value to be satisfied by the specification.
         * @return Returns an [EmptySpecification] of the specified type.
         */
        public fun <T> empty(value: Boolean): EmptySpecification<T> = EmptySpecification<T>(value)

        /**
         * Creates a logical AND specification from all of the specified specifications.
         *
         * @param T The underlying type of of the value to be satisfied by the specification.
         * @param specifications The specifications from which to create a logical AND specification.
         * @return Returns a logical AND specification from all of the specified specifications.
         */
        public fun <T> and(specifications: Iterable<Specification<T>>): Specification<T> {
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
        public fun <T> and(vararg specifications: Specification<T>): Specification<T> {
            return and(specifications.toList())
        }

        /**
         * Creates a logical OR specification from all of the specified specifications.
         *
         * @param T The underlying type of of the value to be satisfied by the specification.
         * @param specifications The specifications from which to create a logical OR specification.
         * @return Returns a logical OR specification from all of the specified specifications.
         */
        public fun <T> or(specifications: Iterable<Specification<T>>): Specification<T> {
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
        public fun <T> or(vararg specifications: Specification<T>): Specification<T> {
            return or(specifications.toList())
        }
    }

    /**
     * Determines whether specified value satisfies the current [Specification].
     *
     * @param value The value to test using the the current [Specification].
     * @return Returns true if the specified value satisfies the current [Specification].
     */
    public abstract fun isSatisfiedBy(value: T): Boolean

    /**
     * Joins the current [Specification] with the specified other [Specification] using logical AND.
     *
     * @param other The other [Specification] to join with the current [Specification].
     * @return Returns the current [Specification] joined with the specified other [Specification] using logical AND.
     */
    public fun and(other: Specification<T>): LogicalAndSpecification<T> = LogicalAndSpecification(this, other)

    /**
     * Joins the current [Specification] with the specified other [Specification] using logical OR.
     *
     * @param other The other [Specification] to join with the current [Specification].
     * @return Returns the current [Specification] joined with the specified other [Specification] using logical OR.
     */
    public fun or(other: Specification<T>): LogicalOrSpecification<T> = LogicalOrSpecification(this, other)

    /**
     * Negates the current [Specification] using logical NOT.
     *
     * @return Returns the current [Specification] negated using logical NOT.
     */
    public fun not(): LogicalNotSpecification<T> = LogicalNotSpecification(this)
}
