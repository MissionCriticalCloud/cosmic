package org.apache.cloudstack.network.lb;

import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventUtils;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientVirtualNetworkCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.UnsupportedServiceException;
import com.cloud.network.IpAddressManager;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkModel;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.lb.LoadBalancingRule;
import com.cloud.network.lb.LoadBalancingRulesManager;
import com.cloud.network.lb.LoadBalancingRulesService;
import com.cloud.network.rules.FirewallRule.State;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.projects.Project.ListProjectResourcesCriteria;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.tags.ResourceTagVO;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallbackWithException;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.Ip;
import com.cloud.utils.net.NetUtils;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.command.user.loadbalancer.ListApplicationLoadBalancersCmd;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;
import org.apache.cloudstack.lb.ApplicationLoadBalancerRuleVO;
import org.apache.cloudstack.lb.dao.ApplicationLoadBalancerRuleDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationLoadBalancerManagerImpl extends ManagerBase implements ApplicationLoadBalancerService {
    private static final Logger s_logger = LoggerFactory.getLogger(ApplicationLoadBalancerManagerImpl.class);

    @Inject
    NetworkModel _networkModel;
    @Inject
    ApplicationLoadBalancerRuleDao _lbDao;
    @Inject
    AccountManager _accountMgr;
    @Inject
    LoadBalancingRulesManager _lbMgr;
    @Inject
    FirewallRulesDao _firewallDao;
    @Inject
    ResourceTagDao _resourceTagDao;
    @Inject
    NetworkOrchestrationService _ntwkMgr;
    @Inject
    IpAddressManager _ipAddrMgr;
    @Inject
    LoadBalancingRulesService _lbService;

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_LOAD_BALANCER_CREATE, eventDescription = "creating load balancer")
    public ApplicationLoadBalancerRule createApplicationLoadBalancer(final String name, final String description, final Scheme scheme, final long sourceIpNetworkId, final String
            sourceIp,
                                                                     final int sourcePort, final int instancePort, final String algorithm, final long networkId, final long
                                                                             lbOwnerId, final Boolean forDisplay)
            throws InsufficientAddressCapacityException, NetworkRuleConflictException,
            InsufficientVirtualNetworkCapacityException {

        //Validate LB rule guest network
        final Network guestNtwk = _networkModel.getNetwork(networkId);
        if (guestNtwk == null || guestNtwk.getTrafficType() != TrafficType.Guest) {
            throw new InvalidParameterValueException("Can't find guest network by id");
        }

        final Account caller = CallContext.current().getCallingAccount();
        _accountMgr.checkAccess(caller, AccessType.UseEntry, false, guestNtwk);

        final Network sourceIpNtwk = _networkModel.getNetwork(sourceIpNetworkId);
        if (sourceIpNtwk == null) {
            throw new InvalidParameterValueException("Can't find source ip network by id");
        }

        final Account lbOwner = _accountMgr.getAccount(lbOwnerId);
        if (lbOwner == null) {
            throw new InvalidParameterValueException("Can't find the lb owner account");
        }

        return createApplicationLoadBalancer(name, description, scheme, sourceIpNtwk, sourceIp, sourcePort, instancePort, algorithm, lbOwner, guestNtwk, forDisplay);
    }

    protected ApplicationLoadBalancerRule createApplicationLoadBalancer(final String name, final String description, final Scheme scheme, final Network sourceIpNtwk, final
    String sourceIp,
                                                                        final int sourcePort, final int instancePort, final String algorithm, final Account lbOwner, final
                                                                        Network guestNtwk, final Boolean
                                                                                forDisplay) throws NetworkRuleConflictException,
            InsufficientVirtualNetworkCapacityException {

        //Only Internal scheme is supported in this release
        if (scheme != Scheme.Internal) {
            throw new UnsupportedServiceException("Only scheme of type " + Scheme.Internal + " is supported");
        }

        //1) Validate LB rule's parameters
        validateLbRule(sourcePort, instancePort, algorithm, guestNtwk, scheme);

        //2) Validate source network
        validateSourceIpNtwkForLbRule(sourceIpNtwk, scheme);

        //3) Get source ip address
        final Ip sourceIpAddr = getSourceIp(scheme, sourceIpNtwk, sourceIp);

        final ApplicationLoadBalancerRuleVO newRule =
                new ApplicationLoadBalancerRuleVO(name, description, sourcePort, instancePort, algorithm, guestNtwk.getId(), lbOwner.getId(), lbOwner.getDomainId(),
                        sourceIpAddr, sourceIpNtwk.getId(), scheme);

        if (forDisplay != null) {
            newRule.setDisplay(forDisplay);
        }

        //4) Validate Load Balancing rule on the providers
        final LoadBalancingRule loadBalancing =
                new LoadBalancingRule(newRule, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), sourceIpAddr, null,
                        null);
        if (!_lbMgr.validateLbRule(loadBalancing)) {
            throw new InvalidParameterValueException("LB service provider cannot support this rule");
        }

        //5) Persist Load Balancer rule
        return persistLbRule(newRule);
    }

    /**
     * Validates Lb rule parameters
     *
     * @param sourcePort
     * @param instancePort
     * @param algorithm
     * @param network
     * @param scheme       TODO
     * @param networkId
     */
    protected void validateLbRule(final int sourcePort, final int instancePort, final String algorithm, final Network network, final Scheme scheme) {
        //1) verify that lb service is supported by the network
        if (!_networkModel.areServicesSupportedInNetwork(network.getId(), Service.Lb)) {
            final InvalidParameterValueException ex = new InvalidParameterValueException("LB service is not supported in specified network id");
            ex.addProxyObject(network.getUuid(), "networkId");
            throw ex;
        }

        //2) verify that lb service is supported by the network
        _lbMgr.isLbServiceSupportedInNetwork(network.getId(), scheme);

        final Map<Network.Capability, String> caps = _networkModel.getNetworkServiceCapabilities(network.getId(), Service.Lb);
        final String supportedProtocols = caps.get(Capability.SupportedProtocols).toLowerCase();
        if (!supportedProtocols.contains(NetUtils.TCP_PROTO.toLowerCase())) {
            throw new InvalidParameterValueException("Protocol " + NetUtils.TCP_PROTO.toLowerCase() + " is not supported in zone " + network.getDataCenterId());
        }

        //3) Validate rule parameters
        if (!NetUtils.isValidPort(instancePort)) {
            throw new InvalidParameterValueException("Invalid value for instance port: " + instancePort);
        }

        if (!NetUtils.isValidPort(sourcePort)) {
            throw new InvalidParameterValueException("Invalid value for source port: " + sourcePort);
        }

        if ((algorithm == null) || !NetUtils.isValidAlgorithm(algorithm)) {
            throw new InvalidParameterValueException("Invalid algorithm: " + algorithm);
        }
    }

    /**
     * Validates source IP network for the LB rule
     *
     * @param sourceNtwk
     * @param scheme
     * @return
     */
    protected Network validateSourceIpNtwkForLbRule(final Network sourceNtwk, final Scheme scheme) {
        //only Internal scheme is supported in this release
        if (scheme != Scheme.Internal) {
            throw new UnsupportedServiceException("Only scheme of type " + Scheme.Internal + " is supported");
        } else {
            //validate source ip network
            return validateSourceIpNtwkForInternalLbRule(sourceNtwk);
        }
    }

    /**
     * Gets source ip address based on the LB rule scheme/source IP network/requested IP address
     *
     * @param scheme
     * @param sourceIpNtwk
     * @param requestedIp
     * @return
     * @throws InsufficientVirtualNetworkCapacityException
     */
    protected Ip getSourceIp(final Scheme scheme, final Network sourceIpNtwk, String requestedIp) throws InsufficientVirtualNetworkCapacityException {

        if (requestedIp != null) {
            if (_lbDao.countBySourceIp(new Ip(requestedIp), sourceIpNtwk.getId()) > 0) {
                s_logger.debug("IP address " + requestedIp + " is already used by existing LB rule, returning it");
                return new Ip(requestedIp);
            }

            validateRequestedSourceIpForLbRule(sourceIpNtwk, new Ip(requestedIp), scheme);
        }

        requestedIp = allocateSourceIpForLbRule(scheme, sourceIpNtwk, requestedIp);

        if (requestedIp == null) {
            throw new InsufficientVirtualNetworkCapacityException("Unable to acquire IP address for network " + sourceIpNtwk, Network.class, sourceIpNtwk.getId());
        }
        return new Ip(requestedIp);
    }

    @DB
    protected ApplicationLoadBalancerRule persistLbRule(final ApplicationLoadBalancerRuleVO newRuleFinal) throws NetworkRuleConflictException {
        boolean success = true;
        ApplicationLoadBalancerRuleVO newRule = null;
        try {
            newRule = Transaction.execute(new TransactionCallbackWithException<ApplicationLoadBalancerRuleVO, NetworkRuleConflictException>() {
                @Override
                public ApplicationLoadBalancerRuleVO doInTransaction(final TransactionStatus status) throws NetworkRuleConflictException {
                    //1) Persist the rule
                    final ApplicationLoadBalancerRuleVO newRule = _lbDao.persist(newRuleFinal);

                    //2) Detect conflicts
                    detectLbRulesConflicts(newRule);
                    if (!_firewallDao.setStateToAdd(newRule)) {
                        throw new CloudRuntimeException("Unable to update the state to add for " + newRule);
                    }
                    s_logger.debug("Load balancer " + newRule.getId() + " for Ip address " + newRule.getSourceIp().addr() + ", source port " +
                            newRule.getSourcePortStart().intValue() + ", instance port " + newRule.getDefaultPortStart() + " is added successfully.");
                    CallContext.current().setEventDetails("Load balancer Id: " + newRule.getId());
                    final Network ntwk = _networkModel.getNetwork(newRule.getNetworkId());
                    UsageEventUtils.publishUsageEvent(EventTypes.EVENT_LOAD_BALANCER_CREATE, newRule.getAccountId(), ntwk.getDataCenterId(), newRule.getId(), null,
                            LoadBalancingRule.class.getName(), newRule.getUuid());

                    return newRule;
                }
            });

            return newRule;
        } catch (final Exception e) {
            success = false;
            if (e instanceof NetworkRuleConflictException) {
                throw (NetworkRuleConflictException) e;
            }
            throw new CloudRuntimeException("Unable to add lb rule for ip address " + newRuleFinal.getSourceIpAddressId(), e);
        } finally {
            if (!success && newRule != null) {
                _lbMgr.removeLBRule(newRule);
            }
        }
    }

    /**
     * Validates source IP network for the Internal LB rule
     *
     * @param sourceIpNtwk
     * @return
     */
    protected Network validateSourceIpNtwkForInternalLbRule(final Network sourceIpNtwk) {
        if (sourceIpNtwk.getTrafficType() != TrafficType.Guest) {
            throw new InvalidParameterValueException("Only traffic type " + TrafficType.Guest + " is supported");
        }

        //Can't create the LB rule if the network's cidr is NULL
        final String ntwkCidr = sourceIpNtwk.getCidr();
        if (ntwkCidr == null) {
            throw new InvalidParameterValueException("Can't create the application load balancer rule for the network having NULL cidr");
        }

        //check if the requested ip address is within the cidr
        return sourceIpNtwk;
    }

    /**
     * Validates requested source ip address of the LB rule based on Lb rule scheme/sourceNetwork
     *
     * @param sourceIpNtwk
     * @param requestedSourceIp
     * @param scheme
     */
    void validateRequestedSourceIpForLbRule(final Network sourceIpNtwk, final Ip requestedSourceIp, final Scheme scheme) {
        //only Internal scheme is supported in this release
        if (scheme != Scheme.Internal) {
            throw new UnsupportedServiceException("Only scheme of type " + Scheme.Internal + " is supported");
        } else {
            //validate guest source ip
            validateRequestedSourceIpForInternalLbRule(sourceIpNtwk, requestedSourceIp);
        }
    }

    /**
     * Allocates new Source IP address for the Load Balancer rule based on LB rule scheme/sourceNetwork
     *
     * @param scheme
     * @param sourceIpNtwk
     * @param requestedIp  TODO
     * @param sourceIp
     * @return
     */
    protected String allocateSourceIpForLbRule(final Scheme scheme, final Network sourceIpNtwk, final String requestedIp) {
        String sourceIp = null;
        if (scheme != Scheme.Internal) {
            throw new InvalidParameterValueException("Only scheme " + Scheme.Internal + " is supported");
        } else {
            sourceIp = allocateSourceIpForInternalLbRule(sourceIpNtwk, requestedIp);
        }
        return sourceIp;
    }

    /**
     * Detects lb rule conflicts against other rules
     *
     * @param newLbRule
     * @throws NetworkRuleConflictException
     */
    protected void detectLbRulesConflicts(final ApplicationLoadBalancerRule newLbRule) throws NetworkRuleConflictException {
        if (newLbRule.getScheme() != Scheme.Internal) {
            throw new UnsupportedServiceException("Only scheme of type " + Scheme.Internal + " is supported");
        } else {
            detectInternalLbRulesConflict(newLbRule);
        }
    }

    /**
     * Validates requested source IP address of Internal Lb rule against sourceNetworkId
     *
     * @param sourceIpNtwk
     * @param requestedSourceIp
     */
    protected void validateRequestedSourceIpForInternalLbRule(final Network sourceIpNtwk, final Ip requestedSourceIp) {
        //Check if the IP is within the network cidr
        final Pair<String, Integer> cidr = NetUtils.getCidr(sourceIpNtwk.getCidr());
        if (!NetUtils.getCidrSubNet(requestedSourceIp.addr(), cidr.second()).equalsIgnoreCase(NetUtils.getCidrSubNet(cidr.first(), cidr.second()))) {
            throw new InvalidParameterValueException("The requested IP is not in the network's CIDR subnet.");
        }
    }

    /**
     * Allocates sourceIp for the Internal LB rule
     *
     * @param sourceIpNtwk
     * @param requestedIp  TODO
     * @return
     */
    protected String allocateSourceIpForInternalLbRule(final Network sourceIpNtwk, final String requestedIp) {
        return _ipAddrMgr.acquireGuestIpAddress(sourceIpNtwk, requestedIp);
    }

    /**
     * Detects Internal Lb Rules conflicts
     *
     * @param newLbRule
     * @throws NetworkRuleConflictException
     */
    protected void detectInternalLbRulesConflict(final ApplicationLoadBalancerRule newLbRule) throws NetworkRuleConflictException {
        final List<ApplicationLoadBalancerRuleVO> lbRules = _lbDao.listBySourceIpAndNotRevoked(newLbRule.getSourceIp(), newLbRule.getSourceIpNetworkId());

        for (final ApplicationLoadBalancerRuleVO lbRule : lbRules) {
            if (lbRule.getId() == newLbRule.getId()) {
                continue; // Skips my own rule.
            }

            if (lbRule.getNetworkId() != newLbRule.getNetworkId() && lbRule.getState() != State.Revoke) {
                throw new NetworkRuleConflictException("New rule is for a different network than what's specified in rule " + lbRule.getXid());
            }

            if ((lbRule.getSourcePortStart().intValue() <= newLbRule.getSourcePortStart().intValue() && lbRule.getSourcePortEnd().intValue() >= newLbRule.getSourcePortStart()
                                                                                                                                                         .intValue()) ||
                    (lbRule.getSourcePortStart().intValue() <= newLbRule.getSourcePortEnd().intValue() && lbRule.getSourcePortEnd().intValue() >= newLbRule.getSourcePortEnd()
                                                                                                                                                           .intValue()) ||
                    (newLbRule.getSourcePortStart().intValue() <= lbRule.getSourcePortStart().intValue() && newLbRule.getSourcePortEnd().intValue() >= lbRule.getSourcePortStart()
                                                                                                                                                             .intValue()) ||
                    (newLbRule.getSourcePortStart().intValue() <= lbRule.getSourcePortEnd().intValue() && newLbRule.getSourcePortEnd().intValue() >= lbRule.getSourcePortEnd()
                                                                                                                                                           .intValue())) {

                throw new NetworkRuleConflictException("The range specified, " + newLbRule.getSourcePortStart().intValue() + "-" + newLbRule.getSourcePortEnd().intValue() +
                        ", conflicts with rule " + lbRule.getId() + " which has " + lbRule.getSourcePortStart().intValue() + "-" + lbRule.getSourcePortEnd().intValue());
            }
        }

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("No network rule conflicts detected for " + newLbRule + " against " + (lbRules.size() - 1) + " existing rules");
        }
    }

    @Override
    public boolean deleteApplicationLoadBalancer(final long id) {
        return _lbService.deleteLoadBalancerRule(id, true);
    }

    @Override
    public Pair<List<? extends ApplicationLoadBalancerRule>, Integer> listApplicationLoadBalancers(final ListApplicationLoadBalancersCmd cmd) {
        final Long id = cmd.getId();
        final String name = cmd.getLoadBalancerRuleName();
        final String ip = cmd.getSourceIp();
        final Long ipNtwkId = cmd.getSourceIpNetworkId();
        final String keyword = cmd.getKeyword();
        final Scheme scheme = cmd.getScheme();
        final Long networkId = cmd.getNetworkId();
        final Boolean display = cmd.getDisplay();

        final Map<String, String> tags = cmd.getTags();

        final Account caller = CallContext.current().getCallingAccount();
        final List<Long> permittedAccounts = new ArrayList<>();

        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), cmd.getProjectId(), permittedAccounts,
                domainIdRecursiveListProject, cmd.listAll(), false);
        final Long domainId = domainIdRecursiveListProject.first();
        final Boolean isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();

        final Filter searchFilter = new Filter(ApplicationLoadBalancerRuleVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());
        final SearchBuilder<ApplicationLoadBalancerRuleVO> sb = _lbDao.createSearchBuilder();
        _accountMgr.buildACLSearchBuilder(sb, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);

        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.EQ);
        sb.and("sourceIpAddress", sb.entity().getSourceIp(), SearchCriteria.Op.EQ);
        sb.and("sourceIpAddressNetworkId", sb.entity().getSourceIpNetworkId(), SearchCriteria.Op.EQ);
        sb.and("scheme", sb.entity().getScheme(), SearchCriteria.Op.EQ);
        sb.and("networkId", sb.entity().getNetworkId(), SearchCriteria.Op.EQ);
        sb.and("display", sb.entity().isDisplay(), SearchCriteria.Op.EQ);

        //list only load balancers having not null sourceIp/sourceIpNtwkId
        sb.and("sourceIpAddress", sb.entity().getSourceIp(), SearchCriteria.Op.NNULL);
        sb.and("sourceIpAddressNetworkId", sb.entity().getSourceIpNetworkId(), SearchCriteria.Op.NNULL);

        if (tags != null && !tags.isEmpty()) {
            final SearchBuilder<ResourceTagVO> tagSearch = _resourceTagDao.createSearchBuilder();
            for (int count = 0; count < tags.size(); count++) {
                tagSearch.or().op("key" + String.valueOf(count), tagSearch.entity().getKey(), SearchCriteria.Op.EQ);
                tagSearch.and("value" + String.valueOf(count), tagSearch.entity().getValue(), SearchCriteria.Op.EQ);
                tagSearch.cp();
            }
            tagSearch.and("resourceType", tagSearch.entity().getResourceType(), SearchCriteria.Op.EQ);
            sb.groupBy(sb.entity().getId());
            sb.join("tagSearch", tagSearch, sb.entity().getId(), tagSearch.entity().getResourceId(), JoinBuilder.JoinType.INNER);
        }

        final SearchCriteria<ApplicationLoadBalancerRuleVO> sc = sb.create();
        _accountMgr.buildACLSearchCriteria(sc, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);

        if (keyword != null) {
            final SearchCriteria<ApplicationLoadBalancerRuleVO> ssc = _lbDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (name != null) {
            sc.setParameters("name", name);
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (ip != null) {
            sc.setParameters("sourceIpAddress", ip);
        }

        if (ipNtwkId != null) {
            sc.setParameters("sourceIpAddressNetworkId", ipNtwkId);
        }

        if (scheme != null) {
            sc.setParameters("scheme", scheme);
        }

        if (networkId != null) {
            sc.setParameters("networkId", networkId);
        }

        if (tags != null && !tags.isEmpty()) {
            int count = 0;
            sc.setJoinParameters("tagSearch", "resourceType", ResourceObjectType.LoadBalancer.toString());
            for (final String key : tags.keySet()) {
                sc.setJoinParameters("tagSearch", "key" + String.valueOf(count), key);
                sc.setJoinParameters("tagSearch", "value" + String.valueOf(count), tags.get(key));
                count++;
            }
        }

        if (display != null) {
            sc.setParameters("display", display);
        }

        final Pair<List<ApplicationLoadBalancerRuleVO>, Integer> result = _lbDao.searchAndCount(sc, searchFilter);
        return new Pair<>(result.first(), result.second());
    }

    @Override
    public ApplicationLoadBalancerRule getApplicationLoadBalancer(final long ruleId) {
        final ApplicationLoadBalancerRule lbRule = _lbDao.findById(ruleId);
        if (lbRule == null) {
            throw new InvalidParameterValueException("Can't find the load balancer by id");
        }
        return lbRule;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_LOAD_BALANCER_UPDATE, eventDescription = "updating load balancer", async = true)
    public ApplicationLoadBalancerRule updateApplicationLoadBalancer(final Long id, final String customId, final Boolean forDisplay) {
        final Account caller = CallContext.current().getCallingAccount();
        final ApplicationLoadBalancerRuleVO rule = _lbDao.findById(id);

        if (rule == null) {
            throw new InvalidParameterValueException("Unable to find load balancer " + id);
        }
        _accountMgr.checkAccess(caller, null, true, rule);

        if (customId != null) {
            rule.setUuid(customId);
        }

        if (forDisplay != null) {
            rule.setDisplay(forDisplay);
        }

        _lbDao.update(id, rule);

        return _lbDao.findById(id);
    }
}
