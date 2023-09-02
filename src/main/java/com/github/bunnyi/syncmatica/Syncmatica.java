package com.github.bunnyi.syncmatica;

import com.github.bunnyi.syncmatica.listener.*;

import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public final class Syncmatica extends JavaPlugin implements Listener, TabExecutor {
    public static Syncmatica instance;
    public final static HashMap<Player, Long> joinPlayers = new HashMap<>();
    public final static List<Player> syncmaticaPlayer = new ArrayList<>();
    public static final String VERSION = "0.3.10";



    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getCommand("test").setExecutor(this);

        Messenger messenger = Bukkit.getServer().getMessenger();
        // 取消插件注册的所有通道, 避免插件被重载重复注册
        messenger.unregisterIncomingPluginChannel(this);
        messenger.unregisterOutgoingPluginChannel(this);
        for (PacketType packetType : PacketType.values()) {
            registerPluginChannel(packetType, new PluginListener());
        }
        Bukkit.getConsoleSender().sendMessage("启用成功！");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("禁用成功!");
    }


    private void registerPluginChannel(PacketType packetType, @Nullable PluginMessageListener pluginMessageListener) {
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, packetType.toString());
        if (pluginMessageListener != null) {
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, packetType.toString(), pluginMessageListener);
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        joinPlayers.put(event.getPlayer(), System.currentTimeMillis());
        Bukkit.getLogger().info("玩家加入事件：" + event.getJoinMessage());
    }

    @EventHandler
    public void onPlayerRegisterChannelEvent(PlayerRegisterChannelEvent event) {
        if (event.getChannel().equals(PacketType.REGISTER_VERSION.toString())){
            PacketByteBuf byteBuf = new PacketByteBuf(Unpooled.buffer(30));
            byteBuf.writeString(VERSION);
            event.getPlayer().sendPluginMessage(this, PacketType.REGISTER_VERSION.toString(), byteBuf.getWrittenBytes());
            List<String> list = new ArrayList<>();
            for (byte b : byteBuf.getWrittenBytes()) {
                list.add(String.format("%02x", b));
            }
            String str = String.join(" ", list);
            Bukkit.getConsoleSender().sendMessage("发送插件消息：" + str);
        }
        Bukkit.getLogger().info("注册通道：" + event.getChannel());
    }
}
