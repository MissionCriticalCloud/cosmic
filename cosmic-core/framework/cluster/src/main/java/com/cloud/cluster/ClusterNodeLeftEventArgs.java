package com.cloud.cluster;

import com.cloud.utils.events.EventArgs;

import java.util.List;

public class ClusterNodeLeftEventArgs extends EventArgs {
    private static final long serialVersionUID = 7236743316223611935L;

    private final List<ManagementServerHostVO> leftNodes;
    private final Long self;

    public ClusterNodeLeftEventArgs(final Long self, final List<ManagementServerHostVO> leftNodes) {
        super(ClusterManager.ALERT_SUBJECT);

        this.self = self;
        this.leftNodes = leftNodes;
    }

    public List<ManagementServerHostVO> getLeftNodes() {
        return leftNodes;
    }

    public Long getSelf() {
        return self;
    }
}
