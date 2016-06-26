package com.cloud.network.security;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.NetworkRulesSystemVmCommand;
import com.cloud.agent.api.NetworkRulesVmSecondaryIpCommand;
import com.cloud.agent.api.SecurityGroupRulesCmd;
import com.cloud.agent.api.SecurityGroupRulesCmd.IpPortAndProto;
import com.cloud.agent.manager.Commands;
import com.cloud.api.query.dao.SecurityGroupJoinDao;
import com.cloud.api.query.vo.SecurityGroupJoinVO;
import com.cloud.configuration.Config;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.event.UsageEventUtils;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.exception.ResourceInUseException;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.security.SecurityGroupWork.Step;
import com.cloud.network.security.SecurityRule.SecurityRuleType;
import com.cloud.network.security.dao.SecurityGroupDao;
import com.cloud.network.security.dao.SecurityGroupRuleDao;
import com.cloud.network.security.dao.SecurityGroupRulesDao;
import com.cloud.network.security.dao.SecurityGroupVMMapDao;
import com.cloud.network.security.dao.SecurityGroupWorkDao;
import com.cloud.network.security.dao.VmRulesetLogDao;
import com.cloud.projects.ProjectManager;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.DomainManager;
import com.cloud.user.dao.AccountDao;
import com.cloud.uservm.UserVm;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GlobalLock;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionCallbackWithException;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.fsm.StateListener;
import com.cloud.utils.fsm.StateMachine2;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicVO;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Event;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.VirtualMachineManager;
import com.cloud.vm.dao.NicDao;
import com.cloud.vm.dao.NicSecondaryIpDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.api.command.user.securitygroup.AuthorizeSecurityGroupEgressCmd;
import org.apache.cloudstack.api.command.user.securitygroup.AuthorizeSecurityGroupIngressCmd;
import org.apache.cloudstack.api.command.user.securitygroup.CreateSecurityGroupCmd;
import org.apache.cloudstack.api.command.user.securitygroup.DeleteSecurityGroupCmd;
import org.apache.cloudstack.api.command.user.securitygroup.RevokeSecurityGroupEgressCmd;
import org.apache.cloudstack.api.command.user.securitygroup.RevokeSecurityGroupIngressCmd;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.engine.orchestration.service.NetworkOrchestrationService;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;
import org.apache.cloudstack.utils.identity.ManagementServerNode;

import javax.ejb.ConcurrentAccessException;
import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityGroupManagerImpl extends ManagerBase implements SecurityGroupManager, SecurityGroupService, StateListener<State, VirtualMachine.Event, VirtualMachine> {
    public static final Logger s_logger = LoggerFactory.getLogger(SecurityGroupManagerImpl.class);
    private final GlobalLock _workLock = GlobalLock.getInternLock("SecurityGroupWork");
    protected long _serverId;
    protected int _numWorkerThreads = WORKER_THREAD_COUNT;
    @Inject
    SecurityGroupDao _securityGroupDao;
    @Inject
    SecurityGroupJoinDao _securityGroupJoinDao;
    @Inject
    SecurityGroupRuleDao _securityGroupRuleDao;
    @Inject
    SecurityGroupVMMapDao _securityGroupVMMapDao;
    @Inject
    SecurityGroupRulesDao _securityGroupRulesDao;
    @Inject
    UserVmDao _userVMDao;
    @Inject
    AccountDao _accountDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    SecurityGroupWorkDao _workDao;
    @Inject
    VmRulesetLogDao _rulesetLogDao;
    @Inject
    DomainDao _domainDao;
    @Inject
    AgentManager _agentMgr;
    @Inject
    VirtualMachineManager _itMgr;
    @Inject
    UserVmManager _userVmMgr;
    @Inject
    VMInstanceDao _vmDao;
    @Inject
    NetworkOrchestrationService _networkMgr;
    @Inject
    NetworkModel _networkModel;
    @Inject
    AccountManager _accountMgr;
    @Inject
    DomainManager _domainMgr;
    @Inject
    ProjectManager _projectMgr;
    @Inject
    ResourceTagDao _resourceTagDao;
    @Inject
    NicDao _nicDao;
    @Inject
    NicSecondaryIpDao _nicSecIpDao;
    ScheduledExecutorService _executorPool;
    ScheduledExecutorService _cleanupExecutor;
    SecurityGroupListener _answerListener;
    private int _timeBetweenCleanups = TIME_BETWEEN_CLEANUPS; // seconds
    private int _globalWorkLockTimeout = 300; // 5 minutes

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_SECURITY_GROUP_CREATE, eventDescription = "creating security group")
    public SecurityGroupVO createSecurityGroup(final CreateSecurityGroupCmd cmd) throws PermissionDeniedException, InvalidParameterValueException {
        final String name = cmd.getSecurityGroupName();
        final Account caller = CallContext.current().getCallingAccount();
        final Account owner = _accountMgr.finalizeOwner(caller, cmd.getAccountName(), cmd.getDomainId(), cmd.getProjectId());

        if (_securityGroupDao.isNameInUse(owner.getId(), owner.getDomainId(), cmd.getSecurityGroupName())) {
            throw new InvalidParameterValueException("Unable to create security group, a group with name " + name + " already exists.");
        }

        return createSecurityGroup(cmd.getSecurityGroupName(), cmd.getDescription(), owner.getDomainId(), owner.getAccountId(), owner.getAccountName());
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_SECURITY_GROUP_REVOKE_INGRESS, eventDescription = "Revoking Ingress Rule ", async = true)
    public boolean revokeSecurityGroupIngress(final RevokeSecurityGroupIngressCmd cmd) {

        final Long id = cmd.getId();
        return revokeSecurityGroupRule(id, SecurityRuleType.IngressRule);
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_SECURITY_GROUP_REVOKE_EGRESS, eventDescription = "Revoking Egress Rule ", async = true)
    public boolean revokeSecurityGroupEgress(final RevokeSecurityGroupEgressCmd cmd) {
        final Long id = cmd.getId();
        return revokeSecurityGroupRule(id, SecurityRuleType.EgressRule);
    }

    @DB
    @Override
    @ActionEvent(eventType = EventTypes.EVENT_SECURITY_GROUP_DELETE, eventDescription = "deleting security group")
    public boolean deleteSecurityGroup(final DeleteSecurityGroupCmd cmd) throws ResourceInUseException {
        final Long groupId = cmd.getId();
        final Account caller = CallContext.current().getCallingAccount();

        final SecurityGroupVO group = _securityGroupDao.findById(groupId);
        if (group == null) {
            throw new InvalidParameterValueException("Unable to find network group: " + groupId + "; failed to delete group.");
        }

        // check permissions
        _accountMgr.checkAccess(caller, null, true, group);

        return Transaction.execute(new TransactionCallbackWithException<Boolean, ResourceInUseException>() {
            @Override
            public Boolean doInTransaction(final TransactionStatus status) throws ResourceInUseException {
                final SecurityGroupVO group = _securityGroupDao.lockRow(groupId, true);
                if (group == null) {
                    throw new InvalidParameterValueException("Unable to find security group by id " + groupId);
                }

                if (group.getName().equalsIgnoreCase(SecurityGroupManager.DEFAULT_GROUP_NAME)) {
                    throw new InvalidParameterValueException("The network group default is reserved");
                }

                final List<SecurityGroupRuleVO> allowingRules = _securityGroupRuleDao.listByAllowedSecurityGroupId(groupId);
                final List<SecurityGroupVMMapVO> securityGroupVmMap = _securityGroupVMMapDao.listBySecurityGroup(groupId);
                if (!allowingRules.isEmpty()) {
                    throw new ResourceInUseException("Cannot delete group when there are security rules that allow this group");
                } else if (!securityGroupVmMap.isEmpty()) {
                    throw new ResourceInUseException("Cannot delete group when it's in use by virtual machines");
                }

                _securityGroupDao.expunge(groupId);

                s_logger.debug("Deleted security group id=" + groupId);

                return true;
            }
        });
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_SECURITY_GROUP_AUTHORIZE_INGRESS, eventDescription = "Adding Ingress Rule ", async = true)
    public List<SecurityGroupRuleVO> authorizeSecurityGroupIngress(final AuthorizeSecurityGroupIngressCmd cmd) {
        final Long securityGroupId = cmd.getSecurityGroupId();
        final String protocol = cmd.getProtocol();
        final Integer startPort = cmd.getStartPort();
        final Integer endPort = cmd.getEndPort();
        final Integer icmpType = cmd.getIcmpType();
        final Integer icmpCode = cmd.getIcmpCode();
        final List<String> cidrList = cmd.getCidrList();
        final Map groupList = cmd.getUserSecurityGroupList();
        return authorizeSecurityGroupRule(securityGroupId, protocol, startPort, endPort, icmpType, icmpCode, cidrList, groupList, SecurityRuleType.IngressRule);
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_SECURITY_GROUP_AUTHORIZE_EGRESS, eventDescription = "Adding Egress Rule ", async = true)
    public List<SecurityGroupRuleVO> authorizeSecurityGroupEgress(final AuthorizeSecurityGroupEgressCmd cmd) {
        final Long securityGroupId = cmd.getSecurityGroupId();
        final String protocol = cmd.getProtocol();
        final Integer startPort = cmd.getStartPort();
        final Integer endPort = cmd.getEndPort();
        final Integer icmpType = cmd.getIcmpType();
        final Integer icmpCode = cmd.getIcmpCode();
        final List<String> cidrList = cmd.getCidrList();
        final Map groupList = cmd.getUserSecurityGroupList();
        return authorizeSecurityGroupRule(securityGroupId, protocol, startPort, endPort, icmpType, icmpCode, cidrList, groupList, SecurityRuleType.EgressRule);
    }

    @Override
    public boolean securityGroupRulesForVmSecIp(final long nicId, final String secondaryIp, final boolean ruleAction) {
        final Account caller = CallContext.current().getCallingAccount();

        if (secondaryIp == null) {
            throw new InvalidParameterValueException("Vm secondaryIp can't be null");
        }

        final NicVO nic = _nicDao.findById(nicId);
        final long vmId = nic.getInstanceId();
        final UserVm vm = _userVMDao.findById(vmId);
        if (vm == null || vm.getType() != VirtualMachine.Type.User) {
            throw new InvalidParameterValueException("Can't configure the SG ipset, arprules rules for the non existing or non user vm");
        }

        // Verify permissions
        _accountMgr.checkAccess(caller, null, false, vm);

        // Validate parameters
        final List<SecurityGroupVO> vmSgGrps = getSecurityGroupsForVm(vmId);
        if (vmSgGrps.isEmpty()) {
            s_logger.debug("Vm is not in any Security group ");
            return true;
        }

        //If network does not support SG service, no need add SG rules for secondary ip
        final Network network = _networkModel.getNetwork(nic.getNetworkId());
        if (!_networkModel.isSecurityGroupSupportedInNetwork(network)) {
            s_logger.debug("Network " + network + " is not enabled with security group service, " +
                    "so not applying SG rules for secondary ip");
            return true;
        }

        final String vmMac = vm.getPrivateMacAddress();
        final String vmName = vm.getInstanceName();
        if (vmMac == null || vmName == null) {
            throw new InvalidParameterValueException("vm name or vm mac can't be null");
        }

        //create command for the to add ip in ipset and arptables rules
        final NetworkRulesVmSecondaryIpCommand cmd = new NetworkRulesVmSecondaryIpCommand(vmName, vmMac, secondaryIp, ruleAction);
        s_logger.debug("Asking agent to configure rules for vm secondary ip");
        Commands cmds = null;

        cmds = new Commands(cmd);
        try {
            _agentMgr.send(vm.getHostId(), cmds);
        } catch (final AgentUnavailableException e) {
            s_logger.debug(e.toString());
        } catch (final OperationTimedoutException e) {
            s_logger.debug(e.toString());
        }

        return true;
    }

    private List<SecurityGroupRuleVO> authorizeSecurityGroupRule(final Long securityGroupId, String protocol, final Integer startPort, Integer endPort, final Integer icmpType,
                                                                 final Integer icmpCode, final List<String> cidrList, final Map groupList, final SecurityRuleType ruleType) {
        Integer startPortOrType = null;
        Integer endPortOrCode = null;

        // Validate parameters
        final SecurityGroup securityGroup = _securityGroupDao.findById(securityGroupId);
        if (securityGroup == null) {
            throw new InvalidParameterValueException("Unable to find security group by id " + securityGroupId);
        }

        if (cidrList == null && groupList == null) {
            throw new InvalidParameterValueException("At least one cidr or at least one security group needs to be specified");
        }

        final Account caller = CallContext.current().getCallingAccount();
        final Account owner = _accountMgr.getAccount(securityGroup.getAccountId());

        if (owner == null) {
            throw new InvalidParameterValueException("Unable to find security group owner by id=" + securityGroup.getAccountId());
        }

        // Verify permissions
        _accountMgr.checkAccess(caller, null, true, securityGroup);
        final Long domainId = owner.getDomainId();

        if (protocol == null) {
            protocol = NetUtils.ALL_PROTO;
        }

        if (cidrList != null) {
            for (final String cidr : cidrList) {
                if (!NetUtils.isValidCIDR(cidr)) {
                    throw new InvalidParameterValueException("Invalid cidr " + cidr);
                }
            }
        }

        if (!NetUtils.isValidSecurityGroupProto(protocol)) {
            throw new InvalidParameterValueException("Invalid protocol " + protocol);
        }
        if ("icmp".equalsIgnoreCase(protocol)) {
            if (icmpType == null || icmpCode == null) {
                throw new InvalidParameterValueException("Invalid ICMP type/code specified, icmpType = " + icmpType + ", icmpCode = " + icmpCode);
            }
            if (icmpType == -1 && icmpCode != -1) {
                throw new InvalidParameterValueException("Invalid icmp code");
            }
            if (icmpType != -1 && icmpCode == -1) {
                throw new InvalidParameterValueException("Invalid icmp code: need non-negative icmp code ");
            }
            if (icmpCode > 255 || icmpType > 255 || icmpCode < -1 || icmpType < -1) {
                throw new InvalidParameterValueException("Invalid icmp type/code ");
            }
            startPortOrType = icmpType;
            endPortOrCode = icmpCode;
        } else if (protocol.equals(NetUtils.ALL_PROTO)) {
            if (startPort != null || endPort != null) {
                throw new InvalidParameterValueException("Cannot specify startPort or endPort without specifying protocol");
            }
            startPortOrType = 0;
            endPortOrCode = 0;
        } else {
            if (startPort == null || endPort == null) {
                throw new InvalidParameterValueException("Invalid port range specified, startPort = " + startPort + ", endPort = " + endPort);
            }
            if (startPort == 0 && endPort == 0) {
                endPort = 65535;
            }
            if (startPort > endPort) {
                throw new InvalidParameterValueException("Invalid port range " + startPort + ":" + endPort);
            }
            if (startPort > 65535 || endPort > 65535 || startPort < -1 || endPort < -1) {
                throw new InvalidParameterValueException("Invalid port numbers " + startPort + ":" + endPort);
            }

            if (startPort < 0 || endPort < 0) {
                throw new InvalidParameterValueException("Invalid port range " + startPort + ":" + endPort);
            }
            startPortOrType = startPort;
            endPortOrCode = endPort;
        }

        protocol = protocol.toLowerCase();

        final List<SecurityGroupVO> authorizedGroups = new ArrayList<>();
        if (groupList != null) {
            final Collection userGroupCollection = groupList.values();
            final Iterator iter = userGroupCollection.iterator();
            while (iter.hasNext()) {
                final HashMap userGroup = (HashMap) iter.next();
                final String group = (String) userGroup.get("group");
                final String authorizedAccountName = (String) userGroup.get("account");

                if (group == null || authorizedAccountName == null) {
                    throw new InvalidParameterValueException(
                            "Invalid user group specified, fields 'group' and 'account' cannot be null, please specify groups in the form:  userGroupList[0]" +
                                    ".group=XXX&userGroupList[0].account=YYY");
                }

                final Account authorizedAccount = _accountDao.findActiveAccount(authorizedAccountName, domainId);
                if (authorizedAccount == null) {
                    throw new InvalidParameterValueException("Nonexistent account: " + authorizedAccountName + " when trying to authorize security group rule  for "
                            + securityGroupId + ":" + protocol + ":" + startPortOrType + ":" + endPortOrCode);
                }

                final SecurityGroupVO groupVO = _securityGroupDao.findByAccountAndName(authorizedAccount.getId(), group);
                if (groupVO == null) {
                    throw new InvalidParameterValueException("Nonexistent group " + group + " for account " + authorizedAccountName + "/" + domainId
                            + " is given, unable to authorize security group rule.");
                }

                // Check permissions
                if (domainId != groupVO.getDomainId()) {
                    throw new PermissionDeniedException("Can't add security group id=" + groupVO.getDomainId() + " as it belongs to different domain");
                }

                authorizedGroups.add(groupVO);
            }
        }

        final Set<SecurityGroupVO> authorizedGroups2 = new TreeSet<>(new SecurityGroupVOComparator());

        authorizedGroups2.addAll(authorizedGroups); // Ensure we don't re-lock the same row

        final Integer startPortOrTypeFinal = startPortOrType;
        final Integer endPortOrCodeFinal = endPortOrCode;
        final String protocolFinal = protocol;
        final List<SecurityGroupRuleVO> newRules = Transaction.execute(new TransactionCallback<List<SecurityGroupRuleVO>>() {
            @Override
            public List<SecurityGroupRuleVO> doInTransaction(final TransactionStatus status) {
                // Prevents other threads/management servers from creating duplicate security rules
                final SecurityGroup securityGroup = _securityGroupDao.acquireInLockTable(securityGroupId);
                if (securityGroup == null) {
                    s_logger.warn("Could not acquire lock on network security group: id= " + securityGroupId);
                    return null;
                }
                final List<SecurityGroupRuleVO> newRules = new ArrayList<>();
                try {
                    for (final SecurityGroupVO ngVO : authorizedGroups2) {
                        final Long ngId = ngVO.getId();
                        // Don't delete the referenced group from under us
                        if (ngVO.getId() != securityGroup.getId()) {
                            final SecurityGroupVO tmpGrp = _securityGroupDao.lockRow(ngId, false);
                            if (tmpGrp == null) {
                                s_logger.warn("Failed to acquire lock on security group: " + ngId);
                                throw new ConcurrentAccessException("Failed to acquire lock on security group: " + ngId);
                            }
                        }
                        SecurityGroupRuleVO securityGroupRule = _securityGroupRuleDao.findByProtoPortsAndAllowedGroupId(securityGroup.getId(), protocolFinal, startPortOrTypeFinal,
                                endPortOrCodeFinal, ngVO.getId());
                        if (securityGroupRule != null && securityGroupRule.getRuleType() == ruleType) {
                            continue; // rule already exists.
                        }
                        securityGroupRule = new SecurityGroupRuleVO(ruleType, securityGroup.getId(), startPortOrTypeFinal, endPortOrCodeFinal, protocolFinal, ngVO.getId());
                        securityGroupRule = _securityGroupRuleDao.persist(securityGroupRule);
                        newRules.add(securityGroupRule);
                    }
                    if (cidrList != null) {
                        for (final String cidr : cidrList) {
                            SecurityGroupRuleVO securityGroupRule = _securityGroupRuleDao.findByProtoPortsAndCidr(securityGroup.getId(), protocolFinal, startPortOrTypeFinal,
                                    endPortOrCodeFinal, cidr);
                            if (securityGroupRule != null && securityGroupRule.getRuleType() == ruleType) {
                                continue;
                            }
                            securityGroupRule = new SecurityGroupRuleVO(ruleType, securityGroup.getId(), startPortOrTypeFinal, endPortOrCodeFinal, protocolFinal, cidr);
                            securityGroupRule = _securityGroupRuleDao.persist(securityGroupRule);
                            newRules.add(securityGroupRule);
                        }
                    }
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Added " + newRules.size() + " rules to security group " + securityGroup.getName());
                    }
                    return newRules;
                } catch (final Exception e) {
                    s_logger.warn("Exception caught when adding security group rules ", e);
                    throw new CloudRuntimeException("Exception caught when adding security group rules", e);
                } finally {
                    if (securityGroup != null) {
                        _securityGroupDao.releaseFromLockTable(securityGroup.getId());
                    }
                }
            }
        });

        try {
            final ArrayList<Long> affectedVms = new ArrayList<>();
            affectedVms.addAll(_securityGroupVMMapDao.listVmIdsBySecurityGroup(securityGroup.getId()));
            scheduleRulesetUpdateToHosts(affectedVms, true, null);
        } catch (final Exception e) {
            s_logger.debug("can't update rules on host, ignore", e);
        }

        return newRules;
    }

    private boolean revokeSecurityGroupRule(final Long id, final SecurityRuleType type) {
        // input validation
        final Account caller = CallContext.current().getCallingAccount();

        final SecurityGroupRuleVO rule = _securityGroupRuleDao.findById(id);
        if (rule == null) {
            s_logger.debug("Unable to find security rule with id " + id);
            throw new InvalidParameterValueException("Unable to find security rule with id " + id);
        }

        // check type
        if (type != rule.getRuleType()) {
            s_logger.debug("Mismatch in rule type for security rule with id " + id);
            throw new InvalidParameterValueException("Mismatch in rule type for security rule with id " + id);
        }

        // Check permissions
        final SecurityGroup securityGroup = _securityGroupDao.findById(rule.getSecurityGroupId());
        _accountMgr.checkAccess(caller, AccessType.OperateEntry, true, securityGroup);

        final long securityGroupId = rule.getSecurityGroupId();
        final Boolean result = Transaction.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(final TransactionStatus status) {
                SecurityGroupVO groupHandle = null;

                try {
                    // acquire lock on parent group (preserving this logic)
                    groupHandle = _securityGroupDao.acquireInLockTable(rule.getSecurityGroupId());
                    if (groupHandle == null) {
                        s_logger.warn("Could not acquire lock on security group id: " + rule.getSecurityGroupId());
                        return false;
                    }

                    _securityGroupRuleDao.remove(id);
                    s_logger.debug("revokeSecurityGroupRule succeeded for security rule id: " + id);

                    return true;
                } catch (final Exception e) {
                    s_logger.warn("Exception caught when deleting security rules ", e);
                    throw new CloudRuntimeException("Exception caught when deleting security rules", e);
                } finally {
                    if (groupHandle != null) {
                        _securityGroupDao.releaseFromLockTable(groupHandle.getId());
                    }
                }
            }
        });

        try {
            final ArrayList<Long> affectedVms = new ArrayList<>();
            affectedVms.addAll(_securityGroupVMMapDao.listVmIdsBySecurityGroup(securityGroupId));
            scheduleRulesetUpdateToHosts(affectedVms, true, null);
        } catch (final Exception e) {
            s_logger.debug("Can't update rules for host, ignore", e);
        }

        return result;
    }

    @DB
    public void scheduleRulesetUpdateToHosts(final List<Long> affectedVms, final boolean updateSeqno, Long delayMs) {
        if (affectedVms.size() == 0) {
            return;
        }

        if (delayMs == null) {
            delayMs = new Long(100l);
        }

        Collections.sort(affectedVms);
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Security Group Mgr: scheduling ruleset updates for " + affectedVms.size() + " vms");
        }
        final boolean locked = _workLock.lock(_globalWorkLockTimeout);
        if (!locked) {
            s_logger.warn("Security Group Mgr: failed to acquire global work lock");
            return;
        }

        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Security Group Mgr: acquired global work lock");
        }

        try {
            Transaction.execute(new TransactionCallbackNoReturn() {
                @Override
                public void doInTransactionWithoutResult(final TransactionStatus status) {
                    for (final Long vmId : affectedVms) {
                        if (s_logger.isTraceEnabled()) {
                            s_logger.trace("Security Group Mgr: scheduling ruleset update for " + vmId);
                        }
                        VmRulesetLogVO log = null;
                        SecurityGroupWorkVO work = null;

                        log = _rulesetLogDao.findByVmId(vmId);
                        if (log == null) {
                            log = new VmRulesetLogVO(vmId);
                            log = _rulesetLogDao.persist(log);
                        }

                        if (log != null && updateSeqno) {
                            log.incrLogsequence();
                            _rulesetLogDao.update(log.getId(), log);
                        }
                        work = _workDao.findByVmIdStep(vmId, Step.Scheduled);
                        if (work == null) {
                            work = new SecurityGroupWorkVO(vmId, null, null, SecurityGroupWork.Step.Scheduled, null);
                            work = _workDao.persist(work);
                            if (s_logger.isTraceEnabled()) {
                                s_logger.trace("Security Group Mgr: created new work item for " + vmId + "; id = " + work.getId());
                            }
                        }

                        work.setLogsequenceNumber(log.getLogsequence());
                        _workDao.update(work.getId(), work);
                    }
                }
            });

            for (final Long vmId : affectedVms) {
                _executorPool.schedule(new WorkerThread(), delayMs, TimeUnit.MILLISECONDS);
            }
        } finally {
            _workLock.unlock();
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Security Group Mgr: released global work lock");
            }
        }
    }

    @Override
    public SecurityGroupVO createSecurityGroup(final String name, final String description, final Long domainId, final Long accountId, final String accountName) {
        SecurityGroupVO group = _securityGroupDao.findByAccountAndName(accountId, name);
        if (group == null) {
            group = new SecurityGroupVO(name, description, domainId, accountId);
            group = _securityGroupDao.persist(group);
            s_logger.debug("Created security group " + group + " for account id=" + accountId);
        } else {
            s_logger.debug("Returning existing security group " + group + " for account id=" + accountId);
        }

        return group;
    }

    @Override
    public SecurityGroupVO createDefaultSecurityGroup(final Long accountId) {
        final SecurityGroupVO groupVO = _securityGroupDao.findByAccountAndName(accountId, SecurityGroupManager.DEFAULT_GROUP_NAME);
        if (groupVO == null) {
            final Account accVO = _accountDao.findById(accountId);
            if (accVO != null) {
                return createSecurityGroup(SecurityGroupManager.DEFAULT_GROUP_NAME, SecurityGroupManager.DEFAULT_GROUP_DESCRIPTION, accVO.getDomainId(), accVO.getId(),
                        accVO.getAccountName());
            }
        }
        return groupVO;
    }

    @Override
    @DB
    public boolean addInstanceToGroups(final Long userVmId, final List<Long> groups) {
        if (!isVmSecurityGroupEnabled(userVmId)) {
            s_logger.trace("User vm " + userVmId + " is not security group enabled, not adding it to security group");
            return false;
        }
        if (groups != null && !groups.isEmpty()) {
            return Transaction.execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(final TransactionStatus status) {
                    final UserVm userVm = _userVMDao.acquireInLockTable(userVmId); // ensures that duplicate entries are not created.
                    final List<SecurityGroupVO> sgs = new ArrayList<>();
                    for (final Long sgId : groups) {
                        sgs.add(_securityGroupDao.findById(sgId));
                    }
                    final Set<SecurityGroupVO> uniqueGroups = new TreeSet<>(new SecurityGroupVOComparator());
                    uniqueGroups.addAll(sgs);
                    if (userVm == null) {
                        s_logger.warn("Failed to acquire lock on user vm id=" + userVmId);
                    }
                    try {
                        for (final SecurityGroupVO securityGroup : uniqueGroups) {
                            // don't let the group be deleted from under us.
                            final SecurityGroupVO ngrpLock = _securityGroupDao.lockRow(securityGroup.getId(), false);
                            if (ngrpLock == null) {
                                s_logger.warn("Failed to acquire lock on network group id=" + securityGroup.getId() + " name=" + securityGroup.getName());
                                throw new ConcurrentModificationException("Failed to acquire lock on network group id=" + securityGroup.getId() + " name="
                                        + securityGroup.getName());
                            }
                            if (_securityGroupVMMapDao.findByVmIdGroupId(userVmId, securityGroup.getId()) == null) {
                                final SecurityGroupVMMapVO groupVmMapVO = new SecurityGroupVMMapVO(securityGroup.getId(), userVmId);
                                _securityGroupVMMapDao.persist(groupVmMapVO);
                            }
                        }
                        return true;
                    } finally {
                        if (userVm != null) {
                            _userVMDao.releaseFromLockTable(userVmId);
                        }
                    }
                }
            });
        }
        return false;
    }

    @Override
    @DB
    public void removeInstanceFromGroups(final long userVmId) {
        if (_securityGroupVMMapDao.countSGForVm(userVmId) < 1) {
            s_logger.trace("No security groups found for vm id=" + userVmId + ", returning");
            return;
        }
        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                final UserVm userVm = _userVMDao.acquireInLockTable(userVmId); // ensures that duplicate entries are not created in
                // addInstance
                if (userVm == null) {
                    s_logger.warn("Failed to acquire lock on user vm id=" + userVmId);
                }
                final int n = _securityGroupVMMapDao.deleteVM(userVmId);
                s_logger.info("Disassociated " + n + " network groups " + " from uservm " + userVmId);
                _userVMDao.releaseFromLockTable(userVmId);
            }
        });
        s_logger.debug("Security group mappings are removed successfully for vm id=" + userVmId);
    }

    @Override
    public void fullSync(final long agentId, final HashMap<String, Pair<Long, Long>> newGroupStates) {
        final ArrayList<Long> affectedVms = new ArrayList<>();
        for (final String vmName : newGroupStates.keySet()) {
            final Long vmId = newGroupStates.get(vmName).first();
            final Long seqno = newGroupStates.get(vmName).second();

            final VmRulesetLogVO log = _rulesetLogDao.findByVmId(vmId);
            if (log != null && log.getLogsequence() != seqno) {
                affectedVms.add(vmId);
            }
        }
        if (affectedVms.size() > 0) {
            s_logger.info("Network Group full sync for agent " + agentId + " found " + affectedVms.size() + " vms out of sync");
            scheduleRulesetUpdateToHosts(affectedVms, false, null);
        }
    }

    @Override
    public String getSecurityGroupsNamesForVm(final long vmId) {
        try {
            final List<SecurityGroupVMMapVO> networkGroupsToVmMap = _securityGroupVMMapDao.listByInstanceId(vmId);
            int size = 0;
            int j = 0;
            final StringBuilder networkGroupNames = new StringBuilder();

            if (networkGroupsToVmMap != null) {
                size = networkGroupsToVmMap.size();

                for (final SecurityGroupVMMapVO nG : networkGroupsToVmMap) {
                    // get the group id and look up for the group name
                    final SecurityGroupVO currentNetworkGroup = _securityGroupDao.findById(nG.getSecurityGroupId());
                    networkGroupNames.append(currentNetworkGroup.getName());

                    if (j < size - 1) {
                        networkGroupNames.append(",");
                        j++;
                    }
                }
            }

            return networkGroupNames.toString();
        } catch (final Exception e) {
            s_logger.warn("Error trying to get network groups for a vm: " + e);
            return null;
        }
    }

    @Override
    public List<SecurityGroupVO> getSecurityGroupsForVm(final long vmId) {
        final List<SecurityGroupVMMapVO> securityGroupsToVmMap = _securityGroupVMMapDao.listByInstanceId(vmId);
        final List<SecurityGroupVO> secGrps = new ArrayList<>();
        if (securityGroupsToVmMap != null && securityGroupsToVmMap.size() > 0) {
            for (final SecurityGroupVMMapVO sG : securityGroupsToVmMap) {
                final SecurityGroupVO currSg = _securityGroupDao.findById(sG.getSecurityGroupId());
                secGrps.add(currSg);
            }
        }
        return secGrps;
    }

    @Override
    public boolean isVmSecurityGroupEnabled(final Long vmId) {
        final VirtualMachine vm = _vmDao.findByIdIncludingRemoved(vmId);
        final List<NicProfile> nics = _networkMgr.getNicProfiles(vm);
        for (final NicProfile nic : nics) {
            final Network network = _networkModel.getNetwork(nic.getNetworkId());
            if (_networkModel.isSecurityGroupSupportedInNetwork(network)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public SecurityGroupVO getDefaultSecurityGroup(final long accountId) {
        return _securityGroupDao.findByAccountAndName(accountId, DEFAULT_GROUP_NAME);
    }

    @Override
    public SecurityGroup getSecurityGroup(final String name, final long accountId) {
        return _securityGroupDao.findByAccountAndName(accountId, name);
    }

    @Override
    public boolean isVmMappedToDefaultSecurityGroup(final long vmId) {
        final UserVmVO vm = _userVmMgr.getVirtualMachine(vmId);
        final SecurityGroup defaultGroup = getDefaultSecurityGroup(vm.getAccountId());
        if (defaultGroup == null) {
            s_logger.warn("Unable to find default security group for account id=" + vm.getAccountId());
            return false;
        }
        final SecurityGroupVMMapVO map = _securityGroupVMMapDao.findByVmIdGroupId(vmId, defaultGroup.getId());
        if (map == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {

        final Map<String, String> configs = _configDao.getConfiguration("Network", params);
        _numWorkerThreads = NumbersUtil.parseInt(configs.get(Config.SecurityGroupWorkerThreads.key()), WORKER_THREAD_COUNT);
        _timeBetweenCleanups = NumbersUtil.parseInt(configs.get(Config.SecurityGroupWorkCleanupInterval.key()), TIME_BETWEEN_CLEANUPS);
        _globalWorkLockTimeout = NumbersUtil.parseInt(configs.get(Config.SecurityGroupWorkGlobalLockTimeout.key()), 300);
    /* register state listener, no matter security group is enabled or not */
        VirtualMachine.State.getStateMachine().registerListener(this);

        _answerListener = new SecurityGroupListener(this, _agentMgr, _workDao);
        _agentMgr.registerForHostEvents(_answerListener, true, true, true);

        _serverId = ManagementServerNode.getManagementServerId();

        s_logger.info("SecurityGroupManager: num worker threads=" + _numWorkerThreads + ", time between cleanups=" + _timeBetweenCleanups + " global lock timeout="
                + _globalWorkLockTimeout);
        createThreadPools();

        return true;
    }

    protected void createThreadPools() {
        _executorPool = Executors.newScheduledThreadPool(_numWorkerThreads, new NamedThreadFactory("NWGRP"));
        _cleanupExecutor = Executors.newScheduledThreadPool(1, new NamedThreadFactory("NWGRP-Cleanup"));
    }

    @Override
    public boolean start() {
        _cleanupExecutor.scheduleAtFixedRate(new CleanupThread(), _timeBetweenCleanups, _timeBetweenCleanups, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @DB
    public void work() {
        if (s_logger.isTraceEnabled()) {
            s_logger.trace("Checking the database");
        }
        final SecurityGroupWorkVO work = _workDao.take(_serverId);
        if (work == null) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Security Group work: no work found");
            }
            return;
        }
        final Long userVmId = work.getInstanceId();
        if (work.getStep() == Step.Done) {
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Security Group work: found a job in done state, rescheduling for vm: " + userVmId);
            }
            final ArrayList<Long> affectedVms = new ArrayList<>();
            affectedVms.add(userVmId);
            scheduleRulesetUpdateToHosts(affectedVms, false, _timeBetweenCleanups * 1000l);
            return;
        }
        s_logger.debug("Working on " + work);

        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {
                UserVm vm = null;
                Long seqnum = null;

                boolean locked = false;
                try {
                    vm = _userVMDao.acquireInLockTable(work.getInstanceId());
                    if (vm == null) {
                        vm = _userVMDao.findById(work.getInstanceId());
                        if (vm == null) {
                            s_logger.info("VM " + work.getInstanceId() + " is removed");
                            locked = true;
                            return;
                        }
                        s_logger.warn("Unable to acquire lock on vm id=" + userVmId);
                        return;
                    }
                    locked = true;
                    Long agentId = null;
                    final VmRulesetLogVO log = _rulesetLogDao.findByVmId(userVmId);
                    if (log == null) {
                        s_logger.warn("Cannot find log record for vm id=" + userVmId);
                        return;
                    }
                    seqnum = log.getLogsequence();

                    if (vm != null && vm.getState() == State.Running) {
                        final Map<PortAndProto, Set<String>> ingressRules = generateRulesForVM(userVmId, SecurityRuleType.IngressRule);
                        final Map<PortAndProto, Set<String>> egressRules = generateRulesForVM(userVmId, SecurityRuleType.EgressRule);
                        agentId = vm.getHostId();
                        if (agentId != null) {
                            // get nic secondary ip address
                            final String privateIp = vm.getPrivateIpAddress();
                            final NicVO nic = _nicDao.findByIp4AddressAndVmId(privateIp, vm.getId());
                            List<String> nicSecIps = null;
                            if (nic != null) {
                                if (nic.getSecondaryIp()) {
                                    nic.getNetworkId();
                                    nicSecIps = _nicSecIpDao.getSecondaryIpAddressesForNic(nic.getId());
                                }
                            }
                            final SecurityGroupRulesCmd cmd = generateRulesetCmd(vm.getInstanceName(), vm.getPrivateIpAddress(), vm.getPrivateMacAddress(), vm.getId(),
                                    generateRulesetSignature(ingressRules, egressRules), seqnum, ingressRules, egressRules, nicSecIps);
                            final Commands cmds = new Commands(cmd);
                            try {
                                _agentMgr.send(agentId, cmds, _answerListener);
                            } catch (final AgentUnavailableException e) {
                                s_logger.debug("Unable to send ingress rules updates for vm: " + userVmId + "(agentid=" + agentId + ")");
                                _workDao.updateStep(work.getInstanceId(), seqnum, Step.Done);
                            }
                        }
                    }
                } finally {
                    if (locked) {
                        _userVMDao.releaseFromLockTable(userVmId);
                        _workDao.updateStep(work.getId(), Step.Done);
                    }
                }
            }
        });
    }

    protected Map<PortAndProto, Set<String>> generateRulesForVM(final Long userVmId, final SecurityRuleType type) {

        final Map<PortAndProto, Set<String>> allowed = new TreeMap<>();

        final List<SecurityGroupVMMapVO> groupsForVm = _securityGroupVMMapDao.listByInstanceId(userVmId);
        for (final SecurityGroupVMMapVO mapVO : groupsForVm) {
            final List<SecurityGroupRuleVO> rules = _securityGroupRuleDao.listBySecurityGroupId(mapVO.getSecurityGroupId(), type);
            for (final SecurityGroupRuleVO rule : rules) {
                final PortAndProto portAndProto = new PortAndProto(rule.getProtocol(), rule.getStartPort(), rule.getEndPort());
                Set<String> cidrs = allowed.get(portAndProto);
                if (cidrs == null) {
                    cidrs = new TreeSet<>(new CidrComparator());
                }
                if (rule.getAllowedNetworkId() != null) {
                    final List<SecurityGroupVMMapVO> allowedInstances = _securityGroupVMMapDao.listBySecurityGroup(rule.getAllowedNetworkId(), State.Running);
                    for (final SecurityGroupVMMapVO ngmapVO : allowedInstances) {
                        final Nic defaultNic = _networkModel.getDefaultNic(ngmapVO.getInstanceId());
                        if (defaultNic != null) {
                            String cidr = defaultNic.getIPv4Address();
                            cidr = cidr + "/32";
                            cidrs.add(cidr);
                        }
                    }
                } else if (rule.getAllowedSourceIpCidr() != null) {
                    cidrs.add(rule.getAllowedSourceIpCidr());
                }
                if (cidrs.size() > 0) {
                    allowed.put(portAndProto, cidrs);
                }
            }
        }

        return allowed;
    }

    protected SecurityGroupRulesCmd generateRulesetCmd(final String vmName, final String guestIp, final String guestMac, final Long vmId, final String signature, final long seqnum,
                                                       final Map<PortAndProto, Set<String>> ingressRules, final Map<PortAndProto, Set<String>> egressRules, final List<String>
                                                               secIps) {
        final List<IpPortAndProto> ingressResult = new ArrayList<>();
        final List<IpPortAndProto> egressResult = new ArrayList<>();
        for (final PortAndProto pAp : ingressRules.keySet()) {
            final Set<String> cidrs = ingressRules.get(pAp);
            if (cidrs.size() > 0) {
                final IpPortAndProto ipPortAndProto = new SecurityGroupRulesCmd.IpPortAndProto(pAp.getProto(), pAp.getStartPort(), pAp.getEndPort(), cidrs.toArray(new String[cidrs
                        .size()]));
                ingressResult.add(ipPortAndProto);
            }
        }
        for (final PortAndProto pAp : egressRules.keySet()) {
            final Set<String> cidrs = egressRules.get(pAp);
            if (cidrs.size() > 0) {
                final IpPortAndProto ipPortAndProto = new SecurityGroupRulesCmd.IpPortAndProto(pAp.getProto(), pAp.getStartPort(), pAp.getEndPort(), cidrs.toArray(new String[cidrs
                        .size()]));
                egressResult.add(ipPortAndProto);
            }
        }
        return new SecurityGroupRulesCmd(guestIp, guestMac, vmName, vmId, signature, seqnum, ingressResult.toArray(new IpPortAndProto[ingressResult.size()]),
                egressResult.toArray(new IpPortAndProto[egressResult.size()]), secIps);
    }

    protected String generateRulesetSignature(final Map<PortAndProto, Set<String>> ingress, final Map<PortAndProto, Set<String>> egress) {
        String ruleset = ingress.toString();
        ruleset = ruleset.concat(egress.toString());
        return DigestUtils.md5Hex(ruleset);
    }

    private Pair<List<SecurityGroupJoinVO>, Integer> listSecurityGroupRulesByVM(final long vmId, final long pageInd, final long pageSize) {
        final Filter sf = new Filter(SecurityGroupVMMapVO.class, null, true, pageInd, pageSize);
        final Pair<List<SecurityGroupVMMapVO>, Integer> sgVmMappingPair = _securityGroupVMMapDao.listByInstanceId(vmId, sf);
        final Integer count = sgVmMappingPair.second();
        if (count.intValue() == 0) {
            // handle empty result cases
            return new Pair<>(new ArrayList<>(), count);
        }
        final List<SecurityGroupVMMapVO> sgVmMappings = sgVmMappingPair.first();
        final Long[] sgIds = new Long[sgVmMappings.size()];
        int i = 0;
        for (final SecurityGroupVMMapVO sgVm : sgVmMappings) {
            sgIds[i++] = sgVm.getSecurityGroupId();
        }
        final List<SecurityGroupJoinVO> sgs = _securityGroupJoinDao.searchByIds(sgIds);
        return new Pair<>(sgs, count);
    }

    public void cleanupFinishedWork() {
        final Date before = new Date(System.currentTimeMillis() - 6 * 3600 * 1000l);
        final int numDeleted = _workDao.deleteFinishedWork(before);
        if (numDeleted > 0) {
            s_logger.info("Network Group Work cleanup deleted " + numDeleted + " finished work items older than " + before.toString());
        }
    }

    private void cleanupUnfinishedWork() {
        final Date before = new Date(System.currentTimeMillis() - 2 * _timeBetweenCleanups * 1000l);
        final List<SecurityGroupWorkVO> unfinished = _workDao.findUnfinishedWork(before);
        if (unfinished.size() > 0) {
            s_logger.info("Network Group Work cleanup found " + unfinished.size() + " unfinished work items older than " + before.toString());
            final ArrayList<Long> affectedVms = new ArrayList<>();
            for (final SecurityGroupWorkVO work : unfinished) {
                affectedVms.add(work.getInstanceId());
                work.setStep(Step.Error);
                _workDao.update(work.getId(), work);
            }
            scheduleRulesetUpdateToHosts(affectedVms, false, null);
        } else {
            s_logger.debug("Network Group Work cleanup found no unfinished work items older than " + before.toString());
        }
    }

    private void processScheduledWork() {
        final List<SecurityGroupWorkVO> scheduled = _workDao.findScheduledWork();
        final int numJobs = scheduled.size();
        if (numJobs > 0) {
            s_logger.debug("Security group work: found scheduled jobs " + numJobs);
            final Random rand = new Random();
            for (int i = 0; i < numJobs; i++) {
                final long delayMs = 100 + 10 * rand.nextInt(numJobs);
                _executorPool.schedule(new WorkerThread(), delayMs, TimeUnit.MILLISECONDS);
            }
        }
    }

    @Override
    public boolean preStateTransitionEvent(final State oldState, final Event event, final State newState, final VirtualMachine vo, final boolean status, final Object opaque) {
        return true;
    }

    @Override
    public boolean postStateTransitionEvent(final StateMachine2.Transition<State, Event> transition, final VirtualMachine vm, final boolean status, final Object opaque) {
        if (!status) {
            return false;
        }

        final State oldState = transition.getCurrentState();
        final State newState = transition.getToState();
        final Event event = transition.getEvent();
        if (VirtualMachine.State.isVmStarted(oldState, event, newState)) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Security Group Mgr: handling start of vm id" + vm.getId());
            }
            handleVmStarted((VMInstanceVO) vm);
        } else if (VirtualMachine.State.isVmStopped(oldState, event, newState)) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Security Group Mgr: handling stop of vm id" + vm.getId());
            }
            handleVmStopped((VMInstanceVO) vm);
        } else if (VirtualMachine.State.isVmMigrated(oldState, event, newState)) {
            if (s_logger.isTraceEnabled()) {
                s_logger.trace("Security Group Mgr: handling migration of vm id" + vm.getId());
            }
            handleVmMigrated((VMInstanceVO) vm);
        }

        return true;
    }

    public void handleVmStarted(final VMInstanceVO vm) {
        if (vm.getType() != VirtualMachine.Type.User || !isVmSecurityGroupEnabled(vm.getId())) {
            return;
        }
        final List<Long> affectedVms = getAffectedVmsForVmStart(vm);
        scheduleRulesetUpdateToHosts(affectedVms, true, null);
    }

    protected void handleVmStopped(final VMInstanceVO vm) {
        if (vm.getType() != VirtualMachine.Type.User || !isVmSecurityGroupEnabled(vm.getId())) {
            return;
        }
        final List<Long> affectedVms = getAffectedVmsForVmStop(vm);
        scheduleRulesetUpdateToHosts(affectedVms, true, null);
    }

    protected void handleVmMigrated(final VMInstanceVO vm) {
        if (!isVmSecurityGroupEnabled(vm.getId())) {
            return;
        }
        if (vm.getType() != VirtualMachine.Type.User) {
            Commands cmds = null;
            final NetworkRulesSystemVmCommand nrc = new NetworkRulesSystemVmCommand(vm.getInstanceName(), vm.getType());
            cmds = new Commands(nrc);
            try {
                _agentMgr.send(vm.getHostId(), cmds);
            } catch (final AgentUnavailableException e) {
                s_logger.debug(e.toString());
            } catch (final OperationTimedoutException e) {
                s_logger.debug(e.toString());
            }
        } else {
            final List<Long> affectedVms = new ArrayList<>();
            affectedVms.add(vm.getId());
            scheduleRulesetUpdateToHosts(affectedVms, true, null);
        }
    }

    protected List<Long> getAffectedVmsForVmStart(final VMInstanceVO vm) {
        final List<Long> affectedVms = new ArrayList<>();
        affectedVms.add(vm.getId());
        final List<SecurityGroupVMMapVO> groupsForVm = _securityGroupVMMapDao.listByInstanceId(vm.getId());
        // For each group, find the security rules that allow the group
        for (final SecurityGroupVMMapVO mapVO : groupsForVm) {// FIXME: use custom sql in the dao
            //Add usage events for security group assign
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_SECURITY_GROUP_ASSIGN, vm.getAccountId(), vm.getDataCenterId(), vm.getId(), mapVO.getSecurityGroupId(), vm
                    .getClass().getName(), vm.getUuid());

            final List<SecurityGroupRuleVO> allowingRules = _securityGroupRuleDao.listByAllowedSecurityGroupId(mapVO.getSecurityGroupId());
            // For each security rule that allows a group that the vm belongs to, find the group it belongs to
            affectedVms.addAll(getAffectedVmsForSecurityRules(allowingRules));
        }
        return affectedVms;
    }

    protected List<Long> getAffectedVmsForVmStop(final VMInstanceVO vm) {
        final List<Long> affectedVms = new ArrayList<>();
        final List<SecurityGroupVMMapVO> groupsForVm = _securityGroupVMMapDao.listByInstanceId(vm.getId());
        // For each group, find the security rules rules that allow the group
        for (final SecurityGroupVMMapVO mapVO : groupsForVm) {// FIXME: use custom sql in the dao
            //Add usage events for security group remove
            UsageEventUtils.publishUsageEvent(EventTypes.EVENT_SECURITY_GROUP_REMOVE, vm.getAccountId(), vm.getDataCenterId(), vm.getId(), mapVO.getSecurityGroupId(), vm
                    .getClass().getName(), vm.getUuid());

            final List<SecurityGroupRuleVO> allowingRules = _securityGroupRuleDao.listByAllowedSecurityGroupId(mapVO.getSecurityGroupId());
            // For each security rule that allows a group that the vm belongs to, find the group it belongs to
            affectedVms.addAll(getAffectedVmsForSecurityRules(allowingRules));
        }
        return affectedVms;
    }

    protected List<Long> getAffectedVmsForSecurityRules(final List<SecurityGroupRuleVO> allowingRules) {
        final Set<Long> distinctGroups = new HashSet<>();
        final List<Long> affectedVms = new ArrayList<>();

        for (final SecurityGroupRuleVO allowingRule : allowingRules) {
            distinctGroups.add(allowingRule.getSecurityGroupId());
        }
        for (final Long groupId : distinctGroups) {
            // allVmUpdates.putAll(generateRulesetForGroupMembers(groupId));
            affectedVms.addAll(_securityGroupVMMapDao.listVmIdsBySecurityGroup(groupId));
        }
        return affectedVms;
    }

    public static class PortAndProto implements Comparable<PortAndProto> {
        String proto;
        int startPort;
        int endPort;

        public PortAndProto(final String proto, final int startPort, final int endPort) {
            this.proto = proto;
            this.startPort = startPort;
            this.endPort = endPort;
        }

        public String getProto() {
            return proto;
        }

        public int getStartPort() {
            return startPort;
        }

        public int getEndPort() {
            return endPort;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + endPort;
            result = prime * result + (proto == null ? 0 : proto.hashCode());
            result = prime * result + startPort;
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PortAndProto other = (PortAndProto) obj;
            if (endPort != other.endPort) {
                return false;
            }
            if (proto == null) {
                if (other.proto != null) {
                    return false;
                }
            } else if (!proto.equals(other.proto)) {
                return false;
            }
            if (startPort != other.startPort) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(final PortAndProto obj) {
            if (this == obj) {
                return 0;
            }
            if (obj == null) {
                return 1;
            }
            if (proto == null) {
                if (obj.proto != null) {
                    return -1;
                } else {
                    return 0;
                }
            }
            if (!obj.proto.equalsIgnoreCase(proto)) {
                return proto.compareTo(obj.proto);
            }
            if (startPort < obj.startPort) {
                return -1;
            } else if (startPort > obj.startPort) {
                return 1;
            }

            if (endPort < obj.endPort) {
                return -1;
            } else if (endPort > obj.endPort) {
                return 1;
            }

            return 0;
        }
    }

    public static class CidrComparator implements Comparator<String> {

        @Override
        public int compare(final String cidr1, final String cidr2) {
            // parse both to find significance first (low number of bits is high)
            // if equal then just do a string compare
            if (significance(cidr1) == significance(cidr2)) {
                return cidr1.compareTo(cidr2);
            } else {
                return significance(cidr2) - significance(cidr1);
            }
        }

        private int significance(final String cidr) {
            return Integer.parseInt(cidr.substring(cidr.indexOf('/') + 1));
        }
    }

    private final class SecurityGroupVOComparator implements Comparator<SecurityGroupVO> {
        @Override
        public int compare(final SecurityGroupVO o1, final SecurityGroupVO o2) {
            return o1.getId() == o2.getId() ? 0 : o1.getId() < o2.getId() ? -1 : 1;
        }
    }

    public class WorkerThread extends ManagedContextRunnable {
        @Override
        protected void runInContext() {
            try {
                work();
            } catch (final Throwable th) {
                s_logger.error("Problem with SG work", th);
            }
        }
    }

    public class CleanupThread extends ManagedContextRunnable {
        @Override
        protected void runInContext() {
            try {
                cleanupFinishedWork();
                cleanupUnfinishedWork();
                //processScheduledWork();
            } catch (final Throwable th) {
                s_logger.error("Problem with SG Cleanup", th);
            }
        }
    }
}
