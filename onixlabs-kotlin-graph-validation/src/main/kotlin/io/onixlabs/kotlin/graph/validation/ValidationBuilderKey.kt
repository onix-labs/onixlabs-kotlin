package io.onixlabs.kotlin.graph.validation

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal sealed interface ValidationBuilderKey<T> {
    val property: KProperty1<*, T>
    fun toString(type: KClass<*>): String

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
