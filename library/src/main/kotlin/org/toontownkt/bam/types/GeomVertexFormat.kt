package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointerList

@Serializable
@SerialName("GeomVertexFormat")
public data class GeomVertexFormatImpl(
    override val animationSpec: GeomVertexAnimationSpec,
    override val formats: ObjList<GeomVertexArrayFormat>,
) : GeomVertexFormat

@Serializable
@SerialName("GeomVertexAnimationSpec")
public data class GeomVertexAnimationSpecImpl(
    override val animationType: UByte,
    override val numTransforms: UShort,
    override val indexedTransforms: Boolean
) : GeomVertexAnimationSpec

public fun BamFactoryScope.getGeomVertexFormat(): GeomVertexFormat {
    val spec = GeomVertexAnimationSpecImpl(getU8(), getU16(), getBool())
    return GeomVertexFormatImpl(spec, getObjPointerList())
}
