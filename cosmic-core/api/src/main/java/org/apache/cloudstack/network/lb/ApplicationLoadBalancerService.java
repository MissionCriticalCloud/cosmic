package org.apache.cloudstack.network.lb;

import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.command.user.loadbalancer.ListApplicationLoadBalancersCmd;

import java.util.List;

public interface ApplicationLoadBalancerService {

    ApplicationLoadBalancerRule createApplicationLoadBalancer(String name, String description, Scheme scheme, long sourceIpNetworkId, String sourceIp, int sourcePort,
                                                              int instancePort, String algorithm, long networkId, long lbOwnerId, Boolean forDisplay) throws
            InsufficientAddressCapacityException, NetworkRuleConflictException,
            InsufficientVirtualNetworkCapacityException;

    boolean deleteApplicationLoadBalancer(long id);

    Pair<List<? extends ApplicationLoadBalancerRule>, Integer> listApplicationLoadBalancers(ListApplicationLoadBalancersCmd cmd);

    ApplicationLoadBalancerRule getApplicationLoadBalancer(long ruleId);

    ApplicationLoadBalancerRule updateApplicationLoadBalancer(Long id, String customId, Boolean forDisplay);
}
