package io.onixlabs.kotlin.core.reflection

import io.onixlabs.kotlin.core.isNull
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

/**
 * Gets the kotlin class for the current object.
 *
 * @receiver The current object from which to obtain the kotlin class.
 * @return Returns the kotlin class for the current object.
 */
val <T : Any> T.kotlinClass: KClass<T>
    get() = javaClass.kotlin

/**
 * Gets the primary constructor for the current [KClass].
 *
 * @receiver The current [KClass] from which to obtain the primary constructor.
 * @return Returns the primary constructor for the current [KClass].
 * @throws IllegalArgumentException if the primary constructor cannot be obtained.
 */
private val <T : Any> KClass<T>.primaryConstructorNotNull: KFunction<T>
    get() = requireNotNull(primaryConstructor) { "Kotlin class '$qualifiedName' has no primary constructor." }

/**
 * Creates an instance of the current [KClass].
 *
 * @receiver The current [KClass] for which to create a new instance.
 * @return Returns an instance of the current [KClass].
 * @throws IllegalArgumentException if the primary constructor of the class to be instantiated cannot be obtained.
 */
fun <T : Any> KClass<T>.createInstance(): T {
    return primaryConstructorNotNull.call()
}

/**
 * Creates an instance of the current [KClass].
 *
 * @receiver The current [KClass] for which to create a new instance.
 * @param args The arguments from which to construct a new instance.
 * @return Returns an instance of the current [KClass].
 * @throws IllegalArgumentException if the primary constructor of the class to be instantiated cannot be obtained.
 */
fun <T : Any> KClass<T>.createInstance(args: Array<Any?>): T {
    return primaryConstructorNotNull.call(*args)
}

/**
 * Creates an instance of the current [KClass].
 *
 * @receiver The current [KClass] for which to create a new instance.
 * @param args The arguments from which to construct a new instance.
 * @return Returns an instance of the current [KClass].
 * @throws IllegalArgumentException if the primary constructor of the class to be instantiated cannot be obtained.
 */
fun <T : Any> KClass<T>.createInstance(args: Map<String, Any?>): T {
    val arguments = primaryConstructorNotNull.parameters
        .associateBy({ it }, { args[it.name] })
        .filterNot { it.key.isOptional && it.value.isNull() }

    return primaryConstructorNotNull.callBy(arguments)
}
