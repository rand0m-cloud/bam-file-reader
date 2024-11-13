package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Matrix4f
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("MovingPartMatrix")
public data class MovingPartMatrixImpl(
    val movingPartBase: MovingPartBase,
    override val value: @Contextual Matrix4f,
    override val defaultValue: @Contextual Matrix4f
) : MovingPartMatrix, MovingPartBase by movingPartBase

public fun BamFactoryScope.getMovingPartMatrix(): MovingPartMatrix {
    return MovingPartMatrixImpl(getMovingPartBase(), getMatrix4f(), getMatrix4f())
}