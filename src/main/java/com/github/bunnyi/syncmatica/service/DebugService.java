package com.github.bunnyi.syncmatica.service;

import com.github.bunnyi.syncmatica.LogManager;
import com.github.bunnyi.syncmatica.util.Identifier;

public class DebugService extends AbstractService {

    private boolean doPacketLogging = false;

    public void logReceivePacket(final Identifier packageType) {
        if (doPacketLogging) {
            LogManager.info("Syncmatica - received packet:[type={}]", packageType);
        }
    }

    public void logSendPacket(final Identifier packetType, final String targetIdentifier) {
        if (doPacketLogging) {
            LogManager.info(
                    "Sending packet[type={}] to ExchangeTarget[id={}]",
                    packetType,
                    targetIdentifier
            );
        }
    }

    @Override
    public void getDefaultConfiguration(final IServiceConfiguration configuration) {
        configuration.saveBoolean("doPackageLogging", false);
    }

    @Override
    public String getConfigKey() {
        return "debug";
    }

    @Override
    public void configure(final IServiceConfiguration configuration) {
        configuration.loadBoolean("doPackageLogging", b -> doPacketLogging = b);
    }

    @Override
    public void startup() { //NOSONAR
    }

    @Override
    public void shutdown() { //NOSONAR
    }
}
