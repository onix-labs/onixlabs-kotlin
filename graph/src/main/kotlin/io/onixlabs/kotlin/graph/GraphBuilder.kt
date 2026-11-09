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
 * Represents the builder for a [Graph]. Graph plugins extend this builder with their DSL entry points, such as
 * `validate { }` and `project<R> { }`.
 *
 * @param T The type described by the graph.
 * @property type The type described by the graph.
 */
@GraphDslMarker
public class GraphBuilder<T : Any> internal constructor(public val type: KClass<T>) {

    private val definitions: MutableMap<GraphPlugin<*>, MutableList<Any>> = linkedMapOf()

    /**
     * Adds a definition for the specified plugin to the graph. A plugin may contribute several definitions to a graph,
     * which it obtains in declaration order from [Graph.definitionsOf].
     *
     * @param D The type of definition contributed by the plugin.
     * @param plugin The plugin contributing the definition.
     * @param definition The definition to add to the graph.
     */
    public fun <D : Any> define(plugin: GraphPlugin<D>, definition: D) {
        definitions.getOrPut(plugin) { mutableListOf() }.add(definition)
    }

    /**
     * Builds an immutable [Graph] from the current builder.
     *
     * @return Returns an immutable [Graph] from the current builder.
     */
    internal fun build(): Graph<T> = DefaultGraph(type, definitions.mapValues { it.value.toList() })
}

/**
 * Creates an immutable [Graph] of the specified type.
 *
 * @param T The type described by the graph.
 * @param type The type described by the graph.
 * @param block The block that declares the graph's plugin definitions.
 * @return Returns an immutable [Graph] of the specified type.
 */
public fun <T : Any> graph(type: KClass<T>, block: GraphBuilder<T>.() -> Unit = {}): Graph<T> {
    return GraphBuilder(type).apply(block).build()
}

/**
 * Creates an immutable [Graph] of the specified type.
 *
 * @param T The type described by the graph.
 * @param block The block that declares the graph's plugin definitions.
 * @return Returns an immutable [Graph] of the specified type.
 */
public inline fun <reified T : Any> graph(noinline block: GraphBuilder<T>.() -> Unit = {}): Graph<T> {
    return graph(T::class, block)
}
