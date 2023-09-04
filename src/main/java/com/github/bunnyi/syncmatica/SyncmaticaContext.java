package com.github.bunnyi.syncmatica;

import com.github.bunnyi.syncmatica.communication.FeatureSet;
import com.github.bunnyi.syncmatica.communication.ServerCommunicationManager;
import com.github.bunnyi.syncmatica.extended_core.PlayerIdentifierProvider;
import com.github.bunnyi.syncmatica.service.DebugService;
import com.github.bunnyi.syncmatica.service.IService;
import com.github.bunnyi.syncmatica.service.JsonConfiguration;
import com.github.bunnyi.syncmatica.service.QuotaService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.Arrays;

public class SyncmaticaContext {
    public final File configFolder;
    public final File configFile;
    public final File litematicFolder;
    public final FileStorage fileStorage;
    public final ServerCommunicationManager communicationManager;
    public final SyncmaticManager syncmaticManager;
    public final QuotaService quotaService;
    public final DebugService debugService;
    public final PlayerIdentifierProvider playerIdentifierProvider;
    public final FeatureSet featureSet = new FeatureSet(Arrays.asList(Feature.values()));
    private boolean isStarted = false;

    public SyncmaticaContext(File configFolder) {
        this.configFolder = configFolder;
        this.configFolder.mkdirs();
        this.litematicFolder = new File(configFolder, "litematic");
        this.litematicFolder.mkdirs();
        this.configFile = new File(configFolder, "config.json");
        this.fileStorage = new FileStorage(this);
        this.communicationManager = new ServerCommunicationManager(this);
        this.syncmaticManager = new SyncmaticManager(this);
        this.quotaService = new QuotaService();
        this.playerIdentifierProvider = new PlayerIdentifierProvider(this);
        this.debugService = new DebugService();
        loadConfiguration();
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void startup() {
        this.quotaService.startup();
        this.debugService.startup();
        this.isStarted = true;
        this.syncmaticManager.startup();
    }

    public void shutdown() {
        this.quotaService.shutdown();
        this.debugService.shutdown();
        this.isStarted = false;
        this.syncmaticManager.shutdown();
    }

    public boolean checkPartnerVersion(final String version) {
        return !version.equals("0.0.1");
    }


    public File getAndCreateConfigFile() throws IOException {
        configFolder.mkdirs();
        configFile.createNewFile();
        return configFile;
    }

    public void loadConfiguration() {
        boolean attemptToLoad = false;
        JsonObject configuration;
        try {
            configuration = new Gson().fromJson(new BufferedReader(new FileReader(configFile)), JsonObject.class);
            attemptToLoad = true;
        } catch (final Exception ignored) {
            configuration = new JsonObject();
        }
        boolean needsRewrite = loadConfigurationForService(quotaService, configuration, attemptToLoad);
        needsRewrite |= loadConfigurationForService(debugService, configuration, attemptToLoad);
        if (needsRewrite) {
            try (
                    final Writer writer = new BufferedWriter(new FileWriter(getAndCreateConfigFile()))
            ) {
                final Gson gson = new GsonBuilder().setPrettyPrinting().create();
                final String jsonString = gson.toJson(configuration);
                writer.write(jsonString);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Boolean loadConfigurationForService(final IService service, final JsonObject configuration, final boolean attemptToLoad) {
        final String configKey = service.getConfigKey();
        JsonObject serviceJson = null;
        JsonConfiguration serviceConfiguration = null;
        boolean started = false;

        if (attemptToLoad && configuration.has(configKey)) {
            try {
                serviceJson = configuration.getAsJsonObject(configKey);
                if (serviceJson != null) {
                    serviceConfiguration = new JsonConfiguration(serviceJson);
                    service.configure(serviceConfiguration);
                    started = true;
                    if (!serviceConfiguration.hadError()) {
                        return false;
                    }
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        if (serviceJson == null) {
            serviceJson = new JsonObject();
            configuration.add(configKey, serviceJson);
        }
        if (serviceConfiguration == null) {
            serviceConfiguration = new JsonConfiguration(serviceJson);
        }
        service.getDefaultConfiguration(serviceConfiguration);
        if (!started) {
            service.configure(serviceConfiguration);
        }
        return true;
    }


    public static class DuplicateContextAssignmentException extends RuntimeException {
        private static final long serialVersionUID = -5147544661160756303L;

        public DuplicateContextAssignmentException(final String reason) {
            super(reason);
        }
    }

    public static class ContextMismatchException extends RuntimeException {
        private static final long serialVersionUID = 2769376183212635479L;

        public ContextMismatchException(final String reason) {
            super(reason);
        }
    }
}
