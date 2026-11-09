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

import kotlin.reflect.KClass

/**
 * Represents an immutable, declarative description of the object graph of a type.
 *
 * A graph holds the definitions that graph plugins, such as validation and projection, contribute to it; the plugins
 * interpret those definitions. Graphs are built once, are thread-safe, and are composed into a [GraphRegistry], which
 * resolves the graphs of nested types at the point of use.
 *
 * Because [Graph] is an interface, a graph can be given a named type by delegation:
 * `object CustomerGraph : Graph<Customer> by graph({ ... })`.
 *
 * @param T The type described by the graph.
 */
public interface Graph<T : Any> {

    /**
     * Gets the type described by the graph.
     */
    public val type: KClass<T>

    /**
     * Gets the plugins that have contributed definitions to the graph.
     */
    public val plugins: Set<GraphPlugin<*>>

    /**
     * Gets the definitions contributed to the graph by the specified plugin, in declaration order.
     *
     * @param D The type of definition contributed by the plugin.
     * @param plugin The plugin whose definitions to obtain.
     * @return Returns the definitions contributed to the graph by the specified plugin, or an empty list if there are none.
     */
    public fun <D : Any> definitionsOf(plugin: GraphPlugin<D>): List<D>
}

/**
 * Represents the default, immutable [Graph] implementation.
 *
 * @param T The type described by the graph.
 * @property type The type described by the graph.
 * @param definitions The definitions contributed to the graph, keyed by plugin.
 */
internal class DefaultGraph<T : Any>(
    override val type: KClass<T>,
    private val definitions: Map<GraphPlugin<*>, List<Any>>,
) : Graph<T> {

    override val plugins: Set<GraphPlugin<*>> get() = definitions.keys

    @Suppress("UNCHECKED_CAST")
    override fun <D : Any> definitionsOf(plugin: GraphPlugin<D>): List<D> = definitions[plugin].orEmpty() as List<D>

    override fun toString(): String = "Graph<${type.simpleName}>"
}
