package com.cloud.network.dao;

import com.cloud.network.dao.ExternalFirewallDeviceVO.FirewallDeviceAllocationState;
import com.cloud.network.dao.ExternalFirewallDeviceVO.FirewallDeviceState;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface ExternalFirewallDeviceDao extends GenericDao<ExternalFirewallDeviceVO, Long> {

    /**
     * list all the firewall devices added in to this physical network?
     *
     * @param physicalNetworkId physical Network Id
     * @return list of ExternalFirewallDeviceVO for the devices added in to this physical network.
     */
    List<ExternalFirewallDeviceVO> listByPhysicalNetwork(long physicalNetworkId);

    /**
     * list the firewall devices added in to this physical network of certain provider type?
     *
     * @param physicalNetworkId physical Network Id
     * @param providerName      netwrok service provider name
     */
    List<ExternalFirewallDeviceVO> listByPhysicalNetworkAndProvider(long physicalNetworkId, String providerName);

    /**
     * list the firewall devices added in to this physical network by their allocation state
     *
     * @param physicalNetworkId physical Network Id
     * @param providerName      netwrok service provider name
     * @param allocationState   firewall device allocation state
     * @return list of ExternalFirewallDeviceVO for the devices in the physical network with a device allocation state
     */
    List<ExternalFirewallDeviceVO> listByProviderAndDeviceAllocationState(long physicalNetworkId, String providerName, FirewallDeviceAllocationState allocationState);

    /**
     * list the load balancer devices added in to this physical network by the device status (enabled/disabled)
     *
     * @param physicalNetworkId physical Network Id
     * @param providerName      netwrok service provider name
     * @param state             firewall device status
     * @return list of ExternalFirewallDeviceVO for the devices in the physical network with a device state
     */
    List<ExternalFirewallDeviceVO> listByProviderAndDeviceStaus(long physicalNetworkId, String providerName, FirewallDeviceState state);
}
