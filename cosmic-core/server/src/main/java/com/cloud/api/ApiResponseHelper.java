package com.cloud.api;

import com.cloud.agent.api.VgpuTypesInfo;
import com.cloud.api.query.ViewResponseHelper;
import com.cloud.api.query.vo.AccountJoinVO;
import com.cloud.api.query.vo.AsyncJobJoinVO;
import com.cloud.api.query.vo.ControlledViewEntity;
import com.cloud.api.query.vo.DataCenterJoinVO;
import com.cloud.api.query.vo.DiskOfferingJoinVO;
import com.cloud.api.query.vo.DomainRouterJoinVO;
import com.cloud.api.query.vo.EventJoinVO;
import com.cloud.api.query.vo.HostJoinVO;
import com.cloud.api.query.vo.ImageStoreJoinVO;
import com.cloud.api.query.vo.InstanceGroupJoinVO;
import com.cloud.api.query.vo.ProjectAccountJoinVO;
import com.cloud.api.query.vo.ProjectInvitationJoinVO;
import com.cloud.api.query.vo.ProjectJoinVO;
import com.cloud.api.query.vo.ResourceTagJoinVO;
import com.cloud.api.query.vo.SecurityGroupJoinVO;
import com.cloud.api.query.vo.ServiceOfferingJoinVO;
import com.cloud.api.query.vo.StoragePoolJoinVO;
import com.cloud.api.query.vo.TemplateJoinVO;
import com.cloud.api.query.vo.UserAccountJoinVO;
import com.cloud.api.query.vo.UserVmJoinVO;
import com.cloud.api.query.vo.VolumeJoinVO;
import com.cloud.api.response.ApiResponseSerializer;
import com.cloud.capacity.Capacity;
import com.cloud.capacity.CapacityVO;
import com.cloud.capacity.dao.CapacityDaoImpl.SummedCapacity;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.Resource.ResourceOwnerType;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.configuration.ResourceCount;
import com.cloud.configuration.ResourceLimit;
import com.cloud.dao.EntityManager;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.Pod;
import com.cloud.dc.StorageNetworkIpRange;
import com.cloud.dc.Vlan;
import com.cloud.dc.Vlan.VlanType;
import com.cloud.dc.VlanVO;
import com.cloud.domain.Domain;
import com.cloud.event.Event;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.gpu.GPU;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.hypervisor.HypervisorCapabilities;
import com.cloud.network.GuestVlan;
import com.cloud.network.IpAddress;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkModel;
import com.cloud.network.NetworkProfile;
import com.cloud.network.Networks.BroadcastDomainType;
import com.cloud.network.Networks.IsolationType;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PhysicalNetwork;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.network.PhysicalNetworkTrafficType;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.Site2SiteCustomerGateway;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.network.Site2SiteVpnGateway;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.network.VpnUser;
import com.cloud.network.VpnUserVO;
import com.cloud.network.as.AutoScalePolicy;
import com.cloud.network.as.AutoScaleVmGroup;
import com.cloud.network.as.AutoScaleVmProfile;
import com.cloud.network.as.AutoScaleVmProfileVO;
import com.cloud.network.as.Condition;
import com.cloud.network.as.ConditionVO;
import com.cloud.network.as.Counter;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.LoadBalancerVO;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.PhysicalNetworkVO;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.rules.FirewallRule;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.network.rules.HealthCheckPolicy;
import com.cloud.network.rules.LoadBalancer;
import com.cloud.network.rules.LoadBalancerContainer.Scheme;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.network.rules.PortForwardingRuleVO;
import com.cloud.network.rules.StaticNatRule;
import com.cloud.network.rules.StickinessPolicy;
import com.cloud.network.security.SecurityGroup;
import com.cloud.network.security.SecurityGroupVO;
import com.cloud.network.security.SecurityRule;
import com.cloud.network.security.SecurityRule.SecurityRuleType;
import com.cloud.network.vpc.NetworkACL;
import com.cloud.network.vpc.NetworkACLItem;
import com.cloud.network.vpc.PrivateGateway;
import com.cloud.network.vpc.StaticRoute;
import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.VpcOffering;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.NetworkOffering.Detail;
import com.cloud.offering.ServiceOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.org.Cluster;
import com.cloud.projects.Project;
import com.cloud.projects.ProjectAccount;
import com.cloud.projects.ProjectInvitation;
import com.cloud.region.ha.GlobalLoadBalancerRule;
import com.cloud.server.ResourceTag;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.GuestOS;
import com.cloud.storage.GuestOSCategoryVO;
import com.cloud.storage.GuestOSHypervisor;
import com.cloud.storage.ImageStore;
import com.cloud.storage.Snapshot;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.StoragePool;
import com.cloud.storage.Upload;
import com.cloud.storage.UploadVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.Volume;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.snapshot.SnapshotPolicy;
import com.cloud.storage.snapshot.SnapshotSchedule;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.SSHKeyPair;
import com.cloud.user.User;
import com.cloud.user.UserAccount;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;
import com.cloud.utils.StringUtils;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.net.Ip;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.ConsoleProxyVO;
import com.cloud.vm.InstanceGroup;
import com.cloud.vm.Nic;
import com.cloud.vm.NicProfile;
import com.cloud.vm.NicSecondaryIp;
import com.cloud.vm.NicVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VirtualMachine.Type;
import com.cloud.vm.dao.NicSecondaryIpVO;
import com.cloud.vm.snapshot.VMSnapshot;
import org.apache.cloudstack.acl.ControlledEntity;
import org.apache.cloudstack.acl.ControlledEntity.ACLType;
import org.apache.cloudstack.affinity.AffinityGroup;
import org.apache.cloudstack.affinity.AffinityGroupResponse;
import org.apache.cloudstack.api.ApiConstants.HostDetails;
import org.apache.cloudstack.api.ApiConstants.VMDetails;
import org.apache.cloudstack.api.ResponseGenerator;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.command.user.job.QueryAsyncJobResultCmd;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.ApplicationLoadBalancerInstanceResponse;
import org.apache.cloudstack.api.response.ApplicationLoadBalancerResponse;
import org.apache.cloudstack.api.response.ApplicationLoadBalancerRuleResponse;
import org.apache.cloudstack.api.response.AsyncJobResponse;
import org.apache.cloudstack.api.response.AutoScalePolicyResponse;
import org.apache.cloudstack.api.response.AutoScaleVmGroupResponse;
import org.apache.cloudstack.api.response.AutoScaleVmProfileResponse;
import org.apache.cloudstack.api.response.CapabilityResponse;
import org.apache.cloudstack.api.response.CapacityResponse;
import org.apache.cloudstack.api.response.ClusterResponse;
import org.apache.cloudstack.api.response.ConditionResponse;
import org.apache.cloudstack.api.response.ConfigurationResponse;
import org.apache.cloudstack.api.response.ControlledEntityResponse;
import org.apache.cloudstack.api.response.ControlledViewEntityResponse;
import org.apache.cloudstack.api.response.CounterResponse;
import org.apache.cloudstack.api.response.CreateCmdResponse;
import org.apache.cloudstack.api.response.CreateSSHKeyPairResponse;
import org.apache.cloudstack.api.response.DiskOfferingResponse;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.DomainRouterResponse;
import org.apache.cloudstack.api.response.EventResponse;
import org.apache.cloudstack.api.response.ExtractResponse;
import org.apache.cloudstack.api.response.FirewallResponse;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.api.response.GlobalLoadBalancerResponse;
import org.apache.cloudstack.api.response.GuestOSResponse;
import org.apache.cloudstack.api.response.GuestOsMappingResponse;
import org.apache.cloudstack.api.response.GuestVlanRangeResponse;
import org.apache.cloudstack.api.response.HostForMigrationResponse;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.HypervisorCapabilitiesResponse;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.cloudstack.api.response.ImageStoreResponse;
import org.apache.cloudstack.api.response.InstanceGroupResponse;
import org.apache.cloudstack.api.response.InternalLoadBalancerElementResponse;
import org.apache.cloudstack.api.response.IpForwardingRuleResponse;
import org.apache.cloudstack.api.response.IsolationMethodResponse;
import org.apache.cloudstack.api.response.LBHealthCheckPolicyResponse;
import org.apache.cloudstack.api.response.LBHealthCheckResponse;
import org.apache.cloudstack.api.response.LBStickinessPolicyResponse;
import org.apache.cloudstack.api.response.LBStickinessResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.LoadBalancerResponse;
import org.apache.cloudstack.api.response.NetworkACLItemResponse;
import org.apache.cloudstack.api.response.NetworkACLResponse;
import org.apache.cloudstack.api.response.NetworkOfferingResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.NicResponse;
import org.apache.cloudstack.api.response.NicSecondaryIpResponse;
import org.apache.cloudstack.api.response.PhysicalNetworkResponse;
import org.apache.cloudstack.api.response.PodResponse;
import org.apache.cloudstack.api.response.PortableIpRangeResponse;
import org.apache.cloudstack.api.response.PortableIpResponse;
import org.apache.cloudstack.api.response.PrivateGatewayResponse;
import org.apache.cloudstack.api.response.ProjectAccountResponse;
import org.apache.cloudstack.api.response.ProjectInvitationResponse;
import org.apache.cloudstack.api.response.ProjectResponse;
import org.apache.cloudstack.api.response.ProviderResponse;
import org.apache.cloudstack.api.response.RegionResponse;
import org.apache.cloudstack.api.response.RemoteAccessVpnResponse;
import org.apache.cloudstack.api.response.ResourceCountResponse;
import org.apache.cloudstack.api.response.ResourceLimitResponse;
import org.apache.cloudstack.api.response.ResourceTagResponse;
import org.apache.cloudstack.api.response.SSHKeyPairResponse;
import org.apache.cloudstack.api.response.SecurityGroupResponse;
import org.apache.cloudstack.api.response.SecurityGroupRuleResponse;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;
import org.apache.cloudstack.api.response.ServiceResponse;
import org.apache.cloudstack.api.response.Site2SiteCustomerGatewayResponse;
import org.apache.cloudstack.api.response.Site2SiteVpnConnectionResponse;
import org.apache.cloudstack.api.response.Site2SiteVpnGatewayResponse;
import org.apache.cloudstack.api.response.SnapshotPolicyResponse;
import org.apache.cloudstack.api.response.SnapshotResponse;
import org.apache.cloudstack.api.response.SnapshotScheduleResponse;
import org.apache.cloudstack.api.response.StaticRouteResponse;
import org.apache.cloudstack.api.response.StorageNetworkIpRangeResponse;
import org.apache.cloudstack.api.response.StoragePoolResponse;
import org.apache.cloudstack.api.response.SystemVmInstanceResponse;
import org.apache.cloudstack.api.response.SystemVmResponse;
import org.apache.cloudstack.api.response.TemplatePermissionsResponse;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.api.response.TrafficMonitorResponse;
import org.apache.cloudstack.api.response.TrafficTypeResponse;
import org.apache.cloudstack.api.response.UpgradeRouterTemplateResponse;
import org.apache.cloudstack.api.response.UsageRecordResponse;
import org.apache.cloudstack.api.response.UserResponse;
import org.apache.cloudstack.api.response.UserVmResponse;
import org.apache.cloudstack.api.response.VMSnapshotResponse;
import org.apache.cloudstack.api.response.VirtualRouterProviderResponse;
import org.apache.cloudstack.api.response.VlanIpRangeResponse;
import org.apache.cloudstack.api.response.VolumeResponse;
import org.apache.cloudstack.api.response.VpcOfferingResponse;
import org.apache.cloudstack.api.response.VpcResponse;
import org.apache.cloudstack.api.response.VpnUsersResponse;
import org.apache.cloudstack.api.response.ZoneResponse;
import org.apache.cloudstack.config.Configuration;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreCapabilities;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreManager;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotDataFactory;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.framework.jobs.AsyncJob;
import org.apache.cloudstack.framework.jobs.AsyncJobManager;
import org.apache.cloudstack.network.lb.ApplicationLoadBalancerRule;
import org.apache.cloudstack.region.PortableIp;
import org.apache.cloudstack.region.PortableIpRange;
import org.apache.cloudstack.region.Region;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.SnapshotDataStoreVO;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.apache.cloudstack.usage.Usage;
import org.apache.cloudstack.usage.UsageService;
import org.apache.cloudstack.usage.UsageTypes;

import javax.inject.Inject;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiResponseHelper implements ResponseGenerator {

    private static final Logger s_logger = LoggerFactory.getLogger(ApiResponseHelper.class);
    private static final DecimalFormat s_percentFormat = new DecimalFormat("##.##");
    @Inject
    protected AccountManager _accountMgr;
    @Inject
    protected AsyncJobManager _jobMgr;
    @Inject
    NetworkModel _ntwkModel;
    @Inject
    ConfigurationManager _configMgr;
    @Inject
    SnapshotDataFactory snapshotfactory;
    @Inject
    private EntityManager _entityMgr;
    @Inject
    private UsageService _usageSvc;
    @Inject
    private VolumeDao _volumeDao;
    @Inject
    private DataStoreManager _dataStoreMgr;
    @Inject
    private SnapshotDataStoreDao _snapshotStoreDao;
    @Inject
    private PrimaryDataStoreDao _storagePoolDao;

    public static List<CapacityResponse> getDataCenterCapacityResponse(final Long zoneId) {
        final List<SummedCapacity> capacities = ApiDBUtils.getCapacityByClusterPodZone(zoneId, null, null);
        final Set<CapacityResponse> capacityResponses = new HashSet<>();

        for (final SummedCapacity capacity : capacities) {
            final CapacityResponse capacityResponse = new CapacityResponse();
            capacityResponse.setCapacityType(capacity.getCapacityType());
            capacityResponse.setCapacityUsed(capacity.getUsedCapacity() + capacity.getReservedCapacity());
            if (capacity.getCapacityType() == Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED) {
                final List<SummedCapacity> c = ApiDBUtils.findNonSharedStorageForClusterPodZone(zoneId, null, null);
                capacityResponse.setCapacityTotal(capacity.getTotalCapacity() - c.get(0).getTotalCapacity());
                capacityResponse.setCapacityUsed(capacity.getUsedCapacity() - c.get(0).getUsedCapacity());
            } else {
                capacityResponse.setCapacityTotal(capacity.getTotalCapacity());
            }
            if (capacityResponse.getCapacityTotal() != 0) {
                capacityResponse.setPercentUsed(s_percentFormat.format((float) capacityResponse.getCapacityUsed() / (float) capacityResponse.getCapacityTotal() * 100f));
            } else {
                capacityResponse.setPercentUsed(s_percentFormat.format(0L));
            }
            capacityResponses.add(capacityResponse);
        }
        // Do it for stats as well.
        capacityResponses.addAll(getStatsCapacityresponse(null, null, null, zoneId));

        return new ArrayList<>(capacityResponses);
    }

    private static List<CapacityResponse> getStatsCapacityresponse(final Long poolId, final Long clusterId, final Long podId, final Long zoneId) {
        final List<CapacityVO> capacities = new ArrayList<>();
        capacities.add(ApiDBUtils.getStoragePoolUsedStats(poolId, clusterId, podId, zoneId));
        if (clusterId == null && podId == null) {
            capacities.add(ApiDBUtils.getSecondaryStorageUsedStats(poolId, zoneId));
        }

        final List<CapacityResponse> capacityResponses = new ArrayList<>();
        for (final CapacityVO capacity : capacities) {
            final CapacityResponse capacityResponse = new CapacityResponse();
            capacityResponse.setCapacityType(capacity.getCapacityType());
            capacityResponse.setCapacityUsed(capacity.getUsedCapacity());
            capacityResponse.setCapacityTotal(capacity.getTotalCapacity());
            if (capacityResponse.getCapacityTotal() != 0) {
                capacityResponse.setPercentUsed(s_percentFormat.format((float) capacityResponse.getCapacityUsed() / (float) capacityResponse.getCapacityTotal() * 100f));
            } else {
                capacityResponse.setPercentUsed(s_percentFormat.format(0L));
            }
            capacityResponses.add(capacityResponse);
        }

        return capacityResponses;
    }

    public static void populateOwner(final ControlledViewEntityResponse response, final ControlledViewEntity object) {

        if (object.getAccountType() == Account.ACCOUNT_TYPE_PROJECT) {
            response.setProjectId(object.getProjectUuid());
            response.setProjectName(object.getProjectName());
        } else {
            response.setAccountName(object.getAccountName());
        }

        response.setDomainId(object.getDomainUuid());
        response.setDomainName(object.getDomainName());
    }

    @Override
    public UserResponse createUserResponse(final UserAccount user) {
        final UserAccountJoinVO vUser = ApiDBUtils.newUserView(user);
        return ApiDBUtils.newUserResponse(vUser);
    }

    @Override
    public AccountResponse createAccountResponse(final ResponseView view, final Account account) {
        final AccountJoinVO vUser = ApiDBUtils.newAccountView(account);
        return ApiDBUtils.newAccountResponse(view, vUser);
    }

    @Override
    public DomainResponse createDomainResponse(final Domain domain) {
        final DomainResponse domainResponse = new DomainResponse();
        domainResponse.setDomainName(domain.getName());
        domainResponse.setId(domain.getUuid());
        domainResponse.setLevel(domain.getLevel());
        domainResponse.setNetworkDomain(domain.getNetworkDomain());
        final Domain parentDomain = ApiDBUtils.findDomainById(domain.getParent());
        if (parentDomain != null) {
            domainResponse.setParentDomainId(parentDomain.getUuid());
        }
        final StringBuilder domainPath = new StringBuilder("ROOT");
        domainPath.append(domain.getPath()).deleteCharAt(domainPath.length() - 1);
        domainResponse.setPath(domainPath.toString());
        if (domain.getParent() != null) {
            domainResponse.setParentDomainName(ApiDBUtils.findDomainById(domain.getParent()).getName());
        }
        if (domain.getChildCount() > 0) {
            domainResponse.setHasChild(true);
        }
        domainResponse.setObjectName("domain");
        return domainResponse;
    }

    @Override
    public DiskOfferingResponse createDiskOfferingResponse(final DiskOffering offering) {
        final DiskOfferingJoinVO vOffering = ApiDBUtils.newDiskOfferingView(offering);
        return ApiDBUtils.newDiskOfferingResponse(vOffering);
    }

    @Override
    public ResourceLimitResponse createResourceLimitResponse(final ResourceLimit limit) {
        final ResourceLimitResponse resourceLimitResponse = new ResourceLimitResponse();
        if (limit.getResourceOwnerType() == ResourceOwnerType.Domain) {
            populateDomain(resourceLimitResponse, limit.getOwnerId());
        } else if (limit.getResourceOwnerType() == ResourceOwnerType.Account) {
            final Account accountTemp = ApiDBUtils.findAccountById(limit.getOwnerId());
            populateAccount(resourceLimitResponse, limit.getOwnerId());
            populateDomain(resourceLimitResponse, accountTemp.getDomainId());
        }
        resourceLimitResponse.setResourceType(Integer.toString(limit.getType().getOrdinal()));

        if ((limit.getType() == ResourceType.primary_storage || limit.getType() == ResourceType.secondary_storage) && limit.getMax() >= 0) {
            resourceLimitResponse.setMax((long) Math.ceil((double) limit.getMax() / ResourceType.bytesToGiB));
        } else {
            resourceLimitResponse.setMax(limit.getMax());
        }
        resourceLimitResponse.setObjectName("resourcelimit");

        return resourceLimitResponse;
    }

    @Override
    public ResourceCountResponse createResourceCountResponse(final ResourceCount resourceCount) {
        final ResourceCountResponse resourceCountResponse = new ResourceCountResponse();

        if (resourceCount.getResourceOwnerType() == ResourceOwnerType.Account) {
            final Account accountTemp = ApiDBUtils.findAccountById(resourceCount.getOwnerId());
            if (accountTemp != null) {
                populateAccount(resourceCountResponse, accountTemp.getId());
                populateDomain(resourceCountResponse, accountTemp.getDomainId());
            }
        } else if (resourceCount.getResourceOwnerType() == ResourceOwnerType.Domain) {
            populateDomain(resourceCountResponse, resourceCount.getOwnerId());
        }

        resourceCountResponse.setResourceType(Integer.toString(resourceCount.getType().getOrdinal()));
        resourceCountResponse.setResourceCount(resourceCount.getCount());
        resourceCountResponse.setObjectName("resourcecount");
        return resourceCountResponse;
    }

    @Override
    public ServiceOfferingResponse createServiceOfferingResponse(final ServiceOffering offering) {
        final ServiceOfferingJoinVO vOffering = ApiDBUtils.newServiceOfferingView(offering);
        return ApiDBUtils.newServiceOfferingResponse(vOffering);
    }

    @Override
    public ConfigurationResponse createConfigurationResponse(final Configuration cfg) {
        final ConfigurationResponse cfgResponse = new ConfigurationResponse();
        cfgResponse.setCategory(cfg.getCategory());
        cfgResponse.setDescription(cfg.getDescription());
        cfgResponse.setName(cfg.getName());
        cfgResponse.setValue(cfg.getValue());
        cfgResponse.setObjectName("configuration");

        return cfgResponse;
    }

    @Override
    public SnapshotResponse createSnapshotResponse(final Snapshot snapshot) {
        final SnapshotResponse snapshotResponse = new SnapshotResponse();
        snapshotResponse.setId(snapshot.getUuid());

        populateOwner(snapshotResponse, snapshot);

        final VolumeVO volume = findVolumeById(snapshot.getVolumeId());
        final String snapshotTypeStr = snapshot.getRecurringType().name();
        snapshotResponse.setSnapshotType(snapshotTypeStr);
        if (volume != null) {
            snapshotResponse.setVolumeId(volume.getUuid());
            snapshotResponse.setVolumeName(volume.getName());
            snapshotResponse.setVolumeType(volume.getVolumeType().name());
            final DataCenter zone = ApiDBUtils.findZoneById(volume.getDataCenterId());
            if (zone != null) {
                snapshotResponse.setZoneId(zone.getUuid());
            }
        }
        snapshotResponse.setCreated(snapshot.getCreated());
        snapshotResponse.setName(snapshot.getName());
        snapshotResponse.setIntervalType(ApiDBUtils.getSnapshotIntervalTypes(snapshot.getId()));
        snapshotResponse.setState(snapshot.getState());

        SnapshotInfo snapshotInfo = null;

        if (snapshot instanceof SnapshotInfo) {
            snapshotInfo = (SnapshotInfo) snapshot;
        } else {
            final DataStoreRole dataStoreRole = getDataStoreRole(snapshot, _snapshotStoreDao, _dataStoreMgr);

            snapshotInfo = snapshotfactory.getSnapshot(snapshot.getId(), dataStoreRole);
        }

        if (snapshotInfo == null) {
            s_logger.debug("Unable to find info for image store snapshot with uuid " + snapshot.getUuid());
            snapshotResponse.setRevertable(false);
        } else {
            snapshotResponse.setRevertable(snapshotInfo.isRevertable());
            snapshotResponse.setPhysicaSize(snapshotInfo.getPhysicalSize());
        }

        // set tag information
        final List<? extends ResourceTag> tags = ApiDBUtils.listByResourceTypeAndId(ResourceObjectType.Snapshot, snapshot.getId());
        final List<ResourceTagResponse> tagResponses = new ArrayList<>();
        for (final ResourceTag tag : tags) {
            final ResourceTagResponse tagResponse = createResourceTagResponse(tag, true);
            CollectionUtils.addIgnoreNull(tagResponses, tagResponse);
        }
        snapshotResponse.setTags(tagResponses);

        snapshotResponse.setObjectName("snapshot");
        return snapshotResponse;
    }

    @Override
    public SnapshotPolicyResponse createSnapshotPolicyResponse(final SnapshotPolicy policy) {
        final SnapshotPolicyResponse policyResponse = new SnapshotPolicyResponse();
        policyResponse.setId(policy.getUuid());
        final Volume vol = ApiDBUtils.findVolumeById(policy.getVolumeId());
        if (vol != null) {
            policyResponse.setVolumeId(vol.getUuid());
        }
        policyResponse.setSchedule(policy.getSchedule());
        policyResponse.setIntervalType(policy.getInterval());
        policyResponse.setMaxSnaps(policy.getMaxSnaps());
        policyResponse.setTimezone(policy.getTimezone());
        policyResponse.setForDisplay(policy.isDisplay());
        policyResponse.setObjectName("snapshotpolicy");

        return policyResponse;
    }

    @Override
    public List<UserVmResponse> createUserVmResponse(final ResponseView view, final String objectName, final UserVm... userVms) {
        final List<UserVmJoinVO> viewVms = ApiDBUtils.newUserVmView(userVms);
        return ViewResponseHelper.createUserVmResponse(view, objectName, viewVms.toArray(new UserVmJoinVO[viewVms.size()]));
    }

    @Override
    public List<UserVmResponse> createUserVmResponse(final ResponseView view, final String objectName, final EnumSet<VMDetails> details, final UserVm... userVms) {
        final List<UserVmJoinVO> viewVms = ApiDBUtils.newUserVmView(userVms);
        return ViewResponseHelper.createUserVmResponse(view, objectName, details, viewVms.toArray(new UserVmJoinVO[viewVms.size()]));
    }

    @Override
    public SystemVmResponse createSystemVmResponse(final VirtualMachine vm) {
        final SystemVmResponse vmResponse = new SystemVmResponse();
        if (vm.getType() == Type.SecondaryStorageVm || vm.getType() == Type.ConsoleProxy || vm.getType() == Type.DomainRouter) {
            // SystemVm vm = (SystemVm) systemVM;
            vmResponse.setId(vm.getUuid());
            // vmResponse.setObjectId(vm.getId());
            vmResponse.setSystemVmType(vm.getType().toString().toLowerCase());

            vmResponse.setName(vm.getHostName());
            if (vm.getPodIdToDeployIn() != null) {
                final HostPodVO pod = ApiDBUtils.findPodById(vm.getPodIdToDeployIn());
                if (pod != null) {
                    vmResponse.setPodId(pod.getUuid());
                }
            }
            final VMTemplateVO template = ApiDBUtils.findTemplateById(vm.getTemplateId());
            if (template != null) {
                vmResponse.setTemplateId(template.getUuid());
            }
            vmResponse.setCreated(vm.getCreated());

            if (vm.getHostId() != null) {
                final Host host = ApiDBUtils.findHostById(vm.getHostId());
                if (host != null) {
                    vmResponse.setHostId(host.getUuid());
                    vmResponse.setHostName(host.getName());
                    vmResponse.setHypervisor(host.getHypervisorType().toString());
                }
            }

            if (vm.getState() != null) {
                vmResponse.setState(vm.getState().toString());
            }

            // for console proxies, add the active sessions
            if (vm.getType() == Type.ConsoleProxy) {
                final ConsoleProxyVO proxy = ApiDBUtils.findConsoleProxy(vm.getId());
                // proxy can be already destroyed
                if (proxy != null) {
                    vmResponse.setActiveViewerSessions(proxy.getActiveSession());
                }
            }

            final DataCenter zone = ApiDBUtils.findZoneById(vm.getDataCenterId());
            if (zone != null) {
                vmResponse.setZoneId(zone.getUuid());
                vmResponse.setZoneName(zone.getName());
                vmResponse.setDns1(zone.getDns1());
                vmResponse.setDns2(zone.getDns2());
            }

            final List<NicProfile> nicProfiles = ApiDBUtils.getNics(vm);
            for (final NicProfile singleNicProfile : nicProfiles) {
                final Network network = ApiDBUtils.findNetworkById(singleNicProfile.getNetworkId());
                if (network != null) {
                    if (network.getTrafficType() == TrafficType.Management) {
                        vmResponse.setPrivateIp(singleNicProfile.getIPv4Address());
                        vmResponse.setPrivateMacAddress(singleNicProfile.getMacAddress());
                        vmResponse.setPrivateNetmask(singleNicProfile.getIPv4Netmask());
                    } else if (network.getTrafficType() == TrafficType.Control) {
                        vmResponse.setLinkLocalIp(singleNicProfile.getIPv4Address());
                        vmResponse.setLinkLocalMacAddress(singleNicProfile.getMacAddress());
                        vmResponse.setLinkLocalNetmask(singleNicProfile.getIPv4Netmask());
                    } else if (network.getTrafficType() == TrafficType.Public) {
                        vmResponse.setPublicIp(singleNicProfile.getIPv4Address());
                        vmResponse.setPublicMacAddress(singleNicProfile.getMacAddress());
                        vmResponse.setPublicNetmask(singleNicProfile.getIPv4Netmask());
                        vmResponse.setGateway(singleNicProfile.getIPv4Gateway());
                    } else if (network.getTrafficType() == TrafficType.Guest) {
            /*
             * In basic zone, public ip has TrafficType.Guest in case EIP service is not enabled.
             * When EIP service is enabled in the basic zone, system VM by default get the public
             * IP allocated for EIP. So return the guest/public IP accordingly.
             * */
                        final NetworkOffering networkOffering = ApiDBUtils.findNetworkOfferingById(network.getNetworkOfferingId());
                        if (networkOffering.getElasticIp()) {
                            final IpAddress ip = ApiDBUtils.findIpByAssociatedVmId(vm.getId());
                            if (ip != null) {
                                final Vlan vlan = ApiDBUtils.findVlanById(ip.getVlanId());
                                vmResponse.setPublicIp(ip.getAddress().addr());
                                vmResponse.setPublicNetmask(vlan.getVlanNetmask());
                                vmResponse.setGateway(vlan.getVlanGateway());
                            }
                        } else {
                            vmResponse.setPublicIp(singleNicProfile.getIPv4Address());
                            vmResponse.setPublicMacAddress(singleNicProfile.getMacAddress());
                            vmResponse.setPublicNetmask(singleNicProfile.getIPv4Netmask());
                            vmResponse.setGateway(singleNicProfile.getIPv4Gateway());
                        }
                    }
                }
            }
        }
        vmResponse.setObjectName("systemvm");
        return vmResponse;
    }

    @Override
    public DomainRouterResponse createDomainRouterResponse(final VirtualRouter router) {
        final List<DomainRouterJoinVO> viewVrs = ApiDBUtils.newDomainRouterView(router);
        final List<DomainRouterResponse> listVrs = ViewResponseHelper.createDomainRouterResponse(viewVrs.toArray(new DomainRouterJoinVO[viewVrs.size()]));
        assert listVrs != null && listVrs.size() == 1 : "There should be one virtual router returned";
        return listVrs.get(0);
    }

    @Override
    public HostResponse createHostResponse(final Host host, final EnumSet<HostDetails> details) {
        final List<HostJoinVO> viewHosts = ApiDBUtils.newHostView(host);
        final List<HostResponse> listHosts = ViewResponseHelper.createHostResponse(details, viewHosts.toArray(new HostJoinVO[viewHosts.size()]));
        assert listHosts != null && listHosts.size() == 1 : "There should be one host returned";
        return listHosts.get(0);
    }

    @Override
    public HostResponse createHostResponse(final Host host) {
        return createHostResponse(host, EnumSet.of(HostDetails.all));
    }

    @Override
    public HostForMigrationResponse createHostForMigrationResponse(final Host host) {
        return createHostForMigrationResponse(host, EnumSet.of(HostDetails.all));
    }

    @Override
    public HostForMigrationResponse createHostForMigrationResponse(final Host host, final EnumSet<HostDetails> details) {
        final List<HostJoinVO> viewHosts = ApiDBUtils.newHostView(host);
        final List<HostForMigrationResponse> listHosts = ViewResponseHelper.createHostForMigrationResponse(details, viewHosts.toArray(new HostJoinVO[viewHosts.size()]));
        assert listHosts != null && listHosts.size() == 1 : "There should be one host returned";
        return listHosts.get(0);
    }

    @Override
    public VlanIpRangeResponse createVlanIpRangeResponse(final Vlan vlan) {
        final Long podId = ApiDBUtils.getPodIdForVlan(vlan.getId());

        final VlanIpRangeResponse vlanResponse = new VlanIpRangeResponse();
        vlanResponse.setId(vlan.getUuid());
        if (vlan.getVlanType() != null) {
            vlanResponse.setForVirtualNetwork(vlan.getVlanType().equals(VlanType.VirtualNetwork));
        }
        vlanResponse.setVlan(vlan.getVlanTag());
        final DataCenter zone = ApiDBUtils.findZoneById(vlan.getDataCenterId());
        if (zone != null) {
            vlanResponse.setZoneId(zone.getUuid());
        }

        if (podId != null) {
            final HostPodVO pod = ApiDBUtils.findPodById(podId);
            if (pod != null) {
                vlanResponse.setPodId(pod.getUuid());
                vlanResponse.setPodName(pod.getName());
            }
        }

        vlanResponse.setGateway(vlan.getVlanGateway());
        vlanResponse.setNetmask(vlan.getVlanNetmask());

        // get start ip and end ip of corresponding vlan
        final String ipRange = vlan.getIpRange();
        if (ipRange != null) {
            final String[] range = ipRange.split("-");
            vlanResponse.setStartIp(range[0]);
            vlanResponse.setEndIp(range[1]);
        }

        vlanResponse.setIp6Gateway(vlan.getIp6Gateway());
        vlanResponse.setIp6Cidr(vlan.getIp6Cidr());

        final String ip6Range = vlan.getIp6Range();
        if (ip6Range != null) {
            final String[] range = ip6Range.split("-");
            vlanResponse.setStartIpv6(range[0]);
            vlanResponse.setEndIpv6(range[1]);
        }

        if (vlan.getNetworkId() != null) {
            final Network nw = ApiDBUtils.findNetworkById(vlan.getNetworkId());
            if (nw != null) {
                vlanResponse.setNetworkId(nw.getUuid());
            }
        }
        final Account owner = ApiDBUtils.getVlanAccount(vlan.getId());
        if (owner != null) {
            populateAccount(vlanResponse, owner.getId());
            populateDomain(vlanResponse, owner.getDomainId());
        } else {
            final Domain domain = ApiDBUtils.getVlanDomain(vlan.getId());
            if (domain != null) {
                populateDomain(vlanResponse, domain.getId());
            } else {
                final Long networkId = vlan.getNetworkId();
                if (networkId != null) {
                    final Network network = _ntwkModel.getNetwork(networkId);
                    if (network != null) {
                        final Long accountId = network.getAccountId();
                        populateAccount(vlanResponse, accountId);
                        populateDomain(vlanResponse, ApiDBUtils.findAccountById(accountId).getDomainId());
                    }
                }
            }
        }

        if (vlan.getPhysicalNetworkId() != null) {
            final PhysicalNetwork pnw = ApiDBUtils.findPhysicalNetworkById(vlan.getPhysicalNetworkId());
            if (pnw != null) {
                vlanResponse.setPhysicalNetworkId(pnw.getUuid());
            }
        }
        vlanResponse.setObjectName("vlan");
        return vlanResponse;
    }

    @Override
    public IPAddressResponse createIPAddressResponse(final ResponseView view, final IpAddress ipAddr) {
        final VlanVO vlan = ApiDBUtils.findVlanById(ipAddr.getVlanId());
        final boolean forVirtualNetworks = vlan.getVlanType().equals(VlanType.VirtualNetwork);
        final long zoneId = ipAddr.getDataCenterId();

        final IPAddressResponse ipResponse = new IPAddressResponse();
        ipResponse.setId(ipAddr.getUuid());
        ipResponse.setIpAddress(ipAddr.getAddress().toString());
        if (ipAddr.getAllocatedTime() != null) {
            ipResponse.setAllocated(ipAddr.getAllocatedTime());
        }
        final DataCenter zone = ApiDBUtils.findZoneById(ipAddr.getDataCenterId());
        if (zone != null) {
            ipResponse.setZoneId(zone.getUuid());
            ipResponse.setZoneName(zone.getName());
        }
        ipResponse.setSourceNat(ipAddr.isSourceNat());
        ipResponse.setIsSystem(ipAddr.getSystem());

        // get account information
        if (ipAddr.getAllocatedToAccountId() != null) {
            populateOwner(ipResponse, ipAddr);
        }

        ipResponse.setForVirtualNetwork(forVirtualNetworks);
        ipResponse.setStaticNat(ipAddr.isOneToOneNat());

        if (ipAddr.getAssociatedWithVmId() != null) {
            final UserVm vm = ApiDBUtils.findUserVmById(ipAddr.getAssociatedWithVmId());
            if (vm != null) {
                ipResponse.setVirtualMachineId(vm.getUuid());
                ipResponse.setVirtualMachineName(vm.getHostName());
                if (vm.getDisplayName() != null) {
                    ipResponse.setVirtualMachineDisplayName(vm.getDisplayName());
                } else {
                    ipResponse.setVirtualMachineDisplayName(vm.getHostName());
                }
            }
        }
        if (ipAddr.getVmIp() != null) {
            ipResponse.setVirtualMachineIp(ipAddr.getVmIp());
        }

        if (ipAddr.getAssociatedWithNetworkId() != null) {
            final Network ntwk = ApiDBUtils.findNetworkById(ipAddr.getAssociatedWithNetworkId());
            if (ntwk != null) {
                ipResponse.setAssociatedNetworkId(ntwk.getUuid());
                ipResponse.setAssociatedNetworkName(ntwk.getName());
            }
        }

        if (ipAddr.getVpcId() != null) {
            final Vpc vpc = ApiDBUtils.findVpcById(ipAddr.getVpcId());
            if (vpc != null) {
                ipResponse.setVpcId(vpc.getUuid());
            }
        }

        // Network id the ip is associated with (if associated networkId is
        // null, try to get this information from vlan)
        final Long vlanNetworkId = ApiDBUtils.getVlanNetworkId(ipAddr.getVlanId());

        // Network id the ip belongs to
        final Long networkId;
        if (vlanNetworkId != null) {
            networkId = vlanNetworkId;
        } else {
            networkId = ApiDBUtils.getPublicNetworkIdByZone(zoneId);
        }

        if (networkId != null) {
            final NetworkVO nw = ApiDBUtils.findNetworkById(networkId);
            if (nw != null) {
                ipResponse.setNetworkId(nw.getUuid());
            }
        }
        ipResponse.setState(ipAddr.getState().toString());

        if (ipAddr.getPhysicalNetworkId() != null) {
            final PhysicalNetworkVO pnw = ApiDBUtils.findPhysicalNetworkById(ipAddr.getPhysicalNetworkId());
            if (pnw != null) {
                ipResponse.setPhysicalNetworkId(pnw.getUuid());
            }
        }

        // show this info to full view only
        if (view == ResponseView.Full) {
            final VlanVO vl = ApiDBUtils.findVlanById(ipAddr.getVlanId());
            if (vl != null) {
                ipResponse.setVlanId(vl.getUuid());
                ipResponse.setVlanName(vl.getVlanTag());
            }
        }

        if (ipAddr.getSystem()) {
            if (ipAddr.isOneToOneNat()) {
                ipResponse.setPurpose(IpAddress.Purpose.StaticNat.toString());
            } else {
                ipResponse.setPurpose(IpAddress.Purpose.Lb.toString());
            }
        }

        ipResponse.setForDisplay(ipAddr.isDisplay());

        ipResponse.setPortable(ipAddr.isPortable());

        //set tag information
        final List<? extends ResourceTag> tags = ApiDBUtils.listByResourceTypeAndId(ResourceObjectType.PublicIpAddress, ipAddr.getId());
        final List<ResourceTagResponse> tagResponses = new ArrayList<>();
        for (final ResourceTag tag : tags) {
            final ResourceTagResponse tagResponse = createResourceTagResponse(tag, true);
            CollectionUtils.addIgnoreNull(tagResponses, tagResponse);
        }
        ipResponse.setTags(tagResponses);

        ipResponse.setObjectName("ipaddress");
        return ipResponse;
    }

    @Override
    public GuestVlanRangeResponse createDedicatedGuestVlanRangeResponse(final GuestVlan vlan) {
        final GuestVlanRangeResponse guestVlanRangeResponse = new GuestVlanRangeResponse();

        guestVlanRangeResponse.setId(vlan.getUuid());
        final Long accountId = ApiDBUtils.getAccountIdForGuestVlan(vlan.getId());
        final Account owner = ApiDBUtils.findAccountById(accountId);
        if (owner != null) {
            populateAccount(guestVlanRangeResponse, owner.getId());
            populateDomain(guestVlanRangeResponse, owner.getDomainId());
        }
        guestVlanRangeResponse.setGuestVlanRange(vlan.getGuestVlanRange());
        guestVlanRangeResponse.setPhysicalNetworkId(vlan.getPhysicalNetworkId());
        final PhysicalNetworkVO physicalNetwork = ApiDBUtils.findPhysicalNetworkById(vlan.getPhysicalNetworkId());
        guestVlanRangeResponse.setZoneId(physicalNetwork.getDataCenterId());

        return guestVlanRangeResponse;
    }

    @Override
    public GlobalLoadBalancerResponse createGlobalLoadBalancerResponse(final GlobalLoadBalancerRule globalLoadBalancerRule) {
        final GlobalLoadBalancerResponse response = new GlobalLoadBalancerResponse();
        response.setAlgorithm(globalLoadBalancerRule.getAlgorithm());
        response.setStickyMethod(globalLoadBalancerRule.getPersistence());
        response.setServiceType(globalLoadBalancerRule.getServiceType());
        response.setServiceDomainName(globalLoadBalancerRule.getGslbDomain() + "." + ApiDBUtils.getDnsNameConfiguredForGslb());
        response.setName(globalLoadBalancerRule.getName());
        response.setDescription(globalLoadBalancerRule.getDescription());
        response.setRegionIdId(globalLoadBalancerRule.getRegion());
        response.setId(globalLoadBalancerRule.getUuid());
        populateOwner(response, globalLoadBalancerRule);
        response.setObjectName("globalloadbalancer");

        final List<LoadBalancerResponse> siteLbResponses = new ArrayList<>();
        final List<? extends LoadBalancer> siteLoadBalaners = ApiDBUtils.listSiteLoadBalancers(globalLoadBalancerRule.getId());
        for (final LoadBalancer siteLb : siteLoadBalaners) {
            final LoadBalancerResponse siteLbResponse = createLoadBalancerResponse(siteLb);
            siteLbResponses.add(siteLbResponse);
        }
        response.setSiteLoadBalancers(siteLbResponses);
        return response;
    }

    @Override
    public LoadBalancerResponse createLoadBalancerResponse(final LoadBalancer loadBalancer) {
        final LoadBalancerResponse lbResponse = new LoadBalancerResponse();
        lbResponse.setId(loadBalancer.getUuid());
        lbResponse.setName(loadBalancer.getName());
        lbResponse.setDescription(loadBalancer.getDescription());
        final List<String> cidrs = ApiDBUtils.findFirewallSourceCidrs(loadBalancer.getId());
        lbResponse.setCidrList(StringUtils.join(cidrs, ","));

        final IPAddressVO publicIp = ApiDBUtils.findIpAddressById(loadBalancer.getSourceIpAddressId());
        lbResponse.setPublicIpId(publicIp.getUuid());
        lbResponse.setPublicIp(publicIp.getAddress().addr());
        lbResponse.setPublicPort(Integer.toString(loadBalancer.getSourcePortStart()));
        lbResponse.setPrivatePort(Integer.toString(loadBalancer.getDefaultPortStart()));
        lbResponse.setAlgorithm(loadBalancer.getAlgorithm());
        lbResponse.setLbProtocol(loadBalancer.getLbProtocol());
        lbResponse.setForDisplay(loadBalancer.isDisplay());
        final FirewallRule.State state = loadBalancer.getState();
        String stateToSet = state.toString();
        if (state.equals(FirewallRule.State.Revoke)) {
            stateToSet = "Deleting";
        }
        lbResponse.setState(stateToSet);
        populateOwner(lbResponse, loadBalancer);
        final DataCenter zone = ApiDBUtils.findZoneById(publicIp.getDataCenterId());
        if (zone != null) {
            lbResponse.setZoneId(zone.getUuid());
        }

        //set tag information
        final List<? extends ResourceTag> tags = ApiDBUtils.listByResourceTypeAndId(ResourceObjectType.LoadBalancer, loadBalancer.getId());
        final List<ResourceTagResponse> tagResponses = new ArrayList<>();
        for (final ResourceTag tag : tags) {
            final ResourceTagResponse tagResponse = createResourceTagResponse(tag, true);
            CollectionUtils.addIgnoreNull(tagResponses, tagResponse);
        }
        lbResponse.setTags(tagResponses);

        final Network ntwk = ApiDBUtils.findNetworkById(loadBalancer.getNetworkId());
        lbResponse.setNetworkId(ntwk.getUuid());

        lbResponse.setObjectName("loadbalancer");
        return lbResponse;
    }

    @Override
    public LBStickinessResponse createLBStickinessPolicyResponse(final List<? extends StickinessPolicy> stickinessPolicies, final LoadBalancer lb) {
        final LBStickinessResponse spResponse = new LBStickinessResponse();

        if (lb == null) {
            return spResponse;
        }
        spResponse.setlbRuleId(lb.getUuid());
        final Account account = ApiDBUtils.findAccountById(lb.getAccountId());
        if (account != null) {
            spResponse.setAccountName(account.getAccountName());
            final Domain domain = ApiDBUtils.findDomainById(account.getDomainId());
            if (domain != null) {
                spResponse.setDomainId(domain.getUuid());
                spResponse.setDomainName(domain.getName());
            }
        }

        final List<LBStickinessPolicyResponse> responses = new ArrayList<>();
        for (final StickinessPolicy stickinessPolicy : stickinessPolicies) {
            final LBStickinessPolicyResponse ruleResponse = new LBStickinessPolicyResponse(stickinessPolicy);
            responses.add(ruleResponse);
        }
        spResponse.setRules(responses);

        spResponse.setObjectName("stickinesspolicies");
        return spResponse;
    }

    @Override
    public LBStickinessResponse createLBStickinessPolicyResponse(final StickinessPolicy stickinessPolicy, final LoadBalancer lb) {
        final LBStickinessResponse spResponse = new LBStickinessResponse();

        spResponse.setlbRuleId(lb.getUuid());
        final Account accountTemp = ApiDBUtils.findAccountById(lb.getAccountId());
        if (accountTemp != null) {
            spResponse.setAccountName(accountTemp.getAccountName());
            final Domain domain = ApiDBUtils.findDomainById(accountTemp.getDomainId());
            if (domain != null) {
                spResponse.setDomainId(domain.getUuid());
                spResponse.setDomainName(domain.getName());
            }
        }

        final List<LBStickinessPolicyResponse> responses = new ArrayList<>();
        final LBStickinessPolicyResponse ruleResponse = new LBStickinessPolicyResponse(stickinessPolicy);
        responses.add(ruleResponse);

        spResponse.setRules(responses);

        spResponse.setObjectName("stickinesspolicies");
        return spResponse;
    }

    @Override
    public LBHealthCheckResponse createLBHealthCheckPolicyResponse(final List<? extends HealthCheckPolicy> healthcheckPolicies, final LoadBalancer lb) {
        final LBHealthCheckResponse hcResponse = new LBHealthCheckResponse();

        if (lb == null) {
            return hcResponse;
        }
        hcResponse.setlbRuleId(lb.getUuid());
        final Account account = ApiDBUtils.findAccountById(lb.getAccountId());
        if (account != null) {
            hcResponse.setAccountName(account.getAccountName());
            final Domain domain = ApiDBUtils.findDomainById(account.getDomainId());
            if (domain != null) {
                hcResponse.setDomainId(domain.getUuid());
                hcResponse.setDomainName(domain.getName());
            }
        }

        final List<LBHealthCheckPolicyResponse> responses = new ArrayList<>();
        for (final HealthCheckPolicy healthcheckPolicy : healthcheckPolicies) {
            final LBHealthCheckPolicyResponse ruleResponse = new LBHealthCheckPolicyResponse(healthcheckPolicy);
            responses.add(ruleResponse);
        }
        hcResponse.setRules(responses);

        hcResponse.setObjectName("healthcheckpolicies");
        return hcResponse;
    }

    @Override
    public LBHealthCheckResponse createLBHealthCheckPolicyResponse(final HealthCheckPolicy healthcheckPolicy, final LoadBalancer lb) {
        final LBHealthCheckResponse hcResponse = new LBHealthCheckResponse();

        hcResponse.setlbRuleId(lb.getUuid());
        final Account accountTemp = ApiDBUtils.findAccountById(lb.getAccountId());
        if (accountTemp != null) {
            hcResponse.setAccountName(accountTemp.getAccountName());
            final Domain domain = ApiDBUtils.findDomainById(accountTemp.getDomainId());
            if (domain != null) {
                hcResponse.setDomainId(domain.getUuid());
                hcResponse.setDomainName(domain.getName());
            }
        }

        final List<LBHealthCheckPolicyResponse> responses = new ArrayList<>();
        final LBHealthCheckPolicyResponse ruleResponse = new LBHealthCheckPolicyResponse(healthcheckPolicy);
        responses.add(ruleResponse);
        hcResponse.setRules(responses);
        hcResponse.setObjectName("healthcheckpolicies");
        return hcResponse;
    }

    @Override
    public PodResponse createPodResponse(final Pod pod, final Boolean showCapacities) {
        String[] ipRange = new String[2];
        if (pod.getDescription() != null && pod.getDescription().length() > 0) {
            ipRange = pod.getDescription().split("-");
        } else {
            ipRange[0] = pod.getDescription();
        }

        final PodResponse podResponse = new PodResponse();
        podResponse.setId(pod.getUuid());
        podResponse.setName(pod.getName());
        final DataCenter zone = ApiDBUtils.findZoneById(pod.getDataCenterId());
        if (zone != null) {
            podResponse.setZoneId(zone.getUuid());
            podResponse.setZoneName(zone.getName());
        }
        podResponse.setNetmask(NetUtils.getCidrNetmask(pod.getCidrSize()));
        podResponse.setStartIp(ipRange[0]);
        podResponse.setEndIp(ipRange.length > 1 && ipRange[1] != null ? ipRange[1] : "");
        podResponse.setGateway(pod.getGateway());
        podResponse.setAllocationState(pod.getAllocationState().toString());
        if (showCapacities != null && showCapacities) {
            final List<SummedCapacity> capacities = ApiDBUtils.getCapacityByClusterPodZone(null, pod.getId(), null);
            final Set<CapacityResponse> capacityResponses = new HashSet<>();
            for (final SummedCapacity capacity : capacities) {
                final CapacityResponse capacityResponse = new CapacityResponse();
                capacityResponse.setCapacityType(capacity.getCapacityType());
                capacityResponse.setCapacityUsed(capacity.getUsedCapacity() + capacity.getReservedCapacity());
                if (capacity.getCapacityType() == Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED) {
                    final List<SummedCapacity> c = ApiDBUtils.findNonSharedStorageForClusterPodZone(null, pod.getId(), null);
                    capacityResponse.setCapacityTotal(capacity.getTotalCapacity() - c.get(0).getTotalCapacity());
                    capacityResponse.setCapacityUsed(capacity.getUsedCapacity() - c.get(0).getUsedCapacity());
                } else {
                    capacityResponse.setCapacityTotal(capacity.getTotalCapacity());
                }
                if (capacityResponse.getCapacityTotal() != 0) {
                    capacityResponse.setPercentUsed(s_percentFormat.format((float) capacityResponse.getCapacityUsed() / (float) capacityResponse.getCapacityTotal() * 100f));
                } else {
                    capacityResponse.setPercentUsed(s_percentFormat.format(0L));
                }
                capacityResponses.add(capacityResponse);
            }
            // Do it for stats as well.
            capacityResponses.addAll(getStatsCapacityresponse(null, null, pod.getId(), pod.getDataCenterId()));
            podResponse.setCapacitites(new ArrayList<>(capacityResponses));
        }
        podResponse.setObjectName("pod");
        return podResponse;
    }

    @Override
    public ZoneResponse createZoneResponse(final ResponseView view, final DataCenter dataCenter, final Boolean showCapacities) {
        final DataCenterJoinVO vOffering = ApiDBUtils.newDataCenterView(dataCenter);
        return ApiDBUtils.newDataCenterResponse(view, vOffering, showCapacities);
    }

    @Override
    public VolumeResponse createVolumeResponse(final ResponseView view, final Volume volume) {
        final List<VolumeJoinVO> viewVrs = ApiDBUtils.newVolumeView(volume);
        final List<VolumeResponse> listVrs = ViewResponseHelper.createVolumeResponse(view, viewVrs.toArray(new VolumeJoinVO[viewVrs.size()]));
        assert listVrs != null && listVrs.size() == 1 : "There should be one volume returned";
        return listVrs.get(0);
    }

    @Override
    public InstanceGroupResponse createInstanceGroupResponse(final InstanceGroup group) {
        final InstanceGroupJoinVO vgroup = ApiDBUtils.newInstanceGroupView(group);
        return ApiDBUtils.newInstanceGroupResponse(vgroup);
    }

  /*
    @Override
    public List<UserVmResponse> createUserVmResponse(String objectName, UserVm... userVms) {
        return createUserVmResponse(null, objectName, userVms);
    }

    @Override
    public List<UserVmResponse> createUserVmResponse(String objectName, EnumSet<VMDetails> details, UserVm... userVms) {
        return createUserVmResponse(null, objectName, userVms);
    }
   */

    @Override
    public StoragePoolResponse createStoragePoolResponse(final StoragePool pool) {
        final List<StoragePoolJoinVO> viewPools = ApiDBUtils.newStoragePoolView(pool);
        final List<StoragePoolResponse> listPools = ViewResponseHelper.createStoragePoolResponse(viewPools.toArray(new StoragePoolJoinVO[viewPools.size()]));
        assert listPools != null && listPools.size() == 1 : "There should be one storage pool returned";
        return listPools.get(0);
    }

    @Override
    public StoragePoolResponse createStoragePoolForMigrationResponse(final StoragePool pool) {
        final List<StoragePoolJoinVO> viewPools = ApiDBUtils.newStoragePoolView(pool);
        final List<StoragePoolResponse> listPools = ViewResponseHelper.createStoragePoolForMigrationResponse(viewPools.toArray(new StoragePoolJoinVO[viewPools.size()]));
        assert listPools != null && listPools.size() == 1 : "There should be one storage pool returned";
        return listPools.get(0);
    }

    @Override
    public ClusterResponse createClusterResponse(final Cluster cluster, final Boolean showCapacities) {
        final ClusterResponse clusterResponse = new ClusterResponse();
        clusterResponse.setId(cluster.getUuid());
        clusterResponse.setName(cluster.getName());
        final HostPodVO pod = ApiDBUtils.findPodById(cluster.getPodId());
        if (pod != null) {
            clusterResponse.setPodId(pod.getUuid());
            clusterResponse.setPodName(pod.getName());
        }
        final DataCenter dc = ApiDBUtils.findZoneById(cluster.getDataCenterId());
        if (dc != null) {
            clusterResponse.setZoneId(dc.getUuid());
            clusterResponse.setZoneName(dc.getName());
        }
        clusterResponse.setHypervisorType(cluster.getHypervisorType().toString());
        clusterResponse.setClusterType(cluster.getClusterType().toString());
        clusterResponse.setAllocationState(cluster.getAllocationState().toString());
        clusterResponse.setManagedState(cluster.getManagedState().toString());
        final String cpuOvercommitRatio = ApiDBUtils.findClusterDetails(cluster.getId(), "cpuOvercommitRatio");
        final String memoryOvercommitRatio = ApiDBUtils.findClusterDetails(cluster.getId(), "memoryOvercommitRatio");
        clusterResponse.setCpuOvercommitRatio(cpuOvercommitRatio);
        clusterResponse.setMemoryOvercommitRatio(memoryOvercommitRatio);

        if (showCapacities != null && showCapacities) {
            final List<SummedCapacity> capacities = ApiDBUtils.getCapacityByClusterPodZone(null, null, cluster.getId());
            final Set<CapacityResponse> capacityResponses = new HashSet<>();

            for (final SummedCapacity capacity : capacities) {
                final CapacityResponse capacityResponse = new CapacityResponse();
                capacityResponse.setCapacityType(capacity.getCapacityType());
                capacityResponse.setCapacityUsed(capacity.getUsedCapacity() + capacity.getReservedCapacity());

                if (capacity.getCapacityType() == Capacity.CAPACITY_TYPE_STORAGE_ALLOCATED) {
                    final List<SummedCapacity> c = ApiDBUtils.findNonSharedStorageForClusterPodZone(null, null, cluster.getId());
                    capacityResponse.setCapacityTotal(capacity.getTotalCapacity() - c.get(0).getTotalCapacity());
                    capacityResponse.setCapacityUsed(capacity.getUsedCapacity() - c.get(0).getUsedCapacity());
                } else {
                    capacityResponse.setCapacityTotal(capacity.getTotalCapacity());
                }
                if (capacityResponse.getCapacityTotal() != 0) {
                    capacityResponse.setPercentUsed(s_percentFormat.format((float) capacityResponse.getCapacityUsed() / (float) capacityResponse.getCapacityTotal() * 100f));
                } else {
                    capacityResponse.setPercentUsed(s_percentFormat.format(0L));
                }
                capacityResponses.add(capacityResponse);
            }
            // Do it for stats as well.
            capacityResponses.addAll(getStatsCapacityresponse(null, cluster.getId(), pod.getId(), pod.getDataCenterId()));
            clusterResponse.setCapacitites(new ArrayList<>(capacityResponses));
        }
        clusterResponse.setObjectName("cluster");
        return clusterResponse;
    }

    @Override
    public FirewallRuleResponse createPortForwardingRuleResponse(final PortForwardingRule fwRule) {
        final FirewallRuleResponse response = new FirewallRuleResponse();
        response.setId(fwRule.getUuid());
        response.setPrivateStartPort(Integer.toString(fwRule.getDestinationPortStart()));
        response.setPrivateEndPort(Integer.toString(fwRule.getDestinationPortEnd()));
        response.setProtocol(fwRule.getProtocol());
        response.setPublicStartPort(Integer.toString(fwRule.getSourcePortStart()));
        response.setPublicEndPort(Integer.toString(fwRule.getSourcePortEnd()));
        final List<String> cidrs = ApiDBUtils.findFirewallSourceCidrs(fwRule.getId());
        response.setCidrList(StringUtils.join(cidrs, ","));

        final Network guestNtwk = ApiDBUtils.findNetworkById(fwRule.getNetworkId());
        response.setNetworkId(guestNtwk.getUuid());

        final IpAddress ip = ApiDBUtils.findIpAddressById(fwRule.getSourceIpAddressId());

        if (ip != null) {
            response.setPublicIpAddressId(ip.getUuid());
            response.setPublicIpAddress(ip.getAddress().addr());
            if (fwRule.getDestinationIpAddress() != null) {
                response.setDestNatVmIp(fwRule.getDestinationIpAddress().toString());
                final UserVm vm = ApiDBUtils.findUserVmById(fwRule.getVirtualMachineId());
                if (vm != null) {
                    response.setVirtualMachineId(vm.getUuid());
                    response.setVirtualMachineName(vm.getHostName());

                    if (vm.getDisplayName() != null) {
                        response.setVirtualMachineDisplayName(vm.getDisplayName());
                    } else {
                        response.setVirtualMachineDisplayName(vm.getHostName());
                    }
                }
            }
        }
        final FirewallRule.State state = fwRule.getState();
        String stateToSet = state.toString();
        if (state.equals(FirewallRule.State.Revoke)) {
            stateToSet = "Deleting";
        }

        // set tag information
        final List<? extends ResourceTag> tags = ApiDBUtils.listByResourceTypeAndId(ResourceObjectType.PortForwardingRule, fwRule.getId());
        final List<ResourceTagResponse> tagResponses = new ArrayList<>();
        for (final ResourceTag tag : tags) {
            final ResourceTagResponse tagResponse = createResourceTagResponse(tag, true);
            CollectionUtils.addIgnoreNull(tagResponses, tagResponse);
        }
        response.setTags(tagResponses);

        response.setState(stateToSet);
        response.setForDisplay(fwRule.isDisplay());
        response.setObjectName("portforwardingrule");
        return response;
    }

    @Override
    public IpForwardingRuleResponse createIpForwardingRuleResponse(final StaticNatRule fwRule) {
        final IpForwardingRuleResponse response = new IpForwardingRuleResponse();
        response.setId(fwRule.getUuid());
        response.setProtocol(fwRule.getProtocol());

        final IpAddress ip = ApiDBUtils.findIpAddressById(fwRule.getSourceIpAddressId());

        if (ip != null) {
            response.setPublicIpAddressId(ip.getId());
            response.setPublicIpAddress(ip.getAddress().addr());
            if (fwRule.getDestIpAddress() != null) {
                final UserVm vm = ApiDBUtils.findUserVmById(ip.getAssociatedWithVmId());
                if (vm != null) {// vm might be destroyed
                    response.setVirtualMachineId(vm.getUuid());
                    response.setVirtualMachineName(vm.getHostName());
                    if (vm.getDisplayName() != null) {
                        response.setVirtualMachineDisplayName(vm.getDisplayName());
                    } else {
                        response.setVirtualMachineDisplayName(vm.getHostName());
                    }
                }
            }
        }
        final FirewallRule.State state = fwRule.getState();
        String stateToSet = state.toString();
        if (state.equals(FirewallRule.State.Revoke)) {
            stateToSet = "Deleting";
        }

        response.setStartPort(fwRule.getSourcePortStart());
        response.setEndPort(fwRule.getSourcePortEnd());
        response.setProtocol(fwRule.getProtocol());
        response.setState(stateToSet);
        response.setObjectName("ipforwardingrule");
        return response;
    }

    @Override
    public User findUserById(final Long userId) {
        return ApiDBUtils.findUserById(userId);
    }

    @Override
    public UserVm findUserVmById(final Long vmId) {
        return ApiDBUtils.findUserVmById(vmId);
    }

    @Override
    public VolumeVO findVolumeById(final Long volumeId) {
        return ApiDBUtils.findVolumeById(volumeId);
    }

    @Override
    public Account findAccountByNameDomain(final String accountName, final Long domainId) {
        return ApiDBUtils.findAccountByNameDomain(accountName, domainId);
    }

    @Override
    public VirtualMachineTemplate findTemplateById(final Long templateId) {
        return ApiDBUtils.findTemplateById(templateId);
    }

    @Override
    public Host findHostById(final Long hostId) {
        return ApiDBUtils.findHostById(hostId);
    }

    @Override
    public VpnUsersResponse createVpnUserResponse(final VpnUser vpnUser) {
        final VpnUsersResponse vpnResponse = new VpnUsersResponse();
        vpnResponse.setId(vpnUser.getUuid());
        vpnResponse.setUserName(vpnUser.getUsername());
        vpnResponse.setState(vpnUser.getState().toString());

        populateOwner(vpnResponse, vpnUser);

        vpnResponse.setObjectName("vpnuser");
        return vpnResponse;
    }

    @Override
    public RemoteAccessVpnResponse createRemoteAccessVpnResponse(final RemoteAccessVpn vpn) {
        final RemoteAccessVpnResponse vpnResponse = new RemoteAccessVpnResponse();
        final IpAddress ip = ApiDBUtils.findIpAddressById(vpn.getServerAddressId());
        if (ip != null) {
            vpnResponse.setPublicIpId(ip.getUuid());
            vpnResponse.setPublicIp(ip.getAddress().addr());
        }
        vpnResponse.setIpRange(vpn.getIpRange());
        vpnResponse.setPresharedKey(vpn.getIpsecPresharedKey());
        populateOwner(vpnResponse, vpn);
        vpnResponse.setState(vpn.getState().toString());
        vpnResponse.setId(vpn.getUuid());
        vpnResponse.setForDisplay(vpn.isDisplay());
        vpnResponse.setObjectName("remoteaccessvpn");

        return vpnResponse;
    }

    @Override
    public List<TemplateResponse> createTemplateResponses(final ResponseView view, final long templateId, final Long zoneId, final boolean readyOnly) {
        final VirtualMachineTemplate template = findTemplateById(templateId);
        return createTemplateResponses(view, template, zoneId, readyOnly);
    }

    @Override
    public List<TemplateResponse> createTemplateResponses(final ResponseView view, final long templateId, final Long snapshotId, final Long volumeId, final boolean readyOnly) {
        Long zoneId = null;

        if (snapshotId != null) {
            final Snapshot snapshot = ApiDBUtils.findSnapshotById(snapshotId);
            final VolumeVO volume = findVolumeById(snapshot.getVolumeId());

            // it seems that the volume can actually be removed from the DB at some point if it's deleted
            // if volume comes back null, use another technique to try to discover the zone
            if (volume == null) {
                final SnapshotDataStoreVO snapshotStore = _snapshotStoreDao.findBySnapshot(snapshot.getId(), DataStoreRole.Primary);

                if (snapshotStore != null) {
                    final long storagePoolId = snapshotStore.getDataStoreId();

                    final StoragePoolVO storagePool = _storagePoolDao.findById(storagePoolId);

                    if (storagePool != null) {
                        zoneId = storagePool.getDataCenterId();
                    }
                }
            } else {
                zoneId = volume.getDataCenterId();
            }
        } else {
            final VolumeVO volume = findVolumeById(volumeId);

            zoneId = volume.getDataCenterId();
        }

        if (zoneId == null) {
            throw new CloudRuntimeException("Unable to determine the zone ID");
        }

        return createTemplateResponses(view, templateId, zoneId, readyOnly);
    }

    @Override
    public SecurityGroupResponse createSecurityGroupResponseFromSecurityGroupRule(final List<? extends SecurityRule> securityRules) {
        final SecurityGroupResponse response = new SecurityGroupResponse();
        final Map<Long, Account> securiytGroupAccounts = new HashMap<>();

        if (securityRules != null && !securityRules.isEmpty()) {
            final SecurityGroupJoinVO securityGroup = ApiDBUtils.findSecurityGroupViewById(securityRules.get(0).getSecurityGroupId()).get(0);
            response.setId(securityGroup.getUuid());
            response.setName(securityGroup.getName());
            response.setDescription(securityGroup.getDescription());

            securiytGroupAccounts.get(securityGroup.getAccountId());

            if (securityGroup.getAccountType() == Account.ACCOUNT_TYPE_PROJECT) {
                response.setProjectId(securityGroup.getProjectUuid());
                response.setProjectName(securityGroup.getProjectName());
            } else {
                response.setAccountName(securityGroup.getAccountName());
            }

            response.setDomainId(securityGroup.getDomainUuid());
            response.setDomainName(securityGroup.getDomainName());

            for (final SecurityRule securityRule : securityRules) {
                final SecurityGroupRuleResponse securityGroupData = new SecurityGroupRuleResponse();

                securityGroupData.setRuleId(securityRule.getUuid());
                securityGroupData.setProtocol(securityRule.getProtocol());
                if ("icmp".equalsIgnoreCase(securityRule.getProtocol())) {
                    securityGroupData.setIcmpType(securityRule.getStartPort());
                    securityGroupData.setIcmpCode(securityRule.getEndPort());
                } else {
                    securityGroupData.setStartPort(securityRule.getStartPort());
                    securityGroupData.setEndPort(securityRule.getEndPort());
                }

                final Long allowedSecurityGroupId = securityRule.getAllowedNetworkId();
                if (allowedSecurityGroupId != null) {
                    final List<SecurityGroupJoinVO> sgs = ApiDBUtils.findSecurityGroupViewById(allowedSecurityGroupId);
                    if (sgs != null && sgs.size() > 0) {
                        final SecurityGroupJoinVO sg = sgs.get(0);
                        securityGroupData.setSecurityGroupName(sg.getName());
                        securityGroupData.setAccountName(sg.getAccountName());
                    }
                } else {
                    securityGroupData.setCidr(securityRule.getAllowedSourceIpCidr());
                }
                if (securityRule.getRuleType() == SecurityRuleType.IngressRule) {
                    securityGroupData.setObjectName("ingressrule");
                    response.addSecurityGroupIngressRule(securityGroupData);
                } else {
                    securityGroupData.setObjectName("egressrule");
                    response.addSecurityGroupEgressRule(securityGroupData);
                }
            }
            response.setObjectName("securitygroup");
        }
        return response;
    }

    @Override
    public SecurityGroupResponse createSecurityGroupResponse(final SecurityGroup group) {
        final List<SecurityGroupJoinVO> viewSgs = ApiDBUtils.newSecurityGroupView(group);
        final List<SecurityGroupResponse> listSgs = ViewResponseHelper.createSecurityGroupResponses(viewSgs);
        assert listSgs != null && listSgs.size() == 1 : "There should be one security group returned";
        return listSgs.get(0);
    }

    @Override
    public ExtractResponse createExtractResponse(final Long uploadId, final Long id, final Long zoneId, final Long accountId, final String mode, final String url) {

        final ExtractResponse response = new ExtractResponse();
        response.setObjectName("template");
        final VMTemplateVO template = ApiDBUtils.findTemplateById(id);
        response.setId(template.getUuid());
        response.setName(template.getName());
        if (zoneId != null) {
            final DataCenter zone = ApiDBUtils.findZoneById(zoneId);
            response.setZoneId(zone.getUuid());
            response.setZoneName(zone.getName());
        }
        response.setMode(mode);
        if (uploadId == null) {
            // region-wide image store
            response.setUrl(url);
            response.setState(Upload.Status.DOWNLOAD_URL_CREATED.toString());
        } else {
            final UploadVO uploadInfo = ApiDBUtils.findUploadById(uploadId);
            response.setUploadId(uploadInfo.getUuid());
            response.setState(uploadInfo.getUploadState().toString());
            response.setUrl(uploadInfo.getUploadUrl());
        }
        final Account account = ApiDBUtils.findAccountById(accountId);
        response.setAccountId(account.getUuid());

        return response;
    }

    //TODO: we need to deprecate uploadVO, since extract is done in a synchronous fashion
    @Override
    public ExtractResponse createExtractResponse(final Long id, final Long zoneId, final Long accountId, final String mode, final String url) {

        final ExtractResponse response = new ExtractResponse();
        response.setObjectName("template");
        final VMTemplateVO template = ApiDBUtils.findTemplateById(id);
        response.setId(template.getUuid());
        response.setName(template.getName());
        if (zoneId != null) {
            final DataCenter zone = ApiDBUtils.findZoneById(zoneId);
            response.setZoneId(zone.getUuid());
            response.setZoneName(zone.getName());
        }
        response.setMode(mode);
        response.setUrl(url);
        response.setState(Upload.Status.DOWNLOAD_URL_CREATED.toString());
        final Account account = ApiDBUtils.findAccountById(accountId);
        response.setAccountId(account.getUuid());

        return response;
    }

    @Override
    public String toSerializedString(final CreateCmdResponse response, final String responseType) {
        return ApiResponseSerializer.toSerializedString(response, responseType);
    }

    @Override
    public EventResponse createEventResponse(final Event event) {
        final EventJoinVO vEvent = ApiDBUtils.newEventView(event);
        return ApiDBUtils.newEventResponse(vEvent);
    }

    @Override
    public TemplateResponse createTemplateUpdateResponse(final ResponseView view, final VirtualMachineTemplate result) {
        final List<TemplateJoinVO> tvo = ApiDBUtils.newTemplateView(result);
        final List<TemplateResponse> listVrs = ViewResponseHelper.createTemplateUpdateResponse(view, tvo.toArray(new TemplateJoinVO[tvo.size()]));
        assert listVrs != null && listVrs.size() == 1 : "There should be one template returned";
        return listVrs.get(0);
    }

    @Override
    public List<TemplateResponse> createTemplateResponses(final ResponseView view, final VirtualMachineTemplate result, final Long zoneId, final boolean readyOnly) {
        List<TemplateJoinVO> tvo = null;
        if (zoneId == null || zoneId == -1 || result.isCrossZones()) {
            tvo = ApiDBUtils.newTemplateView(result);
        } else {
            tvo = ApiDBUtils.newTemplateView(result, zoneId, readyOnly);
        }
        return ViewResponseHelper.createTemplateResponse(view, tvo.toArray(new TemplateJoinVO[tvo.size()]));
    }

    @Override
    public List<CapacityResponse> createCapacityResponse(final List<? extends Capacity> result, final DecimalFormat format) {
        final List<CapacityResponse> capacityResponses = new ArrayList<>();

        for (final Capacity summedCapacity : result) {
            final CapacityResponse capacityResponse = new CapacityResponse();
            capacityResponse.setCapacityTotal(summedCapacity.getTotalCapacity());
            capacityResponse.setCapacityType(summedCapacity.getCapacityType());
            capacityResponse.setCapacityUsed(summedCapacity.getUsedCapacity());
            if (summedCapacity.getPodId() != null) {
                capacityResponse.setPodId(ApiDBUtils.findPodById(summedCapacity.getPodId()).getUuid());
                final HostPodVO pod = ApiDBUtils.findPodById(summedCapacity.getPodId());
                if (pod != null) {
                    capacityResponse.setPodId(pod.getUuid());
                    capacityResponse.setPodName(pod.getName());
                }
            }
            if (summedCapacity.getClusterId() != null) {
                final ClusterVO cluster = ApiDBUtils.findClusterById(summedCapacity.getClusterId());
                if (cluster != null) {
                    capacityResponse.setClusterId(cluster.getUuid());
                    capacityResponse.setClusterName(cluster.getName());
                    if (summedCapacity.getPodId() == null) {
                        final HostPodVO pod = ApiDBUtils.findPodById(cluster.getPodId());
                        capacityResponse.setPodId(pod.getUuid());
                        capacityResponse.setPodName(pod.getName());
                    }
                }
            }
            final DataCenter zone = ApiDBUtils.findZoneById(summedCapacity.getDataCenterId());
            if (zone != null) {
                capacityResponse.setZoneId(zone.getUuid());
                capacityResponse.setZoneName(zone.getName());
            }
            if (summedCapacity.getUsedPercentage() != null) {
                capacityResponse.setPercentUsed(format.format(summedCapacity.getUsedPercentage() * 100f));
            } else if (summedCapacity.getTotalCapacity() != 0) {
                capacityResponse.setPercentUsed(format.format((float) summedCapacity.getUsedCapacity() / (float) summedCapacity.getTotalCapacity() * 100f));
            } else {
                capacityResponse.setPercentUsed(format.format(0L));
            }

            capacityResponse.setObjectName("capacity");
            capacityResponses.add(capacityResponse);
        }

        final List<VgpuTypesInfo> gpuCapacities;
        if (result.size() > 1 && (gpuCapacities = ApiDBUtils.getGpuCapacites(result.get(0).getDataCenterId(), result.get(0).getPodId(), result.get(0).getClusterId())) != null) {
            final HashMap<String, Long> vgpuVMs = ApiDBUtils.getVgpuVmsCount(result.get(0).getDataCenterId(), result.get(0).getPodId(), result.get(0).getClusterId());

            float capacityUsed = 0;
            long capacityMax = 0;
            for (final VgpuTypesInfo capacity : gpuCapacities) {
                if (vgpuVMs.containsKey(capacity.getGroupName().concat(capacity.getModelName()))) {
                    capacityUsed += (float) vgpuVMs.get(capacity.getGroupName().concat(capacity.getModelName())) / capacity.getMaxVpuPerGpu();
                }
                if (capacity.getModelName().equals(GPU.GPUType.passthrough.toString())) {
                    capacityMax += capacity.getMaxCapacity();
                }
            }

            final DataCenter zone = ApiDBUtils.findZoneById(result.get(0).getDataCenterId());
            final CapacityResponse capacityResponse = new CapacityResponse();
            if (zone != null) {
                capacityResponse.setZoneId(zone.getUuid());
                capacityResponse.setZoneName(zone.getName());
            }
            if (result.get(0).getPodId() != null) {
                final HostPodVO pod = ApiDBUtils.findPodById(result.get(0).getPodId());
                capacityResponse.setPodId(pod.getUuid());
                capacityResponse.setPodName(pod.getName());
            }
            if (result.get(0).getClusterId() != null) {
                final ClusterVO cluster = ApiDBUtils.findClusterById(result.get(0).getClusterId());
                capacityResponse.setClusterId(cluster.getUuid());
                capacityResponse.setClusterName(cluster.getName());
            }
            capacityResponse.setCapacityType(Capacity.CAPACITY_TYPE_GPU);
            capacityResponse.setCapacityUsed((long) Math.ceil(capacityUsed));
            capacityResponse.setCapacityTotal(capacityMax);
            if (capacityMax > 0) {
                capacityResponse.setPercentUsed(format.format(capacityUsed / capacityMax * 100f));
            } else {
                capacityResponse.setPercentUsed(format.format(0));
            }
            capacityResponse.setObjectName("capacity");
            capacityResponses.add(capacityResponse);
        }
        return capacityResponses;
    }

    @Override
    public TemplatePermissionsResponse createTemplatePermissionsResponse(final ResponseView view, final List<String> accountNames, final Long id) {
        Long templateOwnerDomain = null;
        final VirtualMachineTemplate template = ApiDBUtils.findTemplateById(id);
        final Account templateOwner = ApiDBUtils.findAccountById(template.getAccountId());
        if (view == ResponseView.Full) {
            // FIXME: we have just template id and need to get template owner
            // from that
            if (templateOwner != null) {
                templateOwnerDomain = templateOwner.getDomainId();
            }
        }

        final TemplatePermissionsResponse response = new TemplatePermissionsResponse();
        response.setId(template.getUuid());
        response.setPublicTemplate(template.isPublicTemplate());
        if (view == ResponseView.Full && templateOwnerDomain != null) {
            final Domain domain = ApiDBUtils.findDomainById(templateOwnerDomain);
            if (domain != null) {
                response.setDomainId(domain.getUuid());
            }
        }

        // Set accounts
        final List<String> projectIds = new ArrayList<>();
        final List<String> regularAccounts = new ArrayList<>();
        for (final String accountName : accountNames) {
            final Account account = ApiDBUtils.findAccountByNameDomain(accountName, templateOwner.getDomainId());
            if (account.getType() != Account.ACCOUNT_TYPE_PROJECT) {
                regularAccounts.add(accountName);
            } else {
                // convert account to projectIds
                final Project project = ApiDBUtils.findProjectByProjectAccountId(account.getId());

                if (project.getUuid() != null && !project.getUuid().isEmpty()) {
                    projectIds.add(project.getUuid());
                } else {
                    projectIds.add(String.valueOf(project.getId()));
                }
            }
        }

        if (!projectIds.isEmpty()) {
            response.setProjectIds(projectIds);
        }

        if (!regularAccounts.isEmpty()) {
            response.setAccountNames(regularAccounts);
        }

        response.setObjectName("templatepermission");
        return response;
    }

    @Override
    public AsyncJobResponse queryJobResult(final QueryAsyncJobResultCmd cmd) {
        final Account caller = CallContext.current().getCallingAccount();

        final AsyncJob job = _entityMgr.findById(AsyncJob.class, cmd.getId());
        if (job == null) {
            throw new InvalidParameterValueException("Unable to find a job by id " + cmd.getId());
        }

        final User userJobOwner = _accountMgr.getUserIncludingRemoved(job.getUserId());
        final Account jobOwner = _accountMgr.getAccount(userJobOwner.getAccountId());

        //check permissions
        if (_accountMgr.isNormalUser(caller.getId())) {
            //regular user can see only jobs he owns
            if (caller.getId() != jobOwner.getId()) {
                throw new PermissionDeniedException("Account " + caller + " is not authorized to see job id=" + job.getId());
            }
        } else if (_accountMgr.isDomainAdmin(caller.getId())) {
            _accountMgr.checkAccess(caller, null, true, jobOwner);
        }

        return createAsyncJobResponse(_jobMgr.queryJob(cmd.getId(), true));
    }

    public AsyncJobResponse createAsyncJobResponse(final AsyncJob job) {
        final AsyncJobJoinVO vJob = ApiDBUtils.newAsyncJobView(job);
        return ApiDBUtils.newAsyncJobResponse(vJob);
    }

    @Override
    public NetworkOfferingResponse createNetworkOfferingResponse(final NetworkOffering offering) {
        final NetworkOfferingResponse response = new NetworkOfferingResponse();
        response.setId(offering.getUuid());
        response.setName(offering.getName());
        response.setDisplayText(offering.getDisplayText());
        response.setTags(offering.getTags());
        response.setTrafficType(offering.getTrafficType().toString());
        response.setIsDefault(offering.isDefault());
        response.setSpecifyVlan(offering.getSpecifyVlan());
        response.setConserveMode(offering.isConserveMode());
        response.setSpecifyIpRanges(offering.getSpecifyIpRanges());
        response.setAvailability(offering.getAvailability().toString());
        response.setIsPersistent(offering.getIsPersistent());
        response.setNetworkRate(ApiDBUtils.getNetworkRate(offering.getId()));
        response.setEgressDefaultPolicy(offering.getEgressDefaultPolicy());
        response.setConcurrentConnections(offering.getConcurrentConnections());
        response.setSupportsStrechedL2Subnet(offering.getSupportsStrechedL2());
        Long so = null;
        if (offering.getServiceOfferingId() != null) {
            so = offering.getServiceOfferingId();
        } else {
            so = ApiDBUtils.findDefaultRouterServiceOffering();
        }
        if (so != null) {
            final ServiceOffering soffering = ApiDBUtils.findServiceOfferingById(so);
            if (soffering != null) {
                response.setServiceOfferingId(soffering.getUuid());
            }
        }

        if (offering.getGuestType() != null) {
            response.setGuestIpType(offering.getGuestType().toString());
        }

        response.setState(offering.getState().name());

        final Map<Service, Set<Provider>> serviceProviderMap = ApiDBUtils.listNetworkOfferingServices(offering.getId());
        final List<ServiceResponse> serviceResponses = new ArrayList<>();
        for (final Map.Entry<Service, Set<Provider>> entry : serviceProviderMap.entrySet()) {
            final Service service = entry.getKey();
            final Set<Provider> srvc_providers = entry.getValue();
            final ServiceResponse svcRsp = new ServiceResponse();
            // skip gateway service
            if (service == Service.Gateway) {
                continue;
            }
            svcRsp.setName(service.getName());
            final List<ProviderResponse> providers = new ArrayList<>();
            for (final Provider provider : srvc_providers) {
                if (provider != null) {
                    final ProviderResponse providerRsp = new ProviderResponse();
                    providerRsp.setName(provider.getName());
                    providers.add(providerRsp);
                }
            }
            svcRsp.setProviders(providers);

            if (Service.Lb == service) {
                final List<CapabilityResponse> lbCapResponse = new ArrayList<>();

                final CapabilityResponse lbIsoaltion = new CapabilityResponse();
                lbIsoaltion.setName(Capability.SupportedLBIsolation.getName());
                lbIsoaltion.setValue(offering.getDedicatedLB() ? "dedicated" : "shared");
                lbCapResponse.add(lbIsoaltion);

                final CapabilityResponse eLb = new CapabilityResponse();
                eLb.setName(Capability.ElasticLb.getName());
                eLb.setValue(offering.getElasticLb() ? "true" : "false");
                lbCapResponse.add(eLb);

                final CapabilityResponse inline = new CapabilityResponse();
                inline.setName(Capability.InlineMode.getName());
                inline.setValue(offering.isInline() ? "true" : "false");
                lbCapResponse.add(inline);

                svcRsp.setCapabilities(lbCapResponse);
            } else if (Service.SourceNat == service) {
                final List<CapabilityResponse> capabilities = new ArrayList<>();
                final CapabilityResponse sharedSourceNat = new CapabilityResponse();
                sharedSourceNat.setName(Capability.SupportedSourceNatTypes.getName());
                sharedSourceNat.setValue(offering.getSharedSourceNat() ? "perzone" : "peraccount");
                capabilities.add(sharedSourceNat);

                final CapabilityResponse redundantRouter = new CapabilityResponse();
                redundantRouter.setName(Capability.RedundantRouter.getName());
                redundantRouter.setValue(offering.getRedundantRouter() ? "true" : "false");
                capabilities.add(redundantRouter);

                svcRsp.setCapabilities(capabilities);
            } else if (service == Service.StaticNat) {
                final List<CapabilityResponse> staticNatCapResponse = new ArrayList<>();

                final CapabilityResponse eIp = new CapabilityResponse();
                eIp.setName(Capability.ElasticIp.getName());
                eIp.setValue(offering.getElasticIp() ? "true" : "false");
                staticNatCapResponse.add(eIp);

                final CapabilityResponse associatePublicIp = new CapabilityResponse();
                associatePublicIp.setName(Capability.AssociatePublicIP.getName());
                associatePublicIp.setValue(offering.getAssociatePublicIP() ? "true" : "false");
                staticNatCapResponse.add(associatePublicIp);

                svcRsp.setCapabilities(staticNatCapResponse);
            }

            serviceResponses.add(svcRsp);
        }
        response.setForVpc(_configMgr.isOfferingForVpc(offering));

        response.setServices(serviceResponses);

        //set network offering details
        final Map<Detail, String> details = _ntwkModel.getNtwkOffDetails(offering.getId());
        if (details != null && !details.isEmpty()) {
            response.setDetails(details);
        }

        response.setObjectName("networkoffering");
        return response;
    }

    @Override
    public NetworkResponse createNetworkResponse(final ResponseView view, final Network network) {
        // need to get network profile in order to retrieve dns information from
        // there
        final NetworkProfile profile = ApiDBUtils.getNetworkProfile(network.getId());
        final NetworkResponse response = new NetworkResponse();
        response.setId(network.getUuid());
        response.setName(network.getName());
        response.setDisplaytext(network.getDisplayText());
        if (network.getBroadcastDomainType() != null) {
            response.setBroadcastDomainType(network.getBroadcastDomainType().toString());
        }

        if (network.getTrafficType() != null) {
            response.setTrafficType(network.getTrafficType().name());
        }

        if (network.getGuestType() != null) {
            response.setType(network.getGuestType().toString());
        }

        response.setGateway(network.getGateway());

        // FIXME - either set netmask or cidr
        response.setCidr(network.getCidr());
        response.setNetworkCidr(network.getNetworkCidr());
        // If network has reservation its entire network cidr is defined by
        // getNetworkCidr()
        // if no reservation is present then getCidr() will define the entire
        // network cidr
        if (network.getNetworkCidr() != null) {
            response.setNetmask(NetUtils.cidr2Netmask(network.getNetworkCidr()));
        }
        if (network.getCidr() != null && network.getNetworkCidr() == null) {
            response.setNetmask(NetUtils.cidr2Netmask(network.getCidr()));
        }

        response.setIp6Gateway(network.getIp6Gateway());
        response.setIp6Cidr(network.getIp6Cidr());

        // create response for reserved IP ranges that can be used for
        // non-cloudstack purposes
        String reservation = null;
        if (network.getCidr() != null && NetUtils.isNetworkAWithinNetworkB(network.getCidr(), network.getNetworkCidr())) {
            final String[] guestVmCidrPair = network.getCidr().split("\\/");
            final String[] guestCidrPair = network.getNetworkCidr().split("\\/");

            final Long guestVmCidrSize = Long.valueOf(guestVmCidrPair[1]);
            final Long guestCidrSize = Long.valueOf(guestCidrPair[1]);

            final String[] guestVmIpRange = NetUtils.getIpRangeFromCidr(guestVmCidrPair[0], guestVmCidrSize);
            final String[] guestIpRange = NetUtils.getIpRangeFromCidr(guestCidrPair[0], guestCidrSize);
            final long startGuestIp = NetUtils.ip2Long(guestIpRange[0]);
            final long endGuestIp = NetUtils.ip2Long(guestIpRange[1]);
            final long startVmIp = NetUtils.ip2Long(guestVmIpRange[0]);
            final long endVmIp = NetUtils.ip2Long(guestVmIpRange[1]);

            if (startVmIp == startGuestIp && endVmIp < endGuestIp - 1) {
                reservation = NetUtils.long2Ip(endVmIp + 1) + "-" + NetUtils.long2Ip(endGuestIp);
            }
            if (endVmIp == endGuestIp && startVmIp > startGuestIp + 1) {
                reservation = NetUtils.long2Ip(startGuestIp) + "-" + NetUtils.long2Ip(startVmIp - 1);
            }
            if (startVmIp > startGuestIp + 1 && endVmIp < endGuestIp - 1) {
                reservation = NetUtils.long2Ip(startGuestIp) + "-" + NetUtils.long2Ip(startVmIp - 1) + " ,  " + NetUtils.long2Ip(endVmIp + 1) + "-" + NetUtils.long2Ip(endGuestIp);
            }
        }
        response.setReservedIpRange(reservation);

        // return vlan information only to Root admin
        if (network.getBroadcastUri() != null && view == ResponseView.Full) {
            final String broadcastUri = network.getBroadcastUri().toString();
            response.setBroadcastUri(broadcastUri);
            String vlan = "N/A";
            switch (BroadcastDomainType.getSchemeValue(network.getBroadcastUri())) {
                case Vlan:
                case Vxlan:
                    vlan = BroadcastDomainType.getValue(network.getBroadcastUri());
                    break;
            }
            // return vlan information only to Root admin
            response.setVlan(vlan);
        }

        final DataCenter zone = ApiDBUtils.findZoneById(network.getDataCenterId());
        if (zone != null) {
            response.setZoneId(zone.getUuid());
            response.setZoneName(zone.getName());
        }
        if (network.getPhysicalNetworkId() != null) {
            final PhysicalNetworkVO pnet = ApiDBUtils.findPhysicalNetworkById(network.getPhysicalNetworkId());
            response.setPhysicalNetworkId(pnet.getUuid());
        }

        // populate network offering information
        final NetworkOffering networkOffering = ApiDBUtils.findNetworkOfferingById(network.getNetworkOfferingId());
        if (networkOffering != null) {
            response.setNetworkOfferingId(networkOffering.getUuid());
            response.setNetworkOfferingName(networkOffering.getName());
            response.setNetworkOfferingDisplayText(networkOffering.getDisplayText());
            response.setNetworkOfferingConserveMode(networkOffering.isConserveMode());
            response.setIsSystem(networkOffering.isSystemOnly());
            response.setNetworkOfferingAvailability(networkOffering.getAvailability().toString());
            response.setIsPersistent(networkOffering.getIsPersistent());
        }

        if (network.getAclType() != null) {
            response.setAclType(network.getAclType().toString());
        }
        response.setDisplayNetwork(network.getDisplayNetwork());
        response.setState(network.getState().toString());
        response.setRestartRequired(network.isRestartRequired());
        final NetworkVO nw = ApiDBUtils.findNetworkById(network.getRelated());
        if (nw != null) {
            response.setRelated(nw.getUuid());
        }
        response.setNetworkDomain(network.getNetworkDomain());

        response.setDns1(profile.getDns1());
        response.setDns2(profile.getDns2());
        // populate capability
        final Map<Service, Map<Capability, String>> serviceCapabilitiesMap = ApiDBUtils.getNetworkCapabilities(network.getId(), network.getDataCenterId());
        final List<ServiceResponse> serviceResponses = new ArrayList<>();
        if (serviceCapabilitiesMap != null) {
            for (final Map.Entry<Service, Map<Capability, String>> entry : serviceCapabilitiesMap.entrySet()) {
                final Service service = entry.getKey();
                final ServiceResponse serviceResponse = new ServiceResponse();
                // skip gateway service
                if (service == Service.Gateway) {
                    continue;
                }
                serviceResponse.setName(service.getName());

                // set list of capabilities for the service
                final List<CapabilityResponse> capabilityResponses = new ArrayList<>();
                final Map<Capability, String> serviceCapabilities = entry.getValue();
                if (serviceCapabilities != null) {
                    for (final Map.Entry<Capability, String> ser_cap_entries : serviceCapabilities.entrySet()) {
                        final Capability capability = ser_cap_entries.getKey();
                        final CapabilityResponse capabilityResponse = new CapabilityResponse();
                        final String capabilityValue = ser_cap_entries.getValue();
                        capabilityResponse.setName(capability.getName());
                        capabilityResponse.setValue(capabilityValue);
                        capabilityResponse.setObjectName("capability");
                        capabilityResponses.add(capabilityResponse);
                    }
                    serviceResponse.setCapabilities(capabilityResponses);
                }

                serviceResponse.setObjectName("service");
                serviceResponses.add(serviceResponse);
            }
        }
        response.setServices(serviceResponses);

        if (network.getAclType() == null || network.getAclType() == ACLType.Account) {
            populateOwner(response, network);
        } else {
            // get domain from network_domain table
            final Pair<Long, Boolean> domainNetworkDetails = ApiDBUtils.getDomainNetworkDetails(network.getId());
            if (domainNetworkDetails.first() != null) {
                final Domain domain = ApiDBUtils.findDomainById(domainNetworkDetails.first());
                if (domain != null) {
                    response.setDomainId(domain.getUuid());
                }
            }
            response.setSubdomainAccess(domainNetworkDetails.second());
        }

        final Long dedicatedDomainId = ApiDBUtils.getDedicatedNetworkDomain(network.getId());
        if (dedicatedDomainId != null) {
            final Domain domain = ApiDBUtils.findDomainById(dedicatedDomainId);
            if (domain != null) {
                response.setDomainId(domain.getUuid());
                response.setDomainName(domain.getName());
            }
        }

        response.setSpecifyIpRanges(network.getSpecifyIpRanges());
        if (network.getVpcId() != null) {
            final Vpc vpc = ApiDBUtils.findVpcById(network.getVpcId());
            if (vpc != null) {
                response.setVpcId(vpc.getUuid());
            }
        }
        response.setCanUseForDeploy(ApiDBUtils.canUseForDeploy(network));

        // set tag information
        final List<? extends ResourceTag> tags = ApiDBUtils.listByResourceTypeAndId(ResourceObjectType.Network, network.getId());
        final List<ResourceTagResponse> tagResponses = new ArrayList<>();
        for (final ResourceTag tag : tags) {
            final ResourceTagResponse tagResponse = createResourceTagResponse(tag, true);
            CollectionUtils.addIgnoreNull(tagResponses, tagResponse);
        }
        response.setTags(tagResponses);

        if (network.getNetworkACLId() != null) {
            final NetworkACL acl = ApiDBUtils.findByNetworkACLId(network.getNetworkACLId());
            if (acl != null) {
                response.setAclId(acl.getUuid());
            }
        }

        response.setStrechedL2Subnet(network.isStrechedL2Network());
        if (network.isStrechedL2Network()) {
            final Set<String> networkSpannedZones = new HashSet<>();
            final List<VMInstanceVO> vmInstances = new ArrayList<>();
            vmInstances.addAll(ApiDBUtils.listUserVMsByNetworkId(network.getId()));
            vmInstances.addAll(ApiDBUtils.listDomainRoutersByNetworkId(network.getId()));
            for (final VirtualMachine vm : vmInstances) {
                final DataCenter vmZone = ApiDBUtils.findZoneById(vm.getDataCenterId());
                networkSpannedZones.add(vmZone.getUuid());
            }
            response.setNetworkSpannedZones(networkSpannedZones);
        }
        response.setObjectName("network");
        return response;
    }

    @Override
    public UserResponse createUserResponse(final User user) {
        final UserAccountJoinVO vUser = ApiDBUtils.newUserView(user);
        return ApiDBUtils.newUserResponse(vUser);
    }

    // this method is used for response generation via createAccount (which
    // creates an account + user)
    @Override
    public AccountResponse createUserAccountResponse(final ResponseView view, final UserAccount user) {
        return ApiDBUtils.newAccountResponse(view, ApiDBUtils.findAccountViewById(user.getAccountId()));
    }

    @Override
    public Long getSecurityGroupId(final String groupName, final long accountId) {
        final SecurityGroup sg = ApiDBUtils.getSecurityGroup(groupName, accountId);
        if (sg == null) {
            return null;
        } else {
            return sg.getId();
        }
    }

    @Override
    public List<TemplateResponse> createIsoResponses(final ResponseView view, final VirtualMachineTemplate result, final Long zoneId, final boolean readyOnly) {
        List<TemplateJoinVO> tvo = null;
        if (zoneId == null || zoneId == -1) {
            tvo = ApiDBUtils.newTemplateView(result);
        } else {
            tvo = ApiDBUtils.newTemplateView(result, zoneId, readyOnly);
        }

        return ViewResponseHelper.createIsoResponse(view, tvo.toArray(new TemplateJoinVO[tvo.size()]));
    }

    @Override
    public ProjectResponse createProjectResponse(final Project project) {
        final List<ProjectJoinVO> viewPrjs = ApiDBUtils.newProjectView(project);
        final List<ProjectResponse> listPrjs = ViewResponseHelper.createProjectResponse(viewPrjs.toArray(new ProjectJoinVO[viewPrjs.size()]));
        assert listPrjs != null && listPrjs.size() == 1 : "There should be one project  returned";
        return listPrjs.get(0);
    }

    @Override
    public List<TemplateResponse> createTemplateResponses(final ResponseView view, final long templateId, final Long vmId) {
        final UserVm vm = findUserVmById(vmId);
        final Long hostId = vm.getHostId() == null ? vm.getLastHostId() : vm.getHostId();
        final Host host = findHostById(hostId);
        return createTemplateResponses(view, templateId, host.getDataCenterId(), true);
    }

    @Override
    public FirewallResponse createFirewallResponse(final FirewallRule fwRule) {
        final FirewallResponse response = new FirewallResponse();

        response.setId(fwRule.getUuid());
        response.setProtocol(fwRule.getProtocol());
        if (fwRule.getSourcePortStart() != null) {
            response.setStartPort(fwRule.getSourcePortStart());
        }

        if (fwRule.getSourcePortEnd() != null) {
            response.setEndPort(fwRule.getSourcePortEnd());
        }

        final List<String> cidrs = ApiDBUtils.findFirewallSourceCidrs(fwRule.getId());
        response.setCidrList(StringUtils.join(cidrs, ","));

        if (fwRule.getTrafficType() == FirewallRule.TrafficType.Ingress) {
            final IpAddress ip = ApiDBUtils.findIpAddressById(fwRule.getSourceIpAddressId());
            response.setPublicIpAddressId(ip.getUuid());
            response.setPublicIpAddress(ip.getAddress().addr());
        }

        final Network network = ApiDBUtils.findNetworkById(fwRule.getNetworkId());
        response.setNetworkId(network.getUuid());

        final FirewallRule.State state = fwRule.getState();
        String stateToSet = state.toString();
        if (state.equals(FirewallRule.State.Revoke)) {
            stateToSet = "Deleting";
        }

        response.setIcmpCode(fwRule.getIcmpCode());
        response.setIcmpType(fwRule.getIcmpType());
        response.setForDisplay(fwRule.isDisplay());

        // set tag information
        final List<? extends ResourceTag> tags = ApiDBUtils.listByResourceTypeAndId(ResourceObjectType.FirewallRule, fwRule.getId());
        final List<ResourceTagResponse> tagResponses = new ArrayList<>();
        for (final ResourceTag tag : tags) {
            final ResourceTagResponse tagResponse = createResourceTagResponse(tag, true);
            CollectionUtils.addIgnoreNull(tagResponses, tagResponse);
        }
        response.setTags(tagResponses);

        response.setState(stateToSet);
        response.setObjectName("firewallrule");
        return response;
    }

    @Override
    public HypervisorCapabilitiesResponse createHypervisorCapabilitiesResponse(final HypervisorCapabilities hpvCapabilities) {
        final HypervisorCapabilitiesResponse hpvCapabilitiesResponse = new HypervisorCapabilitiesResponse();
        hpvCapabilitiesResponse.setId(hpvCapabilities.getUuid());
        hpvCapabilitiesResponse.setHypervisor(hpvCapabilities.getHypervisorType());
        hpvCapabilitiesResponse.setHypervisorVersion(hpvCapabilities.getHypervisorVersion());
        hpvCapabilitiesResponse.setIsSecurityGroupEnabled(hpvCapabilities.isSecurityGroupEnabled());
        hpvCapabilitiesResponse.setMaxGuestsLimit(hpvCapabilities.getMaxGuestsLimit());
        hpvCapabilitiesResponse.setMaxDataVolumesLimit(hpvCapabilities.getMaxDataVolumesLimit());
        hpvCapabilitiesResponse.setMaxHostsPerCluster(hpvCapabilities.getMaxHostsPerCluster());
        hpvCapabilitiesResponse.setIsStorageMotionSupported(hpvCapabilities.isStorageMotionSupported());
        return hpvCapabilitiesResponse;
    }

    @Override
    public ProjectAccountResponse createProjectAccountResponse(final ProjectAccount projectAccount) {
        final ProjectAccountJoinVO vProj = ApiDBUtils.newProjectAccountView(projectAccount);
        final List<ProjectAccountResponse> listProjs = ViewResponseHelper.createProjectAccountResponse(vProj);
        assert listProjs != null && listProjs.size() == 1 : "There should be one project account returned";
        return listProjs.get(0);
    }

    @Override
    public ProjectInvitationResponse createProjectInvitationResponse(final ProjectInvitation invite) {
        final ProjectInvitationJoinVO vInvite = ApiDBUtils.newProjectInvitationView(invite);
        return ApiDBUtils.newProjectInvitationResponse(vInvite);
    }

    @Override
    public SystemVmInstanceResponse createSystemVmInstanceResponse(final VirtualMachine vm) {
        final SystemVmInstanceResponse vmResponse = new SystemVmInstanceResponse();
        vmResponse.setId(vm.getUuid());
        vmResponse.setSystemVmType(vm.getType().toString().toLowerCase());
        vmResponse.setName(vm.getHostName());
        if (vm.getHostId() != null) {
            final Host host = ApiDBUtils.findHostById(vm.getHostId());
            if (host != null) {
                vmResponse.setHostId(host.getUuid());
            }
        }
        if (vm.getState() != null) {
            vmResponse.setState(vm.getState().toString());
        }
        if (vm.getType() == Type.DomainRouter) {
            final VirtualRouter router = (VirtualRouter) vm;
            if (router.getRole() != null) {
                vmResponse.setRole(router.getRole().toString());
            }
        }
        vmResponse.setObjectName("systemvminstance");
        return vmResponse;
    }

    @Override
    public PhysicalNetworkResponse createPhysicalNetworkResponse(final PhysicalNetwork result) {
        final PhysicalNetworkResponse response = new PhysicalNetworkResponse();

        final DataCenter zone = ApiDBUtils.findZoneById(result.getDataCenterId());
        if (zone != null) {
            response.setZoneId(zone.getUuid());
        }
        response.setNetworkSpeed(result.getSpeed());
        response.setVlan(result.getVnetString());
        if (result.getDomainId() != null) {
            final Domain domain = ApiDBUtils.findDomainById(result.getDomainId());
            if (domain != null) {
                response.setDomainId(domain.getUuid());
            }
        }
        response.setId(result.getUuid());
        if (result.getBroadcastDomainRange() != null) {
            response.setBroadcastDomainRange(result.getBroadcastDomainRange().toString());
        }
        response.setIsolationMethods(result.getIsolationMethods());
        response.setTags(result.getTags());
        if (result.getState() != null) {
            response.setState(result.getState().toString());
        }

        response.setName(result.getName());

        response.setObjectName("physicalnetwork");
        return response;
    }

    @Override
    public ServiceResponse createNetworkServiceResponse(final Service service) {
        final ServiceResponse response = new ServiceResponse();
        response.setName(service.getName());

        // set list of capabilities required for the service
        final List<CapabilityResponse> capabilityResponses = new ArrayList<>();
        final Capability[] capabilities = service.getCapabilities();
        for (final Capability cap : capabilities) {
            final CapabilityResponse capabilityResponse = new CapabilityResponse();
            capabilityResponse.setName(cap.getName());
            capabilityResponse.setObjectName("capability");
            if (cap.getName().equals(Capability.SupportedLBIsolation.getName()) || cap.getName().equals(Capability.SupportedSourceNatTypes.getName())
                    || cap.getName().equals(Capability.RedundantRouter.getName())) {
                capabilityResponse.setCanChoose(true);
            } else {
                capabilityResponse.setCanChoose(false);
            }
            capabilityResponses.add(capabilityResponse);
        }
        response.setCapabilities(capabilityResponses);

        // set list of providers providing this service
        final List<? extends Network.Provider> serviceProviders = ApiDBUtils.getProvidersForService(service);
        final List<ProviderResponse> serviceProvidersResponses = new ArrayList<>();
        for (final Network.Provider serviceProvider : serviceProviders) {
            // return only Virtual Router/JuniperSRX/CiscoVnmc as a provider for the firewall
            if (service == Service.Firewall
                    && !(serviceProvider == Provider.VirtualRouter || serviceProvider == Provider.JuniperSRX || serviceProvider == Provider.CiscoVnmc || serviceProvider ==
                    Provider.NuageVsp)) {
                continue;
            }

            final ProviderResponse serviceProviderResponse = createServiceProviderResponse(serviceProvider);
            serviceProvidersResponses.add(serviceProviderResponse);
        }
        response.setProviders(serviceProvidersResponses);

        response.setObjectName("networkservice");
        return response;
    }

    private ProviderResponse createServiceProviderResponse(final Provider serviceProvider) {
        final ProviderResponse response = new ProviderResponse();
        response.setName(serviceProvider.getName());
        final boolean canEnableIndividualServices = ApiDBUtils.canElementEnableIndividualServices(serviceProvider);
        response.setCanEnableIndividualServices(canEnableIndividualServices);
        return response;
    }

    @Override
    public ProviderResponse createNetworkServiceProviderResponse(final PhysicalNetworkServiceProvider result) {
        final ProviderResponse response = new ProviderResponse();
        response.setId(result.getUuid());
        response.setName(result.getProviderName());
        final PhysicalNetwork pnw = ApiDBUtils.findPhysicalNetworkById(result.getPhysicalNetworkId());
        if (pnw != null) {
            response.setPhysicalNetworkId(pnw.getUuid());
        }
        final PhysicalNetwork dnw = ApiDBUtils.findPhysicalNetworkById(result.getDestinationPhysicalNetworkId());
        if (dnw != null) {
            response.setDestinationPhysicalNetworkId(dnw.getUuid());
        }
        response.setState(result.getState().toString());

        // set enabled services
        final List<String> services = new ArrayList<>();
        for (final Service service : result.getEnabledServices()) {
            services.add(service.getName());
        }
        response.setServices(services);

        final Provider serviceProvider = Provider.getProvider(result.getProviderName());
        final boolean canEnableIndividualServices = ApiDBUtils.canElementEnableIndividualServices(serviceProvider);
        response.setCanEnableIndividualServices(canEnableIndividualServices);

        response.setObjectName("networkserviceprovider");
        return response;
    }

    @Override
    public TrafficTypeResponse createTrafficTypeResponse(final PhysicalNetworkTrafficType result) {
        final TrafficTypeResponse response = new TrafficTypeResponse();
        response.setId(result.getUuid());
        final PhysicalNetwork pnet = ApiDBUtils.findPhysicalNetworkById(result.getPhysicalNetworkId());
        if (pnet != null) {
            response.setPhysicalNetworkId(pnet.getUuid());
        }
        if (result.getTrafficType() != null) {
            response.setTrafficType(result.getTrafficType().toString());
        }

        response.setXenLabel(result.getXenNetworkLabel());
        response.setKvmLabel(result.getKvmNetworkLabel());
        response.setOvm3Label(result.getOvm3NetworkLabel());

        response.setObjectName("traffictype");
        return response;
    }

    @Override
    public VirtualRouterProviderResponse createVirtualRouterProviderResponse(final VirtualRouterProvider result) {
        //generate only response of the VR/VPCVR provider type
        if (!(result.getType() == VirtualRouterProvider.Type.VirtualRouter || result.getType() == VirtualRouterProvider.Type.VPCVirtualRouter)) {
            return null;
        }
        final VirtualRouterProviderResponse response = new VirtualRouterProviderResponse();
        response.setId(result.getUuid());
        final PhysicalNetworkServiceProvider nsp = ApiDBUtils.findPhysicalNetworkServiceProviderById(result.getNspId());
        if (nsp != null) {
            response.setNspId(nsp.getUuid());
        }
        response.setEnabled(result.isEnabled());

        response.setObjectName("virtualrouterelement");
        return response;
    }

    @Override
    public StorageNetworkIpRangeResponse createStorageNetworkIpRangeResponse(final StorageNetworkIpRange result) {
        final StorageNetworkIpRangeResponse response = new StorageNetworkIpRangeResponse();
        response.setUuid(result.getUuid());
        response.setVlan(result.getVlan());
        response.setEndIp(result.getEndIp());
        response.setStartIp(result.getStartIp());
        response.setPodUuid(result.getPodUuid());
        response.setZoneUuid(result.getZoneUuid());
        response.setNetworkUuid(result.getNetworkUuid());
        response.setNetmask(result.getNetmask());
        response.setGateway(result.getGateway());
        response.setObjectName("storagenetworkiprange");
        return response;
    }

    @Override
    public RegionResponse createRegionResponse(final Region region) {
        final RegionResponse response = new RegionResponse();
        response.setId(region.getId());
        response.setName(region.getName());
        response.setEndPoint(region.getEndPoint());
        response.setObjectName("region");
        response.setGslbServiceEnabled(region.checkIfServiceEnabled(Region.Service.Gslb));
        response.setPortableipServiceEnabled(region.checkIfServiceEnabled(Region.Service.PortableIp));
        return response;
    }

    @Override
    public ImageStoreResponse createImageStoreResponse(final ImageStore os) {
        final List<ImageStoreJoinVO> viewStores = ApiDBUtils.newImageStoreView(os);
        final List<ImageStoreResponse> listStores = ViewResponseHelper.createImageStoreResponse(viewStores.toArray(new ImageStoreJoinVO[viewStores.size()]));
        assert listStores != null && listStores.size() == 1 : "There should be one image data store returned";
        return listStores.get(0);
    }

    @Override
    public ResourceTagResponse createResourceTagResponse(final ResourceTag resourceTag, final boolean keyValueOnly) {
        final ResourceTagJoinVO rto = ApiDBUtils.newResourceTagView(resourceTag);
        if (rto == null) {
            return null;
        }
        return ApiDBUtils.newResourceTagResponse(rto, keyValueOnly);
    }

    @Override
    public Site2SiteVpnGatewayResponse createSite2SiteVpnGatewayResponse(final Site2SiteVpnGateway result) {
        final Site2SiteVpnGatewayResponse response = new Site2SiteVpnGatewayResponse();
        response.setId(result.getUuid());
        response.setIp(ApiDBUtils.findIpAddressById(result.getAddrId()).getAddress().toString());
        final Vpc vpc = ApiDBUtils.findVpcById(result.getVpcId());
        if (vpc != null) {
            response.setVpcId(vpc.getUuid());
        }
        response.setRemoved(result.getRemoved());
        response.setForDisplay(result.isDisplay());
        response.setObjectName("vpngateway");

        populateAccount(response, result.getAccountId());
        populateDomain(response, result.getDomainId());
        return response;
    }

    @Override
    public VpcOfferingResponse createVpcOfferingResponse(final VpcOffering offering) {
        final VpcOfferingResponse response = new VpcOfferingResponse();
        response.setId(offering.getUuid());
        response.setName(offering.getName());
        response.setDisplayText(offering.getDisplayText());
        response.setIsDefault(offering.isDefault());
        response.setState(offering.getState().name());
        response.setSupportsDistributedRouter(offering.supportsDistributedRouter());
        response.setSupportsRegionLevelVpc(offering.offersRegionLevelVPC());

        final Map<Service, Set<Provider>> serviceProviderMap = ApiDBUtils.listVpcOffServices(offering.getId());
        final List<ServiceResponse> serviceResponses = new ArrayList<>();
        for (final Map.Entry<Service, Set<Provider>> entry : serviceProviderMap.entrySet()) {
            final Service service = entry.getKey();
            final Set<Provider> srvc_providers = entry.getValue();

            final ServiceResponse svcRsp = new ServiceResponse();
            // skip gateway service
            if (service == Service.Gateway) {
                continue;
            }
            svcRsp.setName(service.getName());
            final List<ProviderResponse> providers = new ArrayList<>();
            for (final Provider provider : srvc_providers) {
                if (provider != null) {
                    final ProviderResponse providerRsp = new ProviderResponse();
                    providerRsp.setName(provider.getName());
                    providers.add(providerRsp);
                }
            }
            svcRsp.setProviders(providers);

            serviceResponses.add(svcRsp);
        }
        response.setServices(serviceResponses);
        response.setObjectName("vpcoffering");
        return response;
    }

    @Override
    public VpcResponse createVpcResponse(final ResponseView view, final Vpc vpc) {
        final VpcResponse response = new VpcResponse();
        response.setId(vpc.getUuid());
        response.setName(vpc.getName());
        response.setDisplayText(vpc.getDisplayText());
        response.setState(vpc.getState().name());
        final VpcOffering voff = ApiDBUtils.findVpcOfferingById(vpc.getVpcOfferingId());
        if (voff != null) {
            response.setVpcOfferingId(voff.getUuid());
        }
        response.setCidr(vpc.getCidr());
        response.setRestartRequired(vpc.isRestartRequired());
        response.setNetworkDomain(vpc.getNetworkDomain());
        response.setForDisplay(vpc.isDisplay());
        response.setUsesDistributedRouter(vpc.usesDistributedRouter());
        response.setRedundantRouter(vpc.isRedundant());
        response.setRegionLevelVpc(vpc.isRegionLevelVpc());

        final Map<Service, Set<Provider>> serviceProviderMap = ApiDBUtils.listVpcOffServices(vpc.getVpcOfferingId());
        final List<ServiceResponse> serviceResponses = new ArrayList<>();
        for (final Map.Entry<Service, Set<Provider>> entry : serviceProviderMap.entrySet()) {
            final Service service = entry.getKey();
            final Set<Provider> serviceProviders = entry.getValue();
            final ServiceResponse svcRsp = new ServiceResponse();
            // skip gateway service
            if (service == Service.Gateway) {
                continue;
            }
            svcRsp.setName(service.getName());
            final List<ProviderResponse> providers = new ArrayList<>();
            for (final Provider provider : serviceProviders) {
                if (provider != null) {
                    final ProviderResponse providerRsp = new ProviderResponse();
                    providerRsp.setName(provider.getName());
                    providers.add(providerRsp);
                }
            }
            svcRsp.setProviders(providers);

            serviceResponses.add(svcRsp);
        }

        final List<NetworkResponse> networkResponses = new ArrayList<>();
        final List<? extends Network> networks = ApiDBUtils.listVpcNetworks(vpc.getId());
        for (final Network network : networks) {
            final NetworkResponse ntwkRsp = createNetworkResponse(view, network);
            networkResponses.add(ntwkRsp);
        }

        final DataCenter zone = ApiDBUtils.findZoneById(vpc.getZoneId());
        if (zone != null) {
            response.setZoneId(zone.getUuid());
            response.setZoneName(zone.getName());
        }

        response.setNetworks(networkResponses);
        response.setServices(serviceResponses);
        populateOwner(response, vpc);

        // set tag information
        final List<? extends ResourceTag> tags = ApiDBUtils.listByResourceTypeAndId(ResourceObjectType.Vpc, vpc.getId());
        final List<ResourceTagResponse> tagResponses = new ArrayList<>();
        for (final ResourceTag tag : tags) {
            final ResourceTagResponse tagResponse = createResourceTagResponse(tag, true);
            CollectionUtils.addIgnoreNull(tagResponses, tagResponse);
        }
        response.setTags(tagResponses);
        response.setObjectName("vpc");
        return response;
    }

    @Override
    public NetworkACLItemResponse createNetworkACLItemResponse(final NetworkACLItem aclItem) {
        final NetworkACLItemResponse response = new NetworkACLItemResponse();

        response.setId(aclItem.getUuid());
        response.setProtocol(aclItem.getProtocol());
        if (aclItem.getSourcePortStart() != null) {
            response.setStartPort(Integer.toString(aclItem.getSourcePortStart()));
        }

        if (aclItem.getSourcePortEnd() != null) {
            response.setEndPort(Integer.toString(aclItem.getSourcePortEnd()));
        }

        response.setCidrList(StringUtils.join(aclItem.getSourceCidrList(), ","));

        response.setTrafficType(aclItem.getTrafficType().toString());

        final NetworkACLItem.State state = aclItem.getState();
        String stateToSet = state.toString();
        if (state.equals(NetworkACLItem.State.Revoke)) {
            stateToSet = "Deleting";
        }

        response.setIcmpCode(aclItem.getIcmpCode());
        response.setIcmpType(aclItem.getIcmpType());

        response.setState(stateToSet);
        response.setNumber(aclItem.getNumber());
        response.setAction(aclItem.getAction().toString());
        response.setForDisplay(aclItem.isDisplay());

        final NetworkACL acl = ApiDBUtils.findByNetworkACLId(aclItem.getAclId());
        if (acl != null) {
            response.setAclId(acl.getUuid());
        }

        //set tag information
        final List<? extends ResourceTag> tags = ApiDBUtils.listByResourceTypeAndId(ResourceObjectType.NetworkACL, aclItem.getId());
        final List<ResourceTagResponse> tagResponses = new ArrayList<>();
        for (final ResourceTag tag : tags) {
            final ResourceTagResponse tagResponse = createResourceTagResponse(tag, true);
            CollectionUtils.addIgnoreNull(tagResponses, tagResponse);
        }
        response.setTags(tagResponses);

        response.setObjectName("networkacl");
        return response;
    }

    @Override
    public NetworkACLResponse createNetworkACLResponse(final NetworkACL networkACL) {
        final NetworkACLResponse response = new NetworkACLResponse();
        response.setId(networkACL.getUuid());
        response.setName(networkACL.getName());
        response.setDescription(networkACL.getDescription());
        response.setForDisplay(networkACL.isDisplay());
        final Vpc vpc = ApiDBUtils.findVpcById(networkACL.getVpcId());
        if (vpc != null) {
            response.setVpcId(vpc.getUuid());
        }
        response.setObjectName("networkacllist");
        return response;
    }

    @Override
    public PrivateGatewayResponse createPrivateGatewayResponse(final PrivateGateway result) {
        final PrivateGatewayResponse response = new PrivateGatewayResponse();
        response.setId(result.getUuid());
        response.setBroadcastUri(result.getBroadcastUri());
        response.setGateway(result.getGateway());
        response.setNetmask(result.getNetmask());
        if (result.getVpcId() != null) {
            final Vpc vpc = ApiDBUtils.findVpcById(result.getVpcId());
            response.setVpcId(vpc.getUuid());
        }

        final DataCenter zone = ApiDBUtils.findZoneById(result.getZoneId());
        if (zone != null) {
            response.setZoneId(zone.getUuid());
            response.setZoneName(zone.getName());
        }
        response.setAddress(result.getIp4Address());
        final PhysicalNetwork pnet = ApiDBUtils.findPhysicalNetworkById(result.getPhysicalNetworkId());
        if (pnet != null) {
            response.setPhysicalNetworkId(pnet.getUuid());
        }

        populateAccount(response, result.getAccountId());
        populateDomain(response, result.getDomainId());
        response.setState(result.getState().toString());
        response.setSourceNat(result.getSourceNat());

        final NetworkACL acl = ApiDBUtils.findByNetworkACLId(result.getNetworkACLId());
        if (acl != null) {
            response.setAclId(acl.getUuid());
        }

        response.setObjectName("privategateway");

        return response;
    }

    @Override
    public StaticRouteResponse createStaticRouteResponse(final StaticRoute result) {
        final StaticRouteResponse response = new StaticRouteResponse();
        response.setId(result.getUuid());
        if (result.getVpcId() != null) {
            final Vpc vpc = ApiDBUtils.findVpcById(result.getVpcId());
            if (vpc != null) {
                response.setVpcId(vpc.getUuid());
            }
        }
        response.setCidr(result.getCidr());
        response.setGwIpAddress(result.getGwIpAddress());

        StaticRoute.State state = result.getState();
        if (state.equals(StaticRoute.State.Revoke)) {
            state = StaticRoute.State.Deleting;
        }
        response.setState(state.toString());
        populateAccount(response, result.getAccountId());
        populateDomain(response, result.getDomainId());

        // set tag information
        final List<? extends ResourceTag> tags = ApiDBUtils.listByResourceTypeAndId(ResourceObjectType.StaticRoute, result.getId());
        final List<ResourceTagResponse> tagResponses = new ArrayList<>();
        for (final ResourceTag tag : tags) {
            final ResourceTagResponse tagResponse = createResourceTagResponse(tag, true);
            CollectionUtils.addIgnoreNull(tagResponses, tagResponse);
        }
        response.setTags(tagResponses);
        response.setObjectName("staticroute");

        return response;
    }

    @Override
    public Site2SiteCustomerGatewayResponse createSite2SiteCustomerGatewayResponse(final Site2SiteCustomerGateway result) {
        final Site2SiteCustomerGatewayResponse response = new Site2SiteCustomerGatewayResponse();
        response.setId(result.getUuid());
        response.setName(result.getName());
        response.setGatewayIp(result.getGatewayIp());
        response.setGuestCidrList(result.getGuestCidrList());
        response.setIpsecPsk(result.getIpsecPsk());
        response.setIkePolicy(result.getIkePolicy());
        response.setEspPolicy(result.getEspPolicy());
        response.setIkeLifetime(result.getIkeLifetime());
        response.setEspLifetime(result.getEspLifetime());
        response.setDpd(result.getDpd());
        response.setEncap(result.getEncap());
        response.setRemoved(result.getRemoved());
        response.setObjectName("vpncustomergateway");

        populateAccount(response, result.getAccountId());
        populateDomain(response, result.getDomainId());

        return response;
    }

    @Override
    public Site2SiteVpnConnectionResponse createSite2SiteVpnConnectionResponse(final Site2SiteVpnConnection result) {
        final Site2SiteVpnConnectionResponse response = new Site2SiteVpnConnectionResponse();
        response.setId(result.getUuid());
        response.setPassive(result.isPassive());

        final Long vpnGatewayId = result.getVpnGatewayId();
        if (vpnGatewayId != null) {
            final Site2SiteVpnGateway vpnGateway = ApiDBUtils.findVpnGatewayById(vpnGatewayId);
            if (vpnGateway != null) {
                response.setVpnGatewayId(vpnGateway.getUuid());
                final long ipId = vpnGateway.getAddrId();
                final IPAddressVO ipObj = ApiDBUtils.findIpAddressById(ipId);
                response.setIp(ipObj.getAddress().addr());
            }
        }

        final Long customerGatewayId = result.getCustomerGatewayId();
        if (customerGatewayId != null) {
            final Site2SiteCustomerGateway customerGateway = ApiDBUtils.findCustomerGatewayById(customerGatewayId);
            if (customerGateway != null) {
                response.setCustomerGatewayId(customerGateway.getUuid());
                response.setGatewayIp(customerGateway.getGatewayIp());
                response.setGuestCidrList(customerGateway.getGuestCidrList());
                response.setIpsecPsk(customerGateway.getIpsecPsk());
                response.setIkePolicy(customerGateway.getIkePolicy());
                response.setEspPolicy(customerGateway.getEspPolicy());
                response.setIkeLifetime(customerGateway.getIkeLifetime());
                response.setEspLifetime(customerGateway.getEspLifetime());
                response.setDpd(customerGateway.getDpd());
                response.setEncap(customerGateway.getEncap());
            }
        }

        populateAccount(response, result.getAccountId());
        populateDomain(response, result.getDomainId());

        response.setState(result.getState().toString());
        response.setCreated(result.getCreated());
        response.setRemoved(result.getRemoved());
        response.setForDisplay(result.isDisplay());
        response.setObjectName("vpnconnection");
        return response;
    }

    @Override
    public CounterResponse createCounterResponse(final Counter counter) {
        final CounterResponse response = new CounterResponse();
        response.setId(counter.getUuid());
        response.setSource(counter.getSource().toString());
        response.setName(counter.getName());
        response.setValue(counter.getValue());
        response.setObjectName("counter");
        return response;
    }

    @Override
    public ConditionResponse createConditionResponse(final Condition condition) {
        final ConditionResponse response = new ConditionResponse();
        response.setId(condition.getUuid());
        final List<CounterResponse> counterResponseList = new ArrayList<>();
        counterResponseList.add(createCounterResponse(ApiDBUtils.getCounter(condition.getCounterid())));
        response.setCounterResponse(counterResponseList);
        response.setRelationalOperator(condition.getRelationalOperator().toString());
        response.setThreshold(condition.getThreshold());
        response.setObjectName("condition");
        populateOwner(response, condition);
        return response;
    }

    @Override
    public AutoScalePolicyResponse createAutoScalePolicyResponse(final AutoScalePolicy policy) {
        final AutoScalePolicyResponse response = new AutoScalePolicyResponse();
        response.setId(policy.getUuid());
        response.setDuration(policy.getDuration());
        response.setQuietTime(policy.getQuietTime());
        response.setAction(policy.getAction());
        final List<ConditionVO> vos = ApiDBUtils.getAutoScalePolicyConditions(policy.getId());
        final ArrayList<ConditionResponse> conditions = new ArrayList<>(vos.size());
        for (final ConditionVO vo : vos) {
            conditions.add(createConditionResponse(vo));
        }
        response.setConditions(conditions);
        response.setObjectName("autoscalepolicy");

        // Populates the account information in the response
        populateOwner(response, policy);

        return response;
    }

    @Override
    public AutoScaleVmProfileResponse createAutoScaleVmProfileResponse(final AutoScaleVmProfile profile) {
        final AutoScaleVmProfileResponse response = new AutoScaleVmProfileResponse();
        response.setId(profile.getUuid());
        if (profile.getZoneId() != null) {
            final DataCenter zone = ApiDBUtils.findZoneById(profile.getZoneId());
            if (zone != null) {
                response.setZoneId(zone.getUuid());
            }
        }
        if (profile.getServiceOfferingId() != null) {
            final ServiceOffering so = ApiDBUtils.findServiceOfferingById(profile.getServiceOfferingId());
            if (so != null) {
                response.setServiceOfferingId(so.getUuid());
            }
        }
        if (profile.getTemplateId() != null) {
            final VMTemplateVO template = ApiDBUtils.findTemplateById(profile.getTemplateId());
            if (template != null) {
                response.setTemplateId(template.getUuid());
            }
        }
        response.setOtherDeployParams(profile.getOtherDeployParams());
        response.setCounterParams(profile.getCounterParams());
        response.setDestroyVmGraceperiod(profile.getDestroyVmGraceperiod());
        final User user = ApiDBUtils.findUserById(profile.getAutoScaleUserId());
        if (user != null) {
            response.setAutoscaleUserId(user.getUuid());
        }
        response.setObjectName("autoscalevmprofile");

        // Populates the account information in the response
        populateOwner(response, profile);
        return response;
    }

    @Override
    public AutoScaleVmGroupResponse createAutoScaleVmGroupResponse(final AutoScaleVmGroup vmGroup) {
        final AutoScaleVmGroupResponse response = new AutoScaleVmGroupResponse();
        response.setId(vmGroup.getUuid());
        response.setMinMembers(vmGroup.getMinMembers());
        response.setMaxMembers(vmGroup.getMaxMembers());
        response.setState(vmGroup.getState());
        response.setInterval(vmGroup.getInterval());
        response.setForDisplay(vmGroup.isDisplay());
        final AutoScaleVmProfileVO profile = ApiDBUtils.findAutoScaleVmProfileById(vmGroup.getProfileId());
        if (profile != null) {
            response.setProfileId(profile.getUuid());
        }
        final FirewallRuleVO fw = ApiDBUtils.findFirewallRuleById(vmGroup.getLoadBalancerId());
        if (fw != null) {
            response.setLoadBalancerId(fw.getUuid());
        }

        final List<AutoScalePolicyResponse> scaleUpPoliciesResponse = new ArrayList<>();
        final List<AutoScalePolicyResponse> scaleDownPoliciesResponse = new ArrayList<>();
        response.setScaleUpPolicies(scaleUpPoliciesResponse);
        response.setScaleDownPolicies(scaleDownPoliciesResponse);
        response.setObjectName("autoscalevmgroup");

        // Fetch policies for vmgroup
        final List<AutoScalePolicy> scaleUpPolicies = new ArrayList<>();
        final List<AutoScalePolicy> scaleDownPolicies = new ArrayList<>();
        ApiDBUtils.getAutoScaleVmGroupPolicies(vmGroup.getId(), scaleUpPolicies, scaleDownPolicies);
        // populate policies
        for (final AutoScalePolicy autoScalePolicy : scaleUpPolicies) {
            scaleUpPoliciesResponse.add(createAutoScalePolicyResponse(autoScalePolicy));
        }
        for (final AutoScalePolicy autoScalePolicy : scaleDownPolicies) {
            scaleDownPoliciesResponse.add(createAutoScalePolicyResponse(autoScalePolicy));
        }

        return response;
    }

    @Override
    public GuestOSResponse createGuestOSResponse(final GuestOS guestOS) {
        final GuestOSResponse response = new GuestOSResponse();
        response.setDescription(guestOS.getDisplayName());
        response.setId(guestOS.getUuid());
        response.setIsUserDefined(Boolean.valueOf(guestOS.getIsUserDefined()).toString());
        final GuestOSCategoryVO category = ApiDBUtils.findGuestOsCategoryById(guestOS.getCategoryId());
        if (category != null) {
            response.setOsCategoryId(category.getUuid());
        }

        response.setObjectName("ostype");
        return response;
    }

    @Override
    public GuestOsMappingResponse createGuestOSMappingResponse(final GuestOSHypervisor guestOSHypervisor) {
        final GuestOsMappingResponse response = new GuestOsMappingResponse();
        response.setId(guestOSHypervisor.getUuid());
        response.setHypervisor(guestOSHypervisor.getHypervisorType());
        response.setHypervisorVersion(guestOSHypervisor.getHypervisorVersion());
        response.setOsNameForHypervisor(guestOSHypervisor.getGuestOsName());
        response.setIsUserDefined(Boolean.valueOf(guestOSHypervisor.getIsUserDefined()).toString());
        final GuestOS guestOs = ApiDBUtils.findGuestOSById(guestOSHypervisor.getGuestOsId());
        if (guestOs != null) {
            response.setOsStdName(guestOs.getDisplayName());
            response.setOsTypeId(guestOs.getUuid());
        }

        response.setObjectName("guestosmapping");
        return response;
    }

    @Override
    public SnapshotScheduleResponse createSnapshotScheduleResponse(final SnapshotSchedule snapshotSchedule) {
        final SnapshotScheduleResponse response = new SnapshotScheduleResponse();
        response.setId(snapshotSchedule.getUuid());
        if (snapshotSchedule.getVolumeId() != null) {
            final Volume vol = ApiDBUtils.findVolumeById(snapshotSchedule.getVolumeId());
            if (vol != null) {
                response.setVolumeId(vol.getUuid());
            }
        }
        if (snapshotSchedule.getPolicyId() != null) {
            final SnapshotPolicy policy = ApiDBUtils.findSnapshotPolicyById(snapshotSchedule.getPolicyId());
            if (policy != null) {
                response.setSnapshotPolicyId(policy.getUuid());
            }
        }
        response.setScheduled(snapshotSchedule.getScheduledTimestamp());

        response.setObjectName("snapshot");
        return response;
    }

    @Override
    public UsageRecordResponse createUsageResponse(final Usage usageRecord) {
        final UsageRecordResponse usageRecResponse = new UsageRecordResponse();

        final Account account = ApiDBUtils.findAccountById(usageRecord.getAccountId());
        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            //find the project
            final Project project = ApiDBUtils.findProjectByProjectAccountIdIncludingRemoved(account.getId());
            if (project != null) {
                usageRecResponse.setProjectId(project.getUuid());
                usageRecResponse.setProjectName(project.getName());
            }
        } else {
            usageRecResponse.setAccountId(account.getUuid());
            usageRecResponse.setAccountName(account.getAccountName());
        }

        final Domain domain = ApiDBUtils.findDomainById(usageRecord.getDomainId());
        if (domain != null) {
            usageRecResponse.setDomainId(domain.getUuid());
        }

        if (usageRecord.getZoneId() != null) {
            final DataCenter zone = ApiDBUtils.findZoneById(usageRecord.getZoneId());
            if (zone != null) {
                usageRecResponse.setZoneId(zone.getUuid());
            }
        }
        usageRecResponse.setDescription(usageRecord.getDescription());
        usageRecResponse.setUsage(usageRecord.getUsageDisplay());
        usageRecResponse.setUsageType(usageRecord.getUsageType());
        if (usageRecord.getVmInstanceId() != null) {
            final VMInstanceVO vm = _entityMgr.findByIdIncludingRemoved(VMInstanceVO.class, usageRecord.getVmInstanceId());
            if (vm != null) {
                usageRecResponse.setVirtualMachineId(vm.getUuid());
            }
        }
        usageRecResponse.setVmName(usageRecord.getVmName());
        if (usageRecord.getTemplateId() != null) {
            final VMTemplateVO template = ApiDBUtils.findTemplateById(usageRecord.getTemplateId());
            if (template != null) {
                usageRecResponse.setTemplateId(template.getUuid());
            }
        }

        if (usageRecord.getUsageType() == UsageTypes.RUNNING_VM || usageRecord.getUsageType() == UsageTypes.ALLOCATED_VM) {
            final ServiceOfferingVO svcOffering = _entityMgr.findByIdIncludingRemoved(ServiceOfferingVO.class, usageRecord.getOfferingId().toString());
            //Service Offering Id
            usageRecResponse.setOfferingId(svcOffering.getUuid());
            //VM Instance ID
            final VMInstanceVO vm = _entityMgr.findByIdIncludingRemoved(VMInstanceVO.class, usageRecord.getUsageId().toString());
            if (vm != null) {
                usageRecResponse.setUsageId(vm.getUuid());
            }
            //Hypervisor Type
            usageRecResponse.setType(usageRecord.getType());
            //Dynamic compute offerings details
            usageRecResponse.setCpuNumber(usageRecord.getCpuCores());
            usageRecResponse.setCpuSpeed(usageRecord.getCpuSpeed());
            usageRecResponse.setMemory(usageRecord.getMemory());
        } else if (usageRecord.getUsageType() == UsageTypes.IP_ADDRESS) {
            //isSourceNAT
            usageRecResponse.setSourceNat(usageRecord.getType().equals("SourceNat") ? true : false);
            //isSystem
            usageRecResponse.setSystem(usageRecord.getSize() == 1 ? true : false);
            //IP Address ID
            final IPAddressVO ip = _entityMgr.findByIdIncludingRemoved(IPAddressVO.class, usageRecord.getUsageId().toString());
            if (ip != null) {
                usageRecResponse.setUsageId(ip.getUuid());
            }
        } else if (usageRecord.getUsageType() == UsageTypes.NETWORK_BYTES_SENT || usageRecord.getUsageType() == UsageTypes.NETWORK_BYTES_RECEIVED) {
            //Device Type
            usageRecResponse.setType(usageRecord.getType());
            if (usageRecord.getType().equals("DomainRouter")) {
                //Domain Router Id
                final VMInstanceVO vm = _entityMgr.findByIdIncludingRemoved(VMInstanceVO.class, usageRecord.getUsageId().toString());
                if (vm != null) {
                    usageRecResponse.setUsageId(vm.getUuid());
                }
            } else {
                //External Device Host Id
                final HostVO host = _entityMgr.findByIdIncludingRemoved(HostVO.class, usageRecord.getUsageId().toString());
                if (host != null) {
                    usageRecResponse.setUsageId(host.getUuid());
                }
            }
            //Network ID
            if (usageRecord.getNetworkId() != null && usageRecord.getNetworkId() != 0) {
                final NetworkVO network = _entityMgr.findByIdIncludingRemoved(NetworkVO.class, usageRecord.getNetworkId().toString());
                if (network != null) {
                    usageRecResponse.setNetworkId(network.getUuid());
                }
            }
        } else if (usageRecord.getUsageType() == UsageTypes.VM_DISK_IO_READ || usageRecord.getUsageType() == UsageTypes.VM_DISK_IO_WRITE
                || usageRecord.getUsageType() == UsageTypes.VM_DISK_BYTES_READ || usageRecord.getUsageType() == UsageTypes.VM_DISK_BYTES_WRITE) {
            //Device Type
            usageRecResponse.setType(usageRecord.getType());
            //VM Instance Id
            final VMInstanceVO vm = _entityMgr.findByIdIncludingRemoved(VMInstanceVO.class, usageRecord.getVmInstanceId().toString());
            if (vm != null) {
                usageRecResponse.setVirtualMachineId(vm.getUuid());
            }
            //Volume ID
            final VolumeVO volume = _entityMgr.findByIdIncludingRemoved(VolumeVO.class, usageRecord.getUsageId().toString());
            if (volume != null) {
                usageRecResponse.setUsageId(volume.getUuid());
            }
        } else if (usageRecord.getUsageType() == UsageTypes.VOLUME) {
            //Volume ID
            final VolumeVO volume = _entityMgr.findByIdIncludingRemoved(VolumeVO.class, usageRecord.getUsageId().toString());
            if (volume != null) {
                usageRecResponse.setUsageId(volume.getUuid());
            }
            //Volume Size
            usageRecResponse.setSize(usageRecord.getSize());
            //Disk Offering Id
            if (usageRecord.getOfferingId() != null) {
                final DiskOfferingVO diskOff = _entityMgr.findByIdIncludingRemoved(DiskOfferingVO.class, usageRecord.getOfferingId().toString());
                usageRecResponse.setOfferingId(diskOff.getUuid());
            }
        } else if (usageRecord.getUsageType() == UsageTypes.TEMPLATE || usageRecord.getUsageType() == UsageTypes.ISO) {
            //Template/ISO ID
            final VMTemplateVO tmpl = _entityMgr.findByIdIncludingRemoved(VMTemplateVO.class, usageRecord.getUsageId().toString());
            if (tmpl != null) {
                usageRecResponse.setUsageId(tmpl.getUuid());
            }
            //Template/ISO Size
            usageRecResponse.setSize(usageRecord.getSize());
            if (usageRecord.getUsageType() == UsageTypes.ISO) {
                usageRecResponse.setVirtualSize(usageRecord.getSize());
            } else {
                usageRecResponse.setVirtualSize(usageRecord.getVirtualSize());
            }
        } else if (usageRecord.getUsageType() == UsageTypes.SNAPSHOT) {
            //Snapshot ID
            final SnapshotVO snap = _entityMgr.findByIdIncludingRemoved(SnapshotVO.class, usageRecord.getUsageId().toString());
            if (snap != null) {
                usageRecResponse.setUsageId(snap.getUuid());
            }
            //Snapshot Size
            usageRecResponse.setSize(usageRecord.getSize());
        } else if (usageRecord.getUsageType() == UsageTypes.LOAD_BALANCER_POLICY) {
            //Load Balancer Policy ID
            final LoadBalancerVO lb = _entityMgr.findByIdIncludingRemoved(LoadBalancerVO.class, usageRecord.getUsageId().toString());
            if (lb != null) {
                usageRecResponse.setUsageId(lb.getUuid());
            }
        } else if (usageRecord.getUsageType() == UsageTypes.PORT_FORWARDING_RULE) {
            //Port Forwarding Rule ID
            final PortForwardingRuleVO pf = _entityMgr.findByIdIncludingRemoved(PortForwardingRuleVO.class, usageRecord.getUsageId().toString());
            if (pf != null) {
                usageRecResponse.setUsageId(pf.getUuid());
            }
        } else if (usageRecord.getUsageType() == UsageTypes.NETWORK_OFFERING) {
            //Network Offering Id
            final NetworkOfferingVO netOff = _entityMgr.findByIdIncludingRemoved(NetworkOfferingVO.class, usageRecord.getOfferingId().toString());
            usageRecResponse.setOfferingId(netOff.getUuid());
            //is Default
            usageRecResponse.setDefault(usageRecord.getUsageId() == 1 ? true : false);
        } else if (usageRecord.getUsageType() == UsageTypes.VPN_USERS) {
            //VPN User ID
            final VpnUserVO vpnUser = _entityMgr.findByIdIncludingRemoved(VpnUserVO.class, usageRecord.getUsageId().toString());
            if (vpnUser != null) {
                usageRecResponse.setUsageId(vpnUser.getUuid());
            }
        } else if (usageRecord.getUsageType() == UsageTypes.SECURITY_GROUP) {
            //Security Group Id
            final SecurityGroupVO sg = _entityMgr.findByIdIncludingRemoved(SecurityGroupVO.class, usageRecord.getUsageId().toString());
            if (sg != null) {
                usageRecResponse.setUsageId(sg.getUuid());
            }
        } else if (usageRecord.getUsageType() == UsageTypes.VM_SNAPSHOT) {
            final VMInstanceVO vm = _entityMgr.findByIdIncludingRemoved(VMInstanceVO.class, usageRecord.getVmInstanceId().toString());
            if (vm != null) {
                usageRecResponse.setVmName(vm.getInstanceName());
                usageRecResponse.setUsageId(vm.getUuid());
            }
            usageRecResponse.setSize(usageRecord.getSize());
            if (usageRecord.getOfferingId() != null) {
                usageRecResponse.setOfferingId(usageRecord.getOfferingId().toString());
            }
        }

        if (usageRecord.getRawUsage() != null) {
            final DecimalFormat decimalFormat = new DecimalFormat("###########.######");
            usageRecResponse.setRawUsage(decimalFormat.format(usageRecord.getRawUsage()));
        }

        if (usageRecord.getStartDate() != null) {
            usageRecResponse.setStartDate(getDateStringInternal(usageRecord.getStartDate()));
        }
        if (usageRecord.getEndDate() != null) {
            usageRecResponse.setEndDate(getDateStringInternal(usageRecord.getEndDate()));
        }

        return usageRecResponse;
    }

    public String getDateStringInternal(final Date inputDate) {
        if (inputDate == null) {
            return null;
        }

        final TimeZone tz = _usageSvc.getUsageTimezone();
        final Calendar cal = Calendar.getInstance(tz);
        cal.setTime(inputDate);

        final StringBuilder sb = new StringBuilder(32);
        sb.append(cal.get(Calendar.YEAR)).append('-');

        final int month = cal.get(Calendar.MONTH) + 1;
        if (month < 10) {
            sb.append('0');
        }
        sb.append(month).append('-');

        final int day = cal.get(Calendar.DAY_OF_MONTH);
        if (day < 10) {
            sb.append('0');
        }
        sb.append(day);

        sb.append("'T'");

        final int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) {
            sb.append('0');
        }
        sb.append(hour).append(':');

        final int minute = cal.get(Calendar.MINUTE);
        if (minute < 10) {
            sb.append('0');
        }
        sb.append(minute).append(':');

        final int seconds = cal.get(Calendar.SECOND);
        if (seconds < 10) {
            sb.append('0');
        }
        sb.append(seconds);

        double offset = cal.get(Calendar.ZONE_OFFSET);
        if (tz.inDaylightTime(inputDate)) {
            offset += 1.0 * tz.getDSTSavings(); // add the timezone's DST
            // value (typically 1 hour
            // expressed in milliseconds)
        }

        offset = offset / (1000d * 60d * 60d);
        final int hourOffset = (int) offset;
        final double decimalVal = Math.abs(offset) - Math.abs(hourOffset);
        final int minuteOffset = (int) (decimalVal * 60);

        if (hourOffset < 0) {
            if (hourOffset > -10) {
                sb.append("-0");
            } else {
                sb.append('-');
            }
            sb.append(Math.abs(hourOffset));
        } else {
            if (hourOffset < 10) {
                sb.append("+0");
            } else {
                sb.append("+");
            }
            sb.append(hourOffset);
        }

        sb.append(':');

        if (minuteOffset == 0) {
            sb.append("00");
        } else if (minuteOffset < 10) {
            sb.append('0').append(minuteOffset);
        } else {
            sb.append(minuteOffset);
        }

        return sb.toString();
    }

    @Override
    public TrafficMonitorResponse createTrafficMonitorResponse(final Host trafficMonitor) {
        final Map<String, String> tmDetails = ApiDBUtils.findHostDetailsById(trafficMonitor.getId());
        final TrafficMonitorResponse response = new TrafficMonitorResponse();
        response.setId(trafficMonitor.getUuid());
        response.setIpAddress(trafficMonitor.getPrivateIpAddress());
        response.setNumRetries(tmDetails.get("numRetries"));
        response.setTimeout(tmDetails.get("timeout"));
        return response;
    }

    @Override
    public VMSnapshotResponse createVMSnapshotResponse(final VMSnapshot vmSnapshot) {
        final VMSnapshotResponse vmSnapshotResponse = new VMSnapshotResponse();
        vmSnapshotResponse.setId(vmSnapshot.getUuid());
        vmSnapshotResponse.setName(vmSnapshot.getName());
        vmSnapshotResponse.setState(vmSnapshot.getState());
        vmSnapshotResponse.setCreated(vmSnapshot.getCreated());
        vmSnapshotResponse.setDescription(vmSnapshot.getDescription());
        vmSnapshotResponse.setDisplayName(vmSnapshot.getDisplayName());
        final UserVm vm = ApiDBUtils.findUserVmById(vmSnapshot.getVmId());
        if (vm != null) {
            vmSnapshotResponse.setVirtualMachineid(vm.getUuid());
        }
        if (vmSnapshot.getParent() != null) {
            final VMSnapshot vmSnapshotParent = ApiDBUtils.getVMSnapshotById(vmSnapshot.getParent());
            if (vmSnapshotParent != null) {
                vmSnapshotResponse.setParent(vmSnapshotParent.getUuid());
                vmSnapshotResponse.setParentName(vmSnapshotParent.getDisplayName());
            }
        }
        final Project project = ApiDBUtils.findProjectByProjectAccountId(vmSnapshot.getAccountId());
        if (project != null) {
            vmSnapshotResponse.setProjectId(project.getUuid());
            vmSnapshotResponse.setProjectName(project.getName());
        }
        vmSnapshotResponse.setCurrent(vmSnapshot.getCurrent());
        vmSnapshotResponse.setType(vmSnapshot.getType().toString());
        vmSnapshotResponse.setObjectName("vmsnapshot");
        return vmSnapshotResponse;
    }

    @Override
    public NicSecondaryIpResponse createSecondaryIPToNicResponse(final NicSecondaryIp result) {
        final NicSecondaryIpResponse response = new NicSecondaryIpResponse();
        final NicVO nic = _entityMgr.findById(NicVO.class, result.getNicId());
        final NetworkVO network = _entityMgr.findById(NetworkVO.class, result.getNetworkId());
        response.setId(result.getUuid());
        response.setIpAddr(result.getIp4Address());
        response.setNicId(nic.getUuid());
        response.setNwId(network.getUuid());
        response.setObjectName("nicsecondaryip");
        return response;
    }

    @Override
    public NicResponse createNicResponse(final Nic result) {
        final NicResponse response = new NicResponse();
        final NetworkVO network = _entityMgr.findById(NetworkVO.class, result.getNetworkId());
        final VMInstanceVO vm = _entityMgr.findById(VMInstanceVO.class, result.getInstanceId());

        response.setId(result.getUuid());
        response.setNetworkid(network.getUuid());

        if (vm != null) {
            response.setVmId(vm.getUuid());
        }

        response.setIpaddress(result.getIPv4Address());

        if (result.getSecondaryIp()) {
            final List<NicSecondaryIpVO> secondaryIps = ApiDBUtils.findNicSecondaryIps(result.getId());
            if (secondaryIps != null) {
                final List<NicSecondaryIpResponse> ipList = new ArrayList<>();
                for (final NicSecondaryIpVO ip : secondaryIps) {
                    final NicSecondaryIpResponse ipRes = new NicSecondaryIpResponse();
                    ipRes.setId(ip.getUuid());
                    ipRes.setIpAddr(ip.getIp4Address());
                    ipList.add(ipRes);
                }
                response.setSecondaryIps(ipList);
            }
        }

        response.setGateway(result.getIPv4Gateway());
        response.setNetmask(result.getIPv4Netmask());
        response.setMacAddress(result.getMacAddress());

        if (result.getIPv6Address() != null) {
            response.setIp6Address(result.getIPv6Address());
        }

        response.setDeviceId(String.valueOf(result.getDeviceId()));

        response.setIsDefault(result.isDefaultNic());
        return response;
    }

    @Override
    public ApplicationLoadBalancerResponse createLoadBalancerContainerReponse(final ApplicationLoadBalancerRule lb, final Map<Ip, UserVm> lbInstances) {

        final ApplicationLoadBalancerResponse lbResponse = new ApplicationLoadBalancerResponse();
        lbResponse.setId(lb.getUuid());
        lbResponse.setName(lb.getName());
        lbResponse.setDescription(lb.getDescription());
        lbResponse.setAlgorithm(lb.getAlgorithm());
        lbResponse.setForDisplay(lb.isDisplay());
        final Network nw = ApiDBUtils.findNetworkById(lb.getNetworkId());
        lbResponse.setNetworkId(nw.getUuid());
        populateOwner(lbResponse, lb);

        if (lb.getScheme() == Scheme.Internal) {
            lbResponse.setSourceIp(lb.getSourceIp().addr());
            //TODO - create the view for the load balancer rule to reflect the network uuid
            final Network network = ApiDBUtils.findNetworkById(lb.getNetworkId());
            lbResponse.setSourceIpNetworkId(network.getUuid());
        } else {
            //for public, populate the ip information from the ip address
            final IpAddress publicIp = ApiDBUtils.findIpAddressById(lb.getSourceIpAddressId());
            lbResponse.setSourceIp(publicIp.getAddress().addr());
            final Network ntwk = ApiDBUtils.findNetworkById(publicIp.getNetworkId());
            lbResponse.setSourceIpNetworkId(ntwk.getUuid());
        }

        //set load balancer rules information (only one rule per load balancer in this release)
        final List<ApplicationLoadBalancerRuleResponse> ruleResponses = new ArrayList<>();
        final ApplicationLoadBalancerRuleResponse ruleResponse = new ApplicationLoadBalancerRuleResponse();
        ruleResponse.setInstancePort(lb.getDefaultPortStart());
        ruleResponse.setSourcePort(lb.getSourcePortStart());
        FirewallRule.State stateToSet = lb.getState();
        if (stateToSet.equals(FirewallRule.State.Revoke)) {
            stateToSet = FirewallRule.State.Deleting;
        }
        ruleResponse.setState(stateToSet.toString());
        ruleResponse.setObjectName("loadbalancerrule");
        ruleResponses.add(ruleResponse);
        lbResponse.setLbRules(ruleResponses);

        //set Lb instances information
        final List<ApplicationLoadBalancerInstanceResponse> instanceResponses = new ArrayList<>();
        for (final Map.Entry<Ip, UserVm> entry : lbInstances.entrySet()) {
            final Ip ip = entry.getKey();
            final UserVm vm = entry.getValue();
            final ApplicationLoadBalancerInstanceResponse instanceResponse = new ApplicationLoadBalancerInstanceResponse();
            instanceResponse.setIpAddress(ip.addr());
            instanceResponse.setId(vm.getUuid());
            instanceResponse.setName(vm.getInstanceName());
            instanceResponse.setObjectName("loadbalancerinstance");
            instanceResponses.add(instanceResponse);
        }

        lbResponse.setLbInstances(instanceResponses);

        //set tag information
        final List<? extends ResourceTag> tags = ApiDBUtils.listByResourceTypeAndId(ResourceObjectType.LoadBalancer, lb.getId());
        final List<ResourceTagResponse> tagResponses = new ArrayList<>();
        for (final ResourceTag tag : tags) {
            final ResourceTagResponse tagResponse = createResourceTagResponse(tag, true);
            CollectionUtils.addIgnoreNull(tagResponses, tagResponse);
        }
        lbResponse.setTags(tagResponses);

        lbResponse.setObjectName("loadbalancer");
        return lbResponse;
    }

    @Override
    public AffinityGroupResponse createAffinityGroupResponse(final AffinityGroup group) {

        final AffinityGroupResponse response = new AffinityGroupResponse();

        final Account account = ApiDBUtils.findAccountById(group.getAccountId());
        response.setId(group.getUuid());
        response.setAccountName(account.getAccountName());
        response.setName(group.getName());
        response.setType(group.getType());
        response.setDescription(group.getDescription());
        final Domain domain = ApiDBUtils.findDomainById(account.getDomainId());
        if (domain != null) {
            response.setDomainId(domain.getUuid());
            response.setDomainName(domain.getName());
        }

        response.setObjectName("affinitygroup");
        return response;
    }

    @Override
    public Long getAffinityGroupId(final String groupName, final long accountId) {
        final AffinityGroup ag = ApiDBUtils.getAffinityGroup(groupName, accountId);
        if (ag == null) {
            return null;
        } else {
            return ag.getId();
        }
    }

    @Override
    public PortableIpRangeResponse createPortableIPRangeResponse(final PortableIpRange ipRange) {
        final PortableIpRangeResponse response = new PortableIpRangeResponse();
        response.setId(ipRange.getUuid());
        final String ipRangeStr = ipRange.getIpRange();
        if (ipRangeStr != null) {
            final String[] range = ipRangeStr.split("-");
            response.setStartIp(range[0]);
            response.setEndIp(range[1]);
        }
        response.setVlan(ipRange.getVlanTag());
        response.setGateway(ipRange.getGateway());
        response.setNetmask(ipRange.getNetmask());
        response.setRegionId(ipRange.getRegionId());
        response.setObjectName("portableiprange");
        return response;
    }

    @Override
    public PortableIpResponse createPortableIPResponse(final PortableIp portableIp) {
        final PortableIpResponse response = new PortableIpResponse();
        response.setAddress(portableIp.getAddress());
        final Long accountId = portableIp.getAllocatedInDomainId();
        if (accountId != null) {
            final Account account = ApiDBUtils.findAccountById(accountId);
            response.setAllocatedToAccountId(account.getAccountName());
            final Domain domain = ApiDBUtils.findDomainById(account.getDomainId());
            response.setAllocatedInDomainId(domain.getUuid());
        }

        response.setAllocatedTime(portableIp.getAllocatedTime());

        if (portableIp.getAssociatedDataCenterId() != null) {
            final DataCenter zone = ApiDBUtils.findZoneById(portableIp.getAssociatedDataCenterId());
            if (zone != null) {
                response.setAssociatedDataCenterId(zone.getUuid());
            }
        }

        if (portableIp.getPhysicalNetworkId() != null) {
            final PhysicalNetwork pnw = ApiDBUtils.findPhysicalNetworkById(portableIp.getPhysicalNetworkId());
            if (pnw != null) {
                response.setPhysicalNetworkId(pnw.getUuid());
            }
        }

        if (portableIp.getAssociatedWithNetworkId() != null) {
            final Network ntwk = ApiDBUtils.findNetworkById(portableIp.getAssociatedWithNetworkId());
            if (ntwk != null) {
                response.setAssociatedWithNetworkId(ntwk.getUuid());
            }
        }

        if (portableIp.getAssociatedWithVpcId() != null) {
            final Vpc vpc = ApiDBUtils.findVpcById(portableIp.getAssociatedWithVpcId());
            if (vpc != null) {
                response.setAssociatedWithVpcId(vpc.getUuid());
            }
        }

        response.setState(portableIp.getState().name());
        response.setObjectName("portableip");
        return response;
    }

    @Override
    public InternalLoadBalancerElementResponse createInternalLbElementResponse(final VirtualRouterProvider result) {
        if (result.getType() != VirtualRouterProvider.Type.InternalLbVm) {
            return null;
        }
        final InternalLoadBalancerElementResponse response = new InternalLoadBalancerElementResponse();
        response.setId(result.getUuid());
        final PhysicalNetworkServiceProvider nsp = ApiDBUtils.findPhysicalNetworkServiceProviderById(result.getNspId());
        if (nsp != null) {
            response.setNspId(nsp.getUuid());
        }
        response.setEnabled(result.isEnabled());

        response.setObjectName("internalloadbalancerelement");
        return response;
    }

    @Override
    public IsolationMethodResponse createIsolationMethodResponse(final IsolationType method) {
        final IsolationMethodResponse response = new IsolationMethodResponse();
        response.setIsolationMethodName(method.toString());
        response.setObjectName("isolationmethod");
        return response;
    }

    @Override
    public ListResponse<UpgradeRouterTemplateResponse> createUpgradeRouterTemplateResponse(final List<Long> jobIds) {
        final ListResponse<UpgradeRouterTemplateResponse> response = new ListResponse<>();
        final List<UpgradeRouterTemplateResponse> responses = new ArrayList<>();
        for (final Long jobId : jobIds) {
            final UpgradeRouterTemplateResponse routerResponse = new UpgradeRouterTemplateResponse();
            final AsyncJob job = _entityMgr.findById(AsyncJob.class, jobId);
            routerResponse.setAsyncJobId(job.getUuid());
            routerResponse.setObjectName("asyncjobs");
            responses.add(routerResponse);
        }
        response.setResponses(responses);
        return response;
    }

    @Override
    public SSHKeyPairResponse createSSHKeyPairResponse(final SSHKeyPair sshkeyPair, final boolean privatekey) {
        SSHKeyPairResponse response = new SSHKeyPairResponse(sshkeyPair.getName(), sshkeyPair.getFingerprint());
        if (privatekey) {
            response = new CreateSSHKeyPairResponse(sshkeyPair.getName(), sshkeyPair.getFingerprint(), sshkeyPair.getPrivateKey());
        }
        final Account account = ApiDBUtils.findAccountById(sshkeyPair.getAccountId());
        response.setAccountName(account.getAccountName());
        final Domain domain = ApiDBUtils.findDomainById(sshkeyPair.getDomainId());
        response.setDomainId(domain.getUuid());
        response.setDomainName(domain.getName());
        return response;
    }

    // TODO: we may need to refactor once ControlledEntityResponse and
    // ControlledEntity id to uuid conversion are all done.
    // currently code is scattered in
    private void populateOwner(final ControlledEntityResponse response, final ControlledEntity object) {
        final Account account = ApiDBUtils.findAccountById(object.getAccountId());

        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            // find the project
            final Project project = ApiDBUtils.findProjectByProjectAccountId(account.getId());
            response.setProjectId(project.getUuid());
            response.setProjectName(project.getName());
        } else {
            response.setAccountName(account.getAccountName());
        }

        final Domain domain = ApiDBUtils.findDomainById(object.getDomainId());
        response.setDomainId(domain.getUuid());
        response.setDomainName(domain.getName());
    }

    public static DataStoreRole getDataStoreRole(final Snapshot snapshot, final SnapshotDataStoreDao snapshotStoreDao, final DataStoreManager dataStoreMgr) {
        final SnapshotDataStoreVO snapshotStore = snapshotStoreDao.findBySnapshot(snapshot.getId(), DataStoreRole.Primary);

        if (snapshotStore == null) {
            return DataStoreRole.Image;
        }

        final long storagePoolId = snapshotStore.getDataStoreId();
        final DataStore dataStore = dataStoreMgr.getDataStore(storagePoolId, DataStoreRole.Primary);

        final Map<String, String> mapCapabilities = dataStore.getDriver().getCapabilities();

        if (mapCapabilities != null) {
            final String value = mapCapabilities.get(DataStoreCapabilities.STORAGE_SYSTEM_SNAPSHOT.toString());
            final Boolean supportsStorageSystemSnapshots = new Boolean(value);

            if (supportsStorageSystemSnapshots) {
                return DataStoreRole.Primary;
            }
        }

        return DataStoreRole.Image;
    }

    private void populateDomain(final ControlledEntityResponse response, final long domainId) {
        final Domain domain = ApiDBUtils.findDomainById(domainId);

        response.setDomainId(domain.getUuid());
        response.setDomainName(domain.getName());
    }

    private void populateAccount(final ControlledEntityResponse response, final long accountId) {
        final Account account = ApiDBUtils.findAccountById(accountId);
        if (account.getType() == Account.ACCOUNT_TYPE_PROJECT) {
            // find the project
            final Project project = ApiDBUtils.findProjectByProjectAccountId(account.getId());
            response.setProjectId(project.getUuid());
            response.setProjectName(project.getName());
            response.setAccountName(account.getAccountName());
        } else {
            response.setAccountName(account.getAccountName());
        }
    }
}
