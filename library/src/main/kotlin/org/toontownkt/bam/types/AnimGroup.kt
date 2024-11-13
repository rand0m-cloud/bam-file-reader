package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer
import org.toontownkt.bam.getObjPointerList

@Serializable
@SerialName("AnimGroup")
public data class AnimGroupImpl(
    override val name: String,
    override val root: ObjPointer<AnimBundle>,
    override val children: ObjList<AnimGroup>
) : AnimGroup

public fun BamFactoryScope.getAnimGroup(): AnimGroup =
    AnimGroupImpl(getLengthPrefixedString(), getObjPointer(), getObjPointerList())