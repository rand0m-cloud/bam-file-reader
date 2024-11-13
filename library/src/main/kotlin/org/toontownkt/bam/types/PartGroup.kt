package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointerList

@Serializable
@SerialName("PartGroup")
public data class PartGroupImpl(override val name: String, override val children: ObjList<PartGroup>) : PartGroup

public fun BamFactoryScope.getPartGroup(): PartGroup {
    val name = getLengthPrefixedString()
    val children = mutableListOf<UShort>()

    return PartGroupImpl(name, getObjPointerList())
}