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

/**
 * Requires the map to contain the specified key. Rule `map.containsKey` with parameter `key`.
 *
 * @param key The key that the map must contain.
 * @param message The message reported when the rule fails.
 */
public fun <K> MemberValidationBuilder<out Map<K, *>?>.mustContainKey(key: K, message: String = "must contain the key $key.") {
    rule("map.containsKey", message, mapOf("key" to key)) { (it as Map<*, *>).containsKey(key) }
}

/**
 * Requires the map not to contain the specified key. Rule `map.notContainsKey` with parameter `key`.
 *
 * @param key The key that the map must not contain.
 * @param message The message reported when the rule fails.
 */
public fun <K> MemberValidationBuilder<out Map<K, *>?>.mustNotContainKey(key: K, message: String = "must not contain the key $key.") {
    rule("map.notContainsKey", message, mapOf("key" to key)) { !(it as Map<*, *>).containsKey(key) }
}

/**
 * Requires the map to be empty. Rule `map.empty`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out Map<*, *>?>.mustBeEmpty(message: String = "must be empty.") {
    rule("map.empty", message) { (it as Map<*, *>).isEmpty() }
}

/**
 * Requires the map not to be empty. Rule `map.notEmpty`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out Map<*, *>?>.mustNotBeEmpty(message: String = "must not be empty.") {
    rule("map.notEmpty", message) { (it as Map<*, *>).isNotEmpty() }
}

/**
 * Requires the number of entries to be within the specified inclusive range. Rule `map.countWithinRange` with parameters `minimum` and `maximum`.
 *
 * @param minimum The inclusive minimum number of entries.
 * @param maximum The inclusive maximum number of entries.
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out Map<*, *>?>.mustHaveCountWithinRange(
    minimum: Int,
    maximum: Int,
    message: String = "must contain between $minimum and $maximum entries.",
) {
    rule("map.countWithinRange", message, mapOf("minimum" to minimum, "maximum" to maximum)) { (it as Map<*, *>).size in minimum..maximum }
}

/**
 * Requires the map to contain the specified value. Rule `map.containsValue` with parameter `value`.
 *
 * @param value The value that the map must contain.
 * @param message The message reported when the rule fails.
 */
public fun <V> MemberValidationBuilder<out Map<*, V>?>.mustContainValue(value: V, message: String = "must contain the value $value.") {
    rule("map.containsValue", message, mapOf("value" to value)) { (it as Map<*, *>).containsValue(value) }
}

/**
 * Requires the map not to contain the specified value. Rule `map.notContainsValue` with parameter `value`.
 *
 * @param value The value that the map must not contain.
 * @param message The message reported when the rule fails.
 */
public fun <V> MemberValidationBuilder<out Map<*, V>?>.mustNotContainValue(value: V, message: String = "must not contain the value $value.") {
    rule("map.notContainsValue", message, mapOf("value" to value)) { !(it as Map<*, *>).containsValue(value) }
}

/**
 * Requires the map to contain the specified entry. Rule `map.containsEntry` with parameters `key` and `value`.
 *
 * @param key The key of the entry.
 * @param value The value of the entry.
 * @param message The message reported when the rule fails.
 */
public fun <K, V> MemberValidationBuilder<out Map<K, V>?>.mustContainEntry(key: K, value: V, message: String = "must contain the entry $key=$value.") {
    rule("map.containsEntry", message, mapOf("key" to key, "value" to value)) { (it as Map<*, *>).let { map -> map.containsKey(key) && map[key] == value } }
}

/**
 * Requires the map not to contain the specified entry. Rule `map.notContainsEntry` with parameters `key` and `value`.
 *
 * @param key The key of the entry.
 * @param value The value of the entry.
 * @param message The message reported when the rule fails.
 */
public fun <K, V> MemberValidationBuilder<out Map<K, V>?>.mustNotContainEntry(key: K, value: V, message: String = "must not contain the entry $key=$value.") {
    rule("map.notContainsEntry", message, mapOf("key" to key, "value" to value)) { (it as Map<*, *>).let { map -> !(map.containsKey(key) && map[key] == value) } }
}
