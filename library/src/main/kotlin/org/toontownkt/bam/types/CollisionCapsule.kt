package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Vector3f
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("CollisionCapsule")
public data class CollisionCapsuleImpl(
    val collisionSolid: CollisionSolid,
    override val a: @Contextual Vector3f,
    override val b: @Contextual Vector3f,
    override val radius: Float
) : CollisionCapsule, CollisionSolid by collisionSolid

public fun BamFactoryScope.getCollisionCapsule(): CollisionCapsule =
    CollisionCapsuleImpl(getCollisionSolid(), getVec3f(), getVec3f(), getF32())