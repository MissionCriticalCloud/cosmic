package org.apache.cloudstack.query;

import com.cloud.affinity.AffinityGroupResponse;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.AsyncJobResponse;
import com.cloud.api.response.DiskOfferingResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.DomainRouterResponse;
import com.cloud.api.response.EventResponse;
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
import com.cloud.api.response.SecurityGroupResponse;
import com.cloud.api.response.ServiceOfferingResponse;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.api.response.StorageTagResponse;
import com.cloud.api.response.TemplateResponse;
import com.cloud.api.response.UserResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.exception.PermissionDeniedException;
import org.apache.cloudstack.api.command.admin.domain.ListDomainsCmd;
import org.apache.cloudstack.api.command.admin.host.ListHostTagsCmd;
import org.apache.cloudstack.api.command.admin.host.ListHostsCmd;
import org.apache.cloudstack.api.command.admin.internallb.ListInternalLBVMsCmd;
import org.apache.cloudstack.api.command.admin.router.ListRoutersCmd;
import org.apache.cloudstack.api.command.admin.storage.ListImageStoresCmd;
import org.apache.cloudstack.api.command.admin.storage.ListSecondaryStagingStoresCmd;
import org.apache.cloudstack.api.command.admin.storage.ListStoragePoolsCmd;
import org.apache.cloudstack.api.command.admin.storage.ListStorageTagsCmd;
import org.apache.cloudstack.api.command.admin.user.ListUsersCmd;
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
import org.apache.cloudstack.framework.config.ConfigKey;

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

    ListResponse<SecurityGroupResponse> searchForSecurityGroups(ListSecurityGroupsCmd cmd);

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

    ListResponse<DomainRouterResponse> searchForInternalLbVms(ListInternalLBVMsCmd cmd);

    ListResponse<StorageTagResponse> searchForStorageTags(ListStorageTagsCmd cmd);

    ListResponse<HostTagResponse> searchForHostTags(ListHostTagsCmd cmd);
}
