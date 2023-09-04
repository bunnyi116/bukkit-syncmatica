package com.github.bunnyi.syncmatica.util;

public enum BlockMirror {
    NONE("none"),
    LEFT_RIGHT("left_right"),
    FRONT_BACK("front_back");

    public final String name;

    BlockMirror(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
