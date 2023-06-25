package io.onixlabs.kotlin.core.reflection

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Obtains the argument types from a generic superclass.
 *
 * @return Returns a list of generic argument types.
 * @throws IllegalArgumentException if the superclass is not a [ParameterizedType].
 */
fun Class<*>.getArgumentTypes(): List<Type> = requireNotNull((genericSuperclass as? ParameterizedType)?.actualTypeArguments?.toList()) {
    "Cannot obtain generic argument types from a non-parameterized type."
}

/**
 * Gets an argument type from a generic superclass.
 *
 * @param index The index of the argument to get.
 * @return Returns a list of generic argument types.
 * @throws IllegalArgumentException if the superclass is not a [ParameterizedType].
 */
fun Class<*>.getArgumentType(index: Int): Type {
    return getArgumentTypes()[index]
}
