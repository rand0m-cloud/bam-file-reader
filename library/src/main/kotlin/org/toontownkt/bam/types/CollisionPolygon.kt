package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Matrix4f
import org.joml.Vector2f
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("CollisionPolygon")
public data class CollisionPolygonImpl(
    val collisionPlane: CollisionPlane,
    override val points: List<PointDef>,
    override val to2DMatrix: @Contextual Matrix4f
) : CollisionPolygon, CollisionPlane by collisionPlane

@Serializable
@SerialName("PointDef")
public data class PointDefImpl(
    override val point: @Contextual Vector2f,
    override val normalized: @Contextual Vector2f
) :
    PointDef

public fun BamFactoryScope.getCollisionPolygon(): CollisionPlane =
    CollisionPolygonImpl(getCollisionPlane(), (0..<getU16().toInt()).map {
        PointDefImpl(getVec2f(), getVec2f())
    }, getMatrix4f())