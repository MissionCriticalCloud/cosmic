package com.cloud.storage;

import com.cloud.host.HostVO;
import com.cloud.utils.component.Manager;

import java.util.List;

public interface OCFS2Manager extends Manager {
    static final String CLUSTER_NAME = "clusterName";

    boolean prepareNodes(List<HostVO> hosts, StoragePool pool);

    boolean prepareNodes(Long clusterId);
}
