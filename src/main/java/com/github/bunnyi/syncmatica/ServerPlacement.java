package com.github.bunnyi.syncmatica;

import com.github.bunnyi.syncmatica.extended_core.PlayerIdentifier;
import com.github.bunnyi.syncmatica.extended_core.SubRegionData;
import com.github.bunnyi.syncmatica.material.SyncmaticaMaterialList;
import com.github.bunnyi.syncmatica.util.BlockMirror;
import com.github.bunnyi.syncmatica.util.BlockPos;
import com.github.bunnyi.syncmatica.util.BlockRotation;
import com.github.bunnyi.syncmatica.util.SyncmaticaUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

public class ServerPlacement {
    private final UUID id;                                      // 对象标识
    private final String fileName;                              // 原理图文件名称
    private final UUID hashValue;                               // 原理图文件哈希值UUID
    private PlayerIdentifier owner;                             // 分享它的玩家
    private PlayerIdentifier lastModifiedBy;                    // 上次修改的玩家
    private ServerPosition origin;                              // 原理图在服务器放置的位置
    private BlockRotation rotation;                             // 原理图旋转
    private BlockMirror mirror;                                 // 原理图镜像
    private SubRegionData subRegionData = new SubRegionData();  // 子区域数据
    private SyncmaticaMaterialList matList;                     // 材料列表

    public ServerPlacement(UUID id, String fileName, UUID hashValue, PlayerIdentifier owner) {
        this.id = id;
        this.fileName = fileName;
        this.hashValue = hashValue;
        this.owner = owner;
        this.lastModifiedBy = owner;
    }

    public ServerPlacement(UUID id, File file, PlayerIdentifier owner) {
        this(id, removeExtension(file), generateHash(file), owner);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return fileName;
    }

    public UUID getHash() {
        return hashValue;
    }

    public String getDimension() {
        return origin.getDimensionId();
    }

    public BlockPos getPosition() {
        return origin.getBlockPosition();
    }

    public ServerPosition getOrigin() {
        return origin;
    }

    public BlockRotation getRotation() {
        return rotation;
    }

    public BlockMirror getMirror() {
        return mirror;
    }

    private static String removeExtension(File file) {
        String fileName = file.getName();
        int pos = fileName.lastIndexOf(".");
        return fileName.substring(0, pos);
    }

    private static UUID generateHash(File file) {
        UUID hash;
        try {
            hash = SyncmaticaUtil.createChecksum(Files.newInputStream(file.toPath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return hash;
    }

    public PlayerIdentifier getOwner() {
        return owner;
    }

    public static ServerPlacement fromJson(JsonObject obj, SyncmaticaContext context) {
        if (obj.has("id")
                && obj.has("file_name")
                && obj.has("hash")
                && obj.has("origin")
                && obj.has("rotation")
                && obj.has("mirror")) {

            UUID id = UUID.fromString(obj.get("id").getAsString());
            String name = obj.get("file_name").getAsString();
            UUID hashValue = UUID.fromString(obj.get("hash").getAsString());
            PlayerIdentifier owner = PlayerIdentifier.MISSING_PLAYER;
            if (obj.has("owner")) {
                owner = context.playerIdentifierProvider.fromJson(obj.get("owner").getAsJsonObject());
            }
            ServerPlacement newPlacement = new ServerPlacement(id, name, hashValue, owner);
            ServerPosition pos = ServerPosition.fromJson(obj.get("origin").getAsJsonObject());
            if (pos == null) {
                return null;
            }
            newPlacement.origin = pos;
            newPlacement.rotation = BlockRotation.valueOf(obj.get("rotation").getAsString());
            newPlacement.mirror = BlockMirror.valueOf(obj.get("mirror").getAsString());
            if (obj.has("lastModifiedBy")) {
                newPlacement.lastModifiedBy = context.playerIdentifierProvider.fromJson(obj.get("lastModifiedBy").getAsJsonObject());
            } else {
                newPlacement.lastModifiedBy = owner;
            }
            if (obj.has("subregionData")) {
                newPlacement.subRegionData = SubRegionData.fromJson(obj.get("subregionData"));
            }
            return newPlacement;
        }
        return null;
    }

    public PlayerIdentifier getLastModifiedBy() {
        return lastModifiedBy;
    }

    public ServerPlacement move(String dimensionId, BlockPos origin, BlockRotation rotation, BlockMirror mirror) {
        move(new ServerPosition(origin, dimensionId), rotation, mirror);
        return this;
    }

    public SubRegionData getSubRegionData() {
        return subRegionData;
    }

    public SyncmaticaMaterialList getMaterialList() {
        return matList;
    }

    public ServerPlacement move(ServerPosition origin, BlockRotation rotation, BlockMirror mirror) {
        this.origin = origin;
        this.rotation = rotation;
        this.mirror = mirror;
        return this;
    }

    public void setOwner(PlayerIdentifier playerIdentifier) {
        owner = playerIdentifier;
    }

    public void setLastModifiedBy(PlayerIdentifier lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public ServerPlacement setMaterialList(SyncmaticaMaterialList matList) {
        if (this.matList != null) {
            this.matList = matList;
        }
        return this;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.add("id", new JsonPrimitive(id.toString()));
        obj.add("file_name", new JsonPrimitive(fileName));
        obj.add("hash", new JsonPrimitive(hashValue.toString()));
        obj.add("origin", origin.toJson());
        obj.add("rotation", new JsonPrimitive(rotation.name()));
        obj.add("mirror", new JsonPrimitive(mirror.name()));
        obj.add("owner", owner.toJson());
        if (!owner.equals(lastModifiedBy)) {
            obj.add("lastModifiedBy", lastModifiedBy.toJson());
        }
        if (subRegionData.isModified()) {
            obj.add("subregionData", subRegionData.toJson());
        }
        return obj;
    }
}
