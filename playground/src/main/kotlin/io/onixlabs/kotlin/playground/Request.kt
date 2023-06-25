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
