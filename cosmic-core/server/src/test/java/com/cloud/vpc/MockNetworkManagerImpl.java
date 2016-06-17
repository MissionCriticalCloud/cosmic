// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.vpc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.naming.ConfigurationException;

import com.cloud.deploy.DataCenterDeployment;
import com.cloud.deploy.DeployDestination;
import com.cloud.deploy.DeploymentPlan;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.GuestVlan;
import com.cloud.network.IpAddress;
import com.cloud.network.Network;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkProfile;
import com.cloud.network.NetworkService;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PhysicalNetwork;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.network.PhysicalNetworkTrafficType;
import com.cloud.network.dao.NetworkServiceMapDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.element.DhcpServiceProvider;
import com.cloud.network.element.LoadBalancingServiceProvider;
import com.cloud.network.element.NetworkElement;
import com.cloud.network.element.StaticNatServiceProvider;
import com.cloud.network.element.UserDataServiceProvider;
import com.cloud.network.guru.NetworkGuru;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.dao.NetworkOfferingServiceMapDao;
import com.cloud.user.Account;
import com.cloud.user.User;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ManagerBase;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicSecondaryIp;
import com.cloud.vm.NicVO;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Type;
import com.cloud.vm.VirtualMachineProfile;

import org.apache.cloudstack.acl.ControlledEntity.ACLType;
import org.apache.cloudstack.api.command.admin.network.DedicateGuestVlanRangeCmd;
import org.apache.cloudstack.api.command.admin.network.ListDedicatedGuestVlanRangesCmd;
import org.apache.cloudstack.api.command.admin.usage.ListTrafficTypeImplementorsCmd;
import org.apache.cloudstack.api.command.user.network.CreateNetworkCmd;
import org.apache.cloudstack.api.command.user.network.ListNetworksCmd;
import org.apache.cloudstack.api.command.user.network.RestartNetworkCmd;
import org.apache.cloudstack.api.command.user.vm.ListNicsCmd;
import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockNetworkManagerImpl extends ManagerBase implements NetworkOrchestrationService, NetworkService {
    @Inject
    NetworkServiceMapDao _ntwkSrvcDao;
    @Inject
    NetworkOfferingServiceMapDao _ntwkOfferingSrvcDao;

    @Inject
    List<NetworkElement> _networkElements;

    private static final HashMap<String, String> s_providerToNetworkElementMap = new HashMap<>();
    private static final Logger s_logger = LoggerFactory.getLogger(MockNetworkManagerImpl.class);

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#start()
     */
    @Override
    public boolean start() {
        for (final NetworkElement element : _networkElements) {
            final Provider implementedProvider = element.getProvider();
            if (implementedProvider != null) {
                if (s_providerToNetworkElementMap.containsKey(implementedProvider.getName())) {
                    s_logger.error("Cannot start MapNetworkManager: Provider <-> NetworkElement must be a one-to-one map, " +
                            "multiple NetworkElements found for Provider: " + implementedProvider.getName());
                    return false;
                }
                s_providerToNetworkElementMap.put(implementedProvider.getName(), element.getName());
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#configure(java.lang.String, java.util.Map)
     */
    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.utils.component.Manager#stop()
     */
    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
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
     * @see com.cloud.network.NetworkService#getIsolatedNetworksOwnedByAccountInZone(long, com.cloud.user.Account)
     */
    @Override
    public List<? extends Network> getIsolatedNetworksOwnedByAccountInZone(final long zoneId, final Account owner) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#allocateIP(com.cloud.user.Account, long, java.lang.Long)
     */
    @Override
    public IpAddress allocateIP(final Account ipOwner, final long zoneId, final Long networkId, final Boolean displayIp) throws ResourceAllocationException, InsufficientAddressCapacityException,
            ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IpAddress allocatePortableIP(final Account ipOwner, final int regionId, final Long zoneId, final Long networkId, final Long vpcId) throws ResourceAllocationException,
            InsufficientAddressCapacityException, ConcurrentOperationException {
        return null;
    }

    @Override
    public boolean releasePortableIpAddress(final long ipAddressId) {
        return false;// TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#releaseIpAddress(long)
     */
    @Override
    public boolean releaseIpAddress(final long ipAddressId) throws InsufficientAddressCapacityException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#createGuestNetwork(com.cloud.api.commands.CreateNetworkCmd)
     */
    @Override
    public Network createGuestNetwork(final CreateNetworkCmd cmd) throws InsufficientCapacityException, ConcurrentOperationException, ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#searchForNetworks(com.cloud.api.commands.ListNetworksCmd)
     */
    @Override
    public Pair<List<? extends Network>, Integer> searchForNetworks(final ListNetworksCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#deleteNetwork(long)
     */
    @Override
    public boolean deleteNetwork(final long networkId, final boolean forced) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#restartNetwork(com.cloud.api.commands.RestartNetworkCmd, boolean)
     */
    @Override
    public boolean restartNetwork(final RestartNetworkCmd cmd, final boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException,
            InsufficientCapacityException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#getActiveNicsInNetwork(long)
     */
    @Override
    public int getActiveNicsInNetwork(final long networkId) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#getNetwork(long)
     */
    @Override
    public Network getNetwork(final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#getIp(long)
     */
    @Override
    public IpAddress getIp(final long id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#updateGuestNetwork(long, java.lang.String, java.lang.String, com.cloud.user.Account, com.cloud.user.User, java.lang.String, java.lang.Long, java.lang.Boolean)
     */
    @Override
    public Network updateGuestNetwork(final long networkId, final String name, final String displayText, final Account callerAccount, final User callerUser, final String domainSuffix,
                                      final Long networkOfferingId, final Boolean changeCidr, final String guestVmCidr, final Boolean displayNetwork, final String newUUID) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#createPhysicalNetwork(java.lang.Long, java.lang.String, java.lang.String, java.util.List, java.lang.String, java.lang.Long, java.util.List, java.lang.String)
     */
    @Override
    public PhysicalNetwork createPhysicalNetwork(final Long zoneId, final String vnetRange, final String networkSpeed, final List<String> isolationMethods, final String broadcastDomainRange,
                                                 final Long domainId, final List<String> tags, final String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#searchPhysicalNetworks(java.lang.Long, java.lang.Long, java.lang.String, java.lang.Long, java.lang.Long, java.lang.String)
     */
    @Override
    public Pair<List<? extends PhysicalNetwork>, Integer> searchPhysicalNetworks(final Long id, final Long zoneId, final String keyword, final Long startIndex, final Long pageSize, final String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#updatePhysicalNetwork(java.lang.Long, java.lang.String, java.util.List, java.lang.String, java.lang.String)
     */
    @Override
    public PhysicalNetwork updatePhysicalNetwork(final Long id, final String networkSpeed, final List<String> tags, final String newVnetRangeString, final String state) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#deletePhysicalNetwork(java.lang.Long)
     */
    @Override
    public boolean deletePhysicalNetwork(final Long id) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public GuestVlan dedicateGuestVlanRange(final DedicateGuestVlanRangeCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Pair<List<? extends GuestVlan>, Integer> listDedicatedGuestVlanRanges(final ListDedicatedGuestVlanRangesCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean releaseDedicatedGuestVlanRange(final Long dedicatedGuestVlanRangeId) {
        // TODO Auto-generated method stub
        return true;

    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#listNetworkServices(java.lang.String)
     */
    @Override
    public List<? extends Service> listNetworkServices(final String providerName) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#addProviderToPhysicalNetwork(java.lang.Long, java.lang.String, java.lang.Long, java.util.List)
     */
    @Override
    public PhysicalNetworkServiceProvider addProviderToPhysicalNetwork(final Long physicalNetworkId, final String providerName, final Long destinationPhysicalNetworkId,
                                                                       final List<String> enabledServices) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#listNetworkServiceProviders(java.lang.Long, java.lang.String, java.lang.String, java.lang.Long, java.lang.Long)
     */
    @Override
    public Pair<List<? extends PhysicalNetworkServiceProvider>, Integer> listNetworkServiceProviders(final Long physicalNetworkId, final String name, final String state, final Long startIndex,
                                                                                                     final Long pageSize) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#updateNetworkServiceProvider(java.lang.Long, java.lang.String, java.util.List)
     */
    @Override
    public PhysicalNetworkServiceProvider updateNetworkServiceProvider(final Long id, final String state, final List<String> enabledServices) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#deleteNetworkServiceProvider(java.lang.Long)
     */
    @Override
    public boolean deleteNetworkServiceProvider(final Long id) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#getPhysicalNetwork(java.lang.Long)
     */
    @Override
    public PhysicalNetwork getPhysicalNetwork(final Long physicalNetworkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#getCreatedPhysicalNetwork(java.lang.Long)
     */
    @Override
    public PhysicalNetwork getCreatedPhysicalNetwork(final Long physicalNetworkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#getPhysicalNetworkServiceProvider(java.lang.Long)
     */
    @Override
    public PhysicalNetworkServiceProvider getPhysicalNetworkServiceProvider(final Long providerId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#getCreatedPhysicalNetworkServiceProvider(java.lang.Long)
     */
    @Override
    public PhysicalNetworkServiceProvider getCreatedPhysicalNetworkServiceProvider(final Long providerId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#findPhysicalNetworkId(long, java.lang.String, com.cloud.network.Networks.TrafficType)
     */
    @Override
    public long findPhysicalNetworkId(final long zoneId, final String tag, final TrafficType trafficType) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#addTrafficTypeToPhysicalNetwork(java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PhysicalNetworkTrafficType addTrafficTypeToPhysicalNetwork(final Long physicalNetworkId, final String trafficType, final String isolationMethod, final String xenLabel, final String kvmLabel,
                                                                      final String vlan, final String ovm3Label) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#getPhysicalNetworkTrafficType(java.lang.Long)
     */
    @Override
    public PhysicalNetworkTrafficType getPhysicalNetworkTrafficType(final Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#updatePhysicalNetworkTrafficType(java.lang.Long, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PhysicalNetworkTrafficType updatePhysicalNetworkTrafficType(final Long id, final String xenLabel, final String kvmLabel, final String ovm3Label) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#deletePhysicalNetworkTrafficType(java.lang.Long)
     */
    @Override
    public boolean deletePhysicalNetworkTrafficType(final Long id) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#listTrafficTypes(java.lang.Long)
     */
    @Override
    public Pair<List<? extends PhysicalNetworkTrafficType>, Integer> listTrafficTypes(final Long physicalNetworkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#getExclusiveGuestNetwork(long)
     */
    @Override
    public Network getExclusiveGuestNetwork(final long zoneId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#listTrafficTypeImplementor(org.apache.cloudstack.api.commands.ListTrafficTypeImplementorsCmd)
     */
    @Override
    public List<Pair<TrafficType, String>> listTrafficTypeImplementor(final ListTrafficTypeImplementorsCmd cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#getIsolatedNetworksWithSourceNATOwnedByAccountInZone(long, com.cloud.user.Account)
     */
    @Override
    public List<? extends Network> getIsolatedNetworksWithSourceNATOwnedByAccountInZone(final long zoneId, final Account owner) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#associateIPToNetwork(long, long)
     */
    @Override
    public IpAddress associateIPToNetwork(final long ipId, final long networkId) throws InsufficientAddressCapacityException, ResourceAllocationException,
            ResourceUnavailableException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#createPrivateNetwork(java.lang.String, java.lang.String, long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, java.lang.Long)
     */
    @Override
    public Network createPrivateNetwork(final String networkName, final String displayText, final long physicalNetworkId, final String vlan, final String startIp, final String endIP, final String gateway,
                                        final String netmask, final long networkOwnerId, final Long vpcId, final Boolean sourceNat, final Long networkOfferingId) throws ResourceAllocationException, ConcurrentOperationException,
            InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#setupNetwork(com.cloud.user.Account, com.cloud.offerings.NetworkOfferingVO, com.cloud.deploy.DeploymentPlan, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public List<NetworkVO> setupNetwork(final Account owner, final NetworkOffering offering, final DeploymentPlan plan, final String name, final String displayText, final boolean isDefault)
            throws ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#setupNetwork(com.cloud.user.Account, com.cloud.offerings.NetworkOfferingVO, com.cloud.network.Network, com.cloud.deploy.DeploymentPlan, java.lang.String, java.lang.String, boolean, java.lang.Long, org.apache.cloudstack.acl.ControlledEntity.ACLType, java.lang.Boolean, java.lang.Long)
     */
    @Override
    public List<NetworkVO> setupNetwork(final Account owner, final NetworkOffering offering, final Network predefined, final DeploymentPlan plan, final String name, final String displayText,
                                        final boolean errorIfAlreadySetup, final Long domainId, final ACLType aclType, final Boolean subdomainAccess, final Long vpcId, final Boolean isNetworkDisplayEnabled)
            throws ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#allocate(com.cloud.vm.VirtualMachineProfile, java.util.List)
     */
    @Override
    public void allocate(final VirtualMachineProfile vm, final LinkedHashMap<? extends Network, List<? extends NicProfile>> networks)
            throws InsufficientCapacityException, ConcurrentOperationException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#prepare(com.cloud.vm.VirtualMachineProfile, com.cloud.deploy.DeployDestination, com.cloud.vm.ReservationContext)
     */
    @Override
    public void prepare(final VirtualMachineProfile profile, final DeployDestination dest, final ReservationContext context) throws InsufficientCapacityException,
            ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#release(com.cloud.vm.VirtualMachineProfile, boolean)
     */
    @Override
    public void release(final VirtualMachineProfile vmProfile, final boolean forced) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#cleanupNics(com.cloud.vm.VirtualMachineProfile)
     */
    @Override
    public void cleanupNics(final VirtualMachineProfile vm) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#expungeNics(com.cloud.vm.VirtualMachineProfile)
     */
    @Override
    public void expungeNics(final VirtualMachineProfile vm) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#getNicProfiles(com.cloud.vm.VirtualMachine)
     */
    @Override
    public List<NicProfile> getNicProfiles(final VirtualMachine vm) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#implementNetwork(long, com.cloud.deploy.DeployDestination, com.cloud.vm.ReservationContext)
     */
    @Override
    public Pair<NetworkGuru, NetworkVO> implementNetwork(final long networkId, final DeployDestination dest, final ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#shutdownNetwork(long, com.cloud.vm.ReservationContext, boolean)
     */
    @Override
    public boolean shutdownNetwork(final long networkId, final ReservationContext context, final boolean cleanupElements) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#destroyNetwork(long, com.cloud.vm.ReservationContext)
     */
    @Override
    public boolean destroyNetwork(final long networkId, final ReservationContext context, final boolean forced) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#createGuestNetwork(long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, com.cloud.user.Account, java.lang.Long, com.cloud.network.PhysicalNetwork, long, org.apache.cloudstack.acl.ControlledEntity.ACLType, java.lang.Boolean, java.lang.Long)
     */
    @Override
    public Network createGuestNetwork(final long networkOfferingId, final String name, final String displayText, final String gateway, final String cidr, final String vlanId, final String networkDomain,
                                      final Account owner, final Long domainId, final PhysicalNetwork physicalNetwork, final long zoneId, final ACLType aclType, final Boolean subdomainAccess, final Long vpcId, final String gatewayv6,
                                      final String cidrv6, final Boolean displayNetworkEnabled, final String isolatedPvlan) throws ConcurrentOperationException, InsufficientCapacityException,
            ResourceAllocationException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#getPasswordResetProvider(com.cloud.network.Network)
     */
    @Override
    public UserDataServiceProvider getPasswordResetProvider(final Network network) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserDataServiceProvider getSSHKeyResetProvider(final Network network) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#startNetwork(long, com.cloud.deploy.DeployDestination, com.cloud.vm.ReservationContext)
     */
    @Override
    public boolean startNetwork(final long networkId, final DeployDestination dest, final ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException,
            InsufficientCapacityException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#reallocate(com.cloud.vm.VirtualMachineProfile, com.cloud.deploy.DataCenterDeployment)
     */
    @Override
    public boolean reallocate(final VirtualMachineProfile vm, final DataCenterDeployment dest) throws InsufficientCapacityException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#allocateNic(com.cloud.vm.NicProfile, com.cloud.network.Network, java.lang.Boolean, int, com.cloud.vm.VirtualMachineProfile)
     */
    @Override
    public Pair<NicProfile, Integer> allocateNic(final NicProfile requested, final Network network, final Boolean isDefaultNic, final int deviceId, final VirtualMachineProfile vm)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException, ConcurrentOperationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NicProfile prepareNic(final VirtualMachineProfile vmProfile, final DeployDestination dest, final ReservationContext context, final long nicId, final Network network)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException, ConcurrentOperationException, InsufficientCapacityException,
            ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#removeNic(com.cloud.vm.VirtualMachineProfile, com.cloud.vm.Nic)
     */
    @Override
    public void removeNic(final VirtualMachineProfile vm, final Nic nic) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#setupDns(com.cloud.network.Network, com.cloud.network.Network.Provider)
     */
    @Override
    public boolean setupDns(final Network network, final Provider provider) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#releaseNic(com.cloud.vm.VirtualMachineProfile, com.cloud.vm.Nic)
     */
    @Override
    public void releaseNic(final VirtualMachineProfile vmProfile, final Nic nic) throws ConcurrentOperationException, ResourceUnavailableException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#createNicForVm(com.cloud.network.Network, com.cloud.vm.NicProfile, com.cloud.vm.ReservationContext, com.cloud.vm.VirtualMachineProfileImpl, boolean, boolean)
     */
    @Override
    public NicProfile createNicForVm(final Network network, final NicProfile requested, final ReservationContext context, final VirtualMachineProfile vmProfile, final boolean prepare)
            throws InsufficientVirtualNetworkCapacityException, InsufficientAddressCapacityException, ConcurrentOperationException, InsufficientCapacityException,
            ResourceUnavailableException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#convertNetworkToNetworkProfile(long)
     */
    @Override
    public NetworkProfile convertNetworkToNetworkProfile(final long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#restartNetwork(java.lang.Long, com.cloud.user.Account, com.cloud.user.User, boolean)
     */
    @Override
    public boolean restartNetwork(final Long networkId, final Account callerAccount, final User callerUser, final boolean cleanup) throws ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#shutdownNetworkElementsAndResources(com.cloud.vm.ReservationContext, boolean, com.cloud.network.NetworkVO)
     */
    @Override
    public boolean shutdownNetworkElementsAndResources(final ReservationContext context, final boolean b, final Network network) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#implementNetworkElementsAndResources(com.cloud.deploy.DeployDestination, com.cloud.vm.ReservationContext, com.cloud.network.NetworkVO, com.cloud.offerings.NetworkOfferingVO)
     */
    @Override
    public void implementNetworkElementsAndResources(final DeployDestination dest, final ReservationContext context, final Network network, final NetworkOffering findById)
            throws ConcurrentOperationException, InsufficientAddressCapacityException, ResourceUnavailableException, InsufficientCapacityException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkManager#finalizeServicesAndProvidersForNetwork(com.cloud.offering.NetworkOffering, java.lang.Long)
     */
    @Override
    public Map<String, String> finalizeServicesAndProvidersForNetwork(final NetworkOffering offering, final Long physicalNetworkId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isNetworkInlineMode(final Network network) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<Provider> getProvidersForServiceInNetwork(final Network network, final Service service) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StaticNatServiceProvider getStaticNatProviderForNetwork(final Network network) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LoadBalancingServiceProvider getLoadBalancingProviderForNetwork(final Network network, final Scheme lbScheme) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.cloud.network.NetworkService#getNetwork(java.lang.String)
     */
    @Override
    public Network getNetwork(final String networkUuid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isSecondaryIpSetForNic(final long nicId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public NicSecondaryIp allocateSecondaryGuestIP(final long nicId, final String ipaddress) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean releaseSecondaryIpFromNic(final long ipAddressId) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<? extends Nic> listVmNics(final long vmId, final Long nicId, final Long networkId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<? extends Nic> listNics(final ListNicsCmd listNicsCmd) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Network.Capability, String> getNetworkOfferingServiceCapabilities(final NetworkOffering offering, final Service service) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NicVO savePlaceholderNic(final Network network, final String ip4Address, final String ip6Address, final Type vmType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DhcpServiceProvider getDhcpServiceProvider(final Network network) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeDhcpServiceInSubnet(final Nic nic) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean resourceCountNeedsUpdate(final NetworkOffering ntwkOff, final ACLType aclType) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void prepareAllNicsForMigration(final VirtualMachineProfile vm, final DeployDestination dest) {
        return;
    }

    @Override
    public void prepareNicForMigration(final VirtualMachineProfile vm, final DeployDestination dest) {
        // TODO Auto-generated method stub

    }

    @Override
    public void commitNicForMigration(final VirtualMachineProfile src, final VirtualMachineProfile dst) {
        // TODO Auto-generated method stub

    }

    @Override
    public void rollbackNicForMigration(final VirtualMachineProfile src, final VirtualMachineProfile dst) {
        // TODO Auto-generated method stub

    }

    @Override
    public IpAddress updateIP(final Long id, final String customId, final Boolean displayIp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean configureNicSecondaryIp(final NicSecondaryIp secIp, final boolean isZoneSgEnabled) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
