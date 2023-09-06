package com.github.bunnyi.syncmatica.service;

import com.github.bunnyi.syncmatica.SyncmaticaContext;

public abstract class AbstractService implements IService {
    private SyncmaticaContext context;

    @Override
    public void setContext(SyncmaticaContext context) {
        this.context = context;
    }

    @Override
    public SyncmaticaContext getContext() {
        return context;
    }
}
