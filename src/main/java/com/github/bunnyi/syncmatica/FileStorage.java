package com.github.bunnyi.syncmatica;

import com.github.bunnyi.syncmatica.util.SyncmaticaUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.UUID;

public class FileStorage {
    private final HashMap<ServerPlacement, Long> buffer;
    private final SyncmaticaContext context;

    public FileStorage(SyncmaticaContext syncmaticaContext) {
        this.buffer = new HashMap<>();
        this.context = syncmaticaContext;
    }

    public LocalLitematicState getLocalState(ServerPlacement placement) {
        File localFile = getSchematicPath(placement);
        if (localFile.isFile()) {
            if (isDownloading(placement)) {
                return LocalLitematicState.DOWNLOADING_LITEMATIC;
            }
            if ((buffer.containsKey(placement) && buffer.get(placement) == localFile.lastModified()) || hashCompare(localFile, placement)) {
                return LocalLitematicState.LOCAL_LITEMATIC_PRESENT;
            }
            return LocalLitematicState.LOCAL_LITEMATIC_DESYNC;
        }
        return LocalLitematicState.NO_LOCAL_LITEMATIC;
    }

    private boolean isDownloading(ServerPlacement placement) {
        if (context == null) {
            throw new RuntimeException("No CommunicationManager has been set yet - cannot get litematic state");
        }
        return context.communicationManager.getDownloadState(placement);
    }

    public File getLocalLitematic(ServerPlacement placement) {
        if (getLocalState(placement).isLocalFileReady()) {
            return getSchematicPath(placement);
        } else {
            return null;
        }
    }

    // 为文字数据创建空文件的方法
    public File createLocalLitematic(ServerPlacement placement) {
        if (getLocalState(placement).isLocalFileReady()) {
            throw new IllegalArgumentException("");
        }
        File file = getSchematicPath(placement);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private boolean hashCompare(File localFile, ServerPlacement placement) {
        UUID hash = null;
        try {
            hash = SyncmaticaUtil.createChecksum(Files.newInputStream(localFile.toPath()));
        } catch (Exception e) {
            // 可以安全地忽略，因为我们确定该文件已被找到
            e.printStackTrace();
        }
        if (hash == null) {
            return false;
        }
        if (hash.equals(placement.getHash())) {
            buffer.put(placement, localFile.lastModified());
            return true;
        }
        return false;
    }

    private File getSchematicPath(ServerPlacement placement) {
        File litematicPath = context.litematicFolder;
        return new File(litematicPath, placement.getHash().toString() + ".litematic");
    }
}
