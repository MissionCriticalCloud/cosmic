package org.apache.cloudstack.managed.context;

import org.apache.cloudstack.managed.context.impl.DefaultManagedContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ManagedContextRunnable implements Runnable {

    private static final int SLEEP_COUNT = 120;

    private static final Logger log = LoggerFactory.getLogger(ManagedContextRunnable.class);
    private static final ManagedContext DEFAULT_MANAGED_CONTEXT = new DefaultManagedContext();
    private static ManagedContext context;
    private static boolean managedContext = false;

    /* This is slightly dirty, but the idea is that we only save the ManagedContext
     * in a static global.  Any ManagedContextListener can be a fully managed object
     * and not have to rely on global statics
     */
    public static ManagedContext initializeGlobalContext(final ManagedContext context) {
        setManagedContext(true);
        return ManagedContextRunnable.context = context;
    }

    public static boolean isManagedContext() {
        return managedContext;
    }

    public static void setManagedContext(final boolean managedContext) {
        ManagedContextRunnable.managedContext = managedContext;
    }

    @Override
    public void run() {
        getContext().runWithContext(new Runnable() {
            @Override
            public void run() {
                runInContext();
            }
        });
    }

    protected abstract void runInContext();

    protected ManagedContext getContext() {
        if (!managedContext) {
            return DEFAULT_MANAGED_CONTEXT;
        }

        for (int i = 0; i < SLEEP_COUNT; i++) {
            if (context == null) {
                try {
                    Thread.sleep(1000);

                    if (context == null) {
                        log.info("Sleeping until ManagedContext becomes available");
                    }
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return context;
            }
        }

        throw new RuntimeException("Failed to obtain ManagedContext");
    }
}
