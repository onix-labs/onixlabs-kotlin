package io.onixlabs.kotlin.graph.validation

internal class StopOnErrorException(val propertyValidationBuilder: PropertyValidationBuilder<*>) : RuntimeException()
