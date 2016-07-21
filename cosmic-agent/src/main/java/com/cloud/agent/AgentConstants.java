package com.cloud.agent;

public class AgentConstants {
    public static final String PROPERTY_KEY_HOST = "host";
    public static final String PROPERTY_KEY_PORT = "port";
    public static final String PROPERTY_KEY_WORKERS = "workers";
    public static final String PROPERTY_KEY_ZONE = "zone";
    public static final String PROPERTY_KEY_POD = "pod";
    public static final String PROPERTY_KEY_PING_RETRIES = "ping.retries";
    public static final String PROPERTY_KEY_DEVELOPER = "developer";
    public static final String PROPERTY_KEY_GUID = "guid";
    public static final String PROPERTY_KEY_RESOURCE = "resource";
    public static final String PROPERTY_KEY_IPV6_DISABLED = "ipv6disabled";
    public static final String PROPERTY_KEY_IPV6_PREFERRED = "ipv6prefer";
    public static final String PROPERTY_KEY_INSTANCE = "instance";
    public static final String PROPERTY_KEY_PID_DIR = "piddir";
    public static final String PROPERTY_KEY_CONSOLE_PROXY_HTTP_PORT = "consoleproxy.httpListenPort";
    public static final String PROPERTY_KEY_CONSOLE_VERSION = "version";

    public static final int DEFAULT_PORT = 8250;
    public static final int DEFAULT_CONSOLE_PROXY_HTTP_PORT = 443;
    public static final int DEFAULT_NUMBER_OF_WORKERS = 5;
    public static final int DEFAULT_NUMBER_OF_PING_RETRIES = 5;
    public static final String DEFAULT_ZONE = "default";
    public static final boolean DEFAULT_IPV6_DISABLED = false;
    public static final boolean DEFAULT_IPV6_PREFERRED = false;
    public static final String DEFAULT_PID_DIR = "/var/run";
}
