package com.cloud.vm;

import com.cloud.cluster.ClusterManager;
import com.cloud.cluster.ClusterManagerListener;
import com.cloud.cluster.ManagementServerHost;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

public class ClusteredVirtualMachineManagerImpl extends VirtualMachineManagerImpl implements ClusterManagerListener {

    @Inject
    ClusterManager _clusterMgr;

    protected ClusteredVirtualMachineManagerImpl() {
    }

    @Override
    public void onManagementNodeJoined(final List<? extends ManagementServerHost> nodeList, final long selfNodeId) {

    }

    @Override
    public void onManagementNodeLeft(final List<? extends ManagementServerHost> nodeList, final long selfNodeId) {
        for (final ManagementServerHost node : nodeList) {
            cancelWorkItems(node.getMsid());
        }
    }

    @Override
    public void onManagementNodeIsolated() {
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> xmlParams) throws ConfigurationException {
        super.configure(name, xmlParams);

        _clusterMgr.registerListener(this);

        return true;
    }
}
