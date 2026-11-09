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

import io.onixlabs.kotlin.graph.GraphMember
import kotlin.reflect.KFunction1

/**
 * Represents a validation rule: an identified, parameterised test applied to the value of a member.
 *
 * @property id The namespaced identifier of the rule, such as `string.notBlank`.
 * @property parameters The parameters of the rule, such as `minimum` and `maximum`.
 * @property message The message reported when the rule fails.
 * @property judgesNull Determines whether the rule judges null values. Rules that do not judge null values pass on null.
 * @property test The test applied to the value of the member.
 */
internal class ValidationRule(
    val id: String,
    val parameters: Map<String, Any?>,
    val message: String,
    val judgesNull: Boolean,
    val test: (Any?) -> Boolean,
) {

    /**
     * Determines whether the specified value passes the current rule.
     *
     * @param value The value to test.
     * @return Returns true if the specified value passes the current rule; otherwise, false.
     */
    fun passes(value: Any?): Boolean = (value == null && !judgesNull) || test(value)
}

/**
 * Represents a node of a validation definition that applies to a subject.
 */
internal sealed interface SubjectNode

/**
 * Represents the rules applied to a member of the subject.
 *
 * @property member The member to which the rules apply.
 * @property body The rules applied to the member.
 */
internal class MemberNode(val member: GraphMember, val body: MemberBody) : SubjectNode

/**
 * Represents the rules applied to the result of calling a member function of the subject that takes no arguments.
 *
 * @property function The member function.
 * @property body The rules applied to the result.
 */
internal class FunctionNode(val function: KFunction1<*, *>, val body: MemberBody) : SubjectNode

/**
 * Represents the rules applied to every member of the subject, except those excluded, as declared by `all { }`.
 *
 * @property excluded The names of the excluded properties.
 * @property body The rules applied to each member.
 */
internal class AllMembersNode(val excluded: Set<String>, val body: MemberBody) : SubjectNode

/**
 * Represents nodes that apply to the subject depending on a condition.
 *
 * @property description The description of the condition.
 * @property condition The condition, evaluated against the subject.
 * @property then The nodes that apply when the condition is satisfied.
 * @property otherwise The nodes that apply when the condition is not satisfied.
 */
internal class SubjectConditionalNode(
    val description: String,
    val condition: (Any) -> Boolean,
    val then: List<SubjectNode>,
    val otherwise: List<SubjectNode>,
) : SubjectNode

/**
 * Represents the rules applied to the value of a member.
 *
 * @property items The rules and conditional rules applied to the value.
 * @property requireGraph Determines whether a graph must be registered for the member's type.
 * @property skipGraph Determines whether validation must not descend into the member's registered graphs.
 */
internal class MemberBody(val items: List<MemberItem>, val requireGraph: Boolean, val skipGraph: Boolean)

/**
 * Represents an item applied to the value of a member.
 */
internal sealed interface MemberItem

/**
 * Represents a rule applied to the value of a member.
 *
 * @property rule The rule.
 */
internal class RuleItem(val rule: ValidationRule) : MemberItem

/**
 * Represents rules applied to the value of a member depending on a condition.
 *
 * @property description The description of the condition.
 * @property condition The condition, evaluated against the value.
 * @property then The items that apply when the condition is satisfied.
 * @property otherwise The items that apply when the condition is not satisfied.
 */
internal class MemberConditionalItem(
    val description: String,
    val condition: (Any?) -> Boolean,
    val then: List<MemberItem>,
    val otherwise: List<MemberItem>,
) : MemberItem
