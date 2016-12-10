package com.cloud.region.gslb;

import com.cloud.agent.api.routing.GlobalLoadBalancerConfigCommand;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.region.RegionServiceProvider;

public interface GslbServiceProvider extends RegionServiceProvider {

    public boolean isServiceEnabledInZone(long zoneId, long physicalNetworkId);

    public String getZoneGslbProviderPublicIp(long zoneId, long physicalNetworkId);

    public String getZoneGslbProviderPrivateIp(long zoneId, long physicalNetworkId);

    public boolean applyGlobalLoadBalancerRule(long zoneId, long physicalNetworkId, GlobalLoadBalancerConfigCommand gslbConfigCmd) throws ResourceUnavailableException;
}
