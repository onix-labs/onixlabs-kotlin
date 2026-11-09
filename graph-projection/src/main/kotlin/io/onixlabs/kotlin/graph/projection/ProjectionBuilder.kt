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
import io.onixlabs.kotlin.graph.GraphDslMarker
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Represents how a target parameter or property obtains its value.
 */
internal sealed interface ParameterMapping {

    /**
     * The value is computed from the source and used as-is.
     *
     * @property compute The function that computes the value from the source.
     */
    class Computed(val compute: (Any) -> Any?) : ParameterMapping

    /**
     * The value is read from the specified source property, then converted by the specified converter or, if there
     * is none, by the projection rules.
     *
     * @property property The source property, or null to use the source property with the same name as the target.
     * @property converter The converter, or null to use the projection rules.
     */
    class FromSource(val property: KProperty1<*, *>?, val converter: TypeConverter<*>?) : ParameterMapping

    /**
     * The target keeps its default value.
     */
    data object Ignored : ParameterMapping
}

/**
 * Represents the builder for a projection from a source type to a target type, declared by `project<R> { }`.
 *
 * Every target constructor parameter, and every other public mutable property, that is not mapped explicitly obtains
 * its value from the source property with the same name. Mappings are declared in either of two equivalent forms: the
 * reference form (`CustomerDto::displayName from { ... }`, `CustomerDto::email from Customer::emailAddress`,
 * `CustomerDto::createdAt using converter`), or the verb form (`parameter(CustomerDto::displayName) { ... }`), which also
 * names constructor parameters that are not properties (`parameter("code") { ... }`).
 *
 * @param S The source type.
 * @param R The target type.
 */
@GraphDslMarker
public class ProjectionBuilder<S : Any, R : Any> internal constructor(private val targetType: KClass<R>) {

    private val mappings: MutableMap<String, ParameterMapping> = linkedMapOf()

    /**
     * Computes the value of the current target parameter from the source. The value is used as-is, without conversion.
     *
     * @param P The type of the target parameter.
     * @param compute The function that computes the value from the source.
     */
    @Suppress("UNCHECKED_CAST")
    public infix fun <P> KProperty1<R, P>.from(compute: (S) -> P) {
        map(name, ParameterMapping.Computed { compute(it as S) })
    }

    /**
     * Reads the value of the current target parameter from the specified source property, converting it by the
     * projection rules, or by a converter specified with [ProjectionParameter.using].
     *
     * @param P The type of the target parameter.
     * @param property The source property.
     * @return Returns a handle with which to specify a converter.
     */
    public infix fun <P> KProperty1<R, P>.from(property: KProperty1<S, *>): ProjectionParameter<P> {
        return fromSource(name, property)
    }

    /**
     * Converts the value of the source property with the same name as the current target parameter, using the
     * specified converter.
     *
     * @param P The type of the target parameter.
     * @param converter The converter.
     */
    public infix fun <P> KProperty1<R, P>.using(converter: TypeConverter<P>) {
        map(name, ParameterMapping.FromSource(null, converter))
    }

    /**
     * Leaves the current target parameter at its default value, even if the source has a property with the same name.
     */
    public fun KProperty1<R, *>.ignore() {
        map(name, ParameterMapping.Ignored)
    }

    /**
     * Computes the value of the specified target parameter from the source. The value is used as-is, without conversion.
     *
     * @param P The type of the target parameter.
     * @param target The target parameter.
     * @param compute The function that computes the value from the source.
     */
    public fun <P> parameter(target: KProperty1<R, P>, compute: (S) -> P) {
        target from compute
    }

    /**
     * Reads the value of the specified target parameter from the specified source property, converting it by the
     * projection rules, or by the specified converter.
     *
     * @param P The type of the target parameter.
     * @param target The target parameter.
     * @param source The source property.
     * @param converter The converter, or null to use the projection rules.
     */
    public fun <P> parameter(target: KProperty1<R, P>, source: KProperty1<S, *>, converter: TypeConverter<P>? = null) {
        fromSource<P>(target.name, source).apply { if (converter != null) using(converter) }
    }

    /**
     * Converts the value of the source property with the same name as the specified target parameter, using the
     * specified converter.
     *
     * @param P The type of the target parameter.
     * @param target The target parameter.
     * @param converter The converter.
     */
    public fun <P> parameter(target: KProperty1<R, P>, converter: TypeConverter<P>) {
        target using converter
    }

    /**
     * Computes the value of the target constructor parameter or property with the specified name from the source. The
     * value is used as-is, without conversion.
     *
     * @param name The name of the target constructor parameter or property.
     * @param compute The function that computes the value from the source.
     */
    @Suppress("UNCHECKED_CAST")
    public fun parameter(name: String, compute: (S) -> Any?) {
        map(name, ParameterMapping.Computed { compute(it as S) })
    }

    /**
     * Reads the value of the target constructor parameter or property with the specified name from the specified
     * source property, converting it by the projection rules, or by the specified converter.
     *
     * @param name The name of the target constructor parameter or property.
     * @param source The source property.
     * @param converter The converter, or null to use the projection rules.
     */
    public fun parameter(name: String, source: KProperty1<S, *>, converter: TypeConverter<*>? = null) {
        map(name, ParameterMapping.FromSource(source, converter))
    }

    /**
     * Leaves the specified target parameters at their default values, even if the source has properties with the same names.
     *
     * @param targets The target parameters.
     */
    public fun ignore(vararg targets: KProperty1<R, *>) {
        for (target in targets) target.ignore()
    }

    /**
     * Builds the mappings of the current builder.
     *
     * @return Returns the mappings of the current builder, keyed by target parameter name.
     */
    internal fun build(): Map<String, ParameterMapping> = mappings.toMap()

    internal fun map(name: String, mapping: ParameterMapping) {
        mappings[name] = mapping
    }

    private fun <P> fromSource(name: String, property: KProperty1<S, *>): ProjectionParameter<P> {
        map(name, ParameterMapping.FromSource(property, null))
        return ProjectionParameter(this, name, property)
    }

    override fun toString(): String = "ProjectionBuilder<${targetType.simpleName}>"
}

/**
 * Represents a handle with which to specify the converter for a target parameter that reads a source property.
 *
 * @param P The type of the target parameter.
 */
@GraphDslMarker
public class ProjectionParameter<P> internal constructor(
    private val builder: ProjectionBuilder<*, *>,
    private val name: String,
    private val property: KProperty1<*, *>,
) {

    /**
     * Converts the value of the source property using the specified converter, instead of the projection rules.
     *
     * @param converter The converter.
     */
    public infix fun using(converter: TypeConverter<P>) {
        builder.map(name, ParameterMapping.FromSource(property, converter))
    }
}
