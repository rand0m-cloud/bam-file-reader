package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("InternalName")
public data class InternalNameImpl(override val name: String) : InternalName

public fun BamFactoryScope.getInternalName(): InternalName = InternalNameImpl(getLengthPrefixedString())