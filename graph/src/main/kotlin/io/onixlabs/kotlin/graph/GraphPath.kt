/*
 * Copyright 2020-2026 ONIXLabs
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
package io.onixlabs.kotlin.graph

import io.onixlabs.kotlin.graph.GraphPathSegment.AnyEntry
import io.onixlabs.kotlin.graph.GraphPathSegment.AnyEntryKey
import io.onixlabs.kotlin.graph.GraphPathSegment.AnyIndex
import io.onixlabs.kotlin.graph.GraphPathSegment.Entry
import io.onixlabs.kotlin.graph.GraphPathSegment.EntryKey
import io.onixlabs.kotlin.graph.GraphPathSegment.Function
import io.onixlabs.kotlin.graph.GraphPathSegment.Index
import io.onixlabs.kotlin.graph.GraphPathSegment.Property
import io.onixlabs.kotlin.graph.GraphPathSegment.Root
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Represents a segment of a [GraphPath].
 */
public sealed interface GraphPathSegment {

    /**
     * Represents the root of a path: the type at which the path starts.
     *
     * @property type The type at which the path starts.
     */
    public data class Root(public val type: KClass<*>) : GraphPathSegment

    /**
     * Represents a property.
     *
     * @property name The name of the property.
     */
    public data class Property(public val name: String) : GraphPathSegment

    /**
     * Represents the result of calling a member function that takes no arguments.
     *
     * @property name The name of the function.
     */
    public data class Function(public val name: String) : GraphPathSegment

    /**
     * Represents an element of a collection or array, by index.
     *
     * @property index The zero-based index of the element.
     */
    public data class Index(public val index: Int) : GraphPathSegment

    /**
     * Represents every element of a collection or array, as used when describing rules without a subject.
     */
    public data object AnyIndex : GraphPathSegment

    /**
     * Represents the value of a map entry, by key.
     *
     * @property key The key of the entry.
     */
    public data class Entry(public val key: Any?) : GraphPathSegment

    /**
     * Represents the value of every map entry, as used when describing rules without a subject.
     */
    public data object AnyEntry : GraphPathSegment

    /**
     * Represents the key of a map entry, as used by rules about keys themselves.
     *
     * @property key The key of the entry.
     */
    public data class EntryKey(public val key: Any?) : GraphPathSegment

    /**
     * Represents the key of every map entry, as used when describing rules without a subject.
     */
    public data object AnyEntryKey : GraphPathSegment
}

/**
 * Represents an immutable path to a member of an object graph, such as `Customer.orders[2].total`.
 *
 * A path always starts with a [Root] segment. Paths are comparable and hashable, so results can be grouped, sorted,
 * and matched against the members of a graph. Paths which contain wildcards ([AnyIndex], [AnyEntry], [AnyEntryKey])
 * address every element or entry, and are used when describing rules without a subject; see [matches].
 *
 * The string representation of a path uses [GraphPathFormat.Kotlin]; use [format] for other representations.
 *
 * @property segments The segments of the path, starting with a [Root] segment.
 */
public class GraphPath private constructor(public val segments: List<GraphPathSegment>) : Comparable<GraphPath> {

    init {
        require(segments.firstOrNull() is Root) { "A graph path must start with a root segment." }
        require(segments.drop(1).none { it is Root }) { "A graph path must contain exactly one root segment." }
    }

    public companion object {

        /**
         * Creates a [GraphPath] which starts at the specified type.
         *
         * @param type The type at which the path starts.
         * @return Returns a [GraphPath] which starts at the specified type.
         */
        public fun root(type: KClass<*>): GraphPath = GraphPath(listOf(Root(type)))

        /**
         * Creates a [GraphPath] which starts at the specified type.
         *
         * @param T The type at which the path starts.
         * @return Returns a [GraphPath] which starts at the specified type.
         */
        public inline fun <reified T : Any> root(): GraphPath = root(T::class)

        /**
         * Creates a [GraphPath] from a chain of properties, starting at the type that declares the first property;
         * for example, `GraphPath.of(Customer::address, Address::city)` is `Customer.address.city`.
         *
         * @param properties The chain of properties from which to create the path.
         * @return Returns a [GraphPath] from the specified chain of properties.
         * @throws IllegalArgumentException if no properties are specified, or the declaring type of the first property cannot be determined.
         */
        public fun of(vararg properties: KProperty1<*, *>): GraphPath {
            val first = requireNotNull(properties.firstOrNull()) { "At least one property is required to create a graph path." }
            val owner = first.parameters.firstOrNull()?.type?.classifier as? KClass<*>
            requireNotNull(owner) { "Cannot determine the type that declares property '${first.name}'." }
            return properties.fold(root(owner)) { path, property -> path.property(property) }
        }

        private val GraphPathSegment.order: Int
            get() = when (this) {
                is Root -> 0
                is Property -> 1
                is Function -> 1
                AnyIndex -> 2
                is Index -> 3
                AnyEntry -> 4
                is Entry -> 5
                AnyEntryKey -> 6
                is EntryKey -> 7
            }

        private fun compareSegments(left: GraphPathSegment, right: GraphPathSegment): Int {
            val order = left.order.compareTo(right.order)
            if (order != 0) return order
            return when (left) {
                is Root -> compareValues(left.type.qualifiedName, (right as Root).type.qualifiedName)
                is Property -> left.name.compareTo((right as? Property)?.name ?: (right as Function).name)
                is Function -> left.name.compareTo((right as? Function)?.name ?: (right as Property).name)
                is Index -> left.index.compareTo((right as Index).index)
                is Entry -> compareKeys(left.key, (right as Entry).key)
                is EntryKey -> compareKeys(left.key, (right as EntryKey).key)
                AnyIndex, AnyEntry, AnyEntryKey -> 0
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun compareKeys(left: Any?, right: Any?): Int = when {
            left == null || right == null -> compareValues(left == null, right == null).unaryMinus()
            left is Comparable<*> && left::class == right::class -> (left as Comparable<Any>).compareTo(right)
            else -> left.toString().compareTo(right.toString())
        }

        private fun matchesSegment(segment: GraphPathSegment, pattern: GraphPathSegment): Boolean = when (pattern) {
            AnyIndex -> segment is Index || segment == AnyIndex
            AnyEntry -> segment is Entry || segment == AnyEntry
            AnyEntryKey -> segment is EntryKey || segment == AnyEntryKey
            else -> segment == pattern
        }
    }

    /**
     * Gets the type at which the current path starts.
     */
    public val root: KClass<*> get() = (segments.first() as Root).type

    /**
     * Gets the parent of the current path, or null if the current path is a root path.
     */
    public val parent: GraphPath? get() = if (segments.size == 1) null else GraphPath(segments.dropLast(1))

    /**
     * Gets a value that determines whether the current path addresses a single member, which means it contains no wildcards.
     */
    public val isConcrete: Boolean get() = segments.none { it == AnyIndex || it == AnyEntry || it == AnyEntryKey }

    /**
     * Creates a path to the specified property of the member addressed by the current path.
     *
     * @param name The name of the property.
     * @return Returns a path to the specified property of the member addressed by the current path.
     */
    public fun property(name: String): GraphPath = append(Property(name))

    /**
     * Creates a path to the specified property of the member addressed by the current path.
     *
     * @param property The property.
     * @return Returns a path to the specified property of the member addressed by the current path.
     */
    public fun property(property: KProperty1<*, *>): GraphPath = property(property.name)

    /**
     * Creates a path to the result of calling the specified member function, which takes no arguments, on the member
     * addressed by the current path; for example, `Customer.fullName()`.
     *
     * @param name The name of the function.
     * @return Returns a path to the result of calling the specified member function.
     */
    public fun function(name: String): GraphPath = append(Function(name))

    /**
     * Creates a path to the element at the specified index of the collection addressed by the current path.
     *
     * @param index The zero-based index of the element.
     * @return Returns a path to the element at the specified index of the collection addressed by the current path.
     * @throws IllegalArgumentException if the index is negative.
     */
    public fun index(index: Int): GraphPath {
        require(index >= 0) { "An element index must not be negative: $index." }
        return append(Index(index))
    }

    /**
     * Creates a path to every element of the collection addressed by the current path.
     *
     * @return Returns a path to every element of the collection addressed by the current path.
     */
    public fun anyIndex(): GraphPath = append(AnyIndex)

    /**
     * Creates a path to the value of the entry with the specified key, in the map addressed by the current path.
     *
     * @param key The key of the entry.
     * @return Returns a path to the value of the entry with the specified key, in the map addressed by the current path.
     */
    public fun entry(key: Any?): GraphPath = append(Entry(key))

    /**
     * Creates a path to the value of every entry of the map addressed by the current path.
     *
     * @return Returns a path to the value of every entry of the map addressed by the current path.
     */
    public fun anyEntry(): GraphPath = append(AnyEntry)

    /**
     * Creates a path to the specified key, in the map addressed by the current path.
     *
     * @param key The key.
     * @return Returns a path to the specified key, in the map addressed by the current path.
     */
    public fun entryKey(key: Any?): GraphPath = append(EntryKey(key))

    /**
     * Creates a path to every key of the map addressed by the current path.
     *
     * @return Returns a path to every key of the map addressed by the current path.
     */
    public fun anyEntryKey(): GraphPath = append(AnyEntryKey)

    /**
     * Determines whether the current path starts with the specified path.
     *
     * @param other The path to compare with the start of the current path.
     * @return Returns true if the current path starts with the specified path; otherwise, false.
     */
    public fun startsWith(other: GraphPath): Boolean {
        return segments.size >= other.segments.size && segments.subList(0, other.segments.size) == other.segments
    }

    /**
     * Determines whether the current path matches the specified pattern, in which wildcards match any index, entry, or
     * key; for example, `Customer.orders[2].total` matches `Customer.orders[*].total`.
     *
     * @param pattern The pattern to match.
     * @return Returns true if the current path matches the specified pattern; otherwise, false.
     */
    public fun matches(pattern: GraphPath): Boolean {
        return segments.size == pattern.segments.size && segments.zip(pattern.segments).all { (segment, other) -> matchesSegment(segment, other) }
    }

    /**
     * Formats the current path using the specified formatter.
     *
     * @param formatter The formatter with which to format the current path.
     * @return Returns the current path, formatted using the specified formatter.
     */
    public fun format(formatter: GraphPathFormatter): String = formatter.format(this)

    /**
     * Compares the current path with the specified path, segment by segment.
     *
     * @param other The path to compare with the current path.
     * @return Returns a negative number, zero, or a positive number as the current path is less than, equal to, or greater than the specified path.
     */
    override fun compareTo(other: GraphPath): Int {
        for ((left, right) in segments.zip(other.segments)) {
            val result = compareSegments(left, right)
            if (result != 0) return result
        }
        return segments.size.compareTo(other.segments.size)
    }

    /**
     * Determines whether the specified object is equal to the current object.
     *
     * @param other The object to compare with the current object.
     * @return Returns true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean = other is GraphPath && other.segments == segments

    /**
     * Serves as the default hash function.
     *
     * @return Returns a hash code for the current object.
     */
    override fun hashCode(): Int = segments.hashCode()

    /**
     * Returns the current path formatted using [GraphPathFormat.Kotlin]; for example, `Customer.orders[2].total`.
     *
     * @return Returns the current path formatted using [GraphPathFormat.Kotlin].
     */
    override fun toString(): String = format(GraphPathFormat.Kotlin)

    private fun append(segment: GraphPathSegment): GraphPath = GraphPath(segments + segment)
}
