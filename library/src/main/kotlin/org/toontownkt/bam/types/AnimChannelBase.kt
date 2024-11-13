package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("AnimChannelBase")
public data class AnimChannelBaseImpl(val animGroup: AnimGroup, override val lastFrame: UShort) : AnimChannelBase,
    AnimGroup by animGroup

public fun BamFactoryScope.getAnimChannelBase(): AnimChannelBase = AnimChannelBaseImpl(getAnimGroup(), getU16())