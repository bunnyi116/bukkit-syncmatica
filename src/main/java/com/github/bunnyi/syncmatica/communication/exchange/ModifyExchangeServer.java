package com.github.bunnyi.syncmatica.communication.exchange;

import com.github.bunnyi.syncmatica.ServerPlacement;
import com.github.bunnyi.syncmatica.SyncmaticaContext;
import com.github.bunnyi.syncmatica.communication.ExchangeTarget;
import com.github.bunnyi.syncmatica.communication.PacketType;
import com.github.bunnyi.syncmatica.extended_core.PlayerIdentifier;
import com.github.bunnyi.syncmatica.util.Identifier;
import com.github.bunnyi.syncmatica.util.PacketByteBuf;

import java.util.UUID;

public class ModifyExchangeServer extends Exchange {

    private final ServerPlacement placement;
    private final UUID placementId;

    public ModifyExchangeServer(UUID placeId, ExchangeTarget partner, SyncmaticaContext con) {
        super(partner, con);
        this.placementId = placeId;
        this.placement = con.syncmaticaManager.getPlacement(placementId);
    }

    public boolean checkPacket(Identifier id, PacketByteBuf packetBuf) {
        return id.equals(PacketType.MODIFY_FINISH.identifier) && checkUUID(packetBuf, placement.getId());
    }

    public void handle(Identifier id, PacketByteBuf packetBuf) {
        packetBuf.readUuid(); // 跳过UUID
        if (id.equals(PacketType.MODIFY_FINISH.identifier)) {
            context.communicationManager.receivePositionData(placement, packetBuf, partner);
            PlayerIdentifier identifier = context.playerIdentifierProvider.createOrGet(partner);
            placement.setLastModifiedBy(identifier);
            context.syncmaticaManager.updateServerPlacement(placement);
            succeed();
        }
    }

    public void init() {
        if (getPlacement() == null || context.communicationManager.getModifier(placement) != null) {
            close(true); // 相当于拒绝
        } else {
            accept();
        }
    }

    private void accept() {
        PacketByteBuf buf = new PacketByteBuf();
        buf.writeUuid(placement.getId());
        partner.sendPacket(PacketType.MODIFY_REQUEST_ACCEPT.identifier, buf, context);
        context.communicationManager.setModifier(placement, this);
    }

    @Override
    public void sendCancelPacket() {
        PacketByteBuf buf = new PacketByteBuf();
        buf.writeUuid(placementId);
        partner.sendPacket(PacketType.MODIFY_REQUEST_DENY.identifier, buf, context);
    }

    public ServerPlacement getPlacement() {
        return placement;
    }

    @Override
    protected void onClose() {
        if (context.communicationManager.getModifier(placement) == this) {
            context.communicationManager.setModifier(placement, null);
        }
    }
}
