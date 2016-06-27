package com.cloud.cluster;

import com.cloud.utils.events.EventArgs;

import java.util.List;

public class ClusterNodeJoinEventArgs extends EventArgs {
    private static final long serialVersionUID = 6284545402661799476L;

    private final List<ManagementServerHostVO> joinedNodes;
    private final Long self;

    public ClusterNodeJoinEventArgs(final Long self, final List<ManagementServerHostVO> joinedNodes) {
        super(ClusterManager.ALERT_SUBJECT);

        this.self = self;
        this.joinedNodes = joinedNodes;
    }

    public List<ManagementServerHostVO> getJoinedNodes() {
        return joinedNodes;
    }

    public Long getSelf() {
        return self;
    }
}
