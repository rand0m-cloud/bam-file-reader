package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointerOrNull

@Serializable
@SerialName("SheetNode")
public data class SheetNodeImpl(val pandaNode: PandaNode, override val nullObj: ObjPointer<PandaObject>?) : SheetNode,
    PandaNode by pandaNode

public fun BamFactoryScope.getSheetNode(): SheetNode = SheetNodeImpl(getPandaNode(), getObjPointerOrNull())