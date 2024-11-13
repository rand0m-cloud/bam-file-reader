package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Vector4f
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("CubicCurveseg")
public data class CubicCurvesegImpl(
    val parametricCurve: ParametricCurve,
    override val xBasis: @Contextual Vector4f,
    override val yBasis: @Contextual Vector4f,
    override val zBasis: @Contextual Vector4f,
    override val wBasis: @Contextual Vector4f,
    override val rational: Boolean
) : CubicCurveseg, ParametricCurve by parametricCurve

public fun BamFactoryScope.getCubicCurveseg(): CubicCurveseg =
    CubicCurvesegImpl(getParametricCurve(), getVec4f(), getVec4f(), getVec4f(), getVec4f(), getBool())