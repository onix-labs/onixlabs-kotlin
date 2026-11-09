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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProjectionOverloadTests {

    data class Source(val first: String, val emailAddress: String)
    data class Target(val name: String, val email: String, val channel: String, val code: String)

    @Test
    fun `lambdas, constants, and source properties should each resolve to the intended overload`() {
        val target = project(Source("Ada", "ada@example.com"), Target::class) {
            Target::name from { it.first.uppercase() }
            Target::email from Source::emailAddress
            Target::channel from "web"
            parameter("code", "C-1")
        }
        assertEquals(Target("ADA", "ada@example.com", "web", "C-1"), target)
    }

    @Test
    fun `a mapped value of the wrong type should be reported clearly`() {
        @Suppress("UNCHECKED_CAST")
        val exception = org.junit.jupiter.api.assertThrows<ProjectionException> {
            project(Source("Ada", "ada@example.com"), Target::class) {
                (Target::name as kotlin.reflect.KProperty1<Target, Any>) from 42
            }
        }
        assertEquals("Target.name: the mapping returned 42 (Int), which is not kotlin.String.", exception.failures.first().toString())
    }
}
