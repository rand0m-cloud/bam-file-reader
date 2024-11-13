package org.toontownkt.bam

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

public fun ByteBuffer.getLengthPrefixedString(): String =
    readBytesToArray(getShort().toInt()).toString(Charset.forName("ascii"))


public fun ByteArray.wrapToLEByteBuffer(): ByteBuffer = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
public fun ByteArray.asLEInt(): Int = wrapToLEByteBuffer().getInt()

public fun ByteBuffer.readBytes(len: Int): ByteBuffer = readBytesToArray(len).wrapToLEByteBuffer()
public fun ByteBuffer.readBytesToArray(len: Int): ByteArray = ByteArray(len).also { get(it) }

public fun ByteBuffer.getI8(): Byte = get()
public fun ByteBuffer.getU8(): UByte = getI8().toUByte()

public fun ByteBuffer.getI16(): Short = getShort()
public fun ByteBuffer.getU16(): UShort = getI16().toUShort()

public fun ByteBuffer.getI32(): Int = getInt()
public fun ByteBuffer.getU32(): UInt = getI32().toUInt()

public fun ByteBuffer.getI64(): Long = getLong()
public fun ByteBuffer.getU64(): ULong = getI64().toULong()

public fun ByteBuffer.getBool(): Boolean = getI8().toInt() == 1

public fun ByteBuffer.getRemaining(): ByteArray = readBytesToArray(remaining())