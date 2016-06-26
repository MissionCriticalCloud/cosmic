package org.apache.cloudstack.framework.sampleserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleManagementServer {
    private static final Logger s_logger = LoggerFactory.getLogger(SampleManagementServer.class);

    public void mainLoop() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                s_logger.debug("[ignored] .");
            }
        }
    }
}
