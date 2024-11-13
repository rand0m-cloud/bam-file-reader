package org.toontownkt.bam.types

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.toontownkt.bam.getU16
import java.nio.ByteBuffer

public fun ByteBuffer.getVec2f(): Vector2f = Vector2f(getFloat(), getFloat())
public fun ByteBuffer.getVec3f(): Vector3f = Vector3f(getFloat(), getFloat(), getFloat())
public fun ByteBuffer.getVec4f(): Vector4f = Vector4f(getFloat(), getFloat(), getFloat(), getFloat())
public fun ByteBuffer.getMatrix4f(): Matrix4f {
    val m00 = getFloat()
    val m01 = getFloat()
    val m02 = getFloat()
    val m03 = getFloat()

    val m10 = getFloat()
    val m11 = getFloat()
    val m12 = getFloat()
    val m13 = getFloat()

    val m20 = getFloat()
    val m21 = getFloat()
    val m22 = getFloat()
    val m23 = getFloat()

    val m30 = getFloat()
    val m31 = getFloat()
    val m32 = getFloat()
    val m33 = getFloat()

    return Matrix4f(
        m00, m10, m20, m30, m01, m11, m21, m31, m02, m12, m22, m32, m03, m13, m23, m33,
    )
}

public fun ByteBuffer.getColor(): ColorImpl = ColorImpl(getVec4f())

public inline fun <reified T : PandaObject> ByteBuffer.getObjPointerOrNull(): ObjPointer<T>? = getU16().let {
    if (it.toInt() == 0) {
        null
    } else {
        ObjPointer(it)
    }
}

public inline fun <reified T : PandaObject> ByteBuffer.getObjPointer(): ObjPointer<T> =
    getObjPointerOrNull() ?: error("was not expecting a null obj pointer")

public inline fun <reified T : PandaObject> ByteBuffer.getObjPointerList(): ObjList<T> =
    (0..<getU16().toInt()).map { getObjPointer() }


public typealias ObjList<T> = List<ObjPointer<T>>
public typealias MutableObjList<T> = MutableList<ObjPointer<T>>