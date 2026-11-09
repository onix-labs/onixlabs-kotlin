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

import io.onixlabs.kotlin.core.typeconverters.BigDecimalTypeConverter
import io.onixlabs.kotlin.core.typeconverters.BigIntegerTypeConverter
import io.onixlabs.kotlin.core.typeconverters.BooleanTypeConverter
import io.onixlabs.kotlin.core.typeconverters.ByteTypeConverter
import io.onixlabs.kotlin.core.typeconverters.CharTypeConverter
import io.onixlabs.kotlin.core.typeconverters.DoubleTypeConverter
import io.onixlabs.kotlin.core.typeconverters.FloatTypeConverter
import io.onixlabs.kotlin.core.typeconverters.IntTypeConverter
import io.onixlabs.kotlin.core.typeconverters.LongTypeConverter
import io.onixlabs.kotlin.core.typeconverters.ShortTypeConverter
import io.onixlabs.kotlin.core.typeconverters.StringTypeConverter
import io.onixlabs.kotlin.core.typeconverters.TypeConverter
import io.onixlabs.kotlin.core.typeconverters.UUIDTypeConverter
import io.onixlabs.kotlin.graph.GraphMemberRead
import io.onixlabs.kotlin.graph.GraphPath
import io.onixlabs.kotlin.graph.GraphRegistry
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Collections
import java.util.IdentityHashMap
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType

/**
 * Projects the specified source to an instance of the specified target type.
 *
 * Each target constructor parameter, and then each other public mutable property, obtains its value from an explicit
 * mapping declared by `project<R> { }` on the source's most specific graph, or from the source property with the same
 * name. A value is converted to the target type by the first rule that applies: the parameter's own converter; the
 * value as-is if it already has the target type; element by element for collections, arrays, and maps; a projection
 * registered on the value's graph; a registered type converter; or, for Kotlin classes with a primary constructor and
 * types with a registered factory, projection by name. Projection continues wherever it can and then throws a single
 * [ProjectionException] carrying every failure.
 *
 * @param R The target type.
 * @param source The source to project.
 * @param type The target type.
 * @return Returns an instance of the target type, projected from the specified source.
 * @throws ProjectionException if any target parameter or property could not be projected.
 */
public fun <R : Any> GraphRegistry.project(source: Any, type: KClass<R>): R {
    val run = ProjectionRun(this)
    val sourceRoot = GraphPath.root(mostSpecificGraphFor(source::class)?.type ?: source::class)
    val result = run.projectObject(source, type, GraphPath.root(type), sourceRoot)
    if (run.failures.isNotEmpty()) throw ProjectionException(run.failures)
    @Suppress("UNCHECKED_CAST")
    return result as R
}

/**
 * Projects the specified source to an instance of the specified target type; see [project].
 *
 * @param R The target type.
 * @param source The source to project.
 * @return Returns an instance of the target type, projected from the specified source.
 * @throws ProjectionException if any target parameter or property could not be projected.
 */
public inline fun <reified R : Any> GraphRegistry.project(source: Any): R = project(source, R::class)

/**
 * Represents a single projection run.
 *
 * @param registry The registry from which to obtain graphs, factories, and type converters.
 */
private class ProjectionRun(private val registry: GraphRegistry) {

    private companion object {
        val DEFAULT_CONVERTERS: Map<Class<*>, TypeConverter<*>> = mapOf(
            BigDecimal::class.java to BigDecimalTypeConverter(),
            BigInteger::class.java to BigIntegerTypeConverter(),
            Boolean::class.javaObjectType to BooleanTypeConverter(),
            Byte::class.javaObjectType to ByteTypeConverter(),
            Char::class.javaObjectType to CharTypeConverter(),
            Double::class.javaObjectType to DoubleTypeConverter(),
            Float::class.javaObjectType to FloatTypeConverter(),
            Int::class.javaObjectType to IntTypeConverter(),
            Long::class.javaObjectType to LongTypeConverter(),
            Short::class.javaObjectType to ShortTypeConverter(),
            String::class.java to StringTypeConverter(),
            UUID::class.java to UUIDTypeConverter(),
        )
    }

    /** Marks a value that could not be projected; the failure has been recorded. */
    private object Failed

    /** Marks a target that has no value, and keeps its default. */
    private object Omitted

    val failures: MutableList<ProjectionFailure> = mutableListOf()

    private val sources: MutableSet<Any> = Collections.newSetFromMap(IdentityHashMap())
    private val converters: Map<Class<*>, TypeConverter<*>> = DEFAULT_CONVERTERS +
        registry.extensionsOf(ProjectionTypeConverters).associate { it.type.javaObjectType to it.converter }
    private val factories: List<ProjectionTargetFactory> = registry.extensionsOf(ProjectionTargets)

    fun projectObject(source: Any, type: KClass<*>, path: GraphPath, sourcePath: GraphPath?): Any? {
        if (!sources.add(source)) return fail(path, sourcePath, "the source ${source::class.simpleName} is already being projected, which is a cycle.")
        try {
            val mappings = mappingsFor(source::class, type).orEmpty()
            val factory = factoryFor(type)
            val constructor = if (factory == null) type.primaryConstructor else null
            if (factory == null && constructor == null) {
                return fail(path, sourcePath, "${type.qualifiedName} has no primary constructor, and no projection target factory is registered for it.")
            }

            val properties = sourcePropertiesOf(source)
            val constructed = constructor?.parameters?.mapNotNull { it.name }?.toSet().orEmpty()
            val settable = type.memberProperties
                .filterIsInstance<KMutableProperty1<Any, Any?>>()
                .filter { it.visibility == KVisibility.PUBLIC && it.setter.visibility == KVisibility.PUBLIC && it.name !in constructed }
            var failed = false

            for (name in mappings.keys - constructed - settable.map { it.name }.toSet()) {
                fail(path.property(name), null, "${type.simpleName} has no constructor parameter or mutable property named '$name'.")
                failed = true
            }

            val arguments = mutableMapOf<KParameter, Any?>()
            for (parameter in constructor?.parameters.orEmpty()) {
                val name = parameter.name ?: continue
                val target = path.property(name)
                when (val value = resolve(name, parameter.type, mappings[name], source, properties, target, sourcePath)) {
                    Failed -> failed = true
                    Omitted, null -> when {
                        parameter.isOptional -> Unit
                        parameter.type.isMarkedNullable -> arguments[parameter] = null
                        else -> {
                            val reason = if (value == null) "the value is null, but the parameter is not nullable and has no default."
                            else "there is no value: no mapping or source property, no default, and the parameter is not nullable."
                            fail(target, null, reason)
                            failed = true
                        }
                    }
                    else -> arguments[parameter] = value
                }
            }

            val values = mutableMapOf<KMutableProperty1<Any, Any?>, Any?>()
            for (property in settable) {
                val value = resolve(property.name, property.returnType, mappings[property.name], source, properties, path.property(property), sourcePath)
                when {
                    value == Failed -> failed = true
                    value == Omitted -> Unit
                    value == null && !property.returnType.isMarkedNullable -> Unit
                    else -> values[property] = value
                }
            }
            if (failed) return Failed

            val instance = try {
                factory?.create?.invoke(type) ?: constructor!!.callBy(arguments)
            } catch (exception: Exception) {
                return fail(path, sourcePath, "could not create ${type.simpleName}: ${exception.message}", exception)
            }
            for ((property, value) in values) {
                try {
                    property.set(instance, value)
                } catch (exception: Exception) {
                    fail(path.property(property), sourcePath, "could not set the property: ${exception.message}", exception)
                    failed = true
                }
            }
            return if (failed) Failed else instance
        } finally {
            sources.remove(source)
        }
    }

    private fun resolve(
        name: String,
        type: KType,
        mapping: ParameterMapping?,
        source: Any,
        properties: Map<String, KProperty1<Any, *>>,
        path: GraphPath,
        sourcePath: GraphPath?,
    ): Any? = when (mapping) {
        ParameterMapping.Ignored -> Omitted
        is ParameterMapping.Computed -> try {
            mapping.compute(source)
        } catch (exception: Exception) {
            fail(path, sourcePath, "the mapping failed: ${exception.message}", exception)
        }
        is ParameterMapping.FromSource -> {
            val property = mapping.property ?: properties[name]
            if (property == null) {
                fail(path, sourcePath, "there is no source property named '$name' to convert.")
            } else {
                readAndConvert(source, property, type, mapping.converter, path, sourcePath?.property(property))
            }
        }
        null -> properties[name]?.let { readAndConvert(source, it, type, null, path, sourcePath?.property(it)) } ?: Omitted
    }

    private fun readAndConvert(source: Any, property: KProperty1<*, *>, type: KType, converter: TypeConverter<*>?, path: GraphPath, sourcePath: GraphPath?): Any? {
        val read = registry.read(source, property) as? GraphMemberRead.Value ?: return Omitted
        val value = read.value ?: return null
        return if (converter != null) convertWith(converter, value, type, path, sourcePath) else convert(value, type, path, sourcePath)
    }

    private fun convert(value: Any?, type: KType, path: GraphPath, sourcePath: GraphPath?): Any? {
        if (value == null) return null
        val target = type.classifier as? KClass<*> ?: return value
        val java = target.java

        when {
            java.isArray -> return convertArray(value, type, target, path, sourcePath)
            Map::class.java.isAssignableFrom(java) -> return convertMap(value, type, target, path, sourcePath)
            Iterable::class.java.isAssignableFrom(java) -> return convertCollection(value, type, target, path, sourcePath)
        }

        if (target.javaObjectType.isInstance(value)) return value
        if (mappingsFor(value::class, target) != null) return projectObject(value, target, path, sourcePath)
        converters[target.javaObjectType]?.let { return convertWith(it, value, type, path, sourcePath) }
        if (java.isEnum && (value is Enum<*> || value is String)) return convertEnum(value, type, java, path, sourcePath)
        if (factoryFor(target) != null || isConventional(target)) return projectObject(value, target, path, sourcePath)
        return fail(path, sourcePath, "cannot convert ${describe(value)} to $type.")
    }

    private fun convertCollection(value: Any, type: KType, target: KClass<*>, path: GraphPath, sourcePath: GraphPath?): Any? {
        val elements = elementsOf(value) ?: return fail(path, sourcePath, "cannot convert ${describe(value)} to $type, because it is not a collection or array.")
        val converted = convertElements(elements, type.arguments.getOrNull(0)?.type, path, sourcePath) ?: return Failed
        val result: Collection<Any?> = if (Set::class.java.isAssignableFrom(target.java)) LinkedHashSet(converted) else ArrayList(converted)
        return if (target.java.isInstance(result)) result else fail(path, sourcePath, "cannot create a collection of type $type.")
    }

    private fun convertArray(value: Any, type: KType, target: KClass<*>, path: GraphPath, sourcePath: GraphPath?): Any? {
        val elements = elementsOf(value) ?: return fail(path, sourcePath, "cannot convert ${describe(value)} to $type, because it is not a collection or array.")
        val component = target.java.componentType
        val elementType = type.arguments.getOrNull(0)?.type ?: component.kotlin.starProjectedType
        val converted = convertElements(elements, elementType, path, sourcePath) ?: return Failed
        val array = java.lang.reflect.Array.newInstance(component, converted.size)
        converted.forEachIndexed { index, element -> java.lang.reflect.Array.set(array, index, element) }
        return array
    }

    private fun convertMap(value: Any, type: KType, target: KClass<*>, path: GraphPath, sourcePath: GraphPath?): Any? {
        if (value !is Map<*, *>) return fail(path, sourcePath, "cannot convert ${describe(value)} to $type, because it is not a map.")
        val keyType = type.arguments.getOrNull(0)?.type
        val valueType = type.arguments.getOrNull(1)?.type
        val result = LinkedHashMap<Any?, Any?>()
        var failed = false
        for ((key, element) in value) {
            val convertedKey = convertElement(key, keyType, path.entryKey(key), sourcePath?.entryKey(key))
            val convertedValue = convertElement(element, valueType, path.entry(key), sourcePath?.entry(key))
            if (convertedKey == Failed || convertedValue == Failed) failed = true else result[convertedKey] = convertedValue
        }
        if (failed) return Failed
        return if (target.java.isInstance(result)) result else fail(path, sourcePath, "cannot create a map of type $type.")
    }

    private fun convertElements(elements: List<Any?>, type: KType?, path: GraphPath, sourcePath: GraphPath?): List<Any?>? {
        val converted = elements.mapIndexed { index, element -> convertElement(element, type, path.index(index), sourcePath?.index(index)) }
        return if (converted.any { it == Failed }) null else converted
    }

    private fun convertElement(element: Any?, type: KType?, path: GraphPath, sourcePath: GraphPath?): Any? {
        if (type == null) return element
        val converted = convert(element, type, path, sourcePath)
        return if (converted == null && !type.isMarkedNullable) fail(path, sourcePath, "the value is null, but $type is not nullable.") else converted
    }

    private fun convertWith(converter: TypeConverter<*>, value: Any, type: KType, path: GraphPath, sourcePath: GraphPath?): Any? = try {
        converter.convert(value)
    } catch (exception: Exception) {
        fail(path, sourcePath, "cannot convert ${describe(value)} to $type: ${exception.message}", exception)
    }

    private fun convertEnum(value: Any, type: KType, java: Class<*>, path: GraphPath, sourcePath: GraphPath?): Any? {
        val name = if (value is Enum<*>) value.name else value.toString()
        return java.enumConstants.firstOrNull { (it as Enum<*>).name == name }
            ?: fail(path, sourcePath, "cannot convert ${describe(value)} to $type, because it has no constant named '$name'.")
    }

    private fun elementsOf(value: Any): List<Any?>? = when {
        value is Iterable<*> -> value.toList()
        value.javaClass.isArray -> List(java.lang.reflect.Array.getLength(value)) { java.lang.reflect.Array.get(value, it) }
        else -> null
    }

    private fun mappingsFor(sourceType: KClass<*>, targetType: KClass<*>): Map<String, ParameterMapping>? {
        for (graph in registry.graphsFor(sourceType).asReversed()) {
            val definitions = graph.definitionsOf(ProjectionPlugin).filter { it.targetType == targetType }
            if (definitions.isNotEmpty()) return definitions.fold(emptyMap()) { mappings, definition -> mappings + definition.mappings }
        }
        return null
    }

    private fun factoryFor(type: KClass<*>): ProjectionTargetFactory? {
        val candidates = factories.filter { it.type.java.isAssignableFrom(type.java) }
        return candidates.lastOrNull { candidate -> candidates.none { it !== candidate && candidate.type.java.isAssignableFrom(it.type.java) && it.type != candidate.type } }
    }

    private fun isConventional(type: KClass<*>): Boolean {
        return type.java.isAnnotationPresent(Metadata::class.java) && !type.isAbstract && type.primaryConstructor != null
    }

    private fun sourcePropertiesOf(source: Any): Map<String, KProperty1<Any, *>> {
        val types = registry.graphsFor(source::class).map { it.type } + source::class
        return types.flatMap { registry.membersOf(it) }.distinctBy { it.name }.associateBy { it.name }
    }

    private fun describe(value: Any): String = when (value) {
        is String -> "\"$value\" (String)"
        else -> "$value (${value::class.simpleName})"
    }

    private fun fail(path: GraphPath, sourcePath: GraphPath?, message: String, cause: Throwable? = null): Any {
        failures += ProjectionFailure(path, sourcePath, message, cause)
        return Failed
    }
}
