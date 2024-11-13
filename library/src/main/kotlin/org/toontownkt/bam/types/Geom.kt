package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer
import org.toontownkt.bam.getObjPointerList

public fun BamFactoryScope.getGeom(): Geom = GeomImpl(
    getObjPointer(),
    getObjPointerList(),
    getU8(),
    getU8(),
    getU16(),
    getU8()
)

@Serializable
@SerialName("Geom")
public data class GeomImpl(
    override val vertexData: ObjPointer<GeomVertexData>,
    override val primitives: ObjList<GeomPrimitive>,
    override val primitiveType: UByte,
    override val shadeModel: UByte,
    override val geomRendering: UShort,
    override val boundsType: UByte
) : Geom