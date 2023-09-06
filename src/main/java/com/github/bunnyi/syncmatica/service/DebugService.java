package com.github.bunnyi.syncmatica.service;


import com.github.bunnyi.syncmatica.SyncmaticaPlugin;
import com.github.bunnyi.syncmatica.util.Identifier;
import org.apache.logging.log4j.LogManager;

public class DebugService extends AbstractService {
    private boolean doPacketLogging = false;

    public void logReceivePacket(Identifier packageType) {
        if (doPacketLogging) {
            LogManager.getLogger(SyncmaticaPlugin.class).info("Syncmatica - received packet:[type={}]", packageType);
        }
    }

    public void logSendPacket(Identifier packetType, String targetIdentifier) {
        if (doPacketLogging) {
            LogManager.getLogger(SyncmaticaPlugin.class).info(
                    "Sending packet[type={}] to ExchangeTarget[id={}]",
                    packetType,
                    targetIdentifier
            );
        }
    }

    @Override
    public void getDefaultConfiguration(IServiceConfiguration configuration) {
        configuration.saveBoolean("doPackageLogging", false);
    }

    @Override
    public String getConfigKey() {
        return "debug";
    }

    @Override
    public void configure(IServiceConfiguration configuration) {
        configuration.loadBoolean("doPackageLogging", b -> doPacketLogging = b);
    }

    @Override
    public void startup() {
    }

    @Override
    public void shutdown() {
    }
}
