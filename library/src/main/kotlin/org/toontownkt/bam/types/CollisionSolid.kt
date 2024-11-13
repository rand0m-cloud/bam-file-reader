package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Vector3f
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("CollisionSolid")
public data class CollisionSolidImpl(override val flags: UByte, override val effectiveNormal: @Contextual Vector3f?) :
    CollisionSolid

public fun BamFactoryScope.getCollisionSolid(): CollisionSolid {
    val flags = getU8()
    val effectiveNormal = if (flags.and(2U).toInt() != 0) {
        getVec3f()
    } else {
        null
    }

    return CollisionSolidImpl(flags, effectiveNormal)
}
