package io.onixlabs.kotlin.core.reflection

import io.onixlabs.kotlin.core.UNCHECKED_CAST
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

/**
 * Obtains a [Class] from the current [Type].
 *
 * @receiver The current [Type] from which to obtain a [Class].
 * @return Returns a [Class] from the current [Type].
 */
fun Type.toClass(): Class<*> = when (this) {
    is ParameterizedType -> rawType.toClass()
    is WildcardType -> this.upperBounds[0].toClass()
    is Class<*> -> this
    else -> Class.forName(typeName)
}

/**
 * Obtains a typed [Class] of type [T] from the current [Type].
 *
 * @receiver The current [Type] from which to obtain a [Class].
 * @param T The underlying type of the class
 * @return Returns typed [Class] of type [T] from the current [Type].
 */
@Suppress(UNCHECKED_CAST)
fun <T> Type.toTypedClass(): Class<T> {
    return toClass() as Class<T>
}
