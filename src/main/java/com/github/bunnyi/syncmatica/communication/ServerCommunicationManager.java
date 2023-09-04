package com.github.bunnyi.syncmatica.communication;

import com.github.bunnyi.syncmatica.*;
import com.github.bunnyi.syncmatica.communication.exchange.*;
import com.github.bunnyi.syncmatica.extended_core.PlayerIdentifier;
import com.github.bunnyi.syncmatica.extended_core.PlayerIdentifierProvider;
import com.github.bunnyi.syncmatica.extended_core.SubRegionData;
import com.github.bunnyi.syncmatica.extended_core.SubRegionPlacementModification;
import com.github.bunnyi.syncmatica.util.*;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ServerCommunicationManager {
    private final Collection<ExchangeTarget> broadcastTargets = new ArrayList<>();
    private final Map<UUID, Boolean> downloadState = new HashMap<>();
    private final Map<UUID, Exchange> modifyState = new HashMap<>();
    private final SyncmaticaContext context;
    private static final BlockRotation[] rotOrdinals = BlockRotation.values();
    private static final BlockMirror[] mirOrdinals = BlockMirror.values();
    private final Map<UUID, List<ServerPlacement>> downloadingFile = new HashMap<>();
    private final Map<ExchangeTarget, Player> playerMap = new HashMap<>();

    public ServerCommunicationManager(SyncmaticaContext syncmaticaContext) {
        context = syncmaticaContext;
    }

    public PlayerProfile getPlayerProfile(ExchangeTarget exchangeTarget) {
        return playerMap.get(exchangeTarget).getPlayerProfile();
    }

    public ExchangeTarget getPlayerExchangeTarget(Player player) {
        for (Map.Entry<ExchangeTarget, Player> entry : playerMap.entrySet()) {
            if (entry.getValue().equals(player)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void onPacket(ExchangeTarget source, Identifier id, PacketByteBuf packetBuf) {
        context.debugService.logReceivePacket(id);
        Exchange handler = null;
        final Collection<Exchange> potentialMessageTarget = source.getExchanges();
        if (potentialMessageTarget != null) {
            for (Exchange target : potentialMessageTarget) {
                if (target.checkPacket(id, new PacketByteBuf(packetBuf.toArray()))) {
                    target.handle(id, new PacketByteBuf(packetBuf.toArray()));
                    handler = target;
                    break;
                }
            }
        }
        if (handler == null) {
            handle(source, id, new PacketByteBuf(packetBuf.toArray()));
        } else if (handler.isFinished()) {
            notifyClose(handler);
        }
    }

    public boolean handlePacket(Identifier id) {
        return PacketType.containsIdentifier(id);
    }

    public void sendMetaData(final ServerPlacement metaData, final ExchangeTarget target) {
        final PacketByteBuf buf = new PacketByteBuf();
        putMetaData(metaData, buf, target);
        target.sendPacket(PacketType.REGISTER_METADATA.identifier, buf, context);
    }

    public void putMetaData(final ServerPlacement metaData, final PacketByteBuf buf, final ExchangeTarget exchangeTarget) {
        buf.writeUuid(metaData.getId());
        buf.writeString(SyncmaticaUtil.sanitizeFileName(metaData.getName()));
        buf.writeUuid(metaData.getHash());
        if (exchangeTarget.getFeatureSet().hasFeature(Feature.CORE_EX)) {
            buf.writeUuid(metaData.getOwner().uuid);
            buf.writeString(metaData.getOwner().getName());
            buf.writeUuid(metaData.getLastModifiedBy().uuid);
            buf.writeString(metaData.getLastModifiedBy().getName());
        }
        putPositionData(metaData, buf, exchangeTarget);
    }

    public void putPositionData(final ServerPlacement metaData, final PacketByteBuf buf, final ExchangeTarget exchangeTarget) {
        buf.writeBlockPos(metaData.getPosition());
        buf.writeString(metaData.getDimension());
        buf.writeInt(metaData.getRotation().ordinal());
        buf.writeInt(metaData.getMirror().ordinal());
        if (exchangeTarget.getFeatureSet().hasFeature(Feature.CORE_EX)) {
            if (metaData.getSubRegionData().getModificationData() == null) {
                buf.writeInt(0);
                return;
            }
            final Collection<SubRegionPlacementModification> regionData = metaData.getSubRegionData().getModificationData().values();
            buf.writeInt(regionData.size());

            for (final SubRegionPlacementModification subPlacement : regionData) {
                buf.writeString(subPlacement.name);
                buf.writeBlockPos(subPlacement.position);
                buf.writeInt(subPlacement.rotation.ordinal());
                buf.writeInt(subPlacement.mirror.ordinal());
            }
        }
    }

    public ServerPlacement receiveMetaData(PacketByteBuf buf, ExchangeTarget exchangeTarget) {
        UUID id = buf.readUuid();
        String fileName = SyncmaticaUtil.sanitizeFileName(buf.readString(32767));
        UUID hash = buf.readUuid();
        PlayerIdentifier owner = PlayerIdentifier.MISSING_PLAYER;
        PlayerIdentifier lastModifiedBy = PlayerIdentifier.MISSING_PLAYER;
        if (exchangeTarget.getFeatureSet().hasFeature(Feature.CORE_EX)) {
            PlayerIdentifierProvider provider = context.playerIdentifierProvider;
            owner = provider.createOrGet(buf.readUuid(), buf.readString());
            lastModifiedBy = provider.createOrGet(buf.readUuid(), buf.readString());
        }
        ServerPlacement placement = new ServerPlacement(id, fileName, hash, owner);
        placement.setLastModifiedBy(lastModifiedBy);
        receivePositionData(placement, buf, exchangeTarget);
        return placement;
    }

    public void receivePositionData(final ServerPlacement placement, final PacketByteBuf buf, final ExchangeTarget exchangeTarget) {
        BlockPos pos = buf.readBlockPos();
        String dimensionId = buf.readString();
        BlockRotation rot = rotOrdinals[buf.readInt()];
        BlockMirror mir = mirOrdinals[buf.readInt()];
        placement.move(dimensionId, pos, rot, mir);
        if (exchangeTarget.getFeatureSet().hasFeature(Feature.CORE_EX)) {
            final SubRegionData subRegionData = placement.getSubRegionData();
            subRegionData.reset();
            final int limit = buf.readInt();
            for (int i = 0; i < limit; i++) {
                subRegionData.modify(
                        buf.readString(),
                        buf.readBlockPos(),
                        rotOrdinals[buf.readInt()],
                        mirOrdinals[buf.readInt()]
                );
            }
        }
    }

    public void download(final ServerPlacement syncmatic, final ExchangeTarget source) throws NoSuchAlgorithmException, IOException {
        if (!context.fileStorage.getLocalState(syncmatic).isReadyForDownload()) {
            // forgot a negation here
            throw new IllegalArgumentException(syncmatic.toString() + " is not ready for download local state is: " + context.fileStorage.getLocalState(syncmatic).toString());
        }
        final File toDownload = context.fileStorage.createLocalLitematic(syncmatic);
        final Exchange downloadExchange = new DownloadExchange(syncmatic, toDownload, source, context);
        setDownloadState(syncmatic, true);
        startExchange(downloadExchange);
    }

    public void setDownloadState(final ServerPlacement syncmatic, final boolean b) {
        downloadState.put(syncmatic.getHash(), b);
    }

    public boolean getDownloadState(final ServerPlacement syncmatic) {
        return downloadState.getOrDefault(syncmatic.getHash(), false);
    }

    public void setModifier(final ServerPlacement syncmatic, Exchange exchange) {
        modifyState.put(syncmatic.getHash(), exchange);
    }

    public Exchange getModifier(final ServerPlacement syncmatic) {
        return modifyState.get(syncmatic.getHash());
    }

    public void startExchange(Exchange newExchange) {
        if (!broadcastTargets.contains(newExchange.partner)) {
            throw new IllegalArgumentException(newExchange.partner.toString() + " is not a valid ExchangeTarget");
        }
        startExchangeUnchecked(newExchange);
    }

    protected void startExchangeUnchecked(Exchange newExchange) {
        newExchange.partner.getExchanges().add(newExchange);
        newExchange.init();
        if (newExchange.isFinished()) {
            notifyClose(newExchange);
        }
    }

    public void notifyClose(Exchange e) {
        e.partner.getExchanges().remove(e);
        handleExchange(e);
    }

    public void sendMessage(final ExchangeTarget client, final MessageType type, final String identifier) {
        if (client.getFeatureSet().hasFeature(Feature.MESSAGE)) {
            final PacketByteBuf newPacketBuf = new PacketByteBuf();
            newPacketBuf.writeString(type.toString());
            newPacketBuf.writeString(identifier);
            client.sendPacket(PacketType.MESSAGE.identifier, newPacketBuf, context);
        } else if (playerMap.containsKey(client)) {
            Player player = playerMap.get(client);
            player.sendMessage("Syncmatica " + type.toString() + " " + identifier);
        }
    }

    public void onPlayerJoin(ExchangeTarget newPlayer, Player player) {
        VersionHandshakeServer versionHandshakeServer = new VersionHandshakeServer(newPlayer, context);
        playerMap.put(newPlayer, player);
        PlayerProfile profile = player.getPlayerProfile();
        context.playerIdentifierProvider.updateName(profile.getUniqueId(), profile.getName());
        startExchangeUnchecked(versionHandshakeServer);
    }

    public void onPlayerLeave(ExchangeTarget oldPlayer, Player player) {
        final Collection<Exchange> potentialMessageTarget = oldPlayer.getExchanges();
        if (potentialMessageTarget != null) {
            for (final Exchange target : potentialMessageTarget) {
                target.close(false);
                handleExchange(target);
            }
        }
        broadcastTargets.remove(oldPlayer);
        playerMap.remove(oldPlayer);
    }

    protected void handle(final ExchangeTarget source, final Identifier id, final PacketByteBuf packetBuf) {
        if (id.equals(PacketType.REQUEST_LITEMATIC.identifier)) {
            UUID syncmaticaId = packetBuf.readUuid();
            ServerPlacement placement = context.syncmaticManager.getPlacement(syncmaticaId);
            if (placement == null) {
                return;
            }
            File toUpload = context.fileStorage.getLocalLitematic(placement);
            UploadExchange upload;
            try {
                upload = new UploadExchange(placement, toUpload, source, context);
            } catch (final FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
            startExchange(upload);
            return;
        }
        if (id.equals(PacketType.REGISTER_METADATA.identifier)) {
            final ServerPlacement placement = receiveMetaData(packetBuf, source);
            if (context.syncmaticManager.getPlacement(placement.getId()) != null) {
                cancelShare(source, placement);
                return;
            }
            // 当客户端不与所有者通信时
            PlayerProfile profile = playerMap.get(source).getPlayerProfile();
            PlayerIdentifier playerIdentifier = context.playerIdentifierProvider.createOrGet(profile);
            if (!placement.getOwner().equals(playerIdentifier)) {
                placement.setOwner(playerIdentifier);
                placement.setLastModifiedBy(playerIdentifier);
            }
            if (!context.fileStorage.getLocalState(placement).isLocalFileReady()) {
                // 特殊的边缘情况，因为文件存储是通过放置而不是文件名/哈希来传输的
                if (context.fileStorage.getLocalState(placement) == LocalLitematicState.DOWNLOADING_LITEMATIC) {
                    downloadingFile.computeIfAbsent(placement.getHash(), key -> new ArrayList<>()).add(placement);
                    return;
                }
                try {
                    download(placement, source);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            addPlacement(source, placement);
            return;
        }
        if (id.equals(PacketType.REMOVE_SYNCMATIC.identifier)) {
            final UUID placementId = packetBuf.readUuid();
            final ServerPlacement placement = context.syncmaticManager.getPlacement(placementId);
            if (placement != null) {
                final Exchange modifier = getModifier(placement);
                if (modifier != null) {
                    modifier.close(true);
                    notifyClose(modifier);
                }
                context.syncmaticManager.removePlacement(placement);
                for (final ExchangeTarget client : broadcastTargets) {
                    final PacketByteBuf newPacketBuf = new PacketByteBuf();
                    newPacketBuf.writeUuid(placement.getId());
                    client.sendPacket(PacketType.REMOVE_SYNCMATIC.identifier, newPacketBuf, context);
                }
            }
        }
        if (id.equals(PacketType.MODIFY_REQUEST.identifier)) {
            final UUID placementId = packetBuf.readUuid();
            final ModifyExchangeServer modifier = new ModifyExchangeServer(placementId, source, context);
            startExchange(modifier);
        }
    }

    protected void handleExchange(final Exchange exchange) {
        if (exchange instanceof DownloadExchange) {
            ServerPlacement p = ((DownloadExchange) exchange).getPlacement();
            if (exchange.isSuccessful()) {
                addPlacement(exchange.partner, p);
                if (downloadingFile.containsKey(p.getHash())) {
                    for (final ServerPlacement placement : downloadingFile.get(p.getHash())) {
                        addPlacement(exchange.partner, placement);
                    }
                }
            } else {
                cancelShare(exchange.partner, p);
                if (downloadingFile.containsKey(p.getHash())) {
                    for (final ServerPlacement placement : downloadingFile.get(p.getHash())) {
                        cancelShare(exchange.partner, placement);
                    }
                }
            }

            downloadingFile.remove(p.getHash());
            return;
        }
        if (exchange instanceof VersionHandshakeServer && exchange.isSuccessful()) {
            broadcastTargets.add(exchange.partner);
        }
        if (exchange instanceof ModifyExchangeServer && exchange.isSuccessful()) {
            final ServerPlacement placement = ((ModifyExchangeServer) exchange).getPlacement();
            for (final ExchangeTarget client : broadcastTargets) {
                if (client.getFeatureSet().hasFeature(Feature.MODIFY)) {
                    // client supports modify so just send modify
                    final PacketByteBuf buf = new PacketByteBuf();
                    buf.writeUuid(placement.getId());
                    putPositionData(placement, buf, client);
                    if (client.getFeatureSet().hasFeature(Feature.CORE_EX)) {
                        buf.writeUuid(placement.getLastModifiedBy().uuid);
                        buf.writeString(placement.getLastModifiedBy().getName());
                    }
                    client.sendPacket(PacketType.MODIFY.identifier, buf, context);
                } else {
                    // 客户端不支持修改，所以
                    // 发送数据，然后
                    PacketByteBuf buf = new PacketByteBuf();
                    buf.writeUuid(placement.getId());
                    client.sendPacket(PacketType.REMOVE_SYNCMATIC.identifier, buf, context);
                    PacketByteBuf buf2 = new PacketByteBuf();
                    putMetaData(placement, buf2, client);
                    client.sendPacket(PacketType.REGISTER_METADATA.identifier, buf2, context);
                }
            }
        }
    }

    private void addPlacement(ExchangeTarget t, ServerPlacement placement) {
        if (context.syncmaticManager.getPlacement(placement.getId()) != null) {
            cancelShare(t, placement);
            return;
        }
        context.syncmaticManager.addPlacement(placement);
        for (final ExchangeTarget target : broadcastTargets) {
            sendMetaData(placement, target);
        }
    }

    private void cancelShare(final ExchangeTarget source, final ServerPlacement placement) {
        final PacketByteBuf packetByteBuf = new PacketByteBuf();
        packetByteBuf.writeUuid(placement.getId());
        source.sendPacket(PacketType.CANCEL_SHARE.identifier, packetByteBuf, context);
    }
}
