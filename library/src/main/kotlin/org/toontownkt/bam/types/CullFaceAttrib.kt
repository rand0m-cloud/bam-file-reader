package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("CullFaceAttrib")
public data class CullFaceAttribImpl(override val mode: Byte, override val reverse: Boolean) : CullFaceAttrib

public fun BamFactoryScope.getCullFaceAttrib(): CullFaceAttrib = CullFaceAttribImpl(getI8(), getBool())