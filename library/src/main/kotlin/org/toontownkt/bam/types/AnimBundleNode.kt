package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer

@Serializable
@SerialName("AnimBundleNode")
public data class AnimBundleNodeImpl(val pandaNode: PandaNode, override val bundle: ObjPointer<AnimBundle>) : AnimBundleNode,
    PandaNode by pandaNode

public fun BamFactoryScope.getAnimBundleNode(): AnimBundleNode = AnimBundleNodeImpl(getPandaNode(), getObjPointer())