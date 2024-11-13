package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("ParametricCurve")
public data class ParametricCurveImpl(
    val pandaNode: PandaNode,
    override val curveType: Byte,
    override val numDimensions: Byte
) : ParametricCurve, PandaNode by pandaNode

public fun BamFactoryScope.getParametricCurve(): ParametricCurve = ParametricCurveImpl(getPandaNode(), getI8(), getI8())