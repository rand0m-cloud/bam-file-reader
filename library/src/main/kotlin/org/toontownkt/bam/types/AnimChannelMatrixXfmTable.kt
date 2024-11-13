package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("AnimChannelMatrixXfmTable")
public data class AnimChannelMatrixXfmTableImpl(
    val animChannelMatrix: AnimChannelMatrix,
    override val compressedChannels: Boolean,
    override val newHPRConvention: Boolean,
    override val data: List<List<Float>>
) : AnimChannelMatrixXfmTable,
    AnimChannelMatrix by animChannelMatrix

public fun BamFactoryScope.getAnimChannelMatrixXfmTable(): AnimChannelMatrixXfmTable {
    val animChannelMatrix = getAnimChannelMatrix()
    val compressedChannels = getBool()

    assert(!compressedChannels)

    val newHPRConvention = getBool()
    val data = (0..<12).map {
        (0..<getU16().toInt()).map {
            getF32()
        }
    }

    return AnimChannelMatrixXfmTableImpl(animChannelMatrix, compressedChannels, newHPRConvention, data)
}