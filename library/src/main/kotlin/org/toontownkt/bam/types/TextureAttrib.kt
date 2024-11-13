package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer
import org.toontownkt.bam.getObjPointerList

@Serializable
@SerialName("TextureAttrib")
public data class TextureAttribImpl(
    override val offAllStages: Boolean,
    override val offStages: ObjList<TextureStage>,
    override val onStages: List<OnTextureStage>
) : TextureAttrib

@Serializable
@SerialName("OnTextureStage")
public data class OnTextureStageImpl(
    override val stage: ObjPointer<TextureStage>,
    override val texture: ObjPointer<Texture>,
    override val implicitSort: UShort,
    override val override: Int?,
    override val samplerState: SamplerState?
) : OnTextureStage

public fun BamFactoryScope.getTextureAttrib(): TextureAttrib {
    return TextureAttribImpl(getBool(), getObjPointerList(), (0..<getU16().toInt()).map {
        OnTextureStageImpl(
            getObjPointer(),
            getObjPointer(),
            getU16(),
            if (bamMinorVersion >= 23U) getI32() else null,
            if (bamMinorVersion >= 36U && getBool()) getSamplerState() else null
        )
    })
}