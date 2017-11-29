package com.cloud.network.router;

import com.cloud.agent.api.SetupGuestNetworkCommand;
import com.cloud.agent.api.SetupVRCommand;
import com.cloud.agent.api.routing.CreateIpAliasCommand;
import com.cloud.agent.api.routing.DeleteIpAliasCommand;
import com.cloud.agent.api.routing.DhcpEntryCommand;
import com.cloud.agent.api.routing.DnsMasqConfigCommand;
import com.cloud.agent.api.routing.IpAliasTO;
import com.cloud.agent.api.routing.IpAssocCommand;
import com.cloud.agent.api.routing.IpAssocVpcCommand;
import com.cloud.agent.api.routing.LoadBalancerConfigCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;
import com.cloud.agent.api.routing.RemoteAccessVpnCfgCommand;
import com.cloud.agent.api.routing.SavePasswordCommand;
import com.cloud.agent.api.routing.SetFirewallRulesCommand;
import com.cloud.agent.api.routing.SetNetworkACLCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesCommand;
import com.cloud.agent.api.routing.SetPortForwardingRulesVpcCommand;
import com.cloud.agent.api.routing.SetPublicIpACLCommand;
import com.cloud.agent.api.routing.SetSourceNatCommand;
import com.cloud.agent.api.routing.SetStaticNatRulesCommand;
import com.cloud.agent.api.routing.SetStaticRouteCommand;
import com.cloud.agent.api.routing.SetupPrivateGatewayCommand;
import com.cloud.agent.api.routing.Site2SiteVpnCfgCommand;
import com.cloud.agent.api.routing.VmDataCommand;
import com.cloud.agent.api.routing.VpnUsersCfgCommand;
import com.cloud.agent.api.to.DhcpTO;
import com.cloud.agent.api.to.FirewallRuleTO;
import com.cloud.agent.api.to.IpAddressTO;
import com.cloud.agent.api.to.LoadBalancerTO;
import com.cloud.agent.api.to.NetworkACLTO;
import com.cloud.agent.api.to.NetworkOverviewTO;
import com.cloud.agent.api.to.NetworkOverviewTO.InterfaceTO;
import com.cloud.agent.api.to.NetworkOverviewTO.InterfaceTO.IPv4Address;
import com.cloud.agent.api.to.NetworkOverviewTO.InterfaceTO.MetadataTO;
import com.cloud.agent.api.to.NetworkOverviewTO.RouteTO;
import com.cloud.agent.api.to.NetworkOverviewTO.ServiceTO;
import com.cloud.agent.api.to.NetworkOverviewTO.ServiceTO.ServiceSourceNatTO;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.PortForwardingRuleTO;
import com.cloud.agent.api.to.PublicIpACLTO;
import com.cloud.agent.api.to.StaticNatRuleTO;
import com.cloud.agent.manager.Commands;
import com.cloud.configuration.Config;
import com.cloud.dao.EntityManager;
import com.cloud.db.model.Zone;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.dc.dao.VlanDao;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.model.enumeration.NetworkType;
import com.cloud.network.IpAddress;
import com.cloud.network.Network;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PublicIpAddress;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.network.VpnUser;
import com.cloud.network.VpnUserVO;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.Site2SiteCustomerGatewayDao;
import com.cloud.network.dao.Site2SiteCustomerGatewayVO;
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
import com.cloud.network.vpc.PrivateIpAddress;
import com.cloud.network.vpc.StaticRoute;
import com.cloud.network.vpc.StaticRouteProfile;
import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.VpcGateway;
import com.cloud.network.vpc.dao.StaticRouteDao;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.offering.NetworkOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.GuestOSVO;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.StringUtils;
import com.cloud.utils.net.Ip;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.Nic;
import com.cloud.vm.NicIpAlias;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.VirtualMachineProfile;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.NicIpAliasDao;
import com.cloud.vm.dao.NicIpAliasVO;
import com.cloud.vm.dao.UserVmDao;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

public class CommandSetupHelper {

    private static final Logger s_logger = LoggerFactory.getLogger(CommandSetupHelper.class);
    private final String _dnsBasicZoneUpdates = "all";
    @Inject
    @Qualifier("networkHelper")
    protected NetworkHelper _networkHelper;
    @Inject
    private EntityManager _entityMgr;
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
    private NicIpAliasDao _nicIpAliasDao;
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
    private VpnUserDao _vpnUsersDao;
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
    private GuestOSDao _guestOSDao;
    @Inject
    private RouterControlHelper _routerControlHelper;
    @Inject
    private ZoneRepository zoneRepository;

    public void createIpAlias(final VirtualRouter router, final List<IpAliasTO> ipAliasTOs, final Long networkid, final Commands cmds) {

        final String routerip = _routerControlHelper.getRouterIpInNetwork(networkid, router.getId());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        final CreateIpAliasCommand ipaliasCmd = new CreateIpAliasCommand(routerip, ipAliasTOs);
        ipaliasCmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        ipaliasCmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        ipaliasCmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, routerip);
        ipaliasCmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

        cmds.addCommand("ipalias", ipaliasCmd);
    }

    public void configDnsMasq(final VirtualRouter router, final Network network, final Commands cmds) {
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        final List<NicIpAliasVO> ipAliasVOList = _nicIpAliasDao.listByNetworkIdAndState(network.getId(), NicIpAlias.State.active);
        final List<DhcpTO> ipList = new ArrayList<>();

        final NicVO router_guest_nic = _nicDao.findByNtwkIdAndInstanceId(network.getId(), router.getId());
        final String cidr = NetUtils.getCidrFromGatewayAndNetmask(router_guest_nic.getIPv4Gateway(), router_guest_nic.getIPv4Netmask());
        final String[] cidrPair = cidr.split("\\/");
        final String cidrAddress = cidrPair[0];
        final long cidrSize = Long.parseLong(cidrPair[1]);
        final String startIpOfSubnet = NetUtils.getIpRangeStartIpFromCidr(cidrAddress, cidrSize);

        ipList.add(new DhcpTO(router_guest_nic.getIPv4Address(), router_guest_nic.getIPv4Gateway(), router_guest_nic.getIPv4Netmask(), startIpOfSubnet));
        for (final NicIpAliasVO ipAliasVO : ipAliasVOList) {
            final DhcpTO DhcpTO = new DhcpTO(ipAliasVO.getIp4Address(), ipAliasVO.getGateway(), ipAliasVO.getNetmask(), ipAliasVO.getStartIpOfSubnet());
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("configDnsMasq : adding ip {" + DhcpTO.getGateway() + ", " + DhcpTO.getNetmask() + ", " + DhcpTO.getRouterIp() + ", " + DhcpTO.getStartIpOfSubnet()
                        + "}");
            }
            ipList.add(DhcpTO);
            ipAliasVO.setVmId(router.getId());
        }
        final DnsMasqConfigCommand dnsMasqConfigCmd = new DnsMasqConfigCommand(ipList);
        dnsMasqConfigCmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        dnsMasqConfigCmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        dnsMasqConfigCmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(network.getId(), router.getId()));
        dnsMasqConfigCmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());
        cmds.addCommand("dnsMasqConfig", dnsMasqConfigCmd);
    }

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
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(guestNetworkId, router.getId()));
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
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(guestNetworkId, router.getId()));
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
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(guestNetworkId, router.getId()));
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
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(guestNetworkId, router.getId()));
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
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(guestNetworkId, router.getId()));
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

    public void createAssociateIPCommands(final VirtualRouter router, final List<? extends PublicIpAddress> ips, final Commands cmds) {
        final String ipAssocCommand = "IPAssocCommand";
        createRedundantAssociateIPCommands(router, ips, cmds, ipAssocCommand);
    }

    public void createRedundantAssociateIPCommands(final VirtualRouter router, final List<? extends PublicIpAddress> ips, final Commands cmds, final String ipAssocCommand) {
        final Map<String, ArrayList<PublicIpAddress>> vlanIpMap = getVlanIpMap(ips);

        for (final Map.Entry<String, ArrayList<PublicIpAddress>> vlanAndIp : vlanIpMap.entrySet()) {
            final List<PublicIpAddress> ipAddrList = vlanAndIp.getValue();
            // Source nat ip address should always be sent first
            Collections.sort(ipAddrList, (o1, o2) -> {
                final boolean s1 = o1.isSourceNat();
                final boolean s2 = o2.isSourceNat();
                return (s1 ^ s2) ? (!s1 ? 1 : -1) : 0;
            });

            // Get network rate - required for IpAssoc
            final Integer networkRate = _networkModel.getNetworkRate(ipAddrList.get(0).getNetworkId(), router.getId());
            final Network network = _networkModel.getNetwork(ipAddrList.get(0).getNetworkId());

            final IpAddressTO[] ipsToSend = new IpAddressTO[ipAddrList.size()];
            int i = 0;

            for (final PublicIpAddress ipAddr : ipAddrList) {

                final boolean add = ipAddr.getState() != IpAddress.State.Releasing;
                final boolean sourceNat = ipAddr.isSourceNat();
                final String vlanId = ipAddr.getVlanTag();
                final String vlanGateway = ipAddr.getGateway();
                final String vlanNetmask = ipAddr.getNetmask();
                final String deviceMacAddress = getMacAddressOfPluggedNic(router, ipAddr.getNetworkId());
                final String ipAddress = ipAddr.getAddress().addr();

                final IpAddressTO ip = new IpAddressTO(ipAddr.getAccountId(), ipAddress, add, false, sourceNat,
                        vlanId, vlanGateway, vlanNetmask, deviceMacAddress, networkRate, ipAddr.isOneToOneNat());

                ip.setTrafficType(network.getTrafficType());
                ip.setNetworkName(_networkModel.getNetworkTag(router.getHypervisorType(), network));
                ipsToSend[i++] = ip;
            }

            Long associatedWithNetworkId = ipAddrList.get(0).getAssociatedWithNetworkId();
            if (associatedWithNetworkId == null || associatedWithNetworkId == 0) {
                associatedWithNetworkId = ipAddrList.get(0).getNetworkId();
            }

            final IpAssocCommand cmd;
            if (ipAssocCommand.equals("IPAssocVpcCommand")) {
                cmd = new IpAssocVpcCommand(ipsToSend);
            } else {
                cmd = new IpAssocCommand(ipsToSend);
            }

            final List<Ip> ipsToExclude = ipAddrList.stream()
                                                    .filter(ip -> IpAddress.State.Releasing.equals(ip.getState()))
                                                    .map(PublicIpAddress::getAddress)
                                                    .collect(Collectors.toList());

            final NetworkOverviewTO networkOverviewTO = createNetworkOverviewFromRouter(router, new ArrayList<>(), ipsToExclude, new ArrayList<>());
            cmd.setNetworkOverview(networkOverviewTO);

            cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
            cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(associatedWithNetworkId, router.getId()));
            cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
            final Zone zone = zoneRepository.findOne(router.getDataCenterId());
            cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

            cmds.addCommand(ipAssocCommand, cmd);
        }
    }

    protected Map<String, ArrayList<PublicIpAddress>> getVlanIpMap(final List<? extends PublicIpAddress> ips) {
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

    protected String getMacAddressOfPluggedNic(final VirtualRouter router, final Long networkId) {
        final List<NicVO> nics = _nicDao.listByVmId(router.getId());
        String deviceMacAddress = null;
        for (final NicVO nic : nics) {
            if (nic.getNetworkId() == networkId) {
                deviceMacAddress = nic.getMacAddress();
                break;
            }
        }
        return deviceMacAddress;
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
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(guestNetworkId, router.getId()));
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
            cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(nic.getNetworkId(), router.getId()));
            cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
            cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

            cmds.addCommand("password", cmd);
        }
    }

    public void createApplyStaticNatCommands(final List<? extends StaticNat> rules, final VirtualRouter router, final Commands cmds, final long guestNetworkId) {
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
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(guestNetworkId, router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());

        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());
        cmds.addCommand(cmd);
    }

    public void createStaticRouteCommands(final List<StaticRouteProfile> staticRoutes, final VirtualRouter router, final Commands cmds) {
        final SetStaticRouteCommand cmd = new SetStaticRouteCommand(staticRoutes);

        final List<StaticRouteProfile> staticRoutesToExclude = staticRoutes.stream().filter(route -> route.getState().equals(StaticRoute.State.Revoke)).collect(Collectors.toList());

        final NetworkOverviewTO networkOverviewTO = createNetworkOverviewFromRouter(router, new ArrayList<>(), new ArrayList<>(), staticRoutesToExclude);
        cmd.setNetworkOverview(networkOverviewTO);

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

    public void createApplyVpnCommands(final boolean isCreate, final RemoteAccessVpn vpn, final VirtualRouter router, final Commands cmds) {
        final List<VpnUserVO> vpnUsers = _vpnUsersDao.listByAccount(vpn.getAccountId());

        createApplyVpnUsersCommand(vpnUsers, router, cmds);

        final IpAddress ip = _networkModel.getIp(vpn.getServerAddressId());

        // This block is needed due to the line 206 of the
        // RemoteAccessVpnManagenerImpl:
        // TODO: assumes one virtual network / domr per account per zone
        final String cidr;
        final Network network = _networkDao.findById(vpn.getNetworkId());
        if (network == null) {
            final Vpc vpc = _vpcDao.findById(vpn.getVpcId());
            cidr = vpc.getCidr();
        } else {
            cidr = network.getCidr();
        }

        final RemoteAccessVpnCfgCommand startVpnCmd = new RemoteAccessVpnCfgCommand(isCreate, ip.getAddress().addr(), vpn.getLocalIp(), vpn.getIpRange(),
                vpn.getIpsecPresharedKey(), vpn.getVpcId() != null);
        startVpnCmd.setLocalCidr(cidr);
        startVpnCmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        startVpnCmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        startVpnCmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

        cmds.addCommand("startVpn", startVpnCmd);
    }

    public void createApplyVpnUsersCommand(final List<? extends VpnUser> users, final VirtualRouter router, final Commands cmds) {
        final List<VpnUser> addUsers = new ArrayList<>();
        final List<VpnUser> removeUsers = new ArrayList<>();
        for (final VpnUser user : users) {
            if (user.getState() == VpnUser.State.Add || user.getState() == VpnUser.State.Active) {
                addUsers.add(user);
            } else if (user.getState() == VpnUser.State.Revoke) {
                removeUsers.add(user);
            }
        }

        final VpnUsersCfgCommand cmd = new VpnUsersCfgCommand(addUsers, removeUsers);
        cmd.setAccessDetail(NetworkElementCommand.ACCOUNT_ID, String.valueOf(router.getAccountId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

        cmds.addCommand("users", cmd);
    }

    public void createVmDataCommandForVMs(final DomainRouterVO router, final Commands cmds, final long guestNetworkId) {
        final List<UserVmVO> vms = _userVmDao.listByNetworkIdAndStates(guestNetworkId, VirtualMachine.State.Running, VirtualMachine.State.Migrating, VirtualMachine.State.Stopping);
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        for (final UserVmVO vm : vms) {
            boolean createVmData = true;
            if (zone.getNetworkType() == NetworkType.Basic && router.getPodIdToDeployIn().longValue() != vm.getPodIdToDeployIn().longValue()) {
                createVmData = false;
            }

            if (createVmData) {
                final NicVO nic = _nicDao.findByNtwkIdAndInstanceId(guestNetworkId, vm.getId());
                if (nic != null) {
                    s_logger.debug("Creating user data entry for vm " + vm + " on domR " + router);
                    _userVmDao.loadDetails(vm);
                    createVmDataCommand(router, vm, nic, vm.getDetail("SSH.PublicKey"), cmds);
                }
            }
        }
    }

    public void createVmDataCommand(final VirtualRouter router, final UserVm vm, final NicVO nic, final String publicKey, final Commands cmds) {
        final String serviceOffering = _serviceOfferingDao.findByIdIncludingRemoved(vm.getId(), vm.getServiceOfferingId()).getDisplayText();
        final String zoneName = zoneRepository.findOne(router.getDataCenterId()).getName();
        cmds.addCommand(
                "vmdata",
                generateVmDataCommand(router, nic.getIPv4Address(), vm.getUserData(), serviceOffering, zoneName, nic.getIPv4Address(), vm.getHostName(), vm.getInstanceName(),
                        vm.getId(), vm.getUuid(), publicKey, nic.getNetworkId()));
    }

    private VmDataCommand generateVmDataCommand(final VirtualRouter router, final String vmPrivateIpAddress, final String userData, final String serviceOffering,
                                                final String zoneName, final String guestIpAddress, final String vmName, final String vmInstanceName, final long vmId, final
                                                String vmUuid, final String publicKey, final long guestNetworkId) {
        final VmDataCommand cmd = new VmDataCommand(vmPrivateIpAddress, vmName, _networkModel.getExecuteInSeqNtwkElmtCmd());

        final NetworkVO networkVO = _networkDao.findById(guestNetworkId);
        String vmNameFQDN = vmName;
        if (networkVO != null) {
            vmNameFQDN = vmName + "." + networkVO.getNetworkDomain();
        }

        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(guestNetworkId, router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());

        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

        cmd.addVmData("userdata", "user-data", userData);
        cmd.addVmData("metadata", "service-offering", StringUtils.unicodeEscape(serviceOffering));
        cmd.addVmData("metadata", "availability-zone", StringUtils.unicodeEscape(zoneName));
        cmd.addVmData("metadata", "local-ipv4", guestIpAddress);
        cmd.addVmData("metadata", "local-hostname", StringUtils.unicodeEscape(vmNameFQDN));
        if (zone.getNetworkType() == NetworkType.Basic) {
            cmd.addVmData("metadata", "public-ipv4", guestIpAddress);
            cmd.addVmData("metadata", "public-hostname", StringUtils.unicodeEscape(vmName));
        } else {
            if (router.getPublicIpAddress() == null) {
                cmd.addVmData("metadata", "public-ipv4", guestIpAddress);
            } else {
                cmd.addVmData("metadata", "public-ipv4", router.getPublicIpAddress());
            }
            cmd.addVmData("metadata", "public-hostname", router.getPublicIpAddress());
        }
        if (vmUuid == null) {
            cmd.addVmData("metadata", "instance-id", vmInstanceName);
            cmd.addVmData("metadata", "vm-id", String.valueOf(vmId));
        } else {
            cmd.addVmData("metadata", "instance-id", vmUuid);
            cmd.addVmData("metadata", "vm-id", vmUuid);
        }
        cmd.addVmData("metadata", "public-keys", publicKey);

        String cloudIdentifier = _configDao.getValue("cloud.identifier");
        if (cloudIdentifier == null) {
            cloudIdentifier = "";
        } else {
            cloudIdentifier = "CloudStack-{" + cloudIdentifier + "}";
        }
        cmd.addVmData("metadata", "cloud-identifier", cloudIdentifier);

        return cmd;
    }

    public void createDhcpEntryCommandsForVMs(final DomainRouterVO router, final Commands cmds, final long guestNetworkId) {
        final List<UserVmVO> vms = _userVmDao.listByNetworkIdAndStates(guestNetworkId, VirtualMachine.State.Running, VirtualMachine.State.Migrating, VirtualMachine.State.Stopping);
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        for (final UserVmVO vm : vms) {
            boolean createDhcp = true;
            if (zone.getNetworkType() == NetworkType.Basic && router.getPodIdToDeployIn().longValue() != vm.getPodIdToDeployIn().longValue()
                    && _dnsBasicZoneUpdates.equalsIgnoreCase("pod")) {
                createDhcp = false;
            }
            if (createDhcp) {
                final NicVO nic = _nicDao.findByNtwkIdAndInstanceId(guestNetworkId, vm.getId());
                if (nic != null) {
                    s_logger.debug("Creating dhcp entry for vm " + vm + " on domR " + router + ".");
                    createDhcpEntryCommand(router, vm, nic, cmds);
                }
            }
        }
    }

    public void createDhcpEntryCommand(final VirtualRouter router, final UserVm vm, final NicVO nic, final Commands cmds) {
        final DhcpEntryCommand dhcpCommand = new DhcpEntryCommand(nic.getMacAddress(), nic.getIPv4Address(), vm.getHostName(), nic.getIPv6Address(),
                _networkModel.getExecuteInSeqNtwkElmtCmd());

        if (nic.isDefaultNic()) {
            dhcpCommand.setDefaultRouter(nic.getIPv4Gateway());
        }

        final Zone zone = zoneRepository.findOne(router.getDataCenterId());

        dhcpCommand.setIp6Gateway(nic.getIPv6Gateway());
        String ipaddress = null;
        final NicVO domrDefaultNic = findDefaultDnsIp(vm.getId());
        if (domrDefaultNic != null) {
            ipaddress = domrDefaultNic.getIPv4Address();
        }
        dhcpCommand.setDefaultDns(ipaddress);
        dhcpCommand.setDuid(NetUtils.getDuidLL(nic.getMacAddress()));
        dhcpCommand.setDefault(nic.isDefaultNic());

        dhcpCommand.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        dhcpCommand.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        dhcpCommand.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(nic.getNetworkId(), router.getId()));
        dhcpCommand.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

        cmds.addCommand("dhcp", dhcpCommand);
    }

    private NicVO findDefaultDnsIp(final long userVmId) {
        final NicVO defaultNic = _nicDao.findDefaultNicForVM(userVmId);

        // check if DNS provider is the domR
        if (!_networkModel.isProviderSupportServiceInNetwork(defaultNic.getNetworkId(), Service.Dns, Provider.VirtualRouter)) {
            return null;
        }

        final NetworkOffering offering = _networkOfferingDao.findById(_networkDao.findById(defaultNic.getNetworkId()).getNetworkOfferingId());
        if (offering.getRedundantRouter()) {
            return findGatewayIp(userVmId);
        }

        final Zone zone = zoneRepository.findOne(_networkModel.getNetwork(defaultNic.getNetworkId()).getDataCenterId());
        final boolean isZoneBasic = zone.getNetworkType() == NetworkType.Basic;

        // find domR's nic in the network
        final NicVO domrDefaultNic;
        if (isZoneBasic) {
            domrDefaultNic = _nicDao.findByNetworkIdTypeAndGateway(defaultNic.getNetworkId(), VirtualMachine.Type.DomainRouter, defaultNic.getIPv4Gateway());
        } else {
            domrDefaultNic = _nicDao.findByNetworkIdAndType(defaultNic.getNetworkId(), VirtualMachine.Type.DomainRouter);
        }
        return domrDefaultNic;
    }

    private NicVO findGatewayIp(final long userVmId) {
        final NicVO defaultNic = _nicDao.findDefaultNicForVM(userVmId);
        return defaultNic;
    }

    public void createDeleteIpAliasCommand(final DomainRouterVO router, final List<IpAliasTO> deleteIpAliasTOs, final List<IpAliasTO> createIpAliasTos, final long networkId,
                                           final Commands cmds) {
        final String routerip = _routerControlHelper.getRouterIpInNetwork(networkId, router.getId());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        final DeleteIpAliasCommand deleteIpaliasCmd = new DeleteIpAliasCommand(routerip, deleteIpAliasTOs, createIpAliasTos);
        deleteIpaliasCmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        deleteIpaliasCmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        deleteIpaliasCmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, routerip);
        deleteIpaliasCmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

        cmds.addCommand("deleteIpalias", deleteIpaliasCmd);
    }

    public void createVpcAssociatePublicIPCommands(final VirtualRouter router, final List<? extends PublicIpAddress> ips, final Commands cmds) {
        final String ipAssocCommand = "IPAssocVpcCommand";
        if (router.getIsRedundantRouter()) {
            createRedundantAssociateIPCommands(router, ips, cmds, ipAssocCommand);
            return;
        }

        Pair<IpAddressTO, Long> sourceNatIpAdd = null;
        Boolean addSourceNat = null;
        // Ensure that in multiple vlans case we first send all ip addresses of
        // vlan1, then all ip addresses of vlan2, etc..
        final Map<String, ArrayList<PublicIpAddress>> vlanIpMap = getVlanIpMap(ips);

        for (final Map.Entry<String, ArrayList<PublicIpAddress>> vlanAndIp : vlanIpMap.entrySet()) {
            final List<PublicIpAddress> ipAddrList = vlanAndIp.getValue();

            // Get network rate - required for IpAssoc
            final Integer networkRate = _networkModel.getNetworkRate(ipAddrList.get(0).getNetworkId(), router.getId());
            final Network network = _networkModel.getNetwork(ipAddrList.get(0).getNetworkId());

            final IpAddressTO[] ipsToSend = new IpAddressTO[ipAddrList.size()];
            int i = 0;

            for (final PublicIpAddress ipAddr : ipAddrList) {
                final boolean add = ipAddr.getState() != IpAddress.State.Releasing;

                final String deviceMacAddress = getMacAddressOfPluggedNic(router, ipAddr.getNetworkId());
                final IpAddressTO ip = new IpAddressTO(ipAddr.getAccountId(), ipAddr.getAddress().addr(), add, false,
                        ipAddr.isSourceNat(), BroadcastDomainType.fromString(ipAddr.getVlanTag()).toString(),
                        ipAddr.getGateway(), ipAddr.getNetmask(), deviceMacAddress, networkRate,
                        ipAddr.isOneToOneNat());

                ip.setTrafficType(network.getTrafficType());
                ip.setNetworkName(_networkModel.getNetworkTag(router.getHypervisorType(), network));

                ipsToSend[i++] = ip;
                if (ipAddr.isSourceNat()) {
                    sourceNatIpAdd = new Pair<>(ip, ipAddr.getNetworkId());
                    addSourceNat = add;
                }
            }
            final IpAssocVpcCommand cmd = new IpAssocVpcCommand(ipsToSend);

            final List<Ip> ipsToExclude = ipAddrList.stream()
                                                    .filter(ip -> IpAddress.State.Releasing.equals(ip.getState()))
                                                    .map(PublicIpAddress::getAddress)
                                                    .collect(Collectors.toList());

            final NetworkOverviewTO networkOverviewTO = createNetworkOverviewFromRouter(router, new ArrayList<>(), ipsToExclude, new ArrayList<>());
            cmd.setNetworkOverview(networkOverviewTO);

            cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
            cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(ipAddrList.get(0).getNetworkId(), router.getId()));
            cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
            final Zone zone = zoneRepository.findOne(router.getDataCenterId());
            cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

            cmds.addCommand(ipAssocCommand, cmd);
        }

        // set source nat ip
        if (sourceNatIpAdd != null) {
            final IpAddressTO sourceNatIp = sourceNatIpAdd.first();
            final SetSourceNatCommand cmd = new SetSourceNatCommand(sourceNatIp, addSourceNat);
            cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
            cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
            final Zone zone = zoneRepository.findOne(router.getDataCenterId());
            cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());
            cmds.addCommand("SetSourceNatCommand", cmd);
        }
    }

    public void createSite2SiteVpnCfgCommands(final Site2SiteVpnConnection conn, final boolean isCreate, final VirtualRouter router, final Commands cmds) {
        final Site2SiteCustomerGatewayVO gw = _s2sCustomerGatewayDao.findById(conn.getCustomerGatewayId());
        final Site2SiteVpnGatewayVO vpnGw = _s2sVpnGatewayDao.findById(conn.getVpnGatewayId());
        final IpAddress ip = _ipAddressDao.findById(vpnGw.getAddrId());
        final Vpc vpc = _vpcDao.findById(ip.getVpcId());
        final String localPublicIp = ip.getAddress().toString();
        final String localGuestCidr = vpc.getCidr();
        final String localPublicGateway = _vlanDao.findById(ip.getVlanId()).getVlanGateway();
        final String peerGatewayIp = gw.getGatewayIp();
        final String peerGuestCidrList = gw.getGuestCidrList();
        final String ipsecPsk = gw.getIpsecPsk();
        final String ikePolicy = gw.getIkePolicy();
        final String espPolicy = gw.getEspPolicy();
        final Long ikeLifetime = gw.getIkeLifetime();
        final Long espLifetime = gw.getEspLifetime();
        final Boolean dpd = gw.getDpd();
        final Boolean encap = gw.getEncap();

        final Site2SiteVpnCfgCommand cmd = new Site2SiteVpnCfgCommand(isCreate, localPublicIp, localPublicGateway, localGuestCidr, peerGatewayIp, peerGuestCidrList, ikePolicy,
                espPolicy, ipsecPsk, ikeLifetime, espLifetime, dpd, conn.isPassive(), encap);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());
        cmds.addCommand("applyS2SVpn", cmd);
    }

    public void createSetupPrivateGatewayCommand(final VirtualRouter router, final PrivateIpAddress ipAddr, final Commands cmds, final NicProfile nicProfile, final boolean add) {
        final Network network = _networkModel.getNetwork(ipAddr.getNetworkId());
        final IpAddressTO ip = new IpAddressTO(Account.ACCOUNT_ID_SYSTEM, ipAddr.getIpAddress(), add, false, ipAddr.getSourceNat(), ipAddr.getBroadcastUri(),
                ipAddr.getGateway(), ipAddr.getNetmask(), ipAddr.getMacAddress(), null, false);

        ip.setTrafficType(network.getTrafficType());
        ip.setNetworkName(_networkModel.getNetworkTag(router.getHypervisorType(), network));

        final SetupPrivateGatewayCommand cmd = new SetupPrivateGatewayCommand(ip);

        final List<Ip> ipsToExclude = new ArrayList<>();
        if (!add) {
            ipsToExclude.add(new Ip(ipAddr.getIpAddress()));
        }

        final NetworkOverviewTO networkOverview = createNetworkOverviewFromRouter(router, new ArrayList<>(), ipsToExclude, new ArrayList<>());
        cmd.setNetworkOverview(networkOverview);
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(ipAddr.getNetworkId(), router.getId()));
        cmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());
        final Zone zone = zoneRepository.findOne(router.getDataCenterId());
        cmd.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, zone.getNetworkType().toString());

        cmds.addCommand("SetupPrivateGatewayCommand", cmd);
    }

    public SetupGuestNetworkCommand createSetupGuestNetworkCommand(final VirtualRouter router, final boolean add, final NicProfile guestNic) {
        final Network network = _networkModel.getNetwork(guestNic.getNetworkId());

        final Nic nic = _nicDao.findByNtwkIdAndInstanceId(network.getId(), router.getId());
        final NicProfile nicProfile = _networkModel.getNicProfile(router, nic.getNetworkId(), null);

        final SetupGuestNetworkCommand setupCmd = new SetupGuestNetworkCommand(_itMgr.toNicTO(nicProfile, router.getHypervisorType()));

        final List<Nic> nicsToExclude = new ArrayList<>();
        if (!add) {
            nicsToExclude.add(nic);
        }

        final NetworkOverviewTO networkOverview = createNetworkOverviewFromRouter(router, nicsToExclude, new ArrayList<>(), new ArrayList<>());
        setupCmd.setNetworkOverview(networkOverview);

        final String brd = NetUtils.long2Ip(NetUtils.ip2Long(guestNic.getIPv4Address()) | ~NetUtils.ip2Long(guestNic.getIPv4Netmask()));
        setupCmd.setAccessDetail(NetworkElementCommand.ROUTER_IP, _routerControlHelper.getRouterControlIp(router.getId()));
        setupCmd.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, _routerControlHelper.getRouterIpInNetwork(network.getId(), router.getId()));

        setupCmd.setAccessDetail(NetworkElementCommand.GUEST_NETWORK_GATEWAY, network.getGateway());
        setupCmd.setAccessDetail(NetworkElementCommand.GUEST_BRIDGE, brd);
        setupCmd.setAccessDetail(NetworkElementCommand.ROUTER_NAME, router.getInstanceName());

        if (network.getBroadcastDomainType() == BroadcastDomainType.Vlan) {
            final long guestVlanTag = Long.parseLong(BroadcastDomainType.Vlan.getValueFrom(network.getBroadcastUri()));
            setupCmd.setAccessDetail(NetworkElementCommand.GUEST_VLAN_TAG, String.valueOf(guestVlanTag));
        }

        return setupCmd;
    }

    private NetworkOverviewTO createNetworkOverviewFromRouter(final VirtualRouter router, final List<Nic> nicsToExclude, final List<Ip> ipsToExclude, final List<StaticRouteProfile>
            staticRoutesToExclude) {
        final NetworkOverviewTO networkOverviewTO = new NetworkOverviewTO();
        final List<InterfaceTO> interfacesTO = new ArrayList<>();

        final ServiceTO servicesTO = new ServiceTO();
        final List<ServiceSourceNatTO> serviceSourceNatsTO = new ArrayList<>();

        final List<RouteTO> routesTO = new ArrayList<>();
        if (router.getVpcId() != null) {
            routesTO.addAll(_staticRouteDao.listByVpcId(router.getVpcId())
                                           .stream()
                                           .map(StaticRouteProfile::new)
                                           .filter(route -> !staticRoutesToExclude.contains(route))
                                           .map(route -> new RouteTO(route.getCidr(), route.getGwIpAddress(), route.getMetric()))
                                           .collect(Collectors.toList()));
        }
        networkOverviewTO.setRoutes(routesTO.toArray(new RouteTO[routesTO.size()]));

        final List<NicVO> nics = _nicDao.listByVmId(router.getId());
        nics.stream()
            .filter(nic -> !nicsToExclude.contains(nic))
            .forEach(nic -> {
                final InterfaceTO interfaceTO = new InterfaceTO();
                interfaceTO.setMacAddress(nic.getMacAddress());

                final List<IPv4Address> ipv4Addresses = new ArrayList<>();
                if (StringUtils.isNotBlank(nic.getIPv4Address()) && StringUtils.isNotBlank(nic.getIPv4Netmask())) {
                    ipv4Addresses.add(new IPv4Address(NetUtils.getIpv4AddressWithCidrSize(nic.getIPv4Address(), nic.getIPv4Netmask()), nic.getIPv4Gateway()));
                }

                final NetworkVO network = _networkDao.findById(nic.getNetworkId());
                if (network != null) {
                    final TrafficType trafficType = network.getTrafficType();
                    if (TrafficType.Public.equals(trafficType)) {
                        if (router.getVpcId() != null) {
                            ipv4Addresses.addAll(_ipAddressDao.listByAssociatedVpc(router.getVpcId(), false)
                                                              .stream()
                                                              .map(IPAddressVO::getAddress)
                                                              .filter(ip -> !ipsToExclude.contains(ip))
                                                              .map(Ip::addr)
                                                              .map(ip -> new IPv4Address(NetUtils.getIpv4AddressWithCidrSize(ip, nic.getIPv4Netmask()), nic.getIPv4Gateway()))
                                                              .collect(Collectors.toList()));

                            serviceSourceNatsTO.addAll(_ipAddressDao.listByAssociatedVpc(router.getVpcId(), true)
                                                                    .stream()
                                                                    .map(IPAddressVO::getAddress)
                                                                    .filter(ip -> !ipsToExclude.contains(ip))
                                                                    .map(Ip::addr)
                                                                    .map(ip -> new ServiceSourceNatTO(ip, nic.getIPv4Gateway()))
                                                                    .collect(Collectors.toList()));
                        } else {
                            ipv4Addresses.addAll(_ipAddressDao.listByAssociatedNetwork(network.getId(), false)
                                                              .stream()
                                                              .map(IPAddressVO::getAddress)
                                                              .filter(ip -> !ipsToExclude.contains(ip))
                                                              .map(Ip::addr)
                                                              .map(ip -> new IPv4Address(NetUtils.getIpv4AddressWithCidrSize(ip, nic.getIPv4Netmask()), nic.getIPv4Gateway()))
                                                              .collect(Collectors.toList()));
                        }
                    }

                    interfaceTO.setMetadata(new MetadataTO(network));
                }

                interfaceTO.setIpv4Addresses(ipv4Addresses.toArray(new IPv4Address[ipv4Addresses.size()]));
                interfacesTO.add(interfaceTO);
            });

        networkOverviewTO.setInterfaces(interfacesTO.toArray(new InterfaceTO[interfacesTO.size()]));

        servicesTO.setSourceNat(serviceSourceNatsTO.toArray(new ServiceSourceNatTO[serviceSourceNatsTO.size()]));
        networkOverviewTO.setServices(servicesTO);

        return networkOverviewTO;
    }
}
