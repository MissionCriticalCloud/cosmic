package org.apache.cloudstack.network.lb;

import com.cloud.network.rules.LoadBalancerContainer;
import com.cloud.utils.net.Ip;

public interface ApplicationLoadBalancerContainer extends LoadBalancerContainer {

    public Long getSourceIpNetworkId();

    public Ip getSourceIp();
}
