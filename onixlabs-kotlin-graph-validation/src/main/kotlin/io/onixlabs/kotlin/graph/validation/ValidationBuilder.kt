package io.onixlabs.kotlin.graph.validation

import io.onixlabs.kotlin.graph.GraphDslMarker
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class ValidationBuilder<T : Any> internal constructor(private val context: ValidationBuilderContext<T>) {

    private val builders: MutableMap<ValidationBuilderKey<*>, PropertyValidationBuilder<*>> = mutableMapOf()

    @GraphDslMarker
    fun allExcept(properties: Iterable<KProperty1<T, *>>, action: PropertyValidationBuilder<Any?>.() -> Unit) {
        for (property in context.type.memberProperties.subtract(properties.toSet())) {
            val key = ValidationBuilderKey.Scalar(property)
            val value = PropertyValidationBuilder<Any?>(property.getValueOrNull(), context.stopOnError).apply(action)

            builders[key] = value
        }
    }

    @GraphDslMarker
    fun allExcept(vararg properties: KProperty1<T, *>, action: PropertyValidationBuilder<Any?>.() -> Unit) {
        allExcept(properties.toList(), action)
    }

    @GraphDslMarker
    fun all(action: PropertyValidationBuilder<Any?>.() -> Unit) {
        allExcept(emptyList(), action)
    }

    @GraphDslMarker
    fun <R> scalar(properties: Iterable<KProperty1<T, R>>, action: PropertyValidationBuilder<R>.() -> Unit) {
        for (property in properties) {
            val key = ValidationBuilderKey.Scalar(property)
            val value = PropertyValidationBuilder(property.getValueOrNull(), context.stopOnError).apply(action)

            builders[key] = value
        }
    }

    @GraphDslMarker
    fun <R> scalar(vararg properties: KProperty1<T, R>, action: PropertyValidationBuilder<R>.() -> Unit) {
        scalar(properties.toList(), action)
    }

    @GraphDslMarker
    fun <R> iterable(properties: Iterable<KProperty1<T, Iterable<R>?>>, action: PropertyValidationBuilder<R>.() -> Unit) {
        for (property in properties) {
            for ((index, element) in property.getValueOrNull().withIndex()) {
                val key = ValidationBuilderKey.Vector(property, index)
                val value = PropertyValidationBuilder(element, context.stopOnError).apply(action)

                builders[key] = value
            }
        }
    }

    @GraphDslMarker
    fun <R> iterable(vararg properties: KProperty1<T, Iterable<R>?>, action: PropertyValidationBuilder<R>.() -> Unit) {
        iterable(properties.toList(), action)
    }

    internal fun toValidationResult(): ValidationResult {
        return ValidationResult(context.type, builders)
    }

    private fun <R> KProperty1<T, R>.getValueOrNull(): R? {
        return context.subject?.let(getter)
    }

    private fun <R> KProperty1<T, Iterable<R>?>.getValueOrNull(): List<R?> {
        return context.subject?.let(getter)?.toList() ?: listOf(null)
    }
}
