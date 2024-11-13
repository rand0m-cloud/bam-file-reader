@file:OptIn(ExperimentalUnsignedTypes::class, ExperimentalUnsignedTypes::class)

package org.toontownkt.bam.types

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.toontownkt.bam.BamFactoryScope
import org.toontownkt.bam.readBytesToArray
import java.time.Instant

@Serializable
@SerialName("SimpleRawImage")
public data class SimpleRawImageImpl(
    override val xSize: UInt,
    override val ySize: UInt,
    override val dateGenerated: Int,
    override val data: @Contextual UByteArray
) : SimpleRawImage {
    override fun toString(): String =
        "SimpleRawImageImpl(xSize=$xSize, ySize=$ySize, dateGenerated=${Instant.ofEpochSecond(dateGenerated.toLong())}, data = {${data.size} bytes})"
}

@Serializable
@SerialName("RamImage")
public data class RamImageImpl(override val pageSize: UInt, override val data: UByteArray) : RamImage

@Serializable
@SerialName("Texture")
public data class TextureImpl(
    override val name: String,
    override val fileName: String,
    override val alphaFileName: String,
    override val primaryFileNumChannels: UByte,
    override val alphaFileChannel: UByte,
    override val textureType: UByte,
    override val hasReadMipmaps: Boolean?,
    override val wrapU: UByte,
    override val wrapV: UByte,
    override val wrapW: UByte,
    override val minFilter: UByte,
    override val magFilter: UByte,
    override val anisotropicDegree: Short,
    override val borderColor: Color,
    override val minLod: Float?,
    override val maxLod: Float?,
    override val lodBias: Float?,
    override val compression: UByte,
    override val qualityLevel: UByte,
    override val format: UByte,
    override val numComponents: UByte,
    override val usageHint: UByte?,
    override val autoTextureScale: UByte?,
    override val originalFileXSize: UInt,
    override val originalFileYSize: UInt,
    override val simpleRamImage: SimpleRawImage?,
    override val clearColor: Color?,
    override val xSize: UInt?,
    override val ySize: UInt?,
    override val zSize: UInt?,
    override val padXSize: UInt?,
    override val padYSize: UInt?,
    override val padZSize: UInt?,
    override val numViews: UInt?,
    override val componentType: UByte?,
    override val componentWidth: UByte?,
    override val rawImageCompression: UByte?,
    override val ramImages: List<RamImage>?
) : Texture

public fun BamFactoryScope.getTexture(): Texture {
    val name = getLengthPrefixedString()
    val fileName = getLengthPrefixedString()
    val alphaFileName = getLengthPrefixedString()
    val primaryFileNumChannels = getU8()
    val alphaFileChannel = getU8()

    val hasRawData = getBool()

    var textureType = getU8()

    if (bamMinorVersion < 25U && textureType == 3.toUByte()) {
        // TT_cube_map, TT_2d_texture_array adjustment
        textureType = 4.toUByte()
    }

    val hasReadMipmaps = if (bamMinorVersion >= 32U) {
        getBool()
    } else null


    val wrapU = getU8()
    val wrapV = getU8()
    val wrapW = getU8()
    val minFilter = getU8()
    val magFilter = getU8()
    val anisotropicDegree = getI16()
    val borderColor = ColorImpl(getVec4f())

    var minLod: Float? = null
    var maxLod: Float? = null
    var lodBias: Float? = null
    if (bamMinorVersion >= 36U) {
        minLod = getF32()
        maxLod = getF32()
        lodBias = getF32()
    }

    val compression = getU8()
    val qualityLevel = getU8()
    val format = getU8()
    val numComponents = getU8()

    var usageHint: UByte? = null
    if (textureType == 5U.toUByte()) {
        usageHint = getU8()
    }

    val autoTextureScale = if (bamMinorVersion >= 28U) {
        getU8()
    } else null
    val originalFileXSize = getU32()
    val originalFileYSize = getU32()

    val simpleRamImage = if (getBool()) {
        SimpleRawImageImpl(
            getU32(),
            getU32(),
            getI32(),
            buf.readBytesToArray(getU32().toInt()).toUByteArray()
        )
    } else null

    val clearColor = if (bamMinorVersion >= 45U && getBool()) {
        getColor()
    } else null

    var xSize: UInt? = null
    var ySize: UInt? = null
    var zSize: UInt? = null
    var padXSize: UInt? = null
    var padYSize: UInt? = null
    var padZSize: UInt? = null
    var numViews: UInt? = null

    var componentType: UByte? = null
    var componentWidth: UByte? = null
    var rawImageCompression: UByte? = null
    var ramImages: List<RamImageImpl>? = null
    if (hasRawData) {
        xSize = getU32()
        ySize = getU32()
        zSize = getU32()

        if (bamMinorVersion >= 30U) {
            padXSize = getU32()
            padYSize = getU32()
            padZSize = getU32()
        }

        if (bamMinorVersion >= 26U) {
            numViews = getU32()
        }

        componentType = getU8()
        componentWidth = getU8()
        rawImageCompression = getU8()
        ramImages =
            (0..<getU8().toInt()).map { RamImageImpl(getU32(), buf.readBytesToArray(getU32().toInt()).toUByteArray()) }
    }

    return TextureImpl(
        name,
        fileName,
        alphaFileName,
        primaryFileNumChannels,
        alphaFileChannel,
        textureType,
        hasReadMipmaps,
        wrapU,
        wrapV,
        wrapW,
        minFilter,
        magFilter,
        anisotropicDegree,
        borderColor,
        minLod,
        maxLod,
        lodBias,
        compression,
        qualityLevel,
        format,
        numComponents,
        usageHint,
        autoTextureScale,
        originalFileXSize,
        originalFileYSize,
        simpleRamImage,
        clearColor,
        xSize,
        ySize,
        zSize,
        padXSize,
        padYSize,
        padZSize,
        numViews,
        componentType,
        componentWidth,
        rawImageCompression,
        ramImages
    )
}