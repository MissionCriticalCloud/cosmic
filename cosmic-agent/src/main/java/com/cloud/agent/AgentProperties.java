package com.cloud.agent;

import static com.cloud.agent.AgentConstants.DEFAULT_CONSOLE_PROXY_HTTP_PORT;
import static com.cloud.agent.AgentConstants.DEFAULT_IPV6_DISABLED;
import static com.cloud.agent.AgentConstants.DEFAULT_IPV6_PREFERRED;
import static com.cloud.agent.AgentConstants.DEFAULT_NUMBER_OF_PING_RETRIES;
import static com.cloud.agent.AgentConstants.DEFAULT_NUMBER_OF_WORKERS;
import static com.cloud.agent.AgentConstants.DEFAULT_PID_DIR;
import static com.cloud.agent.AgentConstants.DEFAULT_PORT;
import static com.cloud.agent.AgentConstants.DEFAULT_ZONE;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_CONSOLE_PROXY_HTTP_PORT;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_CONSOLE_VERSION;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_DEVELOPER;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_GUID;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_HOST;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_INSTANCE;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_IPV6_DISABLED;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_IPV6_PREFERRED;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_PID_DIR;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_PING_RETRIES;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_POD;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_PORT;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_RESOURCE;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_WORKERS;
import static com.cloud.agent.AgentConstants.PROPERTY_KEY_ZONE;
import static com.cloud.utils.PropertiesUtil.parse;

import com.cloud.utils.PropertiesPojo;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class AgentProperties implements PropertiesPojo {
    private String host = "";
    private int port = DEFAULT_PORT;
    private int consoleProxyHttpPort = DEFAULT_CONSOLE_PROXY_HTTP_PORT;
    private String zone = DEFAULT_ZONE;
    private String pod = "";
    private String guid = "";
    private String resource = "";
    private String instance = "";
    private int workers = DEFAULT_NUMBER_OF_WORKERS;
    private int pingRetries = DEFAULT_NUMBER_OF_PING_RETRIES;
    private boolean developer = false;
    private boolean ipv6Disabled = DEFAULT_IPV6_DISABLED;
    private boolean ipa6Preferred = DEFAULT_IPV6_PREFERRED;
    private String pidDir = DEFAULT_PID_DIR;
    private String version = "";

    public void load(final Properties properties) {
        host = parse(properties, PROPERTY_KEY_HOST, host);
        workers = parse(properties, PROPERTY_KEY_WORKERS, workers);
        port = parse(properties, PROPERTY_KEY_PORT, port);
        zone = parse(properties, PROPERTY_KEY_ZONE, zone);
        pod = parse(properties, PROPERTY_KEY_POD, pod);
        guid = parse(properties, PROPERTY_KEY_GUID, guid);
        pingRetries = parse(properties, PROPERTY_KEY_PING_RETRIES, pingRetries);
        developer = parse(properties, PROPERTY_KEY_DEVELOPER, developer);
        resource = parse(properties, PROPERTY_KEY_RESOURCE, resource);
        ipv6Disabled = parse(properties, PROPERTY_KEY_IPV6_DISABLED, ipv6Disabled);
        ipa6Preferred = parse(properties, PROPERTY_KEY_IPV6_PREFERRED, ipa6Preferred);
        instance = parse(properties, PROPERTY_KEY_INSTANCE, instance);
        pidDir = parse(properties, PROPERTY_KEY_PID_DIR, pidDir);
        consoleProxyHttpPort = parse(properties, PROPERTY_KEY_CONSOLE_PROXY_HTTP_PORT, consoleProxyHttpPort);
        version = parse(properties, PROPERTY_KEY_CONSOLE_VERSION, version);

        validateValues();
    }

    private void validateValues() {
        workers = workers <= 0 ? DEFAULT_NUMBER_OF_WORKERS : workers;
        pingRetries = pingRetries <= 0 ? DEFAULT_NUMBER_OF_PING_RETRIES : pingRetries;
        guid = developer ? UUID.randomUUID().toString() : guid;
        instance = developer ? UUID.randomUUID().toString() : instance;
    }

    public String getHost() {
        return host;
    }

    public int getWorkers() {
        return workers;
    }

    public int getPort() {
        return port;
    }

    public String getZone() {
        return zone;
    }

    public String getPod() {
        return pod;
    }

    public String getGuid() {
        return guid;
    }

    public int getPingRetries() {
        return pingRetries;
    }

    public boolean isDeveloper() {
        return developer;
    }

    public String getResource() {
        return resource;
    }

    public boolean isIpv6Disabled() {
        return ipv6Disabled;
    }

    public boolean isIpa6Preferred() {
        return ipa6Preferred;
    }

    public String getInstance() {
        return instance;
    }

    public String getPidDir() {
        return pidDir;
    }

    public int getConsoleProxyHttpPort() {
        return consoleProxyHttpPort;
    }

    public String getVersion() {
        return version;
    }

    public boolean hasIntance() {
        return instance != null && !instance.isEmpty();
    }

    public Map<String, Object> buildPropertiesMap() {
        final HashMap<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put(PROPERTY_KEY_HOST, host);
        propertiesMap.put(PROPERTY_KEY_PORT, port);
        propertiesMap.put(PROPERTY_KEY_ZONE, zone);
        propertiesMap.put(PROPERTY_KEY_POD, pod);
        propertiesMap.put(PROPERTY_KEY_GUID, guid);
        propertiesMap.put(PROPERTY_KEY_RESOURCE, resource);
        propertiesMap.put(PROPERTY_KEY_INSTANCE, instance);
        propertiesMap.put(PROPERTY_KEY_PID_DIR, pidDir);
        propertiesMap.put(PROPERTY_KEY_WORKERS, workers);
        propertiesMap.put(PROPERTY_KEY_PING_RETRIES, pingRetries);
        propertiesMap.put(PROPERTY_KEY_DEVELOPER, developer);
        propertiesMap.put(PROPERTY_KEY_IPV6_DISABLED, ipv6Disabled);
        propertiesMap.put(PROPERTY_KEY_IPV6_PREFERRED, ipa6Preferred);
        propertiesMap.put(PROPERTY_KEY_CONSOLE_PROXY_HTTP_PORT, consoleProxyHttpPort);
        return propertiesMap;
    }
}
