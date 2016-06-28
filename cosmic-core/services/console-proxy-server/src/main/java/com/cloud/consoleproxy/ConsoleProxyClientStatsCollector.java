package com.cloud.consoleproxy;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * ConsoleProxyClientStatsCollector collects client stats for console proxy agent to report
 */
public class ConsoleProxyClientStatsCollector {

    ArrayList<ConsoleProxyConnection> connections;

    public ConsoleProxyClientStatsCollector() {
    }

    public ConsoleProxyClientStatsCollector(final Hashtable<String, ConsoleProxyClient> connMap) {
        setConnections(connMap);
    }

    private void setConnections(final Hashtable<String, ConsoleProxyClient> connMap) {

        final ArrayList<ConsoleProxyConnection> conns = new ArrayList<>();
        final Enumeration<String> e = connMap.keys();
        while (e.hasMoreElements()) {
            synchronized (connMap) {
                final String key = e.nextElement();
                final ConsoleProxyClient client = connMap.get(key);

                final ConsoleProxyConnection conn = new ConsoleProxyConnection();

                conn.id = client.getClientId();
                conn.clientInfo = "";
                conn.host = client.getClientHostAddress();
                conn.port = client.getClientHostPort();
                conn.tag = client.getClientTag();
                conn.createTime = client.getClientCreateTime();
                conn.lastUsedTime = client.getClientLastFrontEndActivityTime();
                conns.add(conn);
            }
        }
        connections = conns;
    }

    public String getStatsReport() {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public void getStatsReport(final OutputStreamWriter os) {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(this, os);
    }

    public static class ConsoleProxyConnection {
        public int id;
        public String clientInfo;
        public String host;
        public int port;
        public String tag;
        public long createTime;
        public long lastUsedTime;

        public ConsoleProxyConnection() {
        }
    }
}
