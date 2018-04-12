package com.cloud.network.firewall;

import com.cloud.configuration.Config;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.network.IpAddress;
import com.cloud.network.IpAddressManager;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkModel;
import com.cloud.network.NetworkRuleApplier;
import com.cloud.network.dao.FirewallRulesCidrsDao;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.element.FirewallServiceProvider;
import com.cloud.network.element.NetworkACLServiceProvider;
import com.cloud.network.element.PortForwardingServiceProvider;
import com.cloud.network.element.StaticNatServiceProvider;
import com.cloud.network.rules.FirewallManager;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRule.Purpose;
import com.cloud.network.rules.FirewallRule.State;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.network.rules.dao.PortForwardingRulesDao;
import com.cloud.network.vpc.VpcManager;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.dao.UserVmDao;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FirewallManagerImpl extends ManagerBase implements FirewallService, FirewallManager, NetworkRuleApplier {
    private static final Logger s_logger = LoggerFactory.getLogger(FirewallManagerImpl.class);

    @Inject
    FirewallRulesDao _firewallDao;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    FirewallRulesCidrsDao _firewallCidrsDao;
    @Inject
    AccountManager _accountMgr;
    @Inject
    NetworkModel _networkModel;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    PortForwardingRulesDao _pfRulesDao;
    @Inject
    UserVmDao _vmDao;
    @Inject
    ResourceTagDao _resourceTagDao;
    @Inject
    NetworkDao _networkDao;
    @Inject
    VpcManager _vpcMgr;
    List<FirewallServiceProvider> _firewallElements;

    List<PortForwardingServiceProvider> _pfElements;

    List<StaticNatServiceProvider> _staticNatElements;

    List<NetworkACLServiceProvider> _networkAclElements;
    @Inject
    IpAddressManager _ipAddrMgr;

    private boolean _elbEnabled = false;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;
        final String elbEnabledString = _configDao.getValue(Config.ElasticLoadBalancerEnabled.key());
        _elbEnabled = Boolean.parseBoolean(elbEnabledString);
        return true;
    }

    @Override
    public boolean start() {
        s_logger.info("Firewall provider list is " + _firewallElements.iterator().next());
        return super.start();
    }

    @Override
    public void detectRulesConflict(final FirewallRule newRule) throws NetworkRuleConflictException {
        final List<FirewallRuleVO> rules;
        if (newRule.getSourceIpAddressId() != null) {
            rules = _firewallDao.listByIpAndPurposeAndNotRevoked(newRule.getSourceIpAddressId(), null);
            assert (rules.size() >= 1) : "For network rules, we now always first persist the rule and then check for "
                    + "network conflicts so we should at least have one rule at this point.";
        } else {
            // fetches only firewall egress rules.
            rules = _firewallDao.listByNetworkPurposeTrafficTypeAndNotRevoked(newRule.getNetworkId(), Purpose.Firewall, newRule.getTrafficType());
            assert (rules.size() >= 1);
        }

        for (final FirewallRuleVO rule : rules) {
            if (rule.getId() == newRule.getId()) {
                continue; // Skips my own rule.
            }

            final boolean oneOfRulesIsFirewall =
                    ((rule.getPurpose() == Purpose.Firewall || newRule.getPurpose() == Purpose.Firewall) && ((newRule.getPurpose() != rule.getPurpose()) || (!newRule.getProtocol()
                                                                                                                                                                     .equalsIgnoreCase(rule
                                                                                                                                                                             .getProtocol()))));

            // if both rules are firewall and their cidrs are different, we can skip port ranges verification
            final boolean bothRulesFirewall = (rule.getPurpose() == newRule.getPurpose() && rule.getPurpose() == Purpose.Firewall);
            boolean duplicatedCidrs = false;
            if (bothRulesFirewall) {
                // Verify that the rules have different cidrs
                _firewallDao.loadSourceCidrs(rule);
                _firewallDao.loadSourceCidrs((FirewallRuleVO) newRule);

                final List<String> ruleCidrList = rule.getSourceCidrList();
                final List<String> newRuleCidrList = newRule.getSourceCidrList();

                if (ruleCidrList == null || newRuleCidrList == null) {
                    continue;
                }

                final Collection<String> similar = new HashSet<>(ruleCidrList);
                similar.retainAll(newRuleCidrList);

                if (similar.size() > 0) {
                    duplicatedCidrs = true;
                }
            }

            if (!oneOfRulesIsFirewall) {
                if (rule.getPurpose() == Purpose.StaticNat && newRule.getPurpose() != Purpose.StaticNat) {
                    throw new NetworkRuleConflictException("There is 1 to 1 Nat rule specified for the ip address id=" + newRule.getSourceIpAddressId());
                } else if (rule.getPurpose() != Purpose.StaticNat && newRule.getPurpose() == Purpose.StaticNat) {
                    throw new NetworkRuleConflictException("There is already firewall rule specified for the ip address id=" + newRule.getSourceIpAddressId());
                }
            }

            if (rule.getNetworkId() != newRule.getNetworkId() && rule.getState() != State.Revoke) {
                throw new NetworkRuleConflictException("New rule is for a different network than what's specified in rule " + rule.getXid());
            }

            if (newRule.getProtocol().equalsIgnoreCase(NetUtils.ICMP_PROTO) && newRule.getProtocol().equalsIgnoreCase(rule.getProtocol())) {
                if (newRule.getIcmpCode().longValue() == rule.getIcmpCode().longValue() && newRule.getIcmpType().longValue() == rule.getIcmpType().longValue() &&
                        newRule.getProtocol().equalsIgnoreCase(rule.getProtocol()) && duplicatedCidrs) {
                    throw new InvalidParameterValueException("New rule conflicts with existing rule id=" + rule.getId());
                }
            }

            final boolean notNullPorts =
                    (newRule.getSourcePortStart() != null && rule.getSourcePortStart() != null);
            final boolean nullPorts =
                    (newRule.getSourcePortStart() == null && rule.getSourcePortStart() == null);
            if (nullPorts && duplicatedCidrs && (rule.getProtocol().equalsIgnoreCase(newRule.getProtocol())) && !newRule.getProtocol().equalsIgnoreCase(NetUtils.ICMP_PROTO)) {
                throw new NetworkRuleConflictException("There is already a firewall rule specified with protocol = " + newRule.getProtocol() + " and no ports");
            }
            if (!notNullPorts) {
                continue;
            } else if (!oneOfRulesIsFirewall && !(bothRulesFirewall && !duplicatedCidrs) && (newRule.getSourcePortStart() == rule.getSourcePortStart())) {
                // we allow port forwarding rules with the same parameters but different protocols
                final boolean allowPf =
                        (rule.getPurpose() == Purpose.PortForwarding && newRule.getPurpose() == Purpose.PortForwarding && !newRule.getProtocol().equalsIgnoreCase(rule.getProtocol()))
                                ||
                                (rule.getPurpose() == Purpose.Vpn && newRule.getPurpose() == Purpose.PortForwarding && !newRule.getProtocol().equalsIgnoreCase(rule.getProtocol()));

                final boolean allowStaticNat =
                        (rule.getPurpose() == Purpose.StaticNat
                                && newRule.getPurpose() == Purpose.StaticNat
                                && !newRule.getProtocol().equalsIgnoreCase(rule.getProtocol()));

                final boolean allowVpnPf = (rule.getPurpose() == Purpose.PortForwarding
                        && newRule.getPurpose() == Purpose.Vpn
                        && !newRule.getProtocol().equalsIgnoreCase(rule.getProtocol()));

                final boolean allowVpnLb = (rule.getPurpose() == Purpose.LoadBalancing
                        && newRule.getPurpose() == Purpose.Vpn
                        && !newRule.getProtocol().equalsIgnoreCase(rule.getProtocol()));

                if (!(allowPf || allowStaticNat || allowVpnPf || allowVpnLb)) {
                    throw new NetworkRuleConflictException("The new port specified, " + newRule.getSourcePortStart() + ", conflicts with rule " + rule.getId() + " which has " + rule
                            .getSourcePortStart());
                }
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("No network rule conflicts detected for " + newRule + " against " + (rules.size() - 1) + " existing rules");
        }
    }

    @Override
    public void validateFirewallRule(final Account caller, final IPAddressVO ipAddress, final Integer portStart, final String proto, final Purpose purpose, Long networkId, final FirewallRule
            .TrafficType trafficType) {
        if (portStart != null && !NetUtils.isValidPort(portStart)) {
            throw new InvalidParameterValueException("publicPort is an invalid value: " + portStart);
        }

        if (ipAddress != null) {
            if (ipAddress.getAssociatedWithNetworkId() == null) {
                throw new InvalidParameterValueException("Unable to create firewall rule ; ip with specified id is not associated with any network");
            } else {
                networkId = ipAddress.getAssociatedWithNetworkId();
            }

            // Validate ip address
            _accountMgr.checkAccess(caller, null, true, ipAddress);
        }

        //network id either has to be passed explicitly, or implicitly as a part of ipAddress object
        if (networkId == null) {
            throw new InvalidParameterValueException("Unable to retrieve network id to validate the rule");
        }

        final Network network = _networkModel.getNetwork(networkId);
        assert network != null : "Can't create rule as network associated with public ip address is null?";

        if (trafficType == FirewallRule.TrafficType.Egress) {
            _accountMgr.checkAccess(caller, null, true, network);
        }

        // Verify that the network guru supports the protocol specified
        Map<Network.Capability, String> caps = null;

        if (purpose == Purpose.LoadBalancing) {
            if (!_elbEnabled) {
                caps = _networkModel.getNetworkServiceCapabilities(network.getId(), Service.Lb);
            }
        } else if (purpose == Purpose.PortForwarding) {
            caps = _networkModel.getNetworkServiceCapabilities(network.getId(), Service.PortForwarding);
        } else if (purpose == Purpose.Firewall) {
            caps = _networkModel.getNetworkServiceCapabilities(network.getId(), Service.Firewall);
        }

        if (caps != null) {
            final String supportedProtocols;
            String supportedTrafficTypes = null;
            if (purpose == FirewallRule.Purpose.Firewall) {
                supportedTrafficTypes = caps.get(Capability.SupportedTrafficDirection).toLowerCase();
            }

            if (purpose == FirewallRule.Purpose.Firewall && trafficType == FirewallRule.TrafficType.Egress) {
                supportedProtocols = caps.get(Capability.SupportedEgressProtocols).toLowerCase();
            } else {
                supportedProtocols = caps.get(Capability.SupportedProtocols).toLowerCase();
            }

            if (!supportedProtocols.contains(proto.toLowerCase())) {
                throw new InvalidParameterValueException("Protocol " + proto + " is not supported in zone " + network.getDataCenterId());
            } else if (proto.equalsIgnoreCase(NetUtils.ICMP_PROTO) && purpose != Purpose.Firewall) {
                throw new InvalidParameterValueException("Protocol " + proto + " is currently supported only for rules with purpose " + Purpose.Firewall);
            } else if (purpose == Purpose.Firewall && !supportedTrafficTypes.contains(trafficType.toString().toLowerCase())) {
                throw new InvalidParameterValueException("Traffic Type " + trafficType + " is currently supported by Firewall in network " + networkId);
            }
        }
    }

    @Override
    public boolean applyRules(final List<? extends FirewallRule> rules, final boolean continueOnError, final boolean updateRulesInDB) throws ResourceUnavailableException {
        boolean success = true;
        if (rules == null || rules.size() == 0) {
            s_logger.debug("There are no rules to forward to the network elements");
            return true;
        }
        final Purpose purpose = rules.get(0).getPurpose();
        if (!_ipAddrMgr.applyRules(rules, purpose, this, continueOnError)) {
            s_logger.warn("Rules are not completely applied");
            return false;
        } else {
            if (updateRulesInDB) {
                for (final FirewallRule rule : rules) {
                    if (rule.getState() == FirewallRule.State.Revoke) {
                        final FirewallRuleVO relatedRule = _firewallDao.findByRelatedId(rule.getId());
                        if (relatedRule != null) {
                            s_logger.warn("Can't remove the firewall rule id=" + rule.getId() + " as it has related firewall rule id=" + relatedRule.getId() +
                                    "; leaving it in Revoke state");
                            success = false;
                        } else {
                            removeRule(rule);
                            if (rule.getSourceIpAddressId() != null) {
                                //if the rule is the last one for the ip address assigned to VPC, unassign it from the network
                                final IpAddress ip = _ipAddressDao.findById(rule.getSourceIpAddressId());
                                _vpcMgr.unassignIPFromVpcNetwork(ip.getId(), rule.getNetworkId());
                            }
                        }
                    } else if (rule.getState() == FirewallRule.State.Add) {
                        final FirewallRuleVO ruleVO = _firewallDao.findById(rule.getId());
                        ruleVO.setState(FirewallRule.State.Active);
                        _firewallDao.update(ruleVO.getId(), ruleVO);
                    }
                }
            }
        }

        return success;
    }

    @Override
    @DB
    public void revokeRule(final FirewallRuleVO rule, final Account caller, final long userId, final boolean needUsageEvent) {
        if (caller != null) {
            _accountMgr.checkAccess(caller, null, true, rule);
        }

        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {

                if (rule.getState() == State.Staged) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Found a rule that is still in stage state so just removing it: " + rule);
                    }
                    removeRule(rule);
                } else if (rule.getState() == State.Add || rule.getState() == State.Active) {
                    rule.setState(State.Revoke);
                    _firewallDao.update(rule.getId(), rule);
                }
            }
        });
    }

    @Override
    public void removeRule(final FirewallRule rule) {

        //remove the rule
        _firewallDao.remove(rule.getId());
    }

    @Override
    public boolean applyRules(final Network network, final Purpose purpose, final List<? extends FirewallRule> rules) throws ResourceUnavailableException {
        boolean handled = false;
        switch (purpose) {
            /* StaticNatRule would be applied by Firewall provider, since the incompatible of two object */
            case StaticNat:
            case PortForwarding:
                for (final PortForwardingServiceProvider element : _pfElements) {
                    final Network.Provider provider = element.getProvider();
                    final boolean isPfProvider = _networkModel.isProviderSupportServiceInNetwork(network.getId(), Service.PortForwarding, provider);
                    if (!isPfProvider) {
                        continue;
                    }
                    handled = element.applyPFRules(network, (List<PortForwardingRule>) rules);
                    if (handled) {
                        break;
                    }
                }
                break;
            default:
                assert (false) : "Unexpected fall through in applying rules to the network elements";
                s_logger.error("FirewallManager cannot process rules of type " + purpose);
                throw new CloudRuntimeException("FirewallManager cannot process rules of type " + purpose);
        }
        return handled;
    }

    @Inject
    public void setFirewallElements(final List<FirewallServiceProvider> firewallElements) {
        _firewallElements = firewallElements;
    }

    @Inject
    public void setPfElements(final List<PortForwardingServiceProvider> pfElements) {
        _pfElements = pfElements;
    }

    @Inject
    public void setStaticNatElements(final List<StaticNatServiceProvider> staticNatElements) {
        _staticNatElements = staticNatElements;
    }

    @Inject
    public void setNetworkAclElements(final List<NetworkACLServiceProvider> networkAclElements) {
        _networkAclElements = networkAclElements;
    }
}
