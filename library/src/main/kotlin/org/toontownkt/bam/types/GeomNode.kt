package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer

@Serializable
@SerialName("GeomNode")
public data class GeomNodeImpl(val pandaNode: PandaNode, override val geoms: List<GeomEntry>) : GeomNode,
    PandaNode by pandaNode

public fun BamFactoryScope.getGeomNode(): GeomNode {
    val pandaNode = getPandaNode()
    val entries = mutableListOf<GeomEntry>()
    for (i in 0..<getU16().toShort()) {
        entries += GeomEntryImpl(getObjPointer(), getObjPointer())
    }

    return GeomNodeImpl(pandaNode, entries)
}