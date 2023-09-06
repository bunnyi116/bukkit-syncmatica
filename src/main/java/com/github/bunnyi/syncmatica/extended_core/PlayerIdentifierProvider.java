package com.github.bunnyi.syncmatica.extended_core;

import com.github.bunnyi.syncmatica.SyncmaticaContext;
import com.github.bunnyi.syncmatica.communication.ExchangeTarget;
import com.github.bunnyi.syncmatica.communication.ServerCommunicationManager;
import com.google.gson.JsonObject;
import org.bukkit.profile.PlayerProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerIdentifierProvider {
    private final Map<UUID, PlayerIdentifier> identifiers = new HashMap<>();
    private final SyncmaticaContext context;

    public PlayerIdentifierProvider(SyncmaticaContext context) {
        this.context = context;
        identifiers.put(PlayerIdentifier.MISSING_PLAYER_UUID, PlayerIdentifier.MISSING_PLAYER);
    }

    public PlayerIdentifier createOrGet(ExchangeTarget exchangeTarget) {
        ServerCommunicationManager profileProvider = (ServerCommunicationManager) context.communicationManager;
        return createOrGet(profileProvider.getPlayerProfile(exchangeTarget));
    }

    public PlayerIdentifier createOrGet(PlayerProfile gameProfile) {
        return createOrGet(gameProfile.getUniqueId(), gameProfile.getName());
    }

    public PlayerIdentifier createOrGet(UUID uuid, String playerName) {
        return identifiers.computeIfAbsent(uuid, id -> new PlayerIdentifier(uuid, playerName));
    }

    public void updateName(UUID uuid, String playerName) {
        createOrGet(uuid, playerName).updatePlayerName(playerName);
    }

    public PlayerIdentifier fromJson(JsonObject obj) {
        if (obj.has("uuid") && obj.has("name")) {
            UUID jsonUUID = UUID.fromString(obj.get("uuid").getAsString());
            return identifiers.computeIfAbsent(jsonUUID, key -> new PlayerIdentifier(jsonUUID, obj.get("name").getAsString()));
        }
        return PlayerIdentifier.MISSING_PLAYER;
    }
}
