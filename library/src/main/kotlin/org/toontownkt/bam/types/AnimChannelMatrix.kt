package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("AnimChannelMatrix")
public data class AnimChannelMatrixImpl(val animChannelBase: AnimChannelBase) : AnimChannelMatrix,
    AnimChannelBase by animChannelBase

public fun BamFactoryScope.getAnimChannelMatrix(): AnimChannelMatrix = AnimChannelMatrixImpl(getAnimChannelBase())