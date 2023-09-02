package com.github.bunnyi.syncmatica;

import com.google.common.annotations.VisibleForTesting;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;


public class PacketByteBuf extends ByteBuf {
    private final ByteBuf parent;

    public static final short DEFAULT_MAX_STRING_LENGTH = Short.MAX_VALUE;

    public PacketByteBuf(ByteBuf parent) {
        this.parent = parent;
    }

    @VisibleForTesting
    public byte[] getWrittenBytes() {
        int i = this.writerIndex();
        byte[] bs = new byte[i];
        this.getBytes(0, bs);
        return bs;
    }

    public Vector3f readVector3f() {
        return new Vector3f(this.readFloat(), this.readFloat(), this.readFloat());
    }

    public void writeVector3f(Vector3f vector3f) {
        this.writeFloat(vector3f.x());
        this.writeFloat(vector3f.y());
        this.writeFloat(vector3f.z());
    }

    public int readVarInt() {
        byte b;
        int i = 0;
        int j = 0;
        do {
            b = this.readByte();
            i |= (b & 0x7F) << j++ * 7;
            if (j <= 5) continue;
            throw new RuntimeException("VarInt too big");
        } while ((b & 0x80) == 128);
        return i;
    }

    public long readVarLong() {
        byte b;
        long l = 0L;
        int i = 0;
        do {
            b = this.readByte();
            l |= (long)(b & 0x7F) << i++ * 7;
            if (i <= 10) continue;
            throw new RuntimeException("VarLong too big");
        } while ((b & 0x80) == 128);
        return l;
    }


    public PacketByteBuf writeUuid(UUID uuid) {
        this.writeLong(uuid.getMostSignificantBits());
        this.writeLong(uuid.getLeastSignificantBits());
        return this;
    }


    public UUID readUuid() {
        return new UUID(this.readLong(), this.readLong());
    }


    public PacketByteBuf writeVarInt(int value) {
        while (true) {
            if ((value & 0xFFFFFF80) == 0) {
                this.writeByte(value);
                return this;
            }
            this.writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
    }


    public PacketByteBuf writeVarLong(long value) {
        while (true) {
            if ((value & 0xFFFFFFFFFFFFFF80L) == 0L) {
                this.writeByte((int)value);
                return this;
            }
            this.writeByte((int)(value & 0x7FL) | 0x80);
            value >>>= 7;
        }
    }


    public String readString() {
        return this.toString(this.readerIndex(), this.readVarInt(), StandardCharsets.UTF_8);
    }

    public PacketByteBuf writeString(String string) {
        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        this.writeVarInt(bs.length);
        this.writeBytes(bs);
        return this;
    }

    private static int toEncodedStringLength(int decodedLength) {
        return decodedLength * 3;
    }


    public Date readDate() {
        return new Date(this.readLong());
    }

    public PacketByteBuf writeDate(Date date) {
        this.writeLong(date.getTime());
        return this;
    }

    public Instant readInstant() {
        return Instant.ofEpochMilli(this.readLong());
    }

    public void writeInstant(Instant instant) {
        this.writeLong(instant.toEpochMilli());
    }

    @Override
    public int capacity() {
        return this.parent.capacity();
    }

    @Override
    public ByteBuf capacity(int capacity) {
        return this.parent.capacity(capacity);
    }

    @Override
    public int maxCapacity() {
        return this.parent.maxCapacity();
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.parent.alloc();
    }

    @Override
    public ByteOrder order() {
        return this.parent.order();
    }

    @Override
    public ByteBuf order(ByteOrder byteOrder) {
        return this.parent.order(byteOrder);
    }

    @Override
    public ByteBuf unwrap() {
        return this.parent.unwrap();
    }

    @Override
    public boolean isDirect() {
        return this.parent.isDirect();
    }

    @Override
    public boolean isReadOnly() {
        return this.parent.isReadOnly();
    }

    @Override
    public ByteBuf asReadOnly() {
        return this.parent.asReadOnly();
    }

    @Override
    public int readerIndex() {
        return this.parent.readerIndex();
    }

    @Override
    public ByteBuf readerIndex(int index) {
        return this.parent.readerIndex(index);
    }

    @Override
    public int writerIndex() {
        return this.parent.writerIndex();
    }

    @Override
    public ByteBuf writerIndex(int index) {
        return this.parent.writerIndex(index);
    }

    @Override
    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        return this.parent.setIndex(readerIndex, writerIndex);
    }

    @Override
    public int readableBytes() {
        return this.parent.readableBytes();
    }

    @Override
    public int writableBytes() {
        return this.parent.writableBytes();
    }

    @Override
    public int maxWritableBytes() {
        return this.parent.maxWritableBytes();
    }

    @Override
    public boolean isReadable() {
        return this.parent.isReadable();
    }

    @Override
    public boolean isReadable(int size) {
        return this.parent.isReadable(size);
    }

    @Override
    public boolean isWritable() {
        return this.parent.isWritable();
    }

    @Override
    public boolean isWritable(int size) {
        return this.parent.isWritable(size);
    }

    @Override
    public ByteBuf clear() {
        return this.parent.clear();
    }

    @Override
    public ByteBuf markReaderIndex() {
        return this.parent.markReaderIndex();
    }

    @Override
    public ByteBuf resetReaderIndex() {
        return this.parent.resetReaderIndex();
    }

    @Override
    public ByteBuf markWriterIndex() {
        return this.parent.markWriterIndex();
    }

    @Override
    public ByteBuf resetWriterIndex() {
        return this.parent.resetWriterIndex();
    }

    @Override
    public ByteBuf discardReadBytes() {
        return this.parent.discardReadBytes();
    }

    @Override
    public ByteBuf discardSomeReadBytes() {
        return this.parent.discardSomeReadBytes();
    }

    @Override
    public ByteBuf ensureWritable(int minBytes) {
        return this.parent.ensureWritable(minBytes);
    }

    @Override
    public int ensureWritable(int minBytes, boolean force) {
        return this.parent.ensureWritable(minBytes, force);
    }

    @Override
    public boolean getBoolean(int index) {
        return this.parent.getBoolean(index);
    }

    @Override
    public byte getByte(int index) {
        return this.parent.getByte(index);
    }

    @Override
    public short getUnsignedByte(int index) {
        return this.parent.getUnsignedByte(index);
    }

    @Override
    public short getShort(int index) {
        return this.parent.getShort(index);
    }

    @Override
    public short getShortLE(int index) {
        return this.parent.getShortLE(index);
    }

    @Override
    public int getUnsignedShort(int index) {
        return this.parent.getUnsignedShort(index);
    }

    @Override
    public int getUnsignedShortLE(int index) {
        return this.parent.getUnsignedShortLE(index);
    }

    @Override
    public int getMedium(int index) {
        return this.parent.getMedium(index);
    }

    @Override
    public int getMediumLE(int index) {
        return this.parent.getMediumLE(index);
    }

    @Override
    public int getUnsignedMedium(int index) {
        return this.parent.getUnsignedMedium(index);
    }

    @Override
    public int getUnsignedMediumLE(int index) {
        return this.parent.getUnsignedMediumLE(index);
    }

    @Override
    public int getInt(int index) {
        return this.parent.getInt(index);
    }

    @Override
    public int getIntLE(int index) {
        return this.parent.getIntLE(index);
    }

    @Override
    public long getUnsignedInt(int index) {
        return this.parent.getUnsignedInt(index);
    }

    @Override
    public long getUnsignedIntLE(int index) {
        return this.parent.getUnsignedIntLE(index);
    }

    @Override
    public long getLong(int index) {
        return this.parent.getLong(index);
    }

    @Override
    public long getLongLE(int index) {
        return this.parent.getLongLE(index);
    }

    @Override
    public char getChar(int index) {
        return this.parent.getChar(index);
    }

    @Override
    public float getFloat(int index) {
        return this.parent.getFloat(index);
    }

    @Override
    public double getDouble(int index) {
        return this.parent.getDouble(index);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf buf) {
        return this.parent.getBytes(index, buf);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf buf, int length) {
        return this.parent.getBytes(index, buf, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuf buf, int outputIndex, int length) {
        return this.parent.getBytes(index, buf, outputIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] bytes) {
        return this.parent.getBytes(index, bytes);
    }

    @Override
    public ByteBuf getBytes(int index, byte[] bytes, int outputIndex, int length) {
        return this.parent.getBytes(index, bytes, outputIndex, length);
    }

    @Override
    public ByteBuf getBytes(int index, ByteBuffer buf) {
        return this.parent.getBytes(index, buf);
    }

    @Override
    public ByteBuf getBytes(int index, OutputStream stream, int length) throws IOException {
        return this.parent.getBytes(index, stream, length);
    }

    @Override
    public int getBytes(int index, GatheringByteChannel channel, int length) throws IOException {
        return this.parent.getBytes(index, channel, length);
    }

    @Override
    public int getBytes(int index, FileChannel channel, long pos, int length) throws IOException {
        return this.parent.getBytes(index, channel, pos, length);
    }

    @Override
    public CharSequence getCharSequence(int index, int length, Charset charset) {
        return this.parent.getCharSequence(index, length, charset);
    }

    @Override
    public ByteBuf setBoolean(int index, boolean value) {
        return this.parent.setBoolean(index, value);
    }

    @Override
    public ByteBuf setByte(int index, int value) {
        return this.parent.setByte(index, value);
    }

    @Override
    public ByteBuf setShort(int index, int value) {
        return this.parent.setShort(index, value);
    }

    @Override
    public ByteBuf setShortLE(int index, int value) {
        return this.parent.setShortLE(index, value);
    }

    @Override
    public ByteBuf setMedium(int index, int value) {
        return this.parent.setMedium(index, value);
    }

    @Override
    public ByteBuf setMediumLE(int index, int value) {
        return this.parent.setMediumLE(index, value);
    }

    @Override
    public ByteBuf setInt(int index, int value) {
        return this.parent.setInt(index, value);
    }

    @Override
    public ByteBuf setIntLE(int index, int value) {
        return this.parent.setIntLE(index, value);
    }

    @Override
    public ByteBuf setLong(int index, long value) {
        return this.parent.setLong(index, value);
    }

    @Override
    public ByteBuf setLongLE(int index, long value) {
        return this.parent.setLongLE(index, value);
    }

    @Override
    public ByteBuf setChar(int index, int value) {
        return this.parent.setChar(index, value);
    }

    @Override
    public ByteBuf setFloat(int index, float value) {
        return this.parent.setFloat(index, value);
    }

    @Override
    public ByteBuf setDouble(int index, double value) {
        return this.parent.setDouble(index, value);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf buf) {
        return this.parent.setBytes(index, buf);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf buf, int length) {
        return this.parent.setBytes(index, buf, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuf buf, int sourceIndex, int length) {
        return this.parent.setBytes(index, buf, sourceIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] bytes) {
        return this.parent.setBytes(index, bytes);
    }

    @Override
    public ByteBuf setBytes(int index, byte[] bytes, int sourceIndex, int length) {
        return this.parent.setBytes(index, bytes, sourceIndex, length);
    }

    @Override
    public ByteBuf setBytes(int index, ByteBuffer buf) {
        return this.parent.setBytes(index, buf);
    }

    @Override
    public int setBytes(int index, InputStream stream, int length) throws IOException {
        return this.parent.setBytes(index, stream, length);
    }

    @Override
    public int setBytes(int index, ScatteringByteChannel channel, int length) throws IOException {
        return this.parent.setBytes(index, channel, length);
    }

    @Override
    public int setBytes(int index, FileChannel channel, long pos, int length) throws IOException {
        return this.parent.setBytes(index, channel, pos, length);
    }

    @Override
    public ByteBuf setZero(int index, int length) {
        return this.parent.setZero(index, length);
    }

    @Override
    public int setCharSequence(int index, CharSequence sequence, Charset charset) {
        return this.parent.setCharSequence(index, sequence, charset);
    }

    @Override
    public boolean readBoolean() {
        return this.parent.readBoolean();
    }

    @Override
    public byte readByte() {
        return this.parent.readByte();
    }

    @Override
    public short readUnsignedByte() {
        return this.parent.readUnsignedByte();
    }

    @Override
    public short readShort() {
        return this.parent.readShort();
    }

    @Override
    public short readShortLE() {
        return this.parent.readShortLE();
    }

    @Override
    public int readUnsignedShort() {
        return this.parent.readUnsignedShort();
    }

    @Override
    public int readUnsignedShortLE() {
        return this.parent.readUnsignedShortLE();
    }

    @Override
    public int readMedium() {
        return this.parent.readMedium();
    }

    @Override
    public int readMediumLE() {
        return this.parent.readMediumLE();
    }

    @Override
    public int readUnsignedMedium() {
        return this.parent.readUnsignedMedium();
    }

    @Override
    public int readUnsignedMediumLE() {
        return this.parent.readUnsignedMediumLE();
    }

    @Override
    public int readInt() {
        return this.parent.readInt();
    }

    @Override
    public int readIntLE() {
        return this.parent.readIntLE();
    }

    @Override
    public long readUnsignedInt() {
        return this.parent.readUnsignedInt();
    }

    @Override
    public long readUnsignedIntLE() {
        return this.parent.readUnsignedIntLE();
    }

    @Override
    public long readLong() {
        return this.parent.readLong();
    }

    @Override
    public long readLongLE() {
        return this.parent.readLongLE();
    }

    @Override
    public char readChar() {
        return this.parent.readChar();
    }

    @Override
    public float readFloat() {
        return this.parent.readFloat();
    }

    @Override
    public double readDouble() {
        return this.parent.readDouble();
    }

    @Override
    public ByteBuf readBytes(int length) {
        return this.parent.readBytes(length);
    }

    @Override
    public ByteBuf readSlice(int length) {
        return this.parent.readSlice(length);
    }

    @Override
    public ByteBuf readRetainedSlice(int length) {
        return this.parent.readRetainedSlice(length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf buf) {
        return this.parent.readBytes(buf);
    }

    @Override
    public ByteBuf readBytes(ByteBuf buf, int length) {
        return this.parent.readBytes(buf, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuf buf, int outputIndex, int length) {
        return this.parent.readBytes(buf, outputIndex, length);
    }

    @Override
    public ByteBuf readBytes(byte[] bytes) {
        return this.parent.readBytes(bytes);
    }

    @Override
    public ByteBuf readBytes(byte[] bytes, int outputIndex, int length) {
        return this.parent.readBytes(bytes, outputIndex, length);
    }

    @Override
    public ByteBuf readBytes(ByteBuffer buf) {
        return this.parent.readBytes(buf);
    }

    @Override
    public ByteBuf readBytes(OutputStream stream, int length) throws IOException {
        return this.parent.readBytes(stream, length);
    }

    @Override
    public int readBytes(GatheringByteChannel channel, int length) throws IOException {
        return this.parent.readBytes(channel, length);
    }

    @Override
    public CharSequence readCharSequence(int length, Charset charset) {
        return this.parent.readCharSequence(length, charset);
    }

    @Override
    public int readBytes(FileChannel channel, long pos, int length) throws IOException {
        return this.parent.readBytes(channel, pos, length);
    }

    @Override
    public ByteBuf skipBytes(int length) {
        return this.parent.skipBytes(length);
    }

    @Override
    public ByteBuf writeBoolean(boolean value) {
        return this.parent.writeBoolean(value);
    }

    @Override
    public ByteBuf writeByte(int value) {
        return this.parent.writeByte(value);
    }

    @Override
    public ByteBuf writeShort(int value) {
        return this.parent.writeShort(value);
    }

    @Override
    public ByteBuf writeShortLE(int value) {
        return this.parent.writeShortLE(value);
    }

    @Override
    public ByteBuf writeMedium(int value) {
        return this.parent.writeMedium(value);
    }

    @Override
    public ByteBuf writeMediumLE(int value) {
        return this.parent.writeMediumLE(value);
    }

    @Override
    public ByteBuf writeInt(int value) {
        return this.parent.writeInt(value);
    }

    @Override
    public ByteBuf writeIntLE(int value) {
        return this.parent.writeIntLE(value);
    }

    @Override
    public ByteBuf writeLong(long value) {
        return this.parent.writeLong(value);
    }

    @Override
    public ByteBuf writeLongLE(long value) {
        return this.parent.writeLongLE(value);
    }

    @Override
    public ByteBuf writeChar(int value) {
        return this.parent.writeChar(value);
    }

    @Override
    public ByteBuf writeFloat(float value) {
        return this.parent.writeFloat(value);
    }

    @Override
    public ByteBuf writeDouble(double value) {
        return this.parent.writeDouble(value);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf buf) {
        return this.parent.writeBytes(buf);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf buf, int length) {
        return this.parent.writeBytes(buf, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuf buf, int sourceIndex, int length) {
        return this.parent.writeBytes(buf, sourceIndex, length);
    }

    @Override
    public ByteBuf writeBytes(byte[] bytes) {
        return this.parent.writeBytes(bytes);
    }

    @Override
    public ByteBuf writeBytes(byte[] bytes, int sourceIndex, int length) {
        return this.parent.writeBytes(bytes, sourceIndex, length);
    }

    @Override
    public ByteBuf writeBytes(ByteBuffer buf) {
        return this.parent.writeBytes(buf);
    }

    @Override
    public int writeBytes(InputStream stream, int length) throws IOException {
        return this.parent.writeBytes(stream, length);
    }

    @Override
    public int writeBytes(ScatteringByteChannel channel, int length) throws IOException {
        return this.parent.writeBytes(channel, length);
    }

    @Override
    public int writeBytes(FileChannel channel, long pos, int length) throws IOException {
        return this.parent.writeBytes(channel, pos, length);
    }

    @Override
    public ByteBuf writeZero(int length) {
        return this.parent.writeZero(length);
    }

    @Override
    public int writeCharSequence(CharSequence sequence, Charset charset) {
        return this.parent.writeCharSequence(sequence, charset);
    }

    @Override
    public int indexOf(int from, int to, byte value) {
        return this.parent.indexOf(from, to, value);
    }

    @Override
    public int bytesBefore(byte value) {
        return this.parent.bytesBefore(value);
    }

    @Override
    public int bytesBefore(int length, byte value) {
        return this.parent.bytesBefore(length, value);
    }

    @Override
    public int bytesBefore(int index, int length, byte value) {
        return this.parent.bytesBefore(index, length, value);
    }

    @Override
    public int forEachByte(ByteProcessor byteProcessor) {
        return this.parent.forEachByte(byteProcessor);
    }

    @Override
    public int forEachByte(int index, int length, ByteProcessor byteProcessor) {
        return this.parent.forEachByte(index, length, byteProcessor);
    }

    @Override
    public int forEachByteDesc(ByteProcessor byteProcessor) {
        return this.parent.forEachByteDesc(byteProcessor);
    }

    @Override
    public int forEachByteDesc(int index, int length, ByteProcessor byteProcessor) {
        return this.parent.forEachByteDesc(index, length, byteProcessor);
    }

    @Override
    public ByteBuf copy() {
        return this.parent.copy();
    }

    @Override
    public ByteBuf copy(int index, int length) {
        return this.parent.copy(index, length);
    }

    @Override
    public ByteBuf slice() {
        return this.parent.slice();
    }

    @Override
    public ByteBuf retainedSlice() {
        return this.parent.retainedSlice();
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return this.parent.slice(index, length);
    }

    @Override
    public ByteBuf retainedSlice(int index, int length) {
        return this.parent.retainedSlice(index, length);
    }

    @Override
    public ByteBuf duplicate() {
        return this.parent.duplicate();
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return this.parent.retainedDuplicate();
    }

    @Override
    public int nioBufferCount() {
        return this.parent.nioBufferCount();
    }

    @Override
    public ByteBuffer nioBuffer() {
        return this.parent.nioBuffer();
    }

    @Override
    public ByteBuffer nioBuffer(int index, int length) {
        return this.parent.nioBuffer(index, length);
    }

    @Override
    public ByteBuffer internalNioBuffer(int index, int length) {
        return this.parent.internalNioBuffer(index, length);
    }

    @Override
    public ByteBuffer[] nioBuffers() {
        return this.parent.nioBuffers();
    }

    @Override
    public ByteBuffer[] nioBuffers(int index, int length) {
        return this.parent.nioBuffers(index, length);
    }

    @Override
    public boolean hasArray() {
        return this.parent.hasArray();
    }

    @Override
    public byte[] array() {
        return this.parent.array();
    }

    @Override
    public int arrayOffset() {
        return this.parent.arrayOffset();
    }

    @Override
    public boolean hasMemoryAddress() {
        return this.parent.hasMemoryAddress();
    }

    @Override
    public long memoryAddress() {
        return this.parent.memoryAddress();
    }

    @Override
    public String toString(Charset charset) {
        return this.parent.toString(charset);
    }

    @Override
    public String toString(int index, int length, Charset charset) {
        return this.parent.toString(index, length, charset);
    }

    @Override
    public int hashCode() {
        return this.parent.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.parent.equals(o);
    }

    @Override
    public int compareTo(ByteBuf byteBuf) {
        return this.parent.compareTo(byteBuf);
    }

    @Override
    public String toString() {
        return this.parent.toString();
    }

    @Override
    public ByteBuf retain(int i) {
        return this.parent.retain(i);
    }

    @Override
    public ByteBuf retain() {
        return this.parent.retain();
    }

    @Override
    public ByteBuf touch() {
        return this.parent.touch();
    }

    @Override
    public ByteBuf touch(Object object) {
        return this.parent.touch(object);
    }

    @Override
    public int refCnt() {
        return this.parent.refCnt();
    }

    @Override
    public boolean release() {
        return this.parent.release();
    }

    @Override
    public boolean release(int decrement) {
        return this.parent.release(decrement);
    }
}

