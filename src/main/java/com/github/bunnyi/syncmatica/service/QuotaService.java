package com.github.bunnyi.syncmatica.service;

import com.github.bunnyi.syncmatica.communication.ExchangeTarget;

import java.util.HashMap;
import java.util.Map;

public class QuotaService extends AbstractService {

    public static final Boolean IS_ENABLED_DEFAULT = false;
    public static final Integer QUOTA_LIMIT_DEFAULT = 40000000;

    Map<String, Integer> progress = new HashMap<>();
    Boolean isEnabled = IS_ENABLED_DEFAULT;
    Integer limit = QUOTA_LIMIT_DEFAULT;

    public Boolean isOverQuota(ExchangeTarget sender, Integer newData) {
        if (!Boolean.TRUE.equals(isEnabled)) {
            return false;
        }
        int curValue = progress.getOrDefault(sender.getPersistentName(), 0);
        curValue += newData;
        return curValue > limit;
    }

    public void progressQuota(ExchangeTarget sender, Integer newData) {
        if (Boolean.TRUE.equals(isEnabled)) {
            int curValue = progress.getOrDefault(sender.getPersistentName(), 0);
            progress.put(sender.getPersistentName(), curValue + newData);
        }
    }

    @Override
    public void getDefaultConfiguration(IServiceConfiguration configuration) {
        configuration.saveBoolean("enabled", IS_ENABLED_DEFAULT);
        configuration.saveInteger("limit", QUOTA_LIMIT_DEFAULT);
    }

    @Override
    public String getConfigKey() {
        return "quota";
    }

    @Override
    public void configure(IServiceConfiguration configuration) {
        configuration.loadBoolean("enabled", b -> isEnabled = b);
        configuration.loadInteger("limit", i -> limit = i);
    }

    @Override
    public void startup() {
    }

    @Override
    public void shutdown() {
    }
}
