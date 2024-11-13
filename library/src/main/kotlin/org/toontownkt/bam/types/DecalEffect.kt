package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("DecalEffect")
public data object DecalEffectImpl : DecalEffect

@Suppress("UnusedReceiverParameter")
public fun BamFactoryScope.getDecalEffect(): DecalEffect = DecalEffectImpl