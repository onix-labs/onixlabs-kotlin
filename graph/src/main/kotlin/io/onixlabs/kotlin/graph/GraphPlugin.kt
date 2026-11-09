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

/**
 * Represents a graph plugin, such as validation or projection, which contributes definitions of type [D] to graphs.
 *
 * Plugins are compared by identity, so each plugin should be declared as a singleton object. A plugin's DSL entry
 * point is an extension function on [GraphBuilder] which builds a definition and calls [GraphBuilder.define].
 *
 * @param D The type of definition contributed by the plugin.
 * @property name The name of the plugin, used for diagnostics.
 */
public abstract class GraphPlugin<D : Any>(public val name: String) {

    /**
     * Returns a string that represents the current object.
     *
     * @return Returns a string that represents the current object.
     */
    override fun toString(): String = name
}

/**
 * Represents a plugin definition that can verify its configuration when graphs are composed into a [GraphRegistry].
 * For example, a definition that requires the graph of a nested type can report that the graph is not registered.
 */
public interface GraphVerifiable {

    /**
     * Verifies the current definition against the specified registry.
     *
     * @param graph The graph that declares the current definition.
     * @param registry The registry into which the graph is composed.
     * @return Returns the configuration problems found, or an empty list if the definition is valid.
     */
    public fun verify(graph: Graph<*>, registry: GraphRegistry): List<String>
}

/**
 * Represents an exception that is thrown when graphs are misconfigured, for example when a registry is built.
 *
 * @property problems The configuration problems that were found.
 */
public class GraphConfigurationException(public val problems: List<String>) : IllegalStateException(
    problems.singleOrNull() ?: problems.joinToString(
        separator = "\n",
        prefix = "Found ${problems.size} graph configuration problems:\n",
    ) { " - $it" },
)
