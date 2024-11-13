package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer
import kotlinx.serialization.Serializable

@Serializable
@SerialName("GeomVertexColumn")
public data class GeomVertexColumnImpl(
    override val name: ObjPointer<InternalName>,
    override val numComponents: UByte,
    override val numericType: UByte,
    override val vertexType: UByte,
    override val start: UShort,
    override val columnAlignment: UByte?
) : GeomVertexColumn


@Serializable
@SerialName("GeomVertexArrayFormat")
public data class GeomVertexArrayFormatImpl(
    override val stride: UShort,
    override val totalBytes: UShort,
    override val padTo: UByte,
    override val divisor: UShort?,
    override val columns: List<GeomVertexColumn>,
) : GeomVertexArrayFormat

public fun BamFactoryScope.getGeomVertexColumn(): GeomVertexColumn = GeomVertexColumnImpl(
    getObjPointer(),
    getU8(),
    getU8(),
    getU8(),
    getU16(),
    if (bamMinorVersion >= 29U) {
        getU8()
    } else {
        null
    }
)

public fun BamFactoryScope.getGeomVertexArrayFormat(): GeomVertexArrayFormat {
    return GeomVertexArrayFormatImpl(
        getU16(),
        getU16(),
        getU8(),
        if (bamMinorVersion > 36U) {
            getU16()
        } else {
            null
        },
        (0..<getI16().toInt()).map { getGeomVertexColumn() }
    )
}