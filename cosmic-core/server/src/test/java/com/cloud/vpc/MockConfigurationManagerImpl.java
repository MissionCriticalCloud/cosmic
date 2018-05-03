package com.cloud.vpc;

import com.cloud.api.command.admin.config.UpdateCfgCmd;
import com.cloud.api.command.admin.network.CreateNetworkOfferingCmd;
import com.cloud.api.command.admin.network.DeleteNetworkOfferingCmd;
import com.cloud.api.command.admin.network.UpdateNetworkOfferingCmd;
import com.cloud.api.command.admin.offering.CreateDiskOfferingCmd;
import com.cloud.api.command.admin.offering.CreateServiceOfferingCmd;
import com.cloud.api.command.admin.offering.DeleteDiskOfferingCmd;
import com.cloud.api.command.admin.offering.DeleteServiceOfferingCmd;
import com.cloud.api.command.admin.offering.UpdateDiskOfferingCmd;
import com.cloud.api.command.admin.offering.UpdateServiceOfferingCmd;
import com.cloud.api.command.admin.pod.DeletePodCmd;
import com.cloud.api.command.admin.pod.UpdatePodCmd;
import com.cloud.api.command.admin.vlan.CreateVlanIpRangeCmd;
import com.cloud.api.command.admin.vlan.DedicatePublicIpRangeCmd;
import com.cloud.api.command.admin.vlan.DeleteVlanIpRangeCmd;
import com.cloud.api.command.admin.vlan.ReleasePublicIpRangeCmd;
import com.cloud.api.command.admin.zone.CreateZoneCmd;
import com.cloud.api.command.admin.zone.DeleteZoneCmd;
import com.cloud.api.command.admin.zone.UpdateZoneCmd;
import com.cloud.api.command.user.network.ListNetworkOfferingsCmd;
import com.cloud.config.Configuration;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.ConfigurationService;
import com.cloud.db.model.Zone;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.legacymodel.dc.DataCenter;
import com.cloud.legacymodel.dc.Pod;
import com.cloud.legacymodel.dc.Vlan;
import com.cloud.legacymodel.domain.Domain;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.storage.DiskOffering;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.model.enumeration.AllocationState;
import com.cloud.model.enumeration.GuestType;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.model.enumeration.TrafficType;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Availability;
import com.cloud.offering.ServiceOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDaoImpl;
import com.cloud.utils.component.ManagerBase;

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
     * @see com.cloud.configuration.ConfigurationService#updateConfiguration(com.cloud.api.commands.UpdateCfgCmd)
     */
    @Override
    public Configuration updateConfiguration(final UpdateCfgCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#createServiceOffering(com.cloud.api.commands.CreateServiceOfferingCmd)
     */
    @Override
    public ServiceOffering createServiceOffering(final CreateServiceOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#updateServiceOffering(com.cloud.api.commands.UpdateServiceOfferingCmd)
     */
    @Override
    public ServiceOffering updateServiceOffering(final UpdateServiceOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#deleteServiceOffering(com.cloud.api.commands.DeleteServiceOfferingCmd)
     */
    @Override
    public boolean deleteServiceOffering(final DeleteServiceOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#updateDiskOffering(com.cloud.api.commands.UpdateDiskOfferingCmd)
     */
    @Override
    public DiskOffering updateDiskOffering(final UpdateDiskOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#deleteDiskOffering(com.cloud.api.commands.DeleteDiskOfferingCmd)
     */
    @Override
    public boolean deleteDiskOffering(final DeleteDiskOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#createDiskOffering(com.cloud.api.commands.CreateDiskOfferingCmd)
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
     * @see com.cloud.configuration.ConfigurationService#editPod(com.cloud.api.commands.UpdatePodCmd)
     */
    @Override
    public Pod editPod(final UpdatePodCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#deletePod(com.cloud.api.commands.DeletePodCmd)
     */
    @Override
    public boolean deletePod(final DeletePodCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#createZone(com.cloud.api.commands.CreateZoneCmd)
     */
    @Override
    public DataCenter createZone(final CreateZoneCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#editZone(com.cloud.api.commands.UpdateZoneCmd)
     */
    @Override
    public DataCenter editZone(final UpdateZoneCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#deleteZone(com.cloud.api.commands.DeleteZoneCmd)
     */
    @Override
    public boolean deleteZone(final DeleteZoneCmd cmd) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#createVlanAndPublicIpRange(com.cloud.api.commands.CreateVlanIpRangeCmd)
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
     * @see com.cloud.configuration.ConfigurationService#deleteVlanIpRange(com.cloud.api.commands.DeleteVlanIpRangeCmd)
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
     * @see com.cloud.configuration.ConfigurationService#createNetworkOffering(com.cloud.api.commands.CreateNetworkOfferingCmd)
     */
    @Override
    public NetworkOffering createNetworkOffering(final CreateNetworkOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#updateNetworkOffering(com.cloud.api.commands.UpdateNetworkOfferingCmd)
     */
    @Override
    public NetworkOffering updateNetworkOffering(final UpdateNetworkOfferingCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#searchForNetworkOfferings(com.cloud.api.commands.ListNetworkOfferingsCmd)
     */
    @Override
    public Pair<List<? extends NetworkOffering>, Integer> searchForNetworkOfferings(final ListNetworkOfferingsCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationService#deleteNetworkOffering(com.cloud.api.commands.DeleteNetworkOfferingCmd)
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

    @Override
    public DataCenterVO createZone(final long userId, final String zoneName, final String dns1, final String dns2, final String internalDns1, final String internalDns2, final String guestCidr,
                                   final String domain, final Long domainId, final NetworkType zoneType, final String allocationState, final String networkDomain, final boolean
                                           isLocalStorageEnabled, final String ip6Dns1, final String ip6Dns2) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#deleteVlanAndPublicIpRange(long, long, com.cloud.legacymodel.user.Account)
     */
    @Override
    public boolean deleteVlanAndPublicIpRange(final long userId, final long vlanDbId, final Account caller) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#checkZoneAccess(com.cloud.legacymodel.user.Account, com.cloud.legacymodel.dc.DataCenter)
     */
    @Override
    public void checkZoneAccess(final Account caller, final Zone zone) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#checkDiskOfferingAccess(com.cloud.legacymodel.user.Account, com.cloud.legacymodel.storage.DiskOffering)
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
                                                   final Long serviceOfferingId, final Long secondaryServiceOfferingId,
                                                   final boolean conserveMode, final Map<Service, Map<Capability, String>> serviceCapabilityMap, final boolean specifyIpRanges,
                                                   final boolean isPersistent,
                                                   final Map<NetworkOffering.Detail, String> details, final boolean egressDefaultPolicy, final Integer maxconn, final boolean
                                                           enableKeepAlive) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.configuration.ConfigurationManager#createVlanAndPublicIpRange(long, long, long, boolean, java.lang.Long, java.lang.String, java.lang.String, java.lang
     * .String, java.lang.String, java.lang.String, com.cloud.legacymodel.user.Account)
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
