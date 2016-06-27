package org.apache.cloudstack.managed.context;

import java.util.concurrent.Callable;

public interface ManagedContext {

    public void registerListener(ManagedContextListener<?> listener);

    public void unregisterListener(ManagedContextListener<?> listener);

    public void runWithContext(Runnable run);

    public <T> T callWithContext(Callable<T> callable) throws Exception;
}
