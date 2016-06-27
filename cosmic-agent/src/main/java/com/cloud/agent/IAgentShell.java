package com.cloud.agent;

import com.cloud.utils.backoff.BackoffAlgorithm;

import java.util.Map;
import java.util.Properties;

public interface IAgentShell {
    public Map<String, Object> getCmdLineProperties();

    public Properties getProperties();

    public String getPersistentProperty(String prefix, String name);

    public void setPersistentProperty(String prefix, String name, String value);

    public String getHost();

    public String getPrivateIp();

    public int getPort();

    public int getWorkers();

    public int getProxyPort();

    public String getGuid();

    public String getZone();

    public String getPod();

    public BackoffAlgorithm getBackoffAlgorithm();

    public int getPingRetries();

    public String getVersion();
}
