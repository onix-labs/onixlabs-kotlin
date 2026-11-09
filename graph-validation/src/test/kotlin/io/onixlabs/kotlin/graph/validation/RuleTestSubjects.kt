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

import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Period
import java.util.UUID

data class BooleanSubject(val property: Boolean?)

data class ComparableSubject(val property: Int?)

data class GenericSubject(val property: Any?)

data class IterableSubject(val property: Iterable<Int>?)

data class StringSubject(val property: String?)

data class MapSubject(val property: Map<String, Any>?)

data class NameValuePair(val name: String, val value: Any?)

data class ComplexSubject(
    val title: String,
    val primaryReference: String?,
    val secondaryReference: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val arbitraryAmount: BigDecimal,
    val items: List<NameValuePair>?,
    val metadata: Map<String, UUID>,
    val defaultNameValuePair: NameValuePair,
) {

    // The old library called getMetadataValue("hello"); the new library calls member functions without arguments.
    fun helloMetadataValue(): UUID? = metadata["hello"]
}

fun ComplexSubject.elapsed(): Int = Period.between(startDate, endDate).days

data class NullSubject(
    val a: String? = null,
    val b: Int? = null,
    val c: Boolean? = null,
    val d: List<Any>? = null,
)

/**
 * Validates the subject in [ValidationMode.FIRST_FAILURE] (the replacement for the old STOP_ON_ERROR mode), asserts
 * that [ValidationResult.orThrow] throws, and returns the single failure carried by the exception.
 */
inline fun <reified T : Any> firstFailureOf(subject: T, noinline block: ValidationBuilder<T>.() -> Unit): ValidationFailure {
    val result = validate(subject, ValidationMode.FIRST_FAILURE, block)
    val exception = assertThrows<ValidationException> { result.orThrow() }
    return exception.result.failures.single()
}

/**
 * Validates the subject in [ValidationMode.ALL_FAILURES] and returns whether it is valid.
 */
inline fun <reified T : Any> isValid(subject: T, noinline block: ValidationBuilder<T>.() -> Unit): Boolean {
    return validate(subject, block = block).isValid
}
