package org.apache.cloudstack.managed.context;

import java.util.TimerTask;

public abstract class ManagedContextTimerTask extends TimerTask {

    @Override
    public final void run() {
        new ManagedContextRunnable() {
            @Override
            protected void runInContext() {
                ManagedContextTimerTask.this.runInContext();
            }
        }.run();
    }

    protected abstract void runInContext();
}
