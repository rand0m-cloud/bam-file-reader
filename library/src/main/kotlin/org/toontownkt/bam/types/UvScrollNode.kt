package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("UvScrollNode")
public data class UvScrollNodeImpl(
    val pandaNode: PandaNode,
    override val uSpeed: Float,
    override val vSpeed: Float,
    override val wSpeed: Float?,
    override val rSpeed: Float?
) : UvScrollNode, PandaNode by pandaNode

public fun BamFactoryScope.getUvScrollNode(): UvScrollNode {
    val pandaNode = getPandaNode()
    val uSpeed = getF32()
    val vSpeed = getF32()
    val wSpeed = if (bamMinorVersion >= 33U) getF32() else null
    val rSpeed = if (bamMinorVersion >= 22U) getF32() else null
    return UvScrollNodeImpl(pandaNode, uSpeed, vSpeed, wSpeed, rSpeed)
}