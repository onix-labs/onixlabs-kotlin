package io.onixlabs.kotlin.graph.validation

import kotlin.reflect.KClass

internal data class ValidationBuilderContext<T : Any>(val type: KClass<T>, val subject: T?, val stopOnError: Boolean)
