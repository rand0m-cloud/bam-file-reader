package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.toontownkt.bam.BamFactoryScope

public fun BamFactoryScope.getTransformState(): TransformState {
    val flags = getU32()
    var state = TransformState.Components()

    println(flags)
    if (TransformState.Flags.ComponentsGiven.matches(flags)) {
        state = state.copy(pos = getVec3f())
        state = if (TransformState.Flags.QuaternionGiven.matches(flags)) {
            state.copy(quat = getVec4f())
        } else {
            state.copy(hpr = getVec3f())
        }
        state = state.copy(scale = getVec3f(), shear = getVec3f())
    }

    if (TransformState.Flags.MatrixKnown.matches(flags)) {
        return TransformState.Matrix(getMatrix4f())
    }

    return state
}

@Serializable
@SerialName("TransformState")
public sealed class TransformState : PandaObject {
    @Serializable
    @SerialName("TransformState.Components")
    public data class Components(
        @Contextual
        val pos: Vector3f = Vector3f(0f),
        @Contextual
        val scale: Vector3f = Vector3f(1f),
        @Contextual
        val shear: Vector3f = Vector3f(0f),
        @Contextual
        val hpr: Vector3f? = null,
        @Contextual
        val quat: Vector4f? = null
    ) : TransformState()

    @Serializable
    @SerialName("TransformState.Matrix")
    public data class Matrix(
        @Contextual
        val matrix: Matrix4f = Matrix4f()
    ) : TransformState()

    @Suppress("unused")
    public abstract class Flags(private val value: UInt) {
        public data object Identity : Flags(1U)
        public data object ComponentsGiven : Flags(8U)
        public data object MatrixKnown : Flags(64U)
        public data object QuaternionGiven : Flags(256U)

        public fun matches(i: UInt): Boolean = value.and(i) != 0U
    }
}