package com.cloud.api;

import com.cloud.affinity.AffinityGroup;
import com.cloud.affinity.AffinityGroupResponse;
import com.cloud.affinity.dao.AffinityGroupDao;
import com.cloud.agent.api.VgpuTypesInfo;
import com.cloud.api.ApiConstants.HostDetails;
import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.query.dao.AccountJoinDao;
import com.cloud.api.query.dao.AffinityGroupJoinDao;
import com.cloud.api.query.dao.AsyncJobJoinDao;
import com.cloud.api.query.dao.DataCenterJoinDao;
import com.cloud.api.query.dao.DiskOfferingJoinDao;
import com.cloud.api.query.dao.DomainJoinDao;
import com.cloud.api.query.dao.DomainRouterJoinDao;
import com.cloud.api.query.dao.HostJoinDao;
import com.cloud.api.query.dao.HostTagDao;
import com.cloud.api.query.dao.ImageStoreJoinDao;
import com.cloud.api.query.dao.InstanceGroupJoinDao;
import com.cloud.api.query.dao.ProjectAccountJoinDao;
import com.cloud.api.query.dao.ProjectInvitationJoinDao;
import com.cloud.api.query.dao.ProjectJoinDao;
import com.cloud.api.query.dao.ResourceTagJoinDao;
import com.cloud.api.query.dao.ServiceOfferingJoinDao;
import com.cloud.api.query.dao.StoragePoolJoinDao;
import com.cloud.api.query.dao.StorageTagDao;
import com.cloud.api.query.dao.TemplateJoinDao;
import com.cloud.api.query.dao.UserAccountJoinDao;
import com.cloud.api.query.dao.UserVmJoinDao;
import com.cloud.api.query.dao.VolumeJoinDao;
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
import com.cloud.api.query.vo.ServiceOfferingJoinVO;
import com.cloud.api.query.vo.StoragePoolJoinVO;
import com.cloud.api.query.vo.StorageTagVO;
import com.cloud.api.query.vo.TemplateJoinVO;
import com.cloud.api.query.vo.UserAccountJoinVO;
import com.cloud.api.query.vo.UserVmJoinVO;
import com.cloud.api.query.vo.VolumeJoinVO;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.AsyncJobResponse;
import com.cloud.api.response.DiskOfferingResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.DomainRouterResponse;
import com.cloud.api.response.EventResponse;
import com.cloud.api.response.HostForMigrationResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.HostTagResponse;
import com.cloud.api.response.ImageStoreResponse;
import com.cloud.api.response.InstanceGroupResponse;
import com.cloud.api.response.ProjectAccountResponse;
import com.cloud.api.response.ProjectInvitationResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.ResourceTagResponse;
import com.cloud.api.response.ServiceOfferingResponse;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.api.response.StorageTagResponse;
import com.cloud.api.response.TemplateResponse;
import com.cloud.api.response.UserResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.capacity.CapacityManager;
import com.cloud.capacity.CapacityVO;
import com.cloud.capacity.dao.CapacityDao;
import com.cloud.capacity.dao.CapacityDaoImpl.SummedCapacity;
import com.cloud.configuration.Config;
import com.cloud.configuration.ConfigurationManager;
import com.cloud.configuration.ConfigurationService;
import com.cloud.configuration.Resource;
import com.cloud.configuration.Resource.ResourceType;
import com.cloud.context.CallContext;
import com.cloud.db.repository.ZoneRepository;
import com.cloud.dc.AccountVlanMapVO;
import com.cloud.dc.ClusterDetailsDao;
import com.cloud.dc.ClusterDetailsVO;
import com.cloud.dc.ClusterVO;
import com.cloud.dc.DataCenter;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.Vlan;
import com.cloud.dc.VlanVO;
import com.cloud.dc.dao.AccountVlanMapDao;
import com.cloud.dc.dao.ClusterDao;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.dc.dao.VlanDao;
import com.cloud.domain.Domain;
import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.engine.orchestration.service.NetworkOrchestrationService;
import com.cloud.engine.orchestration.service.VolumeOrchestrationService;
import com.cloud.event.Event;
import com.cloud.event.dao.EventJoinDao;
import com.cloud.framework.config.dao.ConfigurationDao;
import com.cloud.framework.jobs.AsyncJob;
import com.cloud.framework.jobs.AsyncJobManager;
import com.cloud.framework.jobs.dao.AsyncJobDao;
import com.cloud.gpu.HostGpuGroupsVO;
import com.cloud.gpu.VGPUTypesVO;
import com.cloud.gpu.dao.HostGpuGroupsDao;
import com.cloud.gpu.dao.VGPUTypesDao;
import com.cloud.ha.HighAvailabilityManager;
import com.cloud.host.Host;
import com.cloud.host.HostStats;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.host.dao.HostDetailsDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.network.IpAddress;
import com.cloud.network.Network;
import com.cloud.network.Network.Capability;
import com.cloud.network.Network.Provider;
import com.cloud.network.Network.Service;
import com.cloud.network.NetworkModel;
import com.cloud.network.NetworkProfile;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.network.dao.AccountGuestVlanMapDao;
import com.cloud.network.dao.AccountGuestVlanMapVO;
import com.cloud.network.dao.FirewallRulesCidrsDao;
import com.cloud.network.dao.FirewallRulesDao;
import com.cloud.network.dao.IPAddressDao;
import com.cloud.network.dao.IPAddressVO;
import com.cloud.network.dao.LoadBalancerDao;
import com.cloud.network.dao.LoadBalancerVO;
import com.cloud.network.dao.NetworkDao;
import com.cloud.network.dao.NetworkDomainDao;
import com.cloud.network.dao.NetworkDomainVO;
import com.cloud.network.dao.NetworkVO;
import com.cloud.network.dao.PhysicalNetworkDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderDao;
import com.cloud.network.dao.PhysicalNetworkServiceProviderVO;
import com.cloud.network.dao.PhysicalNetworkTrafficTypeDao;
import com.cloud.network.dao.PhysicalNetworkTrafficTypeVO;
import com.cloud.network.dao.PhysicalNetworkVO;
import com.cloud.network.dao.Site2SiteCustomerGatewayDao;
import com.cloud.network.dao.Site2SiteCustomerGatewayVO;
import com.cloud.network.dao.Site2SiteVpnGatewayDao;
import com.cloud.network.dao.Site2SiteVpnGatewayVO;
import com.cloud.network.router.VirtualRouter;
import com.cloud.network.rules.FirewallRuleVO;
import com.cloud.network.vpc.NetworkACL;
import com.cloud.network.vpc.StaticRouteVO;
import com.cloud.network.vpc.VpcGatewayVO;
import com.cloud.network.vpc.VpcManager;
import com.cloud.network.vpc.VpcOffering;
import com.cloud.network.vpc.VpcProvisioningService;
import com.cloud.network.vpc.VpcVO;
import com.cloud.network.vpc.dao.NetworkACLDao;
import com.cloud.network.vpc.dao.StaticRouteDao;
import com.cloud.network.vpc.dao.VpcDao;
import com.cloud.network.vpc.dao.VpcGatewayDao;
import com.cloud.network.vpc.dao.VpcOfferingDao;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.offerings.NetworkOfferingVO;
import com.cloud.offerings.dao.NetworkOfferingDao;
import com.cloud.projects.Project;
import com.cloud.projects.ProjectAccount;
import com.cloud.projects.ProjectInvitation;
import com.cloud.projects.ProjectService;
import com.cloud.resource.ResourceManager;
import com.cloud.server.ManagementServer;
import com.cloud.server.ResourceMetaDataService;
import com.cloud.server.ResourceTag;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.server.StatsCollector;
import com.cloud.server.TaggedResourceService;
import com.cloud.service.ServiceOfferingDetailsVO;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.service.dao.ServiceOfferingDetailsDao;
import com.cloud.storage.DiskOfferingVO;
import com.cloud.storage.GuestOS;
import com.cloud.storage.GuestOSCategoryVO;
import com.cloud.storage.ImageStore;
import com.cloud.storage.Snapshot;
import com.cloud.storage.SnapshotVO;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageManager;
import com.cloud.storage.StoragePool;
import com.cloud.storage.StorageStats;
import com.cloud.storage.UploadVO;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.Volume;
import com.cloud.storage.Volume.Type;
import com.cloud.storage.VolumeVO;
import com.cloud.storage.dao.DiskOfferingDao;
import com.cloud.storage.dao.GuestOSCategoryDao;
import com.cloud.storage.dao.GuestOSDao;
import com.cloud.storage.dao.SnapshotDao;
import com.cloud.storage.dao.UploadDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.storage.dao.VMTemplateDetailsDao;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.storage.datastore.db.PrimaryDataStoreDao;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.template.TemplateManager;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.AccountDetailsDao;
import com.cloud.user.AccountService;
import com.cloud.user.AccountVO;
import com.cloud.user.ResourceLimitService;
import com.cloud.user.SSHKeyPairVO;
import com.cloud.user.User;
import com.cloud.user.UserAccount;
import com.cloud.user.UserStatisticsVO;
import com.cloud.user.UserVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.SSHKeyPairDao;
import com.cloud.user.dao.UserDao;
import com.cloud.user.dao.UserStatisticsDao;
import com.cloud.uservm.UserVm;
import com.cloud.utils.EnumUtils;
import com.cloud.utils.Pair;
import com.cloud.utils.exception.InvalidParameterValueException;
import com.cloud.vm.ConsoleProxyVO;
import com.cloud.vm.DomainRouterVO;
import com.cloud.vm.InstanceGroup;
import com.cloud.vm.InstanceGroupVO;
import com.cloud.vm.NicProfile;
import com.cloud.vm.UserVmDetailVO;
import com.cloud.vm.UserVmManager;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.VirtualMachine;
import com.cloud.vm.VmStats;
import com.cloud.vm.dao.ConsoleProxyDao;
import com.cloud.vm.dao.DomainRouterDao;
import com.cloud.vm.dao.NicSecondaryIpDao;
import com.cloud.vm.dao.NicSecondaryIpVO;
import com.cloud.vm.dao.UserVmDao;
import com.cloud.vm.dao.UserVmDetailsDao;
import com.cloud.vm.dao.VMInstanceDao;
import com.cloud.vm.snapshot.VMSnapshot;
import com.cloud.vm.snapshot.dao.VMSnapshotDao;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class ApiDBUtils {
    static AsyncJobManager s_asyncMgr;
    static StorageManager s_storageMgr;
    static VolumeOrchestrationService s_volumeMgr;
    static UserVmManager s_userVmMgr;
    static NetworkModel s_networkModel;
    static NetworkOrchestrationService s_networkMgr;
    static TemplateManager s_templateMgr;
    static ConfigurationManager s_configMgr;
    static StatsCollector s_statsCollector;
    static AccountDao s_accountDao;
    static AccountVlanMapDao s_accountVlanMapDao;
    static ClusterDao s_clusterDao;
    static CapacityDao s_capacityDao;
    static DiskOfferingDao s_diskOfferingDao;
    static DiskOfferingJoinDao s_diskOfferingJoinDao;
    static DataCenterJoinDao s_dcJoinDao;
    static DomainDao s_domainDao;
    static DomainJoinDao s_domainJoinDao;
    static DomainRouterDao s_domainRouterDao;
    static DomainRouterJoinDao s_domainRouterJoinDao;
    static GuestOSDao s_guestOSDao;
    static GuestOSCategoryDao s_guestOSCategoryDao;
    static HostDao s_hostDao;
    static AccountGuestVlanMapDao s_accountGuestVlanMapDao;
    static IPAddressDao s_ipAddressDao;
    static LoadBalancerDao s_loadBalancerDao;
    static ServiceOfferingJoinDao s_serviceOfferingJoinDao;
    static HostPodDao s_podDao;
    static ServiceOfferingDao s_serviceOfferingDao;
    static ServiceOfferingDetailsDao s_serviceOfferingDetailsDao;
    static SnapshotDao s_snapshotDao;
    static PrimaryDataStoreDao s_storagePoolDao;
    static VMTemplateDao s_templateDao;
    static VMTemplateDetailsDao s_templateDetailsDao;
    static UploadDao s_uploadDao;
    static UserDao s_userDao;
    static UserStatisticsDao s_userStatsDao;
    static UserVmDao s_userVmDao;
    static UserVmJoinDao s_userVmJoinDao;
    static VlanDao s_vlanDao;
    static VolumeDao s_volumeDao;
    static Site2SiteVpnGatewayDao s_site2SiteVpnGatewayDao;
    static Site2SiteCustomerGatewayDao s_site2SiteCustomerGatewayDao;
    static DataCenterDao s_zoneDao;
    static NetworkOfferingDao s_networkOfferingDao;
    static NetworkDao s_networkDao;
    static PhysicalNetworkDao s_physicalNetworkDao;
    static ConfigurationService s_configSvc;
    static ConfigurationDao s_configDao;
    static ConsoleProxyDao s_consoleProxyDao;
    static FirewallRulesCidrsDao s_firewallCidrsDao;
    static VMInstanceDao s_vmDao;
    static ResourceLimitService s_resourceLimitMgr;
    static ProjectService s_projectMgr;
    static ResourceManager s_resourceMgr;
    static AccountDetailsDao s_accountDetailsDao;
    static NetworkDomainDao s_networkDomainDao;
    static HighAvailabilityManager s_haMgr;
    static VpcManager s_vpcMgr;
    static TaggedResourceService s_taggedResourceService;
    static UserVmDetailsDao s_userVmDetailsDao;
    static SSHKeyPairDao s_sshKeyPairDao;
    static ResourceTagJoinDao s_tagJoinDao;
    static EventJoinDao s_eventJoinDao;
    static InstanceGroupJoinDao s_vmGroupJoinDao;
    static UserAccountJoinDao s_userAccountJoinDao;
    static ProjectJoinDao s_projectJoinDao;
    static ProjectAccountJoinDao s_projectAccountJoinDao;
    static ProjectInvitationJoinDao s_projectInvitationJoinDao;
    static HostJoinDao s_hostJoinDao;
    static VolumeJoinDao s_volJoinDao;
    static StoragePoolJoinDao s_poolJoinDao;
    static StorageTagDao s_tagDao;
    static HostTagDao s_hostTagDao;
    static ImageStoreJoinDao s_imageStoreJoinDao;
    static AccountJoinDao s_accountJoinDao;
    static AsyncJobJoinDao s_jobJoinDao;
    static TemplateJoinDao s_templateJoinDao;
    static PhysicalNetworkTrafficTypeDao s_physicalNetworkTrafficTypeDao;
    static PhysicalNetworkServiceProviderDao s_physicalNetworkServiceProviderDao;
    static FirewallRulesDao s_firewallRuleDao;
    static StaticRouteDao s_staticRouteDao;
    static VpcGatewayDao s_vpcGatewayDao;
    static VpcDao s_vpcDao;
    static VpcOfferingDao s_vpcOfferingDao;
    static AsyncJobDao s_asyncJobDao;
    static HostDetailsDao s_hostDetailsDao;
    static VMSnapshotDao s_vmSnapshotDao;
    static ClusterDetailsDao s_clusterDetailsDao;
    static NicSecondaryIpDao s_nicSecondaryIpDao;
    static VpcProvisioningService s_vpcProvSvc;
    static AffinityGroupDao s_affinityGroupDao;
    static AffinityGroupJoinDao s_affinityGroupJoinDao;
    static NetworkACLDao s_networkACLDao;
    static AccountService s_accountService;
    static ResourceMetaDataService s_resourceDetailsService;
    static HostGpuGroupsDao s_hostGpuGroupsDao;
    static VGPUTypesDao s_vgpuTypesDao;
    static ZoneRepository s_zoneRepository;
    private static ManagementServer s_ms;
    @Inject
    public AsyncJobManager asyncMgr;
    @Inject
    private ManagementServer ms;
    @Inject
    private StorageManager storageMgr;
    @Inject
    private UserVmManager userVmMgr;
    @Inject
    private NetworkModel networkModel;
    @Inject
    private NetworkOrchestrationService networkMgr;
    @Inject
    private StatsCollector statsCollector;
    @Inject
    private TemplateManager templateMgr;
    @Inject
    private VolumeOrchestrationService volumeMgr;
    @Inject
    private AccountDao accountDao;
    @Inject
    private AccountVlanMapDao accountVlanMapDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private CapacityDao capacityDao;
    @Inject
    private DataCenterJoinDao dcJoinDao;
    @Inject
    private DiskOfferingDao diskOfferingDao;
    @Inject
    private DiskOfferingJoinDao diskOfferingJoinDao;
    @Inject
    private DomainDao domainDao;
    @Inject
    private DomainJoinDao domainJoinDao;
    @Inject
    private DomainRouterDao domainRouterDao;
    @Inject
    private DomainRouterJoinDao domainRouterJoinDao;
    @Inject
    private GuestOSDao guestOSDao;
    @Inject
    private GuestOSCategoryDao guestOSCategoryDao;
    @Inject
    private HostDao hostDao;
    @Inject
    private AccountGuestVlanMapDao accountGuestVlanMapDao;
    @Inject
    private IPAddressDao ipAddressDao;
    @Inject
    private LoadBalancerDao loadBalancerDao;
    @Inject
    private ServiceOfferingJoinDao serviceOfferingJoinDao;
    @Inject
    private HostPodDao podDao;
    @Inject
    private ServiceOfferingDao serviceOfferingDao;
    @Inject
    private ServiceOfferingDetailsDao serviceOfferingDetailsDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private PrimaryDataStoreDao storagePoolDao;
    @Inject
    private VMTemplateDao templateDao;
    @Inject
    private VMTemplateDetailsDao templateDetailsDao;
    @Inject
    private UploadDao uploadDao;
    @Inject
    private UserDao userDao;
    @Inject
    private UserStatisticsDao userStatsDao;
    @Inject
    private UserVmDao userVmDao;
    @Inject
    private UserVmJoinDao userVmJoinDao;
    @Inject
    private VlanDao vlanDao;
    @Inject
    private VolumeDao volumeDao;
    @Inject
    private Site2SiteVpnGatewayDao site2SiteVpnGatewayDao;
    @Inject
    private Site2SiteCustomerGatewayDao site2SiteCustomerGatewayDao;
    @Inject
    private DataCenterDao zoneDao;
    @Inject
    private NetworkOfferingDao networkOfferingDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private PhysicalNetworkDao physicalNetworkDao;
    @Inject
    private ConfigurationService configSvc;
    @Inject
    private ConfigurationDao configDao;
    @Inject
    private ConsoleProxyDao consoleProxyDao;
    @Inject
    private FirewallRulesCidrsDao firewallCidrsDao;
    @Inject
    private VMInstanceDao vmDao;
    @Inject
    private ResourceLimitService resourceLimitMgr;
    @Inject
    private ProjectService projectMgr;
    @Inject
    private ResourceManager resourceMgr;
    @Inject
    private AccountDetailsDao accountDetailsDao;
    @Inject
    private NetworkDomainDao networkDomainDao;
    @Inject
    private HighAvailabilityManager haMgr;
    @Inject
    private VpcManager vpcMgr;
    @Inject
    private TaggedResourceService taggedResourceService;
    @Inject
    private UserVmDetailsDao userVmDetailsDao;
    @Inject
    private SSHKeyPairDao sshKeyPairDao;
    @Inject
    private ResourceTagJoinDao tagJoinDao;
    @Inject
    private EventJoinDao eventJoinDao;
    @Inject
    private InstanceGroupJoinDao vmGroupJoinDao;
    @Inject
    private UserAccountJoinDao userAccountJoinDao;
    @Inject
    private ProjectJoinDao projectJoinDao;
    @Inject
    private ProjectAccountJoinDao projectAccountJoinDao;
    @Inject
    private ProjectInvitationJoinDao projectInvitationJoinDao;
    @Inject
    private HostJoinDao hostJoinDao;
    @Inject
    private VolumeJoinDao volJoinDao;
    @Inject
    private StoragePoolJoinDao poolJoinDao;
    @Inject
    private StorageTagDao tagDao;
    @Inject
    private HostTagDao hosttagDao;
    @Inject
    private ImageStoreJoinDao imageStoreJoinDao;
    @Inject
    private AccountJoinDao accountJoinDao;
    @Inject
    private AsyncJobJoinDao jobJoinDao;
    @Inject
    private TemplateJoinDao templateJoinDao;

    @Inject
    private PhysicalNetworkTrafficTypeDao physicalNetworkTrafficTypeDao;
    @Inject
    private PhysicalNetworkServiceProviderDao physicalNetworkServiceProviderDao;
    @Inject
    private FirewallRulesDao firewallRuleDao;
    @Inject
    private StaticRouteDao staticRouteDao;
    @Inject
    private VpcGatewayDao vpcGatewayDao;
    @Inject
    private VpcDao vpcDao;
    @Inject
    private VpcOfferingDao vpcOfferingDao;
    @Inject
    private AsyncJobDao asyncJobDao;
    @Inject
    private HostDetailsDao hostDetailsDao;
    @Inject
    private ClusterDetailsDao clusterDetailsDao;
    @Inject
    private VMSnapshotDao vmSnapshotDao;
    @Inject
    private NicSecondaryIpDao nicSecondaryIpDao;
    @Inject
    private VpcProvisioningService vpcProvSvc;
    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private AffinityGroupJoinDao affinityGroupJoinDao;
    @Inject
    private NetworkACLDao networkACLDao;
    @Inject
    private AccountService accountService;
    @Inject
    private ConfigurationManager configMgr;
    @Inject
    private ResourceMetaDataService resourceDetailsService;
    @Inject
    private HostGpuGroupsDao hostGpuGroupsDao;
    @Inject
    private VGPUTypesDao vgpuTypesDao;
    @Inject
    private ZoneRepository zoneRepository;

    public static long getStorageCapacitybyPool(final Long poolId, final short capacityType) {
        // TODO: This method is for the API only, but it has configuration values (ramSize for system vms)
        // so if this Utils class can have some kind of config rather than a static initializer (maybe from
        // management server instantiation?) then maybe the management server method can be moved entirely
        // into this utils class.
        return s_ms.getMemoryOrCpuCapacityByHost(poolId, capacityType);
    }

    // ///////////////////////////////////////////////////////////
    // ManagementServer methods //
    // ///////////////////////////////////////////////////////////

    public static List<SummedCapacity> getCapacityByClusterPodZone(final Long zoneId, final Long podId, final Long clusterId) {
        return s_capacityDao.findByClusterPodZone(zoneId, podId, clusterId);
    }

    public static List<SummedCapacity> findNonSharedStorageForClusterPodZone(final Long zoneId, final Long podId, final Long clusterId) {
        return s_capacityDao.findNonSharedStorageForClusterPodZone(zoneId, podId, clusterId);
    }

    public static List<CapacityVO> getCapacityByPod() {
        return null;
    }

    public static Long getPodIdForVlan(final long vlanDbId) {
        return s_networkModel.getPodIdForVlan(vlanDbId);
    }

    public static String getVersion() {
        return s_ms.getVersion();
    }

    public static long findCorrectResourceLimitForDomain(final Long limit, final boolean isRootDomain, final ResourceType type, final long domainId) {
        final long max = Resource.RESOURCE_UNLIMITED; // if resource limit is not found, then we treat it as unlimited

        // No limits for Root domain
        if (isRootDomain) {
            return max;
        }
        if (limit != null) {
            return limit.longValue();
        } else {
            return findCorrectResourceLimitForDomain(type, domainId);
        }
    }

    public static long findCorrectResourceLimitForDomain(final ResourceType type, final long domainId) {
        final DomainVO domain = s_domainDao.findById(domainId);

        if (domain == null) {
            return -1;
        }

        return s_resourceLimitMgr.findCorrectResourceLimitForDomain(domain, type);
    }

    // ///////////////////////////////////////////////////////////
    // Manager methods //
    // ///////////////////////////////////////////////////////////

    public static long findCorrectResourceLimit(final ResourceType type, final long accountId) {
        final AccountVO account = s_accountDao.findById(accountId);

        if (account == null) {
            return -1;
        }

        return s_resourceLimitMgr.findCorrectResourceLimitForAccount(account, type);
    }

    public static long findCorrectResourceLimit(final Long limit, final long accountId, final ResourceType type) {
        return s_resourceLimitMgr.findCorrectResourceLimitForAccount(accountId, limit, type);
    }

    public static long getResourceCount(final ResourceType type, final long accountId) {
        final AccountVO account = s_accountDao.findById(accountId);

        if (account == null) {
            return -1;
        }

        return s_resourceLimitMgr.getResourceCount(account, type);
    }

    public static String getSnapshotIntervalTypes(final long snapshotId) {
        final SnapshotVO snapshot = s_snapshotDao.findById(snapshotId);
        return snapshot.getRecurringType().name();
    }

    public static String getStoragePoolTags(final long poolId) {
        return s_storageMgr.getStoragePoolTags(poolId);
    }

    public static boolean isLocalStorageActiveOnHost(final Long hostId) {
        return s_storageMgr.isLocalStorageActiveOnHost(hostId);
    }

    public static InstanceGroupVO findInstanceGroupForVM(final long vmId) {
        return s_userVmMgr.getGroupForVm(vmId);
    }

    public static HostStats getHostStatistics(final long hostId) {
        return s_statsCollector.getHostStats(hostId);
    }

    public static StorageStats getStoragePoolStatistics(final long id) {
        return s_statsCollector.getStoragePoolStats(id);
    }

    // ///////////////////////////////////////////////////////////
    // Misc methods //
    // ///////////////////////////////////////////////////////////

    public static VmStats getVmStatistics(final long hostId) {
        return s_statsCollector.getVmStats(hostId);
    }

    public static StorageStats getSecondaryStorageStatistics(final long id) {
        return s_statsCollector.getStorageStats(id);
    }

    public static CapacityVO getStoragePoolUsedStats(final Long poolId, final Long clusterId, final Long podId, final Long zoneId) {
        return s_storageMgr.getStoragePoolUsedStats(poolId, clusterId, podId, zoneId);
    }

    public static CapacityVO getSecondaryStorageUsedStats(final Long hostId, final Long zoneId) {
        return s_storageMgr.getSecondaryStorageUsedStats(hostId, zoneId);
    }

    public static Account findAccountByIdIncludingRemoved(final Long accountId) {
        return s_accountDao.findByIdIncludingRemoved(accountId);
    }

    public static Account findAccountByNameDomain(final String accountName, final Long domainId) {
        return s_accountDao.findActiveAccount(accountName, domainId);
    }

    // ///////////////////////////////////////////////////////////
    // Dao methods //
    // ///////////////////////////////////////////////////////////

    public static ClusterVO findClusterById(final long clusterId) {
        return s_clusterDao.findById(clusterId);
    }

    public static String findClusterDetails(final long clusterId, final String name) {
        final ClusterDetailsVO detailsVO = s_clusterDetailsDao.findDetail(clusterId, name);
        if (detailsVO != null) {
            return detailsVO.getValue();
        }

        return null;
    }

    public static DiskOfferingVO findDiskOfferingById(final Long diskOfferingId) {
        final DiskOfferingVO off = s_diskOfferingDao.findByIdIncludingRemoved(diskOfferingId);
        if (off.getType() == DiskOfferingVO.Type.Disk) {
            return off;
        }
        return null;
    }

    public static DomainVO findDomainById(final Long domainId) {
        return s_domainDao.findByIdIncludingRemoved(domainId);
    }

    public static DomainVO findDomainByIdIncludingRemoved(final Long domainId) {
        return s_domainDao.findByIdIncludingRemoved(domainId);
    }

    public static boolean isChildDomain(final long parentId, final long childId) {
        return s_domainDao.isChildDomain(parentId, childId);
    }

    public static DomainRouterVO findDomainRouterById(final Long routerId) {
        return s_domainRouterDao.findByIdIncludingRemoved(routerId);
    }

    public static GuestOS findGuestOSById(final Long id) {
        return s_guestOSDao.findByIdIncludingRemoved(id);
    }

    public static GuestOS findGuestOSByDisplayName(final String displayName) {
        return s_guestOSDao.listByDisplayName(displayName);
    }

    public static GuestOSCategoryVO getHostGuestOSCategory(final long hostId) {
        final Long guestOSCategoryID = s_resourceMgr.getGuestOSCategoryId(hostId);

        if (guestOSCategoryID != null) {
            return s_guestOSCategoryDao.findById(guestOSCategoryID);
        } else {
            return null;
        }
    }

    public static String getHostTags(final long hostId) {
        return s_resourceMgr.getHostTags(hostId);
    }

    public static LoadBalancerVO findLoadBalancerById(final Long loadBalancerId) {
        return s_loadBalancerDao.findById(loadBalancerId);
    }

    public static HostPodVO findPodById(final Long podId) {
        return s_podDao.findById(podId);
    }

    public static VolumeVO findRootVolume(final long vmId) {
        final List<VolumeVO> volumes = s_volumeDao.findByInstanceAndType(vmId, Type.ROOT);
        if (volumes != null && volumes.size() == 1) {
            return volumes.get(0);
        } else {
            return null;
        }
    }

    public static ServiceOffering findServiceOfferingById(final Long serviceOfferingId) {
        return s_serviceOfferingDao.findByIdIncludingRemoved(serviceOfferingId);
    }

    public static ServiceOfferingDetailsVO findServiceOfferingDetail(final long serviceOfferingId, final String key) {
        return s_serviceOfferingDetailsDao.findDetail(serviceOfferingId, key);
    }

    public static UploadVO findUploadById(final Long id) {
        return s_uploadDao.findById(id);
    }

    public static UserVm findUserVmById(final Long vmId) {
        return s_userVmDao.findById(vmId);
    }

    public static VlanVO findVlanById(final long vlanDbId) {
        return s_vlanDao.findById(vlanDbId);
    }

    public static Site2SiteVpnGatewayVO findVpnGatewayById(final Long vpnGatewayId) {
        return s_site2SiteVpnGatewayDao.findById(vpnGatewayId);
    }

    public static Site2SiteCustomerGatewayVO findCustomerGatewayById(final Long customerGatewayId) {
        return s_site2SiteCustomerGatewayDao.findById(customerGatewayId);
    }

    public static List<UserVO> listUsersByAccount(final long accountId) {
        return s_userDao.listByAccount(accountId);
    }

    public static DataCenterVO findZoneById(final Long zoneId) {
        return s_zoneDao.findById(zoneId);
    }

    public static Long getAccountIdForVlan(final long vlanDbId) {
        final List<AccountVlanMapVO> accountVlanMaps = s_accountVlanMapDao.listAccountVlanMapsByVlan(vlanDbId);
        if (accountVlanMaps.isEmpty()) {
            return null;
        } else {
            return accountVlanMaps.get(0).getAccountId();
        }
    }

    public static Long getAccountIdForGuestVlan(final long vlanDbId) {
        final List<AccountGuestVlanMapVO> accountGuestVlanMaps = s_accountGuestVlanMapDao.listAccountGuestVlanMapsByVlan(vlanDbId);
        if (accountGuestVlanMaps.isEmpty()) {
            return null;
        } else {
            return accountGuestVlanMaps.get(0).getAccountId();
        }
    }

    public static HypervisorType getVolumeHyperType(final long volumeId) {
        return s_volumeDao.getHypervisorType(volumeId);
    }

    public static HypervisorType getHypervisorTypeFromFormat(final long dcId, final ImageFormat format) {
        HypervisorType type = s_storageMgr.getHypervisorTypeFromFormat(format);

        if (format == ImageFormat.RAW) {
            // Currently, KVM only suppoorts RBD images of type RAW.
            // This results in a weird collision with OVM volumes which
            // can only be raw, thus making KVM RBD volumes show up as OVM
            // rather than RBD. This block of code can (hopefuly) by checking to
            // see if the pool is using either RBD or NFS. However, it isn't
            // quite clear what to do if both storage types are used. If the image
            // format is RAW, it narrows the hypervisor choice down to OVM and KVM / RBD or KVM / CLVM
            // This would be better implemented at a cluster level.
            final List<StoragePoolVO> pools = s_storagePoolDao.listByDataCenterId(dcId);
            final ListIterator<StoragePoolVO> itr = pools.listIterator();
            while (itr.hasNext()) {
                final StoragePoolVO pool = itr.next();
                if (pool.getPoolType() == StoragePoolType.RBD || pool.getPoolType() == StoragePoolType.CLVM) {
                    // This case will note the presence of non-qcow2 primary stores, suggesting KVM without NFS. Otherwse,
                    // If this check is not passed, the hypervisor type will remain OVM.
                    type = HypervisorType.KVM;
                    break;
                }
            }
        }
        return type;
    }

    public static List<HostGpuGroupsVO> getGpuGroups(final long hostId) {
        return s_hostGpuGroupsDao.listByHostId(hostId);
    }

    public static List<VgpuTypesInfo> getGpuCapacites(final Long zoneId, final Long podId, final Long clusterId) {
        return s_vgpuTypesDao.listGPUCapacities(zoneId, podId, clusterId);
    }

    public static HashMap<String, Long> getVgpuVmsCount(final Long zoneId, final Long podId, final Long clusterId) {
        return s_vmDao.countVgpuVMs(zoneId, podId, clusterId);
    }

    public static List<VGPUTypesVO> getVgpus(final long groupId) {
        return s_vgpuTypesDao.listByGroupId(groupId);
    }

    public static List<UserStatisticsVO> listUserStatsBy(final Long accountId) {
        return s_userStatsDao.listBy(accountId);
    }

    public static List<UserVmVO> listUserVMsByHostId(final long hostId) {
        return s_userVmDao.listByHostId(hostId);
    }

    public static List<UserVmVO> listUserVMsByNetworkId(final long networkId) {
        return s_userVmDao.listByNetworkIdAndStates(networkId, VirtualMachine.State.Running,
                VirtualMachine.State.Starting, VirtualMachine.State.Stopping, VirtualMachine.State.Unknown,
                VirtualMachine.State.Migrating);
    }

    public static List<DomainRouterVO> listDomainRoutersByNetworkId(final long networkId) {
        return s_domainRouterDao.findByNetwork(networkId);
    }

    public static List<DataCenterVO> listZones() {
        return s_zoneDao.listAll();
    }

    public static boolean volumeIsOnSharedStorage(final long volumeId) {
        // Check that the volume is valid
        final VolumeVO volume = s_volumeDao.findById(volumeId);
        if (volume == null) {
            throw new InvalidParameterValueException("Please specify a valid volume ID.");
        }

        return s_volumeMgr.volumeOnSharedStoragePool(volume);
    }

    public static List<NicProfile> getNics(final VirtualMachine vm) {
        return s_networkMgr.getNicProfiles(vm);
    }

    public static NetworkProfile getNetworkProfile(final long networkId) {
        return s_networkMgr.convertNetworkToNetworkProfile(networkId);
    }

    public static NetworkOfferingVO findNetworkOfferingById(final long networkOfferingId) {
        return s_networkOfferingDao.findByIdIncludingRemoved(networkOfferingId);
    }

    public static List<? extends Vlan> listVlanByNetworkId(final long networkId) {
        return s_vlanDao.listVlansByNetworkId(networkId);
    }

    public static Map<Service, Map<Capability, String>> getNetworkCapabilities(final long networkId, final long zoneId) {
        return s_networkModel.getNetworkCapabilities(networkId);
    }

    public static long getPublicNetworkIdByZone(final long zoneId) {
        return s_networkModel.getSystemNetworkByZoneAndTrafficType(zoneId, TrafficType.Public).getId();
    }

    public static Long getVlanNetworkId(final long vlanId) {
        final VlanVO vlan = s_vlanDao.findById(vlanId);
        if (vlan != null) {
            return vlan.getNetworkId();
        } else {
            return null;
        }
    }

    public static Integer getNetworkRate(final long networkOfferingId) {
        return s_configMgr.getNetworkOfferingNetworkRate(networkOfferingId, null);
    }

    public static Account getVlanAccount(final long vlanId) {
        return s_configSvc.getVlanAccount(vlanId);
    }

    public static Domain getVlanDomain(final long vlanId) {
        return s_configSvc.getVlanDomain(vlanId);
    }

    public static Long getDedicatedNetworkDomain(final long networkId) {
        return s_networkModel.getDedicatedNetworkDomain(networkId);
    }

    public static float getCpuOverprovisioningFactor(final long clusterId) {
        return CapacityManager.CpuOverprovisioningFactor.valueIn(clusterId);
    }

    public static boolean isExtractionDisabled() {
        final String disableExtractionString = s_configDao.getValue(Config.DisableExtraction.toString());
        final boolean disableExtraction = (disableExtractionString == null) ? false : Boolean.parseBoolean(disableExtractionString);
        return disableExtraction;
    }

    public static ConsoleProxyVO findConsoleProxy(final long id) {
        return s_consoleProxyDao.findById(id);
    }

    public static List<String> findFirewallSourceCidrs(final long id) {
        return s_firewallCidrsDao.getSourceCidrs(id);
    }

    public static Account getProjectOwner(final long projectId) {
        return s_projectMgr.getProjectOwner(projectId);
    }

    public static Project findProjectByProjectAccountId(final long projectAccountId) {
        return s_projectMgr.findByProjectAccountId(projectAccountId);
    }

    public static Project findProjectByProjectAccountIdIncludingRemoved(final long projectAccountId) {
        return s_projectMgr.findByProjectAccountIdIncludingRemoved(projectAccountId);
    }

    public static Project findProjectById(final long projectId) {
        return s_projectMgr.getProject(projectId);
    }

    public static long getProjectOwnwerId(final long projectId) {
        return s_projectMgr.getProjectOwner(projectId).getId();
    }

    public static Map<String, String> getAccountDetails(final long accountId) {
        final Map<String, String> details = s_accountDetailsDao.findDetails(accountId);
        return details.isEmpty() ? null : details;
    }

    public static Map<Service, Set<Provider>> listNetworkOfferingServices(final long networkOfferingId) {
        return s_networkModel.getNetworkOfferingServiceProvidersMap(networkOfferingId);
    }

    public static List<Service> getElementServices(final Provider provider) {
        return s_networkModel.getElementServices(provider);
    }

    public static List<? extends Provider> getProvidersForService(final Service service) {
        return s_networkModel.listSupportedNetworkServiceProviders(service.getName());
    }

    public static boolean canElementEnableIndividualServices(final Provider serviceProvider) {
        return s_networkModel.canElementEnableIndividualServices(serviceProvider);
    }

    public static Pair<Long, Boolean> getDomainNetworkDetails(final long networkId) {
        final NetworkDomainVO map = s_networkDomainDao.getDomainNetworkMapByNetworkId(networkId);

        final boolean subdomainAccess = (map.isSubdomainAccess() != null) ? map.isSubdomainAccess() : s_networkModel.getAllowSubdomainAccessGlobal();

        return new Pair<>(map.getDomainId(), subdomainAccess);
    }

    public static long countFreePublicIps() {
        return s_ipAddressDao.countFreePublicIPs();
    }

    public static long findDefaultRouterServiceOffering() {
        final ServiceOfferingVO serviceOffering = s_serviceOfferingDao.findByName(ServiceOffering.routerDefaultOffUniqueName);
        return serviceOffering.getId();
    }

    public static IpAddress findIpByAssociatedVmId(final long vmId) {
        return s_ipAddressDao.findByAssociatedVmId(vmId);
    }

    public static String getHaTag() {
        return s_haMgr.getHaTag();
    }

    public static Map<Service, Set<Provider>> listVpcOffServices(final long vpcOffId) {
        return s_vpcMgr.getVpcOffSvcProvidersMap(vpcOffId);
    }

    public static List<? extends Network> listVpcNetworks(final long vpcId) {
        return s_networkModel.listNetworksByVpc(vpcId);
    }

    public static boolean canUseForDeploy(final Network network) {
        return s_networkModel.canUseForDeploy(network);
    }

    public static VMSnapshot getVMSnapshotById(final Long vmSnapshotId) {
        final VMSnapshot vmSnapshot = s_vmSnapshotDao.findById(vmSnapshotId);
        return vmSnapshot;
    }

    public static String getUuid(final String resourceId, final ResourceObjectType resourceType) {
        return s_taggedResourceService.getUuid(resourceId, resourceType);
    }

    public static List<? extends ResourceTag> listByResourceTypeAndId(final ResourceObjectType type, final long resourceId) {
        return s_taggedResourceService.listByResourceTypeAndId(type, resourceId);
    }

    public static String getKeyPairName(final String sshPublicKey) {
        final SSHKeyPairVO sshKeyPair = s_sshKeyPairDao.findByPublicKey(sshPublicKey);
        //key might be removed prior to this point
        if (sshKeyPair != null) {
            return sshKeyPair.getName();
        }
        return null;
    }

    public static UserVmDetailVO findPublicKeyByVmId(final long vmId) {
        return s_userVmDetailsDao.findDetail(vmId, "SSH.PublicKey");
    }

    public static GuestOSCategoryVO findGuestOsCategoryById(final long catId) {
        return s_guestOSCategoryDao.findById(catId);
    }

    public static VpcVO findVpcById(final long vpcId) {
        return s_vpcDao.findById(vpcId);
    }

    public static VpcOffering findVpcOfferingById(final long offeringId) {
        return s_vpcOfferingDao.findById(offeringId);
    }

    public static NetworkACL findByNetworkACLId(final long aclId) {
        return s_networkACLDao.findById(aclId);
    }

    public static AsyncJob findAsyncJobById(final long jobId) {
        return s_asyncJobDao.findById(jobId);
    }

    public static String findJobInstanceUuid(final AsyncJob job) {
        if (job == null) {
            return null;
        }
        String jobInstanceId = null;
        final ApiCommandJobType jobInstanceType = EnumUtils.fromString(ApiCommandJobType.class, job.getInstanceType(), ApiCommandJobType.None);

        if (job.getInstanceId() == null) {
            // when assert is hit, implement 'getInstanceId' of BaseAsyncCmd and return appropriate instance id
            assert (false);
            return null;
        }

        if (jobInstanceType == ApiCommandJobType.Volume) {
            final VolumeVO volume = ApiDBUtils.findVolumeById(job.getInstanceId());
            if (volume != null) {
                jobInstanceId = volume.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.Template || jobInstanceType == ApiCommandJobType.Iso) {
            final VMTemplateVO template = ApiDBUtils.findTemplateById(job.getInstanceId());
            if (template != null) {
                jobInstanceId = template.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.VirtualMachine || jobInstanceType == ApiCommandJobType.ConsoleProxy ||
                jobInstanceType == ApiCommandJobType.SystemVm || jobInstanceType == ApiCommandJobType.DomainRouter) {
            final VMInstanceVO vm = ApiDBUtils.findVMInstanceById(job.getInstanceId());
            if (vm != null) {
                jobInstanceId = vm.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.Snapshot) {
            final Snapshot snapshot = ApiDBUtils.findSnapshotById(job.getInstanceId());
            if (snapshot != null) {
                jobInstanceId = snapshot.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.Host) {
            final Host host = ApiDBUtils.findHostById(job.getInstanceId());
            if (host != null) {
                jobInstanceId = host.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.StoragePool) {
            final StoragePoolVO spool = ApiDBUtils.findStoragePoolById(job.getInstanceId());
            if (spool != null) {
                jobInstanceId = spool.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.IpAddress) {
            final IPAddressVO ip = ApiDBUtils.findIpAddressById(job.getInstanceId());
            if (ip != null) {
                jobInstanceId = ip.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.PhysicalNetwork) {
            final PhysicalNetworkVO pnet = ApiDBUtils.findPhysicalNetworkById(job.getInstanceId());
            if (pnet != null) {
                jobInstanceId = pnet.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.TrafficType) {
            final PhysicalNetworkTrafficTypeVO trafficType = ApiDBUtils.findPhysicalNetworkTrafficTypeById(job.getInstanceId());
            if (trafficType != null) {
                jobInstanceId = trafficType.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.PhysicalNetworkServiceProvider) {
            final PhysicalNetworkServiceProvider sp = ApiDBUtils.findPhysicalNetworkServiceProviderById(job.getInstanceId());
            if (sp != null) {
                jobInstanceId = sp.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.FirewallRule) {
            final FirewallRuleVO fw = ApiDBUtils.findFirewallRuleById(job.getInstanceId());
            if (fw != null) {
                jobInstanceId = fw.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.Account) {
            final Account acct = ApiDBUtils.findAccountById(job.getInstanceId());
            if (acct != null) {
                jobInstanceId = acct.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.User) {
            final User usr = ApiDBUtils.findUserById(job.getInstanceId());
            if (usr != null) {
                jobInstanceId = usr.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.StaticRoute) {
            final StaticRouteVO route = ApiDBUtils.findStaticRouteById(job.getInstanceId());
            if (route != null) {
                jobInstanceId = route.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.PrivateGateway) {
            final VpcGatewayVO gateway = ApiDBUtils.findVpcGatewayById(job.getInstanceId());
            if (gateway != null) {
                jobInstanceId = gateway.getUuid();
            }
        } else if (jobInstanceType == ApiCommandJobType.Network) {
            final NetworkVO networkVO = ApiDBUtils.findNetworkById(job.getInstanceId());
            if (networkVO != null) {
                jobInstanceId = networkVO.getUuid();
            }
        } else if (jobInstanceType != ApiCommandJobType.None) {
            // TODO : when we hit here, we need to add instanceType -> UUID
            // entity table mapping
            assert (false);
        }
        return jobInstanceId;
    }

    public static VolumeVO findVolumeById(final Long volumeId) {
        return s_volumeDao.findByIdIncludingRemoved(volumeId);
    }

    public static VMTemplateVO findTemplateById(final Long templateId) {
        final VMTemplateVO template = s_templateDao.findByIdIncludingRemoved(templateId);
        if (template != null) {
            final Map<String, String> details = s_templateDetailsDao.listDetailsKeyPairs(templateId);
            if (details != null && !details.isEmpty()) {
                template.setDetails(details);
            }
        }
        return template;
    }

    public static VMInstanceVO findVMInstanceById(final long vmId) {
        return s_vmDao.findByIdIncludingRemoved(vmId);
    }

    public static Snapshot findSnapshotById(final long snapshotId) {
        return s_snapshotDao.findByIdIncludingRemoved(snapshotId);
    }

    public static HostVO findHostById(final Long hostId) {
        return s_hostDao.findByIdIncludingRemoved(hostId);
    }

    public static StoragePoolVO findStoragePoolById(final Long storagePoolId) {
        return s_storagePoolDao.findByIdIncludingRemoved(storagePoolId);
    }

    public static IPAddressVO findIpAddressById(final long addressId) {
        return s_ipAddressDao.findById(addressId);
    }

    public static PhysicalNetworkVO findPhysicalNetworkById(final long id) {
        return s_physicalNetworkDao.findById(id);
    }

    public static PhysicalNetworkTrafficTypeVO findPhysicalNetworkTrafficTypeById(final long id) {
        return s_physicalNetworkTrafficTypeDao.findById(id);
    }

    public static PhysicalNetworkServiceProviderVO findPhysicalNetworkServiceProviderById(final long providerId) {
        return s_physicalNetworkServiceProviderDao.findById(providerId);
    }

    public static FirewallRuleVO findFirewallRuleById(final long ruleId) {
        return s_firewallRuleDao.findById(ruleId);
    }

    public static Account findAccountById(final Long accountId) {
        return s_accountDao.findByIdIncludingRemoved(accountId);
    }

    public static User findUserById(final Long userId) {
        return s_userDao.findById(userId);
    }

    public static StaticRouteVO findStaticRouteById(final long routeId) {
        return s_staticRouteDao.findById(routeId);
    }

    public static VpcGatewayVO findVpcGatewayById(final long gatewayId) {
        return s_vpcGatewayDao.findById(gatewayId);
    }

    public static NetworkVO findNetworkById(final long id) {
        return s_networkDao.findByIdIncludingRemoved(id);
    }

    public static DomainRouterResponse newDomainRouterResponse(final DomainRouterJoinVO vr, final Account caller) {
        return s_domainRouterJoinDao.newDomainRouterResponse(vr, caller);
    }

    ///////////////////////////////////////////////////////////////////////
    //  Newly Added Utility Methods for List API refactoring             //
    ///////////////////////////////////////////////////////////////////////

    public static DomainRouterResponse fillRouterDetails(final DomainRouterResponse vrData, final DomainRouterJoinVO vr) {
        return s_domainRouterJoinDao.setDomainRouterResponse(vrData, vr);
    }

    public static List<DomainRouterJoinVO> newDomainRouterView(final VirtualRouter vr) {
        return s_domainRouterJoinDao.newDomainRouterView(vr);
    }

    public static UserVmResponse newUserVmResponse(final ResponseView view, final String objectName, final UserVmJoinVO userVm, final EnumSet<VMDetails> details, final Account
            caller) {
        return s_userVmJoinDao.newUserVmResponse(view, objectName, userVm, details, caller);
    }

    public static UserVmResponse fillVmDetails(final ResponseView view, final UserVmResponse vmData, final UserVmJoinVO vm) {
        return s_userVmJoinDao.setUserVmResponse(view, vmData, vm);
    }

    public static List<UserVmJoinVO> newUserVmView(final UserVm... userVms) {
        return s_userVmJoinDao.newUserVmView(userVms);
    }

    public static ResourceTagResponse newResourceTagResponse(final ResourceTagJoinVO vsg, final boolean keyValueOnly) {
        return s_tagJoinDao.newResourceTagResponse(vsg, keyValueOnly);
    }

    public static ResourceTagJoinVO newResourceTagView(final ResourceTag sg) {
        return s_tagJoinDao.newResourceTagView(sg);
    }

    public static ResourceTagJoinVO findResourceTagViewById(final Long tagId) {
        return s_tagJoinDao.searchById(tagId);
    }

    public static EventResponse newEventResponse(final EventJoinVO ve) {
        return s_eventJoinDao.newEventResponse(ve);
    }

    public static EventJoinVO newEventView(final Event e) {
        return s_eventJoinDao.newEventView(e);
    }

    public static InstanceGroupResponse newInstanceGroupResponse(final InstanceGroupJoinVO ve) {
        return s_vmGroupJoinDao.newInstanceGroupResponse(ve);
    }

    public static InstanceGroupJoinVO newInstanceGroupView(final InstanceGroup e) {
        return s_vmGroupJoinDao.newInstanceGroupView(e);
    }

    public static UserResponse newUserResponse(final UserAccountJoinVO usr) {
        return newUserResponse(usr, null);
    }

    public static UserResponse newUserResponse(final UserAccountJoinVO usr, final Long domainId) {
        final UserResponse response = s_userAccountJoinDao.newUserResponse(usr);
        if (domainId != null && usr.getDomainId() != domainId) {
            response.setIsCallerChildDomain(true);
        } else {
            response.setIsCallerChildDomain(false);
        }
        return response;
    }

    public static UserAccountJoinVO newUserView(final User usr) {
        return s_userAccountJoinDao.newUserView(usr);
    }

    public static UserAccountJoinVO newUserView(final UserAccount usr) {
        return s_userAccountJoinDao.newUserView(usr);
    }

    public static ProjectResponse newProjectResponse(final ProjectJoinVO proj) {
        return s_projectJoinDao.newProjectResponse(proj);
    }

    public static ProjectResponse fillProjectDetails(final ProjectResponse rsp, final ProjectJoinVO proj) {
        return s_projectJoinDao.setProjectResponse(rsp, proj);
    }

    public static List<ProjectJoinVO> newProjectView(final Project proj) {
        return s_projectJoinDao.newProjectView(proj);
    }

    public static List<UserAccountJoinVO> findUserViewByAccountId(final Long accountId) {
        return s_userAccountJoinDao.searchByAccountId(accountId);
    }

    public static ProjectAccountResponse newProjectAccountResponse(final ProjectAccountJoinVO proj) {
        return s_projectAccountJoinDao.newProjectAccountResponse(proj);
    }

    public static ProjectAccountJoinVO newProjectAccountView(final ProjectAccount proj) {
        return s_projectAccountJoinDao.newProjectAccountView(proj);
    }

    public static ProjectInvitationResponse newProjectInvitationResponse(final ProjectInvitationJoinVO proj) {
        return s_projectInvitationJoinDao.newProjectInvitationResponse(proj);
    }

    public static ProjectInvitationJoinVO newProjectInvitationView(final ProjectInvitation proj) {
        return s_projectInvitationJoinDao.newProjectInvitationView(proj);
    }

    public static HostResponse newHostResponse(final HostJoinVO vr, final EnumSet<HostDetails> details) {
        return s_hostJoinDao.newHostResponse(vr, details);
    }

    public static HostResponse fillHostDetails(final HostResponse vrData, final HostJoinVO vr) {
        return s_hostJoinDao.setHostResponse(vrData, vr);
    }

    public static HostForMigrationResponse newHostForMigrationResponse(final HostJoinVO vr, final EnumSet<HostDetails> details) {
        return s_hostJoinDao.newHostForMigrationResponse(vr, details);
    }

    public static HostForMigrationResponse fillHostForMigrationDetails(final HostForMigrationResponse vrData, final HostJoinVO vr) {
        return s_hostJoinDao.setHostForMigrationResponse(vrData, vr);
    }

    public static List<HostJoinVO> newHostView(final Host vr) {
        return s_hostJoinDao.newHostView(vr);
    }

    public static VolumeResponse newVolumeResponse(final ResponseView view, final VolumeJoinVO vr) {
        return s_volJoinDao.newVolumeResponse(view, vr);
    }

    public static VolumeResponse fillVolumeDetails(final ResponseView view, final VolumeResponse vrData, final VolumeJoinVO vr) {
        return s_volJoinDao.setVolumeResponse(view, vrData, vr);
    }

    public static List<VolumeJoinVO> newVolumeView(final Volume vr) {
        return s_volJoinDao.newVolumeView(vr);
    }

    public static StoragePoolResponse newStoragePoolResponse(final StoragePoolJoinVO vr) {
        return s_poolJoinDao.newStoragePoolResponse(vr);
    }

    public static StorageTagResponse newStorageTagResponse(final StorageTagVO vr) {
        return s_tagDao.newStorageTagResponse(vr);
    }

    public static HostTagResponse newHostTagResponse(final HostTagVO vr) {
        return s_hostTagDao.newHostTagResponse(vr);
    }

    public static StoragePoolResponse fillStoragePoolDetails(final StoragePoolResponse vrData, final StoragePoolJoinVO vr) {
        return s_poolJoinDao.setStoragePoolResponse(vrData, vr);
    }

    public static StoragePoolResponse newStoragePoolForMigrationResponse(final StoragePoolJoinVO vr) {
        return s_poolJoinDao.newStoragePoolForMigrationResponse(vr);
    }

    public static StoragePoolResponse fillStoragePoolForMigrationDetails(final StoragePoolResponse vrData, final StoragePoolJoinVO vr) {
        return s_poolJoinDao.setStoragePoolForMigrationResponse(vrData, vr);
    }

    public static List<StoragePoolJoinVO> newStoragePoolView(final StoragePool vr) {
        return s_poolJoinDao.newStoragePoolView(vr);
    }

    public static ImageStoreResponse newImageStoreResponse(final ImageStoreJoinVO vr) {
        return s_imageStoreJoinDao.newImageStoreResponse(vr);
    }

    public static ImageStoreResponse fillImageStoreDetails(final ImageStoreResponse vrData, final ImageStoreJoinVO vr) {
        return s_imageStoreJoinDao.setImageStoreResponse(vrData, vr);
    }

    public static List<ImageStoreJoinVO> newImageStoreView(final ImageStore vr) {
        return s_imageStoreJoinDao.newImageStoreView(vr);
    }

    public static DomainResponse newDomainResponse(final ResponseView view, final DomainJoinVO ve) {
        return s_domainJoinDao.newDomainResponse(view, ve);
    }

    public static AccountResponse newAccountResponse(final ResponseView view, final AccountJoinVO ve) {
        return s_accountJoinDao.newAccountResponse(view, ve);
    }

    public static AccountJoinVO newAccountView(final Account e) {
        return s_accountJoinDao.newAccountView(e);
    }

    public static AccountJoinVO findAccountViewById(final Long accountId) {
        return s_accountJoinDao.findByIdIncludingRemoved(accountId);
    }

    public static AsyncJobResponse newAsyncJobResponse(final AsyncJobJoinVO ve) {
        return s_jobJoinDao.newAsyncJobResponse(ve);
    }

    public static AsyncJobJoinVO newAsyncJobView(final AsyncJob e) {
        return s_jobJoinDao.newAsyncJobView(e);
    }

    public static DiskOfferingResponse newDiskOfferingResponse(final DiskOfferingJoinVO offering) {
        return s_diskOfferingJoinDao.newDiskOfferingResponse(offering);
    }

    public static DiskOfferingJoinVO newDiskOfferingView(final DiskOffering offering) {
        return s_diskOfferingJoinDao.newDiskOfferingView(offering);
    }

    public static ServiceOfferingResponse newServiceOfferingResponse(final ServiceOfferingJoinVO offering) {
        return s_serviceOfferingJoinDao.newServiceOfferingResponse(offering);
    }

    public static ServiceOfferingJoinVO newServiceOfferingView(final ServiceOffering offering) {
        return s_serviceOfferingJoinDao.newServiceOfferingView(offering);
    }

    public static ZoneResponse newDataCenterResponse(final ResponseView view, final DataCenterJoinVO dc, final Boolean showCapacities) {
        return s_dcJoinDao.newDataCenterResponse(view, dc, showCapacities);
    }

    public static DataCenterJoinVO newDataCenterView(final DataCenter dc) {
        return s_dcJoinDao.newDataCenterView(dc);
    }

    public static Map<String, String> findHostDetailsById(final long hostId) {
        return s_hostDetailsDao.findDetails(hostId);
    }

    public static List<NicSecondaryIpVO> findNicSecondaryIps(final long nicId) {
        return s_nicSecondaryIpDao.listByNicId(nicId);
    }

    public static TemplateResponse newTemplateUpdateResponse(final TemplateJoinVO vr) {
        return s_templateJoinDao.newUpdateResponse(vr);
    }

    public static TemplateResponse newTemplateResponse(final ResponseView view, final TemplateJoinVO vr) {
        return s_templateJoinDao.newTemplateResponse(view, vr);
    }

    public static TemplateResponse newIsoResponse(final TemplateJoinVO vr) {
        return s_templateJoinDao.newIsoResponse(vr);
    }

    public static TemplateResponse fillTemplateDetails(final ResponseView view, final TemplateResponse vrData, final TemplateJoinVO vr) {
        return s_templateJoinDao.setTemplateResponse(view, vrData, vr);
    }

    public static List<TemplateJoinVO> newTemplateView(final VirtualMachineTemplate vr) {
        return s_templateJoinDao.newTemplateView(vr);
    }

    public static List<TemplateJoinVO> newTemplateView(final VirtualMachineTemplate vr, final long zoneId, final boolean readyOnly) {
        return s_templateJoinDao.newTemplateView(vr, zoneId, readyOnly);
    }

    public static AffinityGroup getAffinityGroup(final String groupName, final long accountId) {
        return s_affinityGroupDao.findByAccountAndName(accountId, groupName);
    }

    public static AffinityGroupResponse newAffinityGroupResponse(final AffinityGroupJoinVO group) {
        return s_affinityGroupJoinDao.newAffinityGroupResponse(group);
    }

    public static AffinityGroupResponse fillAffinityGroupDetails(final AffinityGroupResponse resp, final AffinityGroupJoinVO group) {
        return s_affinityGroupJoinDao.setAffinityGroupResponse(resp, group);
    }

    public static Map<String, String> getResourceDetails(final long resourceId, final ResourceObjectType resourceType) {
        final Map<String, String> details;
        if (isAdmin(CallContext.current().getCallingAccount())) {
            details = s_resourceDetailsService.getDetailsMap(resourceId, resourceType, null);
        } else {
            details = s_resourceDetailsService.getDetailsMap(resourceId, resourceType, true);
        }
        return details.isEmpty() ? null : details;
    }

    public static boolean isAdmin(final Account account) {
        return s_accountService.isAdmin(account.getId());
    }

    public static List<ResourceTagJoinVO> listResourceTagViewByResourceUUID(final String resourceUUID, final ResourceObjectType resourceType) {
        return s_tagJoinDao.listBy(resourceUUID, resourceType);
    }

    @PostConstruct
    void init() {
        s_ms = ms;
        s_configMgr = configMgr;
        s_asyncMgr = asyncMgr;
        s_storageMgr = storageMgr;
        s_userVmMgr = userVmMgr;
        s_networkModel = networkModel;
        s_networkMgr = networkMgr;
        s_configSvc = configSvc;
        s_templateMgr = templateMgr;

        s_accountDao = accountDao;
        s_accountGuestVlanMapDao = accountGuestVlanMapDao;
        s_accountVlanMapDao = accountVlanMapDao;
        s_clusterDao = clusterDao;
        s_capacityDao = capacityDao;
        s_dcJoinDao = dcJoinDao;
        s_diskOfferingDao = diskOfferingDao;
        s_diskOfferingJoinDao = diskOfferingJoinDao;
        s_domainDao = domainDao;
        s_domainJoinDao = domainJoinDao;
        s_domainRouterDao = domainRouterDao;
        s_domainRouterJoinDao = domainRouterJoinDao;
        s_guestOSDao = guestOSDao;
        s_guestOSCategoryDao = guestOSCategoryDao;
        s_hostDao = hostDao;
        s_ipAddressDao = ipAddressDao;
        s_loadBalancerDao = loadBalancerDao;
        s_podDao = podDao;
        s_serviceOfferingDao = serviceOfferingDao;
        s_serviceOfferingDetailsDao = serviceOfferingDetailsDao;
        s_serviceOfferingJoinDao = serviceOfferingJoinDao;
        s_snapshotDao = snapshotDao;
        s_storagePoolDao = storagePoolDao;
        s_templateDao = templateDao;
        s_templateDetailsDao = templateDetailsDao;
        s_uploadDao = uploadDao;
        s_userDao = userDao;
        s_userStatsDao = userStatsDao;
        s_userVmDao = userVmDao;
        s_userVmJoinDao = userVmJoinDao;
        s_vlanDao = vlanDao;
        s_volumeDao = volumeDao;
        s_site2SiteVpnGatewayDao = site2SiteVpnGatewayDao;
        s_site2SiteCustomerGatewayDao = site2SiteCustomerGatewayDao;
        s_zoneDao = zoneDao;
        s_networkOfferingDao = networkOfferingDao;
        s_networkDao = networkDao;
        s_physicalNetworkDao = physicalNetworkDao;
        s_configDao = configDao;
        s_consoleProxyDao = consoleProxyDao;
        s_firewallCidrsDao = firewallCidrsDao;
        s_vmDao = vmDao;
        s_resourceLimitMgr = resourceLimitMgr;
        s_projectMgr = projectMgr;
        s_resourceMgr = resourceMgr;
        s_accountDetailsDao = accountDetailsDao;
        s_networkDomainDao = networkDomainDao;
        s_haMgr = haMgr;
        s_vpcMgr = vpcMgr;
        s_taggedResourceService = taggedResourceService;
        s_sshKeyPairDao = sshKeyPairDao;
        s_userVmDetailsDao = userVmDetailsDao;
        s_tagJoinDao = tagJoinDao;
        s_vmGroupJoinDao = vmGroupJoinDao;
        s_eventJoinDao = eventJoinDao;
        s_userAccountJoinDao = userAccountJoinDao;
        s_projectJoinDao = projectJoinDao;
        s_projectAccountJoinDao = projectAccountJoinDao;
        s_projectInvitationJoinDao = projectInvitationJoinDao;
        s_hostJoinDao = hostJoinDao;
        s_volJoinDao = volJoinDao;
        s_poolJoinDao = poolJoinDao;
        s_tagDao = tagDao;
        s_hostTagDao = hosttagDao;
        s_imageStoreJoinDao = imageStoreJoinDao;
        s_accountJoinDao = accountJoinDao;
        s_jobJoinDao = jobJoinDao;
        s_templateJoinDao = templateJoinDao;
        s_physicalNetworkTrafficTypeDao = physicalNetworkTrafficTypeDao;
        s_physicalNetworkServiceProviderDao = physicalNetworkServiceProviderDao;
        s_firewallRuleDao = firewallRuleDao;
        s_staticRouteDao = staticRouteDao;
        s_vpcGatewayDao = vpcGatewayDao;
        s_vpcDao = vpcDao;
        s_vpcOfferingDao = vpcOfferingDao;
        s_asyncJobDao = asyncJobDao;
        s_hostDetailsDao = hostDetailsDao;
        s_clusterDetailsDao = clusterDetailsDao;
        s_vmSnapshotDao = vmSnapshotDao;
        s_nicSecondaryIpDao = nicSecondaryIpDao;
        s_vpcProvSvc = vpcProvSvc;
        s_affinityGroupDao = affinityGroupDao;
        s_affinityGroupJoinDao = affinityGroupJoinDao;
        // Note: stats collector should already have been initialized by this time, otherwise a null instance is returned
        s_statsCollector = StatsCollector.getInstance();
        s_networkACLDao = networkACLDao;
        s_accountService = accountService;
        s_resourceDetailsService = resourceDetailsService;
        s_hostGpuGroupsDao = hostGpuGroupsDao;
        s_vgpuTypesDao = vgpuTypesDao;
        s_zoneRepository = zoneRepository;
    }
}
