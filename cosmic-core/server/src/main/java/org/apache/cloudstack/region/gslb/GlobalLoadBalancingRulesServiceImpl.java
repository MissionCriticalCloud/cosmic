package org.apache.cloudstack.region.gslb;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.routing.GlobalLoadBalancerConfigCommand;
import com.cloud.agent.api.routing.SiteLoadBalancerConfig;
import com.cloud.configuration.Config;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventUtils;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.LoadBalancerVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.network.rules.RulesManager;
import com.cloud.region.ha.GlobalLoadBalancerRule;
import com.cloud.region.ha.GlobalLoadBalancingRulesService;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.utils.Pair;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.NetUtils;
import org.apache.cloudstack.acl.SecurityChecker;
import org.apache.cloudstack.api.command.user.region.ha.gslb.AssignToGlobalLoadBalancerRuleCmd;
import org.apache.cloudstack.api.command.user.region.ha.gslb.CreateGlobalLoadBalancerRuleCmd;
import org.apache.cloudstack.api.command.user.region.ha.gslb.DeleteGlobalLoadBalancerRuleCmd;
import org.apache.cloudstack.api.command.user.region.ha.gslb.ListGlobalLoadBalancerRuleCmd;
import org.apache.cloudstack.api.command.user.region.ha.gslb.RemoveFromGlobalLoadBalancerRuleCmd;
import org.apache.cloudstack.api.command.user.region.ha.gslb.UpdateGlobalLoadBalancerRuleCmd;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.region.Region;
import org.apache.cloudstack.region.dao.RegionDao;

import javax.ejb.Local;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Local(value = {GlobalLoadBalancingRulesService.class})
public class GlobalLoadBalancingRulesServiceImpl implements GlobalLoadBalancingRulesService {

    private static final Logger s_logger = LoggerFactory.getLogger(GlobalLoadBalancingRulesServiceImpl.class);
    protected List<GslbServiceProvider> _gslbProviders;
    @Inject
    AccountManager _accountMgr;
    @Inject
    GlobalLoadBalancerRuleDao _gslbRuleDao;
    @Inject
    GlobalLoadBalancerLbRuleMapDao _gslbLbMapDao;
    @Inject
    RegionDao _regionDao;
    @Inject
    RulesManager _rulesMgr;
    @Inject
    LoadBalancerDao _lbDao;
    @Inject
    NetworkDao _networkDao;
    @Inject
    ConfigurationDao _globalConfigDao;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    AgentManager _agentMgr;

    public void setGslbServiceProviders(final List<GslbServiceProvider> providers) {
        _gslbProviders = providers;
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_GLOBAL_LOAD_BALANCER_CREATE, eventDescription = "creating global load " + "balancer rule", create = true)
    public GlobalLoadBalancerRule createGlobalLoadBalancerRule(final CreateGlobalLoadBalancerRuleCmd newRule) {

        final Integer regionId = newRule.getRegionId();
        final String algorithm = newRule.getAlgorithm();
        final String stickyMethod = newRule.getStickyMethod();
        final String name = newRule.getName();
        final String description = newRule.getDescription();
        final String domainName = newRule.getServiceDomainName();
        final String serviceType = newRule.getServiceType();

        final Account gslbOwner = _accountMgr.getAccount(newRule.getEntityOwnerId());

        if (!GlobalLoadBalancerRule.Algorithm.isValidAlgorithm(algorithm)) {
            throw new InvalidParameterValueException("Invalid Algorithm: " + algorithm);
        }

        if (!GlobalLoadBalancerRule.Persistence.isValidPersistence(stickyMethod)) {
            throw new InvalidParameterValueException("Invalid persistence: " + stickyMethod);
        }

        if (!GlobalLoadBalancerRule.ServiceType.isValidServiceType(serviceType)) {
            throw new InvalidParameterValueException("Invalid service type: " + serviceType);
        }

        if (!NetUtils.verifyDomainName(domainName)) {
            throw new InvalidParameterValueException("Invalid domain name : " + domainName);
        }

        final GlobalLoadBalancerRuleVO gslbRuleWithDomainName = _gslbRuleDao.findByDomainName(domainName);
        if (gslbRuleWithDomainName != null) {
            throw new InvalidParameterValueException("Domain name " + domainName + "is in use");
        }

        final Region region = _regionDao.findById(regionId);
        if (region == null) {
            throw new InvalidParameterValueException("Invalid region ID: " + regionId);
        }

        final String providerDnsName = _globalConfigDao.getValue(Config.CloudDnsName.key());
        if (!region.checkIfServiceEnabled(Region.Service.Gslb) || (providerDnsName == null)) {
            throw new CloudRuntimeException("GSLB service is not enabled in region : " + region.getName());
        }

        final GlobalLoadBalancerRuleVO newGslbRule = Transaction.execute(new TransactionCallback<GlobalLoadBalancerRuleVO>() {
            @Override
            public GlobalLoadBalancerRuleVO doInTransaction(final TransactionStatus status) {
                final GlobalLoadBalancerRuleVO newGslbRule =
                        new GlobalLoadBalancerRuleVO(name, description, domainName, algorithm, stickyMethod, serviceType, regionId, gslbOwner.getId(),
                                gslbOwner.getDomainId(), GlobalLoadBalancerRule.State.Staged);
                _gslbRuleDao.persist(newGslbRule);

                UsageEventUtils.publishUsageEvent(EventTypes.EVENT_GLOBAL_LOAD_BALANCER_CREATE, newGslbRule.getAccountId(), 0, newGslbRule.getId(), name,
                        GlobalLoadBalancerRule.class.getName(), newGslbRule.getUuid());

                return newGslbRule;
            }
        });

        s_logger.debug("successfully created new global load balancer rule for the account " + gslbOwner.getId());

        return newGslbRule;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_GLOBAL_LOAD_BALANCER_DELETE, eventDescription = "Delete global load balancer rule")
    public boolean deleteGlobalLoadBalancerRule(final DeleteGlobalLoadBalancerRuleCmd deleteGslbCmd) {

        final CallContext ctx = CallContext.current();
        final Account caller = ctx.getCallingAccount();
        final long gslbRuleId = deleteGslbCmd.getGlobalLoadBalancerId();

        try {
            revokeGslbRule(gslbRuleId, caller);
        } catch (final Exception e) {
            s_logger.warn("Failed to delete GSLB rule due to" + e.getMessage());
            return false;
        }

        return true;
    }

    @DB
    private void revokeGslbRule(final long gslbRuleId, final Account caller) {

        final GlobalLoadBalancerRuleVO gslbRule = _gslbRuleDao.findById(gslbRuleId);

        if (gslbRule == null) {
            throw new InvalidParameterValueException("Invalid global load balancer rule id: " + gslbRuleId);
        }

        _accountMgr.checkAccess(caller, SecurityChecker.AccessType.OperateEntry, true, gslbRule);

        if (gslbRule.getState() == com.cloud.region.ha.GlobalLoadBalancerRule.State.Staged) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Rule Id: " + gslbRuleId + " is still in Staged state so just removing it.");
            }
            _gslbRuleDao.remove(gslbRuleId);
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_GLOBAL_LOAD_BALANCER_DELETE, gslbRule.getAccountId(), 0, gslbRule.getId(), gslbRule.getName(),
                    GlobalLoadBalancerRule.class.getName(), gslbRule.getUuid());
            return;
        } else if (gslbRule.getState() == GlobalLoadBalancerRule.State.Add || gslbRule.getState() == GlobalLoadBalancerRule.State.Active) {
            //mark the GSlb rule to be in revoke state
            gslbRule.setState(GlobalLoadBalancerRule.State.Revoke);
            _gslbRuleDao.update(gslbRuleId, gslbRule);
        }

        final List<GlobalLoadBalancerLbRuleMapVO> gslbLbMapVos = Transaction.execute(new TransactionCallback<List<GlobalLoadBalancerLbRuleMapVO>>() {
            @Override
            public List<GlobalLoadBalancerLbRuleMapVO> doInTransaction(final TransactionStatus status) {
                final List<GlobalLoadBalancerLbRuleMapVO> gslbLbMapVos = _gslbLbMapDao.listByGslbRuleId(gslbRuleId);
                if (gslbLbMapVos != null) {
                    //mark all the GSLB-LB mapping to be in revoke state
                    for (final GlobalLoadBalancerLbRuleMapVO gslbLbMap : gslbLbMapVos) {
                        gslbLbMap.setRevoke(true);
                        _gslbLbMapDao.update(gslbLbMap.getId(), gslbLbMap);
                    }
                }

                return gslbLbMapVos;
            }
        });

        boolean success = false;
        try {
            if (gslbLbMapVos != null) {
                applyGlobalLoadBalancerRuleConfig(gslbRuleId, true);
            }
            success = true;
        } catch (final ResourceUnavailableException e) {
            throw new CloudRuntimeException("Failed to update the gloabal load balancer");
        }

        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                //remove all mappings between GSLB rule and load balancer rules
                if (gslbLbMapVos != null) {
                    for (final GlobalLoadBalancerLbRuleMapVO gslbLbMap : gslbLbMapVos) {
                        _gslbLbMapDao.remove(gslbLbMap.getId());
                    }
                }

                //remove the GSLB rule itself
                _gslbRuleDao.remove(gslbRuleId);

                UsageEventUtils.publishUsageEvent(EventTypes.EVENT_GLOBAL_LOAD_BALANCER_DELETE, gslbRule.getAccountId(), 0, gslbRule.getId(), gslbRule.getName(),
                        GlobalLoadBalancerRule.class.getName(), gslbRule.getUuid());
            }
        });
    }

    private boolean applyGlobalLoadBalancerRuleConfig(final long gslbRuleId, final boolean revoke) throws ResourceUnavailableException {

        final GlobalLoadBalancerRuleVO gslbRule = _gslbRuleDao.findById(gslbRuleId);
        assert (gslbRule != null);

        final String lbMethod = gslbRule.getAlgorithm();
        final String persistenceMethod = gslbRule.getPersistence();
        final String serviceType = gslbRule.getServiceType();

        // each Gslb rule will have a FQDN, formed from the domain name associated with the gslb rule
        // and the deployment DNS name configured in global config parameter 'cloud.dns.name'
        final String domainName = gslbRule.getGslbDomain();
        final String providerDnsName = _globalConfigDao.getValue(Config.CloudDnsName.key());
        final String gslbFqdn = domainName + "." + providerDnsName;

        final GlobalLoadBalancerConfigCommand gslbConfigCmd = new GlobalLoadBalancerConfigCommand(gslbFqdn, lbMethod, persistenceMethod, serviceType, gslbRuleId, revoke);

        // list of the physical network participating in global load balancing
        final List<Pair<Long, Long>> gslbSiteIds = new ArrayList<>();

        // map of the zone and info corresponding to the load balancer configured in the zone
        final Map<Long, SiteLoadBalancerConfig> zoneSiteLoadbalancerMap = new HashMap<>();

        final List<GlobalLoadBalancerLbRuleMapVO> gslbLbMapVos = _gslbLbMapDao.listByGslbRuleId(gslbRuleId);

        assert (gslbLbMapVos != null && !gslbLbMapVos.isEmpty());

        for (final GlobalLoadBalancerLbRuleMapVO gslbLbMapVo : gslbLbMapVos) {

            // get the zone in which load balancer rule is deployed
            final LoadBalancerVO loadBalancer = _lbDao.findById(gslbLbMapVo.getLoadBalancerId());
            final Network network = _networkDao.findById(loadBalancer.getNetworkId());
            final long dataCenterId = network.getDataCenterId();
            final long physicalNetworkId = network.getPhysicalNetworkId();

            gslbSiteIds.add(new Pair<>(dataCenterId, physicalNetworkId));

            final IPAddressVO ip = _ipAddressDao.findById(loadBalancer.getSourceIpAddressId());
            final SiteLoadBalancerConfig siteLb =
                    new SiteLoadBalancerConfig(gslbLbMapVo.isRevoke(), serviceType, ip.getAddress().addr(), Integer.toString(loadBalancer.getDefaultPortStart()),
                            dataCenterId);

            siteLb.setGslbProviderPublicIp(lookupGslbServiceProvider().getZoneGslbProviderPublicIp(dataCenterId, physicalNetworkId));
            siteLb.setGslbProviderPrivateIp(lookupGslbServiceProvider().getZoneGslbProviderPrivateIp(dataCenterId, physicalNetworkId));
            siteLb.setWeight(gslbLbMapVo.getWeight());

            zoneSiteLoadbalancerMap.put(network.getDataCenterId(), siteLb);
        }

        // loop through all the zones, participating in GSLB, and send GSLB config command
        // to the corresponding GSLB service provider in that zone
        for (final Pair<Long, Long> zoneId : gslbSiteIds) {

            final List<SiteLoadBalancerConfig> slbs = new ArrayList<>();
            // set site as 'local' for the site in that zone
            for (final Pair<Long, Long> innerLoopZoneId : gslbSiteIds) {
                final SiteLoadBalancerConfig siteLb = zoneSiteLoadbalancerMap.get(innerLoopZoneId.first());
                siteLb.setLocal(zoneId.first() == innerLoopZoneId.first());
                slbs.add(siteLb);
            }

            gslbConfigCmd.setSiteLoadBalancers(slbs);
            gslbConfigCmd.setForRevoke(revoke);

            // revoke GSLB configuration completely on the site GSLB provider for the sites that no longer
            // are participants of a GSLB rule
            final SiteLoadBalancerConfig siteLb = zoneSiteLoadbalancerMap.get(zoneId.first());
            if (siteLb.forRevoke()) {
                gslbConfigCmd.setForRevoke(true);
            }

            try {
                lookupGslbServiceProvider().applyGlobalLoadBalancerRule(zoneId.first(), zoneId.second(), gslbConfigCmd);
            } catch (ResourceUnavailableException | NullPointerException e) {
                final String msg = "Failed to configure GSLB rule in the zone " + zoneId.first() + " due to " + e.getMessage();
                s_logger.warn(msg);
                throw new CloudRuntimeException(msg);
            }
        }

        return true;
    }

    protected GslbServiceProvider lookupGslbServiceProvider() {
        return _gslbProviders.size() == 0 ? null : _gslbProviders.get(0);
    }

    @Override
    public GlobalLoadBalancerRule updateGlobalLoadBalancerRule(final UpdateGlobalLoadBalancerRuleCmd updateGslbCmd) {

        final String algorithm = updateGslbCmd.getAlgorithm();
        final String stickyMethod = updateGslbCmd.getStickyMethod();
        final String description = updateGslbCmd.getDescription();

        final long gslbRuleId = updateGslbCmd.getId();
        final GlobalLoadBalancerRuleVO gslbRule = _gslbRuleDao.findById(gslbRuleId);
        if (gslbRule == null) {
            throw new InvalidParameterValueException("Invalid global load balancer rule id: " + gslbRuleId);
        }

        final CallContext ctx = CallContext.current();
        final Account caller = ctx.getCallingAccount();

        _accountMgr.checkAccess(caller, SecurityChecker.AccessType.OperateEntry, true, gslbRule);

        if (algorithm != null && !GlobalLoadBalancerRule.Algorithm.isValidAlgorithm(algorithm)) {
            throw new InvalidParameterValueException("Invalid Algorithm: " + algorithm);
        }

        if (stickyMethod != null && !GlobalLoadBalancerRule.Persistence.isValidPersistence(stickyMethod)) {
            throw new InvalidParameterValueException("Invalid persistence: " + stickyMethod);
        }

        if (algorithm != null) {
            gslbRule.setAlgorithm(algorithm);
        }
        if (stickyMethod != null) {
            gslbRule.setPersistence(stickyMethod);
        }
        if (description != null) {
            gslbRule.setDescription(description);
        }
        gslbRule.setState(GlobalLoadBalancerRule.State.Add);
        _gslbRuleDao.update(gslbRule.getId(), gslbRule);

        try {
            s_logger.debug("Updating global load balancer with id " + gslbRule.getUuid());

            // apply the gslb rule on to the back end gslb service providers on zones participating in gslb
            applyGlobalLoadBalancerRuleConfig(gslbRuleId, false);

            // on success set state to Active
            gslbRule.setState(GlobalLoadBalancerRule.State.Active);
            _gslbRuleDao.update(gslbRule.getId(), gslbRule);

            return gslbRule;
        } catch (final ResourceUnavailableException e) {
            throw new CloudRuntimeException("Failed to configure gslb config due to " + e.getMessage());
        }
    }

    @Override
    public boolean revokeAllGslbRulesForAccount(final com.cloud.user.Account caller, final long accountId) throws com.cloud.exception.ResourceUnavailableException {
        final List<GlobalLoadBalancerRuleVO> gslbRules = _gslbRuleDao.listByAccount(accountId);
        if (gslbRules != null && !gslbRules.isEmpty()) {
            for (final GlobalLoadBalancerRule gslbRule : gslbRules) {
                revokeGslbRule(gslbRule.getId(), caller);
            }
        }
        s_logger.debug("Successfully cleaned up GSLB rules for account id=" + accountId);
        return true;
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_ASSIGN_TO_GLOBAL_LOAD_BALANCER_RULE,
            eventDescription = "Assigning a load balancer rule to global load balancer rule",
            async = true)
    public boolean assignToGlobalLoadBalancerRule(final AssignToGlobalLoadBalancerRuleCmd assignToGslbCmd) {

        final CallContext ctx = CallContext.current();
        final Account caller = ctx.getCallingAccount();

        final long gslbRuleId = assignToGslbCmd.getGlobalLoadBalancerRuleId();
        final GlobalLoadBalancerRuleVO gslbRule = _gslbRuleDao.findById(gslbRuleId);
        if (gslbRule == null) {
            throw new InvalidParameterValueException("Invalid global load balancer rule id: " + gslbRuleId);
        }

        _accountMgr.checkAccess(caller, SecurityChecker.AccessType.OperateEntry, true, gslbRule);

        if (gslbRule.getState() == GlobalLoadBalancerRule.State.Revoke) {
            throw new InvalidParameterValueException("global load balancer rule id: " + gslbRule.getUuid() + " is in revoked state");
        }

        final List<Long> newLbRuleIds = assignToGslbCmd.getLoadBalancerRulesIds();
        if (newLbRuleIds == null || newLbRuleIds.isEmpty()) {
            throw new InvalidParameterValueException("empty list of load balancer rule Ids specified to be assigned" + " global load balancer rule");
        }

        final List<Long> oldLbRuleIds = new ArrayList<>();
        final List<Long> oldZones = new ArrayList<>();
        final List<Long> newZones = new ArrayList<>(oldZones);
        final List<Pair<Long, Long>> physcialNetworks = new ArrayList<>();

        // get the list of load balancer rules id's that are assigned currently to GSLB rule and corresponding zone id's
        final List<GlobalLoadBalancerLbRuleMapVO> gslbLbMapVos = _gslbLbMapDao.listByGslbRuleId(gslbRuleId);
        if (gslbLbMapVos != null) {
            for (final GlobalLoadBalancerLbRuleMapVO gslbLbMapVo : gslbLbMapVos) {
                final LoadBalancerVO loadBalancer = _lbDao.findById(gslbLbMapVo.getLoadBalancerId());
                final Network network = _networkDao.findById(loadBalancer.getNetworkId());
                oldZones.add(network.getDataCenterId());
                oldLbRuleIds.add(gslbLbMapVo.getLoadBalancerId());
            }
        }

        /* check each of the load balancer rule id passed in the 'AssignToGlobalLoadBalancerRuleCmd' command is
         *     valid ID
         *     caller has access to the rule
         *     check rule is not revoked
         *     no two rules are in same zone
         *     rule is not already assigned to gslb rule
         */
        for (final Long lbRuleId : newLbRuleIds) {

            final LoadBalancerVO loadBalancer = _lbDao.findById(lbRuleId);
            if (loadBalancer == null) {
                throw new InvalidParameterValueException("Specified load balancer rule ID does not exist.");
            }

            _accountMgr.checkAccess(caller, null, true, loadBalancer);

            if (gslbRule.getAccountId() != loadBalancer.getAccountId()) {
                throw new InvalidParameterValueException("GSLB rule and load balancer rule does not belong to same account");
            }

            if (loadBalancer.getState() == LoadBalancer.State.Revoke) {
                throw new InvalidParameterValueException("Load balancer ID " + loadBalancer.getUuid() + " is in revoke state");
            }

            if (oldLbRuleIds != null && oldLbRuleIds.contains(loadBalancer.getId())) {
                throw new InvalidParameterValueException("Load balancer ID " + loadBalancer.getUuid() + " is already assigned");
            }

            final Network network = _networkDao.findById(loadBalancer.getNetworkId());

            if (oldZones != null && oldZones.contains(network.getDataCenterId()) || newZones != null && newZones.contains(network.getDataCenterId())) {
                throw new InvalidParameterValueException("Load balancer rule specified should be in unique zone");
            }

            newZones.add(network.getDataCenterId());
            physcialNetworks.add(new Pair<>(network.getDataCenterId(), network.getPhysicalNetworkId()));
        }

        // for each of the physical network check if GSLB service provider configured
        for (final Pair<Long, Long> physicalNetwork : physcialNetworks) {
            if (!checkGslbServiceEnabledInZone(physicalNetwork.first(), physicalNetwork.second())) {
                throw new InvalidParameterValueException("GSLB service is not enabled in the Zone:" + physicalNetwork.first() + " and physical network " +
                        physicalNetwork.second());
            }
        }

        final Map<Long, Long> lbRuleWeightMap = assignToGslbCmd.getLoadBalancerRuleWeightMap();

        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                // persist the mapping for the new Lb rule that needs to assigned to a gslb rule
                for (final Long lbRuleId : newLbRuleIds) {
                    final GlobalLoadBalancerLbRuleMapVO newGslbLbMap = new GlobalLoadBalancerLbRuleMapVO();
                    newGslbLbMap.setGslbLoadBalancerId(gslbRuleId);
                    newGslbLbMap.setLoadBalancerId(lbRuleId);
                    if (lbRuleWeightMap != null && lbRuleWeightMap.get(lbRuleId) != null) {
                        newGslbLbMap.setWeight(lbRuleWeightMap.get(lbRuleId));
                    }
                    _gslbLbMapDao.persist(newGslbLbMap);
                }

                // mark the gslb rule state as add
                if (gslbRule.getState() == GlobalLoadBalancerRule.State.Staged || gslbRule.getState() == GlobalLoadBalancerRule.State.Active) {
                    gslbRule.setState(GlobalLoadBalancerRule.State.Add);
                    _gslbRuleDao.update(gslbRule.getId(), gslbRule);
                }
            }
        });

        boolean success = false;
        try {
            s_logger.debug("Configuring gslb rule configuration on the gslb service providers in the participating zones");

            // apply the gslb rule on to the back end gslb service providers on zones participating in gslb
            if (!applyGlobalLoadBalancerRuleConfig(gslbRuleId, false)) {
                s_logger.warn("Failed to add load balancer rules " + newLbRuleIds + " to global load balancer rule id " + gslbRuleId);
                final CloudRuntimeException ex = new CloudRuntimeException("Failed to add load balancer rules to GSLB rule ");
                throw ex;
            }

            // on success set state to Active
            gslbRule.setState(GlobalLoadBalancerRule.State.Active);
            _gslbRuleDao.update(gslbRule.getId(), gslbRule);

            success = true;
        } catch (final ResourceUnavailableException e) {
            throw new CloudRuntimeException("Failed to apply new GSLB configuration while assigning new LB rules to GSLB rule.");
        }

        return success;
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_REMOVE_FROM_GLOBAL_LOAD_BALANCER_RULE,
            eventDescription = "Removing a load balancer rule to be part of global load balancer rule")
    public boolean removeFromGlobalLoadBalancerRule(final RemoveFromGlobalLoadBalancerRuleCmd removeFromGslbCmd) {

        final CallContext ctx = CallContext.current();
        final Account caller = ctx.getCallingAccount();

        final long gslbRuleId = removeFromGslbCmd.getGlobalLoadBalancerRuleId();
        final GlobalLoadBalancerRuleVO gslbRule = _gslbRuleDao.findById(gslbRuleId);
        if (gslbRule == null) {
            throw new InvalidParameterValueException("Invalid global load balancer rule id: " + gslbRuleId);
        }

        _accountMgr.checkAccess(caller, SecurityChecker.AccessType.OperateEntry, true, gslbRule);

        if (gslbRule.getState() == GlobalLoadBalancerRule.State.Revoke) {
            throw new InvalidParameterValueException("global load balancer rule id: " + gslbRuleId + " is already in revoked state");
        }

        final List<Long> lbRuleIdsToremove = removeFromGslbCmd.getLoadBalancerRulesIds();
        if (lbRuleIdsToremove == null || lbRuleIdsToremove.isEmpty()) {
            throw new InvalidParameterValueException("empty list of load balancer rule Ids specified to be un-assigned" + " to global load balancer rule");
        }

        // get the active list of LB rule id's that are assigned currently to GSLB rule and corresponding zone id's
        final List<Long> oldLbRuleIds = new ArrayList<>();
        final List<Long> oldZones = new ArrayList<>();

        final List<GlobalLoadBalancerLbRuleMapVO> gslbLbMapVos = _gslbLbMapDao.listByGslbRuleId(gslbRuleId);
        if (gslbLbMapVos == null) {
            throw new InvalidParameterValueException(" There are no load balancer rules that are assigned to global " + " load balancer rule id: " + gslbRule.getUuid() +
                    " that are available for deletion");
        }

        for (final Long lbRuleId : lbRuleIdsToremove) {
            final LoadBalancerVO loadBalancer = _lbDao.findById(lbRuleId);
            if (loadBalancer == null) {
                throw new InvalidParameterValueException("Specified load balancer rule ID does not exist.");
            }

            _accountMgr.checkAccess(caller, null, true, loadBalancer);
        }

        for (final GlobalLoadBalancerLbRuleMapVO gslbLbMapVo : gslbLbMapVos) {
            final LoadBalancerVO loadBalancer = _lbDao.findById(gslbLbMapVo.getLoadBalancerId());
            final Network network = _networkDao.findById(loadBalancer.getNetworkId());
            oldLbRuleIds.add(gslbLbMapVo.getLoadBalancerId());
            oldZones.add(network.getDataCenterId());
        }

        for (final Long lbRuleId : lbRuleIdsToremove) {
            final LoadBalancerVO loadBalancer = _lbDao.findById(lbRuleId);
            if (oldLbRuleIds != null && !oldLbRuleIds.contains(loadBalancer.getId())) {
                throw new InvalidParameterValueException("Load balancer ID " + loadBalancer.getUuid() + " is not assigned" + " to global load balancer rule: " +
                        gslbRule.getUuid());
            }
        }

        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                // update the mapping of gslb rule to Lb rule, to revoke state
                for (final Long lbRuleId : lbRuleIdsToremove) {
                    final GlobalLoadBalancerLbRuleMapVO removeGslbLbMap = _gslbLbMapDao.findByGslbRuleIdAndLbRuleId(gslbRuleId, lbRuleId);
                    removeGslbLbMap.setRevoke(true);
                    _gslbLbMapDao.update(removeGslbLbMap.getId(), removeGslbLbMap);
                }

                // mark the gslb rule state as add
                if (gslbRule.getState() == GlobalLoadBalancerRule.State.Staged) {
                    gslbRule.setState(GlobalLoadBalancerRule.State.Add);
                    _gslbRuleDao.update(gslbRule.getId(), gslbRule);
                }
            }
        });

        boolean success = false;
        try {
            s_logger.debug("Attempting to configure global load balancer rule configuration on the gslb service providers ");

            // apply the gslb rule on to the back end gslb service providers
            if (!applyGlobalLoadBalancerRuleConfig(gslbRuleId, false)) {
                s_logger.warn("Failed to remove load balancer rules " + lbRuleIdsToremove + " from global load balancer rule id " + gslbRuleId);
                final CloudRuntimeException ex = new CloudRuntimeException("Failed to remove load balancer rule ids from GSLB rule ");
                throw ex;
            }

            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    // remove the mappings of gslb rule to Lb rule that are in revoked state
                    for (final Long lbRuleId : lbRuleIdsToremove) {
                        final GlobalLoadBalancerLbRuleMapVO removeGslbLbMap = _gslbLbMapDao.findByGslbRuleIdAndLbRuleId(gslbRuleId, lbRuleId);
                        _gslbLbMapDao.remove(removeGslbLbMap.getId());
                    }

                    // on success set state back to Active
                    gslbRule.setState(GlobalLoadBalancerRule.State.Active);
                    _gslbRuleDao.update(gslbRule.getId(), gslbRule);
                }
            });

            success = true;
        } catch (final ResourceUnavailableException e) {
            throw new CloudRuntimeException("Failed to update removed load balancer details from gloabal load balancer");
        }

        return success;
    }

    @Override
    public GlobalLoadBalancerRule findById(final long gslbRuleId) {
        return _gslbRuleDao.findById(gslbRuleId);
    }

    @Override
    public List<GlobalLoadBalancerRule> listGlobalLoadBalancerRule(final ListGlobalLoadBalancerRuleCmd listGslbCmd) {

        final CallContext ctx = CallContext.current();
        final Account caller = ctx.getCallingAccount();

        final Integer regionId = listGslbCmd.getRegionId();
        final Long ruleId = listGslbCmd.getId();
        final List<GlobalLoadBalancerRule> response = new ArrayList<>();
        if (regionId == null && ruleId == null) {
            throw new InvalidParameterValueException("Invalid arguments. At least one of region id, " + "rule id must be specified");
        }

        if (regionId != null && ruleId != null) {
            throw new InvalidParameterValueException("Invalid arguments. Only one of region id, " + "rule id must be specified");
        }

        if (ruleId != null) {
            final GlobalLoadBalancerRule gslbRule = _gslbRuleDao.findById(ruleId);
            if (gslbRule == null) {
                throw new InvalidParameterValueException("Invalid gslb rule id specified");
            }
            _accountMgr.checkAccess(caller, org.apache.cloudstack.acl.SecurityChecker.AccessType.UseEntry, false, gslbRule);

            response.add(gslbRule);
            return response;
        }

        if (regionId != null) {
            final List<GlobalLoadBalancerRuleVO> gslbRules = _gslbRuleDao.listByAccount(caller.getAccountId());
            if (gslbRules != null) {
                response.addAll(gslbRules);
            }
            return response;
        }

        return null;
    }

    @Override
    public List<LoadBalancer> listSiteLoadBalancers(final long gslbRuleId) {
        final List<GlobalLoadBalancerLbRuleMapVO> gslbLbMapVos = _gslbLbMapDao.listByGslbRuleId(gslbRuleId);
        final List<LoadBalancer> siteLoadBalancers = new ArrayList<>();
        if (gslbLbMapVos != null) {
            for (final GlobalLoadBalancerLbRuleMapVO gslbLbMapVo : gslbLbMapVos) {
                final LoadBalancerVO loadBalancer = _lbDao.findById(gslbLbMapVo.getLoadBalancerId());
                siteLoadBalancers.add(loadBalancer);
            }
            return siteLoadBalancers;
        }
        return null;
    }

    private boolean checkGslbServiceEnabledInZone(final long zoneId, final long physicalNetworkId) {

        final GslbServiceProvider gslbProvider = lookupGslbServiceProvider();
        if (gslbProvider == null) {
            throw new CloudRuntimeException("No GSLB provider is available");
        }

        return gslbProvider.isServiceEnabledInZone(zoneId, physicalNetworkId);
    }
}
