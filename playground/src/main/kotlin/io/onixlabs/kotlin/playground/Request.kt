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
package io.onixlabs.kotlin.playground

import java.time.Instant
import java.util.UUID

data class Request(
    val text: String? = null,
    val textNotNull: String = "",
    val number: Int? = null,
    val numberNotNull: Int = 0,
    val boolean: Boolean? = null,
    val booleanNotNull: Boolean = false,
    val timestamp: Instant? = null,
    val timestampNotNull: Instant = Instant.MIN,
    val something: Something? = null,
    val somethingNotNull: Something = Something(0, UUID.randomUUID()),
    val textElements: Collection<String?>? = null,
    val textElementsNotNull: Collection<String> = emptyList(),
    val numberElements: Collection<Long?>? = null,
    val numberElementsNotNull: Collection<Long> = emptyList(),
)

data class Something(val x: Int, val y: UUID)

data class RequestSummary(val text: String?, val number: Long?, val numberElements: List<String>?)
