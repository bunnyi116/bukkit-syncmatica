package com.github.bunnyi.syncmatica.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class PacketByteBuf {
    public static final short DEFAULT_MAX_STRING_LENGTH = Short.MAX_VALUE;
    private byte[] buffer;
    private int writeIndex;
    private int readIndex;

    public PacketByteBuf() {
        this.buffer = new byte[0];
        this.writeIndex = 0;
        this.readIndex = 0;
    }

    public PacketByteBuf(byte[] buffer) {
        this.buffer = buffer;
        this.writeIndex = buffer.length;
        this.readIndex = 0;
    }

    public byte[] toArray() {
        synchronized (this) {
            if (writeIndex != 0) {
                byte[] buffer = new byte[writeIndex];
                System.arraycopy(this.buffer, 0, buffer, 0, buffer.length);
                return buffer;
            }
            return new byte[0];
        }
    }

    public boolean isReadable() {
        return readIndex < writeIndex;
    }

    public void ensureWriteCapacity(int length) {
        synchronized (this) {
            int remainder = this.buffer.length - writeIndex;
            if (length > 0 && length > remainder) {
                int newLength = Math.max(length, 256);
                if (newLength < this.buffer.length * 2) {
                    newLength = this.buffer.length * 2;
                }
                int max = 0X7FFFF000;
                if (this.buffer.length * 2 > max) {
                    newLength = Math.max(length, max);
                }
                byte[] newBuffer = new byte[writeIndex + newLength];
                // 缓冲区有数据才有拷贝的意义
                if (this.buffer.length > 0 || writeIndex == 0) {
                    System.arraycopy(this.buffer, 0, newBuffer, 0, buffer.length);
                }
                this.buffer = newBuffer;
            }
        }
    }

    public void writeBytes(byte[] buffer, int offset, int count) {
        synchronized (this) {
            ensureWriteCapacity(count);
            System.arraycopy(buffer, offset, this.buffer, writeIndex, count);
            writeIndex += count;
        }
    }

    public void writeBytes(byte[] buffer) {
        writeBytes(buffer, 0, buffer.length);
    }

    public void writeByte(byte value) {
        writeBytes(new byte[]{value});
    }

    public void writeShort(short value) {
        byte[] buffer = new byte[2];
        buffer[0] = (byte) ((value >> 8) & 0xFF);
        buffer[1] = (byte) (value & 0xFF);
        writeBytes(buffer);
    }

    public void writeInt(int value) {
        byte[] buffer = new byte[4];
        buffer[0] = (byte) ((value >> 24) & 0xFF);
        buffer[1] = (byte) ((value >> 16) & 0xFF);
        buffer[2] = (byte) ((value >> 8) & 0xFF);
        buffer[3] = (byte) (value & 0xFF);
        writeBytes(buffer);
    }

    public void writeLong(long value) {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) ((value >> 56) & 0xFF);
        buffer[1] = (byte) ((value >> 48) & 0xFF);
        buffer[2] = (byte) ((value >> 40) & 0xFF);
        buffer[3] = (byte) ((value >> 32) & 0xFF);
        buffer[4] = (byte) ((value >> 24) & 0xFF);
        buffer[5] = (byte) ((value >> 16) & 0xFF);
        buffer[6] = (byte) ((value >> 8) & 0xFF);
        buffer[7] = (byte) (value & 0xFF);
        writeBytes(buffer);
    }

    public void writeLong(float value) {
        writeInt(Float.floatToIntBits(value));
    }

    public void writeLong(double value) {
        writeLong(Double.doubleToLongBits(value));
    }

    public void readBytes(byte[] buffer, int offset, int count) {
        synchronized (this) {
            System.arraycopy(this.buffer, readIndex, buffer, offset, count);
            readIndex += count;
        }
    }

    public byte[] readBytes(int length) {
        synchronized (this) {
            if (length > 0) {
                byte[] buffer = new byte[length];
                readBytes(buffer, 0, buffer.length);
                return buffer;
            }
            return new byte[0];
        }
    }

    public void readBytes(OutputStream outputStream, int size) throws IOException {
        byte[] bytes = readBytes(size);
        outputStream.write(bytes);
    }

    public byte readByte() {
        return readBytes(1)[0];
    }

    public short readShort() {
        short value = 0;
        value |= (short) ((readByte() << 8) & 0xff00);
        value |= (short) (readByte() & 0xff);
        return value;
    }

    public int readInt() {
        int value = (readByte() << 24) & 0xff000000;
        value |= (readByte() << 16) & 0xff0000;
        value |= (readByte() << 8) & 0xff00;
        value |= readByte() & 0xff;
        return value;
    }

    public long readLong() {
        long value = ((long) readByte() << 56) & 0xff00000000000000L;
        value |= ((long) readByte() << 48) & 0xff000000000000L;
        value |= ((long) readByte() << 40) & 0xff0000000000L;
        value |= ((long) readByte() << 32) & 0xff00000000L;
        value |= (readByte() << 24) & 0xff000000L;
        value |= (readByte() << 16) & 0xff0000L;
        value |= (readByte() << 8) & 0xff00L;
        value |= readByte() & 0xffL;
        return value;
    }


    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }


    public int readVarInt() {
        byte b;
        int i = 0;
        int j = 0;
        do {
            b = readByte();
            i |= (b & 0x7F) << j++ * 7;
            if (j <= 5) continue;
            throw new RuntimeException("VarInt too big");
        } while ((b & 0x80) == 128);
        return i;
    }

    public void writeVarInt(int value) {
        while (true) {
            if ((value & 0xFFFFFF80) == 0) {
                writeByte((byte) value);
                return;
            }
            writeByte((byte) (value & 0x7F | 0x80));
            value >>>= 7;
        }
    }

    public long readVarLong() {
        byte b;
        long l = 0L;
        int i = 0;
        do {
            b = readByte();
            l |= (long)(b & 0x7F) << i++ * 7;
            if (i <= 10) continue;
            throw new RuntimeException("VarLong too big");
        } while ((b & 0x80) == 128);
        return l;
    }

    public void writeVarLong(long value) {
        while (true) {
            if ((value & 0xFFFFFFFFFFFFFF80L) == 0L) {
                writeByte((byte) value);
                return;
            }
            writeByte((byte) ((value & 0x7FL) | 0x80));
            value >>>= 7;
        }
    }

    public void writeUuid(UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    public UUID readUuid() {
        return new UUID(readLong(), readLong());
    }

    public String readString() {
        return readString(DEFAULT_MAX_STRING_LENGTH);
    }

    public String readString(int maxLength) {
        int encodedLength = toEncodedStringLength(maxLength);
        int byteLength = readVarInt();
        if (byteLength > encodedLength) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + byteLength + " > " + encodedLength + ")");
        } else if (byteLength < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String string = new String(readBytes(byteLength), StandardCharsets.UTF_8);
            if (string.length() > maxLength) {
                int var10002 = string.length();
                throw new DecoderException("The received string length is longer than maximum allowed (" + var10002 + " > " + maxLength + ")");
            } else {
                return string;
            }
        }
    }

    public PacketByteBuf writeString(String string) {
        return writeString(string, DEFAULT_MAX_STRING_LENGTH);
    }

    public PacketByteBuf writeString(String string, int maxLength) {
        if (string.length() > maxLength) {
            int length = string.length();
            throw new EncoderException("String too big (was " + length + " characters, max " + maxLength + ")");
        } else {
            byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
            int encodedLength = toEncodedStringLength(maxLength);
            if (bytes.length > encodedLength) {
                throw new EncoderException("String too big (was " + bytes.length + " bytes encoded, max " + encodedLength + ")");
            } else {
                writeVarInt(bytes.length);
                writeBytes(bytes);
                return this;
            }
        }
    }

    private static int toEncodedStringLength(int decodedLength) {
        return decodedLength * 3;
    }

    public BlockPos readBlockPos() {
        return BlockPos.fromLong(this.readLong());
    }

    public void writeBlockPos(BlockPos position) {
        writeLong(position.asLong());
    }


    private static class DecoderException extends RuntimeException {
        public DecoderException(String message) {
            super(message);
        }
    }

    private static class EncoderException extends RuntimeException {
        public EncoderException(String message) {
            super(message);
        }

    }
}

