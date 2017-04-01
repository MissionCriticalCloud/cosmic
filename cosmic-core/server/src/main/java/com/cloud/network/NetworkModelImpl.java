package com.cloud.network;

import com.cloud.acl.ControlledEntity.ACLType;
import com.cloud.api.ApiDBUtils;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.dao.EntityManager;
import com.cloud.dc.DataCenter;
import com.cloud.dc.PodVlanMapVO;
import com.cloud.dc.Vlan;
import com.cloud.dc.Vlan.VlanType;
import com.cloud.dc.VlanVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.PodVlanMapDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.UnsupportedServiceException;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.lb.dao.ApplicationLoadBalancerRuleDao;
import com.cloud.network.IpAddress.State;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.GuestType;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.addr.PublicIp;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkDomainDao;
import com.cloud.network.dao.NetworkDomainVO;
import com.cloud.network.dao.NetworkServiceMapDao;
import com.cloud.network.dao.NetworkServiceMapVO;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderVO;
import com.cloud.network.dao.PhysicalNetworkTrafficTypeDao;
import com.cloud.network.dao.PhysicalNetworkTrafficTypeVO;
import com.cloud.network.dao.PhysicalNetworkVO;
import com.cloud.network.dao.UserIpv6AddressDao;
import com.cloud.network.element.IpDeployingRequester;
import com.cloud.network.element.NetworkElement;
import com.cloud.network.element.UserDataServiceProvider;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.network.rules.dao.PortForwardingRulesDao;
import com.cloud.network.vpc.dao.PrivateIpDao;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Detail;
import com.cloud.offerings.NetworkOfferingServiceMapVO;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.offerings.dao.NetworkOfferingDetailsDao;
import com.cloud.offerings.dao.NetworkOfferingServiceMapDao;
import com.cloud.projects.dao.ProjectAccountDao;
import com.cloud.server.ConfigurationServer;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.DomainManager;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.StringUtils;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.JoinBuilder.JoinType;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Type;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.NicSecondaryIpDao;
import com.cloud.vm.dao.VMInstanceDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.math.BigInteger;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkModelImpl extends ManagerBase implements NetworkModel {
    static final Logger s_logger = LoggerFactory.getLogger(NetworkModelImpl.class);
    static Long s_privateOfferingId = null;
    static HashMap<Service, List<Provider>> s_serviceToImplementedProvidersMap = new HashMap<>();
    static HashMap<String, String> s_providerToNetworkElementMap = new HashMap<>();
    private final HashMap<String, NetworkOfferingVO> _systemNetworks = new HashMap<>(5);
    protected boolean _executeInSequenceNtwkElmtCmd;
    @Inject
    EntityManager _entityMgr;
    @Inject
    DataCenterDao _dcDao = null;
    @Inject
    VlanDao _vlanDao = null;
    @Inject
    IPAddressDao _ipAddressDao = null;
    @Inject
    AccountDao _accountDao = null;
    @Inject
    DomainDao _domainDao = null;
    @Inject
    AccountManager _accountMgr;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    ConfigurationManager _configMgr;
    @Inject
    NetworkOfferingDao _networkOfferingDao = null;
    @Inject
    NetworkDao _networksDao = null;
    @Inject
    NicDao _nicDao = null;
    @Inject
    PodVlanMapDao _podVlanMapDao;
    @Inject
    ConfigurationServer _configServer;
    List<NetworkElement> networkElements;
    @Inject
    NetworkDomainDao _networkDomainDao;
    @Inject
    VMInstanceDao _vmDao;
    @Inject
    FirewallRulesDao _firewallDao;
    @Inject
    DomainManager _domainMgr;
    @Inject
    NetworkOfferingServiceMapDao _ntwkOfferingSrvcDao;
    @Inject
    PhysicalNetworkDao _physicalNetworkDao;
    @Inject
    PhysicalNetworkServiceProviderDao _pNSPDao;
    @Inject
    PortForwardingRulesDao _portForwardingRulesDao;
    @Inject
    PhysicalNetworkTrafficTypeDao _pNTrafficTypeDao;
    @Inject
    NetworkServiceMapDao _ntwkSrvcDao;
    @Inject
    PrivateIpDao _privateIpDao;
    @Inject
    UserIpv6AddressDao _ipv6Dao;
    @Inject
    NicSecondaryIpDao _nicSecondaryIpDao;
    @Inject
    ApplicationLoadBalancerRuleDao _appLbRuleDao;
    @Inject
    NetworkOfferingDetailsDao _ntwkOffDetailsDao;
    SearchBuilder<IPAddressVO> IpAddressSearch;
    SearchBuilder<NicVO> NicForTrafficTypeSearch;
    HashMap<Long, Long> _lastNetworkIdsToFree = new HashMap<>();
    @Inject
    private ProjectAccountDao _projectAccountDao;
    private boolean _allowSubdomainNetworkAccess;
    private Map<String, String> _configs;

    /**
     *
     */
    public NetworkModelImpl() {
        super();
    }

    public List<NetworkElement> getNetworkElements() {
        return networkElements;
    }

    public void setNetworkElements(final List<NetworkElement> networkElements) {
        this.networkElements = networkElements;
    }

    Set<Purpose> getPublicIpPurposeInRules(final PublicIpAddress ip, final boolean includeRevoked, final boolean includingFirewall) {
        final Set<Purpose> result = new HashSet<>();
        final List<FirewallRuleVO> rules;
        if (includeRevoked) {
            rules = _firewallDao.listByIp(ip.getId());
        } else {
            rules = _firewallDao.listByIpAndNotRevoked(ip.getId());
        }

        if (rules == null || rules.isEmpty()) {
            return null;
        }

        for (final FirewallRuleVO rule : rules) {
            if (rule.getPurpose() != Purpose.Firewall || includingFirewall) {
                result.add(rule.getPurpose());
            }
        }

        return result;
    }

    public boolean canIpUsedForNonConserveService(final PublicIp ip, final Service service) {
        // If it's non-conserve mode, then the new ip should not be used by any other services
        final List<PublicIpAddress> ipList = new ArrayList<>();
        ipList.add(ip);
        final Map<PublicIpAddress, Set<Service>> ipToServices = getIpToServices(ipList, false, false);
        final Set<Service> services = ipToServices.get(ip);
        // Not used currently, safe
        if (services == null || services.isEmpty()) {
            return true;
        }
        // Since it's non-conserve mode, only one service should used for IP
        if (services.size() != 1) {
            throw new InvalidParameterException("There are multiple services used ip " + ip.getAddress() + ".");
        }
        if (service != null && !((Service) services.toArray()[0] == service || service.equals(Service.Firewall))) {
            throw new InvalidParameterException("The IP " + ip.getAddress() + " is already used as " + ((Service) services.toArray()[0]).getName() + " rather than " +
                    service.getName());
        }
        return true;
    }

    @Override
    public NetworkElement getElementImplementingProvider(final String providerName) {
        final String elementName = s_providerToNetworkElementMap.get(providerName);
        final NetworkElement element = AdapterBase.getAdapterByName(networkElements, elementName);
        return element;
    }

    Map<Service, Set<Provider>> getServiceProvidersMap(final long networkId) {
        final Map<Service, Set<Provider>> map = new HashMap<>();
        final List<NetworkServiceMapVO> nsms = _ntwkSrvcDao.getServicesInNetwork(networkId);
        for (final NetworkServiceMapVO nsm : nsms) {
            Set<Provider> providers = map.get(Service.getService(nsm.getService()));
            if (providers == null) {
                providers = new HashSet<>();
            }
            providers.add(Provider.getProvider(nsm.getProvider()));
            map.put(Service.getService(nsm.getService()), providers);
        }
        return map;
    }

    public boolean canIpUsedForService(final PublicIp publicIp, final Service service, Long networkId) {
        final List<PublicIpAddress> ipList = new ArrayList<>();
        ipList.add(publicIp);
        final Map<PublicIpAddress, Set<Service>> ipToServices = getIpToServices(ipList, false, true);
        final Set<Service> services = ipToServices.get(publicIp);
        if (services == null || services.isEmpty()) {
            return true;
        }

        if (networkId == null) {
            networkId = publicIp.getAssociatedWithNetworkId();
        }

        // We only support one provider for one service now
        final Map<Service, Set<Provider>> serviceToProviders = getServiceProvidersMap(networkId);
        // Since IP already has service to bind with, the oldProvider can't be null
        final Set<Provider> newProviders = serviceToProviders.get(service);
        if (newProviders == null || newProviders.isEmpty()) {
            throw new InvalidParameterException("There is no new provider for IP " + publicIp.getAddress() + " of service " + service.getName() + "!");
        }
        final Provider newProvider = (Provider) newProviders.toArray()[0];
        final Set<Provider> oldProviders = serviceToProviders.get(services.toArray()[0]);
        final Provider oldProvider = (Provider) oldProviders.toArray()[0];
        final Network network = _networksDao.findById(networkId);
        final NetworkElement oldElement = getElementImplementingProvider(oldProvider.getName());
        final NetworkElement newElement = getElementImplementingProvider(newProvider.getName());
        if (oldElement instanceof IpDeployingRequester && newElement instanceof IpDeployingRequester) {
            ((IpDeployingRequester) oldElement).getIpDeployer(network);
            ((IpDeployingRequester) newElement).getIpDeployer(network);
        } else {
            throw new InvalidParameterException("Ip cannot be applied for new provider!");
        }
        return true;
    }

    @Override
    public List<Service> getElementServices(final Provider provider) {
        final NetworkElement element = getElementImplementingProvider(provider.getName());
        if (element == null) {
            throw new InvalidParameterValueException("Unable to find the Network Element implementing the Service Provider '" + provider.getName() + "'");
        }
        return new ArrayList<>(element.getCapabilities().keySet());
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _configs = _configDao.getConfiguration("Network", params);
        _allowSubdomainNetworkAccess = Boolean.valueOf(_configs.get(Config.SubDomainNetworkAccess.key()));
        _executeInSequenceNtwkElmtCmd = Boolean.valueOf(_configs.get(Config.ExecuteInSequenceNetworkElementCommands.key()));

        NetworkOfferingVO publicNetworkOffering = new NetworkOfferingVO(NetworkOffering.SystemPublicNetwork, TrafficType.Public, true);
        publicNetworkOffering = _networkOfferingDao.persistDefaultNetworkOffering(publicNetworkOffering);
        _systemNetworks.put(NetworkOffering.SystemPublicNetwork, publicNetworkOffering);
        NetworkOfferingVO managementNetworkOffering = new NetworkOfferingVO(NetworkOffering.SystemManagementNetwork, TrafficType.Management, false);
        managementNetworkOffering = _networkOfferingDao.persistDefaultNetworkOffering(managementNetworkOffering);
        _systemNetworks.put(NetworkOffering.SystemManagementNetwork, managementNetworkOffering);
        NetworkOfferingVO controlNetworkOffering = new NetworkOfferingVO(NetworkOffering.SystemControlNetwork, TrafficType.Control, false);
        controlNetworkOffering = _networkOfferingDao.persistDefaultNetworkOffering(controlNetworkOffering);
        _systemNetworks.put(NetworkOffering.SystemControlNetwork, controlNetworkOffering);
        NetworkOfferingVO storageNetworkOffering = new NetworkOfferingVO(NetworkOffering.SystemStorageNetwork, TrafficType.Storage, true);
        storageNetworkOffering = _networkOfferingDao.persistDefaultNetworkOffering(storageNetworkOffering);
        _systemNetworks.put(NetworkOffering.SystemStorageNetwork, storageNetworkOffering);
        NetworkOfferingVO privateGatewayNetworkOffering = new NetworkOfferingVO(NetworkOffering.DefaultPrivateGatewayNetworkOffering, GuestType.Private, false);
        privateGatewayNetworkOffering = _networkOfferingDao.persistDefaultNetworkOffering(privateGatewayNetworkOffering);
        _systemNetworks.put(NetworkOffering.DefaultPrivateGatewayNetworkOffering, privateGatewayNetworkOffering);
        s_privateOfferingId = privateGatewayNetworkOffering.getId();

        IpAddressSearch = _ipAddressDao.createSearchBuilder();
        IpAddressSearch.and("accountId", IpAddressSearch.entity().getAllocatedToAccountId(), Op.EQ);
        IpAddressSearch.and("dataCenterId", IpAddressSearch.entity().getDataCenterId(), Op.EQ);
        IpAddressSearch.and("vpcId", IpAddressSearch.entity().getVpcId(), Op.EQ);
        IpAddressSearch.and("associatedWithNetworkId", IpAddressSearch.entity().getAssociatedWithNetworkId(), Op.EQ);
        final SearchBuilder<VlanVO> virtualNetworkVlanSB = _vlanDao.createSearchBuilder();
        virtualNetworkVlanSB.and("vlanType", virtualNetworkVlanSB.entity().getVlanType(), Op.EQ);
        IpAddressSearch.join("virtualNetworkVlanSB", virtualNetworkVlanSB, IpAddressSearch.entity().getVlanId(), virtualNetworkVlanSB.entity().getId(),
                JoinBuilder.JoinType.INNER);
        IpAddressSearch.done();

        NicForTrafficTypeSearch = _nicDao.createSearchBuilder();
        final SearchBuilder<NetworkVO> networkSearch = _networksDao.createSearchBuilder();
        NicForTrafficTypeSearch.join("network", networkSearch, networkSearch.entity().getId(), NicForTrafficTypeSearch.entity().getNetworkId(), JoinType.INNER);
        NicForTrafficTypeSearch.and("instance", NicForTrafficTypeSearch.entity().getInstanceId(), Op.EQ);
        networkSearch.and("traffictype", networkSearch.entity().getTrafficType(), Op.EQ);
        NicForTrafficTypeSearch.done();

        s_logger.info("Network Model is configured.");

        return true;
    }

    @Override
    public boolean start() {
        // populate s_serviceToImplementedProvidersMap & s_providerToNetworkElementMap with current _networkElements
        // Need to do this in start() since _networkElements are not completely configured until then.
        for (final NetworkElement element : networkElements) {
            final Map<Service, Map<Capability, String>> capabilities = element.getCapabilities();
            final Provider implementedProvider = element.getProvider();
            if (implementedProvider != null) {
                if (s_providerToNetworkElementMap.containsKey(implementedProvider.getName())) {
                    s_logger.error("Cannot start NetworkModel: Provider <-> NetworkElement must be a one-to-one map, " + "multiple NetworkElements found for Provider: " +
                            implementedProvider.getName());
                    continue;
                }
                s_logger.info("Add provider <-> element map entry. " + implementedProvider.getName() + "-" + element.getName() + "-" + element.getClass().getSimpleName());
                s_providerToNetworkElementMap.put(implementedProvider.getName(), element.getName());
            }
            if (capabilities != null && implementedProvider != null) {
                for (final Service service : capabilities.keySet()) {
                    if (s_serviceToImplementedProvidersMap.containsKey(service)) {
                        final List<Provider> providers = s_serviceToImplementedProvidersMap.get(service);
                        providers.add(implementedProvider);
                    } else {
                        final List<Provider> providers = new ArrayList<>();
                        providers.add(implementedProvider);
                        s_serviceToImplementedProvidersMap.put(service, providers);
                    }
                }
            }
        }
        s_logger.info("Started Network Model");
        return true;
    }

    @Override
    public boolean canElementEnableIndividualServices(final Provider provider) {
        final NetworkElement element = getElementImplementingProvider(provider.getName());
        if (element == null) {
            throw new InvalidParameterValueException("Unable to find the Network Element implementing the Service Provider '" + provider.getName() + "'");
        }
        return element.canEnableIndividualServices();
    }

    @Override
    public boolean stop() {
        return true;
    }

    @Override
    public Map<PublicIpAddress, Set<Service>> getIpToServices(final List<? extends PublicIpAddress> publicIps, final boolean postApplyRules, final boolean includingFirewall) {
        final Map<PublicIpAddress, Set<Service>> ipToServices = new HashMap<>();

        if (publicIps != null && !publicIps.isEmpty()) {
            final Set<Long> networkSNAT = new HashSet<>();
            for (final PublicIpAddress ip : publicIps) {
                Set<Service> services = ipToServices.get(ip);
                if (services == null) {
                    services = new HashSet<>();
                }
                if (ip.isSourceNat()) {
                    if (!networkSNAT.contains(ip.getAssociatedWithNetworkId())) {
                        services.add(Service.SourceNat);
                        networkSNAT.add(ip.getAssociatedWithNetworkId());
                    } else {
                        final CloudRuntimeException ex = new CloudRuntimeException("Multiple generic soure NAT IPs provided for network");
                        // see the IPAddressVO.java class.
                        final IPAddressVO ipAddr = ApiDBUtils.findIpAddressById(ip.getAssociatedWithNetworkId());
                        String ipAddrUuid = ip.getAssociatedWithNetworkId().toString();
                        if (ipAddr != null) {
                            ipAddrUuid = ipAddr.getUuid();
                        }
                        ex.addProxyObject(ipAddrUuid, "networkId");
                        throw ex;
                    }
                }
                ipToServices.put(ip, services);

                // if IP in allocating state then it will not have any rules attached so skip IPAssoc to network service
                // provider
                if (ip.getState() == State.Allocating) {
                    continue;
                }

                // check if any active rules are applied on the public IP
                Set<Purpose> purposes = getPublicIpPurposeInRules(ip, false, includingFirewall);
                // Firewall rules didn't cover static NAT
                if (ip.isOneToOneNat() && ip.getAssociatedWithVmId() != null) {
                    if (purposes == null) {
                        purposes = new HashSet<>();
                    }
                    purposes.add(Purpose.StaticNat);
                }
                if (purposes == null || purposes.isEmpty()) {
                    // since no active rules are there check if any rules are applied on the public IP but are in
                    // revoking state

                    purposes = getPublicIpPurposeInRules(ip, true, includingFirewall);
                    if (ip.isOneToOneNat()) {
                        if (purposes == null) {
                            purposes = new HashSet<>();
                        }
                        purposes.add(Purpose.StaticNat);
                    }
                    if (purposes == null || purposes.isEmpty()) {
                        // IP is not being used for any purpose so skip IPAssoc to network service provider
                        continue;
                    } else {
                        if (postApplyRules) {
                            // no active rules/revoked rules are associated with this public IP, so remove the
                            // association with the provider
                            if (ip.isSourceNat()) {
                                s_logger.debug("Not releasing ip " + ip.getAddress().addr() + " as it is in use for SourceNat");
                            } else {
                                ip.setState(State.Releasing);
                            }
                        } else {
                            if (ip.getState() == State.Releasing) {
                                // rules are not revoked yet, so don't let the network service provider revoke the IP
                                // association
                                // mark IP is allocated so that IP association will not be removed from the provider
                                ip.setState(State.Allocated);
                            }
                        }
                    }
                }
                if (purposes.contains(Purpose.StaticNat)) {
                    services.add(Service.StaticNat);
                }
                if (purposes.contains(Purpose.LoadBalancing)) {
                    services.add(Service.Lb);
                }
                if (purposes.contains(Purpose.PortForwarding)) {
                    services.add(Service.PortForwarding);
                }
                if (purposes.contains(Purpose.Vpn)) {
                    services.add(Service.Vpn);
                }
                if (purposes.contains(Purpose.Firewall)) {
                    services.add(Service.Firewall);
                }
                if (services.isEmpty()) {
                    continue;
                }
                ipToServices.put(ip, services);
            }
        }
        return ipToServices;
    }

    Map<Provider, Set<Service>> getProviderServicesMap(final long networkId) {
        final Map<Provider, Set<Service>> map = new HashMap<>();
        final List<NetworkServiceMapVO> nsms = _ntwkSrvcDao.getServicesInNetwork(networkId);
        for (final NetworkServiceMapVO nsm : nsms) {
            Set<Service> services = map.get(Provider.getProvider(nsm.getProvider()));
            if (services == null) {
                services = new HashSet<>();
            }
            services.add(Service.getService(nsm.getService()));
            map.put(Provider.getProvider(nsm.getProvider()), services);
        }
        return map;
    }

    @Override
    public Map<Provider, ArrayList<PublicIpAddress>> getProviderToIpList(final Network network, final Map<PublicIpAddress, Set<Service>> ipToServices) {
        final NetworkOffering offering = _networkOfferingDao.findById(network.getNetworkOfferingId());
        if (!offering.isConserveMode()) {
            for (final PublicIpAddress ip : ipToServices.keySet()) {
                final Set<Service> services = new HashSet<>();
                services.addAll(ipToServices.get(ip));
                if (services != null && services.contains(Service.Firewall)) {
                    services.remove(Service.Firewall);
                }
                if (services != null && services.size() > 1) {
                    throw new CloudRuntimeException("Ip " + ip.getAddress() + " is used by multiple services!");
                }
            }
        }
        final Map<Service, Set<PublicIpAddress>> serviceToIps = new HashMap<>();
        for (final PublicIpAddress ip : ipToServices.keySet()) {
            for (final Service service : ipToServices.get(ip)) {
                Set<PublicIpAddress> ips = serviceToIps.get(service);
                if (ips == null) {
                    ips = new HashSet<>();
                }
                ips.add(ip);
                serviceToIps.put(service, ips);
            }
        }
        // TODO Check different provider for same IP
        final Map<Provider, Set<Service>> providerToServices = getProviderServicesMap(network.getId());
        final Map<Provider, ArrayList<PublicIpAddress>> providerToIpList = new HashMap<>();
        for (final Provider provider : providerToServices.keySet()) {
            if (!(getElementImplementingProvider(provider.getName()) instanceof IpDeployingRequester)) {
                continue;
            }
            final Set<Service> services = providerToServices.get(provider);
            final ArrayList<PublicIpAddress> ipList = new ArrayList<>();
            final Set<PublicIpAddress> ipSet = new HashSet<>();
            for (final Service service : services) {
                final Set<PublicIpAddress> serviceIps = serviceToIps.get(service);
                if (serviceIps == null || serviceIps.isEmpty()) {
                    continue;
                }
                ipSet.addAll(serviceIps);
            }
            final Set<PublicIpAddress> sourceNatIps = serviceToIps.get(Service.SourceNat);
            if (sourceNatIps != null && !sourceNatIps.isEmpty()) {
                ipList.addAll(0, sourceNatIps);
                ipSet.removeAll(sourceNatIps);
            }
            ipList.addAll(ipSet);
            providerToIpList.put(provider, ipList);
        }
        return providerToIpList;
    }

    @Override
    public List<IPAddressVO> listPublicIpsAssignedToGuestNtwk(final long accountId, final long associatedNetworkId, final Boolean sourceNat) {
        final SearchCriteria<IPAddressVO> sc = IpAddressSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("associatedWithNetworkId", associatedNetworkId);
        if (sourceNat != null) {
            sc.addAnd("sourceNat", SearchCriteria.Op.EQ, sourceNat);
        }
        sc.setJoinParameters("virtualNetworkVlanSB", "vlanType", VlanType.VirtualNetwork);

        return _ipAddressDao.search(sc, null);
    }

    @Override
    public List<IPAddressVO> listPublicIpsAssignedToGuestNtwk(final long associatedNetworkId, final Boolean sourceNat) {
        final SearchCriteria<IPAddressVO> sc = IpAddressSearch.create();
        sc.setParameters("associatedWithNetworkId", associatedNetworkId);

        if (sourceNat != null) {
            sc.addAnd("sourceNat", SearchCriteria.Op.EQ, sourceNat);
        }
        sc.setJoinParameters("virtualNetworkVlanSB", "vlanType", VlanType.VirtualNetwork);

        return _ipAddressDao.search(sc, null);
    }

    @Override
    public List<IPAddressVO> listPublicIpsAssignedToAccount(final long accountId, final long dcId, final Boolean sourceNat) {
        final SearchCriteria<IPAddressVO> sc = IpAddressSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("dataCenterId", dcId);

        if (sourceNat != null) {
            sc.addAnd("sourceNat", SearchCriteria.Op.EQ, sourceNat);
        }
        sc.setJoinParameters("virtualNetworkVlanSB", "vlanType", VlanType.VirtualNetwork);

        return _ipAddressDao.search(sc, null);
    }

    @Override
    public List<? extends Nic> getNics(final long vmId) {
        return _nicDao.listByVmId(vmId);
    }

    @Override
    public String getNextAvailableMacAddressInNetwork(final long networkId) throws InsufficientAddressCapacityException {
        final String mac = _networksDao.getNextAvailableMacAddress(networkId);
        if (mac == null) {
            throw new InsufficientAddressCapacityException("Unable to create another mac address", Network.class, networkId);
        }
        return mac;
    }

    @Override
    @DB
    public Network getNetwork(final long id) {
        return _networksDao.findById(id);
    }

    @Override
    public boolean canUseForDeploy(final Network network) {
        if (network.getTrafficType() != TrafficType.Guest) {
            return false;
        }
        boolean hasFreeIps = true;
        if (network.getGuestType() == GuestType.Shared) {
            if (network.getGateway() != null) {
                hasFreeIps = _ipAddressDao.countFreeIPsInNetwork(network.getId()) > 0;
            }
            if (!hasFreeIps) {
                return false;
            }
            if (network.getIp6Gateway() != null) {
                hasFreeIps = isIP6AddressAvailableInNetwork(network.getId());
            }
        } else {
            if (network.getCidr() == null) {
                s_logger.debug("Network - " + network.getId() + " has NULL CIDR.");
                return false;
            }
            hasFreeIps = getAvailableIps(network, null).size() > 0;
        }

        return hasFreeIps;
    }

    @Override
    public boolean isIP6AddressAvailableInNetwork(final long networkId) {
        final Network network = _networksDao.findById(networkId);
        if (network == null) {
            return false;
        }
        if (network.getIp6Gateway() == null) {
            return false;
        }
        final List<VlanVO> vlans = _vlanDao.listVlansByNetworkId(networkId);
        for (final Vlan vlan : vlans) {
            if (isIP6AddressAvailableInVlan(vlan.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isIP6AddressAvailableInVlan(final long vlanId) {
        final VlanVO vlan = _vlanDao.findById(vlanId);
        if (vlan.getIp6Range() == null) {
            return false;
        }
        final long existedCount = _ipv6Dao.countExistedIpsInVlan(vlanId);
        final BigInteger existedInt = BigInteger.valueOf(existedCount);
        final BigInteger rangeInt = NetUtils.countIp6InRange(vlan.getIp6Range());
        return existedInt.compareTo(rangeInt) < 0;
    }

    @Override
    public Map<Service, Map<Capability, String>> getNetworkCapabilities(final long networkId) {

        final Map<Service, Map<Capability, String>> networkCapabilities = new HashMap<>();

        // list all services of this networkOffering
        final List<NetworkServiceMapVO> servicesMap = _ntwkSrvcDao.getServicesInNetwork(networkId);
        for (final NetworkServiceMapVO instance : servicesMap) {
            final Service service = Service.getService(instance.getService());
            final NetworkElement element = getElementImplementingProvider(instance.getProvider());
            if (element != null) {
                final Map<Service, Map<Capability, String>> elementCapabilities = element.getCapabilities();
                if (elementCapabilities != null) {
                    networkCapabilities.put(service, elementCapabilities.get(service));
                }
            }
        }

        return networkCapabilities;
    }

    @Override
    public Map<Capability, String> getNetworkServiceCapabilities(final long networkId, final Service service) {

        if (!areServicesSupportedInNetwork(networkId, service)) {
            // TBD: networkId to uuid. No VO object being passed. So we will need to call
            // addProxyObject with hardcoded tablename. Or we should probably look up the correct dao proxy object.
            throw new UnsupportedServiceException("Service " + service.getName() + " is not supported in the network id=" + networkId);
        }

        Map<Capability, String> serviceCapabilities = new HashMap<>();

        // get the Provider for this Service for this offering
        final String provider = _ntwkSrvcDao.getProviderForServiceInNetwork(networkId, service);

        final NetworkElement element = getElementImplementingProvider(provider);
        if (element != null) {
            final Map<Service, Map<Capability, String>> elementCapabilities = element.getCapabilities();

            if (elementCapabilities == null || !elementCapabilities.containsKey(service)) {
                throw new UnsupportedServiceException("Service " + service.getName() + " is not supported by the element=" + element.getName() +
                        " implementing Provider=" + provider);
            }
            serviceCapabilities = elementCapabilities.get(service);
        }

        return serviceCapabilities;
    }

    @Override
    public Map<Capability, String> getNetworkOfferingServiceCapabilities(final NetworkOffering offering, final Service service) {

        if (!areServicesSupportedByNetworkOffering(offering.getId(), service)) {
            // TBD: We should be sending networkOfferingId and not the offering object itself.
            throw new UnsupportedServiceException("Service " + service.getName() + " is not supported by the network offering " + offering);
        }

        Map<Capability, String> serviceCapabilities = new HashMap<>();

        // get the Provider for this Service for this offering
        final List<String> providers = _ntwkOfferingSrvcDao.listProvidersForServiceForNetworkOffering(offering.getId(), service);
        if (providers.isEmpty()) {
            // TBD: We should be sending networkOfferingId and not the offering object itself.
            throw new InvalidParameterValueException("Service " + service.getName() + " is not supported by the network offering " + offering);
        }

        // FIXME - in post 3.0 we are going to support multiple providers for the same service per network offering, so
        // we have to calculate capabilities for all of them
        final String provider = providers.get(0);

        // FIXME we return the capabilities of the first provider of the service - what if we have multiple providers
        // for same Service?
        final NetworkElement element = getElementImplementingProvider(provider);
        if (element != null) {
            final Map<Service, Map<Capability, String>> elementCapabilities = element.getCapabilities();

            if (elementCapabilities == null || !elementCapabilities.containsKey(service)) {
                // TBD: We should be sending providerId and not the offering object itself.
                throw new UnsupportedServiceException("Service " + service.getName() + " is not supported by the element=" + element.getName() +
                        " implementing Provider=" + provider);
            }
            serviceCapabilities = elementCapabilities.get(service);
        }

        return serviceCapabilities;
    }

    @Override
    public NetworkVO getSystemNetworkByZoneAndTrafficType(final long zoneId, final TrafficType trafficType) {
        // find system public network offering
        Long networkOfferingId = null;
        final List<NetworkOfferingVO> offerings = _networkOfferingDao.listSystemNetworkOfferings();
        for (final NetworkOfferingVO offering : offerings) {
            if (offering.getTrafficType() == trafficType) {
                networkOfferingId = offering.getId();
                break;
            }
        }

        if (networkOfferingId == null) {
            throw new InvalidParameterValueException("Unable to find system network offering with traffic type " + trafficType);
        }

        final List<NetworkVO> networks = _networksDao.listBy(Account.ACCOUNT_ID_SYSTEM, networkOfferingId, zoneId);
        if (networks == null || networks.isEmpty()) {
            // TBD: send uuid instead of zoneId. Hardcode tablename in call to addProxyObject().
            throw new InvalidParameterValueException("Unable to find network with traffic type " + trafficType + " in zone " + zoneId);
        }
        return networks.get(0);
    }

    @Override
    public NetworkVO getNetworkWithSGWithFreeIPs(final Long zoneId) {
        final List<NetworkVO> networks = _networksDao.listByZoneSecurityGroup(zoneId);
        if (networks == null || networks.isEmpty()) {
            return null;
        }
        NetworkVO ret_network = null;
        for (final NetworkVO nw : networks) {
            final List<VlanVO> vlans = _vlanDao.listVlansByNetworkId(nw.getId());
            for (final VlanVO vlan : vlans) {
                if (_ipAddressDao.countFreeIpsInVlan(vlan.getId()) > 0) {
                    ret_network = nw;
                    break;
                }
            }
            if (ret_network != null) {
                break;
            }
        }
        if (ret_network == null) {
            s_logger.debug("Can not find network with security group enabled with free IPs");
        }
        return ret_network;
    }

    @Override
    public NetworkVO getNetworkWithSecurityGroupEnabled(final Long zoneId) {
        final List<NetworkVO> networks = _networksDao.listByZoneSecurityGroup(zoneId);
        if (networks == null || networks.isEmpty()) {
            return null;
        }

        if (networks.size() > 1) {
            s_logger.debug("There are multiple network with security group enabled? select one of them...");
        }
        return networks.get(0);
    }

    @Override
    public PublicIpAddress getPublicIpAddress(final long ipAddressId) {
        final IPAddressVO addr = _ipAddressDao.findById(ipAddressId);
        if (addr == null) {
            return null;
        }

        return PublicIp.createFromAddrAndVlan(addr, _vlanDao.findById(addr.getVlanId()));
    }

    @Override
    public List<VlanVO> listPodVlans(final long podId) {
        final List<VlanVO> vlans = _vlanDao.listVlansForPodByType(podId, VlanType.DirectAttached);
        return vlans;
    }

    @Override
    public List<NetworkVO> listNetworksUsedByVm(final long vmId, final boolean isSystem) {
        final List<NetworkVO> networks = new ArrayList<>();

        final List<NicVO> nics = _nicDao.listByVmId(vmId);
        if (nics != null) {
            for (final Nic nic : nics) {
                final NetworkVO network = _networksDao.findByIdIncludingRemoved(nic.getNetworkId());

                if (isNetworkSystem(network) == isSystem) {
                    networks.add(network);
                }
            }
        }

        return networks;
    }

    @Override
    public Nic getNicInNetwork(final long vmId, final long networkId) {
        return _nicDao.findByNtwkIdAndInstanceId(networkId, vmId);
    }

    @Override
    public String getIpInNetwork(final long vmId, final long networkId) {
        final Nic guestNic = getNicInNetwork(vmId, networkId);
        assert guestNic != null && guestNic.getIPv4Address() != null : "Vm doesn't belong to network associated with " + "ipAddress or ip4 address is null";
        return guestNic.getIPv4Address();
    }

    @Override
    public String getIpInNetworkIncludingRemoved(final long vmId, final long networkId) {
        final Nic guestNic = getNicInNetworkIncludingRemoved(vmId, networkId);
        assert guestNic != null && guestNic.getIPv4Address() != null : "Vm doesn't belong to network associated with " + "ipAddress or ip4 address is null";
        return guestNic.getIPv4Address();
    }

    @Override
    public List<NicVO> getNicsForTraffic(final long vmId, final TrafficType type) {
        final SearchCriteria<NicVO> sc = NicForTrafficTypeSearch.create();
        sc.setParameters("instance", vmId);
        sc.setJoinParameters("network", "traffictype", type);

        return _nicDao.search(sc, null);
    }

    @Override
    public IpAddress getIp(final long ipAddressId) {
        return _ipAddressDao.findById(ipAddressId);
    }

    @Override
    public Network getDefaultNetworkForVm(final long vmId) {
        final Nic defaultNic = getDefaultNic(vmId);
        if (defaultNic == null) {
            return null;
        } else {
            return _networksDao.findById(defaultNic.getNetworkId());
        }
    }

    @Override
    public Nic getDefaultNic(final long vmId) {
        final List<NicVO> nics = _nicDao.listByVmId(vmId);
        Nic defaultNic = null;
        if (nics != null) {
            for (final Nic nic : nics) {
                if (nic.isDefaultNic()) {
                    defaultNic = nic;
                    break;
                }
            }
        } else {
            s_logger.debug("Unable to find default network for the vm; vm doesn't have any nics");
            return null;
        }

        if (defaultNic == null) {
            s_logger.debug("Unable to find default network for the vm; vm doesn't have default nic");
        }

        return defaultNic;
    }

    @Override
    public UserDataServiceProvider getUserDataUpdateProvider(final Network network) {
        final String userDataProvider = _ntwkSrvcDao.getProviderForServiceInNetwork(network.getId(), Service.UserData);

        if (userDataProvider == null) {
            s_logger.debug("Network " + network + " doesn't support service " + Service.UserData.getName());
            return null;
        }

        return (UserDataServiceProvider) getElementImplementingProvider(userDataProvider);
    }

    @Override
    public boolean isSharedNetworkWithoutServices(final long networkId) {

        final Network network = _networksDao.findById(networkId);

        if (network != null && network.getGuestType() != GuestType.Shared) {
            return false;
        }

        final List<Service> services = listNetworkOfferingServices(network.getNetworkOfferingId());

        if (services == null || services.isEmpty()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean areServicesSupportedByNetworkOffering(final long networkOfferingId, final Service... services) {
        return _ntwkOfferingSrvcDao.areServicesSupportedByNetworkOffering(networkOfferingId, services);
    }

    @Override
    public boolean areServicesSupportedInNetwork(final long networkId, final Service... services) {
        return _ntwkSrvcDao.areServicesSupportedInNetwork(networkId, services);
    }

    @Override
    public String getIpOfNetworkElementInVirtualNetwork(final long accountId, final long dataCenterId) {

        final List<NetworkVO> virtualNetworks = _networksDao.listByZoneAndGuestType(accountId, dataCenterId, Network.GuestType.Isolated, false);

        if (virtualNetworks.isEmpty()) {
            s_logger.trace("Unable to find default Virtual network account id=" + accountId);
            return null;
        }

        final NetworkVO virtualNetwork = virtualNetworks.get(0);

        final NicVO networkElementNic = _nicDao.findByNetworkIdAndType(virtualNetwork.getId(), Type.DomainRouter);

        if (networkElementNic != null) {
            return networkElementNic.getIPv4Address();
        } else {
            s_logger.warn("Unable to set find network element for the network id=" + virtualNetwork.getId());
            return null;
        }
    }

    @Override
    public List<NetworkVO> listNetworksForAccount(final long accountId, final long zoneId, final Network.GuestType type) {
        final List<NetworkVO> accountNetworks = new ArrayList<>();
        final List<NetworkVO> zoneNetworks = _networksDao.listByZone(zoneId);

        for (final NetworkVO network : zoneNetworks) {
            if (!isNetworkSystem(network)) {
                if (network.getGuestType() == Network.GuestType.Shared || !_networksDao.listBy(accountId, network.getId()).isEmpty()) {
                    if (type == null || type == network.getGuestType()) {
                        accountNetworks.add(network);
                    }
                }
            }
        }
        return accountNetworks;
    }

    @Override
    public List<NetworkVO> listAllNetworksInAllZonesByType(final Network.GuestType type) {
        final List<NetworkVO> networks = new ArrayList<>();
        for (final NetworkVO network : _networksDao.listAll()) {
            if (!isNetworkSystem(network)) {
                networks.add(network);
            }
        }
        return networks;
    }

    @Override
    public Long getDedicatedNetworkDomain(final long networkId) {
        final NetworkDomainVO networkMaps = _networkDomainDao.getDomainNetworkMapByNetworkId(networkId);
        if (networkMaps != null) {
            return networkMaps.getDomainId();
        } else {
            return null;
        }
    }

    @Override
    public Integer getNetworkRate(final long networkId, final Long vmId) {
        VMInstanceVO vm = null;
        if (vmId != null) {
            vm = _vmDao.findById(vmId);
        }
        final Network network = getNetwork(networkId);
        final NetworkOffering ntwkOff = _entityMgr.findById(NetworkOffering.class, network.getNetworkOfferingId());

        // For default userVm Default network and domR guest/public network, get rate information from the service
        // offering; for other situations get information
        // from the network offering
        boolean isUserVmsDefaultNetwork = false;
        boolean isDomRGuestOrPublicNetwork = false;
        boolean isSystemVmNetwork = false;
        if (vm != null) {
            final Nic nic = _nicDao.findByNtwkIdAndInstanceId(networkId, vmId);
            if (vm.getType() == Type.User && nic != null && nic.isDefaultNic()) {
                isUserVmsDefaultNetwork = true;
            } else if (vm.getType() == Type.DomainRouter && ntwkOff != null &&
                    (ntwkOff.getTrafficType() == TrafficType.Public || ntwkOff.getTrafficType() == TrafficType.Guest)) {
                isDomRGuestOrPublicNetwork = true;
            } else if (vm.getType() == Type.ConsoleProxy || vm.getType() == Type.SecondaryStorageVm) {
                isSystemVmNetwork = true;
            }
        }
        if (isUserVmsDefaultNetwork || isDomRGuestOrPublicNetwork) {
            return _configMgr.getServiceOfferingNetworkRate(vm.getServiceOfferingId(), network.getDataCenterId());
        } else if (isSystemVmNetwork) {
            return -1;
        } else {
            return _configMgr.getNetworkOfferingNetworkRate(ntwkOff.getId(), network.getDataCenterId());
        }
    }

    @Override
    public String getAccountNetworkDomain(final long accountId, final long zoneId) {
        final String networkDomain = _accountDao.findById(accountId).getNetworkDomain();

        if (networkDomain == null) {
            // get domain level network domain
            return getDomainNetworkDomain(_accountDao.findById(accountId).getDomainId(), zoneId);
        }

        return networkDomain;
    }

    @Override
    public String getStartIpAddress(final long networkId) {
        final List<VlanVO> vlans = _vlanDao.listVlansByNetworkId(networkId);
        if (vlans.isEmpty()) {
            return null;
        }

        String startIP = vlans.get(0).getIpRange().split("-")[0];

        for (final VlanVO vlan : vlans) {
            final String startIP1 = vlan.getIpRange().split("-")[0];
            final long startIPLong = NetUtils.ip2Long(startIP);
            final long startIPLong1 = NetUtils.ip2Long(startIP1);

            if (startIPLong1 < startIPLong) {
                startIP = startIP1;
            }
        }

        return startIP;
    }

    @Override
    public Long getPodIdForVlan(final long vlanDbId) {
        final PodVlanMapVO podVlanMaps = _podVlanMapDao.listPodVlanMapsByVlan(vlanDbId);
        if (podVlanMaps == null) {
            return null;
        } else {
            return podVlanMaps.getPodId();
        }
    }

    @Override
    public Map<Service, Set<Provider>> getNetworkOfferingServiceProvidersMap(final long networkOfferingId) {
        final Map<Service, Set<Provider>> serviceProviderMap = new HashMap<>();
        final List<NetworkOfferingServiceMapVO> map = _ntwkOfferingSrvcDao.listByNetworkOfferingId(networkOfferingId);

        for (final NetworkOfferingServiceMapVO instance : map) {
            final String service = instance.getService();
            Set<Provider> providers;
            providers = serviceProviderMap.get(Service.getService(service));
            if (providers == null) {
                providers = new HashSet<>();
            }
            providers.add(Provider.getProvider(instance.getProvider()));
            serviceProviderMap.put(Service.getService(service), providers);
        }

        return serviceProviderMap;
    }

    @Override
    public boolean isProviderSupportServiceInNetwork(final long networkId, final Service service, final Provider provider) {
        return _ntwkSrvcDao.canProviderSupportServiceInNetwork(networkId, service, provider);
    }

    @Override
    public List<? extends Provider> listSupportedNetworkServiceProviders(final String serviceName) {
        Network.Service service = null;
        if (serviceName != null) {
            service = Network.Service.getService(serviceName);
            if (service == null) {
                throw new InvalidParameterValueException("Invalid Network Service=" + serviceName);
            }
        }

        final Set<Provider> supportedProviders = new HashSet<>();

        if (service != null) {
            final List<Provider> providers = s_serviceToImplementedProvidersMap.get(service);
            if (providers != null && !providers.isEmpty()) {
                supportedProviders.addAll(providers);
            }
        } else {
            for (final List<Provider> pList : s_serviceToImplementedProvidersMap.values()) {
                supportedProviders.addAll(pList);
            }
        }

        return new ArrayList<>(supportedProviders);
    }

    @Override
    public Provider getDefaultUniqueProviderForService(final String serviceName) {
        final List<? extends Provider> providers = listSupportedNetworkServiceProviders(serviceName);
        if (providers.isEmpty()) {
            throw new CloudRuntimeException("No providers supporting service " + serviceName + " found in cloudStack");
        }
        if (providers.size() > 1) {
            throw new CloudRuntimeException("More than 1 provider supporting service " + serviceName + " found in cloudStack");
        }

        return providers.get(0);
    }

    @Override
    public long findPhysicalNetworkId(final long zoneId, final String tag, final TrafficType trafficType) {
        final List<PhysicalNetworkVO> pNtwks;
        if (trafficType != null) {
            pNtwks = _physicalNetworkDao.listByZoneAndTrafficType(zoneId, trafficType);
        } else {
            pNtwks = _physicalNetworkDao.listByZone(zoneId);
        }

        if (pNtwks.isEmpty()) {
            throw new InvalidParameterValueException("Unable to find physical network in zone id=" + zoneId);
        }

        if (pNtwks.size() > 1) {
            if (tag == null) {
                throw new InvalidParameterValueException("More than one physical networks exist in zone id=" + zoneId +
                        " and no tags are specified in order to make a choice");
            }

            Long pNtwkId = null;
            for (final PhysicalNetwork pNtwk : pNtwks) {
                if (pNtwk.getTags().contains(tag)) {
                    s_logger.debug("Found physical network id=" + pNtwk.getId() + " based on requested tags " + tag);
                    pNtwkId = pNtwk.getId();
                    break;
                }
            }
            if (pNtwkId == null) {
                throw new InvalidParameterValueException("Unable to find physical network which match the tags " + tag);
            }
            return pNtwkId;
        } else {
            return pNtwks.get(0).getId();
        }
    }

    @Override
    public List<Long> listNetworkOfferingsForUpgrade(final long networkId) {
        final List<Long> offeringsToReturn = new ArrayList<>();
        final NetworkOffering originalOffering = _entityMgr.findById(NetworkOffering.class, getNetwork(networkId).getNetworkOfferingId());

        final boolean securityGroupSupportedByOriginalOff = areServicesSupportedByNetworkOffering(originalOffering.getId(), Service.SecurityGroup);

        // security group supported property should be the same

        final List<Long> offerings = _networkOfferingDao.getOfferingIdsToUpgradeFrom(originalOffering);

        for (final Long offeringId : offerings) {
            if (areServicesSupportedByNetworkOffering(offeringId, Service.SecurityGroup) == securityGroupSupportedByOriginalOff) {
                offeringsToReturn.add(offeringId);
            }
        }

        return offeringsToReturn;
    }

    @Override
    public boolean isSecurityGroupSupportedInNetwork(final Network network) {
        if (network.getTrafficType() != TrafficType.Guest) {
            s_logger.trace("Security group can be enabled for Guest networks only; and network " + network + " has a diff traffic type");
            return false;
        }

        Long physicalNetworkId = network.getPhysicalNetworkId();

        // physical network id can be null in Guest Network in Basic zone, so locate the physical network
        if (physicalNetworkId == null) {
            physicalNetworkId = findPhysicalNetworkId(network.getDataCenterId(), null, null);
        }

        return isServiceEnabledInNetwork(physicalNetworkId, network.getId(), Service.SecurityGroup);
    }

    @Override
    public PhysicalNetwork getDefaultPhysicalNetworkByZoneAndTrafficType(final long zoneId, final TrafficType trafficType) {

        final List<PhysicalNetworkVO> networkList = _physicalNetworkDao.listByZoneAndTrafficType(zoneId, trafficType);
        final DataCenter dc = ApiDBUtils.findZoneById(zoneId);
        String dcUuid = String.valueOf(zoneId);
        if (dc != null) {
            dcUuid = dc.getUuid();
        }

        if (networkList.isEmpty()) {
            final InvalidParameterValueException ex =
                    new InvalidParameterValueException("Unable to find the default physical network with traffic=" + trafficType + " in the specified zone id");
            ex.addProxyObject(dcUuid, "zoneId");
            throw ex;
        }

        if (networkList.size() > 1) {
            final InvalidParameterValueException ex =
                    new InvalidParameterValueException("More than one physical networks exist in zone id=" + zoneId + " with traffic type=" + trafficType);
            ex.addProxyObject(dcUuid, "zoneId");
            throw ex;
        }

        return networkList.get(0);
    }

    @Override
    public String getDefaultManagementTrafficLabel(final long zoneId, final HypervisorType hypervisorType) {
        try {
            final PhysicalNetwork mgmtPhyNetwork = getDefaultPhysicalNetworkByZoneAndTrafficType(zoneId, TrafficType.Management);
            final PhysicalNetworkTrafficTypeVO mgmtTraffic = _pNTrafficTypeDao.findBy(mgmtPhyNetwork.getId(), TrafficType.Management);
            if (mgmtTraffic != null) {
                String label = null;
                switch (hypervisorType) {
                    case XenServer:
                        label = mgmtTraffic.getXenNetworkLabel();
                        break;
                    case KVM:
                        label = mgmtTraffic.getKvmNetworkLabel();
                        break;
                }
                return label;
            }
        } catch (final Exception ex) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Failed to retrive the default label for management traffic:" + "zone: " + zoneId + " hypervisor: " + hypervisorType + " due to:" +
                        ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public String getDefaultStorageTrafficLabel(final long zoneId, final HypervisorType hypervisorType) {
        try {
            final PhysicalNetwork storagePhyNetwork = getDefaultPhysicalNetworkByZoneAndTrafficType(zoneId, TrafficType.Storage);
            final PhysicalNetworkTrafficTypeVO storageTraffic = _pNTrafficTypeDao.findBy(storagePhyNetwork.getId(), TrafficType.Storage);
            if (storageTraffic != null) {
                String label = null;
                switch (hypervisorType) {
                    case XenServer:
                        label = storageTraffic.getXenNetworkLabel();
                        break;
                    case KVM:
                        label = storageTraffic.getKvmNetworkLabel();
                        break;
                }
                return label;
            }
        } catch (final Exception ex) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Failed to retrive the default label for storage traffic:" + "zone: " + zoneId + " hypervisor: " + hypervisorType + " due to:" +
                        ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public List<PhysicalNetworkSetupInfo> getPhysicalNetworkInfo(final long dcId, final HypervisorType hypervisorType) {
        final List<PhysicalNetworkSetupInfo> networkInfoList = new ArrayList<>();
        final List<PhysicalNetworkVO> physicalNtwkList = _physicalNetworkDao.listByZone(dcId);
        for (final PhysicalNetworkVO pNtwk : physicalNtwkList) {
            final String publicName = _pNTrafficTypeDao.getNetworkTag(pNtwk.getId(), TrafficType.Public, hypervisorType);
            final String privateName = _pNTrafficTypeDao.getNetworkTag(pNtwk.getId(), TrafficType.Management, hypervisorType);
            final String guestName = _pNTrafficTypeDao.getNetworkTag(pNtwk.getId(), TrafficType.Guest, hypervisorType);
            final String storageName = _pNTrafficTypeDao.getNetworkTag(pNtwk.getId(), TrafficType.Storage, hypervisorType);
            // String controlName = _pNTrafficTypeDao.getNetworkTag(pNtwk.getId(), TrafficType.Control, hypervisorType);
            final PhysicalNetworkSetupInfo info = new PhysicalNetworkSetupInfo();
            info.setPhysicalNetworkId(pNtwk.getId());
            info.setGuestNetworkName(guestName);
            info.setPrivateNetworkName(privateName);
            info.setPublicNetworkName(publicName);
            info.setStorageNetworkName(storageName);
            final PhysicalNetworkTrafficTypeVO mgmtTraffic = _pNTrafficTypeDao.findBy(pNtwk.getId(), TrafficType.Management);
            if (mgmtTraffic != null) {
                final String vlan = mgmtTraffic.getVlan();
                info.setMgmtVlan(vlan);
            }
            networkInfoList.add(info);
        }
        return networkInfoList;
    }

    @Override
    public boolean isProviderEnabledInPhysicalNetwork(final long physicalNetowrkId, final String providerName) {
        final PhysicalNetworkServiceProviderVO ntwkSvcProvider = _pNSPDao.findByServiceProvider(physicalNetowrkId, providerName);
        if (ntwkSvcProvider == null) {
            s_logger.warn("Unable to find provider " + providerName + " in physical network id=" + physicalNetowrkId);
            return false;
        }
        return isProviderEnabled(ntwkSvcProvider);
    }

    @Override
    public boolean isProviderEnabledInZone(final long zoneId, final String provider) {
        //the provider has to be enabled at least in one network in the zone
        for (final PhysicalNetwork pNtwk : _physicalNetworkDao.listByZone(zoneId)) {
            if (isProviderEnabledInPhysicalNetwork(pNtwk.getId(), provider)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getNetworkTag(final HypervisorType hType, final Network network) {
        // no network tag for control traffic type
        final TrafficType effectiveTrafficType = network.getTrafficType();

        if (effectiveTrafficType == TrafficType.Control) {
            return null;
        }

        Long physicalNetworkId;
        if (effectiveTrafficType != TrafficType.Guest) {
            physicalNetworkId = getNonGuestNetworkPhysicalNetworkId(network, effectiveTrafficType);
        } else {
            final NetworkOffering offering = _entityMgr.findById(NetworkOffering.class, network.getNetworkOfferingId());
            physicalNetworkId = network.getPhysicalNetworkId();
            if (physicalNetworkId == null) {
                physicalNetworkId = findPhysicalNetworkId(network.getDataCenterId(), offering.getTags(), offering.getTrafficType());
            }
        }

        if (physicalNetworkId == null) {
            assert false : "Can't get the physical network";
            s_logger.warn("Can't get the physical network");
            return null;
        }

        return _pNTrafficTypeDao.getNetworkTag(physicalNetworkId, effectiveTrafficType, hType);
    }

    @Override
    public NetworkVO getExclusiveGuestNetwork(final long zoneId) {
        final List<NetworkVO> networks = _networksDao.listBy(Account.ACCOUNT_ID_SYSTEM, zoneId, GuestType.Shared, TrafficType.Guest);
        if (networks == null || networks.isEmpty()) {
            throw new InvalidParameterValueException("Unable to find network with trafficType " + TrafficType.Guest + " and guestType " + GuestType.Shared + " in zone " +
                    zoneId);
        }

        if (networks.size() > 1) {
            throw new InvalidParameterValueException("Found more than 1 network with trafficType " + TrafficType.Guest + " and guestType " + GuestType.Shared +
                    " in zone " + zoneId);
        }

        return networks.get(0);
    }

    @Override
    public boolean isNetworkSystem(final Network network) {
        final NetworkOffering no = _networkOfferingDao.findByIdIncludingRemoved(network.getNetworkOfferingId());
        if (no.isSystemOnly()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Long getPhysicalNetworkId(final Network network) {
        if (network.getTrafficType() != TrafficType.Guest) {
            return getNonGuestNetworkPhysicalNetworkId(network);
        }

        Long physicalNetworkId = network.getPhysicalNetworkId();
        final NetworkOffering offering = _entityMgr.findById(NetworkOffering.class, network.getNetworkOfferingId());
        if (physicalNetworkId == null) {
            physicalNetworkId = findPhysicalNetworkId(network.getDataCenterId(), offering.getTags(), offering.getTrafficType());
        }
        return physicalNetworkId;
    }

    @Override
    public boolean getAllowSubdomainAccessGlobal() {
        return _allowSubdomainNetworkAccess;
    }

    @Override
    public boolean isProviderForNetwork(final Provider provider, final long networkId) {
        if (_ntwkSrvcDao.isProviderForNetwork(networkId, provider) != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isProviderForNetworkOffering(final Provider provider, final long networkOfferingId) {
        if (_ntwkOfferingSrvcDao.isProviderForNetworkOffering(networkOfferingId, provider)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void canProviderSupportServices(final Map<Provider, Set<Service>> providersMap) {
        for (final Provider provider : providersMap.keySet()) {
            // check if services can be turned off
            final NetworkElement element = getElementImplementingProvider(provider.getName());
            if (element == null) {
                throw new InvalidParameterValueException("Unable to find the Network Element implementing the Service Provider '" + provider.getName() + "'");
            }

            final Set<Service> enabledServices = new HashSet<>();
            enabledServices.addAll(providersMap.get(provider));

            if (enabledServices != null && !enabledServices.isEmpty()) {
                if (!element.canEnableIndividualServices()) {
                    final Set<Service> requiredServices = new HashSet<>();
                    requiredServices.addAll(element.getCapabilities().keySet());

                    if (requiredServices.contains(Network.Service.Gateway)) {
                        requiredServices.remove(Network.Service.Gateway);
                    }

                    if (requiredServices.contains(Network.Service.Firewall)) {
                        requiredServices.remove(Network.Service.Firewall);
                    }

                    if (enabledServices.contains(Network.Service.Firewall)) {
                        enabledServices.remove(Network.Service.Firewall);
                    }

                    // exclude gateway service
                    if (enabledServices.size() != requiredServices.size()) {
                        final StringBuilder servicesSet = new StringBuilder();

                        for (final Service requiredService : requiredServices) {
                            // skip gateway service as we don't allow setting it via API
                            if (requiredService == Service.Gateway) {
                                continue;
                            }
                            servicesSet.append(requiredService.getName() + ", ");
                        }
                        servicesSet.delete(servicesSet.toString().length() - 2, servicesSet.toString().length());

                        throw new InvalidParameterValueException("Cannot enable subset of Services, Please specify the complete list of Services: " +
                                servicesSet.toString() + "  for Service Provider " + provider.getName());
                    }
                }
                final List<String> serviceList = new ArrayList<>();
                for (final Service service : enabledServices) {
                    // check if the service is provided by this Provider
                    if (!element.getCapabilities().containsKey(service)) {
                        throw new UnsupportedServiceException(provider.getName() + " Provider cannot provide service " + service.getName());
                    }
                    serviceList.add(service.getName());
                }
                if (!element.verifyServicesCombination(enabledServices)) {
                    throw new UnsupportedServiceException("Provider " + provider.getName() + " doesn't support services combination: " + serviceList);
                }
            }
        }
    }

    @Override
    public boolean canAddDefaultSecurityGroup() {
        final String defaultAdding = _configDao.getValue(Config.SecurityGroupDefaultAdding.key());
        return defaultAdding != null && defaultAdding.equalsIgnoreCase("true");
    }

    @Override
    public List<Service> listNetworkOfferingServices(final long networkOfferingId) {
        final List<Service> services = new ArrayList<>();
        final List<String> servicesStr = _ntwkOfferingSrvcDao.listServicesForNetworkOffering(networkOfferingId);
        for (final String serviceStr : servicesStr) {
            services.add(Service.getService(serviceStr));
        }

        return services;
    }

    @Override
    public boolean areServicesEnabledInZone(final long zoneId, final NetworkOffering offering, final List<Service> services) {
        final long physicalNtwkId = findPhysicalNetworkId(zoneId, offering.getTags(), offering.getTrafficType());
        boolean result = true;
        final List<String> checkedProvider = new ArrayList<>();
        for (final Service service : services) {
            // get all the providers, and check if each provider is enabled
            final List<String> providerNames = _ntwkOfferingSrvcDao.listProvidersForServiceForNetworkOffering(offering.getId(), service);
            for (final String providerName : providerNames) {
                if (!checkedProvider.contains(providerName)) {
                    result = result && isProviderEnabledInPhysicalNetwork(physicalNtwkId, providerName);
                }
            }
        }

        return result;
    }

    @Override
    public boolean checkIpForService(final IpAddress userIp, final Service service, Long networkId) {
        if (networkId == null) {
            networkId = userIp.getAssociatedWithNetworkId();
        }

        final NetworkVO network = _networksDao.findById(networkId);
        final NetworkOfferingVO offering = _networkOfferingDao.findById(network.getNetworkOfferingId());
        if (offering.getGuestType() != GuestType.Isolated) {
            return true;
        }
        final IPAddressVO ipVO = _ipAddressDao.findById(userIp.getId());
        final PublicIp publicIp = PublicIp.createFromAddrAndVlan(ipVO, _vlanDao.findById(userIp.getVlanId()));
        if (!canIpUsedForService(publicIp, service, networkId)) {
            return false;
        }
        if (!offering.isConserveMode()) {
            return canIpUsedForNonConserveService(publicIp, service);
        }
        return true;
    }

    @Override
    public void checkCapabilityForProvider(final Set<Provider> providers, final Service service, final Capability cap, final String capValue) {
        for (final Provider provider : providers) {
            final NetworkElement element = getElementImplementingProvider(provider.getName());
            if (element != null) {
                final Map<Service, Map<Capability, String>> elementCapabilities = element.getCapabilities();
                if (elementCapabilities == null || !elementCapabilities.containsKey(service)) {
                    throw new UnsupportedServiceException("Service " + service.getName() + " is not supported by the element=" + element.getName() +
                            " implementing Provider=" + provider.getName());
                }
                final Map<Capability, String> serviceCapabilities = elementCapabilities.get(service);
                if (serviceCapabilities == null || serviceCapabilities.isEmpty()) {
                    throw new UnsupportedServiceException("Service " + service.getName() + " doesn't have capabilites for element=" + element.getName() +
                            " implementing Provider=" + provider.getName());
                }

                final String value = serviceCapabilities.get(cap);
                if (value == null || value.isEmpty()) {
                    throw new UnsupportedServiceException("Service " + service.getName() + " doesn't have capability " + cap.getName() + " for element=" +
                            element.getName() + " implementing Provider=" + provider.getName());
                }

                if (!value.toLowerCase().contains(capValue.toLowerCase())) {
                    throw new UnsupportedServiceException("Service " + service.getName() + " doesn't support value " + capValue + " for capability " + cap.getName() +
                            " for element=" + element.getName() + " implementing Provider=" + provider.getName());
                }
            } else {
                throw new UnsupportedServiceException("Unable to find network element for provider " + provider.getName());
            }
        }
    }

    @Override
    public void checkNetworkPermissions(final Account owner, final Network network) {
        // dahn 20140310: I was thinking of making this an assert but
        //                as we hardly ever test with asserts I think
        //                we better make sure at runtime.
        if (network == null) {
            throw new CloudRuntimeException("cannot check permissions on (Network) <null>");
        }
        // Perform account permission check
        if (network.getGuestType() != Network.GuestType.Shared || network.getGuestType() == Network.GuestType.Shared && network.getAclType() == ACLType.Account) {
            final AccountVO networkOwner = _accountDao.findById(network.getAccountId());
            if (networkOwner == null) {
                throw new PermissionDeniedException("Unable to use network with id= " + ((NetworkVO) network).getUuid() +
                        ", network does not have an owner");
            }
            if (owner.getType() != Account.ACCOUNT_TYPE_PROJECT && networkOwner.getType() == Account.ACCOUNT_TYPE_PROJECT) {
                if (!_projectAccountDao.canAccessProjectAccount(owner.getAccountId(), network.getAccountId())) {
                    throw new PermissionDeniedException("Unable to use network with id= " + ((NetworkVO) network).getUuid() +
                            ", permission denied");
                }
            } else {
                final List<NetworkVO> networkMap = _networksDao.listBy(owner.getId(), network.getId());
                if (networkMap == null || networkMap.isEmpty()) {
                    throw new PermissionDeniedException("Unable to use network with id= " + ((NetworkVO) network).getUuid() +
                            ", permission denied");
                }
            }
        } else {
            if (!isNetworkAvailableInDomain(network.getId(), owner.getDomainId())) {
                final DomainVO ownerDomain = _domainDao.findById(owner.getDomainId());
                if (ownerDomain == null) {
                    throw new CloudRuntimeException("cannot check permission on account " + owner.getAccountName() + " whose domain does not exist");
                }
                throw new PermissionDeniedException("Shared network id=" + ((NetworkVO) network).getUuid() + " is not available in domain id=" +
                        ownerDomain.getUuid());
            }
        }
    }

    @Override
    public String getDefaultPublicTrafficLabel(final long dcId, final HypervisorType hypervisorType) {
        try {
            final PhysicalNetwork publicPhyNetwork = getOnePhysicalNetworkByZoneAndTrafficType(dcId, TrafficType.Public);
            final PhysicalNetworkTrafficTypeVO publicTraffic = _pNTrafficTypeDao.findBy(publicPhyNetwork.getId(), TrafficType.Public);
            if (publicTraffic != null) {
                String label = null;
                switch (hypervisorType) {
                    case XenServer:
                        label = publicTraffic.getXenNetworkLabel();
                        break;
                    case KVM:
                        label = publicTraffic.getKvmNetworkLabel();
                        break;
                }
                return label;
            }
        } catch (final Exception ex) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Failed to retrieve the default label for public traffic." + "zone: " + dcId + " hypervisor: " + hypervisorType + " due to: " +
                        ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public String getDefaultGuestTrafficLabel(final long dcId, final HypervisorType hypervisorType) {
        try {
            final PhysicalNetwork guestPhyNetwork = getOnePhysicalNetworkByZoneAndTrafficType(dcId, TrafficType.Guest);
            final PhysicalNetworkTrafficTypeVO guestTraffic = _pNTrafficTypeDao.findBy(guestPhyNetwork.getId(), TrafficType.Guest);
            if (guestTraffic != null) {
                String label = null;
                switch (hypervisorType) {
                    case XenServer:
                        label = guestTraffic.getXenNetworkLabel();
                        break;
                    case KVM:
                        label = guestTraffic.getKvmNetworkLabel();
                        break;
                    default:
                        break;
                }
                return label;
            }
        } catch (final Exception ex) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Failed to retrive the default label for management traffic:" + "zone: " + dcId + " hypervisor: " + hypervisorType + " due to:" +
                        ex.getMessage());
            }
        }
        return null;
    }

    @Override
    public List<? extends Network> listNetworksByVpc(final long vpcId) {
        return _networksDao.listByVpc(vpcId);
    }

    @Override
    public List<Provider> getNtwkOffDistinctProviders(final long ntkwOffId) {
        final List<String> providerNames = _ntwkOfferingSrvcDao.getDistinctProviders(ntkwOffId);
        final List<Provider> providers = new ArrayList<>();
        for (final String providerName : providerNames) {
            providers.add(Network.Provider.getProvider(providerName));
        }

        return providers;
    }

    @Override
    public boolean isVmPartOfNetwork(final long vmId, final long ntwkId) {
        if (_nicDao.findNonReleasedByInstanceIdAndNetworkId(ntwkId, vmId) != null) {
            return true;
        }
        return false;
    }

    @Override
    public List<? extends PhysicalNetwork> getPhysicalNtwksSupportingTrafficType(final long zoneId, final TrafficType trafficType) {

        final List<? extends PhysicalNetwork> pNtwks = _physicalNetworkDao.listByZone(zoneId);

        final Iterator<? extends PhysicalNetwork> it = pNtwks.iterator();
        while (it.hasNext()) {
            final PhysicalNetwork pNtwk = it.next();
            if (!_pNTrafficTypeDao.isTrafficTypeSupported(pNtwk.getId(), trafficType)) {
                it.remove();
            }
        }
        return pNtwks;
    }

    @Override
    public boolean isPrivateGateway(final long ntwkId) {
        final Network network = getNetwork(ntwkId);
        s_logger.debug("Network [" + network.getName() + "] is of type [" + network.getGuestType() + "].");
        return Network.GuestType.Private.equals(network.getGuestType());
    }

    @Override
    public List<NetworkOfferingVO> getSystemAccountNetworkOfferings(final String... offeringNames) {
        final List<NetworkOfferingVO> offerings = new ArrayList<>(offeringNames.length);
        for (final String offeringName : offeringNames) {
            final NetworkOfferingVO network = _systemNetworks.get(offeringName);
            if (network == null) {
                throw new CloudRuntimeException("Unable to find system network profile for " + offeringName);
            }
            offerings.add(network);
        }
        return offerings;
    }

    @Override
    public boolean isNetworkAvailableInDomain(final long networkId, final long domainId) {
        final Long networkDomainId;
        final Network network = getNetwork(networkId);
        if (network.getGuestType() != Network.GuestType.Shared) {
            s_logger.trace("Network id=" + networkId + " is not shared");
            return false;
        }

        final NetworkDomainVO networkDomainMap = _networkDomainDao.getDomainNetworkMapByNetworkId(networkId);
        if (networkDomainMap == null) {
            s_logger.trace("Network id=" + networkId + " is shared, but not domain specific");
            return true;
        } else {
            networkDomainId = networkDomainMap.getDomainId();
        }

        if (domainId == networkDomainId.longValue()) {
            return true;
        }

        if (networkDomainMap.subdomainAccess) {
            final Set<Long> parentDomains = _domainMgr.getDomainParentIds(domainId);

            if (parentDomains.contains(networkDomainId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public SortedSet<Long> getAvailableIps(final Network network, final String requestedIp) {
        final String[] cidr = network.getCidr().split("/");
        final List<String> ips = getUsedIpsInNetwork(network);
        final List<String> excludedIps = getExcludedIpsInNetwork(network);
        final SortedSet<Long> usedIps = new TreeSet<>();

        for (final String ip : ips) {
            if (requestedIp != null && requestedIp.equals(ip)) {
                s_logger.warn("Requested ip address " + requestedIp + " is already in use in network" + network);
                return null;
            }

            usedIps.add(NetUtils.ip2Long(ip));
        }

        for (final String ip : excludedIps) {
            if (requestedIp != null && requestedIp.equals(ip)) {
                s_logger.warn("Requested ip address " + requestedIp + " is in excluded IPs range" + ((NetworkVO)network).getIpExclusionList());
                return null;
            }

            usedIps.add(NetUtils.ip2Long(ip));
        }
        final SortedSet<Long> allPossibleIps = NetUtils.getAllIpsFromCidr(cidr[0], Integer.parseInt(cidr[1]), usedIps);

        final String gateway = network.getGateway();
        if (gateway != null && allPossibleIps.contains(NetUtils.ip2Long(gateway))) {
            allPossibleIps.remove(NetUtils.ip2Long(gateway));
        }

        return allPossibleIps;
    }

    public List<String> getExcludedIpsInNetwork(Network network) {
        return NetUtils.getAllIpsFromRangeList(((NetworkVO) network).getIpExclusionList());
    }

    @Override
    public List<String> getUsedIpsInNetwork(final Network network) {
        //Get all ips used by vms nics
        final List<String> ips = _nicDao.listIpAddressInNetwork(network.getId());
        //Get all secondary ips for nics
        final List<String> secondaryIps = _nicSecondaryIpDao.listSecondaryIpAddressInNetwork(network.getId());
        ips.addAll(secondaryIps);
        //Get ips used by load balancers
        final List<String> lbIps = _appLbRuleDao.listLbIpsBySourceIpNetworkId(network.getId());
        ips.addAll(lbIps);
        return ips;
    }

    @Override
    public String getDomainNetworkDomain(final long domainId, final long zoneId) {
        String networkDomain = null;
        Long searchDomainId = domainId;
        while (searchDomainId != null) {
            final DomainVO domain = _domainDao.findById(searchDomainId);
            if (domain.getNetworkDomain() != null) {
                networkDomain = domain.getNetworkDomain();
                break;
            }
            searchDomainId = domain.getParent();
        }
        if (networkDomain == null) {
            return getZoneNetworkDomain(zoneId);
        }
        return networkDomain;
    }

    boolean isProviderEnabled(final PhysicalNetworkServiceProvider provider) {
        if (provider == null || provider.getState() != PhysicalNetworkServiceProvider.State.Enabled) { // TODO: check
            // for other states: Shutdown?
            return false;
        }
        return true;
    }

    boolean isServiceEnabledInNetwork(final long physicalNetworkId, final long networkId, final Service service) {
        // check if the service is supported in the network
        if (!areServicesSupportedInNetwork(networkId, service)) {
            s_logger.debug("Service " + service.getName() + " is not supported in the network id=" + networkId);
            return false;
        }

        // get provider for the service and check if all of them are supported
        final String provider = _ntwkSrvcDao.getProviderForServiceInNetwork(networkId, service);
        if (!isProviderEnabledInPhysicalNetwork(physicalNetworkId, provider)) {
            s_logger.debug("Provider " + provider + " is not enabled in physical network id=" + physicalNetworkId);
            return false;
        }

        return true;
    }

    @Override
    public Nic getNicInNetworkIncludingRemoved(final long vmId, final long networkId) {
        return _nicDao.findByInstanceIdAndNetworkIdIncludingRemoved(networkId, vmId);
    }

    String getZoneNetworkDomain(final long zoneId) {
        return _dcDao.findById(zoneId).getDomain();
    }

    PhysicalNetwork getOnePhysicalNetworkByZoneAndTrafficType(final long zoneId, final TrafficType trafficType) {
        final List<PhysicalNetworkVO> networkList = _physicalNetworkDao.listByZoneAndTrafficType(zoneId, trafficType);

        if (networkList.isEmpty()) {
            throw new InvalidParameterValueException("Unable to find the default physical network with traffic=" + trafficType + " in zone id=" + zoneId + ". ");
        }

        if (networkList.size() > 1) {
            s_logger.info("More than one physical networks exist in zone id=" + zoneId + " with traffic type=" + trafficType + ". ");
        }

        return networkList.get(0);
    }

    protected Long getNonGuestNetworkPhysicalNetworkId(final Network network, final TrafficType trafficType) {
        // VMware control network is management network
        // we need to retrieve traffic label information through physical network
        Long physicalNetworkId = network.getPhysicalNetworkId();

        if (physicalNetworkId == null) {
            final List<PhysicalNetworkVO> pNtwks = _physicalNetworkDao.listByZone(network.getDataCenterId());
            if (pNtwks.size() == 1) {
                physicalNetworkId = pNtwks.get(0).getId();
            } else {
                // locate physicalNetwork with supported traffic type
                // We can make this assumptions based on the fact that Public/Management/Control traffic types are
                // supported only in one physical network in the zone in 3.0
                for (final PhysicalNetworkVO pNtwk : pNtwks) {
                    if (_pNTrafficTypeDao.isTrafficTypeSupported(pNtwk.getId(), trafficType)) {
                        physicalNetworkId = pNtwk.getId();
                        break;
                    }
                }
            }
        }
        return physicalNetworkId;
    }

    protected Long getNonGuestNetworkPhysicalNetworkId(final Network network) {
        // no physical network for control traffic type
        Long physicalNetworkId = network.getPhysicalNetworkId();

        if (physicalNetworkId == null) {
            final List<PhysicalNetworkVO> pNtwks = _physicalNetworkDao.listByZone(network.getDataCenterId());
            if (pNtwks.size() == 1) {
                physicalNetworkId = pNtwks.get(0).getId();
            } else {
                // locate physicalNetwork with supported traffic type
                // We can make this assumptions based on the fact that Public/Management/Control traffic types are
                // supported only in one physical network in the zone in 3.0
                for (final PhysicalNetworkVO pNtwk : pNtwks) {
                    if (_pNTrafficTypeDao.isTrafficTypeSupported(pNtwk.getId(), network.getTrafficType())) {
                        physicalNetworkId = pNtwk.getId();
                        break;
                    }
                }
            }
        }
        return physicalNetworkId;
    }

    @Override
    public NicProfile getNicProfile(final VirtualMachine vm, final long networkId, final String broadcastUri) {
        final NicVO nic;
        if (broadcastUri != null) {
            nic = _nicDao.findByNetworkIdInstanceIdAndBroadcastUri(networkId, vm.getId(), broadcastUri);
        } else {
            nic = _nicDao.findByNtwkIdAndInstanceId(networkId, vm.getId());
        }
        if (nic == null) {
            return null;
        }
        final NetworkVO network = _networksDao.findById(networkId);
        final Integer networkRate = getNetworkRate(network.getId(), vm.getId());

        //        NetworkGuru guru = _networkGurus.get(network.getGuruName());
        final NicProfile profile =
                new NicProfile(nic, network, nic.getBroadcastUri(), nic.getIsolationUri(), networkRate, isSecurityGroupSupportedInNetwork(network), getNetworkTag(
                        vm.getHypervisorType(), network));
        //        guru.updateNicProfile(profile, network);
        return profile;
    }

    @Override
    public boolean networkIsConfiguredForExternalNetworking(final long zoneId, final long networkId) {
        final List<Provider> networkProviders = getNetworkProviders(networkId);
        for (final Provider provider : networkProviders) {
            if (provider.isExternal()) {
                return true;
            }
        }
        return false;
    }

    private List<Provider> getNetworkProviders(final long networkId) {
        final List<String> providerNames = _ntwkSrvcDao.getDistinctProviders(networkId);
        final Map<String, Provider> providers = new HashMap<>();
        for (final String providerName : providerNames) {
            if (!providers.containsKey(providerName)) {
                providers.put(providerName, Network.Provider.getProvider(providerName));
            }
        }

        return new ArrayList<>(providers.values());
    }

    @Override
    public PublicIpAddress getSourceNatIpAddressForGuestNetwork(final Account owner, final Network guestNetwork) {
        final List<? extends IpAddress> addrs = listPublicIpsAssignedToGuestNtwk(owner.getId(), guestNetwork.getId(), true);

        final IPAddressVO sourceNatIp;
        if (addrs.isEmpty()) {
            return null;
        } else {
            for (final IpAddress addr : addrs) {
                if (addr.isSourceNat()) {
                    sourceNatIp = _ipAddressDao.findById(addr.getId());
                    return PublicIp.createFromAddrAndVlan(sourceNatIp, _vlanDao.findById(sourceNatIp.getVlanId()));
                }
            }
        }

        return null;
    }

    @Override
    public boolean isNetworkInlineMode(final Network network) {
        final NetworkOfferingVO offering = _networkOfferingDao.findById(network.getNetworkOfferingId());
        return offering.isInline();
    }

    @Override
    public void checkIp6Parameters(final String startIPv6, final String endIPv6, final String ip6Gateway, final String ip6Cidr) throws InvalidParameterValueException {
        if (!NetUtils.isValidIpv6(startIPv6)) {
            throw new InvalidParameterValueException("Invalid format for the startIPv6 parameter");
        }
        if (!NetUtils.isValidIpv6(endIPv6)) {
            throw new InvalidParameterValueException("Invalid format for the endIPv6 parameter");
        }

        if (!(ip6Gateway != null && ip6Cidr != null)) {
            throw new InvalidParameterValueException("ip6Gateway and ip6Cidr should be defined when startIPv6/endIPv6 are passed in");
        }

        if (!NetUtils.isValidIpv6(ip6Gateway)) {
            throw new InvalidParameterValueException("Invalid ip6Gateway");
        }
        if (!NetUtils.isValidIp6Cidr(ip6Cidr)) {
            throw new InvalidParameterValueException("Invalid ip6cidr");
        }
        if (!NetUtils.isIp6InNetwork(startIPv6, ip6Cidr)) {
            throw new InvalidParameterValueException("startIPv6 is not in ip6cidr indicated network!");
        }
        if (!NetUtils.isIp6InNetwork(endIPv6, ip6Cidr)) {
            throw new InvalidParameterValueException("endIPv6 is not in ip6cidr indicated network!");
        }
        if (!NetUtils.isIp6InNetwork(ip6Gateway, ip6Cidr)) {
            throw new InvalidParameterValueException("ip6Gateway is not in ip6cidr indicated network!");
        }

        final int cidrSize = NetUtils.getIp6CidrSize(ip6Cidr);
        // we only support cidr == 64
        if (cidrSize != 64) {
            throw new InvalidParameterValueException("The cidr size of IPv6 network must be 64 bits!");
        }
    }

    @Override
    public void checkRequestedIpAddresses(final long networkId, final String ip4, final String ip6) throws InvalidParameterValueException {
        if (ip4 != null) {
            if (!NetUtils.isValidIp(ip4)) {
                throw new InvalidParameterValueException("Invalid specified IPv4 address " + ip4);
            }
            //Other checks for ipv4 are done in assignPublicIpAddress()
        }
        if (ip6 != null) {
            if (!NetUtils.isValidIpv6(ip6)) {
                throw new InvalidParameterValueException("Invalid specified IPv6 address " + ip6);
            }
            if (_ipv6Dao.findByNetworkIdAndIp(networkId, ip6) != null) {
                throw new InvalidParameterValueException("The requested IP is already taken!");
            }
            final List<VlanVO> vlans = _vlanDao.listVlansByNetworkId(networkId);
            if (vlans == null) {
                throw new CloudRuntimeException("Cannot find related vlan attached to network " + networkId);
            }
            Vlan ipVlan = null;
            for (final Vlan vlan : vlans) {
                if (NetUtils.isIp6InRange(ip6, vlan.getIp6Range())) {
                    ipVlan = vlan;
                    break;
                }
            }
            if (ipVlan == null) {
                throw new InvalidParameterValueException("Requested IPv6 is not in the predefined range!");
            }
        }
    }

    @Override
    public String getStartIpv6Address(final long networkId) {
        final List<VlanVO> vlans = _vlanDao.listVlansByNetworkId(networkId);
        if (vlans == null) {
            return null;
        }
        String startIpv6 = null;
        // Get the start ip of first create vlan(not the lowest, because if you add a lower vlan, lowest vlan would change)
        for (final Vlan vlan : vlans) {
            if (vlan.getIp6Range() != null) {
                startIpv6 = vlan.getIp6Range().split("-")[0];
                break;
            }
        }
        return startIpv6;
    }

    @Override
    public NicVO getPlaceholderNicForRouter(final Network network, final Long podId) {
        final List<NicVO> nics = _nicDao.listPlaceholderNicsByNetworkIdAndVmType(network.getId(), VirtualMachine.Type.DomainRouter);
        for (final NicVO nic : nics) {
            if (nic.getReserver() == null && (nic.getIPv4Address() != null || nic.getIPv6Address() != null)) {
                if (podId == null) {
                    return nic;
                } else {
                    //return nic only when its ip address belong to the pod range (for the Basic zone case)
                    final List<? extends Vlan> vlans = _vlanDao.listVlansForPod(podId);
                    for (final Vlan vlan : vlans) {
                        if (nic.getIPv4Address() != null) {
                            final IpAddress ip = _ipAddressDao.findByIpAndSourceNetworkId(network.getId(), nic.getIPv4Address());
                            if (ip != null && ip.getVlanId() == vlan.getId()) {
                                return nic;
                            }
                        } else {
                            final UserIpv6AddressVO ipv6 = _ipv6Dao.findByNetworkIdAndIp(network.getId(), nic.getIPv6Address());
                            if (ipv6 != null && ipv6.getVlanId() == vlan.getId()) {
                                return nic;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public IpAddress getPublicIpAddress(final String ipAddress, final long zoneId) {
        final List<? extends Network> networks = _networksDao.listByZoneAndTrafficType(zoneId, TrafficType.Public);
        if (networks.isEmpty() || networks.size() > 1) {
            throw new CloudRuntimeException("Can't find public network in the zone specified");
        }

        return _ipAddressDao.findByIpAndSourceNetworkId(networks.get(0).getId(), ipAddress);
    }

    @Override
    public Map<Detail, String> getNtwkOffDetails(final long offId) {
        return _ntwkOffDetailsDao.getNtwkOffDetails(offId);
    }

    @Override
    public Networks.IsolationType[] listNetworkIsolationMethods() {
        return Networks.IsolationType.values();
    }

    @Override
    public boolean getExecuteInSeqNtwkElmtCmd() {
        return _executeInSequenceNtwkElmtCmd;
    }

    @Override
    public boolean isNetworkReadyForGc(final long networkId) {
        final Network network = getNetwork(networkId);
        final List<Long> networkIds = _networksDao.findNetworksToGarbageCollect();
        final List<String> secondaryIps = _nicSecondaryIpDao.listSecondaryIpAddressInNetwork(networkId);
        if (!networkIds.contains(networkId)) {
            return false;
        }

        // add an exception for networks that use external networking devices and has secondary guest IP's allocated.
        // On network GC, when network goes through implement phase a new vlan is allocated, based on the acquired VLAN
        // id cidr of the network is decided in case of external networking case. While NIC uses reservation strategy 'Start'
        // which ensures that new primary ip is allocated for the NiC from the new CIDR. Secondary IP's have hardcoded IP's in
        // network rules. So prevent network GC.
        if (secondaryIps != null && !secondaryIps.isEmpty() && networkIsConfiguredForExternalNetworking(network.getDataCenterId(), networkId)) {
            return false;
        }

        //if the network has vms in Starting state (nics for those might not be allocated yet as Starting state also used when vm is being Created)
        //don't GC
        if (_nicDao.countNicsForStartingVms(networkId) > 0) {
            s_logger.debug("Network id=" + networkId + " is not ready for GC as it has vms that are Starting at the moment");
            return false;
        }

        return true;
    }

    @Override
    public boolean getNetworkEgressDefaultPolicy(final Long networkId) {
        final NetworkVO network = _networksDao.findById(networkId);

        if (network != null) {
            final NetworkOfferingVO offering = _networkOfferingDao.findById(network.getNetworkOfferingId());
            return offering.getEgressDefaultPolicy();
        } else {
            final InvalidParameterValueException ex = new InvalidParameterValueException("network with network id does not exist");
            throw ex;
        }
    }

    @Override
    public List<String[]> generateVmData(final String userData, final String serviceOffering, final String zoneName,
                                         final String vmName, final long vmId, final String publicKey, final String password, final Boolean isWindows) {
        final List<String[]> vmData = new ArrayList<>();

        if (userData != null) {
            vmData.add(new String[]{"userdata", "user-data", new String(Base64.decodeBase64(userData), StringUtils.getPreferredCharset())});
        }
        vmData.add(new String[]{"metadata", "service-offering", StringUtils.unicodeEscape(serviceOffering)});
        vmData.add(new String[]{"metadata", "availability-zone", StringUtils.unicodeEscape(zoneName)});
        vmData.add(new String[]{"metadata", "local-hostname", StringUtils.unicodeEscape(vmName)});
        vmData.add(new String[]{"metadata", "instance-id", vmName});
        vmData.add(new String[]{"metadata", "vm-id", String.valueOf(vmId)});
        vmData.add(new String[]{"metadata", "public-keys", publicKey});

        String cloudIdentifier = _configDao.getValue("cloud.identifier");
        if (cloudIdentifier == null) {
            cloudIdentifier = "";
        } else {
            cloudIdentifier = "CloudStack-{" + cloudIdentifier + "}";
        }
        vmData.add(new String[]{"metadata", "cloud-identifier", cloudIdentifier});

        if (password != null && !password.isEmpty() && !password.equals("saved_password")) {

            // Here we are calculating MD5 checksum to reduce the over head of calculating MD5 checksum
            // in windows VM in password reset script.

            if (isWindows) {
                final MessageDigest md5;
                try {
                    md5 = MessageDigest.getInstance("MD5");
                } catch (final NoSuchAlgorithmException e) {
                    s_logger.error("Unexpected exception " + e.getMessage(), e);
                    throw new CloudRuntimeException("Unable to get MD5 MessageDigest", e);
                }
                md5.reset();
                md5.update(password.getBytes(StringUtils.getPreferredCharset()));
                final byte[] digest = md5.digest();
                final BigInteger bigInt = new BigInteger(1, digest);
                final String hashtext = bigInt.toString(16);

                vmData.add(new String[]{"password", "vm-password-md5checksum", hashtext});
            }

            vmData.add(new String[]{"password", "vm-password", password});
        }

        return vmData;
    }
}
