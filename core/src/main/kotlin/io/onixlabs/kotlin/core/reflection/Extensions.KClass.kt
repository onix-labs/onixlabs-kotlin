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

import io.onixlabs.kotlin.core.isNull
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Gets the kotlin class for the current object.
 *
 * @receiver The current object from which to obtain the kotlin class.
 * @return Returns the kotlin class for the current object.
 */
public val <T : Any> T.kotlinClass: KClass<T>
    get() = javaClass.kotlin

/**
 * Gets the primary constructor for the current [KClass].
 *
 * @receiver The current [KClass] from which to obtain the primary constructor.
 * @return Returns the primary constructor for the current [KClass].
 * @throws IllegalArgumentException if the class has no primary constructor.
 */
public fun <T : Any> KClass<T>.getPrimaryConstructor(): KFunction<T> {
    return requireNotNull(primaryConstructor) { "Kotlin class '$qualifiedName' has no primary constructor." }
}

/**
 * Creates an instance of the current [KClass].
 *
 * @receiver The current [KClass] for which to create a new instance.
 * @return Returns an instance of the current [KClass].
 * @throws IllegalArgumentException if the primary constructor of the class to be instantiated cannot be obtained.
 */
public fun <T : Any> KClass<T>.createInstance(): T {
    return getPrimaryConstructor().call()
}

/**
 * Creates an instance of the current [KClass].
 *
 * @receiver The current [KClass] for which to create a new instance.
 * @param args The arguments from which to construct a new instance.
 * @return Returns an instance of the current [KClass].
 * @throws IllegalArgumentException if the primary constructor of the class to be instantiated cannot be obtained.
 */
public fun <T : Any> KClass<T>.createInstance(args: Array<Any?>): T {
    return getPrimaryConstructor().call(*args)
}

/**
 * Creates an instance of the current [KClass].
 *
 * @receiver The current [KClass] for which to create a new instance.
 * @param args The arguments from which to construct a new instance.
 * @return Returns an instance of the current [KClass].
 * @throws IllegalArgumentException if the primary constructor of the class to be instantiated cannot be obtained.
 */
public fun <T : Any> KClass<T>.createInstance(args: Map<String, Any?>): T {
    val arguments = getPrimaryConstructor().parameters
        .associateBy({ it }, { args[it.name] })
        .filterNot { it.key.isOptional && it.value.isNull() }

    return getPrimaryConstructor().callBy(arguments)
}

/**
 * Gets the properties of the current [KClass], including inherited properties.
 *
 * @receiver The current [KClass] from which to obtain the properties.
 * @return Returns the properties of the current [KClass].
 */
public fun <T : Any> KClass<T>.getProperties(): List<KProperty1<T, *>> {
    return memberProperties.toList()
}

/**
 * Gets the property with the specified name from the current [KClass], including inherited properties.
 *
 * @receiver The current [KClass] from which to obtain the property.
 * @param name The name of the property.
 * @return Returns the property with the specified name, or null if the class has no such property.
 */
public fun <T : Any> KClass<T>.getPropertyOrNull(name: String?): KProperty1<T, *>? {
    return memberProperties.singleOrNull { it.name == name }
}

/**
 * Gets the property with the specified name from the current [KClass], including inherited properties.
 *
 * @receiver The current [KClass] from which to obtain the property.
 * @param name The name of the property.
 * @return Returns the property with the specified name.
 * @throws IllegalArgumentException if the class has no property with the specified name.
 */
public fun <T : Any> KClass<T>.getProperty(name: String?): KProperty1<T, *> {
    return requireNotNull(getPropertyOrNull(name)) { "Kotlin class '$qualifiedName' has no property named '$name'." }
}

/**
 * Gets the mutable properties of the current [KClass], including inherited properties.
 *
 * @receiver The current [KClass] from which to obtain the mutable properties.
 * @return Returns the mutable properties of the current [KClass].
 */
public fun <T : Any> KClass<T>.getMutableProperties(): List<KMutableProperty1<T, *>> {
    return memberProperties.filterIsInstance<KMutableProperty1<T, *>>()
}

/**
 * Gets the mutable property with the specified name from the current [KClass], including inherited properties.
 *
 * @receiver The current [KClass] from which to obtain the mutable property.
 * @param name The name of the mutable property.
 * @return Returns the mutable property with the specified name, or null if the class has no such mutable property.
 */
public fun <T : Any> KClass<T>.getMutablePropertyOrNull(name: String?): KMutableProperty1<T, *>? {
    return getMutableProperties().singleOrNull { it.name == name }
}

/**
 * Gets the mutable property with the specified name from the current [KClass], including inherited properties.
 *
 * @receiver The current [KClass] from which to obtain the mutable property.
 * @param name The name of the mutable property.
 * @return Returns the mutable property with the specified name.
 * @throws IllegalArgumentException if the class has no mutable property with the specified name.
 */
public fun <T : Any> KClass<T>.getMutableProperty(name: String?): KMutableProperty1<T, *> {
    return requireNotNull(getMutablePropertyOrNull(name)) { "Kotlin class '$qualifiedName' has no mutable property named '$name'." }
}
