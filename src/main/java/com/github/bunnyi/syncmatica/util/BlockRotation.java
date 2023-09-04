package com.github.bunnyi.syncmatica.util;

public enum BlockRotation {
    NONE("none"),
    CLOCKWISE_90("clockwise_90"),
    CLOCKWISE_180("180"),
    COUNTERCLOCKWISE_90("counterclockwise_90");

    public final String name;

    BlockRotation(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
