package com.cloud.common.managed.context;

import java.util.concurrent.Callable;

public interface ManagedContext {

    void registerListener(ManagedContextListener<?> listener);

    void unregisterListener(ManagedContextListener<?> listener);

    void runWithContext(Runnable run);

    <T> T callWithContext(Callable<T> callable) throws Exception;
}
