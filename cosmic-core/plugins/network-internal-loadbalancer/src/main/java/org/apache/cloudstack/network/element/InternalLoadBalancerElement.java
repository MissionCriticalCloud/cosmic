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

package org.apache.cloudstack.network.element;

import com.cloud.agent.api.to.LoadBalancerTO;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.dao.EntityManager;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.IllegalVirtualMachineException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.network.VirtualRouterProvider.Type;
import com.cloud.network.dao.NetworkServiceMapDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderDao;
import com.cloud.network.dao.VirtualRouterProviderDao;
import com.cloud.network.element.IpDeployer;
import com.cloud.network.element.LoadBalancingServiceProvider;
import com.cloud.network.element.VirtualRouterElement;
import com.cloud.network.element.VirtualRouterProviderVO;
import com.cloud.network.lb.LoadBalancingRule;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.router.VirtualRouter.Role;
import com.cloud.network.rules.LoadBalancerContainer;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.offering.NetworkOffering;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.User;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.Ip;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.DomainRouterDao;
import org.apache.cloudstack.api.command.admin.internallb.ConfigureInternalLoadBalancerElementCmd;
import org.apache.cloudstack.api.command.admin.internallb.CreateInternalLoadBalancerElementCmd;
import org.apache.cloudstack.api.command.admin.internallb.ListInternalLoadBalancerElementsCmd;
import org.apache.cloudstack.lb.dao.ApplicationLoadBalancerRuleDao;
import org.apache.cloudstack.network.lb.InternalLoadBalancerVMManager;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalLoadBalancerElement extends AdapterBase implements LoadBalancingServiceProvider, InternalLoadBalancerElementService, IpDeployer {
    protected static final Map<Service, Map<Capability, String>> capabilities = setCapabilities();
    private static final Logger s_logger = LoggerFactory.getLogger(InternalLoadBalancerElement.class);
    private static InternalLoadBalancerElement internalLbElement = null;

    @Inject
    NetworkModel _ntwkModel;
    @Inject
    NetworkServiceMapDao _ntwkSrvcDao;
    @Inject
    DomainRouterDao _routerDao;
    @Inject
    VirtualRouterProviderDao _vrProviderDao;
    @Inject
    PhysicalNetworkServiceProviderDao _pNtwkSvcProviderDao;
    @Inject
    InternalLoadBalancerVMManager _internalLbMgr;
    @Inject
    ConfigurationManager _configMgr;
    @Inject
    AccountManager _accountMgr;
    @Inject
    ApplicationLoadBalancerRuleDao _appLbDao;
    @Inject
    EntityManager _entityMgr;

    protected InternalLoadBalancerElement() {
    }

    public static InternalLoadBalancerElement getInstance() {
        if (internalLbElement == null) {
            internalLbElement = new InternalLoadBalancerElement();
        }
        return internalLbElement;
    }

    private static Map<Service, Map<Capability, String>> setCapabilities() {
        final Map<Service, Map<Capability, String>> capabilities = new HashMap<>();

        // Set capabilities for LB service
        final Map<Capability, String> lbCapabilities = new HashMap<>();
        lbCapabilities.put(Capability.SupportedLBAlgorithms, "roundrobin,leastconn,source");
        lbCapabilities.put(Capability.SupportedLBIsolation, "dedicated");
        lbCapabilities.put(Capability.SupportedProtocols, "tcp, udp");
        lbCapabilities.put(Capability.SupportedStickinessMethods, VirtualRouterElement.getHAProxyStickinessCapability());
        lbCapabilities.put(Capability.LbSchemes, LoadBalancerContainer.Scheme.Internal.toString());

        capabilities.put(Service.Lb, lbCapabilities);
        return capabilities;
    }

    @Override
    public IpDeployer getIpDeployer(final Network network) {
        return this;
    }

    private boolean canHandle(final Network config, final Scheme lbScheme) {
        //works in Advance zone only
        final DataCenter dc = _entityMgr.findById(DataCenter.class, config.getDataCenterId());
        if (dc.getNetworkType() != NetworkType.Advanced) {
            s_logger.trace("Not hanling zone of network type " + dc.getNetworkType());
            return false;
        }
        if (config.getGuestType() != Network.GuestType.Isolated || config.getTrafficType() != TrafficType.Guest) {
            s_logger.trace("Not handling network with Type  " + config.getGuestType() + " and traffic type " + config.getTrafficType());
            return false;
        }

        final Map<Capability, String> lbCaps = getCapabilities().get(Service.Lb);
        if (!lbCaps.isEmpty()) {
            final String schemeCaps = lbCaps.get(Capability.LbSchemes);
            if (schemeCaps != null && lbScheme != null) {
                if (!schemeCaps.contains(lbScheme.toString())) {
                    s_logger.debug("Scheme " + lbScheme.toString() + " is not supported by the provider " + getName());
                    return false;
                }
            }
        }

        if (!_ntwkModel.isProviderSupportServiceInNetwork(config.getId(), Service.Lb, getProvider())) {
            s_logger.trace("Element " + getProvider().getName() + " doesn't support service " + Service.Lb + " in the network " + config);
            return false;
        }
        return true;
    }

    @Override
    public boolean applyLBRules(final Network network, final List<LoadBalancingRule> rules) throws ResourceUnavailableException {
        //1) Get Internal LB VMs to destroy
        final Set<Ip> vmsToDestroy = getVmsToDestroy(rules);

        //2) Get rules to apply
        final Map<Ip, List<LoadBalancingRule>> rulesToApply = getLbRulesToApply(rules);
        s_logger.debug("Applying " + rulesToApply.size() + " on element " + getName());

        for (final Ip sourceIp : rulesToApply.keySet()) {
            if (vmsToDestroy.contains(sourceIp)) {
                //2.1 Destroy internal lb vm
                final List<? extends VirtualRouter> vms = _internalLbMgr.findInternalLbVms(network.getId(), sourceIp);
                if (vms.size() > 0) {
                    //only one internal lb per IP exists
                    try {
                        s_logger.debug("Destroying internal lb vm for ip " + sourceIp.addr() + " as all the rules for this vm are in Revoke state");
                        return _internalLbMgr.destroyInternalLbVm(vms.get(0).getId(), _accountMgr.getAccount(Account.ACCOUNT_ID_SYSTEM),
                                _accountMgr.getUserIncludingRemoved(User.UID_SYSTEM).getId());
                    } catch (final ConcurrentOperationException e) {
                        s_logger.warn("Failed to apply lb rule(s) for ip " + sourceIp.addr() + " on the element " + getName() + " due to:", e);
                        return false;
                    }
                }
            } else {
                //2.2 Start Internal LB vm per IP address
                final List<? extends VirtualRouter> internalLbVms;
                try {
                    final DeployDestination dest = new DeployDestination(_entityMgr.findById(DataCenter.class, network.getDataCenterId()), null, null, null);
                    internalLbVms = _internalLbMgr.deployInternalLbVm(network, sourceIp, dest, _accountMgr.getAccount(network.getAccountId()), null);
                } catch (final InsufficientCapacityException e) {
                    s_logger.warn("Failed to apply lb rule(s) for ip " + sourceIp.addr() + "on the element " + getName() + " due to:", e);
                    return false;
                } catch (final ConcurrentOperationException e) {
                    s_logger.warn("Failed to apply lb rule(s) for ip " + sourceIp.addr() + "on the element " + getName() + " due to:", e);
                    return false;
                }

                if (internalLbVms == null || internalLbVms.isEmpty()) {
                    throw new ResourceUnavailableException("Can't find/deploy internal lb vm to handle LB rules", DataCenter.class, network.getDataCenterId());
                }

                //2.3 Apply Internal LB rules on the VM
                if (!_internalLbMgr.applyLoadBalancingRules(network, rulesToApply.get(sourceIp), internalLbVms)) {
                    throw new CloudRuntimeException("Failed to apply load balancing rules for ip " + sourceIp.addr() + " in network " + network.getId() + " on element " +
                            getName());
                }
            }
        }

        return true;
    }

    protected Set<Ip> getVmsToDestroy(final List<LoadBalancingRule> rules) {
        //1) Group rules by the source ip address as NetworkManager always passes the entire network lb config to the element
        final Map<Ip, List<LoadBalancingRule>> groupedRules = groupBySourceIp(rules);

        final Set<Ip> vmsToDestroy = new HashSet<>();

        for (final Ip sourceIp : groupedRules.keySet()) {
            //2) Check if there are non revoked rules for the source ip address
            final List<LoadBalancingRule> rulesToCheck = groupedRules.get(sourceIp);
            if (_appLbDao.countBySourceIpAndNotRevoked(sourceIp, rulesToCheck.get(0).getNetworkId()) == 0) {
                s_logger.debug("Have to destroy internal lb vm for source ip " + sourceIp + " as it has 0 rules in non-Revoke state");
                vmsToDestroy.add(sourceIp);
            }
        }
        return vmsToDestroy;
    }

    @Override
    public Map<Service, Map<Capability, String>> getCapabilities() {
        return capabilities;
    }

    protected Map<Ip, List<LoadBalancingRule>> getLbRulesToApply(final List<LoadBalancingRule> rules) {
        //Group rules by the source ip address as NetworkManager always passes the entire network lb config to the element
        final Map<Ip, List<LoadBalancingRule>> rulesToApply = groupBySourceIp(rules);

        return rulesToApply;
    }

    protected Map<Ip, List<LoadBalancingRule>> groupBySourceIp(final List<LoadBalancingRule> rules) {
        final Map<Ip, List<LoadBalancingRule>> groupedRules = new HashMap<>();
        for (final LoadBalancingRule rule : rules) {
            if (rule.getDestinations() != null && !rule.getDestinations().isEmpty()) {
                final Ip sourceIp = rule.getSourceIp();
                if (!groupedRules.containsKey(sourceIp)) {
                    groupedRules.put(sourceIp, null);
                }

                List<LoadBalancingRule> rulesToApply = groupedRules.get(sourceIp);
                if (rulesToApply == null) {
                    rulesToApply = new ArrayList<>();
                }
                rulesToApply.add(rule);
                groupedRules.put(sourceIp, rulesToApply);
            } else {
                s_logger.debug("Internal lb rule " + rule + " doesn't have any vms assigned, skipping");
            }
        }
        return groupedRules;
    }

    @Override
    public Provider getProvider() {
        return Provider.InternalLbVm;
    }

    @Override
    public boolean validateLBRule(final Network network, final LoadBalancingRule rule) {
        final List<LoadBalancingRule> rules = new ArrayList<>();
        rules.add(rule);
        if (canHandle(network, rule.getScheme())) {
            final List<DomainRouterVO> routers = _routerDao.listByNetworkAndRole(network.getId(), Role.INTERNAL_LB_VM);
            if (routers == null || routers.isEmpty()) {
                return true;
            }
            return VirtualRouterElement.validateHAProxyLBRule(rule);
        }
        return true;
    }

    @Override
    public List<LoadBalancerTO> updateHealthChecks(final Network network, final List<LoadBalancingRule> lbrules) {
        return null;
    }

    @Override
    public boolean implement(final Network network, final NetworkOffering offering, final DeployDestination dest, final ReservationContext context) throws
            ConcurrentOperationException,
            ResourceUnavailableException, InsufficientCapacityException {

        if (!canHandle(network, null)) {
            s_logger.trace("No need to implement " + getName());
            return true;
        }

        return implementInternalLbVms(network, dest);
    }

    @Override
    public List<Class<?>> getCommands() {
        final List<Class<?>> cmdList = new ArrayList<>();
        cmdList.add(CreateInternalLoadBalancerElementCmd.class);
        cmdList.add(ConfigureInternalLoadBalancerElementCmd.class);
        cmdList.add(ListInternalLoadBalancerElementsCmd.class);
        return cmdList;
    }

    @Override
    public VirtualRouterProvider configureInternalLoadBalancerElement(final long id, final boolean enable) {
        VirtualRouterProviderVO element = _vrProviderDao.findById(id);
        if (element == null || element.getType() != Type.InternalLbVm) {
            throw new InvalidParameterValueException("Can't find " + getName() + " element with network service provider id " + id + " to be used as a provider for " +
                    getName());
        }

        element.setEnabled(enable);
        element = _vrProviderDao.persist(element);

        return element;
    }

    @Override
    public boolean prepare(final Network network, final NicProfile nic, final VirtualMachineProfile vm, final DeployDestination dest, final ReservationContext context)
            throws ConcurrentOperationException, ResourceUnavailableException, InsufficientCapacityException, IllegalVirtualMachineException {

        if (!canHandle(network, null)) {
            s_logger.trace("No need to prepare " + getName());
            return true;
        }

        if (vm.getType() == VirtualMachine.Type.User) {
            return implementInternalLbVms(network, dest);
        }
        return true;
    }

    @Override
    public VirtualRouterProvider addInternalLoadBalancerElement(final long ntwkSvcProviderId) {
        VirtualRouterProviderVO element = _vrProviderDao.findByNspIdAndType(ntwkSvcProviderId, Type.InternalLbVm);
        if (element != null) {
            s_logger.debug("There is already an " + getName() + " with service provider id " + ntwkSvcProviderId);
            return null;
        }

        final PhysicalNetworkServiceProvider provider = _pNtwkSvcProviderDao.findById(ntwkSvcProviderId);
        if (provider == null || !provider.getProviderName().equalsIgnoreCase(getName())) {
            throw new InvalidParameterValueException("Invalid network service provider is specified");
        }

        element = new VirtualRouterProviderVO(ntwkSvcProviderId, Type.InternalLbVm);
        element = _vrProviderDao.persist(element);
        return element;
    }

    @Override
    public VirtualRouterProvider getInternalLoadBalancerElement(final long id) {
        final VirtualRouterProvider provider = _vrProviderDao.findById(id);
        if (provider == null || provider.getType() != Type.InternalLbVm) {
            throw new InvalidParameterValueException("Unable to find " + getName() + " by id");
        }
        return provider;
    }

    protected boolean implementInternalLbVms(final Network network, final DeployDestination dest) throws ResourceUnavailableException {
        //1) Get all the Ips from the network having LB rules assigned
        final List<String> ips = _appLbDao.listLbIpsBySourceIpNetworkIdAndScheme(network.getId(), Scheme.Internal);

        //2) Start internal lb vms for the ips having active rules
        for (final String ip : ips) {
            final Ip sourceIp = new Ip(ip);
            final long active = _appLbDao.countActiveBySourceIp(sourceIp, network.getId());
            if (active > 0) {
                s_logger.debug("Have to implement internal lb vm for source ip " + sourceIp + " as a part of network " + network + " implement as there are " + active +
                        " internal lb rules exist for this ip");
                final List<? extends VirtualRouter> internalLbVms;
                try {
                    internalLbVms = _internalLbMgr.deployInternalLbVm(network, sourceIp, dest, _accountMgr.getAccount(network.getAccountId()), null);
                } catch (final InsufficientCapacityException e) {
                    s_logger.warn("Failed to deploy element " + getName() + " for ip " + sourceIp + " due to:", e);
                    return false;
                } catch (final ConcurrentOperationException e) {
                    s_logger.warn("Failed to deploy element " + getName() + " for ip " + sourceIp + " due to:", e);
                    return false;
                }

                if (internalLbVms == null || internalLbVms.isEmpty()) {
                    throw new ResourceUnavailableException("Can't deploy " + getName() + " to handle LB rules", DataCenter.class, network.getDataCenterId());
                }
            }
        }

        return true;
    }

    @Override
    public List<? extends VirtualRouterProvider> searchForInternalLoadBalancerElements(final Long id, final Long ntwkSvsProviderId, final Boolean enabled) {

        final QueryBuilder<VirtualRouterProviderVO> sc = QueryBuilder.create(VirtualRouterProviderVO.class);
        if (id != null) {
            sc.and(sc.entity().getId(), Op.EQ, id);
        }
        if (ntwkSvsProviderId != null) {
            sc.and(sc.entity().getNspId(), Op.EQ, ntwkSvsProviderId);
        }
        if (enabled != null) {
            sc.and(sc.entity().isEnabled(), Op.EQ, enabled);
        }

        //return only Internal LB elements
        sc.and(sc.entity().getType(), Op.EQ, VirtualRouterProvider.Type.InternalLbVm);

        return sc.list();
    }

    @Override
    public boolean applyIps(final Network network, final List<? extends PublicIpAddress> ipAddress, final Set<Service> services) throws ResourceUnavailableException {
        //do nothing here; this element just has to extend the ip deployer
        //as the LB service implements IPDeployerRequester
        return true;
    }

    @Override
    public boolean release(final Network network, final NicProfile nic, final VirtualMachineProfile vm, final ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException {
        return true;
    }

    @Override
    public boolean shutdown(final Network network, final ReservationContext context, final boolean cleanup) throws ConcurrentOperationException, ResourceUnavailableException {
        final List<? extends VirtualRouter> internalLbVms = _routerDao.listByNetworkAndRole(network.getId(), Role.INTERNAL_LB_VM);
        if (internalLbVms == null || internalLbVms.isEmpty()) {
            return true;
        }
        boolean result = true;
        for (final VirtualRouter internalLbVm : internalLbVms) {
            result = result && _internalLbMgr.destroyInternalLbVm(internalLbVm.getId(), context.getAccount(), context.getCaller().getId());
            if (cleanup) {
                if (!result) {
                    s_logger.warn("Failed to stop internal lb element " + internalLbVm + ", but would try to process clean up anyway.");
                }
                result = (_internalLbMgr.destroyInternalLbVm(internalLbVm.getId(), context.getAccount(), context.getCaller().getId()));
                if (!result) {
                    s_logger.warn("Failed to clean up internal lb element " + internalLbVm);
                }
            }
        }
        return result;
    }

    @Override
    public boolean destroy(final Network network, final ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        final List<? extends VirtualRouter> internalLbVms = _routerDao.listByNetworkAndRole(network.getId(), Role.INTERNAL_LB_VM);
        if (internalLbVms == null || internalLbVms.isEmpty()) {
            return true;
        }
        boolean result = true;
        for (final VirtualRouter internalLbVm : internalLbVms) {
            result = result && (_internalLbMgr.destroyInternalLbVm(internalLbVm.getId(), context.getAccount(), context.getCaller().getId()));
        }
        return result;
    }

    @Override
    public boolean isReady(final PhysicalNetworkServiceProvider provider) {
        final VirtualRouterProviderVO element = _vrProviderDao.findByNspIdAndType(provider.getId(), Type.InternalLbVm);
        if (element == null) {
            return false;
        }
        return element.isEnabled();
    }

    @Override
    public boolean shutdownProviderInstances(final PhysicalNetworkServiceProvider provider, final ReservationContext context) throws ConcurrentOperationException,
            ResourceUnavailableException {
        final VirtualRouterProviderVO element = _vrProviderDao.findByNspIdAndType(provider.getId(), Type.InternalLbVm);
        if (element == null) {
            return true;
        }
        final long elementId = element.getId();
        final List<DomainRouterVO> internalLbVms = _routerDao.listByElementId(elementId);
        boolean result = true;
        for (final DomainRouterVO internalLbVm : internalLbVms) {
            result = result && (_internalLbMgr.destroyInternalLbVm(internalLbVm.getId(), context.getAccount(), context.getCaller().getId()));
        }
        _vrProviderDao.remove(elementId);

        return result;
    }

    @Override
    public boolean canEnableIndividualServices() {
        return true;
    }

    @Override
    public boolean verifyServicesCombination(final Set<Service> services) {
        return true;
    }
}
