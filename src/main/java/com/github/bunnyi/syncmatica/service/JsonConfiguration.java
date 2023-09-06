package com.github.bunnyi.syncmatica.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class JsonConfiguration implements IServiceConfiguration {

    public final JsonObject configuration;
    private Boolean wasError;

    public JsonConfiguration(JsonObject configuration) {
        this.configuration = configuration;
        this.wasError = false;
    }

    @Override
    public void loadBoolean(String key, Consumer<Boolean> loader) {
        try {
            JsonElement elem = configuration.get(key);
            if (elem != null) {
                loader.accept(elem.getAsBoolean());
            }
        } catch (Exception ignored) {
            wasError = true;
        }
    }

    @Override
    public void saveBoolean(String key, Boolean value) {
        configuration.addProperty(key, value);
    }

    @Override
    public void loadInteger(String key, IntConsumer loader) {
        try {
            JsonElement elem = configuration.get(key);
            if (elem != null) {
                loader.accept(elem.getAsInt());
            }
        } catch (Exception ignored) {
            wasError = true;
        }
    }

    @Override
    public void saveInteger(String key, Integer value) {
        configuration.addProperty(key, value);
    }

    public Boolean hadError() {
        return wasError;
    }
}
