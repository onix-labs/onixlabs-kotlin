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

import io.onixlabs.kotlin.core.reflection.getArgumentType
import io.onixlabs.kotlin.core.reflection.toTypedClass
import kotlin.reflect.KClass

public abstract class Validator<T : Any>(private val action: ValidationBuilder<T>.() -> Unit) {

    public open val subjectType: KClass<T> by lazy { javaClass.getArgumentType(0).toTypedClass<T>().kotlin }

    public fun validate(): ValidationResult {
        val context = ValidationBuilderContext(subjectType, null, false)
        val builder = ValidationBuilder(context).apply(action)
        return builder.toValidationResult()
    }

    public fun validate(subject: T, stopOnError: Boolean = false): ValidationResult {
        val context = ValidationBuilderContext(subjectType, subject, stopOnError)
        val builder = ValidationBuilder(context).apply(action)
        return builder.toValidationResult()
    }
}
