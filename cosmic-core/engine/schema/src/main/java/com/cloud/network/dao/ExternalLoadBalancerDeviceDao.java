package com.cloud.network.dao;

import com.cloud.network.dao.ExternalLoadBalancerDeviceVO.LBDeviceAllocationState;
import com.cloud.network.dao.ExternalLoadBalancerDeviceVO.LBDeviceState;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface ExternalLoadBalancerDeviceDao extends GenericDao<ExternalLoadBalancerDeviceVO, Long> {

    /**
     * list all the load balancer devices added in to this physical network?
     *
     * @param physicalNetworkId physical Network Id
     * @return list of ExternalLoadBalancerDeviceVO for the devices in the physical network.
     */
    List<ExternalLoadBalancerDeviceVO> listByPhysicalNetwork(long physicalNetworkId);

    /**
     * list the load balancer devices added in to this physical network of certain provider type?
     *
     * @param physicalNetworkId physical Network Id
     * @param providerName      netwrok service provider name
     */
    List<ExternalLoadBalancerDeviceVO> listByPhysicalNetworkAndProvider(long physicalNetworkId, String providerName);

    /**
     * list the load balancer devices added in to this physical network by their allocation state
     *
     * @param physicalNetworkId physical Network Id
     * @param providerName      netwrok service provider name
     * @param allocationState   load balancer device allocation state
     * @return list of ExternalLoadBalancerDeviceVO for the devices in the physical network with a device allocation state
     */
    List<ExternalLoadBalancerDeviceVO> listByProviderAndDeviceAllocationState(long physicalNetworkId, String providerName, LBDeviceAllocationState allocationState);

    /**
     * list the load balancer devices added in to this physical network by the device status (enabled/disabled)
     *
     * @param physicalNetworkId physical Network Id
     * @param providerName      netwrok service provider name
     * @param state             load balancer device status
     * @return list of ExternalLoadBalancerDeviceVO for the devices in the physical network with a device state
     */
    List<ExternalLoadBalancerDeviceVO> listByProviderAndDeviceStaus(long physicalNetworkId, String providerName, LBDeviceState state);

    /**
     * list the load balancer devices added in to this physical network by the managed type (external/cloudstack managed)
     *
     * @param physicalNetworkId physical Network Id
     * @param providerName      netwrok service provider name
     * @param managed           managed type
     * @return list of ExternalLoadBalancerDeviceVO for the devices in to this physical network of a managed type
     */
    List<ExternalLoadBalancerDeviceVO> listByProviderAndManagedType(long physicalNetworkId, String providerName, boolean managed);

    /**
     * Find the external load balancer device that is provisioned as GSLB service provider in the pyshical network
     *
     * @param physicalNetworkId physical Network Id
     * @return ExternalLoadBalancerDeviceVO for the device acting as GSLB provider in the physical network
     */
    ExternalLoadBalancerDeviceVO findGslbServiceProvider(long physicalNetworkId, String providerName);
}
