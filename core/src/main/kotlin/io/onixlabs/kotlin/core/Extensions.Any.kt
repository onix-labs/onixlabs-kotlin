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
package io.onixlabs.kotlin.core

public fun <T> T.isEqualTo(value: T): Boolean {
    return this == value
}

public fun <T> T.isEqualToAny(values: Iterable<T>): Boolean {
    return this in values
}

public fun <T> T.isEqualToAny(vararg values: T): Boolean {
    return isEqualToAny(values.toList())
}

public fun <T> T?.isNull(): Boolean {
    return this == null
}
