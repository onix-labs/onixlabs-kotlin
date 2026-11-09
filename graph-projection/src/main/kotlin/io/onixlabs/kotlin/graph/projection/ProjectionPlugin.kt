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
package io.onixlabs.kotlin.graph.projection

import io.onixlabs.kotlin.core.typeconverters.TypeConverter
import io.onixlabs.kotlin.graph.GraphBuilder
import io.onixlabs.kotlin.graph.GraphPath
import io.onixlabs.kotlin.graph.GraphPlugin
import io.onixlabs.kotlin.graph.GraphRegistryBuilder
import io.onixlabs.kotlin.graph.GraphRegistryExtension
import kotlin.reflect.KClass

/**
 * Represents the projection graph plugin, which contributes [ProjectionDefinition]s to graphs through `project<R> { }`.
 */
public object ProjectionPlugin : GraphPlugin<ProjectionDefinition>("projection")

/**
 * Represents a projection from a source type to a target type, declared by `project<R> { }`.
 *
 * @property sourceType The source type.
 * @property targetType The target type.
 */
public class ProjectionDefinition internal constructor(
    public val sourceType: KClass<*>,
    public val targetType: KClass<*>,
    internal val mappings: Map<String, ParameterMapping>,
)

/**
 * Declares a projection from the current graph's type to the specified target type. Target parameters that are not
 * mapped explicitly obtain their values from the source properties with the same names.
 *
 * @param S The source type.
 * @param R The target type.
 * @param target The target type.
 * @param block The block that declares the projection's mappings.
 */
public fun <S : Any, R : Any> GraphBuilder<S>.project(target: KClass<R>, block: ProjectionBuilder<S, R>.() -> Unit = {}) {
    define(ProjectionPlugin, ProjectionDefinition(type, target, ProjectionBuilder<S, R>(target).apply(block).build()))
}

/**
 * Declares a projection from the current graph's type to the specified target type. Target parameters that are not
 * mapped explicitly obtain their values from the source properties with the same names. The source type is inferred,
 * so only the target type needs to be named: `project<_, CustomerDto> { ... }`.
 *
 * @param S The source type.
 * @param R The target type.
 * @param block The block that declares the projection's mappings.
 */
public inline fun <S : Any, reified R : Any> GraphBuilder<S>.project(noinline block: ProjectionBuilder<S, R>.() -> Unit = {}) {
    project(R::class, block)
}

/**
 * Represents a factory that creates instances of target types which have no usable primary constructor.
 *
 * @property type The type created by the factory; the factory also applies to its subtypes.
 * @property create The function that creates an instance of the requested type.
 */
public class ProjectionTargetFactory internal constructor(public val type: KClass<*>, internal val create: (KClass<*>) -> Any)

/**
 * Represents a type converter registered for projection.
 *
 * @property type The type to which the converter converts.
 * @property converter The converter.
 */
public class ProjectionTypeConverter internal constructor(public val type: KClass<*>, public val converter: TypeConverter<*>)

/**
 * Represents the registry extension through which target factories are registered.
 */
public object ProjectionTargets : GraphRegistryExtension<ProjectionTargetFactory>("projection targets")

/**
 * Represents the registry extension through which type converters are registered.
 */
public object ProjectionTypeConverters : GraphRegistryExtension<ProjectionTypeConverter>("projection type converters")

/**
 * Specifies a registry-level projection setting.
 */
public enum class ProjectionSetting {

    /**
     * Projection uses only the type converters registered with `typeConverter<T>(...)`, and not the core type
     * converters that it uses by default.
     */
    NO_DEFAULT_TYPE_CONVERTERS,
}

/**
 * Represents the registry extension through which projection settings are registered.
 */
public object ProjectionSettings : GraphRegistryExtension<ProjectionSetting>("projection settings")

/**
 * Stops projection from using the core type converters by default, so that it uses only the type converters
 * registered with `typeConverter<T>(...)`.
 */
public fun GraphRegistryBuilder.withoutDefaultTypeConverters() {
    extend(ProjectionSettings, ProjectionSetting.NO_DEFAULT_TYPE_CONVERTERS)
}

/**
 * Registers a factory that creates instances of the specified type, and of its subtypes, for projection; for example,
 * `projectionTarget(Entity::class) { type -> Entity.create(type) }`. Every mapped value is then set through the
 * instance's public mutable properties.
 *
 * @param T The type created by the factory.
 * @param type The type created by the factory.
 * @param create The function that creates an instance of the requested type.
 */
@Suppress("UNCHECKED_CAST")
public fun <T : Any> GraphRegistryBuilder.projectionTarget(type: KClass<T>, create: (KClass<out T>) -> T) {
    extend(ProjectionTargets, ProjectionTargetFactory(type) { create(it as KClass<out T>) })
}

/**
 * Registers a factory that creates instances of the specified type for projection; for example,
 * `projectionTarget<KCustomer> { KCustomer() }`.
 *
 * @param T The type created by the factory.
 * @param create The function that creates an instance.
 */
public inline fun <reified T : Any> GraphRegistryBuilder.projectionTarget(noinline create: () -> T) {
    projectionTarget(T::class) { create() }
}

/**
 * Registers a type converter for projection, which converts values to the specified type. It replaces any default
 * converter for the type.
 *
 * @param T The type to which the converter converts.
 * @param type The type to which the converter converts.
 * @param converter The converter.
 */
public fun <T : Any> GraphRegistryBuilder.typeConverter(type: KClass<T>, converter: TypeConverter<T>) {
    extend(ProjectionTypeConverters, ProjectionTypeConverter(type, converter))
}

/**
 * Registers a type converter for projection, which converts values to the specified type. It replaces any default
 * converter for the type.
 *
 * @param T The type to which the converter converts.
 * @param converter The converter.
 */
public inline fun <reified T : Any> GraphRegistryBuilder.typeConverter(converter: TypeConverter<T>) {
    typeConverter(T::class, converter)
}

/**
 * Represents a failure to project a target parameter or property.
 *
 * @property path The path of the target parameter or property, such as `CustomerDto.orders[1].total`.
 * @property sourcePath The path of the source value, if known, such as `Customer.orders[1].total`.
 * @property message The reason for the failure.
 * @property cause The underlying exception, if any.
 */
public data class ProjectionFailure(
    public val path: GraphPath,
    public val sourcePath: GraphPath?,
    public val message: String,
    public val cause: Throwable? = null,
) {

    /**
     * Returns a string that represents the current object.
     *
     * @return Returns a string that represents the current object.
     */
    override fun toString(): String = "$path: $message" + if (sourcePath != null) " (from $sourcePath)" else ""
}

/**
 * Represents an exception that is thrown when projection fails. Projection continues wherever it can, so the
 * exception carries every failure.
 *
 * @property failures The failures.
 */
public class ProjectionException(public val failures: List<ProjectionFailure>) : RuntimeException(
    failures.singleOrNull()?.toString() ?: failures.joinToString("\n", prefix = "${failures.size} projection failures:\n") { " - $it" },
    failures.firstNotNullOfOrNull { it.cause },
)
