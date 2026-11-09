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

/**
 * Requires the value to be true. Rule `boolean.true`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out Boolean?>.mustBeTrue(message: String = "must be true.") {
    rule("boolean.true", message) { it == true }
}

/**
 * Requires the value to be false. Rule `boolean.false`.
 *
 * @param message The message reported when the rule fails.
 */
public fun MemberValidationBuilder<out Boolean?>.mustBeFalse(message: String = "must be false.") {
    rule("boolean.false", message) { it == false }
}
