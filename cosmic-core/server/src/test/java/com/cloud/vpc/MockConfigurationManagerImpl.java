package com.cloud.vpc;

import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.ConfigurationService;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.Pod;
import com.cloud.dc.Vlan;
import com.cloud.domain.Domain;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.GuestType;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.Networks.TrafficType;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.offering.ServiceOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDaoImpl;
import com.cloud.org.Grouping.AllocationState;
import com.cloud.user.Account;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ManagerBase;
import org.apache.cloudstack.api.command.admin.config.UpdateCfgCmd;
import org.apache.cloudstack.api.command.admin.network.CreateNetworkOfferingCmd;
import org.apache.cloudstack.api.command.admin.network.DeleteNetworkOfferingCmd;
import org.apache.cloudstack.api.command.admin.network.UpdateNetworkOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.CreateDiskOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.CreateServiceOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.DeleteDiskOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.DeleteServiceOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.UpdateDiskOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.UpdateServiceOfferingCmd;
import org.apache.cloudstack.api.command.admin.pod.DeletePodCmd;
import org.apache.cloudstack.api.command.admin.pod.UpdatePodCmd;
import org.apache.cloudstack.api.command.admin.region.CreatePortableIpRangeCmd;
import org.apache.cloudstack.api.command.admin.region.DeletePortableIpRangeCmd;
import org.apache.cloudstack.api.command.admin.region.ListPortableIpRangesCmd;
import org.apache.cloudstack.api.command.admin.vlan.CreateVlanIpRangeCmd;
import org.apache.cloudstack.api.command.admin.vlan.DedicatePublicIpRangeCmd;
import org.apache.cloudstack.api.command.admin.vlan.DeleteVlanIpRangeCmd;
import org.apache.cloudstack.api.command.admin.vlan.ReleasePublicIpRangeCmd;
import org.apache.cloudstack.api.command.admin.zone.CreateZoneCmd;
import org.apache.cloudstack.api.command.admin.zone.DeleteZoneCmd;
import org.apache.cloudstack.api.command.admin.zone.UpdateZoneCmd;
import org.apache.cloudstack.api.command.user.network.ListNetworkOfferingsCmd;
import org.apache.cloudstack.config.Configuration;
import org.apache.cloudstack.region.PortableIp;
import org.apache.cloudstack.region.PortableIpRange;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class MockConfigurationManagerImpl extends ManagerBase implements ConfigurationManager, ConfigurationService {
    @Inject
    NetworkOfferingDaoImpl _ntwkOffDao;

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#updateConfiguration(org.apache.cloudstack.api.commands.UpdateCfgCmd)
     */
    @Override
    public Configuration updateConfiguration(final UpdateCfgCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#createServiceOffering(org.apache.cloudstack.api.commands.CreateServiceOfferingCmd)
     */
    @Override
    public ServiceOffering createServiceOffering(final CreateServiceOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#updateServiceOffering(org.apache.cloudstack.api.commands.UpdateServiceOfferingCmd)
     */
    @Override
    public ServiceOffering updateServiceOffering(final UpdateServiceOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#deleteServiceOffering(org.apache.cloudstack.api.commands.DeleteServiceOfferingCmd)
     */
    @Override
    public boolean deleteServiceOffering(final DeleteServiceOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#updateDiskOffering(org.apache.cloudstack.api.commands.UpdateDiskOfferingCmd)
     */
    @Override
    public DiskOffering updateDiskOffering(final UpdateDiskOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#deleteDiskOffering(org.apache.cloudstack.api.commands.DeleteDiskOfferingCmd)
     */
    @Override
    public boolean deleteDiskOffering(final DeleteDiskOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#createDiskOffering(org.apache.cloudstack.api.commands.CreateDiskOfferingCmd)
     */
    @Override
    public DiskOffering createDiskOffering(final CreateDiskOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#createPod(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Pod createPod(final long zoneId, final String name, final String startIp, final String endIp, final String gateway, final String netmask, final String allocationState) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#editPod(org.apache.cloudstack.api.commands.UpdatePodCmd)
     */
    @Override
    public Pod editPod(final UpdatePodCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#deletePod(org.apache.cloudstack.api.commands.DeletePodCmd)
     */
    @Override
    public boolean deletePod(final DeletePodCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#createZone(org.apache.cloudstack.api.commands.CreateZoneCmd)
     */
    @Override
    public DataCenter createZone(final CreateZoneCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#editZone(org.apache.cloudstack.api.commands.UpdateZoneCmd)
     */
    @Override
    public DataCenter editZone(final UpdateZoneCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#deleteZone(org.apache.cloudstack.api.commands.DeleteZoneCmd)
     */
    @Override
    public boolean deleteZone(final DeleteZoneCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#createVlanAndPublicIpRange(org.apache.cloudstack.api.commands.CreateVlanIpRangeCmd)
     */
    @Override
    public Vlan createVlanAndPublicIpRange(final CreateVlanIpRangeCmd cmd) throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException,
            ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#markDefaultZone(java.lang.String, long, long)
     */
    @Override
    public Account markDefaultZone(final String accountName, final long domainId, final long defaultZoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#deleteVlanIpRange(org.apache.cloudstack.api.commands.DeleteVlanIpRangeCmd)
     */
    @Override
    public boolean deleteVlanIpRange(final DeleteVlanIpRangeCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Vlan dedicatePublicIpRange(final DedicatePublicIpRangeCmd cmd) throws ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean releasePublicIpRange(final ReleasePublicIpRangeCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#createNetworkOffering(org.apache.cloudstack.api.commands.CreateNetworkOfferingCmd)
     */
    @Override
    public NetworkOffering createNetworkOffering(final CreateNetworkOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#updateNetworkOffering(org.apache.cloudstack.api.commands.UpdateNetworkOfferingCmd)
     */
    @Override
    public NetworkOffering updateNetworkOffering(final UpdateNetworkOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#searchForNetworkOfferings(org.apache.cloudstack.api.commands.ListNetworkOfferingsCmd)
     */
    @Override
    public Pair<List<? extends NetworkOffering>, Integer> searchForNetworkOfferings(final ListNetworkOfferingsCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#deleteNetworkOffering(org.apache.cloudstack.api.commands.DeleteNetworkOfferingCmd)
     */
    @Override
    public boolean deleteNetworkOffering(final DeleteNetworkOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#getVlanAccount(long)
     */
    @Override
    public Account getVlanAccount(final long vlanId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Domain getVlanDomain(final long vlanId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#listNetworkOfferings(com.cloud.network.Networks.TrafficType, boolean)
     */
    @Override
    public List<? extends NetworkOffering> listNetworkOfferings(final TrafficType trafficType, final boolean systemOnly) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#getDefaultPageSize()
     */
    @Override
    public Long getDefaultPageSize() {
        return 500L;
    }

    @Override
    public PortableIpRange createPortableIpRange(final CreatePortableIpRangeCmd cmd) throws ConcurrentOperationException {
        return null;// TODO Auto-generated method stub
    }

    @Override
    public boolean deletePortableIpRange(final DeletePortableIpRangeCmd cmd) {
        return false;// TODO Auto-generated method stub
    }

    @Override
    public List<? extends PortableIpRange> listPortableIpRanges(final ListPortableIpRangesCmd cmd) {
        return null;// TODO Auto-generated method stub
    }

    @Override
    public List<? extends PortableIp> listPortableIps(final long id) {
        return null;// TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#isOfferingForVpc(com.cloud.offering.NetworkOffering)
     */
    @Override
    public boolean isOfferingForVpc(final NetworkOffering offering) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#getNetworkOfferingNetworkRate(long)
     */
    @Override
    public Integer getNetworkOfferingNetworkRate(final long networkOfferingId, final Long dataCenterId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#getServiceOfferingNetworkRate(long)
     */
    @Override
    public Integer getServiceOfferingNetworkRate(final long serviceOfferingId, final Long dataCenterId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#updateConfiguration(long, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String updateConfiguration(final long userId, final String name, final String category, final String value, final String scope, final Long resourceId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#createPod(long, java.lang.String, long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java
     * .lang.String, boolean)
     */
    @Override
    public HostPodVO createPod(final long userId, final String podName, final long zoneId, final String gateway, final String cidr, final String startIp, final String endIp,
                               final String allocationState,
                               final boolean skipGatewayOverlapCheck) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#createZone(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang
     * .String, java.lang.String, java.lang.Long, com.cloud.dc.DataCenter.NetworkType, java.lang.String, java.lang.String, boolean, boolean)
     */
    @Override
    public DataCenterVO createZone(final long userId, final String zoneName, final String dns1, final String dns2, final String internalDns1, final String internalDns2, final
    String guestCidr, final String domain,
                                   final Long domainId, final NetworkType zoneType, final String allocationState, final String networkDomain, final boolean
                                           isSecurityGroupEnabled, final boolean
                                           isLocalStorageEnabled, final String ip6Dns1,
                                   final String ip6Dns2) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#deleteVlanAndPublicIpRange(long, long, com.cloud.user.Account)
     */
    @Override
    public boolean deleteVlanAndPublicIpRange(final long userId, final long vlanDbId, final Account caller) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#checkZoneAccess(com.cloud.user.Account, com.cloud.dc.DataCenter)
     */
    @Override
    public void checkZoneAccess(final Account caller, final DataCenter zone) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#checkDiskOfferingAccess(com.cloud.user.Account, com.cloud.offering.DiskOffering)
     */
    @Override
    public void checkDiskOfferingAccess(final Account caller, final DiskOffering dof) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#createNetworkOffering(java.lang.String, java.lang.String, com.cloud.network.Networks.TrafficType, java.lang.String,
     * boolean, com.cloud.offering.NetworkOffering.Availability, java.lang.Integer, java.util.Map, boolean, com.cloud.network.Network.GuestType, boolean, java.lang.Long,
     * boolean, java.util.Map, boolean)
     */
    @Override
    public NetworkOfferingVO createNetworkOffering(final String name, final String displayText, final TrafficType trafficType, final String tags, final boolean specifyVlan,
                                                   final Availability availability,
                                                   final Integer networkRate, final Map<Service, Set<Provider>> serviceProviderMap, final boolean isDefault, final GuestType
                                                           type, final boolean systemOnly,
                                                   final Long serviceOfferingId,
                                                   final boolean conserveMode, final Map<Service, Map<Capability, String>> serviceCapabilityMap, final boolean specifyIpRanges,
                                                   final boolean isPersistent,
                                                   final Map<NetworkOffering.Detail, String> details, final boolean egressDefaultPolicy, final Integer maxconn, final boolean
                                                           enableKeepAlive) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#createVlanAndPublicIpRange(long, long, long, boolean, java.lang.Long, java.lang.String, java.lang.String, java.lang
     * .String, java.lang.String, java.lang.String, com.cloud.user.Account)
     */
    @Override
    public Vlan createVlanAndPublicIpRange(final long zoneId, final long networkId, final long physicalNetworkId, final boolean forVirtualNetwork, final Long podId, final String
            startIP, final String endIP,
                                           final String vlanGateway, final String vlanNetmask, final String vlanId, final Domain domain, final Account vlanOwner, final String
                                                   startIPv6, final String endIPv6, final String
                                                   vlanGatewayv6, final String vlanCidrv6)
            throws InsufficientCapacityException, ConcurrentOperationException, InvalidParameterValueException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#createDefaultSystemNetworks(long)
     */
    @Override
    public void createDefaultSystemNetworks(final long zoneId) throws ConcurrentOperationException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#deleteAccountSpecificVirtualRanges(long)
     */
    @Override
    public boolean releaseAccountSpecificVirtualRanges(final long accountId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#editPod(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Pod editPod(final long id, final String name, final String startIp, final String endIp, final String gateway, final String netmask, final String allocationStateStr) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#checkPodCidrSubnets(long, java.lang.Long, java.lang.String)
     */
    @Override
    public void checkPodCidrSubnets(final long zoneId, final Long podIdToBeSkipped, final String cidr) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#findPodAllocationState(com.cloud.dc.HostPodVO)
     */
    @Override
    public AllocationState findPodAllocationState(final HostPodVO pod) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#findClusterAllocationState(com.cloud.dc.ClusterVO)
     */
    @Override
    public AllocationState findClusterAllocationState(final ClusterVO cluster) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#getName()
     */
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#configure(java.lang.String, java.util.Map)
     */
    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#start()
     */
    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#stop()
     */
    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return true;
    }
}
