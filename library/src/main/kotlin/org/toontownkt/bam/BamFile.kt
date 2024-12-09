@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class)

package org.toontownkt.bam

import kotlinx.serialization.Serializable
import org.toontownkt.bam.types.GeomVertexArrayData
import org.toontownkt.bam.types.ObjPointer
import org.toontownkt.bam.types.PandaObject
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset

public sealed interface BamFileError {
    public data object MissingBamFileMagic : Throwable("missing file magic for bam file"), BamFileError
    public data class UnsupportedBamVersion(val major: Short, val minor: Short) :
        Throwable("unsupported bam file version: ($major, $minor)"), BamFileError

    public data object UnsupportedBamEndian :
        Throwable("bam file was not in little endian format"), BamFileError
}

public sealed class BamFileOpcode(public val byte: Byte) {
    public data object Push : BamFileOpcode(0)
    public data object Pop : BamFileOpcode(1)
    public data object Adjunct : BamFileOpcode(2)
    public data object Remove : BamFileOpcode(3)
    public data object FileData : BamFileOpcode(4)
}

public fun Byte.toBamFileOpcode(): BamFileOpcode = when (this) {
    0.toByte() -> BamFileOpcode.Push
    1.toByte() -> BamFileOpcode.Pop
    2.toByte() -> BamFileOpcode.Adjunct
    3.toByte() -> BamFileOpcode.Remove
    4.toByte() -> BamFileOpcode.FileData
    else -> error("unknown bam file opcode: $this")
}

@Serializable
public data class BamFile(
    val majorVersion: UShort,
    val minorVersion: UShort,
    val littleEndian: Boolean,
    val objects: Map<UShort, PandaObject>
) {
    public companion object {
        public fun fromFile(file: File): BamFile = fromRaw(RawBamFile.fromFile(file))

        public fun fromRaw(rawBamFile: RawBamFile): BamFile {
            return BamFile(
                rawBamFile.majorVersion,
                rawBamFile.minorVersion,
                rawBamFile.littleEndian,
                rawBamFile.objectMap.entries.associate {
                    it.key to rawBamFile.instanceBamType<PandaObject>(it.key)
                }
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    public operator fun <T : PandaObject> get(ptr: ObjPointer<T>): T = objects[ptr.objectId] as T

    public inline fun <reified T : PandaObject> getInstancesOf(): Map<ObjPointer<T>, T> =
        objects.filter { (_, obj) -> obj is T }.map { (ptr, obj) -> ObjPointer<T>(ptr) to obj as T }.toMap()
}

@Serializable
public data class RawBamFile(
    val majorVersion: UShort,
    val minorVersion: UShort,
    val littleEndian: Boolean,
    val typeHandles: Map<UShort, BamType>,
    val objectMap: Map<UShort, BamObject>
) {
    public fun getType(id: UShort): BamType = typeHandles[objectMap[id]!!.handle]!!

    public companion object {
        public fun fromFile(file: File): RawBamFile = file.inputStream().use { input ->
            BamFileReader(input).toRawBamFile()
        }
    }
}

@Serializable
public data class BamType(val name: String, val parentClasses: List<UShort>)

@Serializable
public data class BamObject(val handle: UShort, val data: ByteArray) {
    override fun toString(): String {
        val formattedData = data.map {
            object {
                override fun toString(): String = it.toHexString()
            }
        }
        return "BamObject(handle=$handle, data = $formattedData)"
    }
}

private class BamFileReader(val input: InputStream) {
    var nestingLevel = -1
    val typeHandles = mutableMapOf<UShort, BamType>()
    val objectMap = mutableMapOf<UShort, BamObject>()

    fun toRawBamFile(): RawBamFile {
        if (!input.readNBytes(6).contentEquals("pbj\u0000\n\r".toByteArray(Charset.forName("ascii")))) {
            throw BamFileError.MissingBamFileMagic
        }
        val headerDatagramLen = input.readNBytes(4).asLEInt()
        val headerDatagram = input.readNBytes(headerDatagramLen).wrapToLEByteBuffer()
        val majorVersion = headerDatagram.getShort()
        val minorVersion = headerDatagram.getShort()

        if (majorVersion.toInt() != 6) throw BamFileError.UnsupportedBamVersion(
            majorVersion,
            minorVersion
        )

        val littleEndian = headerDatagram.getBool()
        if (!littleEndian) throw BamFileError.UnsupportedBamEndian

        val buf = input.readAllBytes().wrapToLEByteBuffer()

        while (buf.remaining() > 0) {
            readObjectOperation(buf.readBytes(buf.getInt()))
        }

        return RawBamFile(
            majorVersion.toUShort(),
            minorVersion.toUShort(),
            @Suppress("KotlinConstantConditions")
            littleEndian,
            typeHandles,
            objectMap
        )
    }

    fun readObjectOperation(buf: ByteBuffer) {
        val opcode = buf.getI8().toBamFileOpcode()

        when (opcode) {
            BamFileOpcode.Push -> {
                nestingLevel += 1
                readObject(buf)
            }

            BamFileOpcode.Pop -> {
                nestingLevel -= 1
            }

            BamFileOpcode.Adjunct -> {
                readObject(buf)
            }

            BamFileOpcode.Remove -> TODO("remove opcode")
            BamFileOpcode.FileData -> TODO("filedata opcode")
        }
    }

    fun readObject(buf: ByteBuffer): UShort {
        val handleId = readHandle(buf)
        val objId = buf.getU16()
        val data = buf.getRemaining()

        objectMap[objId] = BamObject(handleId, data)
        return objId
    }

    fun readHandle(buf: ByteBuffer): UShort {
        val handle = buf.getU16()
        if (handle == 0U.toUShort()) return handle

        if (!typeHandles.contains(handle)) {
            val name = readString(buf)
            val parents = buf.getI8()
            val parentClasses = mutableListOf<UShort>()
            for (i in 0..<parents) {
                parentClasses += readHandle(buf)
            }

            typeHandles[handle] = BamType(name, parentClasses)
        }

        return handle
    }

    fun readString(buf: ByteBuffer): String {
        val len = buf.getShort()
        val stringBuf = buf.readBytesToArray(len.toInt())
        return stringBuf.decodeToString()
    }
}
