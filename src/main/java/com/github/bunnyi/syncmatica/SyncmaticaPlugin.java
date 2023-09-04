package com.github.bunnyi.syncmatica;

import com.github.bunnyi.syncmatica.communication.ExchangeTarget;
import com.github.bunnyi.syncmatica.communication.PacketType;
import com.github.bunnyi.syncmatica.util.Identifier;
import com.github.bunnyi.syncmatica.util.PacketByteBuf;
import com.github.bunnyi.syncmatica.util.StringTools;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;


public final class SyncmaticaPlugin extends JavaPlugin implements Listener, TabExecutor, PluginMessageListener {
    public static final String VERSION = "0.3.10";
    public static final String MOD_ID = "syncmatica";

    public SyncmaticaContext context;

    @Override
    public void onEnable() {
        registerPluginChannels();
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("test")).setExecutor(this);
        context = new SyncmaticaContext(getDataFolder());
        Bukkit.getConsoleSender().sendMessage("启用成功！");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("禁用成功!");
    }


    private void registerPluginChannels() {
        Messenger messenger = Bukkit.getServer().getMessenger();
        // 取消插件注册的所有通道, 避免插件被重载重复注册
        messenger.unregisterIncomingPluginChannel(this);
        messenger.unregisterOutgoingPluginChannel(this);
        for (PacketType packetType : PacketType.values()) {
            registerPluginChannel(packetType.identifier, this);
        }
    }


    private void registerPluginChannel(Identifier identifier, @Nullable PluginMessageListener pluginMessageListener) {
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, identifier.toString());
        if (pluginMessageListener != null) {
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, identifier.toString(), pluginMessageListener);
        }
    }

    @EventHandler
    public void onPlayerRegisterChannelEvent(PlayerRegisterChannelEvent event) {
        Bukkit.getLogger().info(String.format("[%s] [注册通道事件]：%s", event.getPlayer().getName(), event.getChannel()));
        if (event.getChannel().equals(PacketType.REGISTER_VERSION.toString())) {
            context.communicationManager.onPlayerJoin(getExchangeTarget(event.getPlayer()), event.getPlayer());
        }
    }

    public ExchangeTarget getExchangeTarget(Player player) {
        ExchangeTarget exchangeTarget = context.communicationManager.getPlayerExchangeTarget(player);
        if (exchangeTarget == null) {
            exchangeTarget = new ExchangeTarget(this, player);
        }
        return exchangeTarget;
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerLoginEvent event) {
        context.communicationManager.onPlayerLeave(getExchangeTarget(event.getPlayer()), event.getPlayer());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        Bukkit.getLogger().info(String.format("[接收] [%s] [%s] %s", channel, player.getName(), StringTools.getHexString(bytes)));
        PacketByteBuf packetByteBuf = new PacketByteBuf(bytes);
        Identifier id = new Identifier(channel);
        if (PacketType.containsIdentifier(id)) {
            context.communicationManager.onPacket(getExchangeTarget(player), id, packetByteBuf);
        }
    }
}
