package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer

@Serializable
@SerialName("TransformBlendTable")
public data class TransformBlendTableImpl(override val blends: List<TransformBlend>, override val rows: SparseArray) :
    TransformBlendTable


@Serializable
@SerialName("TransformBlend")
public data class TransformBlendImpl(override val entries: List<TransformEntry>) : TransformBlend


@Serializable
@SerialName("TransformEntry")
public data class TransformEntryImpl(override val transform: ObjPointer<VertexTransform>, override val weight: Float) :
    TransformEntry


@Serializable
@SerialName("SparseArray")
public data class SparseArrayImpl(override val subRanges: List<Pair<Int, Int>>, override val inverse: Boolean) :
    SparseArray

public fun BamFactoryScope.getSparseArray(): SparseArray {
    return SparseArrayImpl((0..<getU32().toInt()).map { getI32() to getI32() }, getBool())
}

public fun BamFactoryScope.getTransformEntry(): TransformEntry {
    return TransformEntryImpl(
        getObjPointer(),
        getF32()
    )
}

public fun BamFactoryScope.getTransformBlend(): TransformBlend {
    return TransformBlendImpl(
        (0..<getU16().toInt()).map {
            getTransformEntry()
        }
    )
}

public fun BamFactoryScope.getTransformBlendTable(): TransformBlendTable {
    val blends = (0..<getU16().toInt()).map { getTransformBlend() }
    val array = getSparseArray()
    return TransformBlendTableImpl(blends, array)
}