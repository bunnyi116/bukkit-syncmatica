package com.github.bunnyi.syncmatica;

/**
 * 本地投影状态
 */
public enum LocalLitematicState {
    NO_LOCAL_LITEMATIC(true, false),            // 没有本地原理图
    LOCAL_LITEMATIC_DESYNC(true, false),        // 本地原理图不同步
    DOWNLOADING_LITEMATIC(false, false),        // 正在下载原理图
    LOCAL_LITEMATIC_PRESENT(false, true);       // 本地原理图存在

    private final boolean downloadReady;
    private final boolean fileReady;

    LocalLitematicState(boolean downloadReady, boolean fileReady) {
        this.downloadReady = downloadReady;
        this.fileReady = fileReady;
    }

    public boolean isReadyForDownload() {
        return downloadReady;
    }

    public boolean isLocalFileReady() {
        return fileReady;
    }
}