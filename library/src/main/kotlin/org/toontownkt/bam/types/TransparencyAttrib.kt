package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("TransparencyAttrib")
public data class TransparencyAttribImpl(override val mode: Byte) : TransparencyAttrib

public fun BamFactoryScope.getTransparenyAttrib(): TransparencyAttrib = TransparencyAttribImpl(getI8())