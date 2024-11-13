package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer

@Serializable
@SerialName("JointVertexTransform")
public data class JointVertexTransformImpl(override val joint: ObjPointer<CharacterJoint>) : JointVertexTransform

public fun BamFactoryScope.getJointVertexTransform(): JointVertexTransform =
    JointVertexTransformImpl(getObjPointer())