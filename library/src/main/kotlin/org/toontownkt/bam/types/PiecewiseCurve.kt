package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer
import kotlinx.serialization.Serializable

@Serializable
@SerialName("PiecewiseCurve")
public data class PiecewiseCurveImpl(val parametricCurve: ParametricCurve, override val segments: List<CurveSegment>) :
    PiecewiseCurve, ParametricCurve by parametricCurve

@Serializable
@SerialName("CurveSegment")
public data class CurveSegmentImpl(override val curve: ObjPointer<ParametricCurve>, override val tend: Double) :
    CurveSegment

public fun BamFactoryScope.getPiecewiseCurve(): PiecewiseCurve =
    PiecewiseCurveImpl(
        getParametricCurve(),
        (0..<getU32().toLong()).map { CurveSegmentImpl(getObjPointer(), getF64()) })