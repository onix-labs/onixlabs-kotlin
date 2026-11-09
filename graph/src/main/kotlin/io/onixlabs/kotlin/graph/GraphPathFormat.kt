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
import io.onixlabs.kotlin.graph.GraphPathSegment.Index
import io.onixlabs.kotlin.graph.GraphPathSegment.Property
import io.onixlabs.kotlin.graph.GraphPathSegment.Root

/**
 * Formats a [GraphPath] as a string.
 */
public fun interface GraphPathFormatter {

    /**
     * Formats the specified path as a string.
     *
     * @param path The path to format.
     * @return Returns the specified path, formatted as a string.
     */
    public fun format(path: GraphPath): String
}

/**
 * Represents a built-in [GraphPathFormatter] whose property names can be transformed, for example to match the names
 * used by a serialization format.
 *
 * @property names The transformation applied to property names.
 */
public abstract class GraphPathFormat internal constructor(internal val names: (String) -> String) : GraphPathFormatter {

    public companion object {

        /**
         * Gets the Kotlin-style format, which includes the root type; for example, `Customer.orders[2].total`,
         * `Customer.attributes["colour"]` for a map value, and `Customer.attributes.keys["colour"]` for a map key.
         * Wildcards are formatted as `[*]`.
         */
        public val Kotlin: GraphPathFormat = KotlinGraphPathFormat { it }

        /**
         * Gets the JSON Pointer format (RFC 6901), which is relative to the document root; for example, `/orders/2/total`.
         * JSON Pointer cannot address map keys, so a key is formatted like its entry. Wildcards are formatted as `*`,
         * which is not part of RFC 6901.
         */
        public val JsonPointer: GraphPathFormat = JsonPointerGraphPathFormat { it }

        /**
         * Gets the JSONPath format (RFC 9535), which is relative to the document root; for example, `$.orders[2].total`.
         * JSONPath cannot address map keys, so a key is formatted like its entry. Wildcards are formatted as `[*]`.
         */
        public val JsonPath: GraphPathFormat = JsonPathGraphPathFormat { it }
    }

    /**
     * Creates a copy of the current format which transforms property names with the specified transformation, after
     * any existing transformation; for example, `GraphPathFormat.JsonPointer.withNames { it.toLowerSnakeCase() }`.
     *
     * @param transform The transformation to apply to property names.
     * @return Returns a copy of the current format which transforms property names with the specified transformation.
     */
    public abstract fun withNames(transform: (String) -> String): GraphPathFormat
}

/**
 * Represents the Kotlin-style [GraphPathFormat].
 */
private class KotlinGraphPathFormat(names: (String) -> String) : GraphPathFormat(names) {

    override fun withNames(transform: (String) -> String): GraphPathFormat = KotlinGraphPathFormat { transform(names(it)) }

    override fun format(path: GraphPath): String = buildString {
        for (segment in path.segments) {
            when (segment) {
                is Root -> append(segment.type.simpleName ?: segment.type.toString())
                is Property -> append('.').append(names(segment.name))
                is Index -> append('[').append(segment.index).append(']')
                AnyIndex, AnyEntry -> append("[*]")
                is Entry -> append('[').append(literal(segment.key)).append(']')
                is EntryKey -> append(".keys[").append(literal(segment.key)).append(']')
                AnyEntryKey -> append(".keys[*]")
            }
        }
    }

    private fun literal(key: Any?): String = when (key) {
        is String -> "\"" + key.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        is Char -> "'$key'"
        else -> key.toString()
    }
}

/**
 * Represents the JSON Pointer (RFC 6901) [GraphPathFormat].
 */
private class JsonPointerGraphPathFormat(names: (String) -> String) : GraphPathFormat(names) {

    override fun withNames(transform: (String) -> String): GraphPathFormat = JsonPointerGraphPathFormat { transform(names(it)) }

    override fun format(path: GraphPath): String = buildString {
        for (segment in path.segments) {
            when (segment) {
                is Root -> Unit
                is Property -> append('/').append(escape(names(segment.name)))
                is Index -> append('/').append(segment.index)
                is Entry -> append('/').append(escape(segment.key.toString()))
                is EntryKey -> append('/').append(escape(segment.key.toString()))
                AnyIndex, AnyEntry, AnyEntryKey -> append("/*")
            }
        }
    }

    private fun escape(token: String): String = token.replace("~", "~0").replace("/", "~1")
}

/**
 * Represents the JSONPath (RFC 9535) [GraphPathFormat].
 */
private class JsonPathGraphPathFormat(names: (String) -> String) : GraphPathFormat(names) {

    private companion object {
        val SHORTHAND_NAME = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
    }

    override fun withNames(transform: (String) -> String): GraphPathFormat = JsonPathGraphPathFormat { transform(names(it)) }

    override fun format(path: GraphPath): String = buildString {
        for (segment in path.segments) {
            when (segment) {
                is Root -> append('$')
                is Property -> names(segment.name).let { if (SHORTHAND_NAME.matches(it)) append('.').append(it) else append(quoted(it)) }
                is Index -> append('[').append(segment.index).append(']')
                is Entry -> append(quoted(segment.key.toString()))
                is EntryKey -> append(quoted(segment.key.toString()))
                AnyIndex, AnyEntry, AnyEntryKey -> append("[*]")
            }
        }
    }

    private fun quoted(name: String): String = "['" + name.replace("\\", "\\\\").replace("'", "\\'") + "']"
}
