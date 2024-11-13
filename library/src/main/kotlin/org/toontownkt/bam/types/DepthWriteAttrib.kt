package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("DepthWriteAttrib")
public data class DepthWriteAttribImpl(override val mode: Byte) : DepthWriteAttrib

public fun BamFactoryScope.getDepthWriteAttrib(): DepthWriteAttrib = DepthWriteAttribImpl(getI8())