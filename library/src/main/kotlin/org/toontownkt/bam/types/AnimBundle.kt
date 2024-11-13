package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("AnimBundle")
public data class AnimBundleImpl(val animGroup: AnimGroup, override val fps: Float, override val numFrames: UShort) :
    AnimBundle, AnimGroup by animGroup


public fun BamFactoryScope.getAnimBundle(): AnimBundle = AnimBundleImpl(getAnimGroup(), getF32(), getU16())
