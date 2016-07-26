package com.cloud.agent;

import java.util.List;

public interface IAgentShell {
    List<String> getHosts();

    int getPort();

    int getWorkers();

    String getGuid();

    String getZone();

    String getPod();
}
