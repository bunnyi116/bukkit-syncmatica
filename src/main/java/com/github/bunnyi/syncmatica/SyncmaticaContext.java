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

/**
 * 同步投影上下文
 */
public class SyncmaticaContext {
    public final File configFolder;                                 // 配置文件夹
    public final File configFile;                                   // 配置文件
    public final File litematicFolder;                              // 投影配置文件夹
    public final FileStorage fileStorage;                           // 文件存储对象
    public final ServerCommunicationManager communicationManager;   // 服务器通信管理器
    public final SyncmaticaManager syncmaticaManager;               // 同步管理器
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
        this.syncmaticaManager = new SyncmaticaManager(this);
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
        this.syncmaticaManager.startup();
    }

    public void shutdown() {
        this.quotaService.shutdown();
        this.debugService.shutdown();
        this.isStarted = false;
        this.syncmaticaManager.shutdown();
    }

    public boolean checkPartnerVersion(String version) {
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
        } catch (Exception ignored) {
            configuration = new JsonObject();
        }
        boolean needsRewrite = loadConfigurationForService(quotaService, configuration, attemptToLoad);
        needsRewrite |= loadConfigurationForService(debugService, configuration, attemptToLoad);
        if (needsRewrite) {
            try (Writer writer = new BufferedWriter(new FileWriter(getAndCreateConfigFile()))) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonString = gson.toJson(configuration);
                writer.write(jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Boolean loadConfigurationForService(IService service, JsonObject configuration, boolean attemptToLoad) {
        String configKey = service.getConfigKey();
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
            } catch (Exception e) {
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

        public DuplicateContextAssignmentException(String reason) {
            super(reason);
        }
    }

    public static class ContextMismatchException extends RuntimeException {
        private static final long serialVersionUID = 2769376183212635479L;

        public ContextMismatchException(String reason) {
            super(reason);
        }
    }
}
