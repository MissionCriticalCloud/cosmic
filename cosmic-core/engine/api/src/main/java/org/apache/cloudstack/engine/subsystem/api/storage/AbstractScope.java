package org.apache.cloudstack.engine.subsystem.api.storage;

public abstract class AbstractScope implements Scope {
    @Override
    public boolean isSameScope(final Scope scope) {
        return this.getScopeType() == scope.getScopeType() && this.getScopeId().equals(scope.getScopeId());
    }
}
