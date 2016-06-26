package com.cloud.api.query;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.query.vo.AccountJoinVO;
import com.cloud.api.query.vo.AffinityGroupJoinVO;
import com.cloud.api.query.vo.AsyncJobJoinVO;
import com.cloud.api.query.vo.DataCenterJoinVO;
import com.cloud.api.query.vo.DiskOfferingJoinVO;
import com.cloud.api.query.vo.DomainJoinVO;
import com.cloud.api.query.vo.DomainRouterJoinVO;
import com.cloud.api.query.vo.EventJoinVO;
import com.cloud.api.query.vo.HostJoinVO;
import com.cloud.api.query.vo.HostTagVO;
import com.cloud.api.query.vo.ImageStoreJoinVO;
import com.cloud.api.query.vo.InstanceGroupJoinVO;
import com.cloud.api.query.vo.ProjectAccountJoinVO;
import com.cloud.api.query.vo.ProjectInvitationJoinVO;
import com.cloud.api.query.vo.ProjectJoinVO;
import com.cloud.api.query.vo.ResourceTagJoinVO;
import com.cloud.api.query.vo.SecurityGroupJoinVO;
import com.cloud.api.query.vo.ServiceOfferingJoinVO;
import com.cloud.api.query.vo.StoragePoolJoinVO;
import com.cloud.api.query.vo.StorageTagVO;
import com.cloud.api.query.vo.TemplateJoinVO;
import com.cloud.api.query.vo.UserAccountJoinVO;
import com.cloud.api.query.vo.UserVmJoinVO;
import com.cloud.api.query.vo.VolumeJoinVO;
import com.cloud.user.Account;
import org.apache.cloudstack.affinity.AffinityGroupResponse;
import org.apache.cloudstack.api.ApiConstants.HostDetails;
import org.apache.cloudstack.api.ApiConstants.VMDetails;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.AsyncJobResponse;
import org.apache.cloudstack.api.response.DiskOfferingResponse;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.DomainRouterResponse;
import org.apache.cloudstack.api.response.EventResponse;
import org.apache.cloudstack.api.response.HostForMigrationResponse;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.HostTagResponse;
import org.apache.cloudstack.api.response.ImageStoreResponse;
import org.apache.cloudstack.api.response.InstanceGroupResponse;
import org.apache.cloudstack.api.response.ProjectAccountResponse;
import org.apache.cloudstack.api.response.ProjectInvitationResponse;
import org.apache.cloudstack.api.response.ProjectResponse;
import org.apache.cloudstack.api.response.ResourceTagResponse;
import org.apache.cloudstack.api.response.SecurityGroupResponse;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;
import org.apache.cloudstack.api.response.StoragePoolResponse;
import org.apache.cloudstack.api.response.StorageTagResponse;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.api.response.UserResponse;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.VolumeResponse;
import org.apache.cloudstack.api.response.ZoneResponse;
import org.apache.cloudstack.context.CallContext;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to generate response from DB view VO objects.
 */
public class ViewResponseHelper {

    public static final Logger s_logger = LoggerFactory.getLogger(ViewResponseHelper.class);

    public static List<EventResponse> createEventResponse(final EventJoinVO... events) {
        final List<EventResponse> respList = new ArrayList<>();
        for (final EventJoinVO vt : events) {
            respList.add(ApiDBUtils.newEventResponse(vt));
        }
        return respList;
    }

    public static List<ResourceTagResponse> createResourceTagResponse(final boolean keyValueOnly, final ResourceTagJoinVO... tags) {
        final List<ResourceTagResponse> respList = new ArrayList<>();
        for (final ResourceTagJoinVO vt : tags) {
            respList.add(ApiDBUtils.newResourceTagResponse(vt, keyValueOnly));
        }
        return respList;
    }

    public static List<InstanceGroupResponse> createInstanceGroupResponse(final InstanceGroupJoinVO... groups) {
        final List<InstanceGroupResponse> respList = new ArrayList<>();
        for (final InstanceGroupJoinVO vt : groups) {
            respList.add(ApiDBUtils.newInstanceGroupResponse(vt));
        }
        return respList;
    }

    public static List<UserVmResponse> createUserVmResponse(final ResponseView view, final String objectName, final UserVmJoinVO... userVms) {
        return createUserVmResponse(view, objectName, EnumSet.of(VMDetails.all), userVms);
    }

    public static List<UserVmResponse> createUserVmResponse(final ResponseView view, final String objectName, final EnumSet<VMDetails> details, final UserVmJoinVO... userVms) {
        final Account caller = CallContext.current().getCallingAccount();

        final Hashtable<Long, UserVmResponse> vmDataList = new Hashtable<>();
        // Initialise the vmdatalist with the input data

        for (final UserVmJoinVO userVm : userVms) {
            UserVmResponse userVmData = vmDataList.get(userVm.getId());
            if (userVmData == null) {
                // first time encountering this vm
                userVmData = ApiDBUtils.newUserVmResponse(view, objectName, userVm, details, caller);
            } else {
                // update nics, securitygroups, tags, affinitygroups for 1 to many mapping fields
                userVmData = ApiDBUtils.fillVmDetails(view, userVmData, userVm);
            }
            vmDataList.put(userVm.getId(), userVmData);
        }
        return new ArrayList<>(vmDataList.values());
    }

    public static List<DomainRouterResponse> createDomainRouterResponse(final DomainRouterJoinVO... routers) {
        final Account caller = CallContext.current().getCallingAccount();
        final Hashtable<Long, DomainRouterResponse> vrDataList = new Hashtable<>();
        // Initialise the vrdatalist with the input data
        for (final DomainRouterJoinVO vr : routers) {
            DomainRouterResponse vrData = vrDataList.get(vr.getId());
            if (vrData == null) {
                // first time encountering this vm
                vrData = ApiDBUtils.newDomainRouterResponse(vr, caller);
            } else {
                // update nics for 1 to many mapping fields
                vrData = ApiDBUtils.fillRouterDetails(vrData, vr);
            }
            vrDataList.put(vr.getId(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }

    public static List<SecurityGroupResponse> createSecurityGroupResponses(final List<SecurityGroupJoinVO> securityGroups) {
        final Account caller = CallContext.current().getCallingAccount();
        final Hashtable<Long, SecurityGroupResponse> vrDataList = new Hashtable<>();
        // Initialise the vrdatalist with the input data
        for (final SecurityGroupJoinVO vr : securityGroups) {
            SecurityGroupResponse vrData = vrDataList.get(vr.getId());
            if (vrData == null) {
                // first time encountering this sg
                vrData = ApiDBUtils.newSecurityGroupResponse(vr, caller);
            } else {
                // update rules for 1 to many mapping fields
                vrData = ApiDBUtils.fillSecurityGroupDetails(vrData, vr);
            }
            vrDataList.put(vr.getId(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }

    public static List<ProjectResponse> createProjectResponse(final ProjectJoinVO... projects) {
        final Hashtable<Long, ProjectResponse> prjDataList = new Hashtable<>();
        // Initialise the prjdatalist with the input data
        for (final ProjectJoinVO p : projects) {
            ProjectResponse pData = prjDataList.get(p.getId());
            if (pData == null) {
                // first time encountering this vm
                pData = ApiDBUtils.newProjectResponse(p);
            } else {
                // update those  1 to many mapping fields
                pData = ApiDBUtils.fillProjectDetails(pData, p);
            }
            prjDataList.put(p.getId(), pData);
        }
        return new ArrayList<>(prjDataList.values());
    }

    public static List<ProjectAccountResponse> createProjectAccountResponse(final ProjectAccountJoinVO... projectAccounts) {
        final List<ProjectAccountResponse> responseList = new ArrayList<>();
        for (final ProjectAccountJoinVO proj : projectAccounts) {
            final ProjectAccountResponse resp = ApiDBUtils.newProjectAccountResponse(proj);
            // update user list
            final Account caller = CallContext.current().getCallingAccount();
            if (ApiDBUtils.isAdmin(caller)) {
                final List<UserAccountJoinVO> users = ApiDBUtils.findUserViewByAccountId(proj.getAccountId());
                resp.setUsers(ViewResponseHelper.createUserResponse(users.toArray(new UserAccountJoinVO[users.size()])));
            }
            responseList.add(resp);
        }
        return responseList;
    }

    public static List<UserResponse> createUserResponse(final UserAccountJoinVO... users) {
        return createUserResponse(null, users);
    }

    public static List<UserResponse> createUserResponse(final Long domainId, final UserAccountJoinVO... users) {
        final List<UserResponse> respList = new ArrayList<>();
        for (final UserAccountJoinVO vt : users) {
            respList.add(ApiDBUtils.newUserResponse(vt, domainId));
        }
        return respList;
    }

    public static List<ProjectInvitationResponse> createProjectInvitationResponse(final ProjectInvitationJoinVO... invites) {
        final List<ProjectInvitationResponse> respList = new ArrayList<>();
        for (final ProjectInvitationJoinVO v : invites) {
            respList.add(ApiDBUtils.newProjectInvitationResponse(v));
        }
        return respList;
    }

    public static List<HostResponse> createHostResponse(final EnumSet<HostDetails> details, final HostJoinVO... hosts) {
        final Hashtable<Long, HostResponse> vrDataList = new Hashtable<>();
        // Initialise the vrdatalist with the input data
        for (final HostJoinVO vr : hosts) {
            HostResponse vrData = vrDataList.get(vr.getId());
            if (vrData == null) {
                // first time encountering this vm
                vrData = ApiDBUtils.newHostResponse(vr, details);
            } else {
                // update tags
                vrData = ApiDBUtils.fillHostDetails(vrData, vr);
            }
            vrDataList.put(vr.getId(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }

    public static List<HostForMigrationResponse> createHostForMigrationResponse(final EnumSet<HostDetails> details, final HostJoinVO... hosts) {
        final Hashtable<Long, HostForMigrationResponse> vrDataList = new Hashtable<>();
        // Initialise the vrdatalist with the input data
        for (final HostJoinVO vr : hosts) {
            HostForMigrationResponse vrData = vrDataList.get(vr.getId());
            if (vrData == null) {
                // first time encountering this vm
                vrData = ApiDBUtils.newHostForMigrationResponse(vr, details);
            } else {
                // update tags
                vrData = ApiDBUtils.fillHostForMigrationDetails(vrData, vr);
            }
            vrDataList.put(vr.getId(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }

    public static List<VolumeResponse> createVolumeResponse(final ResponseView view, final VolumeJoinVO... volumes) {
        final Hashtable<Long, VolumeResponse> vrDataList = new Hashtable<>();
        for (final VolumeJoinVO vr : volumes) {
            VolumeResponse vrData = vrDataList.get(vr.getId());
            if (vrData == null) {
                // first time encountering this volume
                vrData = ApiDBUtils.newVolumeResponse(view, vr);
            } else {
                // update tags
                vrData = ApiDBUtils.fillVolumeDetails(view, vrData, vr);
            }
            vrDataList.put(vr.getId(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }

    public static List<StoragePoolResponse> createStoragePoolResponse(final StoragePoolJoinVO... pools) {
        final Hashtable<Long, StoragePoolResponse> vrDataList = new Hashtable<>();
        // Initialise the vrdatalist with the input data
        for (final StoragePoolJoinVO vr : pools) {
            StoragePoolResponse vrData = vrDataList.get(vr.getId());
            if (vrData == null) {
                // first time encountering this vm
                vrData = ApiDBUtils.newStoragePoolResponse(vr);
            } else {
                // update tags
                vrData = ApiDBUtils.fillStoragePoolDetails(vrData, vr);
            }
            vrDataList.put(vr.getId(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }

    public static List<StorageTagResponse> createStorageTagResponse(final StorageTagVO... storageTags) {
        final ArrayList<StorageTagResponse> list = new ArrayList<>();

        for (final StorageTagVO vr : storageTags) {
            list.add(ApiDBUtils.newStorageTagResponse(vr));
        }

        return list;
    }

    public static List<HostTagResponse> createHostTagResponse(final HostTagVO... hostTags) {
        final ArrayList<HostTagResponse> list = new ArrayList<>();

        for (final HostTagVO vr : hostTags) {
            list.add(ApiDBUtils.newHostTagResponse(vr));
        }

        return list;
    }

    public static List<ImageStoreResponse> createImageStoreResponse(final ImageStoreJoinVO... stores) {
        final Hashtable<Long, ImageStoreResponse> vrDataList = new Hashtable<>();
        // Initialise the vrdatalist with the input data
        for (final ImageStoreJoinVO vr : stores) {
            ImageStoreResponse vrData = vrDataList.get(vr.getId());
            if (vrData == null) {
                // first time encountering this vm
                vrData = ApiDBUtils.newImageStoreResponse(vr);
            } else {
                // update tags
                vrData = ApiDBUtils.fillImageStoreDetails(vrData, vr);
            }
            vrDataList.put(vr.getId(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }

    public static List<StoragePoolResponse> createStoragePoolForMigrationResponse(final StoragePoolJoinVO... pools) {
        final Hashtable<Long, StoragePoolResponse> vrDataList = new Hashtable<>();
        // Initialise the vrdatalist with the input data
        for (final StoragePoolJoinVO vr : pools) {
            StoragePoolResponse vrData = vrDataList.get(vr.getId());
            if (vrData == null) {
                // first time encountering this vm
                vrData = ApiDBUtils.newStoragePoolForMigrationResponse(vr);
            } else {
                // update tags
                vrData = ApiDBUtils.fillStoragePoolForMigrationDetails(vrData, vr);
            }
            vrDataList.put(vr.getId(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }

    public static List<DomainResponse> createDomainResponse(final ResponseView view, final DomainJoinVO... domains) {
        final List<DomainResponse> respList = new ArrayList<>();
        for (final DomainJoinVO vt : domains) {
            respList.add(ApiDBUtils.newDomainResponse(view, vt));
        }
        return respList;
    }

    public static List<AccountResponse> createAccountResponse(final ResponseView view, final AccountJoinVO... accounts) {
        final List<AccountResponse> respList = new ArrayList<>();
        for (final AccountJoinVO vt : accounts) {
            respList.add(ApiDBUtils.newAccountResponse(view, vt));
        }
        return respList;
    }

    public static List<AsyncJobResponse> createAsyncJobResponse(final AsyncJobJoinVO... jobs) {
        final List<AsyncJobResponse> respList = new ArrayList<>();
        for (final AsyncJobJoinVO vt : jobs) {
            respList.add(ApiDBUtils.newAsyncJobResponse(vt));
        }
        return respList;
    }

    public static List<DiskOfferingResponse> createDiskOfferingResponse(final DiskOfferingJoinVO... offerings) {
        final List<DiskOfferingResponse> respList = new ArrayList<>();
        for (final DiskOfferingJoinVO vt : offerings) {
            respList.add(ApiDBUtils.newDiskOfferingResponse(vt));
        }
        return respList;
    }

    public static List<ServiceOfferingResponse> createServiceOfferingResponse(final ServiceOfferingJoinVO... offerings) {
        final List<ServiceOfferingResponse> respList = new ArrayList<>();
        for (final ServiceOfferingJoinVO vt : offerings) {
            respList.add(ApiDBUtils.newServiceOfferingResponse(vt));
        }
        return respList;
    }

    public static List<ZoneResponse> createDataCenterResponse(final ResponseView view, final Boolean showCapacities, final DataCenterJoinVO... dcs) {
        final List<ZoneResponse> respList = new ArrayList<>();
        for (final DataCenterJoinVO vt : dcs) {
            respList.add(ApiDBUtils.newDataCenterResponse(view, vt, showCapacities));
        }
        return respList;
    }

    public static List<TemplateResponse> createTemplateResponse(final ResponseView view, final TemplateJoinVO... templates) {
        final LinkedHashMap<String, TemplateResponse> vrDataList = new LinkedHashMap<>();
        for (final TemplateJoinVO vr : templates) {
            TemplateResponse vrData = vrDataList.get(vr.getTempZonePair());
            if (vrData == null) {
                // first time encountering this volume
                vrData = ApiDBUtils.newTemplateResponse(view, vr);
            } else {
                // update tags
                vrData = ApiDBUtils.fillTemplateDetails(view, vrData, vr);
            }
            vrDataList.put(vr.getTempZonePair(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }

    public static List<TemplateResponse> createTemplateUpdateResponse(final ResponseView view, final TemplateJoinVO... templates) {
        final Hashtable<Long, TemplateResponse> vrDataList = new Hashtable<>();
        for (final TemplateJoinVO vr : templates) {
            TemplateResponse vrData = vrDataList.get(vr.getId());
            if (vrData == null) {
                // first time encountering this volume
                vrData = ApiDBUtils.newTemplateUpdateResponse(vr);
            } else {
                // update tags
                vrData = ApiDBUtils.fillTemplateDetails(view, vrData, vr);
            }
            vrDataList.put(vr.getId(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }

    public static List<TemplateResponse> createIsoResponse(final ResponseView view, final TemplateJoinVO... templates) {
        final Hashtable<String, TemplateResponse> vrDataList = new Hashtable<>();
        for (final TemplateJoinVO vr : templates) {
            TemplateResponse vrData = vrDataList.get(vr.getTempZonePair());
            if (vrData == null) {
                // first time encountering this volume
                vrData = ApiDBUtils.newIsoResponse(vr);
            } else {
                // update tags
                vrData = ApiDBUtils.fillTemplateDetails(view, vrData, vr);
            }
            vrDataList.put(vr.getTempZonePair(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }

    public static List<AffinityGroupResponse> createAffinityGroupResponses(final List<AffinityGroupJoinVO> groups) {
        final Hashtable<Long, AffinityGroupResponse> vrDataList = new Hashtable<>();
        for (final AffinityGroupJoinVO vr : groups) {
            AffinityGroupResponse vrData = vrDataList.get(vr.getId());
            if (vrData == null) {
                // first time encountering this AffinityGroup
                vrData = ApiDBUtils.newAffinityGroupResponse(vr);
            } else {
                // update vms
                vrData = ApiDBUtils.fillAffinityGroupDetails(vrData, vr);
            }
            vrDataList.put(vr.getId(), vrData);
        }
        return new ArrayList<>(vrDataList.values());
    }
}
