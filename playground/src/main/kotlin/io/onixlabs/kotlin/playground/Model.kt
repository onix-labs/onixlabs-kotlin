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

import io.onixlabs.kotlin.graph.graph
import io.onixlabs.kotlin.graph.graphs
import io.onixlabs.kotlin.graph.validation.mustBeGreaterThanOrEqualTo
import io.onixlabs.kotlin.graph.validation.mustContainAny
import io.onixlabs.kotlin.graph.validation.mustHaveCountWithinRange
import io.onixlabs.kotlin.graph.validation.mustNotBeNull
import io.onixlabs.kotlin.graph.validation.mustNotBeNullOrBlank
import io.onixlabs.kotlin.graph.validation.validate

val RequestGraph = graph<Request> {
    validate {
        Request::text {
            mustNotBeNullOrBlank()
            mustContainAny(listOf("Alice", "Bob", "Charlie"))
        }

        Request::number { mustBeGreaterThanOrEqualTo(10) }

        Request::numberElements {
            mustNotBeNull()
            mustHaveCountWithinRange(1, 100)
        }

        Request::numberElements.each { mustBeGreaterThanOrEqualTo(123) }
    }
}

val SomethingGraph = graph<Something> {
    validate { Something::x { mustBeGreaterThanOrEqualTo(1) } }
}

val Model = graphs {
    include(RequestGraph, SomethingGraph)
}
