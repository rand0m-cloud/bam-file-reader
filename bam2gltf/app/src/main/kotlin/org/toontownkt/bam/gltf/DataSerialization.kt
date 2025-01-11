@file:OptIn(ExperimentalEncodingApi::class)

package org.toontownkt.bam.gltf

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


internal fun ByteArray.createDataUri(): String {
    val data = Base64.encode(this)
    return "data:application/octet-stream;base64,$data"
}

internal fun encodeVec3f(vec: Vector3f): List<Double> =
    listOf(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())

internal fun encodeVec4f(vec: Vector4f): List<Double> =
    listOf(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble(), vec.w.toDouble())

internal fun encodeQuatf(quat: Quaternionf): List<Double> =
    listOf(quat.x.toDouble(), quat.y.toDouble(), quat.z.toDouble(), quat.w.toDouble())

internal fun encodeMatrix4f(matrix: Matrix4f): List<Double> =
    with(matrix) { (0..<4).map { getColumn(it, Vector4f()) }.flatMap { encodeVec4f(it) } }

internal fun Matrix4f.serialize(): ByteArray = encodeMatrix4f(this).fold(ByteArrayOutputStream()) { acc, x ->
    acc.apply {
        write(x.toFloat().serialize())
    }
}.toByteArray()

internal fun Vector4f.serialize(): ByteArray = encodeVec4f(this).fold(ByteArrayOutputStream()) { acc, x ->
    acc.apply {
        write(x.toFloat().serialize())
    }
}.toByteArray()

internal fun List<Float>.serialize(): ByteArray = fold(ByteArrayOutputStream()) { acc, fl ->
    acc.apply {
        write(fl.serialize())
    }
}.toByteArray()

internal fun Float.serialize(): ByteArray =
    ByteBuffer.allocate(4).run {
        order(ByteOrder.LITTLE_ENDIAN)
        putFloat(this@serialize)
        rewind()
        ByteArray(4).also { get(it) }
    }

