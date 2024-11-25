package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer
import org.toontownkt.bam.getObjPointerOrNull
import org.toontownkt.bam.readBytesToArray

@Serializable
@SerialName("GeomPrimitive")
public data class GeomPrimitiveImpl(
    override val shadeModel: UByte,
    override val firstVertex: Int,
    override val numVertices: Int,
    override val indexType: UByte,
    override val usageHint: UByte,
    override val indices: ObjPointer<GeomVertexArrayData>?
) : GeomPrimitive

@Serializable
@SerialName("GeomTristrips")
public data class GeomTristripsImpl(val geomPrimitive: GeomPrimitive) : GeomTristrips, GeomPrimitive by geomPrimitive

@Serializable
@SerialName("GeomTriangles")
public data class GeomTrianglesImpl(val geomPrimitive: GeomPrimitive) : GeomTriangles, GeomPrimitive by geomPrimitive

public fun BamFactoryScope.getGeomPrimitive(): GeomPrimitive {
    return GeomPrimitiveImpl(
        getU8(),
        getI32(),
        getI32(),
        getU8(),
        getU8(),
        getObjPointerOrNull(),
    )
}

public fun BamFactoryScope.getGeomTristrips(): GeomTristrips = GeomTristripsImpl(getGeomPrimitive())
public fun BamFactoryScope.getGeomTriangles(): GeomTriangles = GeomTrianglesImpl(getGeomPrimitive())
