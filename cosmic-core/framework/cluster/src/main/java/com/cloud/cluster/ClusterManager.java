package com.cloud.cluster;

import com.cloud.utils.component.Manager;
import org.apache.cloudstack.framework.config.ConfigKey;

public interface ClusterManager extends Manager {
    static final String ALERT_SUBJECT = "cluster-alert";
    final ConfigKey<Integer> HeartbeatInterval = new ConfigKey<>(Integer.class, "cluster.heartbeat.interval", "management-server", "1500",
            "Interval to check for the heart beat between management server nodes", false);
    final ConfigKey<Integer> HeartbeatThreshold = new ConfigKey<>(Integer.class, "cluster.heartbeat.threshold", "management-server", "150000",
            "Threshold before self-fence the management server", true);

    void OnReceiveClusterServicePdu(ClusterServicePdu pdu);

    /**
     * This executes
     *
     * @param strPeer
     * @param agentId
     * @param cmds
     * @param stopOnError
     * @return
     */
    String execute(String strPeer, long agentId, String cmds, boolean stopOnError);

    /**
     * Broadcast the command to all of the  management server nodes.
     *
     * @param agentId agent id this broadcast is regarding
     * @param cmds    commands to broadcast
     */
    void broadcast(long agentId, String cmds);

    void registerListener(ClusterManagerListener listener);

    void unregisterListener(ClusterManagerListener listener);

    void registerDispatcher(Dispatcher dispatcher);

    ManagementServerHost getPeer(String peerName);

    String getSelfPeerName();

    long getManagementNodeId();

    long getCurrentRunId();

    public long getManagementRunId(long msId);

    public interface Dispatcher {
        String getName();

        String dispatch(ClusterServicePdu pdu);
    }
}
