package org.apache.cloudstack.spring.lifecycle;

import com.cloud.utils.LogUtils;

import org.springframework.context.SmartLifecycle;

public class CloudStackLog4jSetup implements SmartLifecycle {

    @Override
    public void start() {
        LogUtils.initLog4j("log4j-cloud.xml");
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(final Runnable callback) {
        callback.run();
    }
}
