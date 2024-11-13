@file:OptIn(
    ExperimentalSerializationApi::class, ExperimentalEncodingApi::class,
    ExperimentalUnsignedTypes::class, ExperimentalUnsignedTypes::class, ExperimentalEncodingApi::class,
    InternalSerializationApi::class, ExperimentalSerializationApi::class
)

package org.toontownkt.bam

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.SerializersModule
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

public val bamSerializersModule: SerializersModule = SerializersModule {
    contextual(Vector4f::class, object : KSerializer<Vector4f> {
        override val descriptor: SerialDescriptor =
            listSerialDescriptor(PrimitiveSerialDescriptor("data", PrimitiveKind.FLOAT))

        override fun deserialize(decoder: Decoder): Vector4f {
            return decoder.decodeStructure(descriptor) {
                Vector4f(
                    decodeFloatElement(descriptor, 0),
                    decodeFloatElement(descriptor, 1),
                    decodeFloatElement(descriptor, 2),
                    decodeFloatElement(descriptor, 3)
                )
            }
        }

        override fun serialize(encoder: Encoder, value: Vector4f) {
            encoder.encodeStructure(descriptor) {
                encodeFloatElement(descriptor, 0, value.x)
                encodeFloatElement(descriptor, 1, value.y)
                encodeFloatElement(descriptor, 2, value.z)
                encodeFloatElement(descriptor, 3, value.w)
            }
        }
    })

    contextual(Vector3f::class, object : KSerializer<Vector3f> {
        override val descriptor: SerialDescriptor = buildSerialDescriptor("Vector3f", StructureKind.LIST)

        override fun deserialize(decoder: Decoder): Vector3f {
            return decoder.decodeStructure(descriptor) {
                Vector3f(
                    decodeFloatElement(descriptor, 0),
                    decodeFloatElement(descriptor, 1),
                    decodeFloatElement(descriptor, 2),
                )
            }
        }

        override fun serialize(encoder: Encoder, value: Vector3f) {
            encoder.encodeStructure(descriptor) {
                encodeFloatElement(descriptor, 0, value.x)
                encodeFloatElement(descriptor, 1, value.y)
                encodeFloatElement(descriptor, 2, value.z)
            }
        }
    })

    contextual(Vector2f::class, object : KSerializer<Vector2f> {
        override val descriptor: SerialDescriptor =
            listSerialDescriptor(PrimitiveSerialDescriptor("data", PrimitiveKind.FLOAT))

        override fun deserialize(decoder: Decoder): Vector2f {
            return decoder.decodeStructure(descriptor) {
                Vector2f(
                    decodeFloatElement(descriptor, 0),
                    decodeFloatElement(descriptor, 1),
                )
            }
        }

        override fun serialize(encoder: Encoder, value: Vector2f) {
            encoder.encodeStructure(descriptor) {
                encodeFloatElement(descriptor, 0, value.x)
                encodeFloatElement(descriptor, 1, value.y)


            }
        }
    })

    contextual(Matrix4f::class, object : KSerializer<Matrix4f> {
        override val descriptor: SerialDescriptor = buildSerialDescriptor("Vector3f", StructureKind.LIST)

        override fun deserialize(decoder: Decoder): Matrix4f {
            return decoder.decodeStructure(descriptor) {
                val m00 = decodeFloatElement(descriptor, 0)
                val m01 = decodeFloatElement(descriptor, 1)
                val m02 = decodeFloatElement(descriptor, 2)
                val m03 = decodeFloatElement(descriptor, 3)
                val m10 = decodeFloatElement(descriptor, 4)
                val m11 = decodeFloatElement(descriptor, 5)
                val m12 = decodeFloatElement(descriptor, 6)
                val m13 = decodeFloatElement(descriptor, 7)
                val m20 = decodeFloatElement(descriptor, 8)
                val m21 = decodeFloatElement(descriptor, 9)
                val m22 = decodeFloatElement(descriptor, 10)
                val m23 = decodeFloatElement(descriptor, 11)
                val m30 = decodeFloatElement(descriptor, 12)
                val m31 = decodeFloatElement(descriptor, 13)
                val m32 = decodeFloatElement(descriptor, 14)
                val m33 = decodeFloatElement(descriptor, 15)
                Matrix4f(m00, m10, m20, m30, m01, m11, m21, m31, m02, m12, m22, m32, m03, m13, m23, m33)
            }
        }

        override fun serialize(encoder: Encoder, value: Matrix4f) {
            encoder.encodeStructure(descriptor) {
                encodeFloatElement(descriptor, 0, value.m00())
                encodeFloatElement(descriptor, 1, value.m10())
                encodeFloatElement(descriptor, 2, value.m20())
                encodeFloatElement(descriptor, 3, value.m30())
                encodeFloatElement(descriptor, 4, value.m01())
                encodeFloatElement(descriptor, 5, value.m11())
                encodeFloatElement(descriptor, 6, value.m21())
                encodeFloatElement(descriptor, 7, value.m31())
                encodeFloatElement(descriptor, 8, value.m02())
                encodeFloatElement(descriptor, 9, value.m12())
                encodeFloatElement(descriptor, 10, value.m22())
                encodeFloatElement(descriptor, 11, value.m32())
                encodeFloatElement(descriptor, 12, value.m03())
                encodeFloatElement(descriptor, 13, value.m13())
                encodeFloatElement(descriptor, 14, value.m23())
                encodeFloatElement(descriptor, 15, value.m33())
            }
        }
    })

    contextual(UByteArray::class, object : KSerializer<UByteArray> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UByteArray", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): UByteArray {
            return Base64.decode(decoder.decodeString()).toUByteArray()
        }

        override fun serialize(encoder: Encoder, value: UByteArray) {
            encoder.encodeString(Base64.encode(value.toByteArray()))
        }

    })
}