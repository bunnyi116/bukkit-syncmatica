package com.github.bunnyi.syncmatica.service;

import com.github.bunnyi.syncmatica.SyncmaticaContext;

abstract class AbstractService implements IService {

    SyncmaticaContext context;

    @Override
    public void setContext(final SyncmaticaContext context) {
        this.context = context;
    }

    @Override
    public SyncmaticaContext getContext() {
        return context;
    }
}
