package io.onixlabs.kotlin.core

fun <T> T.isEqualTo(value: T): Boolean {
    return this == value
}

fun <T> T.isEqualToAny(values: Iterable<T>): Boolean {
    return this in values
}

fun <T> T.isEqualToAny(vararg values: T): Boolean {
    return isEqualToAny(values.toList())
}

fun <T> T?.isNull(): Boolean {
    return this == null
}
