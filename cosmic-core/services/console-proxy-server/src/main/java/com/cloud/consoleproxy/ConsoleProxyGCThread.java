package com.cloud.consoleproxy;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

/**
 * ConsoleProxyGCThread does house-keeping work for the process, it helps cleanup log files,
 * recycle idle client sessions without front-end activities and report client stats to external
 * management software
 */
public class ConsoleProxyGCThread extends Thread {
    private static final Logger s_logger = Logger.getLogger(ConsoleProxyGCThread.class);

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
                s_logger.debug("connMap=" + connMap);
            }
            final Enumeration<String> e = connMap.keys();
            while (e.hasMoreElements()) {
                final String key;
                final ConsoleProxyClient client;

                synchronized (connMap) {
                    key = e.nextElement();
                    client = connMap.get(key);
                }

                final long seconds_unused = (System.currentTimeMillis() - client.getClientLastFrontEndActivityTime()) / 1000;
                if (seconds_unused < MAX_SESSION_IDLE_SECONDS) {
                    continue;
                }

                synchronized (connMap) {
                    connMap.remove(key);
                    bReportLoad = true;
                }

                // close the server connection
                s_logger.info("Dropping " + client + " which has not been used for " + seconds_unused + " seconds");
                client.closeClient();
            }

            if (bReportLoad || System.currentTimeMillis() - lastReportTick > 5000) {
                // report load changes
                final String loadInfo = new ConsoleProxyClientStatsCollector(connMap).getStatsReport();
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
        if (lastLogScan != 0 && System.currentTimeMillis() - lastLogScan < 3600000) {
            return;
        }

        lastLogScan = System.currentTimeMillis();

        final File logDir = new File("./logs");
        final File[] files = logDir.listFiles();
        if (files != null) {
            for (final File file : files) {
                if (System.currentTimeMillis() - file.lastModified() >= 86400000L) {
                    try {
                        file.delete();
                    } catch (final Throwable e) {
                        s_logger.info("[ignored]"
                                + "failed to delete file: " + e.getLocalizedMessage());
                    }
                }
            }
        }
    }
}
