package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer
import org.toontownkt.bam.getObjPointerList

@Serializable
@SerialName("PandaNode")
public data class PandaNodeImpl(
    override val name: String,
    override val state: ObjPointer<RenderState>,
    override val transform: ObjPointer<TransformState>,
    override val effects: ObjPointer<RenderEffects>,
    override val drawControlMask: UInt,
    override val drawShowMask: UInt,
    override val intoCollideMask: UInt,
    override val boundsType: UByte,
    override val keys: Map<String, String>,
    override val children: ObjList<PandaNode>
) : PandaNode

public fun BamFactoryScope.getPandaNode(): PandaNode {
    val name = getLengthPrefixedString()

    val statePtr = getObjPointer<RenderState>()
    val transformPtr = getObjPointer<TransformState>()
    val effectsPtr = getObjPointer<RenderEffects>()

    val drawControlMask = getU32()
    val drawShowMask = getU32()
    val intoCollideMask = getU32()
    val boundsType = getU8()

    val keys = mutableMapOf<String, String>()
    for (i in 0..<getI32()) {
        val k = getLengthPrefixedString()
        val v = getLengthPrefixedString()
        keys[k] = v
    }

    @Suppress("UNUSED_VARIABLE") val parents = getObjPointerList<PandaNode>()

    val children: MutableObjList<PandaNode> = mutableListOf()
    for (i in 0..<getU16().toInt()) {
        children += getObjPointer<PandaNode>()
        @Suppress("UNUSED_VARIABLE") val _sort = getI32()
    }

    val stashedChildren: MutableObjList<PandaNode> = mutableListOf()
    for (i in 0..<getU16().toInt()) {
        stashedChildren += getObjPointer<PandaNode>()
        @Suppress("UNUSED_VARIABLE") val _sort = getI32()
    }

    return PandaNodeImpl(
        name,
        statePtr,
        transformPtr,
        effectsPtr,
        drawControlMask,
        drawShowMask,
        intoCollideMask,
        boundsType,
        keys,
        children + stashedChildren
    )
}
