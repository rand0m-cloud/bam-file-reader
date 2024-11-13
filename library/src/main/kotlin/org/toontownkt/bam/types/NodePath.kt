package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointerOrNull

@Serializable
@SerialName("NodePath")
public data class NodePathImpl(override val nodes: ObjList<PandaNode>) : NodePath

public fun BamFactoryScope.getNodePath(): NodePath {
    val nodes: MutableObjList<PandaNode> = mutableListOf()
    var node = getObjPointerOrNull<PandaNode>()
    while (node != null) {
        nodes += node
        node = getObjPointerOrNull()
    }

    return NodePathImpl(nodes)
}