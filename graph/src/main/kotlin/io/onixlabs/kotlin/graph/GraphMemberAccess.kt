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
 * Determines whether graph plugins may read a member of an object.
 *
 * Plugins read every member through the [GraphRegistry], which consults this policy first. The default policy,
 * [Unrestricted], reads everything. Persistence integrations can supply a policy that declines members which are
 * not loaded, such as lazy JPA associations, so that traversal neither triggers queries nor fails outside a session.
 */
public fun interface GraphMemberAccess {

    /**
     * Determines whether the specified member of the specified object may be read.
     *
     * @param owner The object that declares the member.
     * @param property The member to read.
     * @return Returns true if the member may be read; otherwise, false.
     */
    public fun canRead(owner: Any, property: KProperty1<*, *>): Boolean

    public companion object {

        /**
         * Gets a [GraphMemberAccess] policy that permits every member to be read.
         */
        public val Unrestricted: GraphMemberAccess = GraphMemberAccess { _, _ -> true }
    }
}

/**
 * Represents the outcome of reading a member through a [GraphRegistry].
 */
public sealed interface GraphMemberRead {

    /**
     * Represents a member that was read.
     *
     * @property value The value of the member.
     */
    public data class Value(public val value: Any?) : GraphMemberRead

    /**
     * Represents a member that the registry's [GraphMemberAccess] policy declined to read.
     */
    public data object Unavailable : GraphMemberRead
}
