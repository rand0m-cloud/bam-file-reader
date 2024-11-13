package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointerList


@Serializable
@SerialName("Character")
public data class CharacterImpl(val pandaNode: PandaNode, override val bundles: ObjList<PartBundle>) : Character,
    PandaNode by pandaNode

public fun BamFactoryScope.getCharacter(): Character {
    val pandaNode = getPandaNode()

    val bundles = getObjPointerList<PartBundle>()

    val partsLen = getI16()
    assert(partsLen == 0.toShort())

    return CharacterImpl(pandaNode, bundles)
}