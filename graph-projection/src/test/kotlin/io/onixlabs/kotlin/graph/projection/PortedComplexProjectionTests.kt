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
package io.onixlabs.kotlin.graph.projection

import io.onixlabs.kotlin.graph.graph
import io.onixlabs.kotlin.graph.graphs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

/**
 * Scenarios ported from the old onixlabs-kotlin-projection ComplexProjectorTests.
 */
class PortedComplexProjectionTests {

    data class Post(val title: String, val text: String, val date: LocalDate)

    data class Source(
        val id: String,
        val firstName: String,
        val lastName: String,
        val description: String?,
        val posts: Iterable<Post>,
    )

    data class Target(
        val id: UUID,
        val fullName: String,
        val description: String,
        val postCount: Int,
        val posts: Iterable<Post>,
        val reference: Long = Long.MAX_VALUE,
    )

    private val source = Source(
        id = "079861fc-97f2-4af5-a47b-ece5a433e623",
        firstName = "John",
        lastName = "Smith",
        description = "Hello, world!",
        posts = listOf(Post("My first post", "Lorem ipsum...", LocalDate.of(2020, 1, 1))),
    )

    private val expected = Target(
        id = UUID.fromString("079861fc-97f2-4af5-a47b-ece5a433e623"),
        fullName = "John Smith",
        description = "Hello, world!",
        postCount = 1,
        posts = listOf(Post("My first post", "Lorem ipsum...", LocalDate.of(2020, 1, 1))),
        reference = Long.MAX_VALUE,
    )

    // Replaces the old SourceToTargetProjector subclass: a reusable projection is declared once on the source graph.
    private val sourceGraph = graph<Source> {
        project<_, Target> {
            Target::fullName from { "${it.firstName} ${it.lastName}" }
            Target::description from { it.description ?: "No description" }
            Target::postCount from { it.posts.count() }
        }
    }

    @Test
    fun `projection can project source object to target with no null parameters`() {

        // Act
        val target = project(source, Target::class) {
            parameter(Target::fullName, "${source.firstName} ${source.lastName}")
            parameter(Target::description) { it.description ?: "No description" }
            parameter(Target::postCount, source.posts.count())
        }

        // Assert
        assertEquals(expected, target)
    }

    @Test
    fun `a projection declared on the source graph can project source object to target with no null parameters`() {

        // Arrange
        val registry = graphs { include(sourceGraph) }

        // Act
        val target = registry.project<Target>(source)

        // Assert
        assertEquals(expected, target)
    }

    @Test
    fun `a projection declared on the source graph should apply the fallback for a null description`() {

        // Arrange
        val registry = graphs { include(sourceGraph) }

        // Act
        val target = registry.project<Target>(source.copy(description = null))

        // Assert
        assertEquals(expected.copy(description = "No description"), target)
    }
}
