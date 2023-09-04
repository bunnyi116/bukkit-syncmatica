package com.github.bunnyi.syncmatica.protocol.handshake;

import com.github.bunnyi.syncmatica.SyncmaticaPlugin;
import com.github.bunnyi.syncmatica.communication.exchange.VersionHandshakeServer;
import com.github.bunnyi.syncmatica.util.Identifier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.profile.PlayerProfile;

public class RegisterVersionChannel implements PluginMessageListener {
    public static final Identifier IDENTIFIER = new Identifier("syncmatica:register_version");
    private final SyncmaticaPlugin plugin;

    public RegisterVersionChannel(SyncmaticaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
    }
}
