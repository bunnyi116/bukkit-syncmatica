package com.github.bunnyi.syncmatica;

import com.github.bunnyi.syncmatica.util.SyncmaticaUtil;
import com.google.gson.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class SyncmaticaManager {
    public static final String PLACEMENTS_JSON_KEY = "placements";
    private final Map<UUID, ServerPlacement> schematics = new HashMap<>();
    private final Collection<Consumer<ServerPlacement>> consumers = new ArrayList<>();
    private final SyncmaticaContext context;

    public SyncmaticaManager(SyncmaticaContext syncmaticaContext) {
        this.context = syncmaticaContext;
    }

    public void addPlacement(ServerPlacement placement) {
        schematics.put(placement.getId(), placement);
        updateServerPlacement(placement);
    }

    public ServerPlacement getPlacement(UUID id) {
        return schematics.get(id);
    }

    public Collection<ServerPlacement> getAll() {
        return schematics.values();
    }

    public void removePlacement(ServerPlacement placement) {
        schematics.remove(placement.getId());
        updateServerPlacement(placement);
    }

    public void addServerPlacementConsumer(Consumer<ServerPlacement> consumer) {
        consumers.add(consumer);
    }

    public void removeServerPlacementConsumer(Consumer<ServerPlacement> consumer) {
        consumers.remove(consumer);
    }

    public void updateServerPlacement(ServerPlacement updated) {
        for (Consumer<ServerPlacement> consumer : consumers) {
            consumer.accept(updated);
        }
        saveServer();
    }

    public void startup() {
        loadServer();
    }

    public void shutdown() {
    }

    private void saveServer() {
        JsonObject obj = new JsonObject();
        JsonArray arr = new JsonArray();
        for (ServerPlacement p : getAll()) {
            arr.add(p.toJson());
        }
        obj.add(PLACEMENTS_JSON_KEY, arr);
        File backup = new File(context.configFolder, "placements.json.bak");
        File incoming = new File(context.configFolder, "placements.json.new");
        File current = new File(context.configFolder, "placements.json");
        try (FileWriter writer = new FileWriter(incoming)) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(obj));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        SyncmaticaUtil.backupAndReplace(backup.toPath(), current.toPath(), incoming.toPath());
    }

    private void loadServer() {
        File file = new File(context.configFolder, "placements.json");
        if (file.exists() && file.isFile() && file.canRead()) {
            JsonElement element = null;
            try {
                FileReader reader = new FileReader(file);
                element = JsonParser.parseReader(reader);
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (element == null) return;
            try {
                JsonObject obj = element.getAsJsonObject();
                if (obj == null || !obj.has(PLACEMENTS_JSON_KEY)) return;
                JsonArray array = obj.getAsJsonArray(PLACEMENTS_JSON_KEY);
                for (JsonElement element1 : array) {
                    ServerPlacement placement = ServerPlacement.fromJson(element1.getAsJsonObject(), context);
                    if (placement != null) {
                        schematics.put(placement.getId(), placement);
                    }
                }
            } catch (IllegalStateException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}
