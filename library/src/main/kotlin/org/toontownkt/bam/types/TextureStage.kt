package org.toontownkt.bam.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.joml.Vector4f
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.getObjPointer

@Serializable
@SerialName("TextureStage")
public data class TextureStageImpl(
    val isDefault: Boolean,
    override val name: String = "default",
    override val sort: Int = 0,
    override val priority: Int = 0,
    override val textureCoordName: ObjPointer<InternalName>? = null,
    override val mode: UByte = 0U,
    override val color: Color = ColorImpl(Vector4f()),
    override val rgbScale: UByte = 1U,
    override val alphaScale: UByte = 1U,
    override val savedResult: Boolean = false,
    override val textureViewOffset: Int? = null,
    override val combineRgbMode: UByte = 0U,
    override val combineRgbOperands: UByte = 0U,
    override val combineRgbSource0: UByte = 0U,
    override val combineRgbOperand0: UByte = 0U,
    override val combineRgbSource1: UByte = 0U,
    override val combineRgbOperand1: UByte = 0U,
    override val combineRgbSource2: UByte = 0U,
    override val combineRgbOperand2: UByte = 0U,

    override val combineAlphaMode: UByte = 0U,
    override val numCombineAlphaOperands: UByte = 0U,
    override val combineAlphaSource0: UByte = 0U,
    override val combineAlphaOperand0: UByte = 0U,
    override val combineAlphaSource1: UByte = 0U,
    override val combineAlphaOperand1: UByte = 0U,
    override val combineAlphaSource2: UByte = 0U,
    override val combineAlphaOperand2: UByte = 0U,
) : TextureStage

public fun BamFactoryScope.getTextureStage(): TextureStage {
    val default = getI8()
    if (default.toInt() == 1) {
        return TextureStageImpl(true)
    } else {
        return TextureStageImpl(
            isDefault = false,
            name = getLengthPrefixedString(),
            sort = getI32(),
            priority = getI32(),
            textureCoordName = getObjPointer(),
            mode = getU8(),
            color = ColorImpl(getVec4f()),
            rgbScale = getU8(),
            alphaScale = getU8(),
            savedResult = getBool(),
            textureViewOffset = if (bamMinorVersion >= 26U) getI32() else null,
            combineRgbMode = getU8(),
            combineRgbOperands = getU8(),
            combineRgbSource0 = getU8(),
            combineRgbOperand0 = getU8(),
            combineRgbSource1 = getU8(),
            combineRgbOperand1 = getU8(),
            combineRgbSource2 = getU8(),
            combineRgbOperand2 = getU8(),
            combineAlphaMode = getU8(),
            numCombineAlphaOperands = getU8(),
            combineAlphaSource0 = getU8(),
            combineAlphaOperand0 = getU8(),
            combineAlphaSource1 = getU8(),
            combineAlphaOperand1 = getU8(),
            combineAlphaSource2 = getU8(),
            combineAlphaOperand2 = getU8()
        )
    }
}
