package com.cloud.cluster.agentlb;

import com.cloud.host.HostVO;
import com.cloud.utils.component.Adapter;

import java.util.List;

public interface AgentLoadBalancerPlanner extends Adapter {

    List<HostVO> getHostsToRebalance(long msId, int avLoad);
}
