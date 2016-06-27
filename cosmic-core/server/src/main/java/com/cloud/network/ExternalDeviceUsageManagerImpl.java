package com.cloud.network;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.ExternalNetworkResourceUsageAnswer;
import com.cloud.agent.api.ExternalNetworkResourceUsageCommand;
import com.cloud.configuration.Config;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.dao.ExternalFirewallDeviceDao;
import com.cloud.network.dao.ExternalFirewallDeviceVO;
import com.cloud.network.dao.ExternalLoadBalancerDeviceDao;
import com.cloud.network.dao.ExternalLoadBalancerDeviceVO;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.InlineLoadBalancerNicMapDao;
import com.cloud.network.dao.InlineLoadBalancerNicMapVO;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.LoadBalancerVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkExternalFirewallDao;
import com.cloud.network.dao.NetworkExternalFirewallVO;
import com.cloud.network.dao.NetworkExternalLoadBalancerDao;
import com.cloud.network.dao.NetworkExternalLoadBalancerVO;
import com.cloud.network.dao.NetworkServiceMapDao;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderDao;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.network.rules.PortForwardingRuleVO;
import com.cloud.network.rules.dao.PortForwardingRulesDao;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.resource.ResourceManager;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.UserStatisticsVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserStatisticsDao;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.NicVO;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicDao;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExternalDeviceUsageManagerImpl extends ManagerBase implements ExternalDeviceUsageManager {

    private static final Logger s_logger = LoggerFactory.getLogger(ExternalDeviceUsageManagerImpl.class);
    @Inject
    protected HostPodDao _podDao = null;
    String _name;
    @Inject
    NetworkExternalLoadBalancerDao _networkExternalLBDao;
    @Inject
    ExternalLoadBalancerDeviceDao _externalLoadBalancerDeviceDao;
    @Inject
    HostDao _hostDao;
    @Inject
    DataCenterDao _dcDao;
    @Inject
    InlineLoadBalancerNicMapDao _inlineLoadBalancerNicMapDao;
    @Inject
    NicDao _nicDao;
    @Inject
    AgentManager _agentMgr;
    @Inject
    ResourceManager _resourceMgr;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    VlanDao _vlanDao;
    @Inject
    NetworkOfferingDao _networkOfferingDao;
    @Inject
    AccountDao _accountDao;
    @Inject
    PhysicalNetworkDao _physicalNetworkDao;
    @Inject
    PhysicalNetworkServiceProviderDao _physicalNetworkServiceProviderDao;
    @Inject
    AccountManager _accountMgr;
    @Inject
    UserStatisticsDao _userStatsDao;
    @Inject
    NetworkDao _networkDao;
    @Inject
    DomainRouterDao _routerDao;
    @Inject
    LoadBalancerDao _loadBalancerDao;
    @Inject
    PortForwardingRulesDao _portForwardingRulesDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    HostDetailsDao _hostDetailDao;
    @Inject
    NetworkExternalLoadBalancerDao _networkLBDao;
    @Inject
    NetworkServiceMapDao _ntwkSrvcProviderDao;
    @Inject
    NetworkExternalFirewallDao _networkExternalFirewallDao;
    @Inject
    ExternalFirewallDeviceDao _externalFirewallDeviceDao;
    @Inject
    NetworkModel _networkModel;
    ScheduledExecutorService _executor;
    private int _externalNetworkStatsInterval;

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _externalNetworkStatsInterval = NumbersUtil.parseInt(_configDao.getValue(Config.ExternalNetworkStatsInterval.key()), 300);
        if (_externalNetworkStatsInterval > 0) {
            _executor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("ExternalNetworkMonitor"));
        }
        return true;
    }

    @Override
    public boolean start() {
        if (_externalNetworkStatsInterval > 0) {
            _executor.scheduleAtFixedRate(new ExternalDeviceNetworkUsageTask(), _externalNetworkStatsInterval, _externalNetworkStatsInterval, TimeUnit.SECONDS);
        }
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    private ExternalFirewallDeviceVO getExternalFirewallForNetwork(final Network network) {
        final NetworkExternalFirewallVO fwDeviceForNetwork = _networkExternalFirewallDao.findByNetworkId(network.getId());
        if (fwDeviceForNetwork != null) {
            final long fwDeviceId = fwDeviceForNetwork.getExternalFirewallDeviceId();
            final ExternalFirewallDeviceVO fwDevice = _externalFirewallDeviceDao.findById(fwDeviceId);
            assert (fwDevice != null);
            return fwDevice;
        }
        return null;
    }

    @Override
    public void updateExternalLoadBalancerNetworkUsageStats(final long loadBalancerRuleId) {

        final LoadBalancerVO lb = _loadBalancerDao.findById(loadBalancerRuleId);
        if (lb == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Cannot update usage stats, LB rule is not found");
            }
            return;
        }
        final long networkId = lb.getNetworkId();
        final Network network = _networkDao.findById(networkId);
        if (network == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Cannot update usage stats, Network is not found");
            }
            return;
        }

        final ExternalLoadBalancerDeviceVO lbDeviceVO = getExternalLoadBalancerForNetwork(network);
        if (lbDeviceVO == null) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Cannot update usage stats,  No external LB device found");
            }
            return;
        }

        // Get network stats from the external load balancer
        ExternalNetworkResourceUsageAnswer lbAnswer = null;
        final HostVO externalLoadBalancer = _hostDao.findById(lbDeviceVO.getHostId());
        if (externalLoadBalancer != null) {
            final ExternalNetworkResourceUsageCommand cmd = new ExternalNetworkResourceUsageCommand();
            lbAnswer = (ExternalNetworkResourceUsageAnswer) _agentMgr.easySend(externalLoadBalancer.getId(), cmd);
            if (lbAnswer == null || !lbAnswer.getResult()) {
                final String details = (lbAnswer != null) ? lbAnswer.getDetails() : "details unavailable";
                final String msg = "Unable to get external load balancer stats for network" + networkId + " due to: " + details + ".";
                s_logger.error(msg);
                return;
            }
        }

        final long accountId = lb.getAccountId();
        final AccountVO account = _accountDao.findById(accountId);
        if (account == null) {
            s_logger.debug("Skipping stats update for external LB for account with ID " + accountId);
            return;
        }

        final String publicIp = _networkModel.getIp(lb.getSourceIpAddressId()).getAddress().addr();
        final DataCenterVO zone = _dcDao.findById(network.getDataCenterId());
        String statsEntryIdentifier =
                "account " + account.getAccountName() + ", zone " + zone.getName() + ", network ID " + networkId + ", host ID " + externalLoadBalancer.getName();

        long newCurrentBytesSent = 0;
        long newCurrentBytesReceived = 0;

        if (publicIp != null) {
            long[] bytesSentAndReceived = null;
            statsEntryIdentifier += ", public IP: " + publicIp;
            final boolean inline = _networkModel.isNetworkInlineMode(network);
            if (externalLoadBalancer.getType().equals(Host.Type.ExternalLoadBalancer) && inline) {
                // Look up stats for the guest IP address that's mapped to the public IP address
                final InlineLoadBalancerNicMapVO mapping = _inlineLoadBalancerNicMapDao.findByPublicIpAddress(publicIp);

                if (mapping != null) {
                    final NicVO nic = _nicDao.findById(mapping.getNicId());
                    final String loadBalancingIpAddress = nic.getIPv4Address();
                    bytesSentAndReceived = lbAnswer.ipBytes.get(loadBalancingIpAddress);

                    if (bytesSentAndReceived != null) {
                        bytesSentAndReceived[0] = 0;
                    }
                }
            } else {
                bytesSentAndReceived = lbAnswer.ipBytes.get(publicIp);
            }

            if (bytesSentAndReceived == null) {
                s_logger.debug("Didn't get an external network usage answer for public IP " + publicIp);
            } else {
                newCurrentBytesSent += bytesSentAndReceived[0];
                newCurrentBytesReceived += bytesSentAndReceived[1];
            }

            commitStats(networkId, externalLoadBalancer, accountId, publicIp, zone, statsEntryIdentifier, newCurrentBytesSent, newCurrentBytesReceived);
        }
    }

    private ExternalLoadBalancerDeviceVO getExternalLoadBalancerForNetwork(final Network network) {
        final NetworkExternalLoadBalancerVO lbDeviceForNetwork = _networkExternalLBDao.findByNetworkId(network.getId());
        if (lbDeviceForNetwork != null) {
            final long lbDeviceId = lbDeviceForNetwork.getExternalLBDeviceId();
            final ExternalLoadBalancerDeviceVO lbDeviceVo = _externalLoadBalancerDeviceDao.findById(lbDeviceId);
            assert (lbDeviceVo != null);
            return lbDeviceVo;
        }
        return null;
    }

    private void commitStats(final long networkId, final HostVO externalLoadBalancer, final long accountId, final String publicIp, final DataCenterVO zone,
                             final String statsEntryIdentifier, final long newCurrentBytesSent, final long newCurrentBytesReceived) {
        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                final UserStatisticsVO userStats;
                userStats = _userStatsDao.lock(accountId, zone.getId(), networkId, publicIp, externalLoadBalancer.getId(), externalLoadBalancer.getType().toString());

                if (userStats != null) {
                    final long oldNetBytesSent = userStats.getNetBytesSent();
                    final long oldNetBytesReceived = userStats.getNetBytesReceived();
                    final long oldCurrentBytesSent = userStats.getCurrentBytesSent();
                    final long oldCurrentBytesReceived = userStats.getCurrentBytesReceived();
                    final String warning =
                            "Received an external network stats byte count that was less than the stored value. Zone ID: " + userStats.getDataCenterId() + ", account ID: " +
                                    userStats.getAccountId() + ".";

                    userStats.setCurrentBytesSent(newCurrentBytesSent);
                    if (oldCurrentBytesSent > newCurrentBytesSent) {
                        s_logger.warn(warning + "Stored bytes sent: " + oldCurrentBytesSent + ", new bytes sent: " + newCurrentBytesSent + ".");
                        userStats.setNetBytesSent(oldNetBytesSent + oldCurrentBytesSent);
                    }

                    userStats.setCurrentBytesReceived(newCurrentBytesReceived);
                    if (oldCurrentBytesReceived > newCurrentBytesReceived) {
                        s_logger.warn(warning + "Stored bytes received: " + oldCurrentBytesReceived + ", new bytes received: " + newCurrentBytesReceived + ".");
                        userStats.setNetBytesReceived(oldNetBytesReceived + oldCurrentBytesReceived);
                    }

                    if (_userStatsDao.update(userStats.getId(), userStats)) {
                        s_logger.debug("Successfully updated stats for " + statsEntryIdentifier);
                    } else {
                        s_logger.debug("Failed to update stats for " + statsEntryIdentifier);
                    }
                } else {
                    s_logger.warn("Unable to find user stats entry for " + statsEntryIdentifier);
                }
            }
        });
    }

    protected class ExternalDeviceNetworkUsageTask extends ManagedContextRunnable {

        public ExternalDeviceNetworkUsageTask() {

        }

        @Override
        protected void runInContext() {

            // Check if there are any external devices
            // Skip external device usage collection if none exist

            if (_hostDao.listByType(Host.Type.ExternalFirewall).isEmpty() && _hostDao.listByType(Host.Type.ExternalLoadBalancer).isEmpty()) {
                s_logger.debug("External devices are not used. Skipping external device usage collection");
                return;
            }

            final GlobalLock scanLock = GlobalLock.getInternLock("ExternalDeviceNetworkUsageManagerImpl");
            try {
                if (scanLock.lock(20)) {
                    try {
                        runExternalDeviceNetworkUsageTask();
                    } finally {
                        scanLock.unlock();
                    }
                }
            } catch (final Exception e) {
                s_logger.warn("Problems while getting external device usage", e);
            } finally {
                scanLock.releaseRef();
            }
        }

        protected void runExternalDeviceNetworkUsageTask() {
            s_logger.debug("External devices stats collector is running...");

            for (final DataCenterVO zone : _dcDao.listAll()) {
                final List<DomainRouterVO> domainRoutersInZone = _routerDao.listByDataCenter(zone.getId());
                if (domainRoutersInZone == null) {
                    continue;
                }
                final Map<Long, ExternalNetworkResourceUsageAnswer> lbDeviceUsageAnswerMap = new HashMap<>();
                final Map<Long, ExternalNetworkResourceUsageAnswer> fwDeviceUsageAnswerMap = new HashMap<>();
                final List<Long> accountsProcessed = new ArrayList<>();

                for (final DomainRouterVO domainRouter : domainRoutersInZone) {
                    final long accountId = domainRouter.getAccountId();

                    if (accountsProcessed.contains(new Long(accountId))) {
                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("Networks for Account " + accountId + " are already processed for external network usage, so skipping usage check.");
                        }
                        continue;
                    }

                    final long zoneId = zone.getId();

                    final List<NetworkVO> networksForAccount = _networkDao.listByZoneAndGuestType(accountId, zoneId, Network.GuestType.Isolated, false);
                    if (networksForAccount == null) {
                        continue;
                    }

                    for (final NetworkVO network : networksForAccount) {
                        if (!_networkModel.networkIsConfiguredForExternalNetworking(zoneId, network.getId())) {
                            s_logger.debug("Network " + network.getId() + " is not configured for external networking, so skipping usage check.");
                            continue;
                        }

                        final ExternalFirewallDeviceVO fwDeviceVO = getExternalFirewallForNetwork(network);
                        final ExternalLoadBalancerDeviceVO lbDeviceVO = getExternalLoadBalancerForNetwork(network);
                        if (lbDeviceVO == null && fwDeviceVO == null) {
                            continue;
                        }

                        // Get network stats from the external firewall
                        ExternalNetworkResourceUsageAnswer firewallAnswer = null;
                        HostVO externalFirewall = null;
                        if (fwDeviceVO != null) {
                            externalFirewall = _hostDao.findById(fwDeviceVO.getHostId());
                            if (externalFirewall != null) {
                                final Long fwDeviceId = new Long(externalFirewall.getId());
                                if (!fwDeviceUsageAnswerMap.containsKey(fwDeviceId)) {
                                    try {
                                        final ExternalNetworkResourceUsageCommand cmd = new ExternalNetworkResourceUsageCommand();
                                        firewallAnswer = (ExternalNetworkResourceUsageAnswer) _agentMgr.easySend(externalFirewall.getId(), cmd);
                                        if (firewallAnswer == null || !firewallAnswer.getResult()) {
                                            final String details = (firewallAnswer != null) ? firewallAnswer.getDetails() : "details unavailable";
                                            final String msg = "Unable to get external firewall stats for network" + zone.getName() + " due to: " + details + ".";
                                            s_logger.error(msg);
                                        } else {
                                            fwDeviceUsageAnswerMap.put(fwDeviceId, firewallAnswer);
                                        }
                                    } catch (final Exception e) {
                                        final String msg = "Unable to get external firewall stats for network" + zone.getName();
                                        s_logger.error(msg, e);
                                    }
                                } else {
                                    if (s_logger.isTraceEnabled()) {
                                        s_logger.trace("Reusing usage Answer for device id " + fwDeviceId + "for Network " + network.getId());
                                    }
                                    firewallAnswer = fwDeviceUsageAnswerMap.get(fwDeviceId);
                                }
                            }
                        }

                        // Get network stats from the external load balancer
                        ExternalNetworkResourceUsageAnswer lbAnswer = null;
                        HostVO externalLoadBalancer = null;
                        if (lbDeviceVO != null) {
                            externalLoadBalancer = _hostDao.findById(lbDeviceVO.getHostId());
                            if (externalLoadBalancer != null) {
                                final Long lbDeviceId = new Long(externalLoadBalancer.getId());
                                if (!lbDeviceUsageAnswerMap.containsKey(lbDeviceId)) {
                                    try {
                                        final ExternalNetworkResourceUsageCommand cmd = new ExternalNetworkResourceUsageCommand();
                                        lbAnswer = (ExternalNetworkResourceUsageAnswer) _agentMgr.easySend(externalLoadBalancer.getId(), cmd);
                                        if (lbAnswer == null || !lbAnswer.getResult()) {
                                            final String details = (lbAnswer != null) ? lbAnswer.getDetails() : "details unavailable";
                                            final String msg = "Unable to get external load balancer stats for " + zone.getName() + " due to: " + details + ".";
                                            s_logger.error(msg);
                                        } else {
                                            lbDeviceUsageAnswerMap.put(lbDeviceId, lbAnswer);
                                        }
                                    } catch (final Exception e) {
                                        final String msg = "Unable to get external load balancer stats for " + zone.getName();
                                        s_logger.error(msg, e);
                                    }
                                } else {
                                    if (s_logger.isTraceEnabled()) {
                                        s_logger.trace("Reusing usage Answer for device id " + lbDeviceId + "for Network " + network.getId());
                                    }
                                    lbAnswer = lbDeviceUsageAnswerMap.get(lbDeviceId);
                                }
                            }
                        }

                        if (firewallAnswer == null && lbAnswer == null) {
                            continue;
                        }

                        final AccountVO account = _accountDao.findById(accountId);
                        if (account == null) {
                            s_logger.debug("Skipping stats update for account with ID " + accountId);
                            continue;
                        }

                        if (!manageStatsEntries(true, accountId, zoneId, network, externalFirewall, firewallAnswer, externalLoadBalancer, lbAnswer)) {
                            continue;
                        }

                        manageStatsEntries(false, accountId, zoneId, network, externalFirewall, firewallAnswer, externalLoadBalancer, lbAnswer);
                    }

                    accountsProcessed.add(new Long(accountId));
                }
            }
        }

        /*
         * Creates/updates all necessary stats entries for an account and zone.
         * Stats entries are created for source NAT IP addresses, static NAT rules, port forwarding rules, and load
         * balancing rules
         */
        private boolean manageStatsEntries(final boolean create, final long accountId, final long zoneId, final Network network, final HostVO externalFirewall,
                                           final ExternalNetworkResourceUsageAnswer firewallAnswer, final HostVO externalLoadBalancer, final ExternalNetworkResourceUsageAnswer
                                                   lbAnswer) {
            final String accountErrorMsg = "Failed to update external network stats entry. Details: account ID = " + accountId;
            try {
                Transaction.execute(new TransactionCallbackNoReturn() {
                    @Override
                    public void doInTransactionWithoutResult(final TransactionStatus status) {
                        final String networkErrorMsg = accountErrorMsg + ", network ID = " + network.getId();

                        boolean sharedSourceNat = false;
                        final Map<Network.Capability, String> sourceNatCapabilities = _networkModel.getNetworkServiceCapabilities(network.getId(), Network.Service.SourceNat);
                        if (sourceNatCapabilities != null) {
                            final String supportedSourceNatTypes = sourceNatCapabilities.get(Network.Capability.SupportedSourceNatTypes).toLowerCase();
                            if (supportedSourceNatTypes.contains("zone")) {
                                sharedSourceNat = true;
                            }
                        }

                        if (externalFirewall != null && firewallAnswer != null) {
                            if (!sharedSourceNat) {
                                // Manage the entry for this network's source NAT IP address
                                final List<IPAddressVO> sourceNatIps = _ipAddressDao.listByAssociatedNetwork(network.getId(), true);
                                if (sourceNatIps.size() == 1) {
                                    final String publicIp = sourceNatIps.get(0).getAddress().addr();
                                    if (!createOrUpdateStatsEntry(create, accountId, zoneId, network.getId(), publicIp, externalFirewall.getId(), firewallAnswer, false)) {
                                        throw new CloudRuntimeException(networkErrorMsg + ", source NAT IP = " + publicIp);
                                    }
                                }

                                // Manage one entry for each static NAT rule in this network
                                final List<IPAddressVO> staticNatIps = _ipAddressDao.listStaticNatPublicIps(network.getId());
                                for (final IPAddressVO staticNatIp : staticNatIps) {
                                    final String publicIp = staticNatIp.getAddress().addr();
                                    if (!createOrUpdateStatsEntry(create, accountId, zoneId, network.getId(), publicIp, externalFirewall.getId(), firewallAnswer, false)) {
                                        throw new CloudRuntimeException(networkErrorMsg + ", static NAT rule public IP = " + publicIp);
                                    }
                                }

                                // Manage one entry for each port forwarding rule in this network
                                final List<PortForwardingRuleVO> portForwardingRules = _portForwardingRulesDao.listByNetwork(network.getId());
                                for (final PortForwardingRuleVO portForwardingRule : portForwardingRules) {
                                    final String publicIp = _networkModel.getIp(portForwardingRule.getSourceIpAddressId()).getAddress().addr();
                                    if (!createOrUpdateStatsEntry(create, accountId, zoneId, network.getId(), publicIp, externalFirewall.getId(), firewallAnswer, false)) {
                                        throw new CloudRuntimeException(networkErrorMsg + ", port forwarding rule public IP = " + publicIp);
                                    }
                                }
                            } else {
                                // Manage the account-wide entry for the external firewall
                                if (!createOrUpdateStatsEntry(create, accountId, zoneId, network.getId(), null, externalFirewall.getId(), firewallAnswer, false)) {
                                    throw new CloudRuntimeException(networkErrorMsg);
                                }
                            }
                        }

                        // If an external load balancer is added, manage one entry for each load balancing rule in this network
                        if (externalLoadBalancer != null && lbAnswer != null) {
                            final boolean inline = _networkModel.isNetworkInlineMode(network);
                            final List<LoadBalancerVO> loadBalancers = _loadBalancerDao.listByNetworkIdAndScheme(network.getId(), Scheme.Public);
                            for (final LoadBalancerVO loadBalancer : loadBalancers) {
                                final String publicIp = _networkModel.getIp(loadBalancer.getSourceIpAddressId()).getAddress().addr();
                                if (!createOrUpdateStatsEntry(create, accountId, zoneId, network.getId(), publicIp, externalLoadBalancer.getId(), lbAnswer, inline)) {
                                    throw new CloudRuntimeException(networkErrorMsg + ", load balancing rule public IP = " + publicIp);
                                }
                            }
                        }
                    }
                });
                return true;
            } catch (final Exception e) {
                s_logger.warn("Exception: ", e);
                return false;
            }
        }

        private boolean createOrUpdateStatsEntry(final boolean create, final long accountId, final long zoneId, final long networkId, final String publicIp, final long hostId,
                                                 final ExternalNetworkResourceUsageAnswer answer, final boolean inline) {
            if (create) {
                return createStatsEntry(accountId, zoneId, networkId, publicIp, hostId);
            } else {
                return updateStatsEntry(accountId, zoneId, networkId, publicIp, hostId, answer, inline);
            }
        }

        // Creates a new stats entry for the specified parameters, if one doesn't already exist.
        private boolean createStatsEntry(final long accountId, final long zoneId, final long networkId, final String publicIp, final long hostId) {
            final HostVO host = _hostDao.findById(hostId);
            final UserStatisticsVO userStats = _userStatsDao.findBy(accountId, zoneId, networkId, publicIp, hostId, host.getType().toString());
            if (userStats == null) {
                return (_userStatsDao.persist(new UserStatisticsVO(accountId, zoneId, publicIp, hostId, host.getType().toString(), networkId)) != null);
            } else {
                return true;
            }
        }

        // Updates an existing stats entry with new data from the specified usage answer.
        private boolean updateStatsEntry(final long accountId, final long zoneId, final long networkId, final String publicIp, final long hostId, final
        ExternalNetworkResourceUsageAnswer answer,
                                         final boolean inline) {
            final AccountVO account = _accountDao.findById(accountId);
            final DataCenterVO zone = _dcDao.findById(zoneId);
            final NetworkVO network = _networkDao.findById(networkId);
            final HostVO host = _hostDao.findById(hostId);
            String statsEntryIdentifier =
                    "account " + account.getAccountName() + ", zone " + zone.getName() + ", network ID " + networkId + ", host ID " + host.getName();

            long newCurrentBytesSent = 0;
            long newCurrentBytesReceived = 0;

            if (publicIp != null) {
                long[] bytesSentAndReceived = null;
                statsEntryIdentifier += ", public IP: " + publicIp;

                if (host.getType().equals(Host.Type.ExternalLoadBalancer) && inline) {
                    // Look up stats for the guest IP address that's mapped to the public IP address
                    final InlineLoadBalancerNicMapVO mapping = _inlineLoadBalancerNicMapDao.findByPublicIpAddress(publicIp);

                    if (mapping != null) {
                        final NicVO nic = _nicDao.findById(mapping.getNicId());
                        final String loadBalancingIpAddress = nic.getIPv4Address();
                        bytesSentAndReceived = answer.ipBytes.get(loadBalancingIpAddress);

                        if (bytesSentAndReceived != null) {
                            bytesSentAndReceived[0] = 0;
                        }
                    }
                } else {
                    bytesSentAndReceived = answer.ipBytes.get(publicIp);
                }

                if (bytesSentAndReceived == null) {
                    s_logger.debug("Didn't get an external network usage answer for public IP " + publicIp);
                } else {
                    newCurrentBytesSent += bytesSentAndReceived[0];
                    newCurrentBytesReceived += bytesSentAndReceived[1];
                }
            } else {
                final URI broadcastURI = network.getBroadcastUri();
                if (broadcastURI == null) {
                    s_logger.debug("Not updating stats for guest network with ID " + network.getId() + " because the network is not implemented.");
                    return true;
                } else {
                    final long vlanTag = Integer.parseInt(BroadcastDomainType.getValue(broadcastURI));
                    final long[] bytesSentAndReceived = answer.guestVlanBytes.get(String.valueOf(vlanTag));

                    if (bytesSentAndReceived == null) {
                        s_logger.warn("Didn't get an external network usage answer for guest VLAN " + vlanTag);
                    } else {
                        newCurrentBytesSent += bytesSentAndReceived[0];
                        newCurrentBytesReceived += bytesSentAndReceived[1];
                    }
                }
            }

            final UserStatisticsVO userStats;
            try {
                userStats = _userStatsDao.lock(accountId, zoneId, networkId, publicIp, hostId, host.getType().toString());
            } catch (final Exception e) {
                s_logger.warn("Unable to find user stats entry for " + statsEntryIdentifier);
                return false;
            }

            if (updateBytes(userStats, newCurrentBytesSent, newCurrentBytesReceived)) {
                s_logger.debug("Successfully updated stats for " + statsEntryIdentifier);
                return true;
            } else {
                s_logger.debug("Failed to update stats for " + statsEntryIdentifier);
                return false;
            }
        }

        private boolean updateBytes(final UserStatisticsVO userStats, final long newCurrentBytesSent, final long newCurrentBytesReceived) {
            final long oldNetBytesSent = userStats.getNetBytesSent();
            final long oldNetBytesReceived = userStats.getNetBytesReceived();
            final long oldCurrentBytesSent = userStats.getCurrentBytesSent();
            final long oldCurrentBytesReceived = userStats.getCurrentBytesReceived();
            final String warning =
                    "Received an external network stats byte count that was less than the stored value. Zone ID: " + userStats.getDataCenterId() + ", account ID: " +
                            userStats.getAccountId() + ".";

            userStats.setCurrentBytesSent(newCurrentBytesSent);
            if (oldCurrentBytesSent > newCurrentBytesSent) {
                s_logger.warn(warning + "Stored bytes sent: " + oldCurrentBytesSent + ", new bytes sent: " + newCurrentBytesSent + ".");
                userStats.setNetBytesSent(oldNetBytesSent + oldCurrentBytesSent);
            }

            userStats.setCurrentBytesReceived(newCurrentBytesReceived);
            if (oldCurrentBytesReceived > newCurrentBytesReceived) {
                s_logger.warn(warning + "Stored bytes received: " + oldCurrentBytesReceived + ", new bytes received: " + newCurrentBytesReceived + ".");
                userStats.setNetBytesReceived(oldNetBytesReceived + oldCurrentBytesReceived);
            }

            return _userStatsDao.update(userStats.getId(), userStats);
        }
    }
}
