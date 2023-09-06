package com.github.bunnyi.syncmatica.communication.exchange;

import com.github.bunnyi.syncmatica.SyncmaticaContext;
import com.github.bunnyi.syncmatica.communication.ExchangeTarget;
import com.github.bunnyi.syncmatica.communication.ServerCommunicationManager;
import com.github.bunnyi.syncmatica.util.Identifier;
import com.github.bunnyi.syncmatica.util.PacketByteBuf;

import java.util.UUID;

public abstract class Exchange {
    public final ExchangeTarget partner;
    public final SyncmaticaContext context;
    private boolean success = false;
    private boolean finished = false;

    public Exchange(ExchangeTarget partner, SyncmaticaContext con) {
        this.partner = partner;
        this.context = con;
    }

    public abstract boolean checkPacket(Identifier id, PacketByteBuf packetBuf);

    public abstract void handle(Identifier id, PacketByteBuf packetBuf);
    public void init() {
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isSuccessful() {
        return success;
    }

    public void close(boolean notifyPartner) {
        finished = true;
        success = false;
        onClose();
        if (notifyPartner) {
            sendCancelPacket();
        }
    }

    public ServerCommunicationManager getManager() {
        return context.communicationManager;
    }

    public void sendCancelPacket() {
    }

    protected void onClose() {
    }

    protected void succeed() {
        finished = true;
        success = true;
        onClose();
    }

    public static boolean checkUUID(PacketByteBuf sourceBuf, UUID targetId) {
        UUID sourceId = sourceBuf.readUuid();
        return sourceId.equals(targetId);
    }
}
