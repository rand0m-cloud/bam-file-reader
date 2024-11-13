package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Matrix4f
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer
import org.toontownkt.bam.getObjPointerList

@Serializable
@SerialName("CharacterJoint")
public data class CharacterJointImpl(
    val movingPartMatrix: MovingPartMatrix,
    override val character: ObjPointer<Character>,
    override val netTransformNodes: ObjList<PandaNode>,
    override val localTransformNodes: ObjList<PandaNode>,
    override val initialNetTransformInverse: @Contextual Matrix4f,
) : CharacterJoint, MovingPartMatrix by movingPartMatrix

public fun BamFactoryScope.getCharacterJoint(): CharacterJoint {
    return CharacterJointImpl(
        getMovingPartMatrix(),
        getObjPointer(),
        getObjPointerList(),
        getObjPointerList(),
        getMatrix4f()
    )
}