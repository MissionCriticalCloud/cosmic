package org.apache.cloudstack.affinity;

import com.cloud.dao.EntityManager;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.DomainManager;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;
import com.cloud.utils.component.Manager;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionCallbackNoReturn;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.fsm.StateListener;
import com.cloud.utils.fsm.StateMachine2;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Event;
import com.cloud.vm.VirtualMachine.State;
import com.cloud.vm.dao.UserVmDao;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.acl.ControlledEntity.ACLType;
import org.apache.cloudstack.acl.SecurityChecker.AccessType;
import org.apache.cloudstack.affinity.dao.AffinityGroupDao;
import org.apache.cloudstack.affinity.dao.AffinityGroupDomainMapDao;
import org.apache.cloudstack.affinity.dao.AffinityGroupVMMapDao;
import org.apache.cloudstack.api.command.user.affinitygroup.CreateAffinityGroupCmd;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.messagebus.MessageBus;
import org.apache.cloudstack.framework.messagebus.PublishScope;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AffinityGroupServiceImpl extends ManagerBase implements AffinityGroupService, Manager, StateListener<State, VirtualMachine.Event, VirtualMachine> {

    public static final Logger s_logger = LoggerFactory.getLogger(AffinityGroupServiceImpl.class);
    protected List<AffinityGroupProcessor> _affinityProcessors;
    @Inject
    AccountManager _accountMgr;

    @Inject
    AffinityGroupDao _affinityGroupDao;

    @Inject
    AffinityGroupVMMapDao _affinityGroupVMMapDao;

    @Inject
    AffinityGroupDomainMapDao _affinityGroupDomainMapDao;
    @Inject
    DomainDao _domainDao;
    @Inject
    DomainManager _domainMgr;
    @Inject
    MessageBus _messageBus;
    private String _name;
    @Inject
    private UserVmDao _userVmDao;

    public List<AffinityGroupProcessor> getAffinityGroupProcessors() {
        return _affinityProcessors;
    }

    public void setAffinityGroupProcessors(final List<AffinityGroupProcessor> affinityProcessors) {
        _affinityProcessors = affinityProcessors;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        _name = name;
        VirtualMachine.State.getStateMachine().registerListener(this);
        return true;
    }

    @DB
    @Override
    @ActionEvent(eventType = EventTypes.EVENT_AFFINITY_GROUP_CREATE, eventDescription = "Creating Affinity Group", create = true)
    public AffinityGroup createAffinityGroup(final CreateAffinityGroupCmd createAffinityGroupCmd) {
        return createAffinityGroup(createAffinityGroupCmd.getAccountName(), createAffinityGroupCmd.getProjectId(), createAffinityGroupCmd.getDomainId(), createAffinityGroupCmd
                .getAffinityGroupName(), createAffinityGroupCmd.getAffinityGroupType(), createAffinityGroupCmd.getDescription());
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }

    @DB
    @Override
    public AffinityGroup createAffinityGroup(final String accountName, final Long projectId, final Long domainId, final String affinityGroupName, final String affinityGroupType,
                                             final String description) {

        // validate the affinityGroupType
        final Map<String, AffinityGroupProcessor> typeProcessorMap = getAffinityTypeToProcessorMap();

        if (typeProcessorMap == null || typeProcessorMap.isEmpty()) {
            throw new InvalidParameterValueException("Unable to create affinity group, no Affinity Group Types configured");
        }

        final AffinityGroupProcessor processor = typeProcessorMap.get(affinityGroupType);

        if (processor == null) {
            throw new InvalidParameterValueException("Unable to create affinity group, invalid affinity group type" + affinityGroupType);
        }

        final Account caller = CallContext.current().getCallingAccount();
        if (processor.isAdminControlledGroup() && !_accountMgr.isRootAdmin(caller.getId())) {
            throw new PermissionDeniedException("Cannot create the affinity group");
        }

        ControlledEntity.ACLType aclType = null;
        Account owner = null;
        boolean domainLevel = false;

        if (projectId == null && domainId != null && accountName == null) {
            verifyAccessToDomainWideProcessor(caller, processor);
            final DomainVO domain = getDomain(domainId);
            _accountMgr.checkAccess(caller, domain);

            // domain level group, owner is SYSTEM.
            owner = _accountMgr.getAccount(Account.ACCOUNT_ID_SYSTEM);
            aclType = ControlledEntity.ACLType.Domain;
            domainLevel = true;
        } else {
            owner = _accountMgr.finalizeOwner(caller, accountName, domainId, projectId);
            aclType = ControlledEntity.ACLType.Account;
        }

        verifyAffinityGroupNameInUse(owner.getAccountId(), owner.getDomainId(), affinityGroupName);
        verifyDomainLevelAffinityGroupName(domainLevel, owner.getDomainId(), affinityGroupName);

        final AffinityGroupVO group = createAffinityGroup(processor, owner, aclType, affinityGroupName, affinityGroupType, description);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Created affinity group =" + affinityGroupName);
        }

        return group;
    }

    @Override
    public boolean preStateTransitionEvent(final State oldState, final Event event, final State newState, final VirtualMachine vo, final boolean status, final Object opaque) {
        return true;
    }

    @Override
    public boolean postStateTransitionEvent(final StateMachine2.Transition<State, Event> transition, final VirtualMachine vo, final boolean status, final Object opaque) {
        if (!status) {
            return false;
        }
        final State newState = transition.getToState();
        if ((newState == State.Expunging) || (newState == State.Error)) {
            // cleanup all affinity groups associations of the Expunged VM
            final SearchCriteria<AffinityGroupVMMapVO> sc = _affinityGroupVMMapDao.createSearchCriteria();
            sc.addAnd("instanceId", SearchCriteria.Op.EQ, vo.getId());
            _affinityGroupVMMapDao.expunge(sc);
        }
        return true;
    }

    private void verifyAccessToDomainWideProcessor(final Account caller, final AffinityGroupProcessor processor) {
        if (!_accountMgr.isRootAdmin(caller.getId())) {
            throw new InvalidParameterValueException("Unable to create affinity group, account name must be passed with the domainId");
        }
        if (!processor.canBeSharedDomainWide()) {
            throw new InvalidParameterValueException("Unable to create affinity group, account name is needed. Affinity group type " + processor.getType() + " cannot be shared " +
                    "domain wide");
        }
    }

    private AffinityGroupVO createAffinityGroup(final AffinityGroupProcessor processor, final Account owner, final ACLType aclType, final String affinityGroupName, final String
            affinityGroupType, final String description) {
        return Transaction.execute(new TransactionCallback<AffinityGroupVO>() {
            @Override
            public AffinityGroupVO doInTransaction(final TransactionStatus status) {
                final AffinityGroupVO group =
                        new AffinityGroupVO(affinityGroupName, affinityGroupType, description, owner.getDomainId(), owner.getId(), aclType);
                _affinityGroupDao.persist(group);

                return group;
            }
        });
    }

    private DomainVO getDomain(final Long domainId) {
        final DomainVO domain = _domainDao.findById(domainId);
        if (domain == null) {
            throw new InvalidParameterValueException("Unable to find domain by specified id");
        }
        return domain;
    }

    private void verifyAffinityGroupNameInUse(final long accountId, final long domainId, final String affinityGroupName) {
        if (_affinityGroupDao.isNameInUse(accountId, domainId, affinityGroupName)) {
            throw new InvalidParameterValueException("Unable to create affinity group, a group with name " + affinityGroupName + " already exists.");
        }
    }

    private void verifyDomainLevelAffinityGroupName(final boolean domainLevel, final long domainId, final String affinityGroupName) {
        if (domainLevel && _affinityGroupDao.findDomainLevelGroupByName(domainId, affinityGroupName) != null) {
            throw new InvalidParameterValueException("Unable to create affinity group, a group with name " + affinityGroupName + " already exists under the domain.");
        }
    }

    @DB
    @ActionEvent(eventType = EventTypes.EVENT_AFFINITY_GROUP_DELETE, eventDescription = "Deleting affinity group")
    public boolean deleteAffinityGroup(final Long affinityGroupId, final String account, final Long projectId, final Long domainId, final String affinityGroupName) {

        final AffinityGroupVO group = getAffinityGroup(affinityGroupId, account, projectId, domainId, affinityGroupName);

        // check permissions
        final Account caller = CallContext.current().getCallingAccount();
        _accountMgr.checkAccess(caller, AccessType.OperateEntry, true, group);

        final Long affinityGroupIdFinal = group.getId();
        deleteAffinityGroup(affinityGroupIdFinal);

        // remove its related ACL permission
        final Pair<Class<?>, Long> params = new Pair<>(AffinityGroup.class, affinityGroupIdFinal);
        _messageBus.publish(_name, EntityManager.MESSAGE_REMOVE_ENTITY_EVENT, PublishScope.LOCAL, params);

        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Deleted affinity group id=" + affinityGroupIdFinal);
        }
        return true;
    }

    private AffinityGroupVO getAffinityGroup(final Long affinityGroupId, final String account, final Long projectId, final Long domainId, final String affinityGroupName) {
        AffinityGroupVO group = null;
        if (affinityGroupId != null) {
            group = _affinityGroupDao.findById(affinityGroupId);
        } else if (affinityGroupName != null) {
            group = getAffinityGroupByName(account, projectId, domainId, affinityGroupName);
        } else {
            throw new InvalidParameterValueException("Either the affinity group Id or group name must be specified to delete the group");
        }
        if (group == null) {
            throw new InvalidParameterValueException("Unable to find affinity group " + (affinityGroupId == null ? affinityGroupName : affinityGroupId));
        }
        return group;
    }

    private AffinityGroupVO getAffinityGroupByName(final String account, final Long projectId, final Long domainId, final String affinityGroupName) {
        AffinityGroupVO group = null;
        if (account == null && domainId != null) {
            group = _affinityGroupDao.findDomainLevelGroupByName(domainId, affinityGroupName);
        } else {
            final Long accountId = _accountMgr.finalyzeAccountId(account, domainId, projectId, true);
            if (accountId == null) {
                final Account caller = CallContext.current().getCallingAccount();
                group = _affinityGroupDao.findByAccountAndName(caller.getAccountId(), affinityGroupName);
            } else {
                group = _affinityGroupDao.findByAccountAndName(accountId, affinityGroupName);
            }
        }
        return group;
    }

    private void deleteAffinityGroup(final Long affinityGroupId) {
        Transaction.execute(new TransactionCallbackNoReturn() {
            @Override
            public void doInTransactionWithoutResult(final TransactionStatus status) {

                final AffinityGroupVO group = _affinityGroupDao.lockRow(affinityGroupId, true);
                if (group == null) {
                    throw new InvalidParameterValueException("Unable to find affinity group by id " + affinityGroupId);
                }

                final List<AffinityGroupVMMapVO> affinityGroupVmMap = _affinityGroupVMMapDao.listByAffinityGroup(affinityGroupId);
                if (!affinityGroupVmMap.isEmpty()) {
                    final SearchBuilder<AffinityGroupVMMapVO> listByAffinityGroup = _affinityGroupVMMapDao.createSearchBuilder();
                    listByAffinityGroup.and("affinityGroupId", listByAffinityGroup.entity().getAffinityGroupId(), SearchCriteria.Op.EQ);
                    listByAffinityGroup.done();
                    final SearchCriteria<AffinityGroupVMMapVO> sc = listByAffinityGroup.create();
                    sc.setParameters("affinityGroupId", affinityGroupId);

                    _affinityGroupVMMapDao.lockRows(sc, null, true);
                    _affinityGroupVMMapDao.remove(sc);
                }

                // call processor to handle the group delete
                final AffinityGroupProcessor processor = getAffinityGroupProcessorForType(group.getType());
                if (processor != null) {
                    processor.handleDeleteGroup(group);
                }

                if (_affinityGroupDao.expunge(affinityGroupId)) {
                    final AffinityGroupDomainMapVO groupDomain = _affinityGroupDomainMapDao
                            .findByAffinityGroup(affinityGroupId);
                    if (groupDomain != null) {
                        _affinityGroupDomainMapDao.remove(groupDomain.getId());
                    }
                }
            }
        });
    }

    @Override
    public List<String> listAffinityGroupTypes() {
        final List<String> types = new ArrayList<>();

        for (final AffinityGroupProcessor processor : _affinityProcessors) {
            if (processor.isAdminControlledGroup()) {
                continue; // we dont list the type if this group can be
                // created only as an admin/system operation.
            }
            types.add(processor.getType());
        }

        return types;
    }

    protected Map<String, AffinityGroupProcessor> getAffinityTypeToProcessorMap() {
        final Map<String, AffinityGroupProcessor> typeProcessorMap = new HashMap<>();

        for (final AffinityGroupProcessor processor : _affinityProcessors) {
            typeProcessorMap.put(processor.getType(), processor);
        }

        return typeProcessorMap;
    }

    @Override
    public boolean isAdminControlledGroup(final AffinityGroup group) {

        if (group != null) {
            final String affinityGroupType = group.getType();
            final Map<String, AffinityGroupProcessor> typeProcessorMap = getAffinityTypeToProcessorMap();
            if (typeProcessorMap != null && !typeProcessorMap.isEmpty()) {
                final AffinityGroupProcessor processor = typeProcessorMap.get(affinityGroupType);
                if (processor != null) {
                    return processor.isAdminControlledGroup();
                }
            }
        }
        return false;
    }

    @Override
    public AffinityGroup getAffinityGroup(final Long groupId) {
        return _affinityGroupDao.findById(groupId);
    }

    @Override
    public UserVm updateVMAffinityGroups(final Long vmId, final List<Long> affinityGroupIds) {
        // Verify input parameters
        final UserVmVO vmInstance = _userVmDao.findById(vmId);
        if (vmInstance == null) {
            throw new InvalidParameterValueException("Unable to find a virtual machine with id " + vmId);
        }

        // Check that the VM is stopped
        if (!vmInstance.getState().equals(State.Stopped)) {
            s_logger.warn("Unable to update affinity groups of the virtual machine " + vmInstance.toString() + " in state " + vmInstance.getState());
            throw new InvalidParameterValueException("Unable update affinity groups of the virtual machine " + vmInstance.toString() + " " + "in state " +
                    vmInstance.getState() + "; make sure the virtual machine is stopped and not in an error state before updating.");
        }

        final Account caller = CallContext.current().getCallingAccount();
        final Account owner = _accountMgr.getAccount(vmInstance.getAccountId());

        // check that the affinity groups exist
        for (final Long affinityGroupId : affinityGroupIds) {
            final AffinityGroupVO ag = _affinityGroupDao.findById(affinityGroupId);
            if (ag == null) {
                throw new InvalidParameterValueException("Unable to find affinity group by id " + affinityGroupId);
            } else {
                // verify permissions
                _accountMgr.checkAccess(caller, null, true, owner, ag);
                // Root admin has access to both VM and AG by default, but make sure the
                // owner of these entities is same
                if (caller.getId() == Account.ACCOUNT_ID_SYSTEM || _accountMgr.isRootAdmin(caller.getId())) {
                    if (ag.getAccountId() != owner.getAccountId()) {
                        throw new PermissionDeniedException("Affinity Group " + ag + " does not belong to the VM's account");
                    }
                }
            }
        }
        _affinityGroupVMMapDao.updateMap(vmId, affinityGroupIds);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Updated VM :" + vmId + " affinity groups to =" + affinityGroupIds);
        }
        // APIResponseHelper will pull out the updated affinitygroups.
        return vmInstance;
    }

    @Override
    public boolean isAffinityGroupProcessorAvailable(final String affinityGroupType) {
        for (final AffinityGroupProcessor processor : _affinityProcessors) {
            if (affinityGroupType != null && affinityGroupType.equals(processor.getType())) {
                return true;
            }
        }
        return false;
    }

    private AffinityGroupProcessor getAffinityGroupProcessorForType(final String affinityGroupType) {
        for (final AffinityGroupProcessor processor : _affinityProcessors) {
            if (affinityGroupType != null && affinityGroupType.equals(processor.getType())) {
                return processor;
            }
        }
        return null;
    }

    @Override
    public boolean isAffinityGroupAvailableInDomain(final long affinityGroupId, final long domainId) {
        Long groupDomainId = null;

        final AffinityGroupDomainMapVO domainMap = _affinityGroupDomainMapDao.findByAffinityGroup(affinityGroupId);
        if (domainMap == null) {
            return false;
        } else {
            groupDomainId = domainMap.getDomainId();
        }

        if (domainId == groupDomainId.longValue()) {
            return true;
        }

        if (domainMap.subdomainAccess) {
            final Set<Long> parentDomains = _domainMgr.getDomainParentIds(domainId);
            if (parentDomains.contains(groupDomainId)) {
                return true;
            }
        }

        return false;
    }
}
