@file:OptIn(ExperimentalUnsignedTypes::class)

package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer
import org.toontownkt.bam.readBytesToArray

@Serializable
@SerialName("GeomVertexArrayData")
public data class GeomVertexArrayDataImpl(
    override val arrayFormat: ObjPointer<GeomVertexArrayFormat>,
    override val usageHint: UByte,
    override val data: @Contextual UByteArray
) : GeomVertexArrayData

public fun BamFactoryScope.getGeomVertexArrayData(): GeomVertexArrayData {
    return GeomVertexArrayDataImpl(
        getObjPointer(),
        getU8(),
        buf.readBytesToArray(getI32()).toUByteArray()
    )
}