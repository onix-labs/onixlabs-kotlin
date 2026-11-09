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
import io.onixlabs.kotlin.graph.GraphMember
import io.onixlabs.kotlin.graph.GraphMemberRead
import io.onixlabs.kotlin.graph.GraphPath
import io.onixlabs.kotlin.graph.GraphRegistry
import io.onixlabs.kotlin.graph.graphs
import java.util.Collections
import java.util.IdentityHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1
import kotlin.reflect.KProperty1

/**
 * Validates the specified subject against the validation rules of every graph in the registry that applies to it.
 *
 * Graphs are looked up by the subject's runtime class. Every applicable graph is applied, from the most general to the
 * most specific. Validation then descends automatically into every member whose value has an applicable graph,
 * including collection elements and map values; a member whose type has no graph has nothing to validate. Objects
 * that are already being validated, through a cycle, are not validated again.
 *
 * @param subject The subject to validate.
 * @param mode The mode in which to validate.
 * @return Returns the result of validation.
 */
public fun GraphRegistry.validate(subject: Any, mode: ValidationMode = ValidationMode.ALL_FAILURES): ValidationResult {
    val root = GraphPath.root(mostSpecificGraphFor(subject::class)?.type ?: subject::class)
    return ValidationRun(this, mode).run(root) { validateObject(subject, root) }
}

/**
 * Validates the specified subject against the specified rules, without declaring a graph or a registry. Only these
 * rules apply: nested values are not validated, because there is no registry from which to obtain their graphs.
 *
 * @param T The type of the subject.
 * @param subject The subject to validate.
 * @param mode The mode in which to validate.
 * @param block The block that declares the validation rules.
 * @return Returns the result of validation.
 */
public inline fun <reified T : Any> validate(
    subject: T,
    mode: ValidationMode = ValidationMode.ALL_FAILURES,
    noinline block: ValidationBuilder<T>.() -> Unit,
): ValidationResult {
    return graphs { graph<T> { validate(block) } }.validate(subject, mode)
}

/**
 * Describes the validation rules that apply to the specified type, without a subject. The result is a [ValidationMode.TEST]
 * result: every rule appears as a failure, collection and map members have wildcard paths, every `whenever` branch is
 * included and labelled with its condition, and the rules of registered subtypes are labelled with the subtype.
 *
 * @param type The type whose validation rules to describe.
 * @return Returns a description of the validation rules that apply to the specified type.
 */
public fun GraphRegistry.describe(type: KClass<*>): ValidationResult {
    val root = GraphPath.root(type)
    return ValidationRun(this, ValidationMode.TEST).run(root) { describeType(type, root) }
}

/**
 * Describes the validation rules that apply to the specified type, without a subject; see [describe].
 *
 * @param T The type whose validation rules to describe.
 * @return Returns a description of the validation rules that apply to the specified type.
 */
public inline fun <reified T : Any> GraphRegistry.describe(): ValidationResult = describe(T::class)

/**
 * Represents a single validation run.
 *
 * @param registry The registry from which to obtain graphs.
 * @param mode The mode in which to validate.
 */
private class ValidationRun(private val registry: GraphRegistry, private val mode: ValidationMode) {

    private class Stop : RuntimeException(null, null, false, false)

    private val failures: MutableList<ValidationFailure> = mutableListOf()
    private val objects: MutableSet<Any> = Collections.newSetFromMap(IdentityHashMap())
    private val types: MutableSet<KClass<*>> = mutableSetOf()

    fun run(root: GraphPath, action: ValidationRun.() -> Unit): ValidationResult {
        try {
            action()
        } catch (_: Stop) {
            // FIRST_FAILURE: the first failure has been recorded.
        }
        return ValidationResult.of(mode, root, failures)
    }

    fun validateObject(subject: Any, path: GraphPath) {
        if (!objects.add(subject)) return
        try {
            val graphs = registry.graphsFor(subject::class)
            val definitions = graphs.flatMap { it.definitionsOf(ValidationPlugin) }
            for (definition in definitions) executeNodes(definition.nodes, subject, definition.type, path, emptyList())
            descend(subject, graphs, definitions, path)
        } finally {
            objects.remove(subject)
        }
    }

    fun describeType(type: KClass<*>, path: GraphPath) {
        if (!types.add(type)) return
        try {
            val graphs = registry.graphsFor(type)
            val definitions = graphs.flatMap { it.definitionsOf(ValidationPlugin) }
            for (definition in definitions) describeNodes(definition.nodes, definition.type, path, emptyList())
            val subtypes = if (type == Any::class) emptyList() else registry.graphs.filter { it.type != type && type.java.isAssignableFrom(it.type.java) }
            for (subtype in subtypes) {
                val label = listOf("when ${subtype.type.simpleName}")
                for (definition in subtype.definitionsOf(ValidationPlugin)) describeNodes(definition.nodes, definition.type, path, label)
            }
            describeDescent(graphs + subtypes, definitions + subtypes.flatMap { it.definitionsOf(ValidationPlugin) }, path)
        } finally {
            types.remove(type)
        }
    }

    // ---------------------------------------------------------------- validation with a subject

    private fun executeNodes(nodes: List<SubjectNode>, subject: Any, type: KClass<*>, path: GraphPath, conditions: List<String>) {
        for (node in nodes) {
            when (node) {
                is MemberNode -> executeMember(subject, node.member, node.body, path, conditions)
                is AllMembersNode -> for (property in registry.membersOf(type)) {
                    if (property.name !in node.excluded) executeMember(subject, GraphMember.Property(property), node.body, path, conditions)
                }
                is FunctionNode -> {
                    @Suppress("UNCHECKED_CAST")
                    val value = (node.function as KFunction1<Any, Any?>).call(subject)
                    executeItems(node.body.items, value, path.function(node.function.name), conditions)
                }
                is SubjectConditionalNode -> if (mode == ValidationMode.TEST) {
                    executeNodes(node.then, subject, type, path, conditions + "when ${node.description}")
                    executeNodes(node.otherwise, subject, type, path, conditions + "when not ${node.description}")
                } else if (node.condition(subject)) {
                    executeNodes(node.then, subject, type, path, conditions + "when ${node.description}")
                } else {
                    executeNodes(node.otherwise, subject, type, path, conditions + "when not ${node.description}")
                }
            }
        }
    }

    private fun executeMember(owner: Any, member: GraphMember, body: MemberBody, path: GraphPath, conditions: List<String>) {
        for (value in registry.valuesOf(owner, member, path)) executeItems(body.items, value.value, value.path, conditions)
    }

    private fun executeItems(items: List<MemberItem>, value: Any?, path: GraphPath, conditions: List<String>) {
        for (item in items) {
            when (item) {
                is RuleItem -> if (mode == ValidationMode.TEST || !item.rule.passes(value)) fail(item.rule, path, conditions)
                is MemberConditionalItem -> if (mode == ValidationMode.TEST) {
                    executeItems(item.then, value, path, conditions + "when ${item.description}")
                    executeItems(item.otherwise, value, path, conditions + "when not ${item.description}")
                } else if (item.condition(value)) {
                    executeItems(item.then, value, path, conditions + "when ${item.description}")
                } else {
                    executeItems(item.otherwise, value, path, conditions + "when not ${item.description}")
                }
            }
        }
    }

    private fun descend(subject: Any, graphs: List<Graph<*>>, definitions: List<ValidationDefinition>, path: GraphPath) {
        val skipped = definitions.flatMapTo(mutableSetOf()) { it.skippedProperties }
        for (property in members(graphs)) {
            if (property.name in skipped) continue
            val value = (registry.read(subject, property) as? GraphMemberRead.Value)?.value ?: continue
            val propertyPath = path.property(property)
            when {
                value is Iterable<*> -> value.forEachIndexed { index, element -> nested(element, propertyPath.index(index)) }
                value is Array<*> -> value.forEachIndexed { index, element -> nested(element, propertyPath.index(index)) }
                value is Map<*, *> -> value.forEach { (key, element) -> nested(element, propertyPath.entry(key)) }
                else -> nested(value, propertyPath)
            }
        }
    }

    private fun nested(value: Any?, path: GraphPath) {
        if (value != null && registry.hasGraphFor(value::class)) validateObject(value, path)
    }

    // ---------------------------------------------------------------- description without a subject

    private fun describeNodes(nodes: List<SubjectNode>, type: KClass<*>, path: GraphPath, conditions: List<String>) {
        for (node in nodes) {
            when (node) {
                is MemberNode -> describeItems(node.body.items, node.member.pathFrom(path), conditions)
                is FunctionNode -> describeItems(node.body.items, path.function(node.function.name), conditions)
                is AllMembersNode -> for (property in registry.membersOf(type)) {
                    if (property.name !in node.excluded) describeItems(node.body.items, path.property(property), conditions)
                }
                is SubjectConditionalNode -> {
                    describeNodes(node.then, type, path, conditions + "when ${node.description}")
                    describeNodes(node.otherwise, type, path, conditions + "when not ${node.description}")
                }
            }
        }
    }

    private fun describeItems(items: List<MemberItem>, path: GraphPath, conditions: List<String>) {
        for (item in items) {
            when (item) {
                is RuleItem -> fail(item.rule, path, conditions)
                is MemberConditionalItem -> {
                    describeItems(item.then, path, conditions + "when ${item.description}")
                    describeItems(item.otherwise, path, conditions + "when not ${item.description}")
                }
            }
        }
    }

    private fun describeDescent(graphs: List<Graph<*>>, definitions: List<ValidationDefinition>, path: GraphPath) {
        val skipped = definitions.flatMapTo(mutableSetOf()) { it.skippedProperties }
        for (property in members(graphs)) {
            if (property.name in skipped) continue
            val classifier = property.returnType.classifier as? KClass<*> ?: continue
            val propertyPath = path.property(property)
            val (target, targetPath) = when {
                Iterable::class.java.isAssignableFrom(classifier.java) -> GraphMember.Elements(property).targetType() to propertyPath.anyIndex()
                classifier.java.isArray -> GraphMember.Elements(property).targetType() to propertyPath.anyIndex()
                Map::class.java.isAssignableFrom(classifier.java) -> GraphMember.Values(property).targetType() to propertyPath.anyEntry()
                else -> classifier to propertyPath
            }
            if (target != null && isDescribable(target)) describeType(target, targetPath)
        }
    }

    private fun isDescribable(type: KClass<*>): Boolean {
        // Every type is a subtype of Any, so registered subtypes are only considered for members declared as a domain type.
        return registry.hasGraphFor(type) || (type != Any::class && registry.graphs.any { type.java.isAssignableFrom(it.type.java) })
    }

    // ---------------------------------------------------------------- shared

    private fun members(graphs: List<Graph<*>>): List<KProperty1<Any, *>> {
        return graphs.flatMap { registry.membersOf(it.type) }.distinctBy { it.name }
    }

    private fun fail(rule: ValidationRule, path: GraphPath, conditions: List<String>) {
        failures += ValidationFailure(path, rule.id, rule.parameters, rule.message, conditions)
        if (mode == ValidationMode.FIRST_FAILURE) throw Stop()
    }
}
