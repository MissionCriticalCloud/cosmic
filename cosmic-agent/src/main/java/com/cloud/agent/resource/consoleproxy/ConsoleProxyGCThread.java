package com.cloud.agent.resource.consoleproxy;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConsoleProxyGCThread does house-keeping work for the process, it helps cleanup log files,
 * recycle idle client sessions without front-end activities and report client stats to external
 * management software
 */
public class ConsoleProxyGCThread extends Thread {
    private static final Logger s_logger = LoggerFactory.getLogger(ConsoleProxyGCThread.class);

    private final static int MAX_SESSION_IDLE_SECONDS = 180;

    private final Hashtable<String, ConsoleProxyClient> connMap;
    private long lastLogScan = 0;

    public ConsoleProxyGCThread(final Hashtable<String, ConsoleProxyClient> connMap) {
        this.connMap = connMap;
    }

    @Override
    public void run() {

        boolean bReportLoad = false;
        long lastReportTick = System.currentTimeMillis();
        while (true) {
            cleanupLogging();
            bReportLoad = false;

            if (s_logger.isDebugEnabled()) {
                s_logger.debug("connMap=" + this.connMap);
            }
            final Enumeration<String> e = this.connMap.keys();
            while (e.hasMoreElements()) {
                final String key;
                final ConsoleProxyClient client;

                synchronized (this.connMap) {
                    key = e.nextElement();
                    client = this.connMap.get(key);
                }

                final long seconds_unused = (System.currentTimeMillis() - client.getClientLastFrontEndActivityTime()) / 1000;
                if (seconds_unused < MAX_SESSION_IDLE_SECONDS) {
                    continue;
                }

                synchronized (this.connMap) {
                    this.connMap.remove(key);
                    bReportLoad = true;
                }

                // close the server connection
                s_logger.info("Dropping " + client + " which has not been used for " + seconds_unused + " seconds");
                client.closeClient();
            }

            if (bReportLoad || System.currentTimeMillis() - lastReportTick > 5000) {
                // report load changes
                final String loadInfo = new ConsoleProxyClientStatsCollector(this.connMap).getStatsReport();
                ConsoleProxy.reportLoadInfo(loadInfo);
                lastReportTick = System.currentTimeMillis();

                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Report load change : " + loadInfo);
                }
            }

            try {
                Thread.sleep(5000);
            } catch (final InterruptedException ex) {
                s_logger.debug("[ignored] Console proxy was interupted during GC.");
            }
        }
    }

    private void cleanupLogging() {
        if (this.lastLogScan != 0 && System.currentTimeMillis() - this.lastLogScan < 3600000) {
            return;
        }

        this.lastLogScan = System.currentTimeMillis();

        final File logDir = new File("./logs");
        final File[] files = logDir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (System.currentTimeMillis() - file.lastModified() >= 86400000L) {
                    file.delete();
                }
            }
        }
    }
}
