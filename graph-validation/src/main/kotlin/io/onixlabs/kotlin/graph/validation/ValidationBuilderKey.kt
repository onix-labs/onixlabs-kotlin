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

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal sealed interface ValidationBuilderKey<T> {
    val property: KProperty1<*, T>
    public fun toString(type: KClass<*>): String

    data class Scalar<T>(override val property: KProperty1<*, T>) : ValidationBuilderKey<T> {
        override fun toString(type: KClass<*>): String {
            return "Property '${type.simpleName}.${property.name}' "
        }
    }

    data class Vector<T>(override val property: KProperty1<*, T>, val index: Int) : ValidationBuilderKey<T> {
        override fun toString(type: KClass<*>): String {
            return "Property '${type.simpleName}.${property.name}' element [$index] "
        }
    }
}
