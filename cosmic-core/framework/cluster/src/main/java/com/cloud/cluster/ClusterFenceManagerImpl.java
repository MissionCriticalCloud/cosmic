package com.cloud.cluster;

import com.cloud.utils.component.ManagerBase;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ClusterFenceManagerImpl extends ManagerBase implements ClusterFenceManager, ClusterManagerListener {
    private static final Logger s_logger = LoggerFactory.getLogger(ClusterFenceManagerImpl.class);

    @Inject
    ClusterManager _clusterMgr;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _clusterMgr.registerListener(this);
        return true;
    }

    @Override
    public void onManagementNodeJoined(final List<? extends ManagementServerHost> nodeList, final long selfNodeId) {
    }

    @Override
    public void onManagementNodeLeft(final List<? extends ManagementServerHost> nodeList, final long selfNodeId) {
    }

    @Override
    public void onManagementNodeIsolated() {
        s_logger.error("Received node isolation notification, will perform self-fencing and shut myself down");
        System.exit(SELF_FENCING_EXIT_CODE);
    }
}
