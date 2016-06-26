package com.cloud.network;

import com.cloud.dc.Vlan;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.GuestType;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.Networks.IsolationType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.element.NetworkElement;
import com.cloud.network.element.UserDataServiceProvider;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Detail;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.user.Account;
import com.cloud.utils.component.ManagerBase;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.VirtualMachine;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockNetworkModelImpl extends ManagerBase implements NetworkModel {

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#getName()
     */
    @Override
    public String getName() {
        return "MockNetworkModelImpl";
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#configure(java.lang.String, java.util.Map)
     */
    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#start()
     */
    @Override
    public boolean start() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#stop()
     */
    @Override
    public boolean stop() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#listPublicIpsAssignedToGuestNtwk(long, long, java.lang.Boolean)
     */
    @Override
    public List<IPAddressVO> listPublicIpsAssignedToGuestNtwk(final long accountId, final long associatedNetworkId, final Boolean sourceNat) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#listPublicIpsAssignedToGuestNtwk(long, long, java.lang.Boolean)
     */
    @Override
    public List<IPAddressVO> listPublicIpsAssignedToGuestNtwk(final long associatedNetworkId, final Boolean sourceNat) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getSystemAccountNetworkOfferings(java.lang.String[])
     */
    @Override
    public List<NetworkOfferingVO> getSystemAccountNetworkOfferings(final String... offeringNames) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNics(long)
     */
    @Override
    public List<? extends Nic> getNics(final long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNextAvailableMacAddressInNetwork(long)
     */
    @Override
    public String getNextAvailableMacAddressInNetwork(final long networkConfigurationId) throws InsufficientAddressCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getPublicIpAddress(long)
     */
    @Override
    public PublicIpAddress getPublicIpAddress(final long ipAddressId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#listPodVlans(long)
     */
    @Override
    public List<? extends Vlan> listPodVlans(final long podId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#listNetworksUsedByVm(long, boolean)
     */
    @Override
    public List<NetworkVO> listNetworksUsedByVm(final long vmId, final boolean isSystem) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNicInNetwork(long, long)
     */
    @Override
    public Nic getNicInNetwork(final long vmId, final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNicsForTraffic(long, com.cloud.network.Networks.TrafficType)
     */
    @Override
    public List<? extends Nic> getNicsForTraffic(final long vmId, final TrafficType type) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getDefaultNetworkForVm(long)
     */
    @Override
    public Network getDefaultNetworkForVm(final long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getDefaultNic(long)
     */
    @Override
    public Nic getDefaultNic(final long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getUserDataUpdateProvider(com.cloud.network.Network)
     */
    @Override
    public UserDataServiceProvider getUserDataUpdateProvider(final Network network) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#networkIsConfiguredForExternalNetworking(long, long)
     */
    @Override
    public boolean networkIsConfiguredForExternalNetworking(final long zoneId, final long networkId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNetworkServiceCapabilities(long, com.cloud.network.Network.Service)
     */
    @Override
    public Map<Capability, String> getNetworkServiceCapabilities(final long networkId, final Service service) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSharedNetworkWithoutServices(final long networkId) {
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#areServicesSupportedByNetworkOffering(long, com.cloud.network.Network.Service[])
     */
    @Override
    public boolean areServicesSupportedByNetworkOffering(final long networkOfferingId, final Service... services) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNetworkWithSGWithFreeIPs(java.lang.Long)
     */
    @Override
    public NetworkVO getNetworkWithSGWithFreeIPs(final Long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNetworkWithSecurityGroupEnabled(java.lang.Long)
     */
    @Override
    public NetworkVO getNetworkWithSecurityGroupEnabled(final Long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getIpOfNetworkElementInVirtualNetwork(long, long)
     */
    @Override
    public String getIpOfNetworkElementInVirtualNetwork(final long accountId, final long dataCenterId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#listNetworksForAccount(long, long, com.cloud.network.Network.GuestType)
     */
    @Override
    public List<NetworkVO> listNetworksForAccount(final long accountId, final long zoneId, final GuestType type) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#listAllNetworksInAllZonesByType(com.cloud.network.Network.GuestType)
     */
    @Override
    public List<NetworkVO> listAllNetworksInAllZonesByType(final GuestType type) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getStartIpAddress(long)
     */
    @Override
    public String getStartIpAddress(final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getIpInNetwork(long, long)
     */
    @Override
    public String getIpInNetwork(final long vmId, final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getIpInNetworkIncludingRemoved(long, long)
     */
    @Override
    public String getIpInNetworkIncludingRemoved(final long vmId, final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getPodIdForVlan(long)
     */
    @Override
    public Long getPodIdForVlan(final long vlanDbId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#listNetworkOfferingsForUpgrade(long)
     */
    @Override
    public List<Long> listNetworkOfferingsForUpgrade(final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#isSecurityGroupSupportedInNetwork(com.cloud.network.Network)
     */
    @Override
    public boolean isSecurityGroupSupportedInNetwork(final Network network) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#isProviderSupportServiceInNetwork(long, com.cloud.network.Network.Service, com.cloud.network.Network.Provider)
     */
    @Override
    public boolean isProviderSupportServiceInNetwork(final long networkId, final Service service, final Provider provider) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#isProviderEnabledInPhysicalNetwork(long, java.lang.String)
     */
    @Override
    public boolean isProviderEnabledInPhysicalNetwork(final long physicalNetowrkId, final String providerName) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNetworkTag(com.cloud.hypervisor.Hypervisor.HypervisorType, com.cloud.network.Network)
     */
    @Override
    public String getNetworkTag(final HypervisorType hType, final Network network) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getElementServices(com.cloud.network.Network.Provider)
     */
    @Override
    public List<Service> getElementServices(final Provider provider) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#canElementEnableIndividualServices(com.cloud.network.Network.Provider)
     */
    @Override
    public boolean canElementEnableIndividualServices(final Provider provider) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#areServicesSupportedInNetwork(long, com.cloud.network.Network.Service[])
     */
    @Override
    public boolean areServicesSupportedInNetwork(final long networkId, final Service... services) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#isNetworkSystem(com.cloud.network.Network)
     */
    @Override
    public boolean isNetworkSystem(final Network network) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNetworkOfferingServiceCapabilities(com.cloud.offering.NetworkOffering, com.cloud.network.Network.Service)
     */
    @Override
    public Map<Capability, String> getNetworkOfferingServiceCapabilities(final NetworkOffering offering, final Service service) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getPhysicalNetworkId(com.cloud.network.Network)
     */
    @Override
    public Long getPhysicalNetworkId(final Network network) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getAllowSubdomainAccessGlobal()
     */
    @Override
    public boolean getAllowSubdomainAccessGlobal() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#isProviderForNetwork(com.cloud.network.Network.Provider, long)
     */
    @Override
    public boolean isProviderForNetwork(final Provider provider, final long networkId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#isProviderForNetworkOffering(com.cloud.network.Network.Provider, long)
     */
    @Override
    public boolean isProviderForNetworkOffering(final Provider provider, final long networkOfferingId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#canProviderSupportServices(java.util.Map)
     */
    @Override
    public void canProviderSupportServices(final Map<Provider, Set<Service>> providersMap) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getPhysicalNetworkInfo(long, com.cloud.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public List<PhysicalNetworkSetupInfo> getPhysicalNetworkInfo(final long dcId, final HypervisorType hypervisorType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#canAddDefaultSecurityGroup()
     */
    @Override
    public boolean canAddDefaultSecurityGroup() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#listNetworkOfferingServices(long)
     */
    @Override
    public List<Service> listNetworkOfferingServices(final long networkOfferingId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#areServicesEnabledInZone(long, com.cloud.offering.NetworkOffering, java.util.List)
     */
    @Override
    public boolean areServicesEnabledInZone(final long zoneId, final NetworkOffering offering, final List<Service> services) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getIpToServices(java.util.List, boolean, boolean)
     */
    @Override
    public Map<PublicIpAddress, Set<Service>> getIpToServices(final List<? extends PublicIpAddress> publicIps, final boolean rulesRevoked, final boolean includingFirewall) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getProviderToIpList(com.cloud.network.Network, java.util.Map)
     */
    @Override
    public Map<Provider, ArrayList<PublicIpAddress>> getProviderToIpList(final Network network, final Map<PublicIpAddress, Set<Service>> ipToServices) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#checkIpForService(com.cloud.network.IPAddressVO, com.cloud.network.Network.Service, java.lang.Long)
     */
    @Override
    public boolean checkIpForService(final IpAddress ip, final Service service, final Long networkId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#checkCapabilityForProvider(java.util.Set, com.cloud.network.Network.Service, com.cloud.network.Network.Capability, java.lang.String)
     */
    @Override
    public void checkCapabilityForProvider(final Set<Provider> providers, final Service service, final Capability cap, final String capValue) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getDefaultUniqueProviderForService(java.lang.String)
     */
    @Override
    public Provider getDefaultUniqueProviderForService(final String serviceName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#checkNetworkPermissions(com.cloud.user.Account, com.cloud.network.Network)
     */
    @Override
    public void checkNetworkPermissions(final Account owner, final Network network) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getDefaultManagementTrafficLabel(long, com.cloud.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public String getDefaultManagementTrafficLabel(final long zoneId, final HypervisorType hypervisorType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getDefaultStorageTrafficLabel(long, com.cloud.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public String getDefaultStorageTrafficLabel(final long zoneId, final HypervisorType hypervisorType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getDefaultPublicTrafficLabel(long, com.cloud.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public String getDefaultPublicTrafficLabel(final long dcId, final HypervisorType hypervisor) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getDefaultGuestTrafficLabel(long, com.cloud.hypervisor.Hypervisor.HypervisorType)
     */
    @Override
    public String getDefaultGuestTrafficLabel(final long dcId, final HypervisorType hypervisor) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getElementImplementingProvider(java.lang.String)
     */
    @Override
    public NetworkElement getElementImplementingProvider(final String providerName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getAccountNetworkDomain(long, long)
     */
    @Override
    public String getAccountNetworkDomain(final long accountId, final long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNtwkOffDistinctProviders(long)
     */
    @Override
    public List<Provider> getNtwkOffDistinctProviders(final long ntwkOffId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#listPublicIpsAssignedToAccount(long, long, java.lang.Boolean)
     */
    @Override
    public List<IPAddressVO> listPublicIpsAssignedToAccount(final long accountId, final long dcId, final Boolean sourceNat) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getPhysicalNtwksSupportingTrafficType(long, com.cloud.network.Networks.TrafficType)
     */
    @Override
    public List<? extends PhysicalNetwork> getPhysicalNtwksSupportingTrafficType(final long zoneId, final TrafficType trafficType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#isPrivateGateway(com.cloud.vm.Nic)
     */
    @Override
    public boolean isPrivateGateway(final long ntwkId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNetworkCapabilities(long)
     */
    @Override
    public Map<Service, Map<Capability, String>> getNetworkCapabilities(final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getSystemNetworkByZoneAndTrafficType(long, com.cloud.network.Networks.TrafficType)
     */
    @Override
    public Network getSystemNetworkByZoneAndTrafficType(final long zoneId, final TrafficType trafficType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getDedicatedNetworkDomain(long)
     */
    @Override
    public Long getDedicatedNetworkDomain(final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNetworkOfferingServiceProvidersMap(long)
     */
    @Override
    public Map<Service, Set<Provider>> getNetworkOfferingServiceProvidersMap(final long networkOfferingId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#listSupportedNetworkServiceProviders(java.lang.String)
     */
    @Override
    public List<? extends Provider> listSupportedNetworkServiceProviders(final String serviceName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#listNetworksByVpc(long)
     */
    @Override
    public List<? extends Network> listNetworksByVpc(final long vpcId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#canUseForDeploy(com.cloud.network.Network)
     */
    @Override
    public boolean canUseForDeploy(final Network network) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getExclusiveGuestNetwork(long)
     */
    @Override
    public Network getExclusiveGuestNetwork(final long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#findPhysicalNetworkId(long, java.lang.String, com.cloud.network.Networks.TrafficType)
     */
    @Override
    public long findPhysicalNetworkId(final long zoneId, final String tag, final TrafficType trafficType) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNetworkRate(long, java.lang.Long)
     */
    @Override
    public Integer getNetworkRate(final long networkId, final Long vmId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#isVmPartOfNetwork(long, long)
     */
    @Override
    public boolean isVmPartOfNetwork(final long vmId, final long ntwkId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getDefaultPhysicalNetworkByZoneAndTrafficType(long, com.cloud.network.Networks.TrafficType)
     */
    @Override
    public PhysicalNetwork getDefaultPhysicalNetworkByZoneAndTrafficType(final long zoneId, final TrafficType trafficType) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNetwork(long)
     */
    @Override
    public Network getNetwork(final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getIp(long)
     */
    @Override
    public IpAddress getIp(final long sourceIpAddressId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#isNetworkAvailableInDomain(long, long)
     */
    @Override
    public boolean isNetworkAvailableInDomain(final long networkId, final long domainId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getNicProfile(com.cloud.vm.VirtualMachine, long, java.lang.String)
     */
    @Override
    public NicProfile getNicProfile(final VirtualMachine vm, final long networkId, final String broadcastUri) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getAvailableIps(com.cloud.network.Network, java.lang.String)
     */
    @Override
    public Set<Long> getAvailableIps(final Network network, final String requestedIp) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getDomainNetworkDomain(long, long)
     */
    @Override
    public String getDomainNetworkDomain(final long domainId, final long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#getSourceNatIpAddressForGuestNetwork(com.cloud.user.Account, com.cloud.network.Network)
     */
    @Override
    public PublicIpAddress getSourceNatIpAddressForGuestNetwork(final Account owner, final Network guestNetwork) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkModel#isNetworkInlineMode(com.cloud.network.Network)
     */
    @Override
    public boolean isNetworkInlineMode(final Network network) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isIP6AddressAvailableInNetwork(final long networkId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isIP6AddressAvailableInVlan(final long vlanId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void checkIp6Parameters(final String startIPv6, final String endIPv6, final String ip6Gateway, final String ip6Cidr) throws InvalidParameterValueException {
        // TODO Auto-generated method stub

    }

    @Override
    public void checkRequestedIpAddresses(final long networkId, final String ip4, final String ip6) throws InvalidParameterValueException {
        // TODO Auto-generated method stub
    }

    @Override
    public String getStartIpv6Address(final long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isProviderEnabledInZone(final long zoneId, final String provider) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Nic getPlaceholderNicForRouter(final Network network, final Long podId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IpAddress getPublicIpAddress(final String ipAddress, final long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getUsedIpsInNetwork(final Network network) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Detail, String> getNtwkOffDetails(final long offId) {
        return null;
    }

    @Override
    public IsolationType[] listNetworkIsolationMethods() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Nic getNicInNetworkIncludingRemoved(final long vmId, final long networkId) {
        return null;
    }

    @Override
    public boolean getExecuteInSeqNtwkElmtCmd() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNetworkReadyForGc(final long networkId) {
        return true;
    }

    @Override
    public boolean getNetworkEgressDefaultPolicy(final Long networkId) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String[]> generateVmData(final String userData, final String serviceOffering, final String zoneName, final String vmName, final long vmId, final String
            publicKey, final String password, final Boolean isWindows) {
        return null;
    }
}
