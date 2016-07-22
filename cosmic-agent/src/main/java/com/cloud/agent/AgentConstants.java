package com.cloud.agent;

import com.cloud.utils.CloudConstants;

public class AgentConstants extends CloudConstants {
    public static final String PROPERTY_KEY_PING_RETRIES = "ping.retries";
    public static final String PROPERTY_KEY_IPV6_DISABLED = "ipv6disabled";
    public static final String PROPERTY_KEY_IPV6_PREFERRED = "ipv6prefer";
    public static final String PROPERTY_KEY_WORKERS = "workers";
    public static final String PROPERTY_KEY_PID_DIR = "piddir";
    public static final String PROPERTY_KEY_CONSOLE_PROXY_HTTP_PORT = "consoleproxy.httpListenPort";

    public static final int DEFAULT_PORT = 8250;
    public static final int DEFAULT_CONSOLE_PROXY_HTTP_PORT = 443;
    public static final int DEFAULT_NUMBER_OF_WORKERS = 5;
    public static final int DEFAULT_NUMBER_OF_PING_RETRIES = 5;
    public static final boolean DEFAULT_IPV6_DISABLED = false;
    public static final boolean DEFAULT_IPV6_PREFERRED = false;
    public static final String DEFAULT_PID_DIR = "/var/run";
}
