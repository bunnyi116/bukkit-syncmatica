package com.github.bunnyi.syncmatica;

import com.github.bunnyi.syncmatica.util.BlockPos;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class ServerPosition {
    private final BlockPos position;    // 原理图所在位置
    private final String dimensionId;   // 原理图所在维度

    public ServerPosition(final BlockPos pos, final String dim) {
        position = pos;
        dimensionId = dim;
    }

    public BlockPos getBlockPosition() {
        return position;
    }

    public String getDimensionId() {
        return dimensionId;
    }

    public static ServerPosition fromJson(JsonObject obj) {
        if (obj.has("position") && obj.has("dimension")) {
            int x;
            int y;
            int z;
            JsonArray arr = obj.get("position").getAsJsonArray();
            x = arr.get(0).getAsInt();
            y = arr.get(1).getAsInt();
            z = arr.get(2).getAsInt();
            final BlockPos pos = new BlockPos(x, y, z);
            return new ServerPosition(pos, obj.get("dimension").getAsString());
        }
        return null;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(position.x));
        arr.add(new JsonPrimitive(position.y));
        arr.add(new JsonPrimitive(position.z));
        obj.add("position", arr);
        obj.add("dimension", new JsonPrimitive(dimensionId));
        return obj;
    }
}
