package org.toontownkt.bam

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

internal fun ByteBuffer.getLengthPrefixedString(): String =
    readBytesToArray(getShort().toInt()).toString(Charset.forName("ascii"))


@PublishedApi
internal fun ByteArray.wrapToLEByteBuffer(): ByteBuffer = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
internal fun ByteArray.asLEInt(): Int = wrapToLEByteBuffer().getInt()

internal fun ByteBuffer.readBytes(len: Int): ByteBuffer = readBytesToArray(len).wrapToLEByteBuffer()
internal fun ByteBuffer.readBytesToArray(len: Int): ByteArray = ByteArray(len).also { get(it) }

internal fun ByteBuffer.getI8(): Byte = get()
internal fun ByteBuffer.getU8(): UByte = getI8().toUByte()

internal fun ByteBuffer.getI16(): Short = getShort()

@PublishedApi
internal fun ByteBuffer.getU16(): UShort = getI16().toUShort()

internal fun ByteBuffer.getI32(): Int = getInt()
internal fun ByteBuffer.getU32(): UInt = getI32().toUInt()

internal fun ByteBuffer.getI64(): Long = getLong()
internal fun ByteBuffer.getU64(): ULong = getI64().toULong()

internal fun ByteBuffer.getBool(): Boolean = getI8().toInt() == 1

internal fun ByteBuffer.getRemaining(): ByteArray = readBytesToArray(remaining())