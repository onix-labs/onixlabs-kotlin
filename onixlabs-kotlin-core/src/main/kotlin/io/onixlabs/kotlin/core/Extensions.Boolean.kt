package io.onixlabs.kotlin.core

val Boolean.Companion.TRUE_STRING: String get() = "True"

val Boolean.Companion.FALSE_STRING: String get() = "False"

fun Boolean.isTrue(): Boolean {
    return this
}

fun Boolean.isFalse(): Boolean {
    return !this
}
