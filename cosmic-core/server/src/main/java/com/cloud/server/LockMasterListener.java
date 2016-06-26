package com.cloud.server;

import com.cloud.cluster.ClusterManagerListener;
import com.cloud.cluster.ManagementServerHost;
import com.cloud.utils.db.Merovingian2;

import java.util.List;

/**
 * when a management server is down.
 */
public class LockMasterListener implements ClusterManagerListener {
    Merovingian2 _lockMaster;

    public LockMasterListener(final long msId) {
        _lockMaster = Merovingian2.createLockMaster(msId);
    }

    @Override
    public void onManagementNodeJoined(final List<? extends ManagementServerHost> nodeList, final long selfNodeId) {
    }

    @Override
    public void onManagementNodeLeft(final List<? extends ManagementServerHost> nodeList, final long selfNodeId) {
        for (final ManagementServerHost node : nodeList) {
            _lockMaster.cleanupForServer(node.getMsid());
        }
    }

    @Override
    public void onManagementNodeIsolated() {
    }
}
