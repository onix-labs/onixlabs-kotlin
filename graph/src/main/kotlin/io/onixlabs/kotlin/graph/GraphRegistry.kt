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

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Represents an immutable, thread-safe registry of graphs, which resolves the graphs of nested types at the point of
 * use. Graph plugins are invoked through a registry; for example, `registry.validate(customer)`.
 *
 * Graphs are looked up by the runtime class of an object. [graphsFor] returns every registered graph in the class's
 * hierarchy, including interfaces, ordered from the most general to the most specific; plugins decide whether to
 * compose them (validation) or to use only the most specific (projection). Lookups are cached per class.
 *
 * @property graphs The graphs in the registry, in registration order.
 * @property memberAccess The policy that determines whether members may be read.
 */
public class GraphRegistry internal constructor(
    public val graphs: List<Graph<*>>,
    public val memberAccess: GraphMemberAccess,
    excludedTypes: Set<KClass<*>>,
) {

    private val graphsByType: Map<KClass<*>, Graph<*>> = graphs.associateBy { it.type }
    private val excludedTypes: Set<KClass<*>> = excludedTypes.flatMapTo(mutableSetOf()) { it.allSuperclasses + it }
    private val hierarchyCache: MutableMap<KClass<*>, List<Graph<*>>> = ConcurrentHashMap()
    private val memberCache: MutableMap<KClass<*>, List<KProperty1<Any, *>>> = ConcurrentHashMap()

    /**
     * Gets the graph registered for exactly the specified type, ignoring its supertypes.
     *
     * @param T The type described by the graph.
     * @param type The type described by the graph.
     * @return Returns the graph registered for exactly the specified type, or null if none is registered.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <T : Any> graphFor(type: KClass<T>): Graph<T>? = graphsByType[type] as Graph<T>?

    /**
     * Gets every registered graph that applies to the specified type: the graphs of the type and of its superclasses
     * and interfaces, ordered from the most general to the most specific. Unrelated types, such as two interfaces,
     * keep their registration order.
     *
     * @param type The type for which to obtain graphs, typically the runtime class of an object.
     * @return Returns every registered graph that applies to the specified type, or an empty list if there are none.
     */
    public fun graphsFor(type: KClass<*>): List<Graph<*>> = hierarchyCache.getOrPut(type) { resolveHierarchy(type) }

    /**
     * Gets the most specific registered graph that applies to the specified type.
     *
     * @param type The type for which to obtain a graph, typically the runtime class of an object.
     * @return Returns the most specific registered graph that applies to the specified type, or null if there is none.
     */
    public fun mostSpecificGraphFor(type: KClass<*>): Graph<*>? = graphsFor(type).lastOrNull()

    /**
     * Determines whether any registered graph applies to the specified type.
     *
     * @param type The type to check, typically the declared type of a member.
     * @return Returns true if any registered graph applies to the specified type; otherwise, false.
     */
    public fun hasGraphFor(type: KClass<*>): Boolean = graphsFor(type).isNotEmpty()

    /**
     * Gets the public properties of the specified type which are graph members, as selected by `all { }`. Properties
     * declared by excluded types (see [GraphRegistryBuilder.excludeMembersDeclaredBy]) are omitted. Properties are
     * ordered by primary constructor position, then by name.
     *
     * @param type The type whose members to obtain.
     * @return Returns the public properties of the specified type which are graph members.
     */
    public fun membersOf(type: KClass<*>): List<KProperty1<Any, *>> = memberCache.getOrPut(type) { resolveMembers(type) }

    /**
     * Reads the specified property of the specified object, unless the [memberAccess] policy declines it.
     *
     * @param owner The object that declares the property.
     * @param property The property to read.
     * @return Returns the value that was read, or [GraphMemberRead.Unavailable] if the policy declined to read it.
     */
    @Suppress("UNCHECKED_CAST")
    public fun read(owner: Any, property: KProperty1<*, *>): GraphMemberRead {
        if (!memberAccess.canRead(owner, property)) return GraphMemberRead.Unavailable
        return GraphMemberRead.Value((property as KProperty1<Any, Any?>).get(owner))
    }

    /**
     * Reads the values of the specified member of the specified object, with their concrete paths. A property yields
     * one value; elements, keys, and values yield one value per element or entry, and none if the collection is null.
     * Members that the [memberAccess] policy declines to read yield no values.
     *
     * @param owner The object that declares the member.
     * @param member The member to read.
     * @param parent The path of the object that declares the member.
     * @return Returns the values of the specified member, with their concrete paths.
     * @throws IllegalArgumentException if an element member is not a collection or array, or a key or value member is not a map.
     */
    public fun valuesOf(owner: Any, member: GraphMember, parent: GraphPath): List<GraphMemberValue> {
        val read = read(owner, member.property) as? GraphMemberRead.Value ?: return emptyList()
        val path = parent.property(member.property)
        val value = read.value
        if (member is GraphMember.Property) return listOf(GraphMemberValue(path, value))
        if (value == null) return emptyList()
        return when (member) {
            is GraphMember.Elements -> elementsOf(value, member).mapIndexed { index, element -> GraphMemberValue(path.index(index), element) }
            is GraphMember.Keys -> mapOf(value, member).keys.map { GraphMemberValue(path.entryKey(it), it) }
            is GraphMember.Values -> mapOf(value, member).map { (key, element) -> GraphMemberValue(path.entry(key), element) }
            is GraphMember.Property -> error("Unreachable.")
        }
    }

    private fun elementsOf(value: Any, member: GraphMember): List<Any?> = when {
        value is Iterable<*> -> value.toList()
        value.javaClass.isArray -> List(java.lang.reflect.Array.getLength(value)) { java.lang.reflect.Array.get(value, it) }
        else -> throw IllegalArgumentException("Member '${member.property.name}' is not a collection or array: ${value.javaClass.name}.")
    }

    private fun mapOf(value: Any, member: GraphMember): Map<*, *> {
        return value as? Map<*, *> ?: throw IllegalArgumentException("Member '${member.property.name}' is not a map: ${value.javaClass.name}.")
    }

    private fun resolveHierarchy(type: KClass<*>): List<Graph<*>> {
        val candidates = graphs.filter { it.type.javaObjectType.isAssignableFrom(type.javaObjectType) }.toMutableList()
        val ordered = mutableListOf<Graph<*>>()
        while (candidates.isNotEmpty()) {
            val next = candidates.first { candidate ->
                candidates.none { other -> other !== candidate && other.type.javaObjectType.isAssignableFrom(candidate.type.javaObjectType) }
            }
            ordered += next
            candidates -= next
        }
        return ordered
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveMembers(type: KClass<*>): List<KProperty1<Any, *>> {
        val declaringTypes = (listOf(type) + type.allSuperclasses).filterNot { it in excludedTypes }
        val names = declaringTypes.flatMapTo(mutableSetOf()) { declaring -> declaring.declaredMemberProperties.map { it.name } }
        val positions = type.primaryConstructor?.parameters?.withIndex()?.associate { it.value.name to it.index }.orEmpty()
        return type.memberProperties
            .filter { it.visibility == KVisibility.PUBLIC && it.name in names }
            .sortedWith(compareBy<KProperty1<*, *>> { positions[it.name] ?: Int.MAX_VALUE }.thenBy { it.name })
            .map { it as KProperty1<Any, *> }
    }
}

/**
 * Represents the builder for a [GraphRegistry].
 */
@GraphDslMarker
public class GraphRegistryBuilder internal constructor() {

    private val graphs: MutableList<Graph<*>> = mutableListOf()
    private val excludedTypes: MutableSet<KClass<*>> = linkedSetOf()
    private var memberAccess: GraphMemberAccess = GraphMemberAccess.Unrestricted

    /**
     * Includes the specified graphs in the registry.
     *
     * @param graphs The graphs to include in the registry.
     */
    public fun include(vararg graphs: Graph<*>) {
        this.graphs += graphs
    }

    /**
     * Includes the specified graphs in the registry.
     *
     * @param graphs The graphs to include in the registry.
     */
    public fun include(graphs: Iterable<Graph<*>>) {
        this.graphs += graphs
    }

    /**
     * Declares a graph of the specified type inline, and includes it in the registry. The graph is returned, so that
     * `include(graph<T> { })` also works: including the same graph more than once has no further effect.
     *
     * @param T The type described by the graph.
     * @param block The block that declares the graph's plugin definitions.
     * @return Returns the graph that was declared and included in the registry.
     */
    public inline fun <reified T : Any> graph(noinline block: GraphBuilder<T>.() -> Unit = {}): Graph<T> {
        return graph(T::class, block).also { include(it) }
    }

    /**
     * Sets the policy that determines whether members may be read. The default policy reads every member.
     *
     * @param access The policy that determines whether members may be read.
     */
    public fun memberAccess(access: GraphMemberAccess) {
        memberAccess = access
    }

    /**
     * Excludes the properties declared by the specified types, and by their supertypes, from the members selected by
     * `all { }`; for example, the infrastructure properties of a persistence framework's entity base class.
     *
     * @param types The types whose declared properties to exclude.
     */
    public fun excludeMembersDeclaredBy(vararg types: KClass<*>) {
        excludedTypes += types
    }

    /**
     * Builds an immutable [GraphRegistry] from the current builder, verifying the configuration of every graph.
     *
     * @return Returns an immutable [GraphRegistry] from the current builder.
     * @throws GraphConfigurationException if more than one graph is registered for a type, or a plugin definition reports a configuration problem.
     */
    internal fun build(): GraphRegistry {
        val distinct = graphs.distinct()
        val duplicates = distinct.groupBy { it.type }.filterValues { it.size > 1 }.keys
        if (duplicates.isNotEmpty()) {
            throw GraphConfigurationException(duplicates.map { "More than one graph is registered for ${it.qualifiedName}." })
        }

        val registry = GraphRegistry(distinct, memberAccess, excludedTypes.toSet())
        val problems = distinct.flatMap { graph ->
            graph.plugins.flatMap { plugin ->
                graph.definitionsOf(plugin).filterIsInstance<GraphVerifiable>().flatMap { it.verify(graph, registry) }
            }
        }
        if (problems.isNotEmpty()) throw GraphConfigurationException(problems)
        return registry
    }
}

/**
 * Creates an immutable [GraphRegistry] from the specified graphs and settings.
 *
 * @param block The block that includes graphs in the registry and configures it.
 * @return Returns an immutable [GraphRegistry] from the specified graphs and settings.
 * @throws GraphConfigurationException if the graphs are misconfigured.
 */
public fun graphs(block: GraphRegistryBuilder.() -> Unit): GraphRegistry = GraphRegistryBuilder().apply(block).build()
