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
package io.onixlabs.kotlin.core.typeconverters

import io.onixlabs.kotlin.core.isInteger
import io.onixlabs.kotlin.core.math.isInteger
import io.onixlabs.kotlin.core.reflection.kotlinClass
import io.onixlabs.kotlin.core.toBigDecimal
import io.onixlabs.kotlin.core.toBigInteger
import io.onixlabs.kotlin.core.typeconverters.IllegalTypeConversionException.Companion.NUMERIC_LOSS_OF_PRECISION
import io.onixlabs.kotlin.core.typeconverters.IllegalTypeConversionException.Companion.NUMERIC_OVERFLOW
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Provides a mechanism to safely convert the specified value value to the [Char] type.
 */
public class CharTypeConverter : TypeConverter<Char>() {

    private companion object {
        val CHAR_MIN = Char.MIN_VALUE.code
        val CHAR_MAX = Char.MAX_VALUE.code
        const val EX_STR_TO_CHR = "Illegal type conversion. String value cannot be converted to Char."
    }

    /**
     * Converts the specified value to the [Char] type.
     *
     * @param value The value to convert.
     * @return Returns a [Char] representation of the specified value.
     */
    override fun convert(value: Any): Char = when (value) {
        is Boolean -> if (value) '1' else '0'
        is Byte -> value.toInt().toCharChecked()
        is Short -> value.toInt().toCharChecked()
        is Int -> value.toCharChecked()
        is Long -> value.toCharChecked()
        is BigInteger -> value.toCharChecked()
        is Float -> value.toCharChecked()
        is Double -> value.toCharChecked()
        is BigDecimal -> value.toCharChecked()
        is String -> value.toCharChecked()
        is Char -> value
        else -> throw IllegalTypeConversionException(value.kotlinClass, Char::class)
    }

    /**
     * Performs a checked conversion from [Int] to [Char].
     *
     * @return Returns a [Char] representation of the specified [Int].
     * @throws IllegalTypeConversionException if the value cannot be safely converted to [Char].
     */
    private fun Int.toCharChecked(): Char = when (this) {
        in CHAR_MIN..CHAR_MAX -> toChar()
        else -> throw NUMERIC_OVERFLOW
    }

    /**
     * Performs a checked conversion from [Long] to [Char].
     *
     * @return Returns a [Char] representation of the specified [Long].
     * @throws IllegalTypeConversionException if the value cannot be safely converted to [Char].
     */
    private fun Long.toCharChecked(): Char = when (this) {
        in CHAR_MIN.toLong()..CHAR_MAX.toLong() -> toInt().toChar()
        else -> throw NUMERIC_OVERFLOW
    }

    /**
     * Performs a checked conversion from [BigInteger] to [Char].
     *
     * @return Returns a [Char] representation of the specified [BigInteger].
     * @throws IllegalTypeConversionException if the value cannot be safely converted to [Char].
     */
    private fun BigInteger.toCharChecked(): Char = when (this) {
        in CHAR_MIN.toBigInteger()..CHAR_MAX.toBigInteger() -> toInt().toChar()
        else -> throw NUMERIC_OVERFLOW
    }

    /**
     * Performs a checked conversion from [Float] to [Char].
     *
     * @return Returns a [Char] representation of the specified [Float].
     * @throws IllegalTypeConversionException if the value cannot be safely converted to [Char].
     */
    private fun Float.toCharChecked(): Char = when {
        !isInteger() -> throw NUMERIC_LOSS_OF_PRECISION
        this in CHAR_MIN.toFloat()..CHAR_MAX.toFloat() -> toInt().toChar()
        else -> throw NUMERIC_OVERFLOW
    }

    /**
     * Performs a checked conversion from [Double] to [Char].
     *
     * @return Returns a [Char] representation of the specified [Double].
     * @throws IllegalTypeConversionException if the value cannot be safely converted to [Char].
     */
    private fun Double.toCharChecked(): Char = when {
        !isInteger() -> throw NUMERIC_LOSS_OF_PRECISION
        this in CHAR_MIN.toDouble()..CHAR_MAX.toDouble() -> toInt().toChar()
        else -> throw NUMERIC_OVERFLOW
    }

    /**
     * Performs a checked conversion from [BigDecimal] to [Char].
     *
     * @return Returns a [Char] representation of the specified [BigDecimal].
     * @throws IllegalTypeConversionException if the value cannot be safely converted to [Char].
     */
    private fun BigDecimal.toCharChecked(): Char = when {
        !isInteger() -> throw NUMERIC_LOSS_OF_PRECISION
        this in CHAR_MIN.toBigDecimal()..CHAR_MAX.toBigDecimal() -> toInt().toChar()
        else -> throw NUMERIC_OVERFLOW
    }

    /**
     * Performs a checked conversion from [String] to [Char].
     *
     * @return Returns a [Char] representation of the specified [BigInteger].
     * @throws IllegalTypeConversionException if the value cannot be safely converted to [String].
     */
    private fun String.toCharChecked(): Char = singleOrNull() ?: throw IllegalTypeConversionException(EX_STR_TO_CHR)
}
