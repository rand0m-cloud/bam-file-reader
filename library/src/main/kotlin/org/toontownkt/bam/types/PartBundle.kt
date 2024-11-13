package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Matrix4f
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointerOrNull

@Serializable
@SerialName("PartBundle")
public data class PartBundleImpl(
    val partGroup: PartGroup,
    override val animPreloadTable: ObjPointer<AnimPreloadTable>?,
    override val blendType: UByte,
    override val animBlendFlag: Boolean,
    override val frameBlendFlag: Boolean,
    override val rootTransform: @Contextual Matrix4f
) : PartBundle, PartGroup by partGroup

public fun BamFactoryScope.getPartBundle(): PartBundle {
    return PartBundleImpl(
        getPartGroup(),
        getObjPointerOrNull(),
        getU8(),
        getBool(),
        getBool(),
        getMatrix4f()
    )
}