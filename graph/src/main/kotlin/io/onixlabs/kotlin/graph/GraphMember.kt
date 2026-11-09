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

import kotlin.reflect.KProperty1

/**
 * Represents a member of an object graph that a graph plugin can address: a property, the elements of a collection
 * or array property, or the keys or values of a map property.
 */
public sealed interface GraphMember {

    /**
     * Gets the property through which the member is reached.
     */
    public val property: KProperty1<*, *>

    /**
     * Creates the path of the current member relative to the specified parent path. Element and entry members produce
     * wildcard paths, such as `Customer.orders[*]`, which address every element or entry.
     *
     * @param parent The path of the object that declares the property.
     * @return Returns the path of the current member relative to the specified parent path.
     */
    public fun pathFrom(parent: GraphPath): GraphPath

    /**
     * Represents the value of a property.
     *
     * @property property The property.
     */
    public data class Property(override val property: KProperty1<*, *>) : GraphMember {
        override fun pathFrom(parent: GraphPath): GraphPath = parent.property(property)
    }

    /**
     * Represents each element of a collection or array property.
     *
     * @property property The collection or array property.
     */
    public data class Elements(override val property: KProperty1<*, *>) : GraphMember {
        override fun pathFrom(parent: GraphPath): GraphPath = parent.property(property).anyIndex()
    }

    /**
     * Represents each key of a map property.
     *
     * @property property The map property.
     */
    public data class Keys(override val property: KProperty1<*, *>) : GraphMember {
        override fun pathFrom(parent: GraphPath): GraphPath = parent.property(property).anyEntryKey()
    }

    /**
     * Represents each value of a map property.
     *
     * @property property The map property.
     */
    public data class Values(override val property: KProperty1<*, *>) : GraphMember {
        override fun pathFrom(parent: GraphPath): GraphPath = parent.property(property).anyEntry()
    }
}

/**
 * Represents a value of a [GraphMember] that was read from a subject, together with its concrete path.
 *
 * @property path The concrete path of the value, such as `Customer.orders[2]`.
 * @property value The value.
 */
public data class GraphMemberValue(public val path: GraphPath, public val value: Any?)
