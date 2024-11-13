package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointerList

@Serializable
@SerialName("RenderEffects")
public data class RenderEffectsImpl(override val effects: ObjList<RenderEffect>) : RenderEffects

public fun BamFactoryScope.getRenderEffects(): RenderEffects = RenderEffectsImpl(getObjPointerList())