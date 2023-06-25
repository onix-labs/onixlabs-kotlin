package io.onixlabs.kotlin.graph.validation

import io.onixlabs.kotlin.core.reflection.getArgumentType
import io.onixlabs.kotlin.core.reflection.toTypedClass
import kotlin.reflect.KClass

abstract class Validator<T : Any>(private val action: ValidationBuilder<T>.() -> Unit) {

    open val subjectType: KClass<T> by lazy { javaClass.getArgumentType(0).toTypedClass<T>().kotlin }

    fun validate(): ValidationResult {
        val context = ValidationBuilderContext(subjectType, null, false)
        val builder = ValidationBuilder(context).apply(action)
        return builder.toValidationResult()
    }

    fun validate(subject: T, stopOnError: Boolean = false): ValidationResult {
        val context = ValidationBuilderContext(subjectType, subject, stopOnError)
        val builder = ValidationBuilder(context).apply(action)
        return builder.toValidationResult()
    }
}
