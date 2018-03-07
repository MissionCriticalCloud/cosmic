package com.cloud.network.router;

import com.cloud.agent.api.SetupVRCommand;
import com.cloud.agent.api.UpdateNetworkOverviewCommand;
import com.cloud.agent.api.UpdateVmOverviewCommand;
import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.api.routing.SetFirewallRulesCommand;
import com.cloud.agent.api.routing.SetNetworkACLCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesVpcCommand;
import com.cloud.agent.api.routing.SetPublicIpACLCommand;
import com.cloud.agent.api.routing.SetStaticNatRulesCommand;
import com.cloud.agent.api.to.FirewallRuleTO;
import com.cloud.agent.api.to.LoadBalancerTO;
import com.cloud.agent.api.to.NetworkACLTO;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.PortForwardingRuleTO;
import com.cloud.agent.api.to.PublicIpACLTO;
import com.cloud.agent.api.to.StaticNatRuleTO;
import com.cloud.agent.api.to.overviews.NetworkOverviewTO;
import com.cloud.agent.api.to.overviews.VMOverviewTO;
import com.cloud.agent.manager.Commands;
import com.cloud.configuration.Config;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.dc.VlanVO;
import com.cloud.dc.dao.VlanDao;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.network.IpAddress;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.network.VpnUser;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.RemoteAccessVpnDao;
import com.cloud.network.dao.RemoteAccessVpnVO;
import com.cloud.network.dao.Site2SiteCustomerGatewayDao;
import com.cloud.network.dao.Site2SiteCustomerGatewayVO;
import com.cloud.network.dao.Site2SiteVpnConnectionDao;
import com.cloud.network.dao.Site2SiteVpnConnectionVO;
import com.cloud.network.dao.Site2SiteVpnGatewayDao;
import com.cloud.network.dao.Site2SiteVpnGatewayVO;
import com.cloud.network.dao.VpnUserDao;
import com.cloud.network.lb.LoadBalancingRule;
import com.cloud.network.lb.LoadBalancingRule.LbDestination;
import com.cloud.network.lb.LoadBalancingRule.LbStickinessPolicy;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.network.rules.StaticNat;
import com.cloud.network.rules.StaticNatRule;
import com.cloud.network.vpc.NetworkACLItem;
import com.cloud.network.vpc.StaticRouteProfile;
import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.VpcGateway;
import com.cloud.network.vpc.dao.StaticRouteDao;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.StringUtils;
import com.cloud.utils.net.Ip;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.UserVmDao;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;

public class CommandSetupHelper {

    @Inject
    @Qualifier("networkHelper")
    private NetworkHelper _networkHelper;
    @Inject
    private NicDao _nicDao;
    @Inject
    private NetworkDao _networkDao;
    @Inject
    private DomainRouterDao _routerDao;
    @Inject
    private NetworkModel _networkModel;
    @Inject
    private VirtualMachineManager _itMgr;
    @Inject
    private FirewallRulesDao _rulesDao;
    @Inject
    private NetworkOfferingDao _networkOfferingDao;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private ServiceOfferingDao _serviceOfferingDao;
    @Inject
    private UserVmDao _userVmDao;
    @Inject
    private RemoteAccessVpnDao _remoteAccessVpnDao;
    @Inject
    private VpnUserDao _vpnUsersDao;
    @Inject
    private Site2SiteVpnConnectionDao _s2sVpnDao;
    @Inject
    private Site2SiteCustomerGatewayDao _s2sCustomerGatewayDao;
    @Inject
    private Site2SiteVpnGatewayDao _s2sVpnGatewayDao;
    @Inject
    private StaticRouteDao _staticRouteDao;
    @Inject
    private VpcDao _vpcDao;
    @Inject
    private VlanDao _vlanDao;
    @Inject
    private IPAddressDao _ipAddressDao;
    @Inject
    private RouterControlHelper _routerControlHelper;
    @Inject
    private ZoneRepository zoneRepository;

    public void createApplyLoadBalancingRulesCommands(final List<LoadBalancingRule> rules, final VirtualRouter router, final Commands cmds, final long guestNetworkId) {
        final LoadBalancerTO[] lbs = new LoadBalancerTO[rules.size()];
        int i = 0;
        // We don't support VR to be inline currently
        final boolean inline = false;
        for (final LoadBalancingRule rule : rules) {
            final boolean revoked = rule.getState().equals(FirewallRule.State.Revoke);
            final String protocol = rule.getProtocol();
            final String lb_protocol = rule.getLbProtocol();
            final String algorithm = rule.getAlgorithm();
            final String uuid = rule.getUuid();

            final String srcIp = rule.getSourceIp().addr();
            final int srcPort = rule.getSourcePortStart();
            final List<LbDestination> destinations = rule.getDestinations();
            final List<LbStickinessPolicy> stickinessPolicies = rule.getStickinessPolicies();

            // Load default values and fallback to hardcoded if not available
            final Integer defaultClientTimeout = NumbersUtil.parseInt(_configDao.getValue(Config.DefaultLoadBalancerClientTimeout.key()), 60000);
            final Integer defaultServerTimeout = NumbersUtil.parseInt(_configDao.getValue(Config.DefaultLoadBalancerServerTimeout.key()), 60000);

            // set timeouts, use defaults if not available
            Integer clientTimeout = rule.getClientTimeout();
            if (clientTimeout != null) {
                clientTimeout = NumbersUtil.parseInt(clientTimeout.toString(), defaultClientTimeout);
            } else {
                clientTimeout = defaultClientTimeout;
            }
            Integer serverTimeout = rule.getServerTimeout();
            if (serverTimeout != null) {
                serverTimeout = NumbersUtil.parseInt(serverTimeout.toString(), defaultServerTimeout);
            } else {
                serverTimeout = defaultServerTimeout;
            }
            final LoadBalancerTO lb = new LoadBalancerTO(uuid, srcIp, srcPort, protocol, algorithm, revoked, false, inline, destinations, stickinessPolicies,
                    clientTimeout, serverTimeout);
            lb.setLbProtocol(lb_protocol);
            lbs[i++] = lb;
        }
        String routerPublicIp = null;

        if (router instanceof DomainRouterVO) {
            final DomainRouterVO domr = _routerDao.findById(router.getId());
            routerPublicIp = domr.getPublicIpAddress();
            if (routerPublicIp == null) {
                routerPublicIp = router.getPublicIpAddress();
            }
        }

        final Network guestNetwork = _networkModel.getNetwork(guestNetworkId);
        final Nic nic = _nicDao.findByNtwkIdAndInstanceId(guestNetwork.getId(), router.getId());
        final NicProfile nicProfile = new NicProfile(nic, guestNetwork, nic.getBroadcastUri(), nic.getIsolationUri(), _networkModel.getNetworkRate(guestNetwork.getId(),
                router.getId()), _networkModel.isSecurityGroupSupportedInNetwork(guestNetwork), _networkModel.getNetworkTag(router.getHypervisorType(), guestNetwork));
        final NetworkOffering offering = _networkOfferingDao.findById(guestNetwork.getNetworkOfferingId());
        final String maxconn;
        if (offering.getConcurrentConnections() == null) {
            maxconn = _configDao.getValue(Config.NetworkLBHaproxyMaxConn.key());
        } else {
            maxconn = offering.getConcurrentConnections().toString();
        }

        final LoadBalancerConfigCommand cmd = new LoadBalancerConfigCommand(lbs, routerPublicIp, _routerControlHelper.getRouterIpInNetwork(guestNetworkId, router.getId()),
                router.getPrivateIpAddress(), _itMgr.toNicTO(nicProfile, router.getHypervisorType()), router.getVpcId(), maxconn, offering.isKeepAliveEnabled());

        cmd.lbStatsVisibility = _configDao.getValue(Config.NetworkLBHaproxyStatsVisbility.key());
        cmd.lbStatsUri = _configDao.getValue(Config.NetworkLBHaproxyStatsUri.key());
        cmd.lbStatsAuth = _configDao.getValue(Config.NetworkLBHaproxyStatsAuth.key());
        cmd.lbStatsPort = _configDao.getValue(Config.NetworkLBHaproxyStatsPort.key());

        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());
        cmds.addCommand(cmd);
    }

    public void createApplyPortForwardingRulesCommands(final List<? extends PortForwardingRule> rules, final VirtualRouter router, final Commands cmds, final long guestNetworkId) {
        final List<PortForwardingRuleTO> rulesTO = new ArrayList<>();
        if (rules != null) {
            for (final PortForwardingRule rule : rules) {
                final IpAddress sourceIp = _networkModel.getIp(rule.getSourceIpAddressId());
                final PortForwardingRuleTO ruleTO = new PortForwardingRuleTO(rule, null, sourceIp.getAddress().addr());
                rulesTO.add(ruleTO);
            }
        }

        final SetPortForwardingRulesCommand cmd;

        if (router.getVpcId() != null) {
            cmd = new SetPortForwardingRulesVpcCommand(rulesTO);
        } else {
            cmd = new SetPortForwardingRulesCommand(rulesTO);
        }

        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

        cmds.addCommand(cmd);
    }

    public void createApplyStaticNatRulesCommands(final List<? extends StaticNatRule> rules, final VirtualRouter router, final Commands cmds, final long guestNetworkId) {
        final List<StaticNatRuleTO> rulesTO = new ArrayList<>();
        if (rules != null) {
            for (final StaticNatRule rule : rules) {
                final IpAddress sourceIp = _networkModel.getIp(rule.getSourceIpAddressId());
                final StaticNatRuleTO ruleTO = new StaticNatRuleTO(rule, null, sourceIp.getAddress().addr(), rule.getDestIpAddress());
                rulesTO.add(ruleTO);
            }
        }

        final SetStaticNatRulesCommand cmd = new SetStaticNatRulesCommand(rulesTO, router.getVpcId());
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());
        cmds.addCommand(cmd);
    }

    public void createApplyFirewallRulesCommands(final List<? extends FirewallRule> rules, final VirtualRouter router, final Commands cmds, final long guestNetworkId) {
        final List<FirewallRuleTO> rulesTO = new ArrayList<>();
        String systemRule = null;
        Boolean defaultEgressPolicy = false;
        if (rules != null) {
            if (rules.size() > 0) {
                if (rules.get(0).getTrafficType() == FirewallRule.TrafficType.Egress && rules.get(0).getType() == FirewallRule.FirewallRuleType.System) {
                    systemRule = String.valueOf(FirewallRule.FirewallRuleType.System);
                }
            }
            for (final FirewallRule rule : rules) {
                _rulesDao.loadSourceCidrs((FirewallRuleVO) rule);
                final FirewallRule.TrafficType traffictype = rule.getTrafficType();
                if (traffictype == FirewallRule.TrafficType.Ingress) {
                    final IpAddress sourceIp = _networkModel.getIp(rule.getSourceIpAddressId());
                    final FirewallRuleTO ruleTO = new FirewallRuleTO(rule, null, sourceIp.getAddress().addr(), Purpose.Firewall, traffictype);
                    rulesTO.add(ruleTO);
                } else if (rule.getTrafficType() == FirewallRule.TrafficType.Egress) {
                    final NetworkVO network = _networkDao.findById(guestNetworkId);
                    final NetworkOfferingVO offering = _networkOfferingDao.findById(network.getNetworkOfferingId());
                    defaultEgressPolicy = offering.getEgressDefaultPolicy();
                    assert rule.getSourceIpAddressId() == null : "ipAddressId should be null for egress firewall rule. ";
                    final FirewallRuleTO ruleTO = new FirewallRuleTO(rule, null, "", Purpose.Firewall, traffictype, defaultEgressPolicy);
                    rulesTO.add(ruleTO);
                }
            }
        }

        final SetFirewallRulesCommand cmd = new SetFirewallRulesCommand(rulesTO);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());
        if (systemRule != null) {
            cmd.setAccessDetail(NetworkElementCommand.FIREWALL_EGRESS_DEFAULT, systemRule);
        } else {
            cmd.setAccessDetail(NetworkElementCommand.FIREWALL_EGRESS_DEFAULT, String.valueOf(defaultEgressPolicy));
        }

        cmds.addCommand(cmd);
    }

    public void createFirewallRulesCommands(final List<? extends FirewallRule> rules, final VirtualRouter router, final Commands cmds, final long guestNetworkId) {
        final List<FirewallRuleTO> rulesTO = new ArrayList<>();
        String systemRule = null;
        Boolean defaultEgressPolicy = false;
        if (rules != null) {
            if (rules.size() > 0) {
                if (rules.get(0).getTrafficType() == FirewallRule.TrafficType.Egress && rules.get(0).getType() == FirewallRule.FirewallRuleType.System) {
                    systemRule = String.valueOf(FirewallRule.FirewallRuleType.System);
                }
            }
            for (final FirewallRule rule : rules) {
                _rulesDao.loadSourceCidrs((FirewallRuleVO) rule);
                final FirewallRule.TrafficType traffictype = rule.getTrafficType();
                if (traffictype == FirewallRule.TrafficType.Ingress) {
                    final IpAddress sourceIp = _networkModel.getIp(rule.getSourceIpAddressId());
                    final FirewallRuleTO ruleTO = new FirewallRuleTO(rule, null, sourceIp.getAddress().addr(), Purpose.Firewall, traffictype);
                    rulesTO.add(ruleTO);
                } else if (rule.getTrafficType() == FirewallRule.TrafficType.Egress) {
                    final NetworkVO network = _networkDao.findById(guestNetworkId);
                    final NetworkOfferingVO offering = _networkOfferingDao.findById(network.getNetworkOfferingId());
                    defaultEgressPolicy = offering.getEgressDefaultPolicy();
                    assert rule.getSourceIpAddressId() == null : "ipAddressId should be null for egress firewall rule. ";
                    final FirewallRuleTO ruleTO = new FirewallRuleTO(rule, null, "", Purpose.Firewall, traffictype, defaultEgressPolicy);
                    rulesTO.add(ruleTO);
                }
            }
        }

        final SetFirewallRulesCommand cmd = new SetFirewallRulesCommand(rulesTO);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());
        if (systemRule != null) {
            cmd.setAccessDetail(NetworkElementCommand.FIREWALL_EGRESS_DEFAULT, systemRule);
        } else {
            cmd.setAccessDetail(NetworkElementCommand.FIREWALL_EGRESS_DEFAULT, String.valueOf(defaultEgressPolicy));
        }

        cmds.addCommand(cmd);
    }

    private Map<String, ArrayList<PublicIpAddress>> getVlanIpMap(final List<? extends PublicIpAddress> ips) {
        // Ensure that in multiple vlans case we first send all ip addresses of
        // vlan1, then all ip addresses of vlan2, etc..
        final Map<String, ArrayList<PublicIpAddress>> vlanIpMap = new HashMap<>();
        for (final PublicIpAddress ipAddress : ips) {
            final String vlanTag = ipAddress.getVlanTag();
            ArrayList<PublicIpAddress> ipList = vlanIpMap.get(vlanTag);
            if (ipList == null) {
                ipList = new ArrayList<>();
            }
            // domR doesn't support release for sourceNat IP address; so reset
            // the state
            if (ipAddress.isSourceNat() && ipAddress.getState() == IpAddress.State.Releasing) {
                ipAddress.setState(IpAddress.State.Allocated);
            }
            ipList.add(ipAddress);
            vlanIpMap.put(vlanTag, ipList);
        }
        return vlanIpMap;
    }

    public void createNetworkACLsCommands(final List<? extends NetworkACLItem> rules, final VirtualRouter router, final Commands cmds, final long guestNetworkId,
                                          final boolean privateGateway) {
        final List<NetworkACLTO> rulesTO = new ArrayList<>();
        String guestVlan = null;
        final Network guestNtwk = _networkDao.findById(guestNetworkId);
        final URI uri = guestNtwk.getBroadcastUri();
        if (uri != null) {
            guestVlan = BroadcastDomainType.getValue(uri);
        }

        if (rules != null) {
            for (final NetworkACLItem rule : rules) {
                final NetworkACLTO ruleTO = new NetworkACLTO(rule, guestVlan, rule.getTrafficType());
                rulesTO.add(ruleTO);
            }
        }

        final NicTO nicTO = _networkHelper.getNicTO(router, guestNetworkId, null);
        final SetNetworkACLCommand cmd = new SetNetworkACLCommand(rulesTO, nicTO);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.GUEST_VLAN_TAG, guestVlan);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());
        if (privateGateway) {
            cmd.setAccessDetail(NetworkElementCommand.VPC_PRIVATE_GATEWAY, String.valueOf(VpcGateway.Type.Private));
        }

        cmds.addCommand(cmd);
    }

    public void createPublicIpACLsCommands(final List<? extends NetworkACLItem> rules, final VirtualRouter router, final Commands cmds, final IpAddress publicIp) {
        final List<PublicIpACLTO> rulesTO = new ArrayList<>();

        if (rules != null) {
            for (final NetworkACLItem rule : rules) {
                final PublicIpACLTO ruleTO = new PublicIpACLTO(rule, publicIp.getAddress().toString(), rule.getTrafficType());
                rulesTO.add(ruleTO);
            }
        }

        final NicTO nicTO = _networkHelper.getNicTO(router, publicIp.getNetworkId(), null);
        final SetPublicIpACLCommand cmd = new SetPublicIpACLCommand(rulesTO, nicTO, publicIp.getAddress().toString());
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

        cmds.addCommand(cmd);
    }

    public void createPasswordCommand(final VirtualRouter router, final VirtualMachineProfile profile, final NicVO nic, final Commands cmds) {
        final String password = (String) profile.getParameter(VirtualMachineProfile.Param.VmPassword);
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());

        // password should be set only on default network element
        if (password != null && nic.isDefaultNic()) {
            final SavePasswordCommand cmd = new SavePasswordCommand(password, nic.getIPv4Address(), profile.getVirtualMachine().getHostName(),
                    _networkModel.getExecuteInSeqNtwkElmtCmd());
            cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
            cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
            cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

            cmds.addCommand("password", cmd);
        }
    }

    public void createApplyStaticNatCommands(final List<? extends StaticNat> rules, final VirtualRouter router, final Commands cmds) {
        final List<StaticNatRuleTO> rulesTO = new ArrayList<>();
        if (rules != null) {
            for (final StaticNat rule : rules) {
                final IpAddress sourceIp = _networkModel.getIp(rule.getSourceIpAddressId());
                final StaticNatRuleTO ruleTO = new StaticNatRuleTO(0, sourceIp.getAddress().addr(), null, null, rule.getDestIpAddress(), null, null, null, rule.isForRevoke(),
                        false);
                rulesTO.add(ruleTO);
            }
        }

        final SetStaticNatRulesCommand cmd = new SetStaticNatRulesCommand(rulesTO, router.getVpcId());
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());

        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());
        cmds.addCommand(cmd);
    }

    public void createVRConfigCommands(final Vpc vpc, final DomainRouterVO router, final Commands cmds) {
        final SetupVRCommand cmd = new SetupVRCommand(vpc);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());
        cmds.addCommand(cmd);
    }

    public void findIpsToExclude(final List<? extends PublicIpAddress> ips, final List<Ip> ipsToExclude) {
        // Ensure that in multiple vlans case we first send all ip addresses of
        // vlan1, then all ip addresses of vlan2, etc..
        final Map<String, ArrayList<PublicIpAddress>> vlanIpMap = getVlanIpMap(ips);

        for (final Map.Entry<String, ArrayList<PublicIpAddress>> vlanAndIp : vlanIpMap.entrySet()) {
            final List<PublicIpAddress> ipAddrList = vlanAndIp.getValue();

            ipsToExclude.addAll(
                    ipAddrList.stream()
                              .filter(ip -> IpAddress.State.Releasing.equals(ip.getState()))
                              .map(PublicIpAddress::getAddress)
                              .collect(Collectors.toList())
            );
        }
    }

    public NetworkOverviewTO createNetworkOverviewFromRouter(
            final VirtualRouter router,
            final List<Nic> nicsToExclude,
            final List<Ip> ipsToExclude,
            final List<StaticRouteProfile> staticRoutesToExclude,
            final RemoteAccessVpn remoteAccessVpnToExclude,
            final Site2SiteVpnConnection site2siteVpnToExclude
    ) {
        final NetworkOverviewTO networkOverviewTO = new NetworkOverviewTO();
        final List<NetworkOverviewTO.InterfaceTO> interfacesTO = new ArrayList<>();

        final NetworkOverviewTO.ServiceTO servicesTO = new NetworkOverviewTO.ServiceTO();
        final List<NetworkOverviewTO.ServiceTO.ServiceSourceNatTO> serviceSourceNatsTO = new ArrayList<>();

        configureInterfacesAndIps(router, nicsToExclude, ipsToExclude, networkOverviewTO, interfacesTO, serviceSourceNatsTO);
        configureStaticRoutes(router, staticRoutesToExclude, networkOverviewTO);

        servicesTO.setSourceNat(serviceSourceNatsTO.toArray(new NetworkOverviewTO.ServiceTO.ServiceSourceNatTO[serviceSourceNatsTO.size()]));
        networkOverviewTO.setServices(servicesTO);

        final NetworkOverviewTO.VPNTO vpnTO = new NetworkOverviewTO.VPNTO();
        configureRemoteAccessVpn(router, remoteAccessVpnToExclude, vpnTO);
        configureSite2SiteVpn(router, site2siteVpnToExclude, vpnTO);
        networkOverviewTO.setVpn(vpnTO);

        configureSyslog(router, networkOverviewTO);

        return networkOverviewTO;
    }

    private void configureInterfacesAndIps(final VirtualRouter router, final List<Nic> nicsToExclude, final List<Ip> ipsToExclude, final NetworkOverviewTO networkOverviewTO,
                                           final List<NetworkOverviewTO.InterfaceTO> interfacesTO, final List<NetworkOverviewTO.ServiceTO.ServiceSourceNatTO> serviceSourceNatsTO) {
        final List<NicVO> nics = _nicDao.listByVmId(router.getId());
        nics.stream()
            .filter(nic -> !nicsToExclude.contains(nic))
            .forEach(nic -> {
                final NetworkOverviewTO.InterfaceTO interfaceTO = new NetworkOverviewTO.InterfaceTO();
                interfaceTO.setMacAddress(nic.getMacAddress());

                final List<NetworkOverviewTO.InterfaceTO.IPv4AddressTO> ipv4Addresses = new ArrayList<>();
                if (StringUtils.isNotBlank(nic.getIPv4Address()) && StringUtils.isNotBlank(nic.getIPv4Netmask())) {
                    ipv4Addresses.add(new NetworkOverviewTO.InterfaceTO.IPv4AddressTO(
                            NetUtils.getIpv4AddressWithCidrSize(nic.getIPv4Address(), nic.getIPv4Netmask()),
                            nic.getIPv4Gateway())
                    );
                }

                final NetworkVO network = _networkDao.findById(nic.getNetworkId());
                if (network != null) {
                    final TrafficType trafficType = network.getTrafficType();
                    if (TrafficType.Public.equals(trafficType)) {
                        ipv4Addresses.addAll(_ipAddressDao.listByAssociatedVpc(router.getVpcId(), false)
                                                          .stream()
                                                          .filter(ipAddressVO -> !ipsToExclude.contains(ipAddressVO.getAddress()))
                                                          .map(ipAddressVO -> {
                                                              final Ip ip = ipAddressVO.getAddress();
                                                              final VlanVO vlanVO = _vlanDao.findById(ipAddressVO.getVlanId());
                                                              return new NetworkOverviewTO.InterfaceTO.IPv4AddressTO(
                                                                      NetUtils.getIpv4AddressWithCidrSize(ip.addr(), vlanVO.getVlanNetmask()),
                                                                      nic.getIPv4Gateway());
                                                          })
                                                          .collect(Collectors.toList()));

                        serviceSourceNatsTO.addAll(_ipAddressDao.listByAssociatedVpc(router.getVpcId(), true)
                                                                .stream()
                                                                .map(IPAddressVO::getAddress)
                                                                .filter(ip -> !ipsToExclude.contains(ip))
                                                                .map(Ip::addr)
                                                                .map(ip -> new NetworkOverviewTO.ServiceTO.ServiceSourceNatTO(ip, nic.getIPv4Gateway()))
                                                                .collect(Collectors.toList()));
                    }

                    interfaceTO.setMetadata(new NetworkOverviewTO.InterfaceTO.MetadataTO(network));
                }

                interfaceTO.setIpv4Addresses(ipv4Addresses.toArray(new NetworkOverviewTO.InterfaceTO.IPv4AddressTO[ipv4Addresses.size()]));
                interfacesTO.add(interfaceTO);
            });

        networkOverviewTO.setInterfaces(interfacesTO.toArray(new NetworkOverviewTO.InterfaceTO[interfacesTO.size()]));
    }

    private void configureStaticRoutes(final VirtualRouter router, final List<StaticRouteProfile> staticRoutesToExclude, final NetworkOverviewTO networkOverviewTO) {
        final List<NetworkOverviewTO.RouteTO> routesTO = new ArrayList<>();
        if (router.getVpcId() != null) {
            routesTO.addAll(_staticRouteDao.listByVpcId(router.getVpcId())
                                           .stream()
                                           .map(StaticRouteProfile::new)
                                           .filter(route -> !staticRoutesToExclude.contains(route))
                                           .map(route -> new NetworkOverviewTO.RouteTO(route.getCidr(), route.getGwIpAddress(), route.getMetric()))
                                           .collect(Collectors.toList()));
        }
        networkOverviewTO.setRoutes(routesTO.toArray(new NetworkOverviewTO.RouteTO[routesTO.size()]));
    }

    private void configureRemoteAccessVpn(final VirtualRouter router, final RemoteAccessVpn remoteAccessVpnToExclude, final NetworkOverviewTO.VPNTO vpnTO) {
        final RemoteAccessVpnVO vpn = _remoteAccessVpnDao.findByAccountAndVpc(router.getAccountId(), router.getVpcId());
        if (vpn != null && !vpn.equals(remoteAccessVpnToExclude)) {
            final NetworkOverviewTO.VPNTO.RemoteAccessTO remoteAccessTO = new NetworkOverviewTO.VPNTO.RemoteAccessTO();

            final IpAddress serverIp = _networkModel.getIp(vpn.getServerAddressId());
            remoteAccessTO.setVpnServerIp(serverIp.getAddress().addr());
            remoteAccessTO.setPreSharedKey(vpn.getIpsecPresharedKey());

            remoteAccessTO.setIpRange(vpn.getIpRange());
            remoteAccessTO.setLocalIp(vpn.getLocalIp());

            final Vpc vpc = _vpcDao.findById(vpn.getVpcId());
            remoteAccessTO.setLocalCidr(vpc.getCidr());

            remoteAccessTO.setVpnUsers(
                    _vpnUsersDao.listByAccount(vpn.getAccountId())
                                .stream()
                                .filter(vpnUser -> VpnUser.State.Add.equals(vpnUser.getState()) || VpnUser.State.Active.equals(vpnUser.getState()))
                                .map(vpnUser -> new NetworkOverviewTO.VPNTO.RemoteAccessTO.VPNUserTO(vpnUser.getUsername(), vpnUser.getPassword()))
                                .toArray(NetworkOverviewTO.VPNTO.RemoteAccessTO.VPNUserTO[]::new)
            );

            vpnTO.setRemoteAccess(remoteAccessTO);
        }
    }

    private void configureSite2SiteVpn(final VirtualRouter router, final Site2SiteVpnConnection site2siteVpnToExclude, final NetworkOverviewTO.VPNTO vpnTO) {
        vpnTO.setSite2site(_s2sVpnDao.listByVpcId(router.getVpcId())
                                     .stream()
                                     .filter(vpnConnection -> !vpnConnection.equals(site2siteVpnToExclude))
                                     .map(this::toSite2SiteTO)
                                     .toArray(NetworkOverviewTO.VPNTO.Site2SiteTO[]::new));
    }

    private NetworkOverviewTO.VPNTO.Site2SiteTO toSite2SiteTO(Site2SiteVpnConnectionVO vpnConnection) {
        final NetworkOverviewTO.VPNTO.Site2SiteTO site2SiteTO = new NetworkOverviewTO.VPNTO.Site2SiteTO();

        final Site2SiteCustomerGatewayVO customerGateway = _s2sCustomerGatewayDao.findById(vpnConnection.getCustomerGatewayId());
        site2SiteTO.setDpd(customerGateway.getDpd());
        site2SiteTO.setForceEncaps(customerGateway.getEncap());
        site2SiteTO.setLifetime(customerGateway.getEspLifetime());
        site2SiteTO.setEsp(customerGateway.getEspPolicy());
        site2SiteTO.setIkeLifetime(customerGateway.getIkeLifetime());
        site2SiteTO.setIke(customerGateway.getIkePolicy());
        site2SiteTO.setPsk(customerGateway.getIpsecPsk());

        final Site2SiteVpnGatewayVO vpnGateway = _s2sVpnGatewayDao.findById(vpnConnection.getVpnGatewayId());
        final IpAddress ipAddress = _ipAddressDao.findById(vpnGateway.getAddrId());
        site2SiteTO.setLeft(ipAddress.getAddress().addr());
        site2SiteTO.setLeftSubnet(_vpcDao.findById(ipAddress.getVpcId()).getCidr());

        site2SiteTO.setPassive(vpnConnection.isPassive());

        site2SiteTO.setRight(customerGateway.getGatewayIp());
        site2SiteTO.setPeerList(customerGateway.getGuestCidrList());

        return site2SiteTO;
    }

    private void configureSyslog(final VirtualRouter router, final NetworkOverviewTO networkOverviewTO) {
        final Vpc vpc = _vpcDao.findById(router.getVpcId());

        if (StringUtils.isNotBlank(vpc.getSyslogServerList())) {
            final NetworkOverviewTO.SyslogTO syslogTO = new NetworkOverviewTO.SyslogTO();
            syslogTO.setServers(vpc.getSyslogServerList().split(","));
            networkOverviewTO.setSyslog(syslogTO);
        }
    }

    public VMOverviewTO createVmOverviewFromRouter(final VirtualRouter router) {
        final VMOverviewTO vmOverviewTO = new VMOverviewTO();
        final Map<UserVmVO, List<NicVO>> vmsAndNicsMap = new HashMap<>();

        final List<NicVO> routerNics = _nicDao.listByVmId(router.getId());
        for (final NicVO routerNic : routerNics) {
            final Network network = _networkModel.getNetwork(routerNic.getNetworkId());
            if (TrafficType.Guest.equals(network.getTrafficType()) && !Network.GuestType.Sync.equals(network.getGuestType())) {
                _userVmDao.listByNetworkIdAndStates(
                        network.getId(),
                        VirtualMachine.State.Starting,
                        VirtualMachine.State.Running,
                        VirtualMachine.State.Paused,
                        VirtualMachine.State.Migrating,
                        VirtualMachine.State.Stopping
                ).forEach(vm -> {
                    final NicVO nic = _nicDao.findByNtwkIdAndInstanceId(network.getId(), vm.getId());
                    if (nic != null) {
                        if (!vmsAndNicsMap.containsKey(vm)) {
                            vmsAndNicsMap.put(vm, new ArrayList<NicVO>() {{
                                add(nic);
                            }});
                        } else {
                            vmsAndNicsMap.get(vm).add(nic);
                        }
                    }
                });
            }
        }

        final List<VMOverviewTO.VMTO> vmsTO = new ArrayList<>();
        vmsAndNicsMap.forEach((vm, nics) -> {
            _userVmDao.loadDetails(vm);
            final VMOverviewTO.VMTO vmTO = new VMOverviewTO.VMTO(vm.getHostName());
            final List<VMOverviewTO.VMTO.InterfaceTO> interfacesTO = new ArrayList<>();

            final ServiceOfferingVO serviceOffering = _serviceOfferingDao.findByIdIncludingRemoved(vm.getId(), vm.getServiceOfferingId());
            final Zone zone = zoneRepository.findOne(router.getDataCenterId());
            nics.forEach(nic -> {
                final VMOverviewTO.VMTO.InterfaceTO interfaceTO = new VMOverviewTO.VMTO.InterfaceTO(
                        nic.getIPv4Address(),
                        nic.getMacAddress(),
                        nic.isDefaultNic()
                );

                final NetworkVO networkVO = _networkDao.findById(nic.getNetworkId());
                final String vmNameFQDN = networkVO != null ? vm.getHostName() + "." + networkVO.getNetworkDomain() : vm.getHostName();

                final Map<String, String> metadata = interfaceTO.getMetadata();
                metadata.put("service-offering", StringUtils.unicodeEscape(serviceOffering.getDisplayText()));
                metadata.put("availability-zone", StringUtils.unicodeEscape(zone.getName()));
                metadata.put("local-ipv4", nic.getIPv4Address());
                metadata.put("local-hostname", StringUtils.unicodeEscape(vmNameFQDN));
                metadata.put("public-ipv4", router.getPublicIpAddress() != null ? router.getPublicIpAddress() : nic.getIPv4Address());
                metadata.put("public-hostname", router.getPublicIpAddress());
                metadata.put("instance-id", vm.getUuid() != null ? vm.getUuid() : vm.getInstanceName());
                metadata.put("vm-id", vm.getUuid() != null ? vm.getUuid() : String.valueOf(vm.getId()));
                metadata.put("public-keys", vm.getDetail("SSH.PublicKey"));

                final String cloudIdentifier = _configDao.getValue("cloud.identifier");
                metadata.put("cloud-identifier", cloudIdentifier != null ? "CloudStack-{" + cloudIdentifier + "}" : "");

                final Map<String, String> userData = interfaceTO.getUserData();
                userData.put("user-data", vm.getUserData());

                interfacesTO.add(interfaceTO);
            });

            vmTO.setInterfaces(interfacesTO.toArray(new VMOverviewTO.VMTO.InterfaceTO[interfacesTO.size()]));
            vmsTO.add(vmTO);
        });

        vmOverviewTO.setVms(vmsTO.toArray(new VMOverviewTO.VMTO[vmsTO.size()]));

        return vmOverviewTO;
    }

    public UpdateVmOverviewCommand createUpdateVmOverviewCommand(final VirtualRouter router, final VMOverviewTO vmOverview) {
        final UpdateVmOverviewCommand cmd = new UpdateVmOverviewCommand(vmOverview);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());

        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

        return cmd;
    }

    public UpdateNetworkOverviewCommand createUpdateNetworkOverviewCommand(final VirtualRouter router, final NetworkOverviewTO networkOverview) {
        final UpdateNetworkOverviewCommand cmd = new UpdateNetworkOverviewCommand(networkOverview);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());

        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

        return cmd;
    }
}
