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
package com.cloud.network.as;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.dispatch.DispatchChainFactory;
import com.cloud.api.dispatch.DispatchTask;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.dao.EntityManager;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenter.NetworkType;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.event.ActionEvent;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InsufficientServerCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceInUseException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.IpAddresses;
import com.cloud.network.as.AutoScaleCounter.AutoScaleCounterParam;
import com.cloud.network.as.dao.AutoScalePolicyConditionMapDao;
import com.cloud.network.as.dao.AutoScalePolicyDao;
import com.cloud.network.as.dao.AutoScaleVmGroupDao;
import com.cloud.network.as.dao.AutoScaleVmGroupPolicyMapDao;
import com.cloud.network.as.dao.AutoScaleVmGroupVmMapDao;
import com.cloud.network.as.dao.AutoScaleVmProfileDao;
import com.cloud.network.as.dao.ConditionDao;
import com.cloud.network.as.dao.CounterDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.LoadBalancerVMMapDao;
import com.cloud.network.dao.LoadBalancerVMMapVO;
import com.cloud.network.dao.LoadBalancerVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.lb.LoadBalancingRulesManager;
import com.cloud.network.lb.LoadBalancingRulesService;
import com.cloud.offering.ServiceOffering;
import com.cloud.projects.Project.ListProjectResourcesCriteria;
import com.cloud.template.TemplateManager;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountService;
import com.cloud.user.User;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionStatus;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.UserVmService;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd.HTTPMethod;
import org.apache.cloudstack.api.BaseListAccountResourcesCmd;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.admin.autoscale.CreateCounterCmd;
import org.apache.cloudstack.api.command.user.autoscale.CreateAutoScalePolicyCmd;
import org.apache.cloudstack.api.command.user.autoscale.CreateAutoScaleVmGroupCmd;
import org.apache.cloudstack.api.command.user.autoscale.CreateAutoScaleVmProfileCmd;
import org.apache.cloudstack.api.command.user.autoscale.CreateConditionCmd;
import org.apache.cloudstack.api.command.user.autoscale.ListAutoScalePoliciesCmd;
import org.apache.cloudstack.api.command.user.autoscale.ListAutoScaleVmGroupsCmd;
import org.apache.cloudstack.api.command.user.autoscale.ListAutoScaleVmProfilesCmd;
import org.apache.cloudstack.api.command.user.autoscale.ListConditionsCmd;
import org.apache.cloudstack.api.command.user.autoscale.ListCountersCmd;
import org.apache.cloudstack.api.command.user.autoscale.UpdateAutoScalePolicyCmd;
import org.apache.cloudstack.api.command.user.autoscale.UpdateAutoScaleVmGroupCmd;
import org.apache.cloudstack.api.command.user.autoscale.UpdateAutoScaleVmProfileCmd;
import org.apache.cloudstack.api.command.user.vm.DeployVMCmd;
import org.apache.cloudstack.config.ApiServiceConfiguration;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoScaleManagerImpl<Type> extends ManagerBase implements AutoScaleManager, AutoScaleService {
    private static final Logger s_logger = LoggerFactory.getLogger(AutoScaleManagerImpl.class);
    private final ScheduledExecutorService _executor = Executors.newScheduledThreadPool(1);

    @Inject
    protected DispatchChainFactory dispatchChainFactory = null;
    @Inject
    EntityManager _entityMgr;
    @Inject
    AccountDao _accountDao;
    @Inject
    AccountManager _accountMgr;
    @Inject
    ConfigurationManager _configMgr;
    @Inject
    TemplateManager _templateMgr;
    @Inject
    LoadBalancingRulesManager _lbRulesMgr;
    @Inject
    NetworkDao _networkDao;
    @Inject
    CounterDao _counterDao;
    @Inject
    ConditionDao _conditionDao;
    @Inject
    LoadBalancerVMMapDao _lb2VmMapDao;
    @Inject
    LoadBalancerDao _lbDao;
    @Inject
    AutoScaleVmProfileDao _autoScaleVmProfileDao;
    @Inject
    AutoScalePolicyDao _autoScalePolicyDao;
    @Inject
    AutoScalePolicyConditionMapDao _autoScalePolicyConditionMapDao;
    @Inject
    AutoScaleVmGroupDao _autoScaleVmGroupDao;
    @Inject
    AutoScaleVmGroupPolicyMapDao _autoScaleVmGroupPolicyMapDao;
    @Inject
    AutoScaleVmGroupVmMapDao _autoScaleVmGroupVmMapDao;
    @Inject
    DataCenterDao _dcDao = null;
    @Inject
    UserDao _userDao;
    @Inject
    ConfigurationDao _configDao;
    @Inject
    IPAddressDao _ipAddressDao;
    @Inject
    AccountService _accountService;
    @Inject
    UserVmService _userVmService;
    @Inject
    UserVmManager _userVmManager;
    @Inject
    LoadBalancerVMMapDao _lbVmMapDao;
    @Inject
    LoadBalancingRulesService _loadBalancingRulesService;

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEPOLICY_CREATE, eventDescription = "creating autoscale policy", create = true)
    public AutoScalePolicy createAutoScalePolicy(final CreateAutoScalePolicyCmd cmd) {

        final int duration = cmd.getDuration();
        Integer quietTime = cmd.getQuietTime();
        String action = cmd.getAction();

        if (quietTime == null) {
            quietTime = NetUtils.DEFAULT_AUTOSCALE_POLICY_QUIET_TIME;
        }

        action = action.toLowerCase();
        if (!NetUtils.isValidAutoScaleAction(action)) {
            throw new InvalidParameterValueException("action is invalid, only 'scaleup' and 'scaledown' is supported");
        }

        AutoScalePolicyVO policyVO = new AutoScalePolicyVO(cmd.getDomainId(), cmd.getAccountId(), duration, quietTime, null, action);

        policyVO = checkValidityAndPersist(policyVO, cmd.getConditionIds());
        s_logger.info("Successfully created AutoScale Policy with Id: " + policyVO.getId());
        return policyVO;
    }

    @DB
    protected AutoScalePolicyVO checkValidityAndPersist(final AutoScalePolicyVO autoScalePolicyVOFinal, final List<Long> conditionIds) {
        final int duration = autoScalePolicyVOFinal.getDuration();
        final int quietTime = autoScalePolicyVOFinal.getQuietTime();

        if (duration < 0) {
            throw new InvalidParameterValueException("duration is an invalid value: " + duration);
        }

        if (quietTime < 0) {
            throw new InvalidParameterValueException("quiettime is an invalid value: " + quietTime);
        }

        return Transaction.execute(new TransactionCallback<AutoScalePolicyVO>() {
            @Override
            public AutoScalePolicyVO doInTransaction(final TransactionStatus status) {
                final AutoScalePolicyVO autoScalePolicyVO = _autoScalePolicyDao.persist(autoScalePolicyVOFinal);

                if (conditionIds != null) {
                    final SearchBuilder<ConditionVO> conditionsSearch = _conditionDao.createSearchBuilder();
                    conditionsSearch.and("ids", conditionsSearch.entity().getId(), Op.IN);
                    conditionsSearch.done();
                    final SearchCriteria<ConditionVO> sc = conditionsSearch.create();

                    sc.setParameters("ids", conditionIds.toArray(new Object[0]));
                    final List<ConditionVO> conditions = _conditionDao.search(sc, null);

                    final ControlledEntity[] sameOwnerEntities = conditions.toArray(new ControlledEntity[conditions.size() + 1]);
                    sameOwnerEntities[sameOwnerEntities.length - 1] = autoScalePolicyVO;
                    _accountMgr.checkAccess(CallContext.current().getCallingAccount(), null, true, sameOwnerEntities);

                    if (conditionIds.size() != conditions.size()) {
                        // TODO report the condition id which could not be found
                        throw new InvalidParameterValueException("Unable to find the condition specified");
                    }

                    final ArrayList<Long> counterIds = new ArrayList<>();
                    for (final ConditionVO condition : conditions) {
                        if (counterIds.contains(condition.getCounterid())) {
                            throw new InvalidParameterValueException(
                                    "atleast two conditions in the conditionids have the same counter. It is not right to apply two different conditions for the same counter");
                        }
                        counterIds.add(condition.getCounterid());
                    }

                    /* For update case remove the existing mappings and create fresh ones */
                    _autoScalePolicyConditionMapDao.removeByAutoScalePolicyId(autoScalePolicyVO.getId());

                    for (final Long conditionId : conditionIds) {
                        final AutoScalePolicyConditionMapVO policyConditionMapVO = new AutoScalePolicyConditionMapVO(autoScalePolicyVO.getId(), conditionId);
                        _autoScalePolicyConditionMapDao.persist(policyConditionMapVO);
                    }
                }

                return autoScalePolicyVO;
            }
        });
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEPOLICY_DELETE, eventDescription = "deleting autoscale policy")
    public boolean deleteAutoScalePolicy(final long id) {
        /* Check if entity is in database */
        getEntityInDatabase(CallContext.current().getCallingAccount(), "AutoScale Policy", id, _autoScalePolicyDao);

        if (_autoScaleVmGroupPolicyMapDao.isAutoScalePolicyInUse(id)) {
            throw new InvalidParameterValueException("Cannot delete AutoScale Policy when it is in use by one or more AutoScale Vm Groups");
        }

        return Transaction.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(final TransactionStatus status) {
                boolean success = true;
                success = _autoScalePolicyDao.remove(id);
                if (!success) {
                    s_logger.warn("Failed to remove AutoScale Policy db object");
                    return false;
                }
                success = _autoScalePolicyConditionMapDao.removeByAutoScalePolicyId(id);
                if (!success) {
                    s_logger.warn("Failed to remove AutoScale Policy Condition mappings");
                    return false;
                }
                s_logger.info("Successfully deleted autoscale policy id : " + id);

                return success;
            }
        });
    }

    @Override
    public List<? extends AutoScalePolicy> listAutoScalePolicies(final ListAutoScalePoliciesCmd cmd) {
        final SearchWrapper<AutoScalePolicyVO> searchWrapper = new SearchWrapper<>(_autoScalePolicyDao, AutoScalePolicyVO.class, cmd, cmd.getId());
        final SearchBuilder<AutoScalePolicyVO> sb = searchWrapper.getSearchBuilder();
        final Long id = cmd.getId();
        final Long conditionId = cmd.getConditionId();
        final String action = cmd.getAction();
        final Long vmGroupId = cmd.getVmGroupId();

        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("action", sb.entity().getAction(), SearchCriteria.Op.EQ);

        if (conditionId != null) {
            final SearchBuilder<AutoScalePolicyConditionMapVO> asPolicyConditionSearch = _autoScalePolicyConditionMapDao.createSearchBuilder();
            asPolicyConditionSearch.and("conditionId", asPolicyConditionSearch.entity().getConditionId(), SearchCriteria.Op.EQ);
            sb.join("asPolicyConditionSearch", asPolicyConditionSearch, sb.entity().getId(), asPolicyConditionSearch.entity().getPolicyId(), JoinBuilder.JoinType.INNER);
        }

        if (vmGroupId != null) {
            final SearchBuilder<AutoScaleVmGroupPolicyMapVO> asVmGroupPolicySearch = _autoScaleVmGroupPolicyMapDao.createSearchBuilder();
            asVmGroupPolicySearch.and("vmGroupId", asVmGroupPolicySearch.entity().getVmGroupId(), SearchCriteria.Op.EQ);
            sb.join("asVmGroupPolicySearch", asVmGroupPolicySearch, sb.entity().getId(), asVmGroupPolicySearch.entity().getPolicyId(), JoinBuilder.JoinType.INNER);
        }

        final SearchCriteria<AutoScalePolicyVO> sc = searchWrapper.buildSearchCriteria();

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (action != null) {
            sc.setParameters("action", action);
        }

        if (conditionId != null) {
            sc.setJoinParameters("asPolicyConditionSearch", "conditionId", conditionId);
        }

        if (vmGroupId != null) {
            sc.setJoinParameters("asVmGroupPolicySearch", "vmGroupId", vmGroupId);
        }

        return searchWrapper.search();
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEPOLICY_UPDATE, eventDescription = "updating autoscale policy")
    public AutoScalePolicy updateAutoScalePolicy(final UpdateAutoScalePolicyCmd cmd) {
        final Long policyId = cmd.getId();
        final Integer duration = cmd.getDuration();
        final Integer quietTime = cmd.getQuietTime();
        final List<Long> conditionIds = cmd.getConditionIds();
        AutoScalePolicyVO policy = getEntityInDatabase(CallContext.current().getCallingAccount(), "Auto Scale Policy", policyId, _autoScalePolicyDao);

        if (duration != null) {
            policy.setDuration(duration);
        }

        if (quietTime != null) {
            policy.setQuietTime(quietTime);
        }

        final List<AutoScaleVmGroupPolicyMapVO> vmGroupPolicyList = _autoScaleVmGroupPolicyMapDao.listByPolicyId(policyId);
        for (final AutoScaleVmGroupPolicyMapVO vmGroupPolicy : vmGroupPolicyList) {
            final AutoScaleVmGroupVO vmGroupVO = _autoScaleVmGroupDao.findById(vmGroupPolicy.getVmGroupId());
            if (vmGroupVO == null) {
                s_logger.warn("Stale database entry! There is an entry in VmGroupPolicyMap but the vmGroup is missing:" + vmGroupPolicy.getVmGroupId());

                continue;
            }
            if (!vmGroupVO.getState().equals(AutoScaleVmGroup.State_Disabled)) {
                throw new InvalidParameterValueException("The AutoScale Policy can be updated only if the Vm Group it is associated with is disabled in state");
            }
            if (policy.getDuration() < vmGroupVO.getInterval()) {
                throw new InvalidParameterValueException("duration is less than the associated AutoScaleVmGroup's interval");
            }
        }

        policy = checkValidityAndPersist(policy, conditionIds);
        s_logger.info("Successfully updated Auto Scale Policy id:" + policyId);
        return policy;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEVMPROFILE_CREATE, eventDescription = "creating autoscale vm profile", create = true)
    public AutoScaleVmProfile createAutoScaleVmProfile(final CreateAutoScaleVmProfileCmd cmd) {

        final Account owner = _accountDao.findById(cmd.getAccountId());
        final Account caller = CallContext.current().getCallingAccount();
        _accountMgr.checkAccess(caller, null, true, owner);

        final long zoneId = cmd.getZoneId();
        final long serviceOfferingId = cmd.getServiceOfferingId();
        final long autoscaleUserId = cmd.getAutoscaleUserId();

        final DataCenter zone = _entityMgr.findById(DataCenter.class, zoneId);

        if (zone == null) {
            throw new InvalidParameterValueException("Unable to find zone by id");
        }

        final ServiceOffering serviceOffering = _entityMgr.findById(ServiceOffering.class, serviceOfferingId);
        if (serviceOffering == null) {
            throw new InvalidParameterValueException("Unable to find service offering by id");
        }

        // validations
        final HashMap<String, String> deployParams = cmd.getDeployParamMap();
        if (deployParams.containsKey("networks") && deployParams.get("networks").length() > 0) {
            throw new InvalidParameterValueException(
                    "'networks' is not a valid parameter, network for an AutoScaled VM is chosen automatically. An autoscaled VM is deployed in the loadbalancer's network");
        }
        /*
         * Just for making sure the values are right in other deploy params.
         * For ex. if projectId is given as a string instead of an long value, this
         * will be throwing an error.
         */
        dispatchChainFactory.getStandardDispatchChain().dispatch(new DispatchTask(ComponentContext.inject(DeployVMCmd.class), deployParams));

        AutoScaleVmProfileVO profileVO =
                new AutoScaleVmProfileVO(cmd.getZoneId(), cmd.getDomainId(), cmd.getAccountId(), cmd.getServiceOfferingId(), cmd.getTemplateId(), cmd.getOtherDeployParams(),
                        cmd.getCounterParamList(), cmd.getDestroyVmGraceperiod(), autoscaleUserId);

        if (cmd.getDisplay() != null) {
            profileVO.setDisplay(cmd.getDisplay());
        }

        profileVO = checkValidityAndPersist(profileVO);
        s_logger.info("Successfully create AutoScale Vm Profile with Id: " + profileVO.getId());

        return profileVO;
    }

    @DB
    protected AutoScaleVmProfileVO checkValidityAndPersist(AutoScaleVmProfileVO vmProfile) {
        final long templateId = vmProfile.getTemplateId();
        final long autoscaleUserId = vmProfile.getAutoScaleUserId();
        final int destroyVmGraceperiod = vmProfile.getDestroyVmGraceperiod();

        final VirtualMachineTemplate template = _entityMgr.findById(VirtualMachineTemplate.class, templateId);
        // Make sure a valid template ID was specified
        if (template == null) {
            throw new InvalidParameterValueException("Unable to use the given template.");
        }

        if (destroyVmGraceperiod < 0) {
            throw new InvalidParameterValueException("Destroy Vm Grace Period cannot be less than 0.");
        }

        final User user = _userDao.findById(autoscaleUserId);
        if (user.getAccountId() != vmProfile.getAccountId()) {
            throw new InvalidParameterValueException("AutoScale User id does not belong to the same account");
        }

        final String apiKey = user.getApiKey();
        final String secretKey = user.getSecretKey();
        final String csUrl = ApiServiceConfiguration.ApiServletPath.value();

        if (apiKey == null) {
            throw new InvalidParameterValueException("apiKey for user: " + user.getUsername() + " is empty. Please generate it");
        }

        if (secretKey == null) {
            throw new InvalidParameterValueException("secretKey for user: " + user.getUsername() + " is empty. Please generate it");
        }

        if (csUrl == null || csUrl.contains("localhost")) {
            throw new InvalidParameterValueException("Global setting endpointe.url has to be set to the Management Server's API end point");
        }

        vmProfile = _autoScaleVmProfileDao.persist(vmProfile);

        return vmProfile;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEVMPROFILE_DELETE, eventDescription = "deleting autoscale vm profile")
    public boolean deleteAutoScaleVmProfile(final long id) {
        /* Check if entity is in database */
        getEntityInDatabase(CallContext.current().getCallingAccount(), "AutoScale Vm Profile", id, _autoScaleVmProfileDao);
        if (_autoScaleVmGroupDao.isProfileInUse(id)) {
            throw new InvalidParameterValueException("Cannot delete AutoScale Vm Profile when it is in use by one more vm groups");
        }
        final boolean success = _autoScaleVmProfileDao.remove(id);
        if (success) {
            s_logger.info("Successfully deleted AutoScale Vm Profile with Id: " + id);
        }
        return success;
    }

    @Override
    public List<? extends AutoScaleVmProfile> listAutoScaleVmProfiles(final ListAutoScaleVmProfilesCmd cmd) {
        final Long id = cmd.getId();
        final Long templateId = cmd.getTemplateId();
        final String otherDeployParams = cmd.getOtherDeployParams();
        final Long serviceOffId = cmd.getServiceOfferingId();
        final Long zoneId = cmd.getZoneId();
        final Boolean display = cmd.getDisplay();

        final SearchWrapper<AutoScaleVmProfileVO> searchWrapper = new SearchWrapper<>(_autoScaleVmProfileDao, AutoScaleVmProfileVO.class, cmd, cmd.getId());
        final SearchBuilder<AutoScaleVmProfileVO> sb = searchWrapper.getSearchBuilder();

        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("templateId", sb.entity().getTemplateId(), SearchCriteria.Op.EQ);
        sb.and("serviceOfferingId", sb.entity().getServiceOfferingId(), SearchCriteria.Op.EQ);
        sb.and("otherDeployParams", sb.entity().getOtherDeployParams(), SearchCriteria.Op.LIKE);
        sb.and("zoneId", sb.entity().getZoneId(), SearchCriteria.Op.EQ);
        sb.and("display", sb.entity().isDisplay(), SearchCriteria.Op.EQ);
        final SearchCriteria<AutoScaleVmProfileVO> sc = searchWrapper.buildSearchCriteria();

        if (id != null) {
            sc.setParameters("id", id);
        }
        if (templateId != null) {
            sc.setParameters("templateId", templateId);
        }
        if (otherDeployParams != null) {
            sc.addAnd("otherDeployParams", SearchCriteria.Op.LIKE, "%" + otherDeployParams + "%");
        }

        if (serviceOffId != null) {
            sc.setParameters("serviceOfferingId", serviceOffId);
        }

        if (zoneId != null) {
            sc.setParameters("zoneId", zoneId);
        }

        if (display != null) {
            sc.setParameters("display", display);
        }

        return searchWrapper.search();
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEVMPROFILE_UPDATE, eventDescription = "updating autoscale vm profile")
    public AutoScaleVmProfile updateAutoScaleVmProfile(final UpdateAutoScaleVmProfileCmd cmd) {
        final Long profileId = cmd.getId();
        final Long templateId = cmd.getTemplateId();
        final Long autoscaleUserId = cmd.getAutoscaleUserId();
        final Map counterParamList = cmd.getCounterParamList();

        final Integer destroyVmGraceperiod = cmd.getDestroyVmGraceperiod();

        AutoScaleVmProfileVO vmProfile = getEntityInDatabase(CallContext.current().getCallingAccount(), "Auto Scale Vm Profile", profileId, _autoScaleVmProfileDao);

        final boolean physicalParameterUpdate = (templateId != null || autoscaleUserId != null || counterParamList != null || destroyVmGraceperiod != null);

        if (templateId != null) {
            vmProfile.setTemplateId(templateId);
        }

        if (autoscaleUserId != null) {
            vmProfile.setAutoscaleUserId(autoscaleUserId);
        }

        if (counterParamList != null) {
            vmProfile.setCounterParamsForUpdate(counterParamList);
        }

        if (destroyVmGraceperiod != null) {
            vmProfile.setDestroyVmGraceperiod(destroyVmGraceperiod);
        }

        if (cmd.getCustomId() != null) {
            vmProfile.setUuid(cmd.getCustomId());
        }

        if (cmd.getDisplay() != null) {
            vmProfile.setDisplay(cmd.getDisplay());
        }

        final List<AutoScaleVmGroupVO> vmGroupList = _autoScaleVmGroupDao.listByAll(null, profileId);
        for (final AutoScaleVmGroupVO vmGroupVO : vmGroupList) {
            if (physicalParameterUpdate && !vmGroupVO.getState().equals(AutoScaleVmGroup.State_Disabled)) {
                throw new InvalidParameterValueException("The AutoScale Vm Profile can be updated only if the Vm Group it is associated with is disabled in state");
            }
        }

        vmProfile = checkValidityAndPersist(vmProfile);
        s_logger.info("Updated Auto Scale Vm Profile id:" + vmProfile.getId());

        return vmProfile;
    }

    private <VO extends ControlledEntity> VO getEntityInDatabase(final Account caller, final String paramName, final Long id, final GenericDao<VO, Long> dao) {

        final VO vo = dao.findById(id);

        if (vo == null) {
            throw new InvalidParameterValueException("Unable to find " + paramName);
        }

        _accountMgr.checkAccess(caller, null, false, (ControlledEntity) vo);

        return vo;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEVMGROUP_CREATE, eventDescription = "creating autoscale vm group", create = true)
    public AutoScaleVmGroup createAutoScaleVmGroup(final CreateAutoScaleVmGroupCmd cmd) {
        final int minMembers = cmd.getMinMembers();
        final int maxMembers = cmd.getMaxMembers();
        Integer interval = cmd.getInterval();
        final Boolean forDisplay = cmd.getDisplay();

        if (interval == null) {
            interval = NetUtils.DEFAULT_AUTOSCALE_POLICY_INTERVAL_TIME;
        }

        final LoadBalancerVO loadBalancer = getEntityInDatabase(CallContext.current().getCallingAccount(), ApiConstants.LBID, cmd.getLbRuleId(), _lbDao);

        final Long zoneId = _ipAddressDao.findById(loadBalancer.getSourceIpAddressId()).getDataCenterId();

        if (_autoScaleVmGroupDao.isAutoScaleLoadBalancer(loadBalancer.getId())) {
            throw new InvalidParameterValueException("an AutoScaleVmGroup is already attached to the lb rule, the existing vm group has to be first deleted");
        }

        if (_lb2VmMapDao.isVmAttachedToLoadBalancer(loadBalancer.getId())) {
            throw new InvalidParameterValueException(
                    "there are Vms already bound to the specified LoadBalancing Rule. User bound Vms and AutoScaled Vm Group cannot co-exist on a Load Balancing Rule");
        }

        AutoScaleVmGroupVO vmGroupVO = new AutoScaleVmGroupVO(cmd.getLbRuleId(), zoneId, loadBalancer.getDomainId(), loadBalancer.getAccountId(), minMembers, maxMembers,
                loadBalancer.getDefaultPortStart(), interval, null, cmd.getProfileId(), AutoScaleVmGroup.State_New);

        if (forDisplay != null) {
            vmGroupVO.setDisplay(forDisplay);
        }

        vmGroupVO = checkValidityAndPersist(vmGroupVO, cmd.getScaleUpPolicyIds(), cmd.getScaleDownPolicyIds());
        s_logger.info("Successfully created Autoscale Vm Group with Id: " + vmGroupVO.getId());

        return vmGroupVO;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEVMGROUP_CREATE, eventDescription = "creating autoscale vm group", async = true)
    public boolean configureAutoScaleVmGroup(final CreateAutoScaleVmGroupCmd cmd) throws ResourceUnavailableException {
        return configureAutoScaleVmGroup(cmd.getEntityId(), AutoScaleVmGroup.State_New);
    }

    private boolean configureAutoScaleVmGroup(final long vmGroupid, final String currentState) throws ResourceUnavailableException {
        final AutoScaleVmGroup vmGroup = _autoScaleVmGroupDao.findById(vmGroupid);

        if (isLoadBalancerBasedAutoScaleVmGroup(vmGroup)) {
            try {
                return _lbRulesMgr.configureLbAutoScaleVmGroup(vmGroupid, currentState);
            } catch (final ResourceUnavailableException re) {
                throw re;
            } catch (final Exception e) {
                s_logger.warn("Exception during configureLbAutoScaleVmGroup in lb rules manager", e);
                return false;
            }
        }

        // This should never happen, because today loadbalancerruleid is manadatory for AutoScaleVmGroup.
        throw new InvalidParameterValueException("Only LoadBalancer based AutoScale is supported");
    }

    public boolean isLoadBalancerBasedAutoScaleVmGroup(final AutoScaleVmGroup vmGroup) {
        return vmGroup.getLoadBalancerId() != null;
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEVMGROUP_DELETE, eventDescription = "deleting autoscale vm group", async = true)
    public boolean deleteAutoScaleVmGroup(final long id) {
        final AutoScaleVmGroupVO autoScaleVmGroupVO = getEntityInDatabase(CallContext.current().getCallingAccount(), "AutoScale Vm Group", id, _autoScaleVmGroupDao);

        if (autoScaleVmGroupVO.getState().equals(AutoScaleVmGroup.State_New)) {
            /* This condition is for handling failures during creation command */
            return _autoScaleVmGroupDao.remove(id);
        }
        final String bakupState = autoScaleVmGroupVO.getState();
        autoScaleVmGroupVO.setState(AutoScaleVmGroup.State_Revoke);
        _autoScaleVmGroupDao.persist(autoScaleVmGroupVO);
        boolean success = false;

        try {
            success = configureAutoScaleVmGroup(id, bakupState);
        } catch (final ResourceUnavailableException e) {
            autoScaleVmGroupVO.setState(bakupState);
            _autoScaleVmGroupDao.persist(autoScaleVmGroupVO);
        } finally {
            if (!success) {
                s_logger.warn("Could not delete AutoScale Vm Group id : " + id);
                return false;
            }
        }

        return Transaction.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(final TransactionStatus status) {
                boolean success = _autoScaleVmGroupDao.remove(id);

                if (!success) {
                    s_logger.warn("Failed to remove AutoScale Group db object");
                    return false;
                }

                success = _autoScaleVmGroupPolicyMapDao.removeByGroupId(id);
                if (!success) {
                    s_logger.warn("Failed to remove AutoScale Group Policy mappings");
                    return false;
                }

                s_logger.info("Successfully deleted autoscale vm group id : " + id);
                return success; // Successfull
            }
        });
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEVMGROUP_UPDATE, eventDescription = "updating autoscale vm group", async = true)
    public AutoScaleVmGroup updateAutoScaleVmGroup(final UpdateAutoScaleVmGroupCmd cmd) {
        final Long vmGroupId = cmd.getId();
        final Integer minMembers = cmd.getMinMembers();
        final Integer maxMembers = cmd.getMaxMembers();
        final Integer interval = cmd.getInterval();
        final Boolean forDisplay = cmd.getDisplay();

        final List<Long> scaleUpPolicyIds = cmd.getScaleUpPolicyIds();
        final List<Long> scaleDownPolicyIds = cmd.getScaleDownPolicyIds();

        AutoScaleVmGroupVO vmGroupVO = getEntityInDatabase(CallContext.current().getCallingAccount(), "AutoScale Vm Group", vmGroupId, _autoScaleVmGroupDao);

        final boolean physicalParametersUpdate = (minMembers != null || maxMembers != null || interval != null);

        if (physicalParametersUpdate && !vmGroupVO.getState().equals(AutoScaleVmGroup.State_Disabled)) {
            throw new InvalidParameterValueException("An AutoScale Vm Group can be updated with minMembers/maxMembers/Interval only when it is in disabled state");
        }

        if (minMembers != null) {
            vmGroupVO.setMinMembers(minMembers);
        }

        if (maxMembers != null) {
            vmGroupVO.setMaxMembers(maxMembers);
        }

        if (maxMembers != null) {
            vmGroupVO.setInterval(interval);
        }

        if (cmd.getCustomId() != null) {
            vmGroupVO.setUuid(cmd.getCustomId());
        }

        if (forDisplay != null) {
            vmGroupVO.setDisplay(forDisplay);
        }

        vmGroupVO = checkValidityAndPersist(vmGroupVO, scaleUpPolicyIds, scaleDownPolicyIds);
        if (vmGroupVO != null) {
            s_logger.debug("Updated Auto Scale VmGroup id:" + vmGroupId);
            return vmGroupVO;
        } else {
            return null;
        }
    }

    @Override
    @DB
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEVMGROUP_ENABLE, eventDescription = "enabling autoscale vm group", async = true)
    public AutoScaleVmGroup enableAutoScaleVmGroup(final Long id) {
        AutoScaleVmGroupVO vmGroup = getEntityInDatabase(CallContext.current().getCallingAccount(), "AutoScale Vm Group", id, _autoScaleVmGroupDao);
        boolean success = false;
        if (!vmGroup.getState().equals(AutoScaleVmGroup.State_Disabled)) {
            throw new InvalidParameterValueException("Only a AutoScale Vm Group which is in Disabled state can be enabled.");
        }

        try {
            vmGroup.setState(AutoScaleVmGroup.State_Enabled);
            vmGroup = _autoScaleVmGroupDao.persist(vmGroup);
            success = configureAutoScaleVmGroup(id, AutoScaleVmGroup.State_Disabled);
        } catch (final ResourceUnavailableException e) {
            vmGroup.setState(AutoScaleVmGroup.State_Disabled);
            _autoScaleVmGroupDao.persist(vmGroup);
        } finally {
            if (!success) {
                s_logger.warn("Failed to enable AutoScale Vm Group id : " + id);
                return null;
            }
            s_logger.info("Successfully enabled AutoScale Vm Group with Id:" + id);
        }
        return vmGroup;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_AUTOSCALEVMGROUP_DISABLE, eventDescription = "disabling autoscale vm group", async = true)
    @DB
    public AutoScaleVmGroup disableAutoScaleVmGroup(final Long id) {
        AutoScaleVmGroupVO vmGroup = getEntityInDatabase(CallContext.current().getCallingAccount(), "AutoScale Vm Group", id, _autoScaleVmGroupDao);
        boolean success = false;
        if (!vmGroup.getState().equals(AutoScaleVmGroup.State_Enabled)) {
            throw new InvalidParameterValueException("Only a AutoScale Vm Group which is in Enabled state can be disabled.");
        }

        try {
            vmGroup.setState(AutoScaleVmGroup.State_Disabled);
            vmGroup = _autoScaleVmGroupDao.persist(vmGroup);
            success = configureAutoScaleVmGroup(id, AutoScaleVmGroup.State_Enabled);
        } catch (final ResourceUnavailableException e) {
            vmGroup.setState(AutoScaleVmGroup.State_Enabled);
            _autoScaleVmGroupDao.persist(vmGroup);
        } finally {
            if (!success) {
                s_logger.warn("Failed to disable AutoScale Vm Group id : " + id);
                return null;
            }
            s_logger.info("Successfully disabled AutoScale Vm Group with Id:" + id);
        }
        return vmGroup;
    }

    @Override
    public List<? extends AutoScaleVmGroup> listAutoScaleVmGroups(final ListAutoScaleVmGroupsCmd cmd) {
        final Long id = cmd.getId();
        final Long policyId = cmd.getPolicyId();
        final Long loadBalancerId = cmd.getLoadBalancerId();
        final Long profileId = cmd.getProfileId();
        final Long zoneId = cmd.getZoneId();
        final Boolean forDisplay = cmd.getDisplay();

        final SearchWrapper<AutoScaleVmGroupVO> searchWrapper = new SearchWrapper<>(_autoScaleVmGroupDao, AutoScaleVmGroupVO.class, cmd, cmd.getId());
        final SearchBuilder<AutoScaleVmGroupVO> sb = searchWrapper.getSearchBuilder();

        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("loadBalancerId", sb.entity().getLoadBalancerId(), SearchCriteria.Op.EQ);
        sb.and("profileId", sb.entity().getProfileId(), SearchCriteria.Op.EQ);
        sb.and("zoneId", sb.entity().getZoneId(), SearchCriteria.Op.EQ);
        sb.and("display", sb.entity().isDisplay(), SearchCriteria.Op.EQ);

        if (policyId != null) {
            final SearchBuilder<AutoScaleVmGroupPolicyMapVO> asVmGroupPolicySearch = _autoScaleVmGroupPolicyMapDao.createSearchBuilder();
            asVmGroupPolicySearch.and("policyId", asVmGroupPolicySearch.entity().getPolicyId(), SearchCriteria.Op.EQ);
            sb.join("asVmGroupPolicySearch", asVmGroupPolicySearch, sb.entity().getId(), asVmGroupPolicySearch.entity().getVmGroupId(), JoinBuilder.JoinType.INNER);
        }

        final SearchCriteria<AutoScaleVmGroupVO> sc = searchWrapper.buildSearchCriteria();
        if (id != null) {
            sc.setParameters("id", id);
        }
        if (loadBalancerId != null) {
            sc.setParameters("loadBalancerId", loadBalancerId);
        }
        if (profileId != null) {
            sc.setParameters("profileId", profileId);
        }
        if (zoneId != null) {
            sc.setParameters("zoneId", zoneId);
        }
        if (policyId != null) {
            sc.setJoinParameters("asVmGroupPolicySearch", "policyId", policyId);
        }
        if (forDisplay != null) {
            sc.setParameters("display", forDisplay);
        }
        return searchWrapper.search();
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_COUNTER_CREATE, eventDescription = "Counter", create = true)
    @DB
    public Counter createCounter(final CreateCounterCmd cmd) {
        final String source = cmd.getSource().toLowerCase();
        final String name = cmd.getName();
        final Counter.Source src;
        // Validate Source
        try {
            src = Counter.Source.valueOf(source);
        } catch (final Exception ex) {
            throw new InvalidParameterValueException("The Source " + source + " does not exist; Unable to create Counter");
        }

        CounterVO counter = null;

        s_logger.debug("Adding Counter " + name);
        counter = _counterDao.persist(new CounterVO(src, name, cmd.getValue()));

        CallContext.current().setEventDetails(" Id: " + counter.getId() + " Name: " + name);
        return counter;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_COUNTER_DELETE, eventDescription = "counter")
    public boolean deleteCounter(final long counterId) throws ResourceInUseException {
        // Verify Counter id
        final CounterVO counter = _counterDao.findById(counterId);
        if (counter == null) {
            throw new InvalidParameterValueException("Unable to find Counter");
        }

        // Verify if it is used in any Condition

        final ConditionVO condition = _conditionDao.findByCounterId(counterId);
        if (condition != null) {
            s_logger.info("Cannot delete counter " + counter.getName() + " as it is being used in a condition.");
            throw new ResourceInUseException("Counter is in use.");
        }

        final boolean success = _counterDao.remove(counterId);
        if (success) {
            s_logger.info("Successfully deleted counter with Id: " + counterId);
        }

        return success;
    }

    @Override
    public List<? extends Counter> listCounters(final ListCountersCmd cmd) {
        final String name = cmd.getName();
        final Long id = cmd.getId();
        String source = cmd.getSource();
        if (source != null) {
            source = source.toLowerCase();
        }

        final Filter searchFilter = new Filter(CounterVO.class, "created", false, cmd.getStartIndex(), cmd.getPageSizeVal());

        final List<CounterVO> counters = _counterDao.listCounters(id, name, source, cmd.getKeyword(), searchFilter);

        return counters;
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_CONDITION_CREATE, eventDescription = "Condition", create = true)
    public Condition createCondition(final CreateConditionCmd cmd) {
        checkCallerAccess(cmd.getAccountName(), cmd.getDomainId());
        final String opr = cmd.getRelationalOperator().toUpperCase();
        final long cid = cmd.getCounterId();
        final long threshold = cmd.getThreshold();
        final Condition.Operator op;
        // Validate Relational Operator
        try {
            op = Condition.Operator.valueOf(opr);
        } catch (final IllegalArgumentException ex) {
            throw new InvalidParameterValueException("The Operator " + opr + " does not exist; Unable to create Condition.");
        }
        // TODO - Validate threshold

        final CounterVO counter = _counterDao.findById(cid);

        if (counter == null) {
            throw new InvalidParameterValueException("Unable to find counter");
        }
        ConditionVO condition = null;

        condition = _conditionDao.persist(new ConditionVO(cid, threshold, cmd.getEntityOwnerId(), cmd.getDomainId(), op));
        s_logger.info("Successfully created condition with Id: " + condition.getId());

        CallContext.current().setEventDetails(" Id: " + condition.getId());
        return condition;
    }

    public void checkCallerAccess(final String accountName, final Long domainId) {
        final Account caller = CallContext.current().getCallingAccount();
        final Account owner = _accountDao.findActiveAccount(accountName, domainId);
        if (owner == null) {
            final List<String> idList = new ArrayList<>();
            idList.add(ApiDBUtils.findDomainById(domainId).getUuid());
            throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain with specifed domainId");
        }
        _accountMgr.checkAccess(caller, null, false, owner);
    }

    @Override
    public List<? extends Condition> listConditions(final ListConditionsCmd cmd) {
        final Long id = cmd.getId();
        final Long counterId = cmd.getCounterId();
        final Long policyId = cmd.getPolicyId();
        final SearchWrapper<ConditionVO> searchWrapper = new SearchWrapper<>(_conditionDao, ConditionVO.class, cmd, cmd.getId());
        final SearchBuilder<ConditionVO> sb = searchWrapper.getSearchBuilder();
        if (policyId != null) {
            final SearchBuilder<AutoScalePolicyConditionMapVO> asPolicyConditionSearch = _autoScalePolicyConditionMapDao.createSearchBuilder();
            asPolicyConditionSearch.and("policyId", asPolicyConditionSearch.entity().getPolicyId(), SearchCriteria.Op.EQ);
            sb.join("asPolicyConditionSearch", asPolicyConditionSearch, sb.entity().getId(), asPolicyConditionSearch.entity().getConditionId(),
                    JoinBuilder.JoinType.INNER);
        }

        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("counterId", sb.entity().getCounterid(), SearchCriteria.Op.EQ);

        // now set the SC criteria...
        final SearchCriteria<ConditionVO> sc = searchWrapper.buildSearchCriteria();

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (counterId != null) {
            sc.setParameters("counterId", counterId);
        }

        if (policyId != null) {
            sc.setJoinParameters("asPolicyConditionSearch", "policyId", policyId);
        }

        return searchWrapper.search();
    }

    @Override
    @ActionEvent(eventType = EventTypes.EVENT_CONDITION_DELETE, eventDescription = "condition")
    public boolean deleteCondition(final long conditionId) throws ResourceInUseException {
        /* Check if entity is in database */
        final ConditionVO condition = getEntityInDatabase(CallContext.current().getCallingAccount(), "Condition", conditionId, _conditionDao);
        if (condition == null) {
            throw new InvalidParameterValueException("Unable to find Condition");
        }

        // Verify if condition is used in any autoscale policy
        if (_autoScalePolicyConditionMapDao.isConditionInUse(conditionId)) {
            s_logger.info("Cannot delete condition " + conditionId + " as it is being used in a condition.");
            throw new ResourceInUseException("Cannot delete Condition when it is in use by one or more AutoScale Policies.");
        }
        final boolean success = _conditionDao.remove(conditionId);
        if (success) {
            s_logger.info("Successfully deleted condition " + condition.getId());
        }
        return success;
    }

    @DB
    protected AutoScaleVmGroupVO checkValidityAndPersist(final AutoScaleVmGroupVO vmGroup, final List<Long> passedScaleUpPolicyIds,
                                                         final List<Long> passedScaleDownPolicyIds) {
        final int minMembers = vmGroup.getMinMembers();
        final int maxMembers = vmGroup.getMaxMembers();
        final int interval = vmGroup.getInterval();
        final List<Counter> counters = new ArrayList<>();
        final List<AutoScalePolicyVO> policies = new ArrayList<>();
        final List<Long> policyIds = new ArrayList<>();
        final List<Long> currentScaleUpPolicyIds = new ArrayList<>();
        final List<Long> currentScaleDownPolicyIds = new ArrayList<>();
        if (vmGroup.getCreated() != null) {
            ApiDBUtils.getAutoScaleVmGroupPolicyIds(vmGroup.getId(), currentScaleUpPolicyIds, currentScaleDownPolicyIds);
        }

        if (minMembers < 0) {
            throw new InvalidParameterValueException(ApiConstants.MIN_MEMBERS + " is an invalid value: " + minMembers);
        }

        if (maxMembers < 0) {
            throw new InvalidParameterValueException(ApiConstants.MAX_MEMBERS + " is an invalid value: " + maxMembers);
        }

        if (minMembers > maxMembers) {
            throw new InvalidParameterValueException(ApiConstants.MIN_MEMBERS + " (" + minMembers + ")cannot be greater than " + ApiConstants.MAX_MEMBERS + " (" +
                    maxMembers + ")");
        }

        if (interval < 0) {
            throw new InvalidParameterValueException("interval is an invalid value: " + interval);
        }

        if (passedScaleUpPolicyIds != null) {
            policies.addAll(getAutoScalePolicies("scaleuppolicyid", passedScaleUpPolicyIds, counters, interval, true));
            policyIds.addAll(passedScaleUpPolicyIds);
        } else {
            // Run the interval check for existing policies
            getAutoScalePolicies("scaleuppolicyid", currentScaleUpPolicyIds, counters, interval, true);
            policyIds.addAll(currentScaleUpPolicyIds);
        }

        if (passedScaleDownPolicyIds != null) {
            policies.addAll(getAutoScalePolicies("scaledownpolicyid", passedScaleDownPolicyIds, counters, interval, false));
            policyIds.addAll(passedScaleDownPolicyIds);
        } else {
            // Run the interval check for existing policies
            getAutoScalePolicies("scaledownpolicyid", currentScaleDownPolicyIds, counters, interval, false);
            policyIds.addAll(currentScaleDownPolicyIds);
        }
        final AutoScaleVmProfileVO profileVO =
                getEntityInDatabase(CallContext.current().getCallingAccount(), ApiConstants.VMPROFILE_ID, vmGroup.getProfileId(), _autoScaleVmProfileDao);

        final LoadBalancerVO loadBalancer = getEntityInDatabase(CallContext.current().getCallingAccount(), ApiConstants.LBID, vmGroup.getLoadBalancerId(), _lbDao);
        validateAutoScaleCounters(loadBalancer.getNetworkId(), counters, profileVO.getCounterParams());

        final ControlledEntity[] sameOwnerEntities = policies.toArray(new ControlledEntity[policies.size() + 2]);
        sameOwnerEntities[sameOwnerEntities.length - 2] = loadBalancer;
        sameOwnerEntities[sameOwnerEntities.length - 1] = profileVO;
        _accountMgr.checkAccess(CallContext.current().getCallingAccount(), null, true, sameOwnerEntities);

        return Transaction.execute(new TransactionCallback<AutoScaleVmGroupVO>() {
            @Override
            public AutoScaleVmGroupVO doInTransaction(final TransactionStatus status) {
                final AutoScaleVmGroupVO vmGroupNew = _autoScaleVmGroupDao.persist(vmGroup);

                if (passedScaleUpPolicyIds != null || passedScaleDownPolicyIds != null) {
                    _autoScaleVmGroupPolicyMapDao.removeByGroupId(vmGroupNew.getId());

                    for (final Long policyId : policyIds) {
                        _autoScaleVmGroupPolicyMapDao.persist(new AutoScaleVmGroupPolicyMapVO(vmGroupNew.getId(), policyId));
                    }
                }

                return vmGroupNew;
            }
        });
    }

    private List<AutoScalePolicyVO> getAutoScalePolicies(final String paramName, final List<Long> policyIds, final List<Counter> counters, final int interval, final boolean
            scaleUpPolicies) {
        final SearchBuilder<AutoScalePolicyVO> policySearch = _autoScalePolicyDao.createSearchBuilder();
        policySearch.and("ids", policySearch.entity().getId(), Op.IN);
        policySearch.done();
        final SearchCriteria<AutoScalePolicyVO> sc = policySearch.create();

        sc.setParameters("ids", policyIds.toArray(new Object[0]));
        final List<AutoScalePolicyVO> policies = _autoScalePolicyDao.search(sc, null);

        int prevQuietTime = 0;

        for (final AutoScalePolicyVO policy : policies) {
            final int quietTime = policy.getQuietTime();
            if (prevQuietTime == 0) {
                prevQuietTime = quietTime;
            }
            final int duration = policy.getDuration();
            if (duration < interval) {
                throw new InvalidParameterValueException("duration : " + duration + " specified in a policy cannot be less than vm group's interval : " + interval);
            }

            if (quietTime != prevQuietTime) {
                throw new InvalidParameterValueException("quietTime should be same for all the policies specified in " + paramName);
            }

            if (scaleUpPolicies) {
                if (!isAutoScaleScaleUpPolicy(policy)) {
                    throw new InvalidParameterValueException("Only scaleup policies can be specified in scaleuppolicyids");
                }
            } else {
                if (isAutoScaleScaleUpPolicy(policy)) {
                    throw new InvalidParameterValueException("Only scaledown policies can be specified in scaledownpolicyids");
                }
            }
            final List<AutoScalePolicyConditionMapVO> policyConditionMapVOs = _autoScalePolicyConditionMapDao.listByAll(policy.getId(), null);
            for (final AutoScalePolicyConditionMapVO policyConditionMapVO : policyConditionMapVOs) {
                final long conditionid = policyConditionMapVO.getConditionId();
                final Condition condition = _conditionDao.findById(conditionid);
                final Counter counter = _counterDao.findById(condition.getCounterid());
                counters.add(counter);
            }
        }
        return policies;
    }

    public void validateAutoScaleCounters(final long networkid, final List<Counter> counters, final List<Pair<String, String>> counterParamPassed) {
        final List<AutoScaleCounter> supportedCounters = getSupportedAutoScaleCounters(networkid);
        if (supportedCounters == null) {
            throw new InvalidParameterException("AutoScale is not supported in the network");
        }
        for (final Counter counter : counters) {
            final String counterName = counter.getSource().name().toString();
            boolean isCounterSupported = false;
            for (final AutoScaleCounter autoScaleCounter : supportedCounters) {
                if (autoScaleCounter.getName().equals(counterName)) {
                    isCounterSupported = true;
                    final List<AutoScaleCounterParam> counterParams = autoScaleCounter.getParamList();
                    for (final AutoScaleCounterParam autoScaleCounterParam : counterParams) {
                        final boolean isRequiredParameter = autoScaleCounterParam.getRequired();
                        if (isRequiredParameter) {
                            boolean isRequiredParamPresent = false;
                            for (final Pair<String, String> pair : counterParamPassed) {
                                if (pair.first().equals(autoScaleCounterParam.getParamName())) {
                                    isRequiredParamPresent = true;
                                }
                            }
                            if (!isRequiredParamPresent) {
                                throw new InvalidParameterException("Parameter " + autoScaleCounterParam.getParamName() + " has to be set in AutoScaleVmProfile's " +
                                        ApiConstants.COUNTERPARAM_LIST);
                            }
                        }
                    }
                    break;
                }
            }
            if (!isCounterSupported) {
                throw new InvalidParameterException("AutoScale counter with source='" + counter.getSource().name() + "' is not supported " + "in the network");
            }
        }
    }

    private boolean isAutoScaleScaleUpPolicy(final AutoScalePolicy policyVO) {
        return policyVO.getAction().equals("scaleup");
    }

    public List<AutoScaleCounter> getSupportedAutoScaleCounters(final long networkid) {
        final String capability = _lbRulesMgr.getLBCapability(networkid, Capability.AutoScaleCounters.getName());
        if (capability == null) {
            return null;
        }
        final Gson gson = new Gson();
        final java.lang.reflect.Type listType = new TypeToken<List<AutoScaleCounter>>() {
        }.getType();
        final List<AutoScaleCounter> result = gson.fromJson(capability, listType);
        return result;
    }

    @Override
    public void cleanUpAutoScaleResources(final Long accountId) {
        // cleans Autoscale VmProfiles, AutoScale Policies and Conditions belonging to an account
        int count = 0;
        count = _autoScaleVmProfileDao.removeByAccountId(accountId);
        if (count > 0) {
            s_logger.debug("Deleted " + count + " AutoScale Vm Profile for account Id: " + accountId);
        }
        count = _autoScalePolicyDao.removeByAccountId(accountId);
        if (count > 0) {
            s_logger.debug("Deleted " + count + " AutoScale Policies for account Id: " + accountId);
        }
        count = _conditionDao.removeByAccountId(accountId);
        if (count > 0) {
            s_logger.debug("Deleted " + count + " Conditions for account Id: " + accountId);
        }
    }

    @Override
    public void doScaleUp(final long groupId, final Integer numVm) {
        final AutoScaleVmGroupVO asGroup = _autoScaleVmGroupDao.findById(groupId);
        if (asGroup == null) {
            s_logger.error("Can not find the groupid " + groupId + " for scaling up");
            return;
        }
        if (!checkConditionUp(asGroup, numVm)) {
            return;
        }
        for (int i = 0; i < numVm; i++) {
            final long vmId = createNewVM(asGroup);
            if (vmId == -1) {
                s_logger.error("Can not deploy new VM for scaling up in the group "
                        + asGroup.getId() + ". Waiting for next round");
                break;
            }
            if (startNewVM(vmId)) {
                if (assignLBruleToNewVm(vmId, asGroup)) {
                    // persist to DB
                    final AutoScaleVmGroupVmMapVO GroupVmVO = new AutoScaleVmGroupVmMapVO(
                            asGroup.getId(), vmId);
                    _autoScaleVmGroupVmMapDao.persist(GroupVmVO);
                    // update last_quiettime
                    final List<AutoScaleVmGroupPolicyMapVO> GroupPolicyVOs = _autoScaleVmGroupPolicyMapDao
                            .listByVmGroupId(groupId);
                    for (final AutoScaleVmGroupPolicyMapVO GroupPolicyVO : GroupPolicyVOs) {
                        final AutoScalePolicyVO vo = _autoScalePolicyDao
                                .findById(GroupPolicyVO.getPolicyId());
                        if (vo.getAction().equals("scaleup")) {
                            vo.setLastQuiteTime(new Date());
                            _autoScalePolicyDao.persist(vo);
                            break;
                        }
                    }
                } else {
                    s_logger.error("Can not assign LB rule for this new VM");
                    break;
                }
            } else {
                s_logger.error("Can not deploy new VM for scaling up in the group "
                        + asGroup.getId() + ". Waiting for next round");
                break;
            }
        }
    }

    private boolean checkConditionUp(final AutoScaleVmGroupVO asGroup, final Integer numVm) {
        // check maximum
        final Integer currentVM = _autoScaleVmGroupVmMapDao.countByGroup(asGroup.getId());
        final Integer maxVm = asGroup.getMaxMembers();
        if (currentVM + numVm > maxVm) {
            s_logger.warn("number of VM will greater than the maximum in this group if scaling up, so do nothing more");
            return false;
        }
        return true;
    }

    private long createNewVM(final AutoScaleVmGroupVO asGroup) {
        final AutoScaleVmProfileVO profileVo = _autoScaleVmProfileDao.findById(asGroup.getProfileId());
        final long templateId = profileVo.getTemplateId();
        final long serviceOfferingId = profileVo.getServiceOfferingId();
        if (templateId == -1) {
            return -1;
        }
        // create new VM into DB
        try {
            //Verify that all objects exist before passing them to the service
            final Account owner = _accountService.getActiveAccountById(profileVo.getAccountId());

            final DataCenter zone = _entityMgr.findById(DataCenter.class, profileVo.getZoneId());
            if (zone == null) {
                throw new InvalidParameterValueException("Unable to find zone by id=" + profileVo.getZoneId());
            }

            final ServiceOffering serviceOffering = _entityMgr.findById(ServiceOffering.class, serviceOfferingId);
            if (serviceOffering == null) {
                throw new InvalidParameterValueException("Unable to find service offering: " + serviceOfferingId);
            }

            final VirtualMachineTemplate template = _entityMgr.findById(VirtualMachineTemplate.class, templateId);
            // Make sure a valid template ID was specified
            if (template == null) {
                throw new InvalidParameterValueException("Unable to use template " + templateId);
            }

            if (!zone.isLocalStorageEnabled()) {
                if (serviceOffering.getUseLocalStorage()) {
                    throw new InvalidParameterValueException("Zone is not configured to use local storage but service offering " + serviceOffering.getName() + " uses it");
                }
            }

            UserVm vm = null;
            final IpAddresses addrs = new IpAddresses(null, null);
            if (zone.getNetworkType() == NetworkType.Basic) {
                vm = _userVmService.createBasicSecurityGroupVirtualMachine(zone, serviceOffering, template, null, owner, "autoScaleVm-" + asGroup.getId() + "-" +
                                getCurrentTimeStampString(),
                        "autoScaleVm-" + asGroup.getId() + "-" + getCurrentTimeStampString(), null, null, null, HypervisorType.XenServer, HTTPMethod.GET, null, null, null,
                        null, true, null, null, null, null);
            } else {
                if (zone.isSecurityGroupEnabled()) {
                    vm = _userVmService.createAdvancedSecurityGroupVirtualMachine(zone, serviceOffering, template, null, null,
                            owner, "autoScaleVm-" + asGroup.getId() + "-" + getCurrentTimeStampString(),
                            "autoScaleVm-" + asGroup.getId() + "-" + getCurrentTimeStampString(), null, null, null, HypervisorType.XenServer, HTTPMethod.GET, null, null,
                            null, null, true, null, null, null, null);
                } else {
                    vm = _userVmService.createAdvancedVirtualMachine(zone, serviceOffering, template, null, owner, "autoScaleVm-" + asGroup.getId() + "-" +
                                    getCurrentTimeStampString(), "autoScaleVm-" + asGroup.getId() + "-" + getCurrentTimeStampString(),
                            null, null, null, HypervisorType.XenServer, HTTPMethod.GET, null, null, null, addrs, true, null, null, null, null);
                }
            }

            if (vm != null) {
                return vm.getId();
            } else {
                return -1;
            }
        } catch (final InsufficientCapacityException ex) {
            s_logger.info(ex.toString());
            s_logger.trace(ex.getMessage(), ex);
            throw new ServerApiException(ApiErrorCode.INSUFFICIENT_CAPACITY_ERROR, ex.getMessage());
        } catch (final ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        } catch (final ConcurrentOperationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        } catch (final ResourceAllocationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_ALLOCATION_ERROR, ex.getMessage());
        }
    }

    private boolean startNewVM(final long vmId) {
        try {
            CallContext.current().setEventDetails("Vm Id: " + vmId);
            _userVmManager.startVirtualMachine(vmId, null, null, null);
        } catch (final ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        } catch (final ConcurrentOperationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        } catch (final InsufficientCapacityException ex) {
            final StringBuilder message = new StringBuilder(ex.getMessage());
            if (ex instanceof InsufficientServerCapacityException) {
                if (((InsufficientServerCapacityException) ex).isAffinityApplied()) {
                    message.append(", Please check the affinity groups provided, there may not be sufficient capacity to follow them");
                }
            }
            s_logger.info(ex.toString());
            s_logger.info(message.toString(), ex);
            throw new ServerApiException(ApiErrorCode.INSUFFICIENT_CAPACITY_ERROR, message.toString());
        }
        return true;
    }

    private boolean assignLBruleToNewVm(final long vmId, final AutoScaleVmGroupVO asGroup) {
        final List<Long> lstVmId = new ArrayList<>();
        final long lbId = asGroup.getLoadBalancerId();

        final List<LoadBalancerVMMapVO> LbVmMapVos = _lbVmMapDao.listByLoadBalancerId(lbId);
        if ((LbVmMapVos != null) && (LbVmMapVos.size() > 0)) {
            for (final LoadBalancerVMMapVO LbVmMapVo : LbVmMapVos) {
                final long instanceId = LbVmMapVo.getInstanceId();
                if (instanceId == vmId) {
                    s_logger.warn("the new VM is already mapped to LB rule. What's wrong?");
                    return true;
                }
            }
        }
        lstVmId.add(new Long(vmId));
        return _loadBalancingRulesService.assignToLoadBalancer(lbId, lstVmId, new HashMap<>());
    }

    private String getCurrentTimeStampString() {
        final Date current = new Date();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        return sdf.format(current);
    }

    @Override
    public void doScaleDown(final long groupId) {
        final AutoScaleVmGroupVO asGroup = _autoScaleVmGroupDao.findById(groupId);
        if (asGroup == null) {
            s_logger.error("Can not find the groupid " + groupId + " for scaling up");
            return;
        }
        if (!checkConditionDown(asGroup)) {
            return;
        }
        final long vmId = removeLBrule(asGroup);
        if (vmId != -1) {
            final long profileId = asGroup.getProfileId();

            // update group-vm mapping
            _autoScaleVmGroupVmMapDao.remove(groupId, vmId);
            // update last_quiettime
            final List<AutoScaleVmGroupPolicyMapVO> GroupPolicyVOs = _autoScaleVmGroupPolicyMapDao.listByVmGroupId(groupId);
            for (final AutoScaleVmGroupPolicyMapVO GroupPolicyVO : GroupPolicyVOs) {
                final AutoScalePolicyVO vo = _autoScalePolicyDao.findById(GroupPolicyVO.getPolicyId());
                if (vo.getAction().equals("scaledown")) {
                    vo.setLastQuiteTime(new Date());
                    _autoScalePolicyDao.persist(vo);
                    break;
                }
            }

            // get destroyvmgrace param
            final AutoScaleVmProfileVO asProfile = _autoScaleVmProfileDao.findById(profileId);
            final Integer destroyVmGracePeriod = asProfile.getDestroyVmGraceperiod();
            if (destroyVmGracePeriod >= 0) {
                _executor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            _userVmManager.destroyVm(vmId);
                        } catch (final ResourceUnavailableException e) {
                            e.printStackTrace();
                        } catch (final ConcurrentOperationException e) {
                            e.printStackTrace();
                        }
                    }
                }, destroyVmGracePeriod, TimeUnit.SECONDS);
            }
        } else {
            s_logger.error("Can not remove LB rule for the VM being destroyed. Do nothing more.");
        }
    }

    private boolean checkConditionDown(final AutoScaleVmGroupVO asGroup) {
        final Integer currentVM = _autoScaleVmGroupVmMapDao.countByGroup(asGroup.getId());
        final Integer minVm = asGroup.getMinMembers();
        if (currentVM - 1 < minVm) {
            s_logger.warn("number of VM will less than the minimum in this group if scaling down, so do nothing more");
            return false;
        }
        return true;
    }

    private long removeLBrule(final AutoScaleVmGroupVO asGroup) {
        final long lbId = asGroup.getLoadBalancerId();
        long instanceId = -1;
        final List<LoadBalancerVMMapVO> LbVmMapVos = _lbVmMapDao.listByLoadBalancerId(lbId);
        if ((LbVmMapVos != null) && (LbVmMapVos.size() > 0)) {
            for (final LoadBalancerVMMapVO LbVmMapVo : LbVmMapVos) {
                instanceId = LbVmMapVo.getInstanceId();
            }
        }
        // take last VM out of the list
        final List<Long> lstVmId = new ArrayList<>();
        if (instanceId != -1) {
            lstVmId.add(instanceId);
        }
        if (_loadBalancingRulesService.removeFromLoadBalancer(lbId, lstVmId, new HashMap<>())) {
            return instanceId;
        } else {
            return -1;
        }
    }

    private long removeLBrule(final AutoScaleVmGroupVO asGroup) {
        final long lbId = asGroup.getLoadBalancerId();
        long instanceId = -1;
        final List<LoadBalancerVMMapVO> LbVmMapVos = _lbVmMapDao.listByLoadBalancerId(lbId);
        if ((LbVmMapVos != null) && (LbVmMapVos.size() > 0)) {
            for (final LoadBalancerVMMapVO LbVmMapVo : LbVmMapVos) {
                instanceId = LbVmMapVo.getInstanceId();
            }
        }
        // take last VM out of the list
        final List<Long> lstVmId = new ArrayList<>();
        if (instanceId != -1) {
            lstVmId.add(instanceId);
        }
        if (_loadBalancingRulesService.removeFromLoadBalancer(lbId, lstVmId, new HashMap<>())) {
            return instanceId;
        } else {
            return -1;
        }
    }

    private class SearchWrapper<VO extends ControlledEntity> {
        GenericDao<VO, Long> dao;
        SearchBuilder<VO> searchBuilder;
        SearchCriteria<VO> searchCriteria;
        Long domainId;
        boolean isRecursive;
        List<Long> permittedAccounts = new ArrayList<>();
        ListProjectResourcesCriteria listProjectResourcesCriteria;
        Filter searchFilter;

        public SearchWrapper(final GenericDao<VO, Long> dao, final Class<VO> entityClass, final BaseListAccountResourcesCmd cmd, final Long id) {
            this.dao = dao;
            this.searchBuilder = dao.createSearchBuilder();
            domainId = cmd.getDomainId();
            final String accountName = cmd.getAccountName();
            isRecursive = cmd.isRecursive();
            final boolean listAll = cmd.listAll();
            final long startIndex = cmd.getStartIndex();
            final long pageSizeVal = cmd.getPageSizeVal();
            final Account caller = CallContext.current().getCallingAccount();

            final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(domainId, isRecursive, null);
            _accountMgr.buildACLSearchParameters(caller, id, accountName, null, permittedAccounts, domainIdRecursiveListProject,
                    listAll, false);
            domainId = domainIdRecursiveListProject.first();
            isRecursive = domainIdRecursiveListProject.second();
            final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();
            _accountMgr.buildACLSearchBuilder(searchBuilder, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);
            searchFilter = new Filter(entityClass, "id", false, startIndex, pageSizeVal);
        }

        public SearchBuilder<VO> getSearchBuilder() {
            return searchBuilder;
        }

        public SearchCriteria<VO> buildSearchCriteria() {
            searchCriteria = searchBuilder.create();
            _accountMgr.buildACLSearchCriteria(searchCriteria, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);
            return searchCriteria;
        }

        public List<VO> search() {
            return dao.search(searchCriteria, searchFilter);
        }
    }
}
