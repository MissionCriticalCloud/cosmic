package com.cloud.network;

import com.cloud.dc.Vlan;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.IpAddresses;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.element.NetworkElement;
import com.cloud.network.element.UserDataServiceProvider;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Detail;
import com.cloud.user.Account;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.VirtualMachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * The NetworkModel presents a read-only view into the Network data such as L2 networks,
 * Nics, PublicIps, NetworkOfferings, traffic labels, physical networks and the like
 * The idea is that only the orchestration core should be able to modify the data, while other
 * participants in the orchestration can use this interface to query the data.
 */
public interface NetworkModel {

    List<? extends IpAddress> listPublicIpsAssignedToGuestNtwk(long accountId, long associatedNetworkId, Boolean sourceNat);

    List<? extends IpAddress> listPublicIpsAssignedToGuestNtwk(long associatedNetworkId, Boolean sourceNat);

    List<? extends NetworkOffering> getSystemAccountNetworkOfferings(String... offeringNames);

    List<? extends Nic> getNics(long vmId);

    String getNextAvailableMacAddressInNetwork(long networkConfigurationId) throws InsufficientAddressCapacityException;

    PublicIpAddress getPublicIpAddress(long ipAddressId);

    List<? extends Vlan> listPodVlans(long podId);

    Nic getNicInNetwork(long vmId, long networkId);

    List<? extends Nic> getNicsForTraffic(long vmId, TrafficType type);

    Network getDefaultNetworkForVm(long vmId);

    Nic getDefaultNic(long vmId);

    UserDataServiceProvider getUserDataUpdateProvider(Network network);

    Map<Capability, String> getNetworkServiceCapabilities(long networkId, Service service);

    boolean isSharedNetworkWithoutServices(long networkId);

    boolean areServicesSupportedByNetworkOffering(long networkOfferingId, Service... services);

    Network getNetworkWithSGWithFreeIPs(Long zoneId);

    Network getNetworkWithSecurityGroupEnabled(Long zoneId);

    List<? extends Network> listNetworksForAccount(long accountId, long zoneId, Network.GuestType type);

    String getStartIpAddress(long networkId);

    Long getPodIdForVlan(long vlanDbId);

    List<Long> listNetworkOfferingsForUpgrade(long networkId);

    boolean isSecurityGroupSupportedInNetwork(Network network);

    boolean isProviderSupportServiceInNetwork(long networkId, Service service, Provider provider);

    boolean isProviderEnabledInPhysicalNetwork(long physicalNetowrkId, String providerName);

    String getNetworkTag(HypervisorType hType, Network network);

    List<Service> getElementServices(Provider provider);

    boolean canElementEnableIndividualServices(Provider provider);

    boolean areServicesSupportedInNetwork(long networkId, Service... services);

    boolean isNetworkSystem(Network network);

    Map<Capability, String> getNetworkOfferingServiceCapabilities(NetworkOffering offering, Service service);

    Long getPhysicalNetworkId(Network network);

    boolean getAllowSubdomainAccessGlobal();

    boolean isProviderForNetwork(Provider provider, long networkId);

    void canProviderSupportServices(Map<Provider, Set<Service>> providersMap);

    List<PhysicalNetworkSetupInfo> getPhysicalNetworkInfo(long dcId, HypervisorType hypervisorType);

    boolean canAddDefaultSecurityGroup();

    List<Service> listNetworkOfferingServices(long networkOfferingId);

    boolean areServicesEnabledInZone(long zoneId, NetworkOffering offering, List<Service> services);

    Map<PublicIpAddress, Set<Service>> getIpToServices(List<? extends PublicIpAddress> publicIps, boolean rulesRevoked, boolean includingFirewall);

    Map<Provider, ArrayList<PublicIpAddress>> getProviderToIpList(Network network, Map<PublicIpAddress, Set<Service>> ipToServices);

    boolean checkIpForService(IpAddress ip, Service service, Long networkId);

    void checkCapabilityForProvider(Set<Provider> providers, Service service, Capability cap, String capValue);

    Provider getDefaultUniqueProviderForService(String serviceName);

    void checkNetworkPermissions(Account owner, Network network);

    String getDefaultManagementTrafficLabel(long zoneId, HypervisorType hypervisorType);

    String getDefaultStorageTrafficLabel(long zoneId, HypervisorType hypervisorType);

    NetworkElement getElementImplementingProvider(String providerName);

    String getAccountNetworkDomain(long accountId, long zoneId);

    List<Provider> getNtwkOffDistinctProviders(long ntwkOffId);

    boolean isPrivateGateway(long ntwkId);

    Map<Service, Map<Capability, String>> getNetworkCapabilities(long networkId);

    Network getSystemNetworkByZoneAndTrafficType(long zoneId, TrafficType trafficType);

    Long getDedicatedNetworkDomain(long networkId);

    Map<Service, Set<Provider>> getNetworkOfferingServiceProvidersMap(long networkOfferingId);

    List<? extends Provider> listSupportedNetworkServiceProviders(String serviceName);

    List<? extends Network> listNetworksByVpc(long vpcId);

    boolean canUseForDeploy(Network network);

    Network getExclusiveGuestNetwork(long zoneId);

    long findPhysicalNetworkId(long zoneId, String tag, TrafficType trafficType);

    Integer getNetworkRate(long networkId, Long vmId);

    boolean isVmPartOfNetwork(long vmId, long ntwkId);

    PhysicalNetwork getDefaultPhysicalNetworkByZoneAndTrafficType(long zoneId, TrafficType trafficType);

    Network getNetwork(long networkId);

    IpAddress getIp(long sourceIpAddressId);

    boolean isNetworkAvailableInDomain(long networkId, long domainId);

    NicProfile getNicProfile(VirtualMachine vm, long networkId, String broadcastUri);

    SortedSet<Long> getAvailableIps(Network network, String requestedIp);

    String getDomainNetworkDomain(long domainId, long zoneId);

    PublicIpAddress getSourceNatIpAddressForGuestNetwork(Account owner, Network guestNetwork);

    boolean isIP6AddressAvailableInNetwork(long networkId);

    boolean isIP6AddressAvailableInVlan(long vlanId);

    void checkIp6Parameters(String startIPv6, String endIPv6, String ip6Gateway, String ip6Cidr) throws InvalidParameterValueException;

    void checkRequestedIpAddresses(long networkId, IpAddresses ips) throws InvalidParameterValueException;

    String getStartIpv6Address(long id);

    boolean isProviderEnabledInZone(long zoneId, String provider);

    Nic getPlaceholderNicForRouter(Network network, Long podId);

    IpAddress getPublicIpAddress(String ipAddress, long zoneId);

    List<String> getUsedIpsInNetwork(Network network);

    Map<Detail, String> getNtwkOffDetails(long offId);

    Networks.IsolationType[] listNetworkIsolationMethods();

    Nic getNicInNetworkIncludingRemoved(long vmId, long networkId);

    boolean getExecuteInSeqNtwkElmtCmd();

    boolean isNetworkReadyForGc(long networkId);

    boolean getNetworkEgressDefaultPolicy(Long networkId);

    List<String[]> generateVmData(String userData, String serviceOffering, String zoneName,
                                  String vmName, long vmId, String publicKey, String password, Boolean isWindows, Network network);
}
