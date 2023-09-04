package com.github.bunnyi.syncmatica.util;

import java.util.Objects;

public class BlockPos {
    public int x;
    public int y;
    public int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockPos fromLong(long value) {
        int x = (int) (value >> 38);
        int y = (int) (value << 52 >> 52);
        int z = (int) (value << 26 >> 38);
        return new BlockPos(x, y, z);
    }

    public long asLong() {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }
    @Override
    public String toString() {
        return String.format("[%s, %s, %s]", x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPos blockPos = (BlockPos) o;
        return x == blockPos.x && y == blockPos.y && z == blockPos.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
