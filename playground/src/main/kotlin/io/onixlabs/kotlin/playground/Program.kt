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

import io.onixlabs.kotlin.graph.projection.project
import io.onixlabs.kotlin.graph.validation.describe
import io.onixlabs.kotlin.graph.validation.validate

fun main() {
    val request = Request(text = "Dave", number = 3, numberElements = listOf(200, 5))
    val result = Model.validate(request)

    println("== Failures")
    println(result)
    println()
    println("== Flat JSON")
    println(result.toFlatJson())
    println()
    println("== Tree JSON")
    println(result.toTreeJson())
    println()
    println("== Describe Request")
    println(Model.describe<Request>())
    println()
    println("== Projection (no configuration)")
    println(Model.project<RequestSummary>(request))
}
