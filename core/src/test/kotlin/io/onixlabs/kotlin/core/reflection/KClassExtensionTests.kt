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
package io.onixlabs.kotlin.core.reflection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KClassExtensionTests {

    open class Base(val id: Int)

    class Person(id: Int, val name: String, var nickname: String = "none") : Base(id)

    interface NoConstructor

    @Test
    fun `KClass_getPrimaryConstructor should return the primary constructor, or throw if there is none`() {

        // Act
        val constructor = Person::class.getPrimaryConstructor()
        val exception = assertThrows<IllegalArgumentException> { NoConstructor::class.getPrimaryConstructor() }

        // Assert
        assertEquals(listOf("id", "name", "nickname"), constructor.parameters.map { it.name })
        assertEquals("Kotlin class 'io.onixlabs.kotlin.core.reflection.KClassExtensionTests.NoConstructor' has no primary constructor.", exception.message)
    }

    @Test
    fun `KClass_getProperties should include inherited properties`() {

        // Act
        val names = Person::class.getProperties().map { it.name }.sorted()

        // Assert
        assertEquals(listOf("id", "name", "nickname"), names)
    }

    @Test
    fun `KClass_getProperty should return the named property, or throw if there is none`() {

        // Arrange
        val person = Person(1, "Ada")

        // Act
        val property = Person::class.getProperty("name")
        val exception = assertThrows<IllegalArgumentException> { Person::class.getProperty("age") }

        // Assert
        assertEquals("Ada", property.get(person))
        assertNull(Person::class.getPropertyOrNull("age"))
        assertEquals("Kotlin class 'io.onixlabs.kotlin.core.reflection.KClassExtensionTests.Person' has no property named 'age'.", exception.message)
    }

    @Test
    fun `KClass_getMutableProperty should return only mutable properties`() {

        // Arrange
        val person = Person(1, "Ada")

        // Act
        val nickname = Person::class.getMutableProperty("nickname")
        nickname.setter.call(person, "Countess")

        // Assert
        assertEquals(listOf("nickname"), Person::class.getMutableProperties().map { it.name })
        assertEquals("Countess", person.nickname)
        assertNull(Person::class.getMutablePropertyOrNull("name"))
        assertThrows<IllegalArgumentException> { Person::class.getMutableProperty("name") }
    }
}
