package com.cloud.cluster;

import java.util.List;

public class ClusterManagerMessage {
    MessageType _type;

    List<ManagementServerHostVO> _nodes;

    public ClusterManagerMessage(final MessageType type) {
        _type = type;
    }

    public ClusterManagerMessage(final MessageType type, final List<ManagementServerHostVO> nodes) {
        _type = type;
        _nodes = nodes;
    }

    public MessageType getMessageType() {
        return _type;
    }

    public List<ManagementServerHostVO> getNodes() {
        return _nodes;
    }

    public static enum MessageType {
        nodeAdded, nodeRemoved, nodeIsolated
    }
}
