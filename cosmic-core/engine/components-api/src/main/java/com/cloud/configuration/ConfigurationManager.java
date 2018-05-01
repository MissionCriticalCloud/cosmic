package com.cloud.configuration;

import com.cloud.db.model.Zone;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.domain.Domain;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.legacymodel.dc.Pod;
import com.cloud.legacymodel.dc.Vlan;
import com.cloud.legacymodel.user.Account;
import com.cloud.model.enumeration.AllocationState;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.Networks.TrafficType;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.utils.exception.InvalidParameterValueException;

import java.util.Map;
import java.util.Set;

/**
 * ConfigurationManager handles adding pods/zones, changing IP ranges, enabling external firewalls, and editing
 * configuration values
 */
public interface ConfigurationManager {
    /**
     * @param offering
     * @return
     */
    boolean isOfferingForVpc(NetworkOffering offering);

    Integer getNetworkOfferingNetworkRate(long networkOfferingId, Long dataCenterId);

    Integer getServiceOfferingNetworkRate(long serviceOfferingId, Long dataCenterId);

    /**
     * Updates a configuration entry with a new value
     *
     * @param userId
     * @param name
     * @param value
     */
    String updateConfiguration(long userId, String name, String category, String value, String scope, Long id);

    /**
     * Creates a new pod
     *
     * @param userId
     * @param podName
     * @param zoneId
     * @param gateway
     * @param cidr
     * @param startIp
     * @param endIp
     * @param allocationState
     * @param skipGatewayOverlapCheck (true if it is ok to not validate that gateway IP address overlap with Start/End IP of the POD)
     * @return Pod
     */
    HostPodVO createPod(long userId, String podName, long zoneId, String gateway, String cidr, String startIp, String endIp, String allocationState,
                        boolean skipGatewayOverlapCheck);

    /**
     * Creates a new zone
     *
     * @param userId
     * @param zoneName
     * @param dns1
     * @param dns2
     * @param internalDns1
     * @param internalDns2
     * @param guestCidr
     * @param zoneType
     * @param allocationState
     * @param networkDomain   TODO
     * @param ip6Dns1         TODO
     * @param ip6Dns2         TODO
     * @return
     * @throws
     * @throws
     */
    DataCenterVO createZone(long userId, String zoneName, String dns1, String dns2, String internalDns1, String internalDns2, String guestCidr, String domain, Long domainId, NetworkType zoneType,
                            String allocationState, String networkDomain, boolean isLocalStorageEnabled, String ip6Dns1, String ip6Dns2);

    /**
     * Deletes a VLAN from the database, along with all of its IP addresses. Will not delete VLANs that have allocated
     * IP addresses.
     *
     * @param userId
     * @param vlanDbId
     * @param caller   TODO
     * @return success/failure
     */
    boolean deleteVlanAndPublicIpRange(long userId, long vlanDbId, Account caller);

    void checkZoneAccess(Account caller, Zone zone);

    void checkDiskOfferingAccess(Account caller, DiskOffering dof);

    /**
     * Creates a new network offering
     */

    NetworkOfferingVO createNetworkOffering(String name, String displayText, TrafficType trafficType, String tags, boolean specifyVlan, Availability availability,
                                            Integer networkRate, Map<Service, Set<Provider>> serviceProviderMap, boolean isDefault, Network.GuestType type, boolean systemOnly,
                                            Long serviceOfferingId, Long secondaryServiceOfferingId,
                                            boolean conserveMode, Map<Service, Map<Capability, String>> serviceCapabilityMap, boolean specifyIpRanges, boolean isPersistent,
                                            Map<NetworkOffering.Detail, String> details, boolean egressDefaultPolicy, Integer maxconn, boolean enableKeepAlive);

    Vlan createVlanAndPublicIpRange(long zoneId, long networkId, long physicalNetworkId, boolean forVirtualNetwork, Long podId, String startIP, String endIP,
                                    String vlanGateway, String vlanNetmask, String vlanId, Domain domain, Account vlanOwner, String startIPv6, String endIPv6, String
                                            vlanIp6Gateway, String vlanIp6Cidr)
            throws InsufficientCapacityException, ConcurrentOperationException, InvalidParameterValueException;

    void createDefaultSystemNetworks(long zoneId) throws ConcurrentOperationException;

    boolean releaseAccountSpecificVirtualRanges(long accountId);

    /**
     * Edits a pod in the database. Will not allow you to edit pods that are being used anywhere in the system.
     */
    Pod editPod(long id, String name, String startIp, String endIp, String gateway, String netmask, String allocationStateStr);

    void checkPodCidrSubnets(long zoneId, Long podIdToBeSkipped, String cidr);

    AllocationState findPodAllocationState(HostPodVO pod);

    AllocationState findClusterAllocationState(ClusterVO cluster);
}
