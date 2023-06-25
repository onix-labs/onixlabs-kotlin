/**
 * Copyright 2023 ONIXLabs
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

import java.util.UUID

/**
 * Gets an empty string.
 */
val String.Companion.EMPTY: String get() = ""

fun String.containsAll(values: Iterable<String>): Boolean {
    return values.all { it in this }
}

fun String.containsAll(vararg values: String): Boolean {
    return containsAll(values.toList())
}

fun String.containsAny(values: Iterable<String>): Boolean {
    return values.any { it in this }
}

fun String.containsAny(vararg values: String): Boolean {
    return containsAny(values.toList())
}

fun String.isRegexMatch(pattern: Regex): Boolean {
    return pattern.matches(this)
}

fun String.isRegexMatch(pattern: String): Boolean {
    return isRegexMatch(Regex(pattern))
}

fun String.isValidUUID(): Boolean {
    return try {
        UUID.fromString(this)
        true
    } catch (ex: Exception) {
        false
    }
}

/**
 * Determines whether the current [String] is a valid Mod 10 (Luhn) checksum.
 *
 * @receiver The current [String] to check.
 * @return Returns true if the current [String] is a valid Mod 10 (Luhn) checksum; otherwise, false.
 */
fun String.isValidMod10Checksum(): Boolean {
    fun isValidInput(value: String): Boolean {
        return value.all { it.isDigit() } && value.length > 1
    }

    fun computeChecksumDigit(value: String, index: Int, digit: Int): Int = when {
        (value.length - index + 1) % 2 == 0 -> digit
        digit >= 5 -> digit * 2 - 9
        else -> digit * 2
    }

    fun isValidChecksum(value: String): Boolean {
        return value
            .map(Character::getNumericValue)
            .mapIndexed { index, digit -> computeChecksumDigit(value, index, digit) }
            .sum() % 10 == 0
    }

    return isValidInput(this) && isValidChecksum(this)
}

/**
 * Formats the current [String] to UPPER_SNAKE_CASE.
 *
 * @receiver The current [String] to format.
 * @returns Returns the current [String] formatted to UPPER_SNAKE_CASE.
 */
fun String.toUpperSnakeCase(): String = buildString {
    val stringThis = this@toUpperSnakeCase
    stringThis.forEachIndexed { index, char ->
        if (index > 0
            && index + 1 < stringThis.length
            && !stringThis[index + 1].isUpperCase()
            && char.isUpperCase()
        ) append("_")
        append(char.uppercaseChar())
    }
}

/**
 * Formats the current [String] to lower_snake_case.
 *
 * @receiver The current [String] to format.
 * @returns Returns the current [String] formatted to lower_snake_case.
 */
fun String.toLowerSnakeCase(): String = buildString {
    val stringThis = this@toLowerSnakeCase
    stringThis.forEachIndexed { index, char ->
        if (index > 0
            && index + 1 < stringThis.length
            && !stringThis[index + 1].isUpperCase()
            && char.isUpperCase()
        ) append("_")
        append(char.lowercaseChar())
    }
}

/**
 * Formats the current [String] to UPPER-KEBAB-CASE.
 *
 * @receiver The current [String] to format.
 * @returns Returns the current [String] formatted to UPPER-KEBAB-CASE.
 */
fun String.toUpperKebabCase(): String = buildString {
    val stringThis = this@toUpperKebabCase
    stringThis.forEachIndexed { index, char ->
        if (index > 0
            && index + 1 < stringThis.length
            && !stringThis[index + 1].isUpperCase()
            && char.isUpperCase()
        ) append("-")
        append(char.uppercaseChar())
    }
}

/**
 * Formats the current [String] to lower-kebab-case.
 *
 * @receiver The current [String] to format.
 * @returns Returns the current [String] formatted to lower-kebab-case.
 */
fun String.toLowerKebabCase(): String = buildString {
    val stringThis = this@toLowerKebabCase
    stringThis.forEachIndexed { index, char ->
        if (index > 0
            && index + 1 < stringThis.length
            && !stringThis[index + 1].isUpperCase()
            && char.isUpperCase()
        ) append("-")
        append(char.lowercaseChar())
    }
}

/**
 * Wraps the current [String] between the specified start and end strings.
 *
 * @receiver The current [String] to wrap.
 * @param start The start [String] to prepend to the current [String].
 * @param end The end [String] to append to the current [String].
 * @return Returns the current [String] between the specified start and end strings.
 */
fun String.wrap(start: String, end: String): String {
    return "$start$this$end"
}
