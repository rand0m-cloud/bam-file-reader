package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer

@Serializable
@SerialName("CollisionNode")
public data class CollisionNodeImpl(
    val pandaNode: PandaNode,
    override val solids: ObjList<CollisionSolid>,
    override val fromCollideMask: UInt
) : CollisionNode, PandaNode by pandaNode

public fun BamFactoryScope.getCollisionNode(): CollisionNode {
    val pandaNode = getPandaNode()

    var len = getU16().toUInt()
    if (len == 0xFFFFU) {
        len = getU32()
    }

    val solids = (0..<len.toLong()).map { getObjPointer<CollisionSolid>() }
    val fromCollideMask = getU32()

    return CollisionNodeImpl(pandaNode, solids, fromCollideMask)
}