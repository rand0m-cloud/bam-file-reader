package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("SequenceNode")
public data class SequenceNodeImpl(val pandaNode: PandaNode, val animInterface: AnimInterface) : SequenceNode,
    PandaNode by pandaNode, AnimInterface by animInterface

@Serializable
@SerialName("AnimInterface")
public data class AnimIntefaceImpl(
    override val numFrames: Int,
    override val frameRate: Float,
    override val playMode: UByte,
    override val startTime: Float,
    override val startFrame: Float,
    override val playFrames: Float,
    override val fromFrame: Int,
    override val toFrame: Int,
    override val playRate: Float,
    override val paused: Boolean,
    override val pausedF: Float
) : AnimInterface

public fun BamFactoryScope.getAnimInterface(): AnimInterface = AnimIntefaceImpl(
    getI32(),
    getF32(),
    getU8(),
    getF32(),
    getF32(),
    getF32(),
    getI32(),
    getI32(),
    getF32(),
    getBool(),
    getF32()
)

public fun BamFactoryScope.getSequenceNode(): SequenceNode = SequenceNodeImpl(getPandaNode(), getAnimInterface())