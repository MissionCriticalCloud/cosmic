package org.apache.cloudstack.spring.module.util;

import org.apache.cloudstack.spring.module.factory.CloudStackSpringContext;

public class Main {

    long start = System.currentTimeMillis();

    public Main() {

    }

    public static void main(final String... args) {
        final Main main = new Main();

        try {
            main.start();
            System.out.println("STARTUP COMPLETE [" + main.getTime() + "] ms");
        } catch (final Exception e) {
            e.printStackTrace();
            System.out.println("STARTUP FAILED [" + main.getTime() + "] ms");
            System.err.println("STARTUP FAILED [" + main.getTime() + "] ms");
            System.exit(1);
        }
    }

    public void start() throws Exception {
        final CloudStackSpringContext context = new CloudStackSpringContext();
        context.registerShutdownHook();

        if (Boolean.getBoolean("force.exit")) {
            System.exit(0);
        }
    }

    public long getTime() {
        return System.currentTimeMillis() - start;
    }
}
