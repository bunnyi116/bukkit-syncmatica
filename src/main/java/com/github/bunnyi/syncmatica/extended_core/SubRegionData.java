package com.github.bunnyi.syncmatica.extended_core;

import com.github.bunnyi.syncmatica.util.BlockMirror;
import com.github.bunnyi.syncmatica.util.BlockPos;
import com.github.bunnyi.syncmatica.util.BlockRotation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

public class SubRegionData {
    private boolean isModified;
    private Map<String, SubRegionPlacementModification> modificationData; // is null when isModified is false

    public SubRegionData() {
        this(false, null);
    }

    public SubRegionData(boolean isModified, Map<String, SubRegionPlacementModification> modificationData) {
        this.isModified = isModified;
        this.modificationData = modificationData;
    }

    public void reset() {
        isModified = false;
        modificationData = null;
    }

    public static SubRegionData fromJson(JsonElement obj) {
        SubRegionData newSubRegionData = new SubRegionData();
        newSubRegionData.isModified = true;
        for (JsonElement modification : obj.getAsJsonArray()) {
            newSubRegionData.modify(SubRegionPlacementModification.fromJson(modification.getAsJsonObject()));
        }
        return newSubRegionData;
    }

    public void modify(final SubRegionPlacementModification subRegionPlacementModification) {
        if (subRegionPlacementModification == null) {
            return;
        }
        isModified = true;
        if (modificationData == null) {
            modificationData = new HashMap<>();
        }
        modificationData.put(subRegionPlacementModification.name, subRegionPlacementModification);
    }

    public boolean isModified() {
        return isModified;
    }

    public Map<String, SubRegionPlacementModification> getModificationData() {
        return modificationData;
    }

    public JsonElement toJson() {

        return modificationDataToJson();
    }

    public void modify(String name, BlockPos position, BlockRotation rotation, BlockMirror mirror) {
        modify(new SubRegionPlacementModification(name, position, rotation, mirror));
    }

    private JsonElement modificationDataToJson() {
        JsonArray arr = new JsonArray();
        for (Map.Entry<String, SubRegionPlacementModification> entry : modificationData.entrySet()) {
            arr.add(entry.getValue().toJson());
        }
        return arr;
    }

    @Override
    public String toString() {
        if (!isModified) {
            return "[]";
        }
        return modificationData == null ? "[ERROR:null]" : modificationData.toString();
    }
}
