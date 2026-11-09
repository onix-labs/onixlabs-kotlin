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
package io.onixlabs.kotlin.graph.validation

import io.onixlabs.kotlin.core.containsAll
import io.onixlabs.kotlin.core.containsAny
import io.onixlabs.kotlin.core.isValidMod10Checksum
import io.onixlabs.kotlin.core.isValidUUID

/**
 * Requires the string to be null or blank. Rule `string.nullOrBlank`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustBeNullOrBlank(message: String = "must be null or blank.") {
    rule("string.nullOrBlank", message, judgesNull = true) { (it as String?).isNullOrBlank() }
}

/**
 * Requires the string not to be null or blank. Rule `string.notNullOrBlank`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustNotBeNullOrBlank(message: String = "must not be null or blank.") {
    rule("string.notNullOrBlank", message, judgesNull = true) { !(it as String?).isNullOrBlank() }
}

/**
 * Requires the string to be null or empty. Rule `string.nullOrEmpty`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustBeNullOrEmpty(message: String = "must be null or empty.") {
    rule("string.nullOrEmpty", message, judgesNull = true) { (it as String?).isNullOrEmpty() }
}

/**
 * Requires the string not to be null or empty. Rule `string.notNullOrEmpty`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustNotBeNullOrEmpty(message: String = "must not be null or empty.") {
    rule("string.notNullOrEmpty", message, judgesNull = true) { !(it as String?).isNullOrEmpty() }
}

/**
 * Requires the string to be blank. Rule `string.blank`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustBeBlank(message: String = "must be blank.") {
    rule("string.blank", message) { (it as String).isBlank() }
}

/**
 * Requires the string not to be blank. Rule `string.notBlank`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustNotBeBlank(message: String = "must not be blank.") {
    rule("string.notBlank", message) { (it as String).isNotBlank() }
}

/**
 * Requires the string to be empty. Rule `string.empty`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustBeEmpty(message: String = "must be empty.") {
    rule("string.empty", message) { (it as String).isEmpty() }
}

/**
 * Requires the string not to be empty. Rule `string.notEmpty`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustNotBeEmpty(message: String = "must not be empty.") {
    rule("string.notEmpty", message) { (it as String).isNotEmpty() }
}

/**
 * Requires the string to contain the specified value. Rule `string.contains` with parameter `value`.
 *
 * @param value The value that the string must contain.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustContain(value: String, message: String = "must contain \"$value\".") {
    rule("string.contains", message, mapOf("value" to value)) { (it as String).contains(value) }
}

/**
 * Requires the string not to contain the specified value. Rule `string.notContains` with parameter `value`.
 *
 * @param value The value that the string must not contain.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustNotContain(value: String, message: String = "must not contain \"$value\".") {
    rule("string.notContains", message, mapOf("value" to value)) { !(it as String).contains(value) }
}

/**
 * Requires the string to contain all of the specified values. Rule `string.containsAll` with parameter `values`.
 *
 * @param values The values that the string must contain.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustContainAll(values: Iterable<String>, message: String = "must contain all of: ${values.joinToString()}.") {
    rule("string.containsAll", message, mapOf("values" to values.toList())) { (it as String).containsAll(values) }
}

/**
 * Requires the string to contain any of the specified values. Rule `string.containsAny` with parameter `values`.
 *
 * @param values The values, one of which the string must contain.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustContainAny(values: Iterable<String>, message: String = "must contain any of: ${values.joinToString()}.") {
    rule("string.containsAny", message, mapOf("values" to values.toList())) { (it as String).containsAny(values) }
}

/**
 * Requires the string to start with the specified value. Rule `string.startsWith` with parameter `value`.
 *
 * @param value The value with which the string must start.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustStartWith(value: String, message: String = "must start with \"$value\".") {
    rule("string.startsWith", message, mapOf("value" to value)) { (it as String).startsWith(value) }
}

/**
 * Requires the string not to start with the specified value. Rule `string.notStartsWith` with parameter `value`.
 *
 * @param value The value with which the string must not start.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustNotStartWith(value: String, message: String = "must not start with \"$value\".") {
    rule("string.notStartsWith", message, mapOf("value" to value)) { !(it as String).startsWith(value) }
}

/**
 * Requires the string to end with the specified value. Rule `string.endsWith` with parameter `value`.
 *
 * @param value The value with which the string must end.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustEndWith(value: String, message: String = "must end with \"$value\".") {
    rule("string.endsWith", message, mapOf("value" to value)) { (it as String).endsWith(value) }
}

/**
 * Requires the string not to end with the specified value. Rule `string.notEndsWith` with parameter `value`.
 *
 * @param value The value with which the string must not end.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustNotEndWith(value: String, message: String = "must not end with \"$value\".") {
    rule("string.notEndsWith", message, mapOf("value" to value)) { !(it as String).endsWith(value) }
}

/**
 * Requires the string to have exactly the specified length. Rule `string.length` with parameter `length`.
 *
 * @param length The length that the string must have.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustHaveLength(length: Int, message: String = "must have a length of $length.") {
    rule("string.length", message, mapOf("length" to length)) { (it as String).length == length }
}

/**
 * Requires the length of the string to be within the specified inclusive range. Rule `string.lengthWithinRange` with parameters `minimum` and `maximum`.
 *
 * @param minimum The inclusive minimum length.
 * @param maximum The inclusive maximum length.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustHaveLengthWithinRange(
    minimum: Int,
    maximum: Int,
    message: String = "must have a length between $minimum and $maximum.",
) {
    rule("string.lengthWithinRange", message, mapOf("minimum" to minimum, "maximum" to maximum)) { (it as String).length in minimum..maximum }
}

/**
 * Requires the length of the string to be within the specified inclusive range. Rule `string.lengthWithinRange` with parameters `minimum` and `maximum`.
 *
 * @param range The inclusive range of the length.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustHaveLengthWithinRange(
    range: IntRange,
    message: String = "must have a length between ${range.first} and ${range.last}.",
) {
    mustHaveLengthWithinRange(range.first, range.last, message)
}

/**
 * Requires the length of the string to be at most the specified length. Rule `string.maximumLength` with parameter `length`.
 *
 * @param length The inclusive maximum length.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustHaveMaximumLength(length: Int, message: String = "must have a length of at most $length.") {
    rule("string.maximumLength", message, mapOf("length" to length)) { (it as String).length <= length }
}

/**
 * Requires the length of the string to be at least the specified length. Rule `string.minimumLength` with parameter `length`.
 *
 * @param length The inclusive minimum length.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustHaveMinimumLength(length: Int, message: String = "must have a length of at least $length.") {
    rule("string.minimumLength", message, mapOf("length" to length)) { (it as String).length >= length }
}

/**
 * Requires the string to be a valid Mod10 (Luhn) checksum. Rule `string.mod10Checksum`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustBeValidMod10Checksum(message: String = "must be a valid Mod10 (Luhn) checksum.") {
    rule("string.mod10Checksum", message) { (it as String).isValidMod10Checksum() }
}

/**
 * Requires the whole string to match the specified regular expression. Rule `string.matches` with parameter `pattern`.
 *
 * @param pattern The regular expression that the string must match.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustMatchRegularExpression(pattern: String, message: String = "must match the regular expression $pattern.") {
    val regex = Regex(pattern)
    rule("string.matches", message, mapOf("pattern" to pattern)) { regex.matches(it as String) }
}

/**
 * Requires the string to be a valid UUID. Rule `string.uuid`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out String?>.mustBeValidUUID(message: String = "must be a valid UUID.") {
    rule("string.uuid", message) { (it as String).isValidUUID() }
}
