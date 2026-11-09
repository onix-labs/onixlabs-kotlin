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

import io.onixlabs.kotlin.graph.Graph
import io.onixlabs.kotlin.graph.GraphBuilder
import io.onixlabs.kotlin.graph.GraphMember
import io.onixlabs.kotlin.graph.GraphPath
import io.onixlabs.kotlin.graph.GraphPlugin
import io.onixlabs.kotlin.graph.GraphRegistry
import io.onixlabs.kotlin.graph.GraphVerifiable
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Represents the validation graph plugin, which contributes [ValidationDefinition]s to graphs through `validate { }`.
 */
public object ValidationPlugin : GraphPlugin<ValidationDefinition>("validation")

/**
 * Represents the validation rules that a graph declares with `validate { }`.
 *
 * @property type The type of the subject.
 */
public class ValidationDefinition internal constructor(public val type: KClass<*>, internal val nodes: List<SubjectNode>) : GraphVerifiable {

    /**
     * Gets the names of the properties whose registered graphs validation must not descend into.
     */
    internal val skippedProperties: Set<String> by lazy {
        memberNodes(nodes).filter { it.body.skipGraph }.mapTo(mutableSetOf()) { it.member.property.name }
    }

    /**
     * Verifies that a graph is registered for every member that declares `requireGraph()`.
     *
     * @param graph The graph that declares the current definition.
     * @param registry The registry into which the graph is composed.
     * @return Returns the configuration problems found, or an empty list if the definition is valid.
     */
    override fun verify(graph: Graph<*>, registry: GraphRegistry): List<String> {
        return memberNodes(nodes).filter { it.body.requireGraph }.mapNotNull { node ->
            val path = node.member.pathFrom(GraphPath.root(type))
            val target = node.member.targetType()
            when {
                target == null -> "$path requires a graph, but its type cannot be determined."
                registry.hasGraphFor(target) -> null
                else -> "$path requires a graph, but no graph is registered for ${target.qualifiedName}."
            }
        }
    }

    private fun memberNodes(nodes: List<SubjectNode>): List<MemberNode> = nodes.flatMap { node ->
        when (node) {
            is MemberNode -> listOf(node)
            is AllMembersNode, is FunctionNode -> emptyList()
            is SubjectConditionalNode -> memberNodes(node.then) + memberNodes(node.otherwise)
        }
    }
}

/**
 * Declares the validation rules of the current graph.
 *
 * @param T The type described by the graph.
 * @param block The block that declares the validation rules.
 */
public fun <T : Any> GraphBuilder<T>.validate(block: ValidationBuilder<T>.() -> Unit) {
    define(ValidationPlugin, ValidationDefinition(type, ValidationBuilder(type).apply(block).build()))
}

/**
 * Gets the declared type of the values of the current member: the property type, the element type of a collection or
 * array, or the key or value type of a map.
 *
 * @return Returns the declared type of the values of the current member, or null if it cannot be determined.
 */
internal fun GraphMember.targetType(): KClass<*>? {
    val type = property.returnType
    return when (this) {
        is GraphMember.Property -> type.classifier as? KClass<*>
        is GraphMember.Elements -> type.argumentClass(0)
        is GraphMember.Keys -> type.argumentClass(0)
        is GraphMember.Values -> type.argumentClass(1)
    }
}

/**
 * Gets the class of the type argument at the specified index of the current type.
 *
 * @param index The index of the type argument.
 * @return Returns the class of the type argument at the specified index, or null if it cannot be determined.
 */
internal fun KType.argumentClass(index: Int): KClass<*>? = arguments.getOrNull(index)?.type?.classifier as? KClass<*>
