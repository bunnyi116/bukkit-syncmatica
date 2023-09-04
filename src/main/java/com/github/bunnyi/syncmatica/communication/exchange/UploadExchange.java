package com.github.bunnyi.syncmatica.communication.exchange;

import com.github.bunnyi.syncmatica.SyncmaticaContext;
import com.github.bunnyi.syncmatica.ServerPlacement;
import com.github.bunnyi.syncmatica.communication.ExchangeTarget;
import com.github.bunnyi.syncmatica.communication.PacketType;
import com.github.bunnyi.syncmatica.util.PacketByteBuf;
import com.github.bunnyi.syncmatica.util.Identifier;

import java.io.*;

// uploading part of transmit data exchange
// pairs with Download Exchange

public class UploadExchange extends Exchange {

    // The maximum buffer size for CustomPayloadPackets is actually 32767
    // so 32768 is a bad value to send - thus adjusted it to 16384 - exactly halved
    private static final int BUFFER_SIZE = 16384;

    private final ServerPlacement toUpload;
    private final InputStream inputStream;
    private final byte[] buffer = new byte[BUFFER_SIZE];

    public UploadExchange(final ServerPlacement syncmatic, final File uploadFile, final ExchangeTarget partner, final SyncmaticaContext con) throws FileNotFoundException {
        super(partner, con);
        toUpload = syncmatic;
        inputStream = new FileInputStream(uploadFile);
    }

    public boolean checkPacket(final Identifier id, final PacketByteBuf packetBuf) {
        if (id.equals(PacketType.RECEIVED_LITEMATIC.identifier)
                || id.equals(PacketType.CANCEL_LITEMATIC.identifier)) {
            return checkUUID(packetBuf, toUpload.getId());
        }
        return false;
    }

    public void handle(final Identifier id, final PacketByteBuf packetBuf) {
        packetBuf.readUuid(); // uncertain if the data has to be consumed
        if (id.equals(PacketType.RECEIVED_LITEMATIC.identifier)) {
            send();
        }
        if (id.equals(PacketType.CANCEL_LITEMATIC.identifier)) {
            close(false);
        }
    }

    private void send() {
        // might fail when an empty file is attempted to be transmitted
        int bytesRead;
        try {
            bytesRead = inputStream.read(buffer);
        } catch (final IOException e) {
            close(true);
            e.printStackTrace();
            return;
        }
        if (bytesRead == -1) {
            sendFinish();
        } else {
            sendData(bytesRead);
        }
    }

    public void init() {
        send();
    }

    private void sendData(final int bytesRead) {
        PacketByteBuf packetByteBuf = new PacketByteBuf();
        packetByteBuf.writeUuid(toUpload.getId());
        packetByteBuf.writeInt(bytesRead);
        packetByteBuf.writeBytes(buffer, 0, bytesRead);
        partner.sendPacket(PacketType.SEND_LITEMATIC.identifier, packetByteBuf, context);
    }

    private void sendFinish() {
        final PacketByteBuf packetByteBuf = new PacketByteBuf();
        packetByteBuf.writeUuid(toUpload.getId());
        partner.sendPacket(PacketType.FINISHED_LITEMATIC.identifier, packetByteBuf, context);
        succeed();
    }

    @Override
    protected void onClose() {
        try {
            inputStream.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendCancelPacket() {
        final PacketByteBuf packetByteBuf = new PacketByteBuf();
        packetByteBuf.writeUuid(toUpload.getId());
        partner.sendPacket(PacketType.CANCEL_LITEMATIC.identifier, packetByteBuf, context);
    }

}
