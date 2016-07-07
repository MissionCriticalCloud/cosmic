package com.cloud.usage;

import com.cloud.utils.component.ComponentContext;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UsageServer implements Daemon {
    public static final String Name = "usage-server";
    private static final Logger s_logger = LoggerFactory.getLogger(UsageServer.class.getName());
    private UsageManager mgr;
    private ClassPathXmlApplicationContext appContext;

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final UsageServer usage = new UsageServer();
        usage.start();
    }

    @Override
    public void init(final DaemonContext arg0) throws DaemonInitException, Exception {
    }

    @Override
    public void start() {

        appContext = new ClassPathXmlApplicationContext("usageApplicationContext.xml");

        try {
            ComponentContext.initComponentsLifeCycle();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        mgr = appContext.getBean(UsageManager.class);

        if (mgr != null) {
            if (s_logger.isInfoEnabled()) {
                s_logger.info("UsageServer ready...");
            }
        }
    }

    @Override
    public void stop() {
        appContext.close();
    }

    @Override
    public void destroy() {

    }
}
