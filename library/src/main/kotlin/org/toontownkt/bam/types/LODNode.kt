package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Vector3f
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("LODNode")
public data class LODNodeImpl(
    val pandaNode: PandaNode,
    override val center: @Contextual Vector3f,
    override val switches: List<SwitchVector>
) : LODNode, PandaNode by pandaNode

@Serializable
@SerialName("SwitchVector")
public data class SwitchVectorImpl(
    override val `in`: Float, override val out: Float
) : SwitchVector

public fun BamFactoryScope.getLODNode(): LODNode =
    LODNodeImpl(getPandaNode(), getVec3f(), (0..<getU16().toInt()).map { SwitchVectorImpl(getF32(), getF32()) })