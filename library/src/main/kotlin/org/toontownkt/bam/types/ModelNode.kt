package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("ModelNode")
public data class ModelNodeImpl(
    val pandaNode: PandaNode,
    override val preserveTransform: UByte,
    override val preserveAttributes: UShort
) : ModelNode, PandaNode by pandaNode

public fun BamFactoryScope.getModelNode(): ModelNode = ModelNodeImpl(getPandaNode(), getU8(), getU16())