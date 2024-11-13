package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Vector3f
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("BillboardEffect")
public data class BillboardEffectImpl(
    override val off: Boolean,
    override val upVector: @Contextual Vector3f,
    override val eyeRelative: Boolean,
    override val axialRotate: Boolean,
    override val offset: Float,
    override val lookAtPoint: @Contextual Vector3f, override val lookAt: NodePath?,
    override val fixedDepth: Boolean?
) : BillboardEffect

public fun BamFactoryScope.getBillboardEffect(): BillboardEffect =
    BillboardEffectImpl(
        getBool(),
        getVec3f(),
        getBool(),
        getBool(),
        getF32(),
        getVec3f(),
        if (bamMinorVersion >= 43U) getNodePath() else null,
        if (bamMinorVersion >= 43U) getBool() else null
    )