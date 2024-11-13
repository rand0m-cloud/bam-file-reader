package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer
import org.toontownkt.bam.getObjPointerList
import org.toontownkt.bam.getObjPointerOrNull

@Serializable
@SerialName("GeomVertexData")
public data class GeomVertexDataImpl(
    override val name: String,
    override val format: ObjPointer<GeomVertexFormat>,
    override val usageHint: UByte,
    override val arrays: ObjList<GeomVertexArrayData>,
    override val transformTable: ObjPointer<TransformTable>?,
    override val transformBlendTable: ObjPointer<TransformBlendTable>?,
    override val sliderTable: ObjPointer<SliderTable>?
) : GeomVertexData

public fun BamFactoryScope.getGeomVertexData(): GeomVertexData {
    return GeomVertexDataImpl(
        name = getLengthPrefixedString(),
        format = getObjPointer(),
        usageHint = getU8(),
        arrays = getObjPointerList(),
        transformTable = getObjPointerOrNull(),
        transformBlendTable = getObjPointerOrNull(),
        sliderTable = getObjPointerOrNull(),
    )
}