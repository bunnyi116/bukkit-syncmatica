package com.github.bunnyi.syncmatica.protocol;

import com.github.bunnyi.syncmatica.SyncmaticaPlugin;
import com.github.bunnyi.syncmatica.communication.PacketType;
import com.github.bunnyi.syncmatica.protocol.handshake.RegisterVersionChannel;
import com.github.bunnyi.syncmatica.util.Identifier;
import org.bukkit.Bukkit;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nullable;

public class PluginChannelManager {
    private final SyncmaticaPlugin plugin;

    public PluginChannelManager(SyncmaticaPlugin plugin) {
        this.plugin = plugin;
    }

    private void registerPluginChannels() {
        registerPluginChannel(RegisterVersionChannel.IDENTIFIER, new RegisterVersionChannel(plugin));
    }

    private void registerPluginChannel(Identifier identifier, @Nullable PluginMessageListener pluginMessageListener) {
        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, identifier.toString());
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, identifier.toString());
        if (pluginMessageListener != null) {
            Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, identifier.toString());
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, identifier.toString(), pluginMessageListener);
        }
    }
}
