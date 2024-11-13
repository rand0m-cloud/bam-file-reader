package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("CharacterJointBundle")
public data class CharacterJointBundleImpl(val partBundle: PartBundle) : CharacterJointBundle, PartBundle by partBundle

public fun BamFactoryScope.getCharacterJointBundle(): CharacterJointBundle {
    return CharacterJointBundleImpl(getPartBundle())
}