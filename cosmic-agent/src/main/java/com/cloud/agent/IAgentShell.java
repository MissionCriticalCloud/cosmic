package com.cloud.agent;

import com.cloud.utils.backoff.BackoffAlgorithm;

import java.util.Map;
import java.util.Properties;

public interface IAgentShell {
    Map<String, Object> getCmdLineProperties();

    Properties getProperties();

    String getPersistentProperty(String prefix, String name);

    void setPersistentProperty(String prefix, String name, String value);

    String getHost();

    int getPort();

    int getWorkers();

    String getGuid();

    String getZone();

    String getPod();

    BackoffAlgorithm getBackoffAlgorithm();

    int getPingRetries();

    String getVersion();
}
