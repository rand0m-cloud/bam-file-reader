package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("CullBinAttrib")
public data class CullBinAttribImpl(
    override val binName: String,
    override val drawOrder: Int
) : CullBinAttrib

public fun BamFactoryScope.getCullBinAttrib(): CullBinAttrib = CullBinAttribImpl(getLengthPrefixedString(), getI32())