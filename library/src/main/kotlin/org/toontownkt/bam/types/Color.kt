package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Vector4f

@Serializable
@SerialName("Color")
public data class ColorImpl(override val colorVec: @Contextual Vector4f) : Color