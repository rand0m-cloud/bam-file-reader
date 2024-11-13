package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Vector4f
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("CollisionPlane")
public data class CollisionPlaneImpl(val collisionSolid: CollisionSolid, override val plane: @Contextual Vector4f) :
    CollisionPlane,
    CollisionSolid by collisionSolid

public fun BamFactoryScope.getCollisionPlane(): CollisionPlane = CollisionPlaneImpl(getCollisionSolid(), getVec4f())