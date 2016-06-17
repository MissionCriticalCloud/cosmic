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
package com.cloud.api.query;

import com.cloud.api.query.dao.*;
import com.cloud.api.query.vo.*;
import com.cloud.dc.DedicatedResourceVO;
import com.cloud.dc.dao.DataCenterDetailsDao;
import com.cloud.dc.dao.DedicatedResourceDao;
import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.event.dao.EventJoinDao;
import com.cloud.exception.CloudAuthenticationException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.ha.HighAvailabilityManager;
import com.cloud.hypervisor.Hypervisor;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.dao.NetworkDetailsDao;
import com.cloud.network.security.SecurityGroupVMMapVO;
import com.cloud.network.security.dao.SecurityGroupVMMapDao;
import com.cloud.org.Grouping;
import com.cloud.projects.Project;
import com.cloud.projects.Project.ListProjectResourcesCriteria;
import com.cloud.projects.ProjectInvitation;
import com.cloud.projects.ProjectManager;
import com.cloud.projects.dao.ProjectAccountDao;
import com.cloud.projects.dao.ProjectDao;
import com.cloud.resource.ResourceManager;
import com.cloud.server.ResourceMetaDataService;
import com.cloud.server.ResourceTag;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.server.TaggedResourceService;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.*;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.TemplateType;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.tags.ResourceTagVO;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.template.VirtualMachineTemplate.State;
import com.cloud.template.VirtualMachineTemplate.TemplateFilter;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.DomainManager;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.DateUtil;
import com.cloud.utils.Pair;
import com.cloud.utils.Ternary;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.UserVmDetailsDao;
import com.cloud.vm.dao.VMInstanceDao;
import org.apache.cloudstack.acl.ControlledEntity.ACLType;
import org.apache.cloudstack.affinity.AffinityGroupDomainMapVO;
import org.apache.cloudstack.affinity.AffinityGroupResponse;
import org.apache.cloudstack.affinity.AffinityGroupVMMapVO;
import org.apache.cloudstack.affinity.dao.AffinityGroupDomainMapDao;
import org.apache.cloudstack.affinity.dao.AffinityGroupVMMapDao;
import org.apache.cloudstack.api.BaseListProjectAndAccountResourcesCmd;
import org.apache.cloudstack.api.ResourceDetail;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.command.admin.account.ListAccountsCmdByAdmin;
import org.apache.cloudstack.api.command.admin.domain.ListDomainsCmd;
import org.apache.cloudstack.api.command.admin.domain.ListDomainsCmdByAdmin;
import org.apache.cloudstack.api.command.admin.host.ListHostTagsCmd;
import org.apache.cloudstack.api.command.admin.host.ListHostsCmd;
import org.apache.cloudstack.api.command.admin.internallb.ListInternalLBVMsCmd;
import org.apache.cloudstack.api.command.admin.iso.ListIsosCmdByAdmin;
import org.apache.cloudstack.api.command.admin.router.ListRoutersCmd;
import org.apache.cloudstack.api.command.admin.storage.ListImageStoresCmd;
import org.apache.cloudstack.api.command.admin.storage.ListSecondaryStagingStoresCmd;
import org.apache.cloudstack.api.command.admin.storage.ListStoragePoolsCmd;
import org.apache.cloudstack.api.command.admin.storage.ListStorageTagsCmd;
import org.apache.cloudstack.api.command.admin.template.ListTemplatesCmdByAdmin;
import org.apache.cloudstack.api.command.admin.user.ListUsersCmd;
import org.apache.cloudstack.api.command.admin.vm.ListVMsCmdByAdmin;
import org.apache.cloudstack.api.command.admin.volume.ListVolumesCmdByAdmin;
import org.apache.cloudstack.api.command.admin.zone.ListZonesCmdByAdmin;
import org.apache.cloudstack.api.command.user.account.ListAccountsCmd;
import org.apache.cloudstack.api.command.user.account.ListProjectAccountsCmd;
import org.apache.cloudstack.api.command.user.affinitygroup.ListAffinityGroupsCmd;
import org.apache.cloudstack.api.command.user.event.ListEventsCmd;
import org.apache.cloudstack.api.command.user.iso.ListIsosCmd;
import org.apache.cloudstack.api.command.user.job.ListAsyncJobsCmd;
import org.apache.cloudstack.api.command.user.offering.ListDiskOfferingsCmd;
import org.apache.cloudstack.api.command.user.offering.ListServiceOfferingsCmd;
import org.apache.cloudstack.api.command.user.project.ListProjectInvitationsCmd;
import org.apache.cloudstack.api.command.user.project.ListProjectsCmd;
import org.apache.cloudstack.api.command.user.securitygroup.ListSecurityGroupsCmd;
import org.apache.cloudstack.api.command.user.tag.ListTagsCmd;
import org.apache.cloudstack.api.command.user.template.ListTemplatesCmd;
import org.apache.cloudstack.api.command.user.vm.ListVMsCmd;
import org.apache.cloudstack.api.command.user.vmgroup.ListVMGroupsCmd;
import org.apache.cloudstack.api.command.user.volume.ListResourceDetailsCmd;
import org.apache.cloudstack.api.command.user.volume.ListVolumesCmd;
import org.apache.cloudstack.api.command.user.zone.ListZonesCmd;
import org.apache.cloudstack.api.response.*;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.engine.subsystem.api.storage.*;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.query.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;

@Component
public class QueryManagerImpl extends ManagerBase implements QueryService, Configurable {

    public static final Logger s_logger = LoggerFactory.getLogger(QueryManagerImpl.class);

    private static final String ID_FIELD = "id";

    @Inject
    private AccountManager _accountMgr;

    @Inject
    private ProjectManager _projectMgr;

    @Inject
    private DomainDao _domainDao;

    @Inject
    private DomainJoinDao _domainJoinDao;

    @Inject
    private UserAccountJoinDao _userAccountJoinDao;

    @Inject
    private EventJoinDao _eventJoinDao;

    @Inject
    private ResourceTagJoinDao _resourceTagJoinDao;

    @Inject
    private InstanceGroupJoinDao _vmGroupJoinDao;

    @Inject
    private UserVmJoinDao _userVmJoinDao;

    @Inject
    private UserVmDao _userVmDao;

    @Inject
    private VMInstanceDao _vmInstanceDao;

    @Inject
    private SecurityGroupJoinDao _securityGroupJoinDao;

    @Inject
    private SecurityGroupVMMapDao _securityGroupVMMapDao;

    @Inject
    private DomainRouterJoinDao _routerJoinDao;

    @Inject
    private ProjectInvitationJoinDao _projectInvitationJoinDao;

    @Inject
    private ProjectJoinDao _projectJoinDao;

    @Inject
    private ProjectDao _projectDao;

    @Inject
    private ProjectAccountDao _projectAccountDao;

    @Inject
    private ProjectAccountJoinDao _projectAccountJoinDao;

    @Inject
    private HostJoinDao _hostJoinDao;

    @Inject
    private VolumeJoinDao _volumeJoinDao;

    @Inject
    private AccountDao _accountDao;

    @Inject
    private ConfigurationDao _configDao;

    @Inject
    private AccountJoinDao _accountJoinDao;

    @Inject
    private AsyncJobJoinDao _jobJoinDao;

    @Inject
    private StoragePoolJoinDao _poolJoinDao;

    @Inject
    private StorageTagDao _storageTagDao;

    @Inject
    private HostTagDao _hostTagDao;

    @Inject
    private ImageStoreJoinDao _imageStoreJoinDao;

    @Inject
    private DiskOfferingJoinDao _diskOfferingJoinDao;

    @Inject
    private ServiceOfferingJoinDao _srvOfferingJoinDao;

    @Inject
    private ServiceOfferingDao _srvOfferingDao;

    @Inject
    private DataCenterJoinDao _dcJoinDao;

    @Inject
    private DomainRouterDao _routerDao;

    @Inject
    UserVmDetailsDao _userVmDetailDao;

    @Inject
    private HighAvailabilityManager _haMgr;

    @Inject
    private VMTemplateDao _templateDao;

    @Inject
    private TemplateJoinDao _templateJoinDao;

    @Inject
    ResourceManager _resourceMgr;
    @Inject
    private ResourceMetaDataService _resourceMetaDataMgr;

    @Inject
    private TaggedResourceService _taggedResourceMgr;

    @Inject
    AffinityGroupVMMapDao _affinityGroupVMMapDao;

    @Inject
    private AffinityGroupJoinDao _affinityGroupJoinDao;

    @Inject
    private DedicatedResourceDao _dedicatedDao;

    @Inject
    DataCenterDetailsDao _dcDetailsDao;

    @Inject
    DomainManager _domainMgr;

    @Inject
    AffinityGroupDomainMapDao _affinityGroupDomainMapDao;

    @Inject
    NetworkDetailsDao _networkDetailsDao;

    @Inject
    ResourceTagDao _resourceTagDao;

    @Inject
    DataStoreManager dataStoreManager;

    /*
     * (non-Javadoc)
     *
     * @see
     * com.cloud.api.query.QueryService#searchForUsers(org.apache.cloudstack
     * .api.command.admin.user.ListUsersCmd)
     */
    @Override
    public ListResponse<UserResponse> searchForUsers(final ListUsersCmd cmd) throws PermissionDeniedException {
        final Pair<List<UserAccountJoinVO>, Integer> result = searchForUsersInternal(cmd);
        final ListResponse<UserResponse> response = new ListResponse<>();
        final List<UserResponse> userResponses =
                ViewResponseHelper.createUserResponse(CallContext.current().getCallingAccount().getDomainId(),
                        result.first().toArray(new UserAccountJoinVO[result.first().size()]));
        response.setResponses(userResponses, result.second());
        return response;
    }

    private Pair<List<UserAccountJoinVO>, Integer> searchForUsersInternal(final ListUsersCmd cmd) throws PermissionDeniedException {
        final Account caller = CallContext.current().getCallingAccount();

        final List<Long> permittedAccounts = new ArrayList<>();

        final boolean listAll = cmd.listAll();
        Long id = cmd.getId();
        if (caller.getType() == Account.ACCOUNT_TYPE_NORMAL) {
            final long currentId = CallContext.current().getCallingUser().getId();
            if (id != null && currentId != id.longValue()) {
                throw new PermissionDeniedException("Calling user is not authorized to see the user requested by id");
            }
            id = currentId;
        }
        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), null, permittedAccounts,
                domainIdRecursiveListProject, listAll, false);
        final Long domainId = domainIdRecursiveListProject.first();
        final Boolean isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();

        final Filter searchFilter = new Filter(UserAccountJoinVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());

        final Object username = cmd.getUsername();
        final Object type = cmd.getAccountType();
        final Object accountName = cmd.getAccountName();
        final Object state = cmd.getState();
        final Object keyword = cmd.getKeyword();

        final SearchBuilder<UserAccountJoinVO> sb = _userAccountJoinDao.createSearchBuilder();
        _accountMgr.buildACLViewSearchBuilder(sb, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);
        sb.and("username", sb.entity().getUsername(), SearchCriteria.Op.LIKE);
        if (id != null && id == 1) {
            // system user should NOT be searchable
            final List<UserAccountJoinVO> emptyList = new ArrayList<>();
            return new Pair<>(emptyList, 0);
        } else if (id != null) {
            sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        } else {
            // this condition is used to exclude system user from the search
            // results
            sb.and("id", sb.entity().getId(), SearchCriteria.Op.NEQ);
        }

        sb.and("type", sb.entity().getAccountType(), SearchCriteria.Op.EQ);
        sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);
        sb.and("accountName", sb.entity().getAccountName(), SearchCriteria.Op.EQ);
        sb.and("state", sb.entity().getState(), SearchCriteria.Op.EQ);

        if (accountName == null && domainId != null) {
            sb.and("domainPath", sb.entity().getDomainPath(), SearchCriteria.Op.LIKE);
        }

        final SearchCriteria<UserAccountJoinVO> sc = sb.create();

        // building ACL condition
        _accountMgr.buildACLViewSearchCriteria(sc, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        if (keyword != null) {
            final SearchCriteria<UserAccountJoinVO> ssc = _userAccountJoinDao.createSearchCriteria();
            ssc.addOr("username", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("firstname", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("lastname", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("email", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("state", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("accountName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("accountType", SearchCriteria.Op.LIKE, "%" + keyword + "%");

            sc.addAnd("username", SearchCriteria.Op.SC, ssc);
        }

        if (username != null) {
            sc.setParameters("username", username);
        }

        if (id != null) {
            sc.setParameters("id", id);
        } else {
            // Don't return system user, search builder with NEQ
            sc.setParameters("id", 1);
        }

        if (type != null) {
            sc.setParameters("type", type);
        }

        if (accountName != null) {
            sc.setParameters("accountName", accountName);
            if (domainId != null) {
                sc.setParameters("domainId", domainId);
            }
        } else if (domainId != null) {
            final DomainVO domainVO = _domainDao.findById(domainId);
            sc.setParameters("domainPath", domainVO.getPath() + "%");
        }

        if (state != null) {
            sc.setParameters("state", state);
        }

        return _userAccountJoinDao.searchAndCount(sc, searchFilter);
    }

    @Override
    public ListResponse<EventResponse> searchForEvents(final ListEventsCmd cmd) {
        final Pair<List<EventJoinVO>, Integer> result = searchForEventsInternal(cmd);
        final ListResponse<EventResponse> response = new ListResponse<>();
        final List<EventResponse> eventResponses = ViewResponseHelper.createEventResponse(result.first().toArray(new EventJoinVO[result.first().size()]));
        response.setResponses(eventResponses, result.second());
        return response;
    }

    private Pair<List<EventJoinVO>, Integer> searchForEventsInternal(final ListEventsCmd cmd) {
        final Account caller = CallContext.current().getCallingAccount();
        final List<Long> permittedAccounts = new ArrayList<>();

        final Long id = cmd.getId();
        final String type = cmd.getType();
        final String level = cmd.getLevel();
        final Date startDate = cmd.getStartDate();
        final Date endDate = cmd.getEndDate();
        final String keyword = cmd.getKeyword();
        final Integer entryTime = cmd.getEntryTime();
        final Integer duration = cmd.getDuration();

        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), cmd.getProjectId(), permittedAccounts,
                domainIdRecursiveListProject, cmd.listAll(), false);
        final Long domainId = domainIdRecursiveListProject.first();
        final Boolean isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();

        final Filter searchFilter = new Filter(EventJoinVO.class, "createDate", false, cmd.getStartIndex(), cmd.getPageSizeVal());
        final SearchBuilder<EventJoinVO> sb = _eventJoinDao.createSearchBuilder();
        _accountMgr.buildACLViewSearchBuilder(sb, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("levelL", sb.entity().getLevel(), SearchCriteria.Op.LIKE);
        sb.and("levelEQ", sb.entity().getLevel(), SearchCriteria.Op.EQ);
        sb.and("type", sb.entity().getType(), SearchCriteria.Op.EQ);
        sb.and("createDateB", sb.entity().getCreateDate(), SearchCriteria.Op.BETWEEN);
        sb.and("createDateG", sb.entity().getCreateDate(), SearchCriteria.Op.GTEQ);
        sb.and("createDateL", sb.entity().getCreateDate(), SearchCriteria.Op.LTEQ);
        sb.and("state", sb.entity().getState(), SearchCriteria.Op.NEQ);
        sb.and("startId", sb.entity().getStartId(), SearchCriteria.Op.EQ);
        sb.and("createDate", sb.entity().getCreateDate(), SearchCriteria.Op.BETWEEN);
        sb.and("displayEvent", sb.entity().getDisplay(), SearchCriteria.Op.EQ);
        sb.and("archived", sb.entity().getArchived(), SearchCriteria.Op.EQ);

        final SearchCriteria<EventJoinVO> sc = sb.create();
        // building ACL condition
        _accountMgr.buildACLViewSearchCriteria(sc, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        // For end users display only enabled events
        if (!_accountMgr.isRootAdmin(caller.getId())) {
            sc.setParameters("displayEvent", true);
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (keyword != null) {
            final SearchCriteria<EventJoinVO> ssc = _eventJoinDao.createSearchCriteria();
            ssc.addOr("type", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("level", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("level", SearchCriteria.Op.SC, ssc);
        }

        if (level != null) {
            sc.setParameters("levelEQ", level);
        }

        if (type != null) {
            sc.setParameters("type", type);
        }

        if (startDate != null && endDate != null) {
            sc.setParameters("createDateB", startDate, endDate);
        } else if (startDate != null) {
            sc.setParameters("createDateG", startDate);
        } else if (endDate != null) {
            sc.setParameters("createDateL", endDate);
        }

        sc.setParameters("archived", false);

        Pair<List<EventJoinVO>, Integer> eventPair = null;
        // event_view will not have duplicate rows for each event, so
        // searchAndCount should be good enough.
        if (entryTime != null && duration != null) {
            // TODO: waiting for response from dev list, logic is mystery to
            // me!!
      /*
       * if (entryTime <= duration) { throw new
       * InvalidParameterValueException
       * ("Entry time must be greater than duration"); } Calendar calMin =
       * Calendar.getInstance(); Calendar calMax = Calendar.getInstance();
       * calMin.add(Calendar.SECOND, -entryTime);
       * calMax.add(Calendar.SECOND, -duration); Date minTime =
       * calMin.getTime(); Date maxTime = calMax.getTime();
       *
       * sc.setParameters("state", com.cloud.event.Event.State.Completed);
       * sc.setParameters("startId", 0); sc.setParameters("createDate",
       * minTime, maxTime); List<EventJoinVO> startedEvents =
       * _eventJoinDao.searchAllEvents(sc, searchFilter);
       * List<EventJoinVO> pendingEvents = new ArrayList<EventJoinVO>();
       * for (EventVO event : startedEvents) { EventVO completedEvent =
       * _eventDao.findCompletedEvent(event.getId()); if (completedEvent
       * == null) { pendingEvents.add(event); } } return pendingEvents;
       */
        } else {
            eventPair = _eventJoinDao.searchAndCount(sc, searchFilter);
        }
        return eventPair;

    }

    @Override
    public ListResponse<ResourceTagResponse> listTags(final ListTagsCmd cmd) {
        final Pair<List<ResourceTagJoinVO>, Integer> tags = listTagsInternal(cmd);
        final ListResponse<ResourceTagResponse> response = new ListResponse<>();
        final List<ResourceTagResponse> tagResponses = ViewResponseHelper.createResourceTagResponse(false, tags.first().toArray(new ResourceTagJoinVO[tags.first().size()]));
        response.setResponses(tagResponses, tags.second());
        return response;
    }

    private Pair<List<ResourceTagJoinVO>, Integer> listTagsInternal(final ListTagsCmd cmd) {
        final Account caller = CallContext.current().getCallingAccount();
        final List<Long> permittedAccounts = new ArrayList<>();
        final String key = cmd.getKey();
        final String value = cmd.getValue();
        final String resourceId = cmd.getResourceId();
        final String resourceType = cmd.getResourceType();
        final String customerName = cmd.getCustomer();
        final boolean listAll = cmd.listAll();

        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject =
                new Ternary<>(cmd.getDomainId(), cmd.isRecursive(), null);

        _accountMgr.buildACLSearchParameters(caller, null, cmd.getAccountName(), cmd.getProjectId(), permittedAccounts,
                domainIdRecursiveListProject, listAll, false);
        final Long domainId = domainIdRecursiveListProject.first();
        final Boolean isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();
        final Filter searchFilter = new Filter(ResourceTagJoinVO.class, "resourceType", false, cmd.getStartIndex(), cmd.getPageSizeVal());

        final SearchBuilder<ResourceTagJoinVO> sb = _resourceTagJoinDao.createSearchBuilder();
        _accountMgr.buildACLViewSearchBuilder(sb, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        sb.and("key", sb.entity().getKey(), SearchCriteria.Op.EQ);
        sb.and("value", sb.entity().getValue(), SearchCriteria.Op.EQ);

        if (resourceId != null) {
            sb.and("resourceId", sb.entity().getResourceId(), SearchCriteria.Op.EQ);
            sb.and("resourceUuid", sb.entity().getResourceUuid(), SearchCriteria.Op.EQ);
        }

        sb.and("resourceType", sb.entity().getResourceType(), SearchCriteria.Op.EQ);
        sb.and("customer", sb.entity().getCustomer(), SearchCriteria.Op.EQ);

        // now set the SC criteria...
        final SearchCriteria<ResourceTagJoinVO> sc = sb.create();
        _accountMgr.buildACLViewSearchCriteria(sc, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        if (key != null) {
            sc.setParameters("key", key);
        }

        if (value != null) {
            sc.setParameters("value", value);
        }

        if (resourceId != null) {
            try {
                final long rid = Long.parseLong(resourceId);
                sc.setParameters("resourceId", rid);
            } catch (final NumberFormatException ex) {
                // internal id instead of resource id is passed
                sc.setParameters("resourceUuid", resourceId);
            }
        }

        if (resourceType != null) {
            sc.setParameters("resourceType", resourceType);
        }

        if (customerName != null) {
            sc.setParameters("customer", customerName);
        }

        final Pair<List<ResourceTagJoinVO>, Integer> result = _resourceTagJoinDao.searchAndCount(sc, searchFilter);
        return result;
    }

    @Override
    public ListResponse<InstanceGroupResponse> searchForVmGroups(final ListVMGroupsCmd cmd) {
        final Pair<List<InstanceGroupJoinVO>, Integer> groups = searchForVmGroupsInternal(cmd);
        final ListResponse<InstanceGroupResponse> response = new ListResponse<>();
        final List<InstanceGroupResponse> grpResponses = ViewResponseHelper.createInstanceGroupResponse(groups.first().toArray(new InstanceGroupJoinVO[groups.first().size()]));
        response.setResponses(grpResponses, groups.second());
        return response;
    }

    private Pair<List<InstanceGroupJoinVO>, Integer> searchForVmGroupsInternal(final ListVMGroupsCmd cmd) {
        final Long id = cmd.getId();
        final String name = cmd.getGroupName();
        final String keyword = cmd.getKeyword();

        final Account caller = CallContext.current().getCallingAccount();
        final List<Long> permittedAccounts = new ArrayList<>();

        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), cmd.getProjectId(), permittedAccounts,
                domainIdRecursiveListProject, cmd.listAll(), false);
        final Long domainId = domainIdRecursiveListProject.first();
        final Boolean isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();

        final Filter searchFilter = new Filter(InstanceGroupJoinVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());

        final SearchBuilder<InstanceGroupJoinVO> sb = _vmGroupJoinDao.createSearchBuilder();
        _accountMgr.buildACLViewSearchBuilder(sb, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.LIKE);

        final SearchCriteria<InstanceGroupJoinVO> sc = sb.create();
        _accountMgr.buildACLViewSearchCriteria(sc, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);


        if (keyword != null) {
            final SearchCriteria<InstanceGroupJoinVO> ssc = _vmGroupJoinDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (name != null) {
            sc.setParameters("name", "%" + name + "%");
        }

        return _vmGroupJoinDao.searchAndCount(sc, searchFilter);
    }

    @Override
    public ListResponse<UserVmResponse> searchForUserVMs(final ListVMsCmd cmd) {
        final Pair<List<UserVmJoinVO>, Integer> result = searchForUserVMsInternal(cmd);
        final ListResponse<UserVmResponse> response = new ListResponse<>();
        ResponseView respView = ResponseView.Restricted;
        if (cmd instanceof ListVMsCmdByAdmin) {
            respView = ResponseView.Full;
        }
        final List<UserVmResponse> vmResponses = ViewResponseHelper.createUserVmResponse(respView, "virtualmachine", cmd.getDetails(),
                result.first().toArray(new UserVmJoinVO[result.first().size()]));

        response.setResponses(vmResponses, result.second());
        return response;
    }

    private Pair<List<UserVmJoinVO>, Integer> searchForUserVMsInternal(final ListVMsCmd cmd) {
        final Account caller = CallContext.current().getCallingAccount();
        final List<Long> permittedAccounts = new ArrayList<>();

        final boolean listAll = cmd.listAll();
        final Long id = cmd.getId();
        final Long userId = cmd.getUserId();
        final Map<String, String> tags = cmd.getTags();
        final Boolean display = cmd.getDisplay();
        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), cmd.getProjectId(), permittedAccounts,
                domainIdRecursiveListProject, listAll, false);
        final Long domainId = domainIdRecursiveListProject.first();
        final Boolean isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();

        final Filter searchFilter = new Filter(UserVmJoinVO.class, "id", true, cmd.getStartIndex(),
                cmd.getPageSizeVal());

        List<Long> ids = null;
        if (cmd.getId() != null) {
            if (cmd.getIds() != null && !cmd.getIds().isEmpty()) {
                throw new InvalidParameterValueException("Specify either id or ids but not both parameters");
            }
            ids = new ArrayList<>();
            ids.add(cmd.getId());
        } else {
            ids = cmd.getIds();
        }

        // first search distinct vm id by using query criteria and pagination
        final SearchBuilder<UserVmJoinVO> sb = _userVmJoinDao.createSearchBuilder();
        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct ids

        _accountMgr.buildACLViewSearchBuilder(sb, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        final String hypervisor = cmd.getHypervisor();
        final Object name = cmd.getName();
        final String state = cmd.getState();
        final Object zoneId = cmd.getZoneId();
        final Object keyword = cmd.getKeyword();
        boolean isAdmin = false;
        boolean isRootAdmin = false;
        if (_accountMgr.isAdmin(caller.getId())) {
            isAdmin = true;
        }
        if (_accountMgr.isRootAdmin(caller.getId())) {
            isRootAdmin = true;
        }

        final Object groupId = cmd.getGroupId();
        final Object networkId = cmd.getNetworkId();
        if (HypervisorType.getType(hypervisor) == HypervisorType.None && hypervisor != null) {
            // invalid hypervisor type input
            throw new InvalidParameterValueException("Invalid HypervisorType " + hypervisor);
        }
        final Object templateId = cmd.getTemplateId();
        final Object isoId = cmd.getIsoId();
        final Object vpcId = cmd.getVpcId();
        final Object affinityGroupId = cmd.getAffinityGroupId();
        final Object keyPairName = cmd.getKeyPairName();
        final Object serviceOffId = cmd.getServiceOfferingId();
        Object pod = null;
        Object hostId = null;
        Object storageId = null;
        if (cmd instanceof ListVMsCmdByAdmin) {
            final ListVMsCmdByAdmin adCmd = (ListVMsCmdByAdmin) cmd;
            pod = adCmd.getPodId();
            hostId = adCmd.getHostId();
            storageId = adCmd.getStorageId();
        }

        sb.and("displayName", sb.entity().getDisplayName(), SearchCriteria.Op.LIKE);
        sb.and("idIN", sb.entity().getId(), SearchCriteria.Op.IN);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.LIKE);
        sb.and("stateEQ", sb.entity().getState(), SearchCriteria.Op.EQ);
        sb.and("stateNEQ", sb.entity().getState(), SearchCriteria.Op.NEQ);
        sb.and("stateNIN", sb.entity().getState(), SearchCriteria.Op.NIN);
        sb.and("dataCenterId", sb.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        sb.and("podId", sb.entity().getPodId(), SearchCriteria.Op.EQ);
        sb.and("hypervisorType", sb.entity().getHypervisorType(), SearchCriteria.Op.EQ);
        sb.and("hostIdEQ", sb.entity().getHostId(), SearchCriteria.Op.EQ);
        sb.and("templateId", sb.entity().getTemplateId(), SearchCriteria.Op.EQ);
        sb.and("isoId", sb.entity().getIsoId(), SearchCriteria.Op.EQ);
        sb.and("instanceGroupId", sb.entity().getInstanceGroupId(), SearchCriteria.Op.EQ);

        if (serviceOffId != null) {
            sb.and("serviceOfferingId", sb.entity().getServiceOfferingId(), SearchCriteria.Op.EQ);
        }
        if (display != null) {
            sb.and("display", sb.entity().isDisplayVm(), SearchCriteria.Op.EQ);
        }
        if (groupId != null && (Long) groupId != -1) {
            sb.and("instanceGroupId", sb.entity().getInstanceGroupId(), SearchCriteria.Op.EQ);
        }

        if (userId != null) {
            sb.and("userId", sb.entity().getUserId(), SearchCriteria.Op.EQ);
        }

        if (networkId != null) {
            sb.and("networkId", sb.entity().getNetworkId(), SearchCriteria.Op.EQ);
        }

        if (vpcId != null && networkId == null) {
            sb.and("vpcId", sb.entity().getVpcId(), SearchCriteria.Op.EQ);
        }

        if (storageId != null) {
            sb.and("poolId", sb.entity().getPoolId(), SearchCriteria.Op.EQ);
        }

        if (affinityGroupId != null) {
            sb.and("affinityGroupId", sb.entity().getAffinityGroupId(), SearchCriteria.Op.EQ);
        }

        if (keyPairName != null) {
            sb.and("keyPairName", sb.entity().getKeypairName(), SearchCriteria.Op.EQ);
        }

        if (!isRootAdmin) {
            sb.and("displayVm", sb.entity().isDisplayVm(), SearchCriteria.Op.EQ);
        }

        // populate the search criteria with the values passed in
        final SearchCriteria<UserVmJoinVO> sc = sb.create();

        // building ACL condition
        _accountMgr.buildACLViewSearchCriteria(sc, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        if (tags != null && !tags.isEmpty()) {
            final SearchCriteria<UserVmJoinVO> tagSc = _userVmJoinDao.createSearchCriteria();
            for (final Map.Entry<String, String> entry : tags.entrySet()) {
                final SearchCriteria<UserVmJoinVO> tsc = _userVmJoinDao.createSearchCriteria();
                tsc.addAnd("tagKey", SearchCriteria.Op.EQ, entry.getKey());
                tsc.addAnd("tagValue", SearchCriteria.Op.EQ, entry.getValue());
                tagSc.addOr("tagKey", SearchCriteria.Op.SC, tsc);
            }
            sc.addAnd("tagKey", SearchCriteria.Op.SC, tagSc);
        }

        if (groupId != null && (Long) groupId != -1) {
            sc.setParameters("instanceGroupId", groupId);
        }

        if (keyword != null) {
            final SearchCriteria<UserVmJoinVO> ssc = _userVmJoinDao.createSearchCriteria();
            ssc.addOr("displayName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("instanceName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("state", SearchCriteria.Op.EQ, keyword);
            sc.addAnd("displayName", SearchCriteria.Op.SC, ssc);
        }

        if (serviceOffId != null) {
            sc.setParameters("serviceOfferingId", serviceOffId);
        }

        if (display != null) {
            sc.setParameters("display", display);
        }

        if (ids != null && !ids.isEmpty()) {
            sc.setParameters("idIN", ids.toArray());
        }

        if (templateId != null) {
            sc.setParameters("templateId", templateId);
        }

        if (isoId != null) {
            sc.setParameters("isoId", isoId);
        }

        if (userId != null) {
            sc.setParameters("userId", userId);
        }

        if (networkId != null) {
            sc.setParameters("networkId", networkId);
        }

        if (vpcId != null && networkId == null) {
            sc.setParameters("vpcId", vpcId);
        }

        if (name != null) {
            sc.setParameters("name", "%" + name + "%");
        }

        if (state != null) {
            if (state.equalsIgnoreCase("present")) {
                sc.setParameters("stateNIN", "Destroyed", "Expunging");
            } else {
                sc.setParameters("stateEQ", state);
            }
        }

        if (hypervisor != null) {
            sc.setParameters("hypervisorType", hypervisor);
        }

        // Don't show Destroyed and Expunging vms to the end user if the AllowUserViewDestroyedVM flag is not set.
        if (!isAdmin && !AllowUserViewDestroyedVM.valueIn(caller.getAccountId())) {
            sc.setParameters("stateNIN", "Destroyed", "Expunging");
        }

        if (zoneId != null) {
            sc.setParameters("dataCenterId", zoneId);
        }

        if (affinityGroupId != null) {
            sc.setParameters("affinityGroupId", affinityGroupId);
        }

        if (keyPairName != null) {
            sc.setParameters("keyPairName", keyPairName);
        }

        if (cmd instanceof ListVMsCmdByAdmin) {
            final ListVMsCmdByAdmin aCmd = (ListVMsCmdByAdmin) cmd;
            if (aCmd.getPodId() != null) {
                sc.setParameters("podId", pod);

                if (state == null) {
                    sc.setParameters("stateNEQ", "Destroyed");
                }
            }

            if (hostId != null) {
                sc.setParameters("hostIdEQ", hostId);
            }

            if (storageId != null) {
                sc.setParameters("poolId", storageId);
            }
        }

        if (!isRootAdmin) {
            sc.setParameters("displayVm", 1);
        }
        // search vm details by ids
        final Pair<List<UserVmJoinVO>, Integer> uniqueVmPair = _userVmJoinDao.searchAndDistinctCount(sc, searchFilter);
        final Integer count = uniqueVmPair.second();
        if (count.intValue() == 0) {
            // handle empty result cases
            return uniqueVmPair;
        }
        final List<UserVmJoinVO> uniqueVms = uniqueVmPair.first();
        final Long[] vmIds = new Long[uniqueVms.size()];
        int i = 0;
        for (final UserVmJoinVO v : uniqueVms) {
            vmIds[i++] = v.getId();
        }
        final List<UserVmJoinVO> vms = _userVmJoinDao.searchByIds(vmIds);
        return new Pair<>(vms, count);
    }

    @Override
    public ListResponse<SecurityGroupResponse> searchForSecurityGroups(final ListSecurityGroupsCmd cmd) {
        final Pair<List<SecurityGroupJoinVO>, Integer> result = searchForSecurityGroupsInternal(cmd);
        final ListResponse<SecurityGroupResponse> response = new ListResponse<>();
        final List<SecurityGroupResponse> routerResponses = ViewResponseHelper.createSecurityGroupResponses(result.first());
        response.setResponses(routerResponses, result.second());
        return response;
    }

    private Pair<List<SecurityGroupJoinVO>, Integer> searchForSecurityGroupsInternal(final ListSecurityGroupsCmd cmd) throws PermissionDeniedException,
            InvalidParameterValueException {
        final Account caller = CallContext.current().getCallingAccount();
        final Long instanceId = cmd.getVirtualMachineId();
        final String securityGroup = cmd.getSecurityGroupName();
        final Long id = cmd.getId();
        final Object keyword = cmd.getKeyword();
        final List<Long> permittedAccounts = new ArrayList<>();
        final Map<String, String> tags = cmd.getTags();

        if (instanceId != null) {
            final UserVmVO userVM = _userVmDao.findById(instanceId);
            if (userVM == null) {
                throw new InvalidParameterValueException("Unable to list network groups for virtual machine instance " + instanceId + "; instance not found.");
            }
            _accountMgr.checkAccess(caller, null, true, userVM);
            return listSecurityGroupRulesByVM(instanceId.longValue(), cmd.getStartIndex(), cmd.getPageSizeVal());
        }

        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), cmd.getProjectId(), permittedAccounts,
                domainIdRecursiveListProject, cmd.listAll(), false);
        final Long domainId = domainIdRecursiveListProject.first();
        final Boolean isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();

        final Filter searchFilter = new Filter(SecurityGroupJoinVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());
        final SearchBuilder<SecurityGroupJoinVO> sb = _securityGroupJoinDao.createSearchBuilder();
        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct
        // ids
        _accountMgr.buildACLViewSearchBuilder(sb, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.EQ);

        final SearchCriteria<SecurityGroupJoinVO> sc = sb.create();
        _accountMgr.buildACLViewSearchCriteria(sc, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (tags != null && !tags.isEmpty()) {
            final SearchCriteria<SecurityGroupJoinVO> tagSc = _securityGroupJoinDao.createSearchCriteria();
            for (final String key : tags.keySet()) {
                final SearchCriteria<SecurityGroupJoinVO> tsc = _securityGroupJoinDao.createSearchCriteria();
                tsc.addAnd("tagKey", SearchCriteria.Op.EQ, key);
                tsc.addAnd("tagValue", SearchCriteria.Op.EQ, tags.get(key));
                tagSc.addOr("tagKey", SearchCriteria.Op.SC, tsc);
            }
            sc.addAnd("tagKey", SearchCriteria.Op.SC, tagSc);
        }

        if (securityGroup != null) {
            sc.setParameters("name", securityGroup);
        }

        if (keyword != null) {
            final SearchCriteria<SecurityGroupJoinVO> ssc = _securityGroupJoinDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        // search security group together with rules
        final Pair<List<SecurityGroupJoinVO>, Integer> uniqueSgPair = _securityGroupJoinDao.searchAndCount(sc, searchFilter);
        final Integer count = uniqueSgPair.second();
        if (count.intValue() == 0) {
            // handle empty result cases
            return uniqueSgPair;
        }

        final List<SecurityGroupJoinVO> uniqueSgs = uniqueSgPair.first();
        final Long[] sgIds = new Long[uniqueSgs.size()];
        int i = 0;
        for (final SecurityGroupJoinVO v : uniqueSgs) {
            sgIds[i++] = v.getId();
        }
        final List<SecurityGroupJoinVO> sgs = _securityGroupJoinDao.searchByIds(sgIds);
        return new Pair<>(sgs, count);
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

    @Override
    public ListResponse<DomainRouterResponse> searchForRouters(final ListRoutersCmd cmd) {
        final Pair<List<DomainRouterJoinVO>, Integer> result =
                searchForRoutersInternal(cmd, cmd.getId(), cmd.getRouterName(), cmd.getState(), cmd.getZoneId(), cmd.getPodId(), cmd.getClusterId(), cmd.getHostId(),
                        cmd.getKeyword(), cmd.getNetworkId(), cmd.getVpcId(), cmd.getForVpc(), cmd.getRole(), cmd.getVersion());
        final ListResponse<DomainRouterResponse> response = new ListResponse<>();

        final List<DomainRouterResponse> routerResponses = ViewResponseHelper.createDomainRouterResponse(result.first().toArray(new DomainRouterJoinVO[result.first().size()]));
        response.setResponses(routerResponses, result.second());
        return response;
    }

    @Override
    public ListResponse<DomainRouterResponse> searchForInternalLbVms(final ListInternalLBVMsCmd cmd) {
        final Pair<List<DomainRouterJoinVO>, Integer> result =
                searchForRoutersInternal(cmd, cmd.getId(), cmd.getRouterName(), cmd.getState(), cmd.getZoneId(), cmd.getPodId(), null, cmd.getHostId(), cmd.getKeyword(),
                        cmd.getNetworkId(), cmd.getVpcId(), cmd.getForVpc(), cmd.getRole(), null);
        final ListResponse<DomainRouterResponse> response = new ListResponse<>();

        final List<DomainRouterResponse> routerResponses = ViewResponseHelper.createDomainRouterResponse(result.first().toArray(new DomainRouterJoinVO[result.first().size()]));
        response.setResponses(routerResponses, result.second());
        return response;
    }

    private Pair<List<DomainRouterJoinVO>, Integer> searchForRoutersInternal(final BaseListProjectAndAccountResourcesCmd cmd, final Long id, final String name, final String state, final Long zoneId,
                                                                             final Long podId, final Long clusterId, final Long hostId, final String keyword, final Long networkId, final Long vpcId, final Boolean forVpc, final String role, final String version) {

        final Account caller = CallContext.current().getCallingAccount();
        final List<Long> permittedAccounts = new ArrayList<>();

        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), cmd.getProjectId(), permittedAccounts,
                domainIdRecursiveListProject, cmd.listAll(), false);
        final Long domainId = domainIdRecursiveListProject.first();
        final Boolean isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();
        final Filter searchFilter = new Filter(DomainRouterJoinVO.class, "id", true, cmd.getStartIndex(),
                cmd.getPageSizeVal());

        final SearchBuilder<DomainRouterJoinVO> sb = _routerJoinDao.createSearchBuilder();
        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct
        // ids to get
        // number of
        // records with
        // pagination
        _accountMgr.buildACLViewSearchBuilder(sb, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        sb.and("name", sb.entity().getInstanceName(), SearchCriteria.Op.LIKE);
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("accountId", sb.entity().getAccountId(), SearchCriteria.Op.IN);
        sb.and("state", sb.entity().getState(), SearchCriteria.Op.EQ);
        sb.and("dataCenterId", sb.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        sb.and("podId", sb.entity().getPodId(), SearchCriteria.Op.EQ);
        sb.and("clusterId", sb.entity().getClusterId(), SearchCriteria.Op.EQ);
        sb.and("hostId", sb.entity().getHostId(), SearchCriteria.Op.EQ);
        sb.and("vpcId", sb.entity().getVpcId(), SearchCriteria.Op.EQ);
        sb.and("role", sb.entity().getRole(), SearchCriteria.Op.EQ);
        sb.and("version", sb.entity().getTemplateVersion(), SearchCriteria.Op.LIKE);

        if (forVpc != null) {
            if (forVpc) {
                sb.and("forVpc", sb.entity().getVpcId(), SearchCriteria.Op.NNULL);
            } else {
                sb.and("forVpc", sb.entity().getVpcId(), SearchCriteria.Op.NULL);
            }
        }

        if (networkId != null) {
            sb.and("networkId", sb.entity().getNetworkId(), SearchCriteria.Op.EQ);
        }

        final SearchCriteria<DomainRouterJoinVO> sc = sb.create();
        _accountMgr.buildACLViewSearchCriteria(sc, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        if (keyword != null) {
            final SearchCriteria<DomainRouterJoinVO> ssc = _routerJoinDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("instanceName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("state", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("networkName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("vpcName", SearchCriteria.Op.LIKE, "%" + keyword + "%");

            sc.addAnd("instanceName", SearchCriteria.Op.SC, ssc);
        }

        if (name != null) {
            sc.setParameters("name", "%" + name + "%");
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (state != null) {
            sc.setParameters("state", state);
        }

        if (zoneId != null) {
            sc.setParameters("dataCenterId", zoneId);
        }

        if (podId != null) {
            sc.setParameters("podId", podId);
        }

        if (clusterId != null) {
            sc.setParameters("clusterId", clusterId);
        }

        if (hostId != null) {
            sc.setParameters("hostId", hostId);
        }

        if (networkId != null) {
            sc.setParameters("networkId", networkId);
        }

        if (vpcId != null) {
            sc.setParameters("vpcId", vpcId);
        }

        if (role != null) {
            sc.setParameters("role", role);
        }

        if (version != null) {
            sc.setParameters("version", "Cloudstack Release " + version + "%");
        }

        // search VR details by ids
        final Pair<List<DomainRouterJoinVO>, Integer> uniqueVrPair = _routerJoinDao.searchAndCount(sc, searchFilter);
        final Integer count = uniqueVrPair.second();
        if (count.intValue() == 0) {
            // empty result
            return uniqueVrPair;
        }
        final List<DomainRouterJoinVO> uniqueVrs = uniqueVrPair.first();
        final Long[] vrIds = new Long[uniqueVrs.size()];
        int i = 0;
        for (final DomainRouterJoinVO v : uniqueVrs) {
            vrIds[i++] = v.getId();
        }
        final List<DomainRouterJoinVO> vrs = _routerJoinDao.searchByIds(vrIds);
        return new Pair<>(vrs, count);
    }

    @Override
    public ListResponse<ProjectResponse> listProjects(final ListProjectsCmd cmd) {
        final Pair<List<ProjectJoinVO>, Integer> projects = listProjectsInternal(cmd);
        final ListResponse<ProjectResponse> response = new ListResponse<>();
        final List<ProjectResponse> projectResponses = ViewResponseHelper.createProjectResponse(projects.first().toArray(new ProjectJoinVO[projects.first().size()]));
        response.setResponses(projectResponses, projects.second());
        return response;
    }

    private Pair<List<ProjectJoinVO>, Integer> listProjectsInternal(final ListProjectsCmd cmd) {

        final Long id = cmd.getId();
        final String name = cmd.getName();
        final String displayText = cmd.getDisplayText();
        final String state = cmd.getState();
        final String accountName = cmd.getAccountName();
        final Long domainId = cmd.getDomainId();
        final String keyword = cmd.getKeyword();
        final Long startIndex = cmd.getStartIndex();
        final Long pageSize = cmd.getPageSizeVal();
        final boolean listAll = cmd.listAll();
        final boolean isRecursive = cmd.isRecursive();
        cmd.getTags();

        final Account caller = CallContext.current().getCallingAccount();
        Long accountId = null;
        String path = null;

        final Filter searchFilter = new Filter(ProjectJoinVO.class, "id", false, startIndex, pageSize);
        final SearchBuilder<ProjectJoinVO> sb = _projectJoinDao.createSearchBuilder();
        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct
        // ids

        if (_accountMgr.isAdmin(caller.getId())) {
            if (domainId != null) {
                final DomainVO domain = _domainDao.findById(domainId);
                if (domain == null) {
                    throw new InvalidParameterValueException("Domain id=" + domainId + " doesn't exist in the system");
                }

                _accountMgr.checkAccess(caller, domain);

                if (accountName != null) {
                    final Account owner = _accountMgr.getActiveAccountByName(accountName, domainId);
                    if (owner == null) {
                        throw new InvalidParameterValueException("Unable to find account " + accountName + " in domain " + domainId);
                    }
                    accountId = owner.getId();
                }
            } else { // domainId == null
                if (accountName != null) {
                    throw new InvalidParameterValueException("could not find account " + accountName + " because domain is not specified");
                }

            }
        } else {
            if (accountName != null && !accountName.equals(caller.getAccountName())) {
                throw new PermissionDeniedException("Can't list account " + accountName + " projects; unauthorized");
            }

            if (domainId != null && !domainId.equals(caller.getDomainId())) {
                throw new PermissionDeniedException("Can't list domain id= " + domainId + " projects; unauthorized");
            }

            accountId = caller.getId();
        }

        if (domainId == null && accountId == null && (_accountMgr.isNormalUser(caller.getId()) || !listAll)) {
            accountId = caller.getId();
        } else if (_accountMgr.isDomainAdmin(caller.getId()) || isRecursive && !listAll) {
            final DomainVO domain = _domainDao.findById(caller.getDomainId());
            path = domain.getPath();
        }

        if (path != null) {
            sb.and("domainPath", sb.entity().getDomainPath(), SearchCriteria.Op.LIKE);
        }

        if (accountId != null) {
            sb.and("accountId", sb.entity().getAccountId(), SearchCriteria.Op.EQ);
        }

        final SearchCriteria<ProjectJoinVO> sc = sb.create();

        if (id != null) {
            sc.addAnd("id", Op.EQ, id);
        }

        if (domainId != null && !isRecursive) {
            sc.addAnd("domainId", Op.EQ, domainId);
        }

        if (name != null) {
            sc.addAnd("name", Op.EQ, name);
        }

        if (displayText != null) {
            sc.addAnd("displayText", Op.EQ, displayText);
        }

        if (accountId != null) {
            sc.setParameters("accountId", accountId);
        }

        if (state != null) {
            sc.addAnd("state", Op.EQ, state);
        }

        if (keyword != null) {
            final SearchCriteria<ProjectJoinVO> ssc = _projectJoinDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("displayText", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (path != null) {
            sc.setParameters("domainPath", path);
        }

        // search distinct projects to get count
        final Pair<List<ProjectJoinVO>, Integer> uniquePrjPair = _projectJoinDao.searchAndCount(sc, searchFilter);
        final Integer count = uniquePrjPair.second();
        if (count.intValue() == 0) {
            // handle empty result cases
            return uniquePrjPair;
        }
        final List<ProjectJoinVO> uniquePrjs = uniquePrjPair.first();
        final Long[] prjIds = new Long[uniquePrjs.size()];
        int i = 0;
        for (final ProjectJoinVO v : uniquePrjs) {
            prjIds[i++] = v.getId();
        }
        final List<ProjectJoinVO> prjs = _projectJoinDao.searchByIds(prjIds);
        return new Pair<>(prjs, count);
    }

    @Override
    public ListResponse<ProjectInvitationResponse> listProjectInvitations(final ListProjectInvitationsCmd cmd) {
        final Pair<List<ProjectInvitationJoinVO>, Integer> invites = listProjectInvitationsInternal(cmd);
        final ListResponse<ProjectInvitationResponse> response = new ListResponse<>();
        final List<ProjectInvitationResponse> projectInvitationResponses =
                ViewResponseHelper.createProjectInvitationResponse(invites.first().toArray(new ProjectInvitationJoinVO[invites.first().size()]));

        response.setResponses(projectInvitationResponses, invites.second());
        return response;
    }

    public Pair<List<ProjectInvitationJoinVO>, Integer> listProjectInvitationsInternal(final ListProjectInvitationsCmd cmd) {
        final Long id = cmd.getId();
        final Long projectId = cmd.getProjectId();
        final String accountName = cmd.getAccountName();
        Long domainId = cmd.getDomainId();
        final String state = cmd.getState();
        final boolean activeOnly = cmd.isActiveOnly();
        final Long startIndex = cmd.getStartIndex();
        final Long pageSizeVal = cmd.getPageSizeVal();
        boolean isRecursive = cmd.isRecursive();
        final boolean listAll = cmd.listAll();

        final Account caller = CallContext.current().getCallingAccount();
        final List<Long> permittedAccounts = new ArrayList<>();

        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                domainId, isRecursive, null);
        _accountMgr.buildACLSearchParameters(caller, id, accountName, projectId, permittedAccounts,
                domainIdRecursiveListProject, listAll, true);
        domainId = domainIdRecursiveListProject.first();
        isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();

        final Filter searchFilter = new Filter(ProjectInvitationJoinVO.class, "id", true, startIndex, pageSizeVal);
        final SearchBuilder<ProjectInvitationJoinVO> sb = _projectInvitationJoinDao.createSearchBuilder();
        _accountMgr.buildACLViewSearchBuilder(sb, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        sb.and("projectId", sb.entity().getProjectId(), SearchCriteria.Op.EQ);
        sb.and("state", sb.entity().getState(), SearchCriteria.Op.EQ);
        sb.and("created", sb.entity().getCreated(), SearchCriteria.Op.GT);
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);

        final SearchCriteria<ProjectInvitationJoinVO> sc = sb.create();
        _accountMgr.buildACLViewSearchCriteria(sc, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        if (projectId != null) {
            sc.setParameters("projectId", projectId);
        }

        if (state != null) {
            sc.setParameters("state", state);
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (activeOnly) {
            sc.setParameters("state", ProjectInvitation.State.Pending);
            sc.setParameters("created", new Date(DateUtil.currentGMTTime().getTime() - _projectMgr.getInvitationTimeout()));
        }

        return _projectInvitationJoinDao.searchAndCount(sc, searchFilter);

    }

    @Override
    public ListResponse<ProjectAccountResponse> listProjectAccounts(final ListProjectAccountsCmd cmd) {
        final Pair<List<ProjectAccountJoinVO>, Integer> projectAccounts = listProjectAccountsInternal(cmd);
        final ListResponse<ProjectAccountResponse> response = new ListResponse<>();
        final List<ProjectAccountResponse> projectResponses =
                ViewResponseHelper.createProjectAccountResponse(projectAccounts.first().toArray(new ProjectAccountJoinVO[projectAccounts.first().size()]));
        response.setResponses(projectResponses, projectAccounts.second());
        return response;
    }

    public Pair<List<ProjectAccountJoinVO>, Integer> listProjectAccountsInternal(final ListProjectAccountsCmd cmd) {
        final long projectId = cmd.getProjectId();
        final String accountName = cmd.getAccountName();
        final String role = cmd.getRole();
        final Long startIndex = cmd.getStartIndex();
        final Long pageSizeVal = cmd.getPageSizeVal();

        // long projectId, String accountName, String role, Long startIndex,
        // Long pageSizeVal) {
        final Account caller = CallContext.current().getCallingAccount();

        // check that the project exists
        final Project project = _projectDao.findById(projectId);

        if (project == null) {
            throw new InvalidParameterValueException("Unable to find the project id=" + projectId);
        }

        // verify permissions - only accounts belonging to the project can list
        // project's account
        if (!_accountMgr.isAdmin(caller.getId()) && _projectAccountDao.findByProjectIdAccountId(projectId, caller.getAccountId()) == null) {
            throw new PermissionDeniedException("Account " + caller + " is not authorized to list users of the project id=" + projectId);
        }

        final Filter searchFilter = new Filter(ProjectAccountJoinVO.class, "id", false, startIndex, pageSizeVal);
        final SearchBuilder<ProjectAccountJoinVO> sb = _projectAccountJoinDao.createSearchBuilder();
        sb.and("accountRole", sb.entity().getAccountRole(), Op.EQ);
        sb.and("projectId", sb.entity().getProjectId(), Op.EQ);

        if (accountName != null) {
            sb.and("accountName", sb.entity().getAccountName(), Op.EQ);
        }

        final SearchCriteria<ProjectAccountJoinVO> sc = sb.create();

        sc.setParameters("projectId", projectId);

        if (role != null) {
            sc.setParameters("accountRole", role);
        }

        if (accountName != null) {
            sc.setParameters("accountName", accountName);
        }

        return _projectAccountJoinDao.searchAndCount(sc, searchFilter);
    }

    @Override
    public ListResponse<HostResponse> searchForServers(final ListHostsCmd cmd) {
        // FIXME: do we need to support list hosts with VmId, maybe we should
        // create another command just for this
        // Right now it is handled separately outside this QueryService
        s_logger.debug(">>>Searching for hosts>>>");
        final Pair<List<HostJoinVO>, Integer> hosts = searchForServersInternal(cmd);
        final ListResponse<HostResponse> response = new ListResponse<>();
        s_logger.debug(">>>Generating Response>>>");
        final List<HostResponse> hostResponses = ViewResponseHelper.createHostResponse(cmd.getDetails(), hosts.first().toArray(new HostJoinVO[hosts.first().size()]));
        response.setResponses(hostResponses, hosts.second());
        return response;
    }

    public Pair<List<HostJoinVO>, Integer> searchForServersInternal(final ListHostsCmd cmd) {

        final Long zoneId = _accountMgr.checkAccessAndSpecifyAuthority(CallContext.current().getCallingAccount(), cmd.getZoneId());
        final Object name = cmd.getHostName();
        final Object type = cmd.getType();
        final Object state = cmd.getState();
        final Object pod = cmd.getPodId();
        final Object cluster = cmd.getClusterId();
        final Object id = cmd.getId();
        final Object keyword = cmd.getKeyword();
        final Object resourceState = cmd.getResourceState();
        final Object haHosts = cmd.getHaHost();
        final Long startIndex = cmd.getStartIndex();
        final Long pageSize = cmd.getPageSizeVal();
        final Hypervisor.HypervisorType hypervisorType = cmd.getHypervisor();

        final Filter searchFilter = new Filter(HostJoinVO.class, "id", Boolean.TRUE, startIndex, pageSize);

        final SearchBuilder<HostJoinVO> sb = _hostJoinDao.createSearchBuilder();
        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct
        // ids
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.LIKE);
        sb.and("type", sb.entity().getType(), SearchCriteria.Op.LIKE);
        sb.and("status", sb.entity().getStatus(), SearchCriteria.Op.EQ);
        sb.and("dataCenterId", sb.entity().getZoneId(), SearchCriteria.Op.EQ);
        sb.and("podId", sb.entity().getPodId(), SearchCriteria.Op.EQ);
        sb.and("clusterId", sb.entity().getClusterId(), SearchCriteria.Op.EQ);
        sb.and("resourceState", sb.entity().getResourceState(), SearchCriteria.Op.EQ);
        sb.and("hypervisor_type", sb.entity().getHypervisorType(), SearchCriteria.Op.EQ);

        final String haTag = _haMgr.getHaTag();
        if (haHosts != null && haTag != null && !haTag.isEmpty()) {
            if ((Boolean) haHosts) {
                sb.and("tag", sb.entity().getTag(), SearchCriteria.Op.EQ);
            } else {
                sb.and().op("tag", sb.entity().getTag(), SearchCriteria.Op.NEQ);
                sb.or("tagNull", sb.entity().getTag(), SearchCriteria.Op.NULL);
                sb.cp();
            }

        }

        final SearchCriteria<HostJoinVO> sc = sb.create();

        if (keyword != null) {
            final SearchCriteria<HostJoinVO> ssc = _hostJoinDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("status", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("type", SearchCriteria.Op.LIKE, "%" + keyword + "%");

            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (name != null) {
            sc.setParameters("name", "%" + name + "%");
        }
        if (type != null) {
            sc.setParameters("type", "%" + type);
        }
        if (state != null) {
            sc.setParameters("status", state);
        }
        if (zoneId != null) {
            sc.setParameters("dataCenterId", zoneId);
        }
        if (pod != null) {
            sc.setParameters("podId", pod);
        }
        if (cluster != null) {
            sc.setParameters("clusterId", cluster);
        }

        if (resourceState != null) {
            sc.setParameters("resourceState", resourceState);
        }

        if (haHosts != null && haTag != null && !haTag.isEmpty()) {
            sc.setParameters("tag", haTag);
        }

        if (hypervisorType != HypervisorType.None && hypervisorType != HypervisorType.Any) {
            sc.setParameters("hypervisor_type", hypervisorType);
        }
        // search host details by ids
        final Pair<List<HostJoinVO>, Integer> uniqueHostPair = _hostJoinDao.searchAndCount(sc, searchFilter);
        final Integer count = uniqueHostPair.second();
        if (count.intValue() == 0) {
            // handle empty result cases
            return uniqueHostPair;
        }
        final List<HostJoinVO> uniqueHosts = uniqueHostPair.first();
        final Long[] hostIds = new Long[uniqueHosts.size()];
        int i = 0;
        for (final HostJoinVO v : uniqueHosts) {
            hostIds[i++] = v.getId();
        }
        final List<HostJoinVO> hosts = _hostJoinDao.searchByIds(hostIds);
        return new Pair<>(hosts, count);

    }

    @Override
    public ListResponse<VolumeResponse> searchForVolumes(final ListVolumesCmd cmd) {
        final Pair<List<VolumeJoinVO>, Integer> result = searchForVolumesInternal(cmd);
        final ListResponse<VolumeResponse> response = new ListResponse<>();

        ResponseView respView = ResponseView.Restricted;
        if (cmd instanceof ListVolumesCmdByAdmin) {
            respView = ResponseView.Full;
        }

        final List<VolumeResponse> volumeResponses = ViewResponseHelper.createVolumeResponse(respView, result.first().toArray(
                new VolumeJoinVO[result.first().size()]));

        for (final VolumeResponse vr : volumeResponses) {
            final String poolId = vr.getStoragePoolId();
            if (poolId == null) {
                continue;
            }

            final DataStore store = dataStoreManager.getPrimaryDataStore(poolId);
            if (store == null) {
                continue;
            }

            final DataStoreDriver driver = store.getDriver();
            if (driver == null) {
                continue;
            }

            final Map<String, String> caps = driver.getCapabilities();
            if (caps != null) {
                final boolean quiescevm = Boolean.parseBoolean(caps.get(DataStoreCapabilities.VOLUME_SNAPSHOT_QUIESCEVM.toString()));
                vr.setNeedQuiescevm(quiescevm);
            }
        }
        response.setResponses(volumeResponses, result.second());
        return response;
    }

    private Pair<List<VolumeJoinVO>, Integer> searchForVolumesInternal(final ListVolumesCmd cmd) {

        final Account caller = CallContext.current().getCallingAccount();
        final List<Long> permittedAccounts = new ArrayList<>();

        final Long id = cmd.getId();
        final Long vmInstanceId = cmd.getVirtualMachineId();
        final String name = cmd.getVolumeName();
        final String keyword = cmd.getKeyword();
        final String type = cmd.getType();
        final Map<String, String> tags = cmd.getTags();
        final Long storageId = cmd.getStorageId();
        final Long diskOffId = cmd.getDiskOfferingId();
        final Boolean display = cmd.getDisplay();

        final Long zoneId = cmd.getZoneId();
        final Long podId = cmd.getPodId();

        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), cmd.getProjectId(), permittedAccounts,
                domainIdRecursiveListProject, cmd.listAll(), false);
        final Long domainId = domainIdRecursiveListProject.first();
        final Boolean isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();
        final Filter searchFilter = new Filter(VolumeJoinVO.class, "created", false, cmd.getStartIndex(), cmd.getPageSizeVal());

        // hack for now, this should be done better but due to needing a join I
        // opted to
        // do this quickly and worry about making it pretty later
        final SearchBuilder<VolumeJoinVO> sb = _volumeJoinDao.createSearchBuilder();
        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct
        // ids to get
        // number of
        // records with
        // pagination
        _accountMgr.buildACLViewSearchBuilder(sb, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        sb.and("name", sb.entity().getName(), SearchCriteria.Op.EQ);
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("volumeType", sb.entity().getVolumeType(), SearchCriteria.Op.LIKE);
        sb.and("instanceId", sb.entity().getVmId(), SearchCriteria.Op.EQ);
        sb.and("dataCenterId", sb.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        sb.and("podId", sb.entity().getPodId(), SearchCriteria.Op.EQ);
        sb.and("storageId", sb.entity().getPoolId(), SearchCriteria.Op.EQ);
        sb.and("diskOfferingId", sb.entity().getDiskOfferingId(), SearchCriteria.Op.EQ);
        sb.and("display", sb.entity().isDisplayVolume(), SearchCriteria.Op.EQ);
        // Only return volumes that are not destroyed
        sb.and("state", sb.entity().getState(), SearchCriteria.Op.NEQ);
        sb.and("systemUse", sb.entity().isSystemUse(), SearchCriteria.Op.NEQ);
        sb.and("nulltype", sb.entity().getVmType(), SearchCriteria.Op.NULL);

        // now set the SC criteria...
        final SearchCriteria<VolumeJoinVO> sc = sb.create();
        _accountMgr.buildACLViewSearchCriteria(sc, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        if (keyword != null) {
            final SearchCriteria<VolumeJoinVO> ssc = _volumeJoinDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("volumeType", SearchCriteria.Op.LIKE, "%" + keyword + "%");

            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (name != null) {
            sc.setParameters("name", name);
        }

        if (display != null) {
            sc.setParameters("display", display);
        }

        // normal users and domain admins cannot see volumes of type system
        sc.setParameters("systemUse", 1);
        // root admins can see them
        if (caller.getType() == Account.ACCOUNT_TYPE_ADMIN) {
            sc.setParameters("systemUse", -1);
        }

        if (tags != null && !tags.isEmpty()) {
            final SearchCriteria<VolumeJoinVO> tagSc = _volumeJoinDao.createSearchCriteria();
            for (final String key : tags.keySet()) {
                final SearchCriteria<VolumeJoinVO> tsc = _volumeJoinDao.createSearchCriteria();
                tsc.addAnd("tagKey", SearchCriteria.Op.EQ, key);
                tsc.addAnd("tagValue", SearchCriteria.Op.EQ, tags.get(key));
                tagSc.addOr("tagKey", SearchCriteria.Op.SC, tsc);
            }
            sc.addAnd("tagKey", SearchCriteria.Op.SC, tagSc);
        }

        if (diskOffId != null) {
            sc.setParameters("diskOfferingId", diskOffId);
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (type != null) {
            sc.setParameters("volumeType", "%" + type + "%");
        }
        if (vmInstanceId != null) {
            sc.setParameters("instanceId", vmInstanceId);
        }
        if (zoneId != null) {
            sc.setParameters("dataCenterId", zoneId);
        }
        if (podId != null) {
            sc.setParameters("podId", podId);
        }

        if (storageId != null) {
            sc.setParameters("storageId", storageId);
        }

        // Only return volumes that are not destroyed
        sc.setParameters("state", Volume.State.Destroy);

        // search Volume details by ids
        final Pair<List<VolumeJoinVO>, Integer> uniqueVolPair = _volumeJoinDao.searchAndCount(sc, searchFilter);
        final Integer count = uniqueVolPair.second();
        if (count.intValue() == 0) {
            // empty result
            return uniqueVolPair;
        }
        final List<VolumeJoinVO> uniqueVols = uniqueVolPair.first();
        final Long[] vrIds = new Long[uniqueVols.size()];
        int i = 0;
        for (final VolumeJoinVO v : uniqueVols) {
            vrIds[i++] = v.getId();
        }
        final List<VolumeJoinVO> vrs = _volumeJoinDao.searchByIds(vrIds);
        return new Pair<>(vrs, count);
    }

    @Override
    public ListResponse<DomainResponse> searchForDomains(final ListDomainsCmd cmd) {
        final Pair<List<DomainJoinVO>, Integer> result = searchForDomainsInternal(cmd);
        final ListResponse<DomainResponse> response = new ListResponse<>();

        ResponseView respView = ResponseView.Restricted;
        if (cmd instanceof ListDomainsCmdByAdmin) {
            respView = ResponseView.Full;
        }

        final List<DomainResponse> domainResponses = ViewResponseHelper.createDomainResponse(respView, result.first().toArray(
                new DomainJoinVO[result.first().size()]));
        response.setResponses(domainResponses, result.second());
        return response;
    }

    private Pair<List<DomainJoinVO>, Integer> searchForDomainsInternal(final ListDomainsCmd cmd) {
        final Account caller = CallContext.current().getCallingAccount();
        Long domainId = cmd.getId();
        final boolean listAll = cmd.listAll();
        boolean isRecursive = false;

        if (domainId != null) {
            final Domain domain = _domainDao.findById(domainId);
            if (domain == null) {
                throw new InvalidParameterValueException("Domain id=" + domainId + " doesn't exist");
            }
            _accountMgr.checkAccess(caller, domain);
        } else {
            if (caller.getType() != Account.ACCOUNT_TYPE_ADMIN) {
                domainId = caller.getDomainId();
            }
            if (listAll) {
                isRecursive = true;
            }
        }

        final Filter searchFilter = new Filter(DomainJoinVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());
        final String domainName = cmd.getDomainName();
        final Integer level = cmd.getLevel();
        final Object keyword = cmd.getKeyword();

        final SearchBuilder<DomainJoinVO> sb = _domainJoinDao.createSearchBuilder();
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.EQ);
        sb.and("level", sb.entity().getLevel(), SearchCriteria.Op.EQ);
        sb.and("path", sb.entity().getPath(), SearchCriteria.Op.LIKE);
        sb.and("state", sb.entity().getState(), SearchCriteria.Op.EQ);

        final SearchCriteria<DomainJoinVO> sc = sb.create();

        if (keyword != null) {
            final SearchCriteria<DomainJoinVO> ssc = _domainJoinDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (domainName != null) {
            sc.setParameters("name", domainName);
        }

        if (level != null) {
            sc.setParameters("level", level);
        }

        if (domainId != null) {
            if (isRecursive) {
                sc.setParameters("path", _domainDao.findById(domainId).getPath() + "%");
            } else {
                sc.setParameters("id", domainId);
            }
        }

        // return only Active domains to the API
        sc.setParameters("state", Domain.State.Active);

        return _domainJoinDao.searchAndCount(sc, searchFilter);
    }

    @Override
    public ListResponse<AccountResponse> searchForAccounts(final ListAccountsCmd cmd) {
        final Pair<List<AccountJoinVO>, Integer> result = searchForAccountsInternal(cmd);
        final ListResponse<AccountResponse> response = new ListResponse<>();

        ResponseView respView = ResponseView.Restricted;
        if (cmd instanceof ListAccountsCmdByAdmin) {
            respView = ResponseView.Full;
        }

        final List<AccountResponse> accountResponses = ViewResponseHelper.createAccountResponse(respView, result.first().toArray(
                new AccountJoinVO[result.first().size()]));
        response.setResponses(accountResponses, result.second());
        return response;
    }

    private Pair<List<AccountJoinVO>, Integer> searchForAccountsInternal(final ListAccountsCmd cmd) {
        final Account caller = CallContext.current().getCallingAccount();
        Long domainId = cmd.getDomainId();
        Long accountId = cmd.getId();
        final String accountName = cmd.getSearchName();
        boolean isRecursive = cmd.isRecursive();
        final boolean listAll = cmd.listAll();
        Boolean listForDomain = false;

        if (accountId != null) {
            final Account account = _accountDao.findById(accountId);
            if (account == null || account.getId() == Account.ACCOUNT_ID_SYSTEM) {
                throw new InvalidParameterValueException("Unable to find account by id " + accountId);
            }

            _accountMgr.checkAccess(caller, null, true, account);
        }

        if (domainId != null) {
            final Domain domain = _domainDao.findById(domainId);
            if (domain == null) {
                throw new InvalidParameterValueException("Domain id=" + domainId + " doesn't exist");
            }

            _accountMgr.checkAccess(caller, domain);

            if (accountName != null) {
                final Account account = _accountDao.findActiveAccount(accountName, domainId);
                if (account == null || account.getId() == Account.ACCOUNT_ID_SYSTEM) {
                    throw new InvalidParameterValueException("Unable to find account by name " + accountName
                            + " in domain " + domainId);
                }
                _accountMgr.checkAccess(caller, null, true, account);
            }
        }

        if (accountId == null) {
            if (_accountMgr.isAdmin(caller.getId()) && listAll && domainId == null) {
                listForDomain = true;
                isRecursive = true;
                if (domainId == null) {
                    domainId = caller.getDomainId();
                }
            } else if (_accountMgr.isAdmin(caller.getId()) && domainId != null) {
                listForDomain = true;
            } else {
                accountId = caller.getAccountId();
            }
        }

        final Filter searchFilter = new Filter(AccountJoinVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());

        final Object type = cmd.getAccountType();
        final Object state = cmd.getState();
        final Object isCleanupRequired = cmd.isCleanupRequired();
        final Object keyword = cmd.getKeyword();

        final SearchBuilder<AccountJoinVO> sb = _accountJoinDao.createSearchBuilder();
        sb.and("accountName", sb.entity().getAccountName(), SearchCriteria.Op.EQ);
        sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("type", sb.entity().getType(), SearchCriteria.Op.EQ);
        sb.and("state", sb.entity().getState(), SearchCriteria.Op.EQ);
        sb.and("needsCleanup", sb.entity().isNeedsCleanup(), SearchCriteria.Op.EQ);
        sb.and("typeNEQ", sb.entity().getType(), SearchCriteria.Op.NEQ);
        sb.and("idNEQ", sb.entity().getId(), SearchCriteria.Op.NEQ);

        if (listForDomain && isRecursive) {
            sb.and("path", sb.entity().getDomainPath(), SearchCriteria.Op.LIKE);
        }

        final SearchCriteria<AccountJoinVO> sc = sb.create();

        sc.setParameters("idNEQ", Account.ACCOUNT_ID_SYSTEM);

        if (keyword != null) {
            final SearchCriteria<AccountJoinVO> ssc = _accountJoinDao.createSearchCriteria();
            ssc.addOr("accountName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("state", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("accountName", SearchCriteria.Op.SC, ssc);
        }

        if (type != null) {
            sc.setParameters("type", type);
        }

        if (state != null) {
            sc.setParameters("state", state);
        }

        if (isCleanupRequired != null) {
            sc.setParameters("needsCleanup", isCleanupRequired);
        }

        if (accountName != null) {
            sc.setParameters("accountName", accountName);
        }

        // don't return account of type project to the end user
        sc.setParameters("typeNEQ", 5);

        if (accountId != null) {
            sc.setParameters("id", accountId);
        }

        if (listForDomain) {
            if (isRecursive) {
                final Domain domain = _domainDao.findById(domainId);
                sc.setParameters("path", domain.getPath() + "%");
            } else {
                sc.setParameters("domainId", domainId);
            }
        }

        return _accountJoinDao.searchAndCount(sc, searchFilter);
    }

    @Override
    public ListResponse<AsyncJobResponse> searchForAsyncJobs(final ListAsyncJobsCmd cmd) {
        final Pair<List<AsyncJobJoinVO>, Integer> result = searchForAsyncJobsInternal(cmd);
        final ListResponse<AsyncJobResponse> response = new ListResponse<>();
        final List<AsyncJobResponse> jobResponses = ViewResponseHelper.createAsyncJobResponse(result.first().toArray(new AsyncJobJoinVO[result.first().size()]));
        response.setResponses(jobResponses, result.second());
        return response;
    }

    private Pair<List<AsyncJobJoinVO>, Integer> searchForAsyncJobsInternal(final ListAsyncJobsCmd cmd) {

        final Account caller = CallContext.current().getCallingAccount();

        final List<Long> permittedAccounts = new ArrayList<>();

        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, null, cmd.getAccountName(), null, permittedAccounts,
                domainIdRecursiveListProject, cmd.listAll(), false);
        final Long domainId = domainIdRecursiveListProject.first();
        final Boolean isRecursive = domainIdRecursiveListProject.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();

        final Filter searchFilter = new Filter(AsyncJobJoinVO.class, "id", true, cmd.getStartIndex(), cmd.getPageSizeVal());
        final SearchBuilder<AsyncJobJoinVO> sb = _jobJoinDao.createSearchBuilder();
        sb.and("accountIdIN", sb.entity().getAccountId(), SearchCriteria.Op.IN);
        boolean accountJoinIsDone = false;
        if (permittedAccounts.isEmpty() && domainId != null) {
            sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);
            sb.and("path", sb.entity().getDomainPath(), SearchCriteria.Op.LIKE);
            accountJoinIsDone = true;
        }

        if (listProjectResourcesCriteria != null) {

            if (listProjectResourcesCriteria == Project.ListProjectResourcesCriteria.ListProjectResourcesOnly) {
                sb.and("type", sb.entity().getAccountType(), SearchCriteria.Op.EQ);
            } else if (listProjectResourcesCriteria == Project.ListProjectResourcesCriteria.SkipProjectResources) {
                sb.and("type", sb.entity().getAccountType(), SearchCriteria.Op.NEQ);
            }

            if (!accountJoinIsDone) {
                sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);
                sb.and("path", sb.entity().getDomainPath(), SearchCriteria.Op.LIKE);
            }
        }

        final Object keyword = cmd.getKeyword();
        final Object startDate = cmd.getStartDate();

        final SearchCriteria<AsyncJobJoinVO> sc = sb.create();
        if (listProjectResourcesCriteria != null) {
            sc.setParameters("type", Account.ACCOUNT_TYPE_PROJECT);
        }

        if (!permittedAccounts.isEmpty()) {
            sc.setParameters("accountIdIN", permittedAccounts.toArray());
        } else if (domainId != null) {
            final DomainVO domain = _domainDao.findById(domainId);
            if (isRecursive) {
                sc.setParameters("path", domain.getPath() + "%");
            } else {
                sc.setParameters("domainId", domainId);
            }
        }

        if (keyword != null) {
            sc.addAnd("cmd", SearchCriteria.Op.LIKE, "%" + keyword + "%");
        }

        if (startDate != null) {
            sc.addAnd("created", SearchCriteria.Op.GTEQ, startDate);
        }

        return _jobJoinDao.searchAndCount(sc, searchFilter);
    }

    @Override
    public ListResponse<StoragePoolResponse> searchForStoragePools(final ListStoragePoolsCmd cmd) {
        final Pair<List<StoragePoolJoinVO>, Integer> result = searchForStoragePoolsInternal(cmd);
        final ListResponse<StoragePoolResponse> response = new ListResponse<>();

        final List<StoragePoolResponse> poolResponses = ViewResponseHelper.createStoragePoolResponse(result.first().toArray(new StoragePoolJoinVO[result.first().size()]));
        for (final StoragePoolResponse poolResponse : poolResponses) {
            final DataStore store = dataStoreManager.getPrimaryDataStore(poolResponse.getId());
            if (store != null) {
                final DataStoreDriver driver = store.getDriver();
                if (driver != null && driver.getCapabilities() != null) {
                    poolResponse.setCaps(driver.getCapabilities());
                }
            }
        }

        response.setResponses(poolResponses, result.second());
        return response;
    }

    private Pair<List<StoragePoolJoinVO>, Integer> searchForStoragePoolsInternal(final ListStoragePoolsCmd cmd) {
        ScopeType scopeType = null;
        if (cmd.getScope() != null) {
            try {
                scopeType = Enum.valueOf(ScopeType.class, cmd.getScope().toUpperCase());
            } catch (final Exception e) {
                throw new InvalidParameterValueException("Invalid scope type: " + cmd.getScope());
            }
        }

        final Long zoneId = _accountMgr.checkAccessAndSpecifyAuthority(CallContext.current().getCallingAccount(), cmd.getZoneId());
        final Object id = cmd.getId();
        final Object name = cmd.getStoragePoolName();
        final Object path = cmd.getPath();
        final Object pod = cmd.getPodId();
        final Object cluster = cmd.getClusterId();
        final Object address = cmd.getIpAddress();
        final Object keyword = cmd.getKeyword();
        final Long startIndex = cmd.getStartIndex();
        final Long pageSize = cmd.getPageSizeVal();

        final Filter searchFilter = new Filter(StoragePoolJoinVO.class, "id", Boolean.TRUE, startIndex, pageSize);

        final SearchBuilder<StoragePoolJoinVO> sb = _poolJoinDao.createSearchBuilder();
        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct
        // ids
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.EQ);
        sb.and("path", sb.entity().getPath(), SearchCriteria.Op.EQ);
        sb.and("dataCenterId", sb.entity().getZoneId(), SearchCriteria.Op.EQ);
        sb.and("podId", sb.entity().getPodId(), SearchCriteria.Op.EQ);
        sb.and("clusterId", sb.entity().getClusterId(), SearchCriteria.Op.EQ);
        sb.and("hostAddress", sb.entity().getHostAddress(), SearchCriteria.Op.EQ);
        sb.and("scope", sb.entity().getScope(), SearchCriteria.Op.EQ);

        final SearchCriteria<StoragePoolJoinVO> sc = sb.create();

        if (keyword != null) {
            final SearchCriteria<StoragePoolJoinVO> ssc = _poolJoinDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("poolType", SearchCriteria.Op.LIKE, "%" + keyword + "%");

            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (name != null) {
            sc.setParameters("name", name);
        }

        if (path != null) {
            sc.setParameters("path", path);
        }
        if (zoneId != null) {
            sc.setParameters("dataCenterId", zoneId);
        }
        if (pod != null) {
            sc.setParameters("podId", pod);
        }
        if (address != null) {
            sc.setParameters("hostAddress", address);
        }
        if (cluster != null) {
            sc.setParameters("clusterId", cluster);
        }
        if (scopeType != null) {
            sc.setParameters("scope", scopeType.toString());
        }

        // search Pool details by ids
        final Pair<List<StoragePoolJoinVO>, Integer> uniquePoolPair = _poolJoinDao.searchAndCount(sc, searchFilter);
        final Integer count = uniquePoolPair.second();
        if (count.intValue() == 0) {
            // empty result
            return uniquePoolPair;
        }
        final List<StoragePoolJoinVO> uniquePools = uniquePoolPair.first();
        final Long[] vrIds = new Long[uniquePools.size()];
        int i = 0;
        for (final StoragePoolJoinVO v : uniquePools) {
            vrIds[i++] = v.getId();
        }
        final List<StoragePoolJoinVO> vrs = _poolJoinDao.searchByIds(vrIds);
        return new Pair<>(vrs, count);

    }

    @Override
    public ListResponse<StorageTagResponse> searchForStorageTags(final ListStorageTagsCmd cmd) {
        final Pair<List<StorageTagVO>, Integer> result = searchForStorageTagsInternal(cmd);
        final ListResponse<StorageTagResponse> response = new ListResponse<>();
        final List<StorageTagResponse> tagResponses = ViewResponseHelper.createStorageTagResponse(result.first().toArray(new StorageTagVO[result.first().size()]));

        response.setResponses(tagResponses, result.second());

        return response;
    }

    private Pair<List<StorageTagVO>, Integer> searchForStorageTagsInternal(final ListStorageTagsCmd cmd) {
        final Filter searchFilter = new Filter(StorageTagVO.class, "id", Boolean.TRUE, null, null);

        final SearchBuilder<StorageTagVO> sb = _storageTagDao.createSearchBuilder();

        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct

        final SearchCriteria<StorageTagVO> sc = sb.create();

        // search storage tag details by ids
        final Pair<List<StorageTagVO>, Integer> uniqueTagPair = _storageTagDao.searchAndCount(sc, searchFilter);
        final Integer count = uniqueTagPair.second();

        if (count.intValue() == 0) {
            return uniqueTagPair;
        }

        final List<StorageTagVO> uniqueTags = uniqueTagPair.first();
        final Long[] vrIds = new Long[uniqueTags.size()];
        int i = 0;

        for (final StorageTagVO v : uniqueTags) {
            vrIds[i++] = v.getId();
        }

        final List<StorageTagVO> vrs = _storageTagDao.searchByIds(vrIds);

        return new Pair<>(vrs, count);
    }

    @Override
    public ListResponse<HostTagResponse> searchForHostTags(final ListHostTagsCmd cmd) {
        final Pair<List<HostTagVO>, Integer> result = searchForHostTagsInternal(cmd);
        final ListResponse<HostTagResponse> response = new ListResponse<>();
        final List<HostTagResponse> tagResponses = ViewResponseHelper.createHostTagResponse(result.first().toArray(new HostTagVO[result.first().size()]));

        response.setResponses(tagResponses, result.second());

        return response;
    }

    private Pair<List<HostTagVO>, Integer> searchForHostTagsInternal(final ListHostTagsCmd cmd) {
        final Filter searchFilter = new Filter(HostTagVO.class, "id", Boolean.TRUE, null, null);

        final SearchBuilder<HostTagVO> sb = _hostTagDao.createSearchBuilder();

        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct

        final SearchCriteria<HostTagVO> sc = sb.create();

        // search host tag details by ids
        final Pair<List<HostTagVO>, Integer> uniqueTagPair = _hostTagDao.searchAndCount(sc, searchFilter);
        final Integer count = uniqueTagPair.second();

        if (count.intValue() == 0) {
            return uniqueTagPair;
        }

        final List<HostTagVO> uniqueTags = uniqueTagPair.first();
        final Long[] vrIds = new Long[uniqueTags.size()];
        int i = 0;

        for (final HostTagVO v : uniqueTags) {
            vrIds[i++] = v.getId();
        }

        final List<HostTagVO> vrs = _hostTagDao.searchByIds(vrIds);

        return new Pair<>(vrs, count);
    }

    @Override
    public ListResponse<ImageStoreResponse> searchForImageStores(final ListImageStoresCmd cmd) {
        final Pair<List<ImageStoreJoinVO>, Integer> result = searchForImageStoresInternal(cmd);
        final ListResponse<ImageStoreResponse> response = new ListResponse<>();

        final List<ImageStoreResponse> poolResponses = ViewResponseHelper.createImageStoreResponse(result.first().toArray(new ImageStoreJoinVO[result.first().size()]));
        response.setResponses(poolResponses, result.second());
        return response;
    }

    private Pair<List<ImageStoreJoinVO>, Integer> searchForImageStoresInternal(final ListImageStoresCmd cmd) {

        final Long zoneId = _accountMgr.checkAccessAndSpecifyAuthority(CallContext.current().getCallingAccount(), cmd.getZoneId());
        final Object id = cmd.getId();
        final Object name = cmd.getStoreName();
        final String provider = cmd.getProvider();
        final String protocol = cmd.getProtocol();
        final Object keyword = cmd.getKeyword();
        final Long startIndex = cmd.getStartIndex();
        final Long pageSize = cmd.getPageSizeVal();

        final Filter searchFilter = new Filter(ImageStoreJoinVO.class, "id", Boolean.TRUE, startIndex, pageSize);

        final SearchBuilder<ImageStoreJoinVO> sb = _imageStoreJoinDao.createSearchBuilder();
        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct
        // ids
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.EQ);
        sb.and("dataCenterId", sb.entity().getZoneId(), SearchCriteria.Op.EQ);
        sb.and("protocol", sb.entity().getProtocol(), SearchCriteria.Op.EQ);
        sb.and("provider", sb.entity().getProviderName(), SearchCriteria.Op.EQ);
        sb.and("role", sb.entity().getRole(), SearchCriteria.Op.EQ);

        final SearchCriteria<ImageStoreJoinVO> sc = sb.create();
        sc.setParameters("role", DataStoreRole.Image);

        if (keyword != null) {
            final SearchCriteria<ImageStoreJoinVO> ssc = _imageStoreJoinDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("providerName", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (name != null) {
            sc.setParameters("name", name);
        }

        if (zoneId != null) {
            sc.setParameters("dataCenterId", zoneId);
        }
        if (provider != null) {
            sc.setParameters("provider", provider);
        }
        if (protocol != null) {
            sc.setParameters("protocol", protocol);
        }

        // search Store details by ids
        final Pair<List<ImageStoreJoinVO>, Integer> uniqueStorePair = _imageStoreJoinDao.searchAndCount(sc, searchFilter);
        final Integer count = uniqueStorePair.second();
        if (count.intValue() == 0) {
            // empty result
            return uniqueStorePair;
        }
        final List<ImageStoreJoinVO> uniqueStores = uniqueStorePair.first();
        final Long[] vrIds = new Long[uniqueStores.size()];
        int i = 0;
        for (final ImageStoreJoinVO v : uniqueStores) {
            vrIds[i++] = v.getId();
        }
        final List<ImageStoreJoinVO> vrs = _imageStoreJoinDao.searchByIds(vrIds);
        return new Pair<>(vrs, count);

    }

    @Override
    public ListResponse<ImageStoreResponse> searchForSecondaryStagingStores(final ListSecondaryStagingStoresCmd cmd) {
        final Pair<List<ImageStoreJoinVO>, Integer> result = searchForCacheStoresInternal(cmd);
        final ListResponse<ImageStoreResponse> response = new ListResponse<>();

        final List<ImageStoreResponse> poolResponses = ViewResponseHelper.createImageStoreResponse(result.first().toArray(new ImageStoreJoinVO[result.first().size()]));
        response.setResponses(poolResponses, result.second());
        return response;
    }

    private Pair<List<ImageStoreJoinVO>, Integer> searchForCacheStoresInternal(final ListSecondaryStagingStoresCmd cmd) {

        final Long zoneId = _accountMgr.checkAccessAndSpecifyAuthority(CallContext.current().getCallingAccount(), cmd.getZoneId());
        final Object id = cmd.getId();
        final Object name = cmd.getStoreName();
        final String provider = cmd.getProvider();
        final String protocol = cmd.getProtocol();
        final Object keyword = cmd.getKeyword();
        final Long startIndex = cmd.getStartIndex();
        final Long pageSize = cmd.getPageSizeVal();

        final Filter searchFilter = new Filter(ImageStoreJoinVO.class, "id", Boolean.TRUE, startIndex, pageSize);

        final SearchBuilder<ImageStoreJoinVO> sb = _imageStoreJoinDao.createSearchBuilder();
        sb.select(null, Func.DISTINCT, sb.entity().getId()); // select distinct
        // ids
        sb.and("id", sb.entity().getId(), SearchCriteria.Op.EQ);
        sb.and("name", sb.entity().getName(), SearchCriteria.Op.EQ);
        sb.and("dataCenterId", sb.entity().getZoneId(), SearchCriteria.Op.EQ);
        sb.and("protocol", sb.entity().getProtocol(), SearchCriteria.Op.EQ);
        sb.and("provider", sb.entity().getProviderName(), SearchCriteria.Op.EQ);
        sb.and("role", sb.entity().getRole(), SearchCriteria.Op.EQ);

        final SearchCriteria<ImageStoreJoinVO> sc = sb.create();
        sc.setParameters("role", DataStoreRole.ImageCache);

        if (keyword != null) {
            final SearchCriteria<ImageStoreJoinVO> ssc = _imageStoreJoinDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("provider", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (id != null) {
            sc.setParameters("id", id);
        }

        if (name != null) {
            sc.setParameters("name", name);
        }

        if (zoneId != null) {
            sc.setParameters("dataCenterId", zoneId);
        }
        if (provider != null) {
            sc.setParameters("provider", provider);
        }
        if (protocol != null) {
            sc.setParameters("protocol", protocol);
        }

        // search Store details by ids
        final Pair<List<ImageStoreJoinVO>, Integer> uniqueStorePair = _imageStoreJoinDao.searchAndCount(sc, searchFilter);
        final Integer count = uniqueStorePair.second();
        if (count.intValue() == 0) {
            // empty result
            return uniqueStorePair;
        }
        final List<ImageStoreJoinVO> uniqueStores = uniqueStorePair.first();
        final Long[] vrIds = new Long[uniqueStores.size()];
        int i = 0;
        for (final ImageStoreJoinVO v : uniqueStores) {
            vrIds[i++] = v.getId();
        }
        final List<ImageStoreJoinVO> vrs = _imageStoreJoinDao.searchByIds(vrIds);
        return new Pair<>(vrs, count);

    }

    @Override
    public ListResponse<DiskOfferingResponse> searchForDiskOfferings(final ListDiskOfferingsCmd cmd) {
        final Pair<List<DiskOfferingJoinVO>, Integer> result = searchForDiskOfferingsInternal(cmd);
        final ListResponse<DiskOfferingResponse> response = new ListResponse<>();
        final List<DiskOfferingResponse> offeringResponses =
                ViewResponseHelper.createDiskOfferingResponse(result.first().toArray(new DiskOfferingJoinVO[result.first().size()]));
        response.setResponses(offeringResponses, result.second());
        return response;
    }

    private Pair<List<DiskOfferingJoinVO>, Integer> searchForDiskOfferingsInternal(final ListDiskOfferingsCmd cmd) {
        // Note
        // The list method for offerings is being modified in accordance with
        // discussion with Will/Kevin
        // For now, we will be listing the following based on the usertype
        // 1. For root, we will list all offerings
        // 2. For domainAdmin and regular users, we will list everything in
        // their domains+parent domains ... all the way
        // till
        // root

        Boolean isAscending = Boolean.parseBoolean(_configDao.getValue("sortkey.algorithm"));
        isAscending = isAscending == null ? true : isAscending;
        final Filter searchFilter = new Filter(DiskOfferingJoinVO.class, "sortKey", isAscending, cmd.getStartIndex(), cmd.getPageSizeVal());
        final SearchCriteria<DiskOfferingJoinVO> sc = _diskOfferingJoinDao.createSearchCriteria();
        sc.addAnd("type", Op.EQ, DiskOfferingVO.Type.Disk);

        final Account account = CallContext.current().getCallingAccount();
        final Object name = cmd.getDiskOfferingName();
        final Object id = cmd.getId();
        final Object keyword = cmd.getKeyword();
        final Long domainId = cmd.getDomainId();
        final Boolean isRootAdmin = _accountMgr.isRootAdmin(account.getAccountId());
        final Boolean isRecursive = cmd.isRecursive();
        // Keeping this logic consistent with domain specific zones
        // if a domainId is provided, we just return the disk offering
        // associated with this domain
        if (domainId != null) {
            if (_accountMgr.isRootAdmin(account.getId()) || isPermissible(account.getDomainId(), domainId)) {
                // check if the user's domain == do's domain || user's domain is
                // a child of so's domain for non-root users
                sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
                if (!isRootAdmin) {
                    sc.addAnd("displayOffering", SearchCriteria.Op.EQ, 1);
                }
                return _diskOfferingJoinDao.searchAndCount(sc, searchFilter);
            } else {
                throw new PermissionDeniedException("The account:" + account.getAccountName() + " does not fall in the same domain hierarchy as the disk offering");
            }
        }

        List<Long> domainIds = null;
        // For non-root users, only return all offerings for the user's domain,
        // and everything above till root
        if (_accountMgr.isNormalUser(account.getId()) || _accountMgr.isDomainAdmin(account.getId())
                || account.getType() == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN) {
            if (isRecursive) { // domain + all sub-domains
                if (account.getType() == Account.ACCOUNT_TYPE_NORMAL) {
                    throw new InvalidParameterValueException("Only ROOT admins and Domain admins can list disk offerings with isrecursive=true");
                }
                final DomainVO domainRecord = _domainDao.findById(account.getDomainId());
                sc.addAnd("domainPath", SearchCriteria.Op.LIKE, domainRecord.getPath() + "%");
            } else { // domain + all ancestors
                // find all domain Id up to root domain for this account
                domainIds = new ArrayList<>();
                DomainVO domainRecord = _domainDao.findById(account.getDomainId());
                if (domainRecord == null) {
                    s_logger.error("Could not find the domainId for account:" + account.getAccountName());
                    throw new CloudAuthenticationException("Could not find the domainId for account:" + account.getAccountName());
                }
                domainIds.add(domainRecord.getId());
                while (domainRecord.getParent() != null) {
                    domainRecord = _domainDao.findById(domainRecord.getParent());
                    domainIds.add(domainRecord.getId());
                }

                final SearchCriteria<DiskOfferingJoinVO> spc = _diskOfferingJoinDao.createSearchCriteria();

                spc.addOr("domainId", SearchCriteria.Op.IN, domainIds.toArray());
                spc.addOr("domainId", SearchCriteria.Op.NULL); // include public offering as where
                sc.addAnd("domainId", SearchCriteria.Op.SC, spc);
                sc.addAnd("systemUse", SearchCriteria.Op.EQ, false); // non-root users should not see system offering at all
            }

        }

        if (keyword != null) {
            final SearchCriteria<DiskOfferingJoinVO> ssc = _diskOfferingJoinDao.createSearchCriteria();
            ssc.addOr("displayText", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");

            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (id != null) {
            sc.addAnd("id", SearchCriteria.Op.EQ, id);
        }

        if (name != null) {
            sc.addAnd("name", SearchCriteria.Op.EQ, name);
        }

        // FIXME: disk offerings should search back up the hierarchy for
        // available disk offerings...
    /*
     * sb.addAnd("domainId", sb.entity().getDomainId(),
     * SearchCriteria.Op.EQ); if (domainId != null) {
     * SearchBuilder<DomainVO> domainSearch =
     * _domainDao.createSearchBuilder(); domainSearch.addAnd("path",
     * domainSearch.entity().getPath(), SearchCriteria.Op.LIKE);
     * sb.join("domainSearch", domainSearch, sb.entity().getDomainId(),
     * domainSearch.entity().getId()); }
     */

        // FIXME: disk offerings should search back up the hierarchy for
        // available disk offerings...
    /*
     * if (domainId != null) { sc.setParameters("domainId", domainId); //
     * //DomainVO domain = _domainDao.findById((Long)domainId); // // I want
     * to join on user_vm.domain_id = domain.id where domain.path like
     * 'foo%' //sc.setJoinParameters("domainSearch", "path",
     * domain.getPath() + "%"); // }
     */

        return _diskOfferingJoinDao.searchAndCount(sc, searchFilter);
    }

    @Override
    public ListResponse<ServiceOfferingResponse> searchForServiceOfferings(final ListServiceOfferingsCmd cmd) {
        final Pair<List<ServiceOfferingJoinVO>, Integer> result = searchForServiceOfferingsInternal(cmd);
        result.first();
        final ListResponse<ServiceOfferingResponse> response = new ListResponse<>();
        final List<ServiceOfferingResponse> offeringResponses =
                ViewResponseHelper.createServiceOfferingResponse(result.first().toArray(new ServiceOfferingJoinVO[result.first().size()]));
        response.setResponses(offeringResponses, result.second());
        return response;
    }

    private Pair<List<ServiceOfferingJoinVO>, Integer> searchForServiceOfferingsInternal(final ListServiceOfferingsCmd cmd) {
        // Note
        // The filteredOfferings method for offerings is being modified in accordance with
        // discussion with Will/Kevin
        // For now, we will be listing the following based on the usertype
        // 1. For root, we will filteredOfferings all offerings
        // 2. For domainAdmin and regular users, we will filteredOfferings everything in
        // their domains+parent domains ... all the way
        // till
        // root
        Boolean isAscending = Boolean.parseBoolean(_configDao.getValue("sortkey.algorithm"));
        isAscending = isAscending == null ? true : isAscending;
        final Filter searchFilter = new Filter(ServiceOfferingJoinVO.class, "sortKey", isAscending, cmd.getStartIndex(), cmd.getPageSizeVal());

        final Account caller = CallContext.current().getCallingAccount();
        final Object name = cmd.getServiceOfferingName();
        final Object id = cmd.getId();
        final Object keyword = cmd.getKeyword();
        final Long vmId = cmd.getVirtualMachineId();
        final Long domainId = cmd.getDomainId();
        final Boolean isSystem = cmd.getIsSystem();
        final String vmTypeStr = cmd.getSystemVmType();
        ServiceOfferingVO currentVmOffering = null;
        final Boolean isRecursive = cmd.isRecursive();

        final SearchCriteria<ServiceOfferingJoinVO> sc = _srvOfferingJoinDao.createSearchCriteria();
        if (!_accountMgr.isRootAdmin(caller.getId()) && isSystem) {
            throw new InvalidParameterValueException("Only ROOT admins can access system's offering");
        }

        // Keeping this logic consistent with domain specific zones
        // if a domainId is provided, we just return the so associated with this
        // domain
        if (domainId != null && !_accountMgr.isRootAdmin(caller.getId())) {
            // check if the user's domain == so's domain || user's domain is a
            // child of so's domain
            if (!isPermissible(caller.getDomainId(), domainId)) {
                throw new PermissionDeniedException("The account:" + caller.getAccountName() + " does not fall in the same domain hierarchy as the service offering");
            }
        }

        if (vmId != null) {
            final VMInstanceVO vmInstance = _vmInstanceDao.findById(vmId);
            if (vmInstance == null || vmInstance.getRemoved() != null) {
                final InvalidParameterValueException ex = new InvalidParameterValueException("unable to find a virtual machine with specified id");
                ex.addProxyObject(vmId.toString(), "vmId");
                throw ex;
            }

            _accountMgr.checkAccess(caller, null, true, vmInstance);

            currentVmOffering = _srvOfferingDao.findByIdIncludingRemoved(vmInstance.getId(), vmInstance.getServiceOfferingId());
            sc.addAnd("id", SearchCriteria.Op.NEQ, currentVmOffering.getId());

            // 1. Only return offerings with the same storage type
            sc.addAnd("useLocalStorage", SearchCriteria.Op.EQ, currentVmOffering.getUseLocalStorage());

            // 2.In case vm is running return only offerings greater than equal to current offering compute.
            if (vmInstance.getState() == VirtualMachine.State.Running) {
                sc.addAnd("cpu", Op.GTEQ, currentVmOffering.getCpu());
                sc.addAnd("speed", Op.GTEQ, currentVmOffering.getSpeed());
                sc.addAnd("ramSize", Op.GTEQ, currentVmOffering.getRamSize());
            }
        }

        // boolean includePublicOfferings = false;
        if (_accountMgr.isNormalUser(caller.getId()) || _accountMgr.isDomainAdmin(caller.getId())
                || caller.getType() == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN) {
            // For non-root users.
            if (isSystem) {
                throw new InvalidParameterValueException("Only root admins can access system's offering");
            }
            if (isRecursive) { // domain + all sub-domains
                if (caller.getType() == Account.ACCOUNT_TYPE_NORMAL) {
                    throw new InvalidParameterValueException("Only ROOT admins and Domain admins can list service offerings with isrecursive=true");
                }
                final DomainVO domainRecord = _domainDao.findById(caller.getDomainId());
                sc.addAnd("domainPath", SearchCriteria.Op.LIKE, domainRecord.getPath() + "%");
            } else { // domain + all ancestors
                // find all domain Id up to root domain for this account
                final List<Long> domainIds = new ArrayList<>();
                DomainVO domainRecord;
                if (vmId != null) {
                    final UserVmVO vmInstance = _userVmDao.findById(vmId);
                    domainRecord = _domainDao.findById(vmInstance.getDomainId());
                    if (domainRecord == null) {
                        s_logger.error("Could not find the domainId for vmId:" + vmId);
                        throw new CloudAuthenticationException("Could not find the domainId for vmId:" + vmId);
                    }
                } else {
                    domainRecord = _domainDao.findById(caller.getDomainId());
                    if (domainRecord == null) {
                        s_logger.error("Could not find the domainId for account:" + caller.getAccountName());
                        throw new CloudAuthenticationException("Could not find the domainId for account:" + caller.getAccountName());
                    }
                }
                domainIds.add(domainRecord.getId());
                while (domainRecord.getParent() != null) {
                    domainRecord = _domainDao.findById(domainRecord.getParent());
                    domainIds.add(domainRecord.getId());
                }

                final SearchCriteria<ServiceOfferingJoinVO> spc = _srvOfferingJoinDao.createSearchCriteria();
                spc.addOr("domainId", SearchCriteria.Op.IN, domainIds.toArray());
                spc.addOr("domainId", SearchCriteria.Op.NULL); // include public offering as well
                sc.addAnd("domainId", SearchCriteria.Op.SC, spc);
            }
        } else {
            // for root users
            if (caller.getDomainId() != 1 && isSystem) { // NON ROOT admin
                throw new InvalidParameterValueException("Non ROOT admins cannot access system's offering");
            }
            if (domainId != null) {
                sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);
            }
        }

        if (keyword != null) {
            final SearchCriteria<ServiceOfferingJoinVO> ssc = _srvOfferingJoinDao.createSearchCriteria();
            ssc.addOr("displayText", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");

            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        if (id != null) {
            sc.addAnd("id", SearchCriteria.Op.EQ, id);
        }

        if (isSystem != null) {
            // note that for non-root users, isSystem is always false when
            // control comes to here
            sc.addAnd("systemUse", SearchCriteria.Op.EQ, isSystem);
        }

        if (name != null) {
            sc.addAnd("name", SearchCriteria.Op.EQ, name);
        }

        if (vmTypeStr != null) {
            sc.addAnd("vmType", SearchCriteria.Op.EQ, vmTypeStr);
        }

        final Pair<List<ServiceOfferingJoinVO>, Integer> result = _srvOfferingJoinDao.searchAndCount(sc, searchFilter);
        return result;
    }

    @Override
    public ListResponse<ZoneResponse> listDataCenters(final ListZonesCmd cmd) {
        final Pair<List<DataCenterJoinVO>, Integer> result = listDataCentersInternal(cmd);
        final ListResponse<ZoneResponse> response = new ListResponse<>();

        ResponseView respView = ResponseView.Restricted;
        if (cmd instanceof ListZonesCmdByAdmin) {
            respView = ResponseView.Full;
        }

        final List<ZoneResponse> dcResponses = ViewResponseHelper.createDataCenterResponse(respView, cmd.getShowCapacities(), result
                .first().toArray(new DataCenterJoinVO[result.first().size()]));
        response.setResponses(dcResponses, result.second());
        return response;
    }

    private Pair<List<DataCenterJoinVO>, Integer> listDataCentersInternal(final ListZonesCmd cmd) {
        final Account account = CallContext.current().getCallingAccount();
        final Long domainId = cmd.getDomainId();
        final Long id = cmd.getId();
        final String keyword = cmd.getKeyword();
        final String name = cmd.getName();
        final String networkType = cmd.getNetworkType();
        final Map<String, String> resourceTags = cmd.getTags();

        final SearchBuilder<DataCenterJoinVO> sb = _dcJoinDao.createSearchBuilder();
        if (resourceTags != null && !resourceTags.isEmpty()) {
            final SearchBuilder<ResourceTagVO> tagSearch = _resourceTagDao.createSearchBuilder();
            for (int count = 0; count < resourceTags.size(); count++) {
                tagSearch.or().op("key" + String.valueOf(count), tagSearch.entity().getKey(), SearchCriteria.Op.EQ);
                tagSearch.and("value" + String.valueOf(count), tagSearch.entity().getValue(), SearchCriteria.Op.EQ);
                tagSearch.cp();
            }
            tagSearch.and("resourceType", tagSearch.entity().getResourceType(), SearchCriteria.Op.EQ);
            sb.groupBy(sb.entity().getId());
            sb.join("tagSearch", tagSearch, sb.entity().getId(), tagSearch.entity().getResourceId(), JoinBuilder.JoinType.INNER);
        }

        final Filter searchFilter = new Filter(DataCenterJoinVO.class, null, false, cmd.getStartIndex(), cmd.getPageSizeVal());
        final SearchCriteria<DataCenterJoinVO> sc = sb.create();

        if (networkType != null) {
            sc.addAnd("networkType", SearchCriteria.Op.EQ, networkType);
        }

        if (id != null) {
            sc.addAnd("id", SearchCriteria.Op.EQ, id);
        } else if (name != null) {
            sc.addAnd("name", SearchCriteria.Op.EQ, name);
        } else {
            if (keyword != null) {
                final SearchCriteria<DataCenterJoinVO> ssc = _dcJoinDao.createSearchCriteria();
                ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
                ssc.addOr("description", SearchCriteria.Op.LIKE, "%" + keyword + "%");
                sc.addAnd("name", SearchCriteria.Op.SC, ssc);
            }

      /*
       * List all resources due to Explicit Dedication except the
       * dedicated resources of other account
       */
            if (domainId != null) { //
                // for domainId != null // right now, we made the decision to
                // only list zones associated // with this domain, private zone
                sc.addAnd("domainId", SearchCriteria.Op.EQ, domainId);

                if (_accountMgr.isNormalUser(account.getId())) {
                    // accountId == null (zones dedicated to a domain) or
                    // accountId = caller
                    final SearchCriteria<DataCenterJoinVO> sdc = _dcJoinDao.createSearchCriteria();
                    sdc.addOr("accountId", SearchCriteria.Op.EQ, account.getId());
                    sdc.addOr("accountId", SearchCriteria.Op.NULL);

                    sc.addAnd("accountId", SearchCriteria.Op.SC, sdc);
                }

            } else if (_accountMgr.isNormalUser(account.getId())) {
                // it was decided to return all zones for the user's domain, and
                // everything above till root
                // list all zones belonging to this domain, and all of its
                // parents
                // check the parent, if not null, add zones for that parent to
                // list

                // find all domain Id up to root domain for this account
                final List<Long> domainIds = new ArrayList<>();
                DomainVO domainRecord = _domainDao.findById(account.getDomainId());
                if (domainRecord == null) {
                    s_logger.error("Could not find the domainId for account:" + account.getAccountName());
                    throw new CloudAuthenticationException("Could not find the domainId for account:" + account.getAccountName());
                }
                domainIds.add(domainRecord.getId());
                while (domainRecord.getParent() != null) {
                    domainRecord = _domainDao.findById(domainRecord.getParent());
                    domainIds.add(domainRecord.getId());
                }
                // domainId == null (public zones) or domainId IN [all domain id
                // up to root domain]
                final SearchCriteria<DataCenterJoinVO> sdc = _dcJoinDao.createSearchCriteria();
                sdc.addOr("domainId", SearchCriteria.Op.IN, domainIds.toArray());
                sdc.addOr("domainId", SearchCriteria.Op.NULL);
                sc.addAnd("domainId", SearchCriteria.Op.SC, sdc);

                // remove disabled zones
                sc.addAnd("allocationState", SearchCriteria.Op.NEQ, Grouping.AllocationState.Disabled);

                // accountId == null (zones dedicated to a domain) or
                // accountId = caller
                final SearchCriteria<DataCenterJoinVO> sdc2 = _dcJoinDao.createSearchCriteria();
                sdc2.addOr("accountId", SearchCriteria.Op.EQ, account.getId());
                sdc2.addOr("accountId", SearchCriteria.Op.NULL);

                sc.addAnd("accountId", SearchCriteria.Op.SC, sdc2);

                // remove Dedicated zones not dedicated to this domainId or
                // subdomainId
                final List<Long> dedicatedZoneIds = removeDedicatedZoneNotSuitabe(domainIds);
                if (!dedicatedZoneIds.isEmpty()) {
                    sdc.addAnd("id", SearchCriteria.Op.NIN, dedicatedZoneIds.toArray(new Object[dedicatedZoneIds.size()]));
                }

            } else if (_accountMgr.isDomainAdmin(account.getId())
                    || account.getType() == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN) {
                // it was decided to return all zones for the domain admin, and
                // everything above till root, as well as zones till the domain
                // leaf
                final List<Long> domainIds = new ArrayList<>();
                DomainVO domainRecord = _domainDao.findById(account.getDomainId());
                if (domainRecord == null) {
                    s_logger.error("Could not find the domainId for account:" + account.getAccountName());
                    throw new CloudAuthenticationException("Could not find the domainId for account:" + account.getAccountName());
                }
                domainIds.add(domainRecord.getId());
                // find all domain Ids till leaf
                final List<DomainVO> allChildDomains = _domainDao.findAllChildren(domainRecord.getPath(), domainRecord.getId());
                for (final DomainVO domain : allChildDomains) {
                    domainIds.add(domain.getId());
                }
                // then find all domain Id up to root domain for this account
                while (domainRecord.getParent() != null) {
                    domainRecord = _domainDao.findById(domainRecord.getParent());
                    domainIds.add(domainRecord.getId());
                }

                // domainId == null (public zones) or domainId IN [all domain id
                // up to root domain]
                final SearchCriteria<DataCenterJoinVO> sdc = _dcJoinDao.createSearchCriteria();
                sdc.addOr("domainId", SearchCriteria.Op.IN, domainIds.toArray());
                sdc.addOr("domainId", SearchCriteria.Op.NULL);
                sc.addAnd("domainId", SearchCriteria.Op.SC, sdc);

                // remove disabled zones
                sc.addAnd("allocationState", SearchCriteria.Op.NEQ, Grouping.AllocationState.Disabled);

                // remove Dedicated zones not dedicated to this domainId or
                // subdomainId
                final List<Long> dedicatedZoneIds = removeDedicatedZoneNotSuitabe(domainIds);
                if (!dedicatedZoneIds.isEmpty()) {
                    sdc.addAnd("id", SearchCriteria.Op.NIN, dedicatedZoneIds.toArray(new Object[dedicatedZoneIds.size()]));
                }
            }

            // handle available=FALSE option, only return zones with at least
            // one VM running there
            final Boolean available = cmd.isAvailable();
            if (account != null) {
                if (available != null && Boolean.FALSE.equals(available)) {
                    final Set<Long> dcIds = new HashSet<>(); // data centers with
                    // at least one VM
                    // running
                    final List<DomainRouterVO> routers = _routerDao.listBy(account.getId());
                    for (final DomainRouterVO router : routers) {
                        dcIds.add(router.getDataCenterId());
                    }
                    if (dcIds.size() == 0) {
                        return new Pair<>(new ArrayList<>(), 0);
                    } else {
                        sc.addAnd("id", SearchCriteria.Op.IN, dcIds.toArray());
                    }

                }
            }
        }

        if (resourceTags != null && !resourceTags.isEmpty()) {
            int count = 0;
            sc.setJoinParameters("tagSearch", "resourceType", ResourceObjectType.Zone.toString());
            for (final Map.Entry<String, String> entry : resourceTags.entrySet()) {
                sc.setJoinParameters("tagSearch", "key" + String.valueOf(count), entry.getKey());
                sc.setJoinParameters("tagSearch", "value" + String.valueOf(count), entry.getValue());
                count++;
            }
        }

        return _dcJoinDao.searchAndCount(sc, searchFilter);
    }

    private List<Long> removeDedicatedZoneNotSuitabe(final List<Long> domainIds) {
        // remove dedicated zone of other domain
        final List<Long> dedicatedZoneIds = new ArrayList<>();
        final List<DedicatedResourceVO> dedicatedResources = _dedicatedDao.listZonesNotInDomainIds(domainIds);
        for (final DedicatedResourceVO dr : dedicatedResources) {
            if (dr != null) {
                dedicatedZoneIds.add(dr.getDataCenterId());
            }
        }
        return dedicatedZoneIds;
    }

    // This method is used for permissions check for both disk and service
    // offerings
    private boolean isPermissible(final Long accountDomainId, final Long offeringDomainId) {

        if (accountDomainId.equals(offeringDomainId)) {
            return true; // account and service offering in same domain
        }

        DomainVO domainRecord = _domainDao.findById(accountDomainId);

        if (domainRecord != null) {
            while (true) {
                if (domainRecord.getId() == offeringDomainId) {
                    return true;
                }

                // try and move on to the next domain
                if (domainRecord.getParent() != null) {
                    domainRecord = _domainDao.findById(domainRecord.getParent());
                } else {
                    break;
                }
            }
        }

        return false;
    }

    @Override
    public ListResponse<TemplateResponse> listTemplates(final ListTemplatesCmd cmd) {
        final Pair<List<TemplateJoinVO>, Integer> result = searchForTemplatesInternal(cmd);
        final ListResponse<TemplateResponse> response = new ListResponse<>();

        ResponseView respView = ResponseView.Restricted;
        if (cmd instanceof ListTemplatesCmdByAdmin) {
            respView = ResponseView.Full;
        }

        final List<TemplateResponse> templateResponses = ViewResponseHelper.createTemplateResponse(respView, result.first().toArray(
                new TemplateJoinVO[result.first().size()]));
        response.setResponses(templateResponses, result.second());
        return response;
    }


    private Pair<List<TemplateJoinVO>, Integer> searchForTemplatesInternal(final ListTemplatesCmd cmd) {
        final TemplateFilter templateFilter = TemplateFilter.valueOf(cmd.getTemplateFilter());
        final Long id = cmd.getId();
        final Map<String, String> tags = cmd.getTags();
        final boolean showRemovedTmpl = cmd.getShowRemoved();
        final Account caller = CallContext.current().getCallingAccount();

        boolean listAll = false;
        if (templateFilter != null && templateFilter == TemplateFilter.all) {
            if (caller.getType() == Account.ACCOUNT_TYPE_NORMAL) {
                throw new InvalidParameterValueException("Filter " + TemplateFilter.all
                        + " can be specified by admin only");
            }
            listAll = true;
        }

        final List<Long> permittedAccountIds = new ArrayList<>();
        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), cmd.getProjectId(), permittedAccountIds,
                domainIdRecursiveListProject, listAll, false);
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();
        final List<Account> permittedAccounts = new ArrayList<>();
        for (final Long accountId : permittedAccountIds) {
            permittedAccounts.add(_accountMgr.getAccount(accountId));
        }

        final boolean showDomr = templateFilter != TemplateFilter.selfexecutable && templateFilter != TemplateFilter.featured;
        final HypervisorType hypervisorType = HypervisorType.getType(cmd.getHypervisor());

        return searchForTemplatesInternal(id, cmd.getTemplateName(), cmd.getKeyword(), templateFilter, false, null,
                cmd.getPageSizeVal(), cmd.getStartIndex(), cmd.getZoneId(), hypervisorType, showDomr,
                cmd.listInReadyState(), permittedAccounts, caller, listProjectResourcesCriteria, tags, showRemovedTmpl);
    }

    private Pair<List<TemplateJoinVO>, Integer> searchForTemplatesInternal(final Long templateId, final String name,
                                                                           final String keyword, final TemplateFilter templateFilter, final boolean isIso, final Boolean bootable, final Long pageSize,
                                                                           final Long startIndex, final Long zoneId, final HypervisorType hyperType, final boolean showDomr, final boolean onlyReady,
                                                                           final List<Account> permittedAccounts, final Account caller, final ListProjectResourcesCriteria listProjectResourcesCriteria,
                                                                           final Map<String, String> tags, final boolean showRemovedTmpl) {

        // check if zone is configured, if not, just return empty list
        List<HypervisorType> hypers = null;
        if (!isIso) {
            hypers = _resourceMgr.listAvailHypervisorInZone(null, null);
            if (hypers == null || hypers.isEmpty()) {
                return new Pair<>(new ArrayList<>(), 0);
            }
        }

        VMTemplateVO template = null;

        Boolean isAscending = Boolean.parseBoolean(_configDao.getValue("sortkey.algorithm"));
        isAscending = isAscending == null ? Boolean.TRUE : isAscending;
        final Filter searchFilter = new Filter(TemplateJoinVO.class, "sortKey", isAscending, startIndex, pageSize);
        searchFilter.addOrderBy(TemplateJoinVO.class, "tempZonePair", isAscending);

        final SearchBuilder<TemplateJoinVO> sb = _templateJoinDao.createSearchBuilder();
        sb.select(null, Func.DISTINCT, sb.entity().getTempZonePair()); // select distinct (templateId, zoneId) pair
        final SearchCriteria<TemplateJoinVO> sc = sb.create();

        // verify templateId parameter and specially handle it
        if (templateId != null) {
            template = _templateDao.findByIdIncludingRemoved(templateId); // Done for backward compatibility - Bug-5221
            if (template == null) {
                throw new InvalidParameterValueException("Please specify a valid template ID.");
            }// If ISO requested then it should be ISO.
            if (isIso && template.getFormat() != ImageFormat.ISO) {
                s_logger.error("Template Id " + templateId + " is not an ISO");
                final InvalidParameterValueException ex = new InvalidParameterValueException(
                        "Specified Template Id is not an ISO");
                ex.addProxyObject(template.getUuid(), "templateId");
                throw ex;
            }// If ISO not requested then it shouldn't be an ISO.
            if (!isIso && template.getFormat() == ImageFormat.ISO) {
                s_logger.error("Incorrect format of the template id " + templateId);
                final InvalidParameterValueException ex = new InvalidParameterValueException("Incorrect format "
                        + template.getFormat() + " of the specified template id");
                ex.addProxyObject(template.getUuid(), "templateId");
                throw ex;
            }

            // if template is not public, perform permission check here
            if (!template.isPublicTemplate() && caller.getType() != Account.ACCOUNT_TYPE_ADMIN) {
                _accountMgr.checkAccess(caller, null, false, template);
            }

            // if templateId is specified, then we will just use the id to
            // search and ignore other query parameters
            sc.addAnd("id", SearchCriteria.Op.EQ, templateId);
        } else {

            DomainVO domain = null;
            if (!permittedAccounts.isEmpty()) {
                domain = _domainDao.findById(permittedAccounts.get(0).getDomainId());
            } else {
                domain = _domainDao.findById(Domain.ROOT_DOMAIN);
            }

            // List<HypervisorType> hypers = null;
            // if (!isIso) {
            // hypers = _resourceMgr.listAvailHypervisorInZone(null, null);
            // }

            // add criteria for project or not
            if (listProjectResourcesCriteria == ListProjectResourcesCriteria.SkipProjectResources) {
                sc.addAnd("accountType", SearchCriteria.Op.NEQ, Account.ACCOUNT_TYPE_PROJECT);
            } else if (listProjectResourcesCriteria == ListProjectResourcesCriteria.ListProjectResourcesOnly) {
                sc.addAnd("accountType", SearchCriteria.Op.EQ, Account.ACCOUNT_TYPE_PROJECT);
            }

            // add criteria for domain path in case of domain admin
            if ((templateFilter == TemplateFilter.self || templateFilter == TemplateFilter.selfexecutable)
                    && (caller.getType() == Account.ACCOUNT_TYPE_DOMAIN_ADMIN || caller.getType() == Account.ACCOUNT_TYPE_RESOURCE_DOMAIN_ADMIN)) {
                sc.addAnd("domainPath", SearchCriteria.Op.LIKE, domain.getPath() + "%");
            }

            final List<Long> relatedDomainIds = new ArrayList<>();
            final List<Long> permittedAccountIds = new ArrayList<>();
            if (!permittedAccounts.isEmpty()) {
                for (final Account account : permittedAccounts) {
                    permittedAccountIds.add(account.getId());
                    final boolean publicTemplates = templateFilter == TemplateFilter.featured || templateFilter == TemplateFilter.community;

                    // get all parent domain ID's all the way till root domain
                    DomainVO domainTreeNode = null;
                    //if template filter is featured, or community, all child domains should be included in search
                    if (publicTemplates) {
                        domainTreeNode = _domainDao.findById(Domain.ROOT_DOMAIN);

                    } else {
                        domainTreeNode = _domainDao.findById(account.getDomainId());
                    }
                    relatedDomainIds.add(domainTreeNode.getId());
                    while (domainTreeNode.getParent() != null) {
                        domainTreeNode = _domainDao.findById(domainTreeNode.getParent());
                        relatedDomainIds.add(domainTreeNode.getId());
                    }

                    // get all child domain ID's
                    if (_accountMgr.isAdmin(account.getId()) || publicTemplates) {
                        final List<DomainVO> allChildDomains = _domainDao.findAllChildren(domainTreeNode.getPath(), domainTreeNode.getId());
                        for (final DomainVO childDomain : allChildDomains) {
                            relatedDomainIds.add(childDomain.getId());
                        }
                    }
                }
            }

            if (!isIso) {
                // add hypervisor criteria for template case
                if (hypers != null && !hypers.isEmpty()) {
                    final String[] relatedHypers = new String[hypers.size()];
                    for (int i = 0; i < hypers.size(); i++) {
                        relatedHypers[i] = hypers.get(i).toString();
                    }
                    sc.addAnd("hypervisorType", SearchCriteria.Op.IN, relatedHypers);
                }
            }

            // control different template filters
            if (templateFilter == TemplateFilter.featured || templateFilter == TemplateFilter.community) {
                sc.addAnd("publicTemplate", SearchCriteria.Op.EQ, true);
                if (templateFilter == TemplateFilter.featured) {
                    sc.addAnd("featured", SearchCriteria.Op.EQ, true);
                } else {
                    sc.addAnd("featured", SearchCriteria.Op.EQ, false);
                }
                if (!permittedAccounts.isEmpty()) {
                    final SearchCriteria<TemplateJoinVO> scc = _templateJoinDao.createSearchCriteria();
                    scc.addOr("domainId", SearchCriteria.Op.IN, relatedDomainIds.toArray());
                    scc.addOr("domainId", SearchCriteria.Op.NULL);
                    sc.addAnd("domainId", SearchCriteria.Op.SC, scc);
                }
            } else if (templateFilter == TemplateFilter.self || templateFilter == TemplateFilter.selfexecutable) {
                if (!permittedAccounts.isEmpty()) {
                    sc.addAnd("accountId", SearchCriteria.Op.IN, permittedAccountIds.toArray());
                }
            } else if (templateFilter == TemplateFilter.sharedexecutable || templateFilter == TemplateFilter.shared) {
                // only show templates shared by others
                sc.addAnd("sharedAccountId", SearchCriteria.Op.IN, permittedAccountIds.toArray());
            } else if (templateFilter == TemplateFilter.executable) {
                final SearchCriteria<TemplateJoinVO> scc = _templateJoinDao.createSearchCriteria();
                scc.addOr("publicTemplate", SearchCriteria.Op.EQ, true);
                if (!permittedAccounts.isEmpty()) {
                    scc.addOr("accountId", SearchCriteria.Op.IN, permittedAccountIds.toArray());
                }
                sc.addAnd("publicTemplate", SearchCriteria.Op.SC, scc);
            }

            // add tags criteria
            if (tags != null && !tags.isEmpty()) {
                final SearchCriteria<TemplateJoinVO> scc = _templateJoinDao.createSearchCriteria();
                for (final Map.Entry<String, String> entry : tags.entrySet()) {
                    final SearchCriteria<TemplateJoinVO> scTag = _templateJoinDao.createSearchCriteria();
                    scTag.addAnd("tagKey", SearchCriteria.Op.EQ, entry.getKey());
                    scTag.addAnd("tagValue", SearchCriteria.Op.EQ, entry.getValue());
                    if (isIso) {
                        scTag.addAnd("tagResourceType", SearchCriteria.Op.EQ, ResourceObjectType.ISO);
                    } else {
                        scTag.addAnd("tagResourceType", SearchCriteria.Op.EQ, ResourceObjectType.Template);
                    }
                    scc.addOr("tagKey", SearchCriteria.Op.SC, scTag);
                }
                sc.addAnd("tagKey", SearchCriteria.Op.SC, scc);
            }

            // other criteria

            if (keyword != null) {
                sc.addAnd("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            } else if (name != null) {
                sc.addAnd("name", SearchCriteria.Op.EQ, name);
            }

            if (isIso) {
                sc.addAnd("format", SearchCriteria.Op.EQ, "ISO");

            } else {
                sc.addAnd("format", SearchCriteria.Op.NEQ, "ISO");
            }

            if (!hyperType.equals(HypervisorType.None)) {
                sc.addAnd("hypervisorType", SearchCriteria.Op.EQ, hyperType);
            }

            if (bootable != null) {
                sc.addAnd("bootable", SearchCriteria.Op.EQ, bootable);
            }

            if (onlyReady) {
                final SearchCriteria<TemplateJoinVO> readySc = _templateJoinDao.createSearchCriteria();
                readySc.addOr("state", SearchCriteria.Op.EQ, TemplateState.Ready);
                final SearchCriteria<TemplateJoinVO> isoPerhostSc = _templateJoinDao.createSearchCriteria();
                isoPerhostSc.addAnd("format", SearchCriteria.Op.EQ, ImageFormat.ISO);
                isoPerhostSc.addAnd("templateType", SearchCriteria.Op.EQ, TemplateType.PERHOST);
                readySc.addOr("templateType", SearchCriteria.Op.SC, isoPerhostSc);
                sc.addAnd("state", SearchCriteria.Op.SC, readySc);
            }

            if (!showDomr) {
                // excluding system template
                sc.addAnd("templateType", SearchCriteria.Op.NEQ, Storage.TemplateType.SYSTEM);
            }
        }

        if (zoneId != null) {
            final SearchCriteria<TemplateJoinVO> zoneSc = _templateJoinDao.createSearchCriteria();
            zoneSc.addOr("dataCenterId", SearchCriteria.Op.EQ, zoneId);
            zoneSc.addOr("dataStoreScope", SearchCriteria.Op.EQ, ScopeType.REGION);
            // handle the case where xs-tools.iso do not have data_center information in template_view
            final SearchCriteria<TemplateJoinVO> isoPerhostSc = _templateJoinDao.createSearchCriteria();
            isoPerhostSc.addAnd("format", SearchCriteria.Op.EQ, ImageFormat.ISO);
            isoPerhostSc.addAnd("templateType", SearchCriteria.Op.EQ, TemplateType.PERHOST);
            zoneSc.addOr("templateType", SearchCriteria.Op.SC, isoPerhostSc);
            sc.addAnd("dataCenterId", SearchCriteria.Op.SC, zoneSc);
        }

        // don't return removed template, this should not be needed since we
        // changed annotation for removed field in TemplateJoinVO.
        // sc.addAnd("removed", SearchCriteria.Op.NULL);

        // search unique templates and find details by Ids
        Pair<List<TemplateJoinVO>, Integer> uniqueTmplPair = null;
        if (showRemovedTmpl) {
            uniqueTmplPair = _templateJoinDao.searchIncludingRemovedAndCount(sc, searchFilter);
        } else {
            sc.addAnd("templateState", SearchCriteria.Op.IN, new State[]{State.Active, State.NotUploaded, State.UploadInProgress});
            uniqueTmplPair = _templateJoinDao.searchAndCount(sc, searchFilter);
        }

        final Integer count = uniqueTmplPair.second();
        if (count.intValue() == 0) {
            // empty result
            return uniqueTmplPair;
        }
        final List<TemplateJoinVO> uniqueTmpls = uniqueTmplPair.first();
        final String[] tzIds = new String[uniqueTmpls.size()];
        int i = 0;
        for (final TemplateJoinVO v : uniqueTmpls) {
            tzIds[i++] = v.getTempZonePair();
        }
        final List<TemplateJoinVO> vrs = _templateJoinDao.searchByTemplateZonePair(showRemovedTmpl, tzIds);
        return new Pair<>(vrs, count);

        // TODO: revisit the special logic for iso search in
        // VMTemplateDaoImpl.searchForTemplates and understand why we need to
        // specially handle ISO. The original logic is very twisted and no idea
        // about what the code was doing.

    }

    @Override
    public ListResponse<TemplateResponse> listIsos(final ListIsosCmd cmd) {
        final Pair<List<TemplateJoinVO>, Integer> result = searchForIsosInternal(cmd);
        final ListResponse<TemplateResponse> response = new ListResponse<>();

        ResponseView respView = ResponseView.Restricted;
        if (cmd instanceof ListIsosCmdByAdmin) {
            respView = ResponseView.Full;
        }

        final List<TemplateResponse> templateResponses = ViewResponseHelper.createIsoResponse(respView, result.first().toArray(
                new TemplateJoinVO[result.first().size()]));
        response.setResponses(templateResponses, result.second());
        return response;
    }

    private Pair<List<TemplateJoinVO>, Integer> searchForIsosInternal(final ListIsosCmd cmd) {
        final TemplateFilter isoFilter = TemplateFilter.valueOf(cmd.getIsoFilter());
        final Long id = cmd.getId();
        final Map<String, String> tags = cmd.getTags();
        final boolean showRemovedISO = cmd.getShowRemoved();
        final Account caller = CallContext.current().getCallingAccount();

        boolean listAll = false;
        if (isoFilter != null && isoFilter == TemplateFilter.all) {
            if (caller.getType() == Account.ACCOUNT_TYPE_NORMAL) {
                throw new InvalidParameterValueException("Filter " + TemplateFilter.all
                        + " can be specified by admin only");
            }
            listAll = true;
        }

        final List<Long> permittedAccountIds = new ArrayList<>();
        final Ternary<Long, Boolean, ListProjectResourcesCriteria> domainIdRecursiveListProject = new Ternary<>(
                cmd.getDomainId(), cmd.isRecursive(), null);
        _accountMgr.buildACLSearchParameters(caller, id, cmd.getAccountName(), cmd.getProjectId(), permittedAccountIds,
                domainIdRecursiveListProject, listAll, false);
        final ListProjectResourcesCriteria listProjectResourcesCriteria = domainIdRecursiveListProject.third();
        final List<Account> permittedAccounts = new ArrayList<>();
        for (final Long accountId : permittedAccountIds) {
            permittedAccounts.add(_accountMgr.getAccount(accountId));
        }

        final HypervisorType hypervisorType = HypervisorType.getType(cmd.getHypervisor());

        return searchForTemplatesInternal(cmd.getId(), cmd.getIsoName(), cmd.getKeyword(), isoFilter, true,
                cmd.isBootable(), cmd.getPageSizeVal(), cmd.getStartIndex(), cmd.getZoneId(), hypervisorType, true,
                cmd.listInReadyState(), permittedAccounts, caller, listProjectResourcesCriteria, tags, showRemovedISO);
    }

    @Override
    public ListResponse<AffinityGroupResponse> searchForAffinityGroups(final ListAffinityGroupsCmd cmd) {
        final Pair<List<AffinityGroupJoinVO>, Integer> result = searchForAffinityGroupsInternal(cmd);
        final ListResponse<AffinityGroupResponse> response = new ListResponse<>();
        final List<AffinityGroupResponse> agResponses = ViewResponseHelper.createAffinityGroupResponses(result.first());
        response.setResponses(agResponses, result.second());
        return response;
    }

    public Pair<List<AffinityGroupJoinVO>, Integer> searchForAffinityGroupsInternal(final ListAffinityGroupsCmd cmd) {

        final Long affinityGroupId = cmd.getId();
        final String affinityGroupName = cmd.getAffinityGroupName();
        final String affinityGroupType = cmd.getAffinityGroupType();
        final Long vmId = cmd.getVirtualMachineId();
        final String accountName = cmd.getAccountName();
        Long domainId = cmd.getDomainId();
        final Long projectId = cmd.getProjectId();
        Boolean isRecursive = cmd.isRecursive();
        final Boolean listAll = cmd.listAll();
        final Long startIndex = cmd.getStartIndex();
        final Long pageSize = cmd.getPageSizeVal();
        final String keyword = cmd.getKeyword();

        final Account caller = CallContext.current().getCallingAccount();

        if (vmId != null) {
            final UserVmVO userVM = _userVmDao.findById(vmId);
            if (userVM == null) {
                throw new InvalidParameterValueException("Unable to list affinity groups for virtual machine instance " + vmId + "; instance not found.");
            }
            _accountMgr.checkAccess(caller, null, true, userVM);
            return listAffinityGroupsByVM(vmId.longValue(), startIndex, pageSize);
        }

        final List<Long> permittedAccounts = new ArrayList<>();
        final Ternary<Long, Boolean, ListProjectResourcesCriteria> ternary = new Ternary<>(domainId, isRecursive, null);

        _accountMgr.buildACLSearchParameters(caller, affinityGroupId, accountName, projectId, permittedAccounts, ternary, listAll, false);

        domainId = ternary.first();
        isRecursive = ternary.second();
        final ListProjectResourcesCriteria listProjectResourcesCriteria = ternary.third();

        final Filter searchFilter = new Filter(AffinityGroupJoinVO.class, ID_FIELD, true, startIndex, pageSize);

        final SearchCriteria<AffinityGroupJoinVO> sc = buildAffinityGroupSearchCriteria(domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria, affinityGroupId,
                affinityGroupName, affinityGroupType, keyword);

        final Pair<List<AffinityGroupJoinVO>, Integer> uniqueGroupsPair = _affinityGroupJoinDao.searchAndCount(sc, searchFilter);

        // search group details by ids
        List<AffinityGroupJoinVO> affinityGroups = new ArrayList<>();

        final Integer count = uniqueGroupsPair.second();
        if (count.intValue() != 0) {
            final List<AffinityGroupJoinVO> uniqueGroups = uniqueGroupsPair.first();
            final Long[] vrIds = new Long[uniqueGroups.size()];
            int i = 0;
            for (final AffinityGroupJoinVO v : uniqueGroups) {
                vrIds[i++] = v.getId();
            }
            affinityGroups = _affinityGroupJoinDao.searchByIds(vrIds);
        }

        if (!permittedAccounts.isEmpty()) {
            // add domain level affinity groups
            if (domainId != null) {
                final SearchCriteria<AffinityGroupJoinVO> scDomain = buildAffinityGroupSearchCriteria(null, isRecursive, new ArrayList<>(), listProjectResourcesCriteria,
                        affinityGroupId, affinityGroupName, affinityGroupType, keyword);
                affinityGroups.addAll(listDomainLevelAffinityGroups(scDomain, searchFilter, domainId));
            } else {

                for (final Long permAcctId : permittedAccounts) {
                    final Account permittedAcct = _accountDao.findById(permAcctId);
                    final SearchCriteria<AffinityGroupJoinVO> scDomain = buildAffinityGroupSearchCriteria(null, isRecursive, new ArrayList<>(), listProjectResourcesCriteria,
                            affinityGroupId, affinityGroupName, affinityGroupType, keyword);

                    affinityGroups.addAll(listDomainLevelAffinityGroups(scDomain, searchFilter, permittedAcct.getDomainId()));
                }
            }
        } else if (permittedAccounts.isEmpty() && domainId != null && isRecursive) {
            // list all domain level affinity groups for the domain admin case
            final SearchCriteria<AffinityGroupJoinVO> scDomain = buildAffinityGroupSearchCriteria(null, isRecursive, new ArrayList<>(), listProjectResourcesCriteria,
                    affinityGroupId, affinityGroupName, affinityGroupType, keyword);
            affinityGroups.addAll(listDomainLevelAffinityGroups(scDomain, searchFilter, domainId));
        }

        return new Pair<>(affinityGroups, affinityGroups.size());

    }

    private void buildAffinityGroupViewSearchBuilder(final SearchBuilder<AffinityGroupJoinVO> sb, final Long domainId,
                                                     final boolean isRecursive, final List<Long> permittedAccounts, final ListProjectResourcesCriteria listProjectResourcesCriteria) {

        sb.and("accountIdIN", sb.entity().getAccountId(), SearchCriteria.Op.IN);
        sb.and("domainId", sb.entity().getDomainId(), SearchCriteria.Op.EQ);

        if (permittedAccounts.isEmpty() && domainId != null && isRecursive) {
            // if accountId isn't specified, we can do a domain match for the
            // admin case if isRecursive is true
            sb.and("domainPath", sb.entity().getDomainPath(), SearchCriteria.Op.LIKE);
        }

        if (listProjectResourcesCriteria != null) {
            if (listProjectResourcesCriteria == Project.ListProjectResourcesCriteria.ListProjectResourcesOnly) {
                sb.and("accountType", sb.entity().getAccountType(), SearchCriteria.Op.EQ);
            } else if (listProjectResourcesCriteria == Project.ListProjectResourcesCriteria.SkipProjectResources) {
                sb.and("accountType", sb.entity().getAccountType(), SearchCriteria.Op.NEQ);
            }
        }

    }

    private void buildAffinityGroupViewSearchCriteria(final SearchCriteria<AffinityGroupJoinVO> sc,
                                                      final Long domainId, final boolean isRecursive, final List<Long> permittedAccounts, final ListProjectResourcesCriteria listProjectResourcesCriteria) {

        if (listProjectResourcesCriteria != null) {
            sc.setParameters("accountType", Account.ACCOUNT_TYPE_PROJECT);
        }

        if (!permittedAccounts.isEmpty()) {
            sc.setParameters("accountIdIN", permittedAccounts.toArray());
        } else if (domainId != null) {
            final DomainVO domain = _domainDao.findById(domainId);
            if (isRecursive) {
                sc.setParameters("domainPath", domain.getPath() + "%");
            } else {
                sc.setParameters("domainId", domainId);
            }
        }
    }

    private SearchCriteria<AffinityGroupJoinVO> buildAffinityGroupSearchCriteria(final Long domainId, final boolean isRecursive, final List<Long> permittedAccounts,
                                                                                 final ListProjectResourcesCriteria listProjectResourcesCriteria, final Long affinityGroupId, final String affinityGroupName, final String affinityGroupType, final String keyword) {

        final SearchBuilder<AffinityGroupJoinVO> groupSearch = _affinityGroupJoinDao.createSearchBuilder();
        buildAffinityGroupViewSearchBuilder(groupSearch, domainId, isRecursive, permittedAccounts,
                listProjectResourcesCriteria);

        groupSearch.select(null, Func.DISTINCT, groupSearch.entity().getId()); // select
        // distinct

        final SearchCriteria<AffinityGroupJoinVO> sc = groupSearch.create();
        buildAffinityGroupViewSearchCriteria(sc, domainId, isRecursive, permittedAccounts, listProjectResourcesCriteria);

        if (affinityGroupId != null) {
            sc.addAnd("id", SearchCriteria.Op.EQ, affinityGroupId);
        }

        if (affinityGroupName != null) {
            sc.addAnd("name", SearchCriteria.Op.EQ, affinityGroupName);
        }

        if (affinityGroupType != null) {
            sc.addAnd("type", SearchCriteria.Op.EQ, affinityGroupType);
        }

        if (keyword != null) {
            final SearchCriteria<AffinityGroupJoinVO> ssc = _affinityGroupJoinDao.createSearchCriteria();
            ssc.addOr("name", SearchCriteria.Op.LIKE, "%" + keyword + "%");
            ssc.addOr("type", SearchCriteria.Op.LIKE, "%" + keyword + "%");

            sc.addAnd("name", SearchCriteria.Op.SC, ssc);
        }

        return sc;
    }

    private Pair<List<AffinityGroupJoinVO>, Integer> listAffinityGroupsByVM(final long vmId, final long pageInd, final long pageSize) {
        final Filter sf = new Filter(SecurityGroupVMMapVO.class, null, true, pageInd, pageSize);
        final Pair<List<AffinityGroupVMMapVO>, Integer> agVmMappingPair = _affinityGroupVMMapDao.listByInstanceId(vmId, sf);
        final Integer count = agVmMappingPair.second();
        if (count.intValue() == 0) {
            // handle empty result cases
            return new Pair<>(new ArrayList<>(), count);
        }
        final List<AffinityGroupVMMapVO> agVmMappings = agVmMappingPair.first();
        final Long[] agIds = new Long[agVmMappings.size()];
        int i = 0;
        for (final AffinityGroupVMMapVO agVm : agVmMappings) {
            agIds[i++] = agVm.getAffinityGroupId();
        }
        final List<AffinityGroupJoinVO> ags = _affinityGroupJoinDao.searchByIds(agIds);
        return new Pair<>(ags, count);
    }

    private List<AffinityGroupJoinVO> listDomainLevelAffinityGroups(final SearchCriteria<AffinityGroupJoinVO> sc, final Filter searchFilter, final long domainId) {
        final List<Long> affinityGroupIds = new ArrayList<>();
        final Set<Long> allowedDomains = _domainMgr.getDomainParentIds(domainId);
        final List<AffinityGroupDomainMapVO> maps = _affinityGroupDomainMapDao.listByDomain(allowedDomains.toArray());

        for (final AffinityGroupDomainMapVO map : maps) {
            final boolean subdomainAccess = map.isSubdomainAccess();
            if (map.getDomainId() == domainId || subdomainAccess) {
                affinityGroupIds.add(map.getAffinityGroupId());
            }
        }

        if (!affinityGroupIds.isEmpty()) {
            final SearchCriteria<AffinityGroupJoinVO> domainSC = _affinityGroupJoinDao.createSearchCriteria();
            domainSC.addAnd("id", SearchCriteria.Op.IN, affinityGroupIds.toArray());
            domainSC.addAnd("aclType", SearchCriteria.Op.EQ, ACLType.Domain.toString());

            sc.addAnd("id", SearchCriteria.Op.SC, domainSC);

            final Pair<List<AffinityGroupJoinVO>, Integer> uniqueGroupsPair = _affinityGroupJoinDao.searchAndCount(sc, searchFilter);
            // search group by ids
            final Integer count = uniqueGroupsPair.second();
            if (count.intValue() == 0) {
                // empty result
                return new ArrayList<>();
            }
            final List<AffinityGroupJoinVO> uniqueGroups = uniqueGroupsPair.first();
            final Long[] vrIds = new Long[uniqueGroups.size()];
            int i = 0;
            for (final AffinityGroupJoinVO v : uniqueGroups) {
                vrIds[i++] = v.getId();
            }
            final List<AffinityGroupJoinVO> vrs = _affinityGroupJoinDao.searchByIds(vrIds);
            return vrs;
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<ResourceDetailResponse> listResourceDetails(final ListResourceDetailsCmd cmd) {
        final String key = cmd.getKey();
        final Boolean forDisplay = cmd.getDisplay();
        final ResourceTag.ResourceObjectType resourceType = cmd.getResourceType();
        final String resourceIdStr = cmd.getResourceId();
        final String value = cmd.getValue();
        Long resourceId = null;

        //Validation - 1.1 - resourceId and value cant be null.
        if (resourceIdStr == null && value == null) {
            throw new InvalidParameterValueException("Insufficient parameters passed for listing by resourceId OR key,value pair. Please check your params and try again.");
        }

        //Validation - 1.2 - Value has to be passed along with key.
        if (value != null && key == null) {
            throw new InvalidParameterValueException("Listing by (key, value) but key is null. Please check the params and try again");
        }

        //Validation - 1.3
        if (resourceIdStr != null) {
            resourceId = _taggedResourceMgr.getResourceId(resourceIdStr, resourceType);
            if (resourceId == null) {
                throw new InvalidParameterValueException("Cannot find resource with resourceId " + resourceIdStr + " and of resource type " + resourceType);
            }
        }


        List<? extends ResourceDetail> detailList = new ArrayList<>();
        ResourceDetail requestedDetail = null;

        if (key == null) {
            detailList = _resourceMetaDataMgr.getDetailsList(resourceId, resourceType, forDisplay);
        } else if (value == null) {
            requestedDetail = _resourceMetaDataMgr.getDetail(resourceId, resourceType, key);
            if (requestedDetail != null && forDisplay != null && requestedDetail.isDisplay() != forDisplay) {
                requestedDetail = null;
            }
        } else {
            detailList = _resourceMetaDataMgr.getDetails(resourceType, key, value, forDisplay);
        }

        final List<ResourceDetailResponse> responseList = new ArrayList<>();
        if (requestedDetail != null) {
            final ResourceDetailResponse detailResponse = createResourceDetailsResponse(requestedDetail, resourceType);
            responseList.add(detailResponse);
        } else {
            for (final ResourceDetail detail : detailList) {
                final ResourceDetailResponse detailResponse = createResourceDetailsResponse(detail, resourceType);
                responseList.add(detailResponse);
            }
        }

        return responseList;
    }

    protected ResourceDetailResponse createResourceDetailsResponse(final ResourceDetail requestedDetail, final ResourceTag.ResourceObjectType resourceType) {
        final ResourceDetailResponse resourceDetailResponse = new ResourceDetailResponse();
        resourceDetailResponse.setResourceId(String.valueOf(requestedDetail.getResourceId()));
        resourceDetailResponse.setName(requestedDetail.getName());
        resourceDetailResponse.setValue(requestedDetail.getValue());
        resourceDetailResponse.setForDisplay(requestedDetail.isDisplay());
        resourceDetailResponse.setResourceType(resourceType.toString().toString());
        resourceDetailResponse.setObjectName("resourcedetail");
        return resourceDetailResponse;
    }

    @Override
    public String getConfigComponentName() {
        return QueryService.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[]{AllowUserViewDestroyedVM};
    }
}
