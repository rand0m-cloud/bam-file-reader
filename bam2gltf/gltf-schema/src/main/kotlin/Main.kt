@file:OptIn(ExperimentalSerializationApi::class)

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.jvm.jvmInline
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.File

val json = Json {
    prettyPrint = true
}

@Serializable
data class JsonSchema(
    @SerialName("\$schema") val schema: String? = null,
    @SerialName("\$id") val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val properties: Map<String, JsonSchema>? = null,
    val allOf: List<JsonSchema>? = null,
    val anyOf: List<JsonSchema>? = null,
    @SerialName("\$ref") val ref: String? = null,
    val items: JsonSchema? = null,
    val uniqueItems: Boolean? = null,
    val minItems: Int? = null,
    val maxItems: Int? = null,
    @SerialName("gltf_detailedDescription") val gltfDetailedDescription: String? = null,
    val dependencies: Map<String, List<String>>? = null,
    val required: List<String>? = null,
    val minimum: JsonElement? = null,
    val default: JsonElement? = null,
    @SerialName("gltf_webgl") val gltfWebGL: String? = null,
    val const: JsonElement? = null,
    val additionalProperties: JsonSchema? = null,
    @SerialName("gltf_sectionDescription") val gltfSectionDescription: String? = null,
    val pattern: String? = null,
    val format: String? = null,
    @SerialName("gltf_uriType") val gltfUriType: String? = null,
    val maximum: JsonElement? = null,
    val multipleOf: Int? = null,
    val not: JsonSchema? = null,
    val exclusiveMinimum: JsonElement? = null,
    val oneOf: List<JsonSchema>? = null,
    val minProperties: Int? = null
) {
    fun removeRefPointers(cache: MutableMap<String, JsonSchema>, dir: File): JsonSchema {
        if (ref != null) {
            return cache.getOrPut(ref) {
                json.decodeFromStream<JsonSchema>(File(dir, ref).inputStream()).removeRefPointers(cache, dir)
            }
        }

        return copy(
            properties = properties?.let { it.mapValues { it.value.removeRefPointers(cache, dir) } },
            allOf = allOf?.let { it.map { it.removeRefPointers(cache, dir) } },
            anyOf = anyOf?.let { it.map { it.removeRefPointers(cache, dir) } },
            items = items?.removeRefPointers(cache, dir),
            additionalProperties = additionalProperties?.removeRefPointers(cache, dir)
        )


    }

    fun visit(visitor: (JsonSchema) -> Unit) {
        visitor(this)
        properties?.values?.forEach { it.visit(visitor) }
        anyOf?.forEach { it.visit(visitor) }
        allOf?.forEach { it.visit(visitor) }
        items?.visit(visitor)
        additionalProperties?.visit(visitor)
    }

    fun flatten(): JsonSchema {
        return allOf?.map { it.flatten() }?.fold(this, { acc, schema ->
            acc.copy(
                properties = acc.properties.orEmpty().plus(schema.properties.orEmpty()),
                title = acc.title ?: schema.title
            )
        })
            ?: this
    }

    override fun toString(): String = json.encodeToString(this)
}

fun main() {
    val dir = File("src/main/resources/schema")


    val rootSchema =
        json.decodeFromStream<JsonSchema>(File(dir, "glTF.schema.json").inputStream())
    val map = mutableMapOf("glTF.schema.json" to rootSchema)

    val set = mutableSetOf<JsonSchema>()
    rootSchema.removeRefPointers(map, dir).visit {
        set += it
    }

    val classes = set.filter { it.title != null && it.anyOf == null }
    val enums = set.filter { it.title != null && it.anyOf != null }

    val scope = object : FileBuilderScope {
        override val fileSpec: FileSpec.Builder = FileSpec.builder("gltf", "Gltf")
    }

    classes.forEach { scope.addObject(it.flatten()) }
    enums.forEach { scope.addEnum(it.flatten()) }

    scope.fileSpec.build().writeTo(File("../build/generated/"))
}

interface FileBuilderScope {
    val fileSpec: FileSpec.Builder
}

fun makeKotlinName(string: String): String = string.replace(" ", "").replaceFirstChar { it.uppercase() }

fun JsonSchema.toKotlinType(isRequired: Boolean): TypeName = flatten().run {
    if (title != null) ClassName("gltf", makeKotlinName(title)) else {
        when (type) {
            "array" -> {
                List::class.asTypeName().parameterizedBy(items!!.toKotlinType(true))
            }

            "string" -> String::class.asTypeName()
            "integer" -> Long::class.asTypeName()
            "boolean" -> Boolean::class.asTypeName()
            "number" -> Double::class.asTypeName()

            else -> JsonElement::class.asTypeName()
        }
    }.let {
        if (isRequired) it else it.copy(nullable = true)
    }
}

fun JsonSchema.toProperties(): Map<String, Pair<TypeName, String>> {
    val set = allOf.orEmpty().fold(mutableMapOf<String, Pair<TypeName, String>>()) { acc, x ->
        acc.putAll(x.toProperties())
        acc
    }
    properties.orEmpty().map { it to required.orEmpty().contains(it.key) }
        .forEach { (entry, isRequired) ->
            set[entry.key] =
                entry.value.toKotlinType(isRequired) to (entry.value.description ?: entry.value.title.orEmpty())
        }

    return set
}

fun FileBuilderScope.addObject(schema: JsonSchema) {
    val name = makeKotlinName(schema.title!!)
    when (schema.type) {

        "object" -> {
            val properties = schema.toProperties().toList().sortedBy { it.second.first.isNullable }
            if (properties.isEmpty()) {
                fileSpec.addType(
                    TypeSpec.classBuilder(name)
                        .addModifiers(KModifier.VALUE)
                        .addKdoc(schema.description.orEmpty())
                        .jvmInline()
                        .addAnnotation(Serializable::class)
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter("inner", JsonElement::class)
                                .build()
                        )
                        .addProperty(
                            PropertySpec.builder("inner", JsonElement::class)

                                .initializer("inner")
                                .build()
                        )
                        .build()
                )
            } else {
                fileSpec.addType(
                    TypeSpec.classBuilder(name)
                        .addModifiers(KModifier.DATA)
                        .apply {
                            primaryConstructor(FunSpec.constructorBuilder().apply {
                                for ((propName, ty) in properties) {
                                    if (ty.first.isNullable) {
                                        addParameter(
                                            ParameterSpec.builder(propName, ty.first).defaultValue("null").build()
                                        )
                                    } else {
                                        addParameter(propName, ty.first)
                                    }
                                }
                            }.build())

                            for ((propName, ty) in properties) {
                                addProperty(
                                    PropertySpec.builder(propName, ty.first).addKdoc(ty.second).initializer(propName)
                                        .build()
                                )
                            }
                        }
                        .addKdoc(schema.description.orEmpty())
                        .addAnnotation(Serializable::class)
                        .build()
                )
            }
        }

        "integer" -> fileSpec.addType(
            TypeSpec.classBuilder(name)
                .addModifiers(KModifier.VALUE)
                .addAnnotation(Serializable::class)
                .jvmInline()
                .primaryConstructor(FunSpec.constructorBuilder().addParameter("inner", Long::class).build())
                .addProperty(PropertySpec.builder("inner", Long::class).initializer("inner").build())
                .build()
        )

        else -> fileSpec.addType(
            TypeSpec.classBuilder(name)
                .addModifiers(KModifier.VALUE)
                .addAnnotation(Serializable::class)
                .jvmInline()
                .primaryConstructor(FunSpec.constructorBuilder().addParameter("inner", JsonElement::class).build())
                .addProperty(PropertySpec.builder("inner", JsonElement::class).initializer("inner").build())
                .build()
        )
    }
}

fun FileBuilderScope.addEnum(schema: JsonSchema) {
    val valueType = schema.anyOf!!.first { it.type != null }.toKotlinType(true)
    fileSpec.addType(
        TypeSpec.classBuilder(schema.title!!)
            .addModifiers(KModifier.VALUE)
            .addAnnotation(Serializable::class)
            .addKdoc(schema.description!!)
            .jvmInline()

            .primaryConstructor(FunSpec.constructorBuilder().addParameter("inner", valueType).build())
            .addProperty(PropertySpec.builder("inner", valueType).initializer("inner").build())
            .addType(
                TypeSpec.companionObjectBuilder().apply {
                    schema.anyOf.forEach {
                        if (it.const != null) {
                            val name =
                                (if (it.const.jsonPrimitive.isString) it.const.jsonPrimitive.content else it.description!!).replace(
                                    "/",
                                    "_"
                                )
                            val value = if (it.const.jsonPrimitive.isString) {
                                "\"${it.const.jsonPrimitive.content}\""
                            } else {
                                it.const.jsonPrimitive.content
                            }
                            addProperty(
                                PropertySpec.builder(name, ClassName("gltf", schema.title))
                                    .initializer("${schema.title}($value)").build()
                            )
                        }
                    }
                }.build()
            ).build()
    )
}