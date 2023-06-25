package io.onixlabs.kotlin.graph.validation

import kotlin.reflect.KClass

class ValidationResult internal constructor(
    private val type: KClass<*>,
    private val builders: Map<ValidationBuilderKey<*>, PropertyValidationBuilder<*>>
) {

    override fun toString(): String = buildString {
        for (builder in builders) {
            for (assertion in builder.value.assertions) {
                appendLine(builder.key.toString(type) + assertion.message)
            }
        }
    }
}
