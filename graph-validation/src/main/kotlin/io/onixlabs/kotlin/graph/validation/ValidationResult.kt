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
package io.onixlabs.kotlin.graph.validation

import io.onixlabs.kotlin.graph.GraphPath
import io.onixlabs.kotlin.graph.GraphPathFormat
import io.onixlabs.kotlin.graph.GraphPathFormatter
import io.onixlabs.kotlin.graph.GraphPathSegment

/**
 * Specifies how validation proceeds.
 */
public enum class ValidationMode {

    /**
     * Every rule is evaluated, and every failure is collected.
     */
    ALL_FAILURES,

    /**
     * Validation stops at the first failure, and the result contains only that failure.
     */
    FIRST_FAILURE,

    /**
     * Every rule is assumed to fail, so the result describes everything the validator checks. Every `whenever`
     * branch is included, labelled with its condition.
     */
    TEST,
}

/**
 * Represents a failed validation rule.
 *
 * @property path The concrete path of the member that failed, or a wildcard path when describing rules without a subject.
 * @property rule The namespaced identifier of the rule, such as `string.notBlank`.
 * @property parameters The parameters of the rule, such as `minimum` and `maximum`.
 * @property message The message of the rule.
 * @property conditions The descriptions of the `whenever` conditions under which the rule applies, outermost first.
 */
public data class ValidationFailure(
    public val path: GraphPath,
    public val rule: String,
    public val parameters: Map<String, Any?>,
    public val message: String,
    public val conditions: List<String> = emptyList(),
) {

    /**
     * Returns a string that represents the current object; for example, `Customer.name must not be blank.`.
     *
     * @return Returns a string that represents the current object.
     */
    override fun toString(): String {
        val suffix = if (conditions.isEmpty()) "" else " (${conditions.joinToString(", ")})"
        return "$path $message$suffix"
    }
}

/**
 * Represents a node of a validation result tree: a member of the validated graph, the failures of the member itself,
 * and the nodes of its descendants that contain failures.
 *
 * @property path The path of the member.
 * @property failures The failures of the member itself.
 * @property children The nodes of the member's descendants that contain failures, in the order in which they were found.
 */
public class ValidationResultNode internal constructor(
    public val path: GraphPath,
    public val failures: List<ValidationFailure>,
    public val children: List<ValidationResultNode>,
) {

    /**
     * Gets every failure of the current node and its descendants, depth first.
     */
    public val allFailures: List<ValidationFailure> get() = failures + children.flatMap { it.allFailures }

    /**
     * Returns a string that represents the current object.
     *
     * @return Returns a string that represents the current object.
     */
    override fun toString(): String = "$path (${allFailures.size} failures)"
}

/**
 * Represents the result of validating an object graph.
 *
 * The result is a tree of [ValidationResultNode]s rooted at the subject, which mirrors the validated graph. [failures]
 * is a flat, depth-first view of the tree, and [at], [under], and [matching] query it by path. The result renders
 * as JSON in two shapes: [toTreeJson], which mirrors the subject, and [toFlatJson], a list of failures.
 *
 * @property mode The mode in which validation ran.
 * @property root The root node of the result tree.
 */
public class ValidationResult internal constructor(public val mode: ValidationMode, public val root: ValidationResultNode) {

    internal companion object {

        /**
         * Creates a [ValidationResult] from the specified failures.
         *
         * @param mode The mode in which validation ran.
         * @param rootPath The path of the subject.
         * @param failures The failures, in the order in which they were found: members in declaration order, then nested graphs.
         * @return Returns a [ValidationResult] from the specified failures.
         */
        fun of(mode: ValidationMode, rootPath: GraphPath, failures: List<ValidationFailure>): ValidationResult {
            return ValidationResult(mode, node(rootPath, failures))
        }

        private fun node(path: GraphPath, failures: List<ValidationFailure>): ValidationResultNode {
            val depth = path.segments.size
            val own = failures.filter { it.path.segments.size == depth }
            val children = failures.filter { it.path.segments.size > depth }
                .groupBy { it.path.segments[depth] }
                .map { (segment, nested) -> node(path.append(segment), nested) }
            return ValidationResultNode(path, own, children)
        }

        private fun GraphPath.append(segment: GraphPathSegment): GraphPath = when (segment) {
            is GraphPathSegment.Root -> error("A root segment cannot be appended to a path.")
            is GraphPathSegment.Property -> property(segment.name)
            is GraphPathSegment.Index -> index(segment.index)
            GraphPathSegment.AnyIndex -> anyIndex()
            is GraphPathSegment.Entry -> entry(segment.key)
            GraphPathSegment.AnyEntry -> anyEntry()
            is GraphPathSegment.EntryKey -> entryKey(segment.key)
            GraphPathSegment.AnyEntryKey -> anyEntryKey()
        }
    }

    /**
     * Gets every failure, depth first through the result tree: each member's own failures in rule declaration order,
     * then its descendants. Members appear in the order in which validation reached them, which follows declaration order.
     */
    public val failures: List<ValidationFailure> by lazy { root.allFailures }

    /**
     * Gets a value that determines whether validation found no failures.
     */
    public val isValid: Boolean get() = failures.isEmpty()

    /**
     * Gets the failures of exactly the specified member.
     *
     * @param path The path of the member.
     * @return Returns the failures of exactly the specified member.
     */
    public fun at(path: GraphPath): List<ValidationFailure> = failures.filter { it.path == path }

    /**
     * Gets the failures of the specified member and its descendants.
     *
     * @param path The path of the member.
     * @return Returns the failures of the specified member and its descendants.
     */
    public fun under(path: GraphPath): List<ValidationFailure> = failures.filter { it.path.startsWith(path) }

    /**
     * Gets the failures whose paths match the specified pattern, in which wildcards match any index, entry, or key.
     *
     * @param pattern The pattern to match; for example, `GraphPath.of(Customer::orders).anyIndex().property(Order::total)`.
     * @return Returns the failures whose paths match the specified pattern.
     */
    public fun matching(pattern: GraphPath): List<ValidationFailure> = failures.filter { it.path.matches(pattern) }

    /**
     * Throws a [ValidationException] if validation found any failures.
     *
     * @return Returns the current result if validation found no failures.
     * @throws ValidationException if validation found any failures.
     */
    public fun orThrow(): ValidationResult = if (isValid) this else throw ValidationException(this)

    /**
     * Gets the result as JSON-compatible data shaped like the subject. Each member with failures is an object, keyed by
     * property name, element index, or map key, as the subject is. Failures of the member itself are under `$failures`,
     * failures of map keys are under `$keys`, and wildcards are keyed `*`; for example,
     * `{"valid": false, "errors": {"orders": {"1": {"total": {"$failures": [...]}}}}}`.
     *
     * @return Returns the result as JSON-compatible data shaped like the subject.
     */
    public fun toTreeMap(): Map<String, Any?> {
        val errors: MutableMap<String, Any?> = linkedMapOf()
        for (failure in failures) {
            var node = errors
            for (segment in failure.path.segments.drop(1)) {
                val (container, key) = when (segment) {
                    is GraphPathSegment.Root -> error("A path contains exactly one root segment.")
                    is GraphPathSegment.Property -> node to segment.name
                    is GraphPathSegment.Index -> node to segment.index.toString()
                    is GraphPathSegment.Entry -> node to segment.key.toString()
                    GraphPathSegment.AnyIndex, GraphPathSegment.AnyEntry -> node to "*"
                    is GraphPathSegment.EntryKey -> node.child("\$keys") to segment.key.toString()
                    GraphPathSegment.AnyEntryKey -> node.child("\$keys") to "*"
                }
                node = container.child(key)
            }
            @Suppress("UNCHECKED_CAST")
            (node.getOrPut("\$failures") { mutableListOf<Any?>() } as MutableList<Any?>) += failure.toMap(path = null)
        }
        return linkedMapOf("valid" to isValid, "errors" to errors)
    }

    /**
     * Gets the result as JSON-compatible data containing a flat list of failures; for example,
     * `{"valid": false, "failures": [{"path": "/orders/1/total", "rule": "comparable.greaterThan", ...}]}`.
     *
     * @param formatter The formatter with which to format paths. The default is JSON Pointer (RFC 6901).
     * @return Returns the result as JSON-compatible data containing a flat list of failures.
     */
    public fun toFlatMap(formatter: GraphPathFormatter = GraphPathFormat.JsonPointer): Map<String, Any?> {
        return linkedMapOf("valid" to isValid, "failures" to failures.map { it.toMap(it.path.format(formatter)) })
    }

    /**
     * Gets the result as JSON shaped like the subject; see [toTreeMap].
     *
     * @return Returns the result as JSON shaped like the subject.
     */
    public fun toTreeJson(): String = Json.write(toTreeMap())

    /**
     * Gets the result as JSON containing a flat list of failures; see [toFlatMap].
     *
     * @param formatter The formatter with which to format paths. The default is JSON Pointer (RFC 6901).
     * @return Returns the result as JSON containing a flat list of failures.
     */
    public fun toFlatJson(formatter: GraphPathFormatter = GraphPathFormat.JsonPointer): String = Json.write(toFlatMap(formatter))

    /**
     * Returns a string that represents the current object: one failure per line.
     *
     * @return Returns a string that represents the current object.
     */
    override fun toString(): String = if (isValid) "Valid." else failures.joinToString("\n")

    @Suppress("UNCHECKED_CAST")
    private fun MutableMap<String, Any?>.child(key: String): MutableMap<String, Any?> {
        return getOrPut(key) { linkedMapOf<String, Any?>() } as MutableMap<String, Any?>
    }

    private fun ValidationFailure.toMap(path: String?): Map<String, Any?> = buildMap {
        if (path != null) put("path", path)
        put("rule", rule)
        put("parameters", parameters)
        put("message", message)
        if (conditions.isNotEmpty()) put("conditions", conditions)
    }
}

/**
 * Represents an exception that is thrown by [ValidationResult.orThrow] when validation found failures.
 *
 * @property result The result of validation.
 */
public class ValidationException(public val result: ValidationResult) : RuntimeException(result.toString())

/**
 * Writes JSON-compatible data (maps, iterables, arrays, strings, numbers, booleans, and null) as JSON. Other values
 * are written as strings.
 */
internal object Json {

    /**
     * Writes the specified value as JSON.
     *
     * @param value The value to write.
     * @return Returns the specified value as JSON.
     */
    fun write(value: Any?): String = buildString { write(value) }

    private fun StringBuilder.write(value: Any?) {
        when (value) {
            null -> append("null")
            is Boolean -> append(value)
            is java.math.BigDecimal -> append(value.toPlainString())
            is Double -> if (value.isFinite()) append(value) else string(value.toString())
            is Float -> if (value.isFinite()) append(value) else string(value.toString())
            is Number -> append(value)
            is Map<*, *> -> {
                append('{')
                value.entries.forEachIndexed { index, (key, item) ->
                    if (index > 0) append(',')
                    string(key.toString())
                    append(':')
                    write(item)
                }
                append('}')
            }
            is Iterable<*> -> list(value.toList())
            is Array<*> -> list(value.toList())
            else -> string(value.toString())
        }
    }

    private fun StringBuilder.list(values: List<Any?>) {
        append('[')
        values.forEachIndexed { index, item ->
            if (index > 0) append(',')
            write(item)
        }
        append(']')
    }

    private fun StringBuilder.string(value: String) {
        append('"')
        for (char in value) {
            when {
                char == '"' -> append("\\\"")
                char == '\\' -> append("\\\\")
                char == '\n' -> append("\\n")
                char == '\r' -> append("\\r")
                char == '\t' -> append("\\t")
                char < ' ' -> append("\\u%04x".format(char.code))
                else -> append(char)
            }
        }
        append('"')
    }
}
