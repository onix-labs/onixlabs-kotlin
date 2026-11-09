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
 * Gets the constant ZERO of type [Float].
 */
public val Float.Companion.ZERO: Float get() = 0.0F

/**
 * Gets the constant ONE of type [Float].
 */
public val Float.Companion.ONE: Float get() = 1.0F

/**
 * Determines whether the current [Float] value represents an integer.
 *
 * @receiver The current [Float] value to determine is an integer.
 * @return Returns true of the current [Float] value represents an integer; otherwise false.
 */
public fun Float.isInteger(): Boolean {
    return this % Float.ONE == Float.ZERO
}

/**
 * Converts the current [Float] to a [BigInteger] value.
 *
 * @receiver The current [Float] to convert.
 * @return Returns a [BigInteger] representation of the current [Float] value.
 */
public fun Float.toBigInteger(): BigInteger {
    return BigInteger.valueOf(toLong())
}

/**
 * Converts the current [Float] to a [BigDecimal] value.
 *
 * @receiver The current [Float] to convert.
 * @return Returns a [BigDecimal] representation of the current [Float] value.
 */
public fun Float.toBigDecimal(): BigDecimal {
    return BigDecimal.valueOf(toDouble())
}
