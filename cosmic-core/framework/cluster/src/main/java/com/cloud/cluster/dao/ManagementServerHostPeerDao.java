package com.cloud.cluster.dao;

import com.cloud.cluster.ManagementServerHost;
import com.cloud.cluster.ManagementServerHostPeerVO;
import com.cloud.utils.db.GenericDao;

public interface ManagementServerHostPeerDao extends GenericDao<ManagementServerHostPeerVO, Long> {
    void clearPeerInfo(long ownerMshost);

    void updatePeerInfo(long ownerMshost, long peerMshost, long peerRunid, ManagementServerHost.State peerState);

    int countStateSeenInPeers(long mshost, long runid, ManagementServerHost.State state);
}
