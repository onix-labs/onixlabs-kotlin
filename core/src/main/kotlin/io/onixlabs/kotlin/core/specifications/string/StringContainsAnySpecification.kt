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
package io.onixlabs.kotlin.core.specifications.string

import io.onixlabs.kotlin.core.containsAny
import io.onixlabs.kotlin.core.specifications.Specification

public class StringContainsAnySpecification(private val values: Iterable<String>) : Specification<String?>() {

    public constructor(vararg values: String) : this(values.toList())

    override fun isSatisfiedBy(value: String?): Boolean = value?.containsAny(values) ?: false
}
