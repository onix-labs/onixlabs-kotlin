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

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Obtains the argument types from a generic superclass.
 *
 * @return Returns a list of generic argument types.
 * @throws IllegalArgumentException if the superclass is not a [ParameterizedType].
 */
public fun Class<*>.getArgumentTypes(): List<Type> = requireNotNull((genericSuperclass as? ParameterizedType)?.actualTypeArguments?.toList()) {
    "Cannot obtain generic argument types from a non-parameterized type."
}

/**
 * Gets an argument type from a generic superclass.
 *
 * @param index The index of the argument to get.
 * @return Returns a list of generic argument types.
 * @throws IllegalArgumentException if the superclass is not a [ParameterizedType].
 */
public fun Class<*>.getArgumentType(index: Int): Type {
    return getArgumentTypes()[index]
}
