package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope

@Serializable
@SerialName("CharacterJointEffect")
public data class CharacterJointEffectImpl(override val character: ObjPointer<Character>) : CharacterJointEffect

public fun BamFactoryScope.getCharacterJointEffect(): CharacterJointEffect =
    CharacterJointEffectImpl(ObjPointer(getU16()))