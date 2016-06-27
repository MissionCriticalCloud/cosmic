package com.cloud.cluster;

import java.util.List;

public interface ClusterManagerListener {
    void onManagementNodeJoined(List<? extends ManagementServerHost> nodeList, long selfNodeId);

    void onManagementNodeLeft(List<? extends ManagementServerHost> nodeList, long selfNodeId);

    void onManagementNodeIsolated();
}
