package io.onixlabs.kotlin.core

fun <T : Comparable<T>> T.isGreaterThan(other: T): Boolean {
    return this > other
}

fun <T : Comparable<T>> T.isGreaterThanOrEqualTo(other: T): Boolean {
    return this >= other
}

fun <T : Comparable<T>> T.isLessThan(other: T): Boolean {
    return this < other
}

fun <T : Comparable<T>> T.isLessThanOrEqualTo(other: T): Boolean {
    return this <= other
}

fun <T : Comparable<T>> T.isWithinRange(range: ClosedRange<T>): Boolean {
    return this in range
}

fun <T : Comparable<T>> T.isWithinRange(minimum: T, maximum: T): Boolean {
    return isWithinRange(minimum..maximum)
}
