package org.apache.cloudstack.network.element;

import com.cloud.network.VirtualRouterProvider;
import com.cloud.utils.component.PluggableService;

import java.util.List;

public interface InternalLoadBalancerElementService extends PluggableService {
    /**
     * Configures existing Internal Load Balancer Element (enables or disables it)
     *
     * @param id
     * @param enable
     * @return
     */
    VirtualRouterProvider configureInternalLoadBalancerElement(long id, boolean enable);

    /**
     * Adds Internal Load Balancer element to the Network Service Provider
     *
     * @param ntwkSvcProviderId
     * @return
     */
    VirtualRouterProvider addInternalLoadBalancerElement(long ntwkSvcProviderId);

    /**
     * Retrieves existing Internal Load Balancer element
     *
     * @param id
     * @return
     */
    VirtualRouterProvider getInternalLoadBalancerElement(long id);

    /**
     * Searches for existing Internal Load Balancer elements based on parameters passed to the call
     *
     * @param id
     * @param ntwkSvsProviderId
     * @param enabled
     * @return
     */
    List<? extends VirtualRouterProvider> searchForInternalLoadBalancerElements(Long id, Long ntwkSvsProviderId, Boolean enabled);
}
