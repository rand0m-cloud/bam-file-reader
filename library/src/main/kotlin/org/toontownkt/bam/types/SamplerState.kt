package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("SamplerState")
public data class SamplerStateImpl(
    override val wrapU: UByte,
    override val wrapV: UByte,
    override val wrapW: UByte,
    override val minFilter: UByte,
    override val magFilter: UByte,
    override val anisotropicDegree: Short,
    override val borderColor: Color,
    override val minLod: Float,
    override val maxLod: Float,
    override val lodBias: Float
) : SamplerState

public fun BamFactoryScope.getSamplerState(): SamplerState =
    SamplerStateImpl(getU8(), getU8(), getU8(), getU8(), getU8(), getI16(), getColor(), getF32(), getF32(), getF32())