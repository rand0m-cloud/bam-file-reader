package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("ColorAttrib")
public data class ColorAttribImpl(override val attribType: Byte, override val color: Color) : ColorAttrib

public fun BamFactoryScope.getColorAttrib(): ColorAttrib = ColorAttribImpl(getI8(), ColorImpl(getVec4f()))