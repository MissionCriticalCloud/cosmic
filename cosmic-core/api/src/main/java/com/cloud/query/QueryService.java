package com.cloud.query;

import com.cloud.affinity.AffinityGroupResponse;
import com.cloud.api.command.admin.cloudops.ListHAWorkersCmd;
import com.cloud.api.command.admin.cloudops.ListWhoHasThisIpCmd;
import com.cloud.api.command.admin.cloudops.ListWhoHasThisMacCmd;
import com.cloud.api.command.admin.domain.ListDomainsCmd;
import com.cloud.api.command.admin.host.ListHostTagsCmd;
import com.cloud.api.command.admin.host.ListHostsCmd;
import com.cloud.api.command.admin.router.ListRoutersCmd;
import com.cloud.api.command.admin.storage.ListImageStoresCmd;
import com.cloud.api.command.admin.storage.ListSecondaryStagingStoresCmd;
import com.cloud.api.command.admin.storage.ListStoragePoolsCmd;
import com.cloud.api.command.admin.storage.ListStorageTagsCmd;
import com.cloud.api.command.admin.user.ListUsersCmd;
import com.cloud.api.command.user.account.ListAccountsCmd;
import com.cloud.api.command.user.account.ListProjectAccountsCmd;
import com.cloud.api.command.user.affinitygroup.ListAffinityGroupsCmd;
import com.cloud.api.command.user.event.ListEventsCmd;
import com.cloud.api.command.user.iso.ListIsosCmd;
import com.cloud.api.command.user.job.ListAsyncJobsCmd;
import com.cloud.api.command.user.offering.ListDiskOfferingsCmd;
import com.cloud.api.command.user.offering.ListServiceOfferingsCmd;
import com.cloud.api.command.user.project.ListProjectInvitationsCmd;
import com.cloud.api.command.user.project.ListProjectsCmd;
import com.cloud.api.command.user.tag.ListTagsCmd;
import com.cloud.api.command.user.template.ListTemplatesCmd;
import com.cloud.api.command.user.vm.ListVMsCmd;
import com.cloud.api.command.user.vmgroup.ListVMGroupsCmd;
import com.cloud.api.command.user.volume.ListResourceDetailsCmd;
import com.cloud.api.command.user.volume.ListVolumesCmd;
import com.cloud.api.command.user.zone.ListZonesCmd;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.AsyncJobResponse;
import com.cloud.api.response.DiskOfferingResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.DomainRouterResponse;
import com.cloud.api.response.EventResponse;
import com.cloud.api.response.HAWorkerResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.HostTagResponse;
import com.cloud.api.response.ImageStoreResponse;
import com.cloud.api.response.InstanceGroupResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.ProjectAccountResponse;
import com.cloud.api.response.ProjectInvitationResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.ResourceDetailResponse;
import com.cloud.api.response.ResourceTagResponse;
import com.cloud.api.response.ServiceOfferingResponse;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.api.response.StorageTagResponse;
import com.cloud.api.response.TemplateResponse;
import com.cloud.api.response.UserResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.api.response.WhoHasThisAddressResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.framework.config.ConfigKey;
import com.cloud.legacymodel.exceptions.PermissionDeniedException;

import java.util.List;

/**
 * Service used for list api query.
 */
public interface QueryService {

    // Config keys
    static final ConfigKey<Boolean> AllowUserViewDestroyedVM = new ConfigKey<>("Advanced", Boolean.class, "allow.user.view.destroyed.vm", "false",
            "Determines whether users can view their destroyed or expunging vm ", true, ConfigKey.Scope.Account);

    ListResponse<UserResponse> searchForUsers(ListUsersCmd cmd) throws PermissionDeniedException;

    ListResponse<EventResponse> searchForEvents(ListEventsCmd cmd);

    ListResponse<ResourceTagResponse> listTags(ListTagsCmd cmd);

    ListResponse<InstanceGroupResponse> searchForVmGroups(ListVMGroupsCmd cmd);

    ListResponse<UserVmResponse> searchForUserVMs(ListVMsCmd cmd);

    ListResponse<DomainRouterResponse> searchForRouters(ListRoutersCmd cmd);

    ListResponse<ProjectInvitationResponse> listProjectInvitations(ListProjectInvitationsCmd cmd);

    ListResponse<ProjectResponse> listProjects(ListProjectsCmd cmd);

    ListResponse<ProjectAccountResponse> listProjectAccounts(ListProjectAccountsCmd cmd);

    ListResponse<HostResponse> searchForServers(ListHostsCmd cmd);

    ListResponse<VolumeResponse> searchForVolumes(ListVolumesCmd cmd);

    ListResponse<StoragePoolResponse> searchForStoragePools(ListStoragePoolsCmd cmd);

    ListResponse<ImageStoreResponse> searchForImageStores(ListImageStoresCmd cmd);

    ListResponse<ImageStoreResponse> searchForSecondaryStagingStores(ListSecondaryStagingStoresCmd cmd);

    ListResponse<DomainResponse> searchForDomains(ListDomainsCmd cmd);

    ListResponse<AccountResponse> searchForAccounts(ListAccountsCmd cmd);

    ListResponse<AsyncJobResponse> searchForAsyncJobs(ListAsyncJobsCmd cmd);

    ListResponse<DiskOfferingResponse> searchForDiskOfferings(ListDiskOfferingsCmd cmd);

    ListResponse<ServiceOfferingResponse> searchForServiceOfferings(ListServiceOfferingsCmd cmd);

    ListResponse<ZoneResponse> listDataCenters(ListZonesCmd cmd);

    ListResponse<TemplateResponse> listTemplates(ListTemplatesCmd cmd);

    ListResponse<TemplateResponse> listIsos(ListIsosCmd cmd);

    ListResponse<AffinityGroupResponse> searchForAffinityGroups(ListAffinityGroupsCmd cmd);

    List<ResourceDetailResponse> listResourceDetails(ListResourceDetailsCmd cmd);

    ListResponse<StorageTagResponse> searchForStorageTags(ListStorageTagsCmd cmd);

    ListResponse<HostTagResponse> searchForHostTags(ListHostTagsCmd cmd);

    ListResponse<HAWorkerResponse> listHAWorkers(ListHAWorkersCmd cmd);

    ListResponse<WhoHasThisAddressResponse> listWhoHasThisIp(ListWhoHasThisIpCmd cmd);

    ListResponse<WhoHasThisAddressResponse> listWhoHasThisMac(ListWhoHasThisMacCmd cmd);
}
