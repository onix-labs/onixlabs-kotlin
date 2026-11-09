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

import io.onixlabs.kotlin.graph.GraphPath
import io.onixlabs.kotlin.graph.GraphRegistry
import io.onixlabs.kotlin.graph.graphs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

/**
 * Ports the old ComplexSubjectValidator and NullSubjectValidator scenarios. The old library described its rules by
 * calling validate() without a subject (emulation); the new library does so with registry.describe<T>().
 *
 * The old ComplexSubjectValidator nested two different validators for NameValuePair (one for items, one for
 * defaultNameValuePair). In the new library a nested type has one registered graph, so the common NameValuePair rules
 * live in its graph and the context-specific value rules are declared on the owning members.
 */
class ComplexSubjectTests {

    private val metadataValue = UUID.fromString("f1758a23-c14f-4397-957d-c63af8ca6273")
    private val boundary = LocalDate.of(2000, 1, 1)

    private val registry: GraphRegistry = complexSubjectRegistry(skipAnyTypedGraphs = false)

    // describe treats every registered graph as a possible subtype of an Any?-typed member, so NameValuePair.value is
    // described as NameValuePair and ComplexSubject; skipGraph() on that member works around it.
    private val describableRegistry: GraphRegistry = complexSubjectRegistry(skipAnyTypedGraphs = true)

    private fun complexSubjectRegistry(skipAnyTypedGraphs: Boolean): GraphRegistry = graphs {
        graph<NameValuePair> {
            validate {
                NameValuePair::name { mustNotStartWith("$") }
                NameValuePair::value {
                    mustNotBeNull()
                    if (skipAnyTypedGraphs) skipGraph()
                }
            }
        }
        graph<ComplexSubject> {
            validate {
                ComplexSubject::title {
                    mustHaveMinimumLength(10)
                    mustHaveMaximumLength(100)
                }
                properties(ComplexSubject::primaryReference, ComplexSubject::secondaryReference) {
                    mustNotBeNullOrBlank()
                    mustStartWith("REF")
                }
                ComplexSubject::secondaryReference { mustEndWith("_2ND") }
                ComplexSubject::startDate { mustBeLessThanOrEqualTo(boundary) }
                ComplexSubject::endDate { mustBeGreaterThan(boundary) }
                ComplexSubject::arbitraryAmount { mustBeGreaterThanOrEqualTo(BigDecimal.ZERO) }
                ComplexSubject::items {
                    mustNotBeNull()
                    mustHaveCount(10)
                }
                ComplexSubject::items.each {
                    must("nameValuePair.booleanValue", "must have a boolean value.") { it.value is Boolean }
                }
                ComplexSubject::metadata.values { mustBeEqualTo(metadataValue) }
                ComplexSubject::defaultNameValuePair {
                    must("nameValuePair.trueValue", "must have the value true.") { it.value == true }
                }
                function(ComplexSubject::elapsed) { mustBeGreaterThan(2) }
                ComplexSubject::helloMetadataValue { mustBeEqualTo(metadataValue) }
            }
        }
    }

    private fun validSubject() = ComplexSubject(
        title = "A sufficiently long title",
        primaryReference = "REF_1ST",
        secondaryReference = "REF_2ND",
        startDate = LocalDate.of(1999, 12, 1),
        endDate = LocalDate.of(2000, 1, 5),
        arbitraryAmount = BigDecimal.ONE,
        items = List(10) { NameValuePair("name$it", true) },
        metadata = mapOf("hello" to metadataValue),
        defaultNameValuePair = NameValuePair("default", true),
    )

    private val expectedRules: List<Triple<String, String, Map<String, Any?>>> by lazy {
        listOf(
                Triple("ComplexSubject.title", "string.minimumLength", mapOf("length" to 10)),
                Triple("ComplexSubject.title", "string.maximumLength", mapOf("length" to 100)),
                Triple("ComplexSubject.primaryReference", "string.notNullOrBlank", emptyMap()),
                Triple("ComplexSubject.primaryReference", "string.startsWith", mapOf("value" to "REF")),
                Triple("ComplexSubject.secondaryReference", "string.notNullOrBlank", emptyMap()),
                Triple("ComplexSubject.secondaryReference", "string.startsWith", mapOf("value" to "REF")),
                Triple("ComplexSubject.secondaryReference", "string.endsWith", mapOf("value" to "_2ND")),
                Triple("ComplexSubject.startDate", "comparable.lessThanOrEqualTo", mapOf("value" to boundary)),
                Triple("ComplexSubject.endDate", "comparable.greaterThan", mapOf("value" to boundary)),
                Triple("ComplexSubject.arbitraryAmount", "comparable.greaterThanOrEqualTo", mapOf("value" to BigDecimal.ZERO)),
                Triple("ComplexSubject.items", "generic.notNull", emptyMap()),
                Triple("ComplexSubject.items", "iterable.count", mapOf("count" to 10)),
                Triple("ComplexSubject.items[*]", "nameValuePair.booleanValue", emptyMap()),
                Triple("ComplexSubject.items[*].name", "string.notStartsWith", mapOf("value" to "$")),
                Triple("ComplexSubject.items[*].value", "generic.notNull", emptyMap()),
                Triple("ComplexSubject.metadata[*]", "generic.equalTo", mapOf("value" to metadataValue)),
                Triple("ComplexSubject.defaultNameValuePair", "nameValuePair.trueValue", emptyMap()),
                Triple("ComplexSubject.defaultNameValuePair.name", "string.notStartsWith", mapOf("value" to "$")),
                Triple("ComplexSubject.defaultNameValuePair.value", "generic.notNull", emptyMap()),
                Triple("ComplexSubject.elapsed()", "comparable.greaterThan", mapOf("value" to 2)),
                Triple("ComplexSubject.helloMetadataValue()", "generic.equalTo", mapOf("value" to metadataValue)),
        )
    }

    private val expectedOutput: List<String> by lazy {
        listOf(
                "ComplexSubject.title must have a length of at least 10.",
                "ComplexSubject.title must have a length of at most 100.",
                "ComplexSubject.primaryReference must not be null or blank.",
                "ComplexSubject.primaryReference must start with \"REF\".",
                "ComplexSubject.secondaryReference must not be null or blank.",
                "ComplexSubject.secondaryReference must start with \"REF\".",
                "ComplexSubject.secondaryReference must end with \"_2ND\".",
                "ComplexSubject.startDate must be less than or equal to 2000-01-01.",
                "ComplexSubject.endDate must be greater than 2000-01-01.",
                "ComplexSubject.arbitraryAmount must be greater than or equal to 0.",
                "ComplexSubject.items must not be null.",
                "ComplexSubject.items must contain exactly 10 elements.",
                "ComplexSubject.items[*] must have a boolean value.",
                "ComplexSubject.items[*].name must not start with \"$\".",
                "ComplexSubject.items[*].value must not be null.",
                "ComplexSubject.metadata[*] must be equal to $metadataValue.",
                "ComplexSubject.defaultNameValuePair must have the value true.",
                "ComplexSubject.defaultNameValuePair.name must not start with \"$\".",
                "ComplexSubject.defaultNameValuePair.value must not be null.",
                "ComplexSubject.elapsed() must be greater than 2.",
                "ComplexSubject.helloMetadataValue() must be equal to $metadataValue.",
        )
    }

    @Test
    fun `describe should produce the expected rules for every member`() {

        // Act
        val result = registry.describe<ComplexSubject>()

        // Assert
        assertEquals(expectedRules, result.failures.map { Triple(it.path.toString(), it.rule, it.parameters) })
    }

    @Test
    fun `describe should produce the expected validation output`() {

        // Act
        val result = registry.describe<ComplexSubject>()

        // Assert
        assertEquals(expectedOutput, result.failures.map { it.toString() })
    }

    @Test
    fun `describe should produce the expected rules for every member when Any-typed members skip their graphs`() {

        // Act
        val result = describableRegistry.describe<ComplexSubject>()

        // Assert
        assertEquals(expectedRules, result.failures.map { Triple(it.path.toString(), it.rule, it.parameters) })
    }

    @Test
    fun `describe should produce the expected validation output when Any-typed members skip their graphs`() {

        // Act
        val result = describableRegistry.describe<ComplexSubject>()

        // Assert
        assertEquals(ValidationMode.TEST, result.mode)
        assertEquals(expectedOutput, result.failures.map { it.toString() })
    }

    @Test
    fun `TEST mode with a subject should report every rule at concrete paths`() {

        // Arrange
        val subject = validSubject().copy(items = listOf(NameValuePair("a", true), NameValuePair("b", false)))

        // Act
        val result = registry.validate(subject, ValidationMode.TEST)

        // Assert
        assertEquals(ValidationMode.TEST, result.mode)
        assertEquals(
            listOf("ComplexSubject.items[0]", "ComplexSubject.items[0].name", "ComplexSubject.items[0].value"),
            result.under(GraphPath.of(ComplexSubject::items).index(0)).map { it.path.toString() },
        )
        assertEquals(24, result.failures.size)
        assertTrue(result.failures.any { it.path.toString() == "ComplexSubject.metadata[\"hello\"]" })
    }

    @Test
    fun `validate should pass for a valid complex subject`() {

        // Act
        val result = registry.validate(validSubject())

        // Assert
        assertTrue(result.isValid, result.toString())
    }

    @Test
    fun `validate should report failures at concrete paths for an invalid complex subject`() {

        // Arrange
        val subject = validSubject().copy(
            title = "Short",
            secondaryReference = "REF_3RD",
            items = listOf(NameValuePair("ok", true), NameValuePair("\$bad", null)),
            metadata = mapOf("hello" to UUID.randomUUID()),
            defaultNameValuePair = NameValuePair("default", false),
            endDate = LocalDate.of(1999, 12, 2),
        )

        // Act
        val result = registry.validate(subject)

        // Assert
        assertEquals(
            listOf(
                "ComplexSubject.title string.minimumLength",
                "ComplexSubject.secondaryReference string.endsWith",
                "ComplexSubject.endDate comparable.greaterThan",
                "ComplexSubject.items iterable.count",
                "ComplexSubject.items[1] nameValuePair.booleanValue",
                "ComplexSubject.items[1].name string.notStartsWith",
                "ComplexSubject.items[1].value generic.notNull",
                "ComplexSubject.metadata[\"hello\"] generic.equalTo",
                "ComplexSubject.defaultNameValuePair nameValuePair.trueValue",
                "ComplexSubject.elapsed() comparable.greaterThan",
                "ComplexSubject.helloMetadataValue() generic.equalTo",
            ),
            result.failures.map { "${it.path} ${it.rule}" },
        )
    }

    @Test
    fun `FIRST_FAILURE should return the first failure, and orThrow should throw it`() {

        // Arrange
        val subject = validSubject().copy(title = "Short", endDate = LocalDate.of(1999, 12, 2))

        // Act
        val result = registry.validate(subject, ValidationMode.FIRST_FAILURE)
        val exception = assertThrows<ValidationException> { result.orThrow() }

        // Assert
        assertEquals(listOf("ComplexSubject.title string.minimumLength"), result.failures.map { "${it.path} ${it.rule}" })
        assertEquals("ComplexSubject.title must have a length of at least 10.", exception.message)
    }

    private val nullSubjectRegistry = graphs {
        graph<NullSubject> { validate { all { mustNotBeNull() } } }
    }

    @Test
    fun `describe should produce the expected rules for a subject whose rules apply to all members`() {

        // Act
        val result = nullSubjectRegistry.describe<NullSubject>()

        // Assert
        assertEquals(
            listOf("NullSubject.a", "NullSubject.b", "NullSubject.c", "NullSubject.d"),
            result.failures.map { it.path.toString() },
        )
        assertTrue(result.failures.all { it.rule == "generic.notNull" && it.parameters.isEmpty() })
    }

    @Test
    fun `describe should produce the expected validation output for a subject whose rules apply to all members`() {

        // Act
        val result = nullSubjectRegistry.describe<NullSubject>()

        // Assert
        assertEquals(
            listOf(
                "NullSubject.a must not be null.",
                "NullSubject.b must not be null.",
                "NullSubject.c must not be null.",
                "NullSubject.d must not be null.",
            ),
            result.failures.map { it.toString() },
        )
    }

    @Test
    fun `describe should produce the expected rules for a subject whose rules apply to all members when Any-typed members skip their graphs`() {

        // Arrange
        val registry = graphs {
            graph<NullSubject> {
                validate {
                    all { mustNotBeNull() }
                    NullSubject::d { skipGraph() }
                }
            }
        }

        // Act
        val result = registry.describe<NullSubject>()

        // Assert
        assertEquals(
            listOf(
                "NullSubject.a must not be null.",
                "NullSubject.b must not be null.",
                "NullSubject.c must not be null.",
                "NullSubject.d must not be null.",
            ),
            result.failures.map { it.toString() },
        )
        assertTrue(result.failures.all { it.rule == "generic.notNull" && it.parameters.isEmpty() })
    }

    @Test
    fun `validate should fail every null member and pass when every member is set`() {

        // Act
        val invalid = nullSubjectRegistry.validate(NullSubject())
        val valid = nullSubjectRegistry.validate(NullSubject("a", 1, true, emptyList()))

        // Assert
        assertEquals(4, invalid.failures.size)
        assertTrue(valid.isValid)
    }
}
