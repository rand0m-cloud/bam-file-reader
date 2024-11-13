package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RenderAttribEntry")
public data class RenderAttribEntryImpl(override val attrib: ObjPointer<RenderAttrib>, override val override: Int) :
    RenderAttribEntry

@Serializable
@SerialName("RenderState")
public data class RenderStateImpl(override val attribs: List<RenderAttribEntry>) : RenderState

public fun BamFactoryScope.getRenderState(): RenderState {
    val entries = mutableListOf<RenderAttribEntry>()

    for (i in 0..<getI16().toInt()) {
        val attrib = getObjPointer<RenderAttrib>()
        val override = getI32()

        entries += RenderAttribEntryImpl(attrib, override)
    }

    return RenderStateImpl(entries)
}
