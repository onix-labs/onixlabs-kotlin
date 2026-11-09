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
package io.onixlabs.kotlin.core

import java.math.BigDecimal
import java.math.BigInteger

/**
 * Gets the constant ZERO of type [Double].
 */
public val Double.Companion.ZERO: Double get() = 0.0

/**
 * Gets the constant ONE of type [Double].
 */
public val Double.Companion.ONE: Double get() = 1.0

/**
 * Determines whether the current [Double] value represents an integer.
 *
 * @receiver The current [Double] value to determine is an integer.
 * @return Returns true of the current [Double] value represents an integer; otherwise false.
 */
public fun Double.isInteger(): Boolean {
    return this % Double.ONE == Double.ZERO
}

/**
 * Converts the current [Double] to a [BigInteger] value.
 *
 * @receiver The current [Double] to convert.
 * @return Returns a [BigInteger] representation of the current [Double] value.
 */
public fun Double.toBigInteger(): BigInteger {
    return BigInteger.valueOf(toLong())
}

/**
 * Converts the current [Double] to a [BigDecimal] value.
 *
 * @receiver The current [Double] to convert.
 * @return Returns a [BigDecimal] representation of the current [Double] value.
 */
public fun Double.toBigDecimal(): BigDecimal {
    return BigDecimal.valueOf(this)
}
