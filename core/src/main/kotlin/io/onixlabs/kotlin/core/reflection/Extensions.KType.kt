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
package io.onixlabs.kotlin.core.reflection

import io.onixlabs.kotlin.core.EMPTY
import io.onixlabs.kotlin.core.wrap
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

/**
 * Gets the formatted simple name of the type, including any generic type arguments.
 */
public val KType.formattedSimpleName: String
    get() = "${jvmErasure.simpleName}${getFormattedArguments(false)}$nullableToken"

/**
 * Gets the formatted qualified name of the type, including any generic type arguments.
 */
public val KType.formattedQualifiedName: String
    get() = "${jvmErasure.qualifiedName}${getFormattedArguments(true)}$nullableToken"

/**
 * Gets a nullable token for the specified string, or an empty string if the type is non-nullable.
 */
private val KType.nullableToken: String
    get() = if (isMarkedNullable) "?" else String.EMPTY

/**
 * Gets a formatted list of generic type argument names, formatting nested type arguments recursively.
 *
 * @param useQualifiedNames Determines whether to use simple or qualified type names.
 * @return Returns a formatted list of generic type argument names, or an empty string if the type has no type arguments.
 */
private fun KType.getFormattedArguments(useQualifiedNames: Boolean): String {
    if (arguments.isEmpty()) return String.EMPTY
    return arguments.joinToString {
        val type = it.type
        when {
            type == null -> "*"
            useQualifiedNames -> type.formattedQualifiedName
            else -> type.formattedSimpleName
        }
    }.wrap("<", ">")
}
