package com.cloud.network;

import com.cloud.utils.component.Manager;

/* ExternalDeviceUsageManager implements a periodic task that retrieves and updates the network usage stats from all external load balancer and firewall devices.
 */

public interface ExternalDeviceUsageManager extends Manager {

    /**
     * updates the network usage stats for a LB rule, associated with an external LB device, that is being revoked as part of Delete LB rule or release IP actions
     *
     * @param loadBalancerRuleId
     */
    public void updateExternalLoadBalancerNetworkUsageStats(long loadBalancerRuleId);
}
