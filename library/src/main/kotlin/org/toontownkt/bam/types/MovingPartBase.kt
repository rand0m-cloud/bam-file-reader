package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointerOrNull

@Serializable
@SerialName("MovingPartBase")
public data class MovingPartBaseImpl(
    val partGroup: PartGroup,
    override val forcedChannel: ObjPointer<AnimChannelBase>?,
) : MovingPartBase, PartGroup by partGroup

public fun BamFactoryScope.getMovingPartBase(): MovingPartBase {
    return MovingPartBaseImpl(
        getPartGroup(),
        getObjPointerOrNull()
    )
}