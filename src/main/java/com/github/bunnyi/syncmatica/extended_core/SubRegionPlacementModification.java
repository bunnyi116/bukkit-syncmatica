package com.github.bunnyi.syncmatica.extended_core;

import com.github.bunnyi.syncmatica.util.BlockMirror;
import com.github.bunnyi.syncmatica.util.BlockPos;
import com.github.bunnyi.syncmatica.util.BlockRotation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class SubRegionPlacementModification {
    public final String name;               // 子区域名称
    public final BlockPos position;         // 子区域位置
    public final BlockRotation rotation;    // 子区域旋转
    public final BlockMirror mirror;        // 子区域镜像

    SubRegionPlacementModification(String name, BlockPos position, BlockRotation rotation, BlockMirror mirror) {
        this.name = name;
        this.position = position;
        this.rotation = rotation;
        this.mirror = mirror;
    }

    public static SubRegionPlacementModification fromJson(JsonObject obj) {
        if (!obj.has("name") || !obj.has("position") || !obj.has("rotation") || !obj.has("mirror")) {
            return null;
        }
        String name = obj.get("name").getAsString();
        JsonArray arr = obj.get("position").getAsJsonArray();
        if (arr.size() != 3) {
            return null;
        }
        BlockPos position = new BlockPos(
                arr.get(0).getAsInt(),
                arr.get(1).getAsInt(),
                arr.get(2).getAsInt()
        );
        BlockRotation rotation = BlockRotation.valueOf(obj.get("rotation").getAsString());
        BlockMirror mirror = BlockMirror.valueOf(obj.get("mirror").getAsString());
        return new SubRegionPlacementModification(name, position, rotation, mirror);
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(position.x);
        arr.add(position.y);
        arr.add(position.z);
        obj.add("position", arr);
        obj.add("name", new JsonPrimitive(name));
        obj.add("rotation", new JsonPrimitive(rotation.name()));
        obj.add("mirror", new JsonPrimitive(mirror.name()));
        return obj;
    }

    @Override
    public String toString() {
        return String.format("[name=%s, position=%s, rotation=%s, mirror=%s]", name, position, rotation, mirror);
    }
}
