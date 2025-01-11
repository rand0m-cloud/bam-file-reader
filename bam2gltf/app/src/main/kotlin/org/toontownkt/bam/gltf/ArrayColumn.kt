package org.toontownkt.bam.gltf

import gltf.AccessorType
import gltf.ComponentType
import org.toontownkt.bam.BamFile
import org.toontownkt.bam.types.GeomVertexColumn
import org.toontownkt.bam.types.InternalName

sealed class ArrayColumn(val byteLen: UInt) {
    data object Vertex : ArrayColumn(12U)
    data object UV : ArrayColumn(8U)
    data object TransformBlend : ArrayColumn(2U)
    data object Index : ArrayColumn(2U)

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        fun fromInternalName(name: InternalName): ArrayColumn = when (name.name) {
            "vertex" -> Vertex
            "texcoord" -> UV
            "transform_blend" -> TransformBlend
            "index" -> Index
            else -> error("don't know column $name")
        }

        fun fromGeomVertexColumn(bamFile: BamFile, col: GeomVertexColumn): ArrayColumn =
            fromInternalName(bamFile[col.name])

    }

    fun toAccessorTypes(): Pair<ComponentType, AccessorType>? = when (this) {
        Index -> ComponentType.UNSIGNED_SHORT to AccessorType.SCALAR
        TransformBlend -> null
        UV -> ComponentType.FLOAT to AccessorType.VEC2
        Vertex -> ComponentType.FLOAT to AccessorType.VEC3
    }
}

internal fun List<GeomVertexColumn>.toArrayColumns(bam: BamFile): List<ArrayColumn> {
    return map { ArrayColumn.fromGeomVertexColumn(bam, it) }
}