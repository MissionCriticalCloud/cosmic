package org.apache.cloudstack.spring.lifecycle;

import org.springframework.context.SmartLifecycle;

public abstract class AbstractSmartLifeCycle implements SmartLifecycle {

    boolean running = false;

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(final Runnable callback) {
        stop();
        callback.run();
    }
}
