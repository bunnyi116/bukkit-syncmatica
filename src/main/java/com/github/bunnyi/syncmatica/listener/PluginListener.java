package com.github.bunnyi.syncmatica.listener;

import com.github.bunnyi.syncmatica.*;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PluginListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        // 接收
        {
            List<String> list = new ArrayList<>();
            for (byte b : bytes) {
                list.add(String.format("%02x", b));
            }
            String str = String.join(" ", list);
            Bukkit.getLogger().info(String.format("[接收] [%s] [%s] %s", channel, player.getName(), str));
        }
        // 发送
        {
            if (channel.equals(PacketType.FEATURE_REQUEST.toString())) {
                PacketByteBuf byteBuf = new PacketByteBuf(Unpooled.buffer());
                FeatureSet fs = new FeatureSet(Arrays.asList(Feature.values()));
                byteBuf.writeString(fs.toString());
                player.sendPluginMessage(Syncmatica.instance, PacketType.FEATURE.toString(), byteBuf.getWrittenBytes());
                List<String> list = new ArrayList<>();
                for (byte b : byteBuf.getWrittenBytes()) {
                    list.add(String.format("%02x", b));
                }
                String str = String.join(" ", list);
                Bukkit.getConsoleSender().sendMessage("发送插件消息：" + str);
            } else if (channel.equals(PacketType.REGISTER_VERSION.toString())) {
                player.sendPluginMessage(Syncmatica.instance, PacketType.FEATURE_REQUEST.toString(), new PacketByteBuf(Unpooled.buffer()).getWrittenBytes());
            } else if (channel.equals(PacketType.FEATURE.toString())) {
                 PacketByteBuf byteBuf = new PacketByteBuf(Unpooled.buffer());
                 byteBuf.writeInt(0);
                 player.sendPluginMessage(Syncmatica.instance, PacketType.CONFIRM_USER.toString(),byteBuf.getWrittenBytes());
            }
        }
    }
}
