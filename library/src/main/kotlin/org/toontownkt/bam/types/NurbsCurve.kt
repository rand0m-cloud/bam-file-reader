package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Vector4f
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("NurbsCurve")
public data class NurbsCurveImpl(
    val piecewiseCurve: PiecewiseCurve,
    override val order: Byte,
    override val controlVertices: List<ControlVertex>
) : NurbsCurve, PiecewiseCurve by piecewiseCurve

@Serializable
@SerialName("ControlVertex")
public data class ControlVertexImpl(override val vertex: @Contextual Vector4f, override val weight: Double) :
    ControlVertex

public fun BamFactoryScope.getNurbsCurve(): NurbsCurve =
    NurbsCurveImpl(
        getPiecewiseCurve(),
        getI8(),
        (0..<getU32().toLong()).map { ControlVertexImpl(getVec4f(), getF64()) })