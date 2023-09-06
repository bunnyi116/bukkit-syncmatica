package com.github.bunnyi.syncmatica.communication.exchange;

import com.github.bunnyi.syncmatica.ServerPlacement;
import com.github.bunnyi.syncmatica.SyncmaticaContext;
import com.github.bunnyi.syncmatica.SyncmaticaPlugin;
import com.github.bunnyi.syncmatica.communication.ExchangeTarget;
import com.github.bunnyi.syncmatica.communication.FeatureSet;
import com.github.bunnyi.syncmatica.communication.PacketType;
import com.github.bunnyi.syncmatica.util.Identifier;
import com.github.bunnyi.syncmatica.util.PacketByteBuf;
import org.apache.logging.log4j.LogManager;

import java.util.Collection;

public class VersionHandshakeServer extends FeatureExchange {

    private String partnerVersion;

    public VersionHandshakeServer(ExchangeTarget partner, SyncmaticaContext con) {
        super(partner, con);
    }

    @Override
    public boolean checkPacket(Identifier id, PacketByteBuf packetBuf) {
        return id.equals(PacketType.REGISTER_VERSION.identifier) || super.checkPacket(id, packetBuf);
    }

    @Override
    public void handle(Identifier id, PacketByteBuf packetBuf) {
        if (id.equals(PacketType.REGISTER_VERSION.identifier)) {
            partnerVersion = packetBuf.readString(32767);
            if (!context.checkPartnerVersion(partnerVersion)) {
                LogManager.getLogger(VersionHandshakeServer.class).info("Denying syncmatica join due to outdated client with local version {} and client version {}", SyncmaticaPlugin.VERSION, partnerVersion);
                close(false);
                return;
            }
            FeatureSet fs = FeatureSet.fromVersionString(partnerVersion);
            if (fs == null) {
                requestFeatureSet();
            } else {
                partner.setFeatureSet(fs);
                onFeatureSetReceive();
            }
        } else {
            super.handle(id, packetBuf);
        }
    }

    @Override
    public void onFeatureSetReceive() {
        LogManager.getLogger(VersionHandshakeServer.class).info("Syncmatica client joining with local version {} and client version {}", SyncmaticaPlugin.VERSION, partnerVersion);
        PacketByteBuf newBuf = new PacketByteBuf();
        Collection<ServerPlacement> collection = context.syncmaticaManager.getAll();
        newBuf.writeInt(collection.size());
        for (ServerPlacement placement : collection) {
            getManager().putMetaData(placement, newBuf, partner);
        }
        partner.sendPacket(PacketType.CONFIRM_USER.identifier, newBuf, context);
        succeed();
    }

    public void init() {
        PacketByteBuf newBuf = new PacketByteBuf();
        newBuf.writeString(SyncmaticaPlugin.VERSION);
        partner.sendPacket(PacketType.REGISTER_VERSION.identifier, newBuf, context);
    }
}
