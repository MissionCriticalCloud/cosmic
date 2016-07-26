package com.cloud.agent;

public interface IAgentShell {
    String getHost();

    int getPort();

    int getWorkers();

    String getGuid();

    String getZone();

    String getPod();
}
