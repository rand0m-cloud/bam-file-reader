package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Vector3f
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("CollisionSphere")
public data class CollisionSphereImpl(
    val collisionSolid: CollisionSolid,
    override val center: @Contextual Vector3f,
    override val radius: Float
) : CollisionSphere, CollisionSolid by collisionSolid

public fun BamFactoryScope.getCollisionSphere(): CollisionSphere =
    CollisionSphereImpl(getCollisionSolid(), getVec3f(), getF32())