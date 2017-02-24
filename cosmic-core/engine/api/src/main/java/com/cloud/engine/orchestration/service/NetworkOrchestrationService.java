package com.cloud.engine.orchestration.service;

import com.cloud.acl.ControlledEntity.ACLType;
import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.framework.config.ConfigKey;
import com.cloud.framework.config.ConfigKey.Scope;
import com.cloud.network.Network;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkProfile;
import com.cloud.network.PhysicalNetwork;
import com.cloud.network.element.DhcpServiceProvider;
import com.cloud.network.element.LoadBalancingServiceProvider;
import com.cloud.network.element.StaticNatServiceProvider;
import com.cloud.network.element.UserDataServiceProvider;
import com.cloud.network.guru.NetworkGuru;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.offering.NetworkOffering;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.utils.Pair;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Type;
import com.cloud.vm.VirtualMachineProfile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * NetworkManager manages the network for the different end users.
 */
public interface NetworkOrchestrationService {
    static final String NetworkLockTimeoutCK = "network.lock.timeout";
    static final String GuestDomainSuffixCK = "guest.domain.suffix";
    static final String BlacklistedRoutesCK = "blacklisted.routes";
    static final String NetworkThrottlingRateCK = "network.throttling.rate";
    static final String MinVRVersionCK = "minreq.sysvmtemplate.version";

    static final ConfigKey<String> MinVRVersion = new ConfigKey<>(String.class, MinVRVersionCK, "Advanced", "4.6.0",
            "What version should the Virtual Routers report", true, ConfigKey.Scope.Zone, null);

    static final ConfigKey<Integer> NetworkLockTimeout = new ConfigKey<>(Integer.class, NetworkLockTimeoutCK,
            "Network", "600",
            "Lock wait timeout (seconds) while implementing network", true, Scope.Global, null);
    static final ConfigKey<String> GuestDomainSuffix = new ConfigKey<>(String.class, GuestDomainSuffixCK, "Network",
            "cloud.internal",
            "Default domain name for vms inside virtualized networks fronted by router", true, ConfigKey.Scope.Zone, null);
    static final ConfigKey<String> BlacklistedRoutes = new ConfigKey<>(String.class, BlacklistedRoutesCK,
            "Advanced", "",
            "Routes that are blacklisted, can not be used for Static Routes creation for the VPC Private Gateway", true,
            ConfigKey.Scope.Zone,
            null);
    static final ConfigKey<Integer> NetworkThrottlingRate = new ConfigKey<>("Network", Integer.class,
            NetworkThrottlingRateCK, "200",
            "Default data transfer rate in megabits per second allowed in network.", true, ConfigKey.Scope.Zone);

    List<? extends Network> setupNetwork(Account owner, NetworkOffering offering, DeploymentPlan plan, String name,
                                         String displayText, boolean isDefault)
            throws ConcurrentOperationException;

    List<? extends Network> setupNetwork(Account owner, NetworkOffering offering, Network predefined, DeploymentPlan plan,
                                         String name, String displayText,
                                         boolean errorIfAlreadySetup, Long domainId, ACLType aclType, Boolean subdomainAccess, Long vpcId,
                                         Boolean isDisplayNetworkEnabled, String dns1, String dns2, final String ipExclusionList)
            throws ConcurrentOperationException;

    void allocate(VirtualMachineProfile vm, LinkedHashMap<? extends Network, List<? extends NicProfile>> networks)
            throws InsufficientCapacityException,
            ConcurrentOperationException;

    void prepare(VirtualMachineProfile profile, DeployDestination dest, ReservationContext context)
            throws InsufficientCapacityException, ConcurrentOperationException,
            ResourceUnavailableException;

    void release(VirtualMachineProfile vmProfile, boolean forced)
            throws ConcurrentOperationException, ResourceUnavailableException;

    void cleanupNics(VirtualMachineProfile vm);

    void expungeNics(VirtualMachineProfile vm);

    List<NicProfile> getNicProfiles(VirtualMachine vm);

    Pair<? extends NetworkGuru, ? extends Network> implementNetwork(long networkId, DeployDestination dest,
                                                                    ReservationContext context)
            throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException;

    /**
     * prepares vm nic change for migration
     * <p>
     * This method will be called in migration transaction before the vm migration.
     *
     * @param vm
     * @param dest
     */
    void prepareNicForMigration(VirtualMachineProfile vm, DeployDestination dest);

    /**
     * commit vm nic change for migration
     * <p>
     * This method will be called in migration transaction after the successful vm migration.
     *
     * @param src
     * @param dst
     */
    void commitNicForMigration(VirtualMachineProfile src, VirtualMachineProfile dst);

    /**
     * rollback vm nic change for migration
     * <p>
     * This method will be called in migaration transaction after vm migration failure.
     *
     * @param src
     * @param dst
     */
    void rollbackNicForMigration(VirtualMachineProfile src, VirtualMachineProfile dst);

    boolean shutdownNetwork(long networkId, ReservationContext context, boolean cleanupElements);

    boolean destroyNetwork(long networkId, ReservationContext context, boolean forced);

    Network createGuestNetwork(long networkOfferingId, String name, String displayText, String gateway, String cidr,
                               String vlanId, String networkDomain, Account owner, Long domainId, PhysicalNetwork physicalNetwork,
                               long zoneId, ACLType aclType, Boolean subdomainAccess, Long vpcId, String ip6Gateway, String ip6Cidr,
                               Boolean displayNetworkEnabled, String isolatedPvlan, String dns1, String dns2, final String ipExclusionList)
            throws ConcurrentOperationException, InsufficientCapacityException, ResourceAllocationException;

    UserDataServiceProvider getPasswordResetProvider(Network network);

    UserDataServiceProvider getSSHKeyResetProvider(Network network);

    boolean startNetwork(long networkId, DeployDestination dest, ReservationContext context)
            throws ConcurrentOperationException, ResourceUnavailableException,
            InsufficientCapacityException;

    boolean reallocate(VirtualMachineProfile vm, DataCenterDeployment dest)
            throws InsufficientCapacityException, ConcurrentOperationException;

    /**
     * @param requested
     * @param network
     * @param isDefaultNic
     * @param deviceId
     * @param vm
     * @return
     * @throws InsufficientVirtualNetworkCapacityException
     * @throws InsufficientAddressCapacityException
     * @throws ConcurrentOperationException
     */
    Pair<NicProfile, Integer> allocateNic(NicProfile requested, Network network, Boolean isDefaultNic, int deviceId,
                                          VirtualMachineProfile vm)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException,
            ConcurrentOperationException;

    /**
     * @param vmProfile
     * @param dest
     * @param context
     * @param nicId
     * @param network
     * @return
     * @throws InsufficientVirtualNetworkCapacityException
     * @throws InsufficientAddressCapacityException
     * @throws ConcurrentOperationException
     * @throws InsufficientCapacityException
     * @throws ResourceUnavailableException
     */
    NicProfile prepareNic(VirtualMachineProfile vmProfile, DeployDestination dest, ReservationContext context, long nicId, Network network) throws ConcurrentOperationException,
            InsufficientCapacityException, ResourceUnavailableException;

    void removeNic(VirtualMachineProfile vm, Nic nic);

    void releaseNic(VirtualMachineProfile vmProfile, Nic nic)
            throws ConcurrentOperationException, ResourceUnavailableException;

    NicProfile createNicForVm(Network network, NicProfile requested, ReservationContext context, VirtualMachineProfile vmProfile, boolean prepare) throws
            ConcurrentOperationException, InsufficientCapacityException, ResourceUnavailableException;

    NetworkProfile convertNetworkToNetworkProfile(long networkId);

    boolean restartNetwork(Long networkId, Account callerAccount, User callerUser, boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException,
            InsufficientCapacityException;

    boolean shutdownNetworkElementsAndResources(ReservationContext context, boolean b, Network network);

    void implementNetworkElementsAndResources(DeployDestination dest, ReservationContext context, Network network, NetworkOffering findById) throws ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException;

    Map<String, String> finalizeServicesAndProvidersForNetwork(NetworkOffering offering, Long physicalNetworkId);

    List<Provider> getProvidersForServiceInNetwork(Network network, Service service);

    StaticNatServiceProvider getStaticNatProviderForNetwork(Network network);

    boolean isNetworkInlineMode(Network network);

    LoadBalancingServiceProvider getLoadBalancingProviderForNetwork(Network network, Scheme lbScheme);

    boolean isSecondaryIpSetForNic(long nicId);

    List<? extends Nic> listVmNics(long vmId, Long nicId, Long networkId);

    Nic savePlaceholderNic(Network network, String ip4Address, String ip6Address, Type vmType);

    DhcpServiceProvider getDhcpServiceProvider(Network network);

    void removeDhcpServiceInSubnet(Nic nic);

    boolean resourceCountNeedsUpdate(NetworkOffering ntwkOff, ACLType aclType);

    void prepareAllNicsForMigration(VirtualMachineProfile vm, DeployDestination dest);
}
