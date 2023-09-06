package com.github.bunnyi.syncmatica.util;

import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

public class SyncmaticaUtil {

    private static final int[] ILLEGAL_CHARS = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};
    private static final String ILLEGAL_PATTERNS = "(^(con|prn|aux|nul|com[0-9]|lpt[0-9])(\\..*)?$)|(^\\.\\.*$)";

    private SyncmaticaUtil() {
    }

    // 取自堆栈溢出
    static {
        Arrays.sort(ILLEGAL_CHARS);
    }

    public static UUID createChecksum(final InputStream fis) throws NoSuchAlgorithmException, IOException {
        // 源堆栈溢出
        byte[] buffer = new byte[4096]; // 4096是最常见的集群大小
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                messageDigest.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return UUID.nameUUIDFromBytes(messageDigest.digest());
    }

    public static String sanitizeFileName(String badFileName) {
        StringBuilder sanitized = new StringBuilder();
        int len = badFileName.codePointCount(0, badFileName.length());

        for (int i = 0; i < len; i++) {
            int c = badFileName.codePointAt(i);
            if (Arrays.binarySearch(ILLEGAL_CHARS, c) < 0) {
                sanitized.appendCodePoint(c);
                if (sanitized.length() == 255) { // 确保长度保持在255以下
                    break;
                }
            }
        }
        // ^ sanitizes unique characters
        // v sanitizes entire patterns
        return sanitized.toString().replaceAll(ILLEGAL_PATTERNS, "_");
    }

    public static void backupAndReplace(Path backup, Path current, Path incoming) {
        if (!Files.exists(incoming)) {
            return;
        }
        if (overwrite(backup, current, 2) && !overwrite(current, incoming, 4)) {
            overwrite(current, backup, 8); // 恢复备份
        }
    }

    private static boolean overwrite(Path backup, Path current, int tries) {
        if (!Files.exists(current)) {
            return true;
        }
        try {
            Files.deleteIfExists(backup);
            Files.move(current, backup);
        } catch (IOException exception) {
            if (tries <= 0) {
                LogManager.getLogger(SyncmaticaUtil.class).error("Excessive retries when trying to write Syncmatica placement", exception);
                return false;
            }
            return overwrite(backup, current, tries - 1);
        }
        return true;
    }

    public static double getBlockDistanceSquared(final BlockPos a, final double x, final double y, final double z) {
        double combinedX = a.x - x;
        double combinedY = a.y - y;
        double combinedZ = a.z - z;
        return combinedX * combinedX + combinedY * combinedY + combinedZ * combinedZ;
    }
}
