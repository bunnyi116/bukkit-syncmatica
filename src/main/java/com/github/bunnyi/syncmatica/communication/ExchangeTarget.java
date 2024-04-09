package com.github.bunnyi.syncmatica.communication;

import com.github.bunnyi.syncmatica.SyncmaticaContext;
import com.github.bunnyi.syncmatica.SyncmaticaPlugin;
import com.github.bunnyi.syncmatica.communication.exchange.Exchange;
import com.github.bunnyi.syncmatica.util.Identifier;
import com.github.bunnyi.syncmatica.util.PacketByteBuf;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ExchangeTarget {
    public SyncmaticaPlugin plugin;
    public Player player;
    public final String persistentName;
    public FeatureSet features;
    public final List<Exchange> ongoingExchanges = new ArrayList<>();

    public ExchangeTarget(SyncmaticaPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.persistentName = player.getUniqueId().toString();
    }

    public void sendPacket(Identifier identifier, PacketByteBuf packetBuf, SyncmaticaContext context) {
        context.debugService.logSendPacket(identifier, persistentName);
        byte[] data = packetBuf.toArray();
        // Bukkit.getLogger().info(String.format("[发送] [%s]: %s", identifier.toString(), StringTools.getHexString(data)));
        player.sendPluginMessage(plugin, identifier.toString(), data);
    }

    public FeatureSet getFeatureSet() {
        return features;
    }

    public void setFeatureSet(final FeatureSet f) {
        features = f;
    }

    public Collection<Exchange> getExchanges() {
        return ongoingExchanges;
    }

    public String getPersistentName() {
        return persistentName;
    }
}
