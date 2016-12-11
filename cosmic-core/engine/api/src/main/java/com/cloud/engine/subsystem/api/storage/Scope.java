package com.cloud.engine.subsystem.api.storage;

import com.cloud.storage.ScopeType;

public interface Scope {
    ScopeType getScopeType();

    boolean isSameScope(Scope scope);

    Long getScopeId();
}
