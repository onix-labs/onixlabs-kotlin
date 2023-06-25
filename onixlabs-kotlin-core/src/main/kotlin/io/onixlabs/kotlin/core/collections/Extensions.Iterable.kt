/**
 * Copyright 2023 ONIXLabs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.onixlabs.kotlin.core.collections

import io.onixlabs.kotlin.core.ONE
import io.onixlabs.kotlin.core.ZERO

/**
 * Determines whether all elements of the current [Iterable] are equal by a specified property.
 *
 * @receiver The [Iterable] on which to perform the operation.
 * @param T The underlying type of the [Iterable].
 * @param P The underlying type of the property to select.
 * @param selector The selector function that will be used to map the desired property from each element.
 * @return Returns true if all selected element properties are equal; otherwise, false.
 */
fun <T, P> Iterable<T>.allEqualBy(selector: (T) -> P): Boolean {
    val elements = map(selector).toSet()
    return elements.isNotEmpty() && elements.isSingle()
}

/**
 * Determines whether any elements of the current [Iterable] are equal by a specified property.
 *
 * @receiver The [Iterable] on which to perform the operation.
 * @param T The underlying type of the [Iterable].
 * @param P The underlying type of the property to select.
 * @param selector The selector function that will be used to map the desired property from each element.
 * @return Returns true if any selected element properties are equal; otherwise, false.
 */
fun <T, P> Iterable<T>.anyEqualBy(selector: (T) -> P): Boolean {
    val elements = map(selector).toList()
    val distinctElements = elements.toSet()
    return distinctElements.isNotEmpty() && distinctElements.size <= elements.count()
}

/**
 * Determines whether all the specified elements exist in the current [Iterable].
 *
 * @receiver The [Iterable] on which to perform the operation.
 * @param T The underlying type of the [Iterable].
 * @param elements The elements to check exist in the current [Iterable].
 * @return Returns true if all the specified elements exist in the current [Iterable]; otherwise, false.
 */
fun <T> Iterable<T>.containsAll(elements: Iterable<T>): Boolean {
    return all { it in elements }
}

/**
 * Determines whether all of the specified elements exist in the current [Iterable].
 *
 * @receiver The [Iterable] on which to perform the operation.
 * @param T The underlying type of the [Iterable].
 * @param elements The elements to check exist in the current [Iterable].
 * @return Returns true if all of the specified elements exist in the current [Iterable]; otherwise, false.
 */
fun <T> Iterable<T>.containsAll(vararg elements: T): Boolean {
    return containsAll(elements.toList())
}

/**
 * Determines whether any of the specified elements exist in the current [Iterable].
 *
 * @receiver The [Iterable] on which to perform the operation.
 * @param T The underlying type of the [Iterable].
 * @param elements The elements to check exist in the current [Iterable].
 * @return Returns true if any of the specified elements exist in the current [Iterable]; otherwise, false.
 */
fun <T> Iterable<T>.containsAny(elements: Iterable<T>): Boolean {
    return any { it in elements }
}

/**
 * Determines whether any of the specified elements exist in the current [Iterable].
 *
 * @receiver The [Iterable] on which to perform the operation.
 * @param T The underlying type of the [Iterable].
 * @param elements The elements to check exist in the current [Iterable].
 * @return Returns true if any of the specified elements exist in the current [Iterable]; otherwise, false.
 */
fun <T> Iterable<T>.containsAny(vararg elements: T): Boolean {
    return containsAny(elements.toList())
}

/**
 * Counts the number of elements in the current [Iterable] that satisfy the specified predicate.
 *
 * @receiver The [Iterable] on which to perform the operation.
 * @param T The underlying type of the [Iterable].
 * @param predicate The function to test each element for a condition.
 * @return Returns the number of elements in the current [Iterable] that satisfy the specified predicate.
 */
fun <T> Iterable<T>.countNot(predicate: (T) -> Boolean): Int {
    return count { !predicate(it) }
}

/**
 * Determines whether the number of elements that satisfy the specified predicate match the specified count.
 *
 * @receiver The [Iterable] of elements to count.
 * @param T The underlying type of the iterable elements.
 * @param count The expected number of elements.
 * @param predicate The predicate against which to evaluate the elements.
 * @return Returns true if the elements that satisfy the specified predicate match the expected count; otherwise, false.
 */
fun <T> Iterable<T>.hasCount(count: Int, predicate: (T) -> Boolean = { true }): Boolean {
    return count(predicate) == count
}

/**
 * Determines whether the count of the current [Iterable] is even.
 *
 * @receiver The [Iterable] on which to perform the operation.
 * @param T The underlying type of the [Iterable].
 * @return Returns true if the count of the current [Iterable] is even; otherwise; false.
 */
fun <T> Iterable<T>.isCountEven(): Boolean {
    val count = if (this is Collection<T>) size else count()
    return count % 2 == Int.ZERO
}

/**
 * Determines whether the count of the current [Iterable] is odd.
 *
 * @receiver The [Iterable] on which to perform the operation.
 * @param T The underlying type of the [Iterable].
 * @return Returns true if the count of the current [Iterable] is odd; otherwise; false.
 */
fun <T> Iterable<T>.isCountOdd(): Boolean {
    return !isCountEven()
}

/**
 * Determines whether the current [Iterable] contains distinct elements.
 *
 * @receiver The current [Iterable] of elements to determine is distinct.
 * @param T The underlying type of the iterable elements.
 * @return Returns true if the current [Iterable] is distinct; otherwise, false.
 */
fun <T> Iterable<T>.isDistinct(): Boolean {
    return distinct().count() == count()
}

/**
 * Determines whether the current [Iterable] is empty.
 *
 * @receiver The current [Iterable] of elements to determine is empty.
 * @param T The underlying type of the iterable elements.
 * @return Returns true if the current [Iterable] is empty; otherwise, false.
 */
fun <T> Iterable<T>.isEmpty(): Boolean {
    return if (this is Collection<T>) this.size == Int.ZERO else count() == Int.ZERO
}

/**
 * Determines whether the current [Iterable] is not empty.
 *
 * @receiver The current [Iterable] of elements to determine is not empty.
 * @param T The underlying type of the iterable elements.
 * @return Returns true if the current [Iterable] is not empty; otherwise, false.
 */
fun <T> Iterable<T>.isNotEmpty(): Boolean {
    return !isEmpty()
}

/**
 * Determines whether the current [Iterable] contains a single element.
 *
 * @receiver The current [Iterable] of elements to determine contains a single element.
 * @param T The underlying type of the iterable elements.
 * @return Returns true if the current [Iterable] contains a single element; otherwise, false.
 */
fun <T> Iterable<T>.isSingle(): Boolean {
    return count() == Int.ONE
}

/**
 * Performs an ordered check to determine whether the content in two iterable collections is identical.
 *
 * @receiver The current [Iterable] of elements to compare.
 * @param other The other iterable collection to compare.
 * @return Returns true if the content in each collection is equal and ordered; otherwise, false.
 */
fun <T> Iterable<T>.orderedContentEquals(other: Iterable<T>): Boolean {
    return this == other
}

/**
 * Performs an unordered check to determine whether the content in two iterable collections is identical.
 *
 * @receiver The current [Iterable] of elements to compare.
 * @param other The other iterable collection to compare.
 * @return Returns true if the content in each collection is equal regardless of order; otherwise, false.
 */
fun <T> Iterable<T>.unorderedContentEquals(other: Iterable<T>): Boolean {
    if (count() != other.count()) return false;

    val thisAsSet = toSet()
    val otherAsSet = other.toSet()
    return ((thisAsSet subtract otherAsSet) union (otherAsSet subtract thisAsSet)).isEmpty()
}
