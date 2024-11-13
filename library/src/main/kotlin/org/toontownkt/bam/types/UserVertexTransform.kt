package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Matrix4f
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("UserVertexTransform")
public data class UserVertexTransformImpl(override val matrix: @Contextual Matrix4f) :
    UserVertexTransform

public fun BamFactoryScope.getUserVertexTransform(): UserVertexTransform = UserVertexTransformImpl(getMatrix4f())