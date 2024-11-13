package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.BamFile

@Serializable
@SerialName("ModelRoot")
public data class ModelRootImpl(
    val modelNode: ModelNode
) : ModelRoot, ModelNode by modelNode

public fun BamFactoryScope.getModelRoot(): ModelRoot = ModelRootImpl(getModelNode())

public fun BamFile.asModelRoot(): ModelRoot? {
    return objects[1U]!! as? ModelRoot
}

