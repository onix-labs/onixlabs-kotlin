package io.onixlabs.kotlin.graph.validation

import io.onixlabs.kotlin.graph.GraphConfigurationBuilder
import io.onixlabs.kotlin.graph.GraphDslMarker

@GraphDslMarker
fun <T : Any> GraphConfigurationBuilder<T>.validator(validator: Validator<T>): Validator<T> {
    return validator
}

@GraphDslMarker
fun <T : Any> GraphConfigurationBuilder<T>.validator(action: ValidationBuilder<T>.() -> Unit): Validator<T> {
    return object : Validator<T>(action) {}
}
