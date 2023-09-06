package com.github.bunnyi.syncmatica.communication.exchange;

import com.github.bunnyi.syncmatica.SyncmaticaContext;
import com.github.bunnyi.syncmatica.communication.ExchangeTarget;
import com.github.bunnyi.syncmatica.communication.FeatureSet;
import com.github.bunnyi.syncmatica.communication.PacketType;
import com.github.bunnyi.syncmatica.util.Identifier;
import com.github.bunnyi.syncmatica.util.PacketByteBuf;

public abstract class FeatureExchange extends Exchange {

    protected FeatureExchange(ExchangeTarget partner, SyncmaticaContext con) {
        super(partner, con);
    }

    public boolean checkPacket(Identifier id, PacketByteBuf packetBuf) {
        return id.equals(PacketType.FEATURE_REQUEST.identifier)
                || id.equals(PacketType.FEATURE.identifier);
    }

    public void handle(final Identifier id, final PacketByteBuf packetBuf) {
        if (id.equals(PacketType.FEATURE_REQUEST.identifier)) {
            sendFeatures();
        } else if (id.equals(PacketType.FEATURE.identifier)) {
            FeatureSet fs = FeatureSet.fromString(packetBuf.readString());
            partner.setFeatureSet(fs);
            onFeatureSetReceive();
        }
    }

    protected void onFeatureSetReceive() {
        succeed();
    }

    public void requestFeatureSet() {
        partner.sendPacket(PacketType.FEATURE_REQUEST.identifier, new PacketByteBuf(), context);
    }

    private void sendFeatures() {
        PacketByteBuf buf = new PacketByteBuf();
        FeatureSet fs = context.featureSet;
        buf.writeString(fs.toString());
        partner.sendPacket(PacketType.FEATURE.identifier, buf, context);
    }
}
