package com.github.bunnyi.syncmatica.service;

import com.github.bunnyi.syncmatica.SyncmaticaContext;

public interface IService {

    void setContext(SyncmaticaContext context);

    SyncmaticaContext getContext();

    void getDefaultConfiguration(IServiceConfiguration configuration);

    String getConfigKey();

    void configure(IServiceConfiguration configuration);

    void startup();

    void shutdown();
}
