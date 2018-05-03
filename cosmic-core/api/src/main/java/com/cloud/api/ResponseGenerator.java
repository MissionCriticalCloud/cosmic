package com.cloud.api;

import com.cloud.affinity.AffinityGroup;
import com.cloud.affinity.AffinityGroupResponse;
import com.cloud.api.ApiConstants.HostDetails;
import com.cloud.api.ApiConstants.VMDetails;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.command.user.job.QueryAsyncJobResultCmd;
import com.cloud.api.response.AccountResponse;
import com.cloud.api.response.AsyncJobResponse;
import com.cloud.api.response.CapacityResponse;
import com.cloud.api.response.ClusterResponse;
import com.cloud.api.response.ConfigurationResponse;
import com.cloud.api.response.CreateCmdResponse;
import com.cloud.api.response.DiskOfferingResponse;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.DomainRouterResponse;
import com.cloud.api.response.ExtractResponse;
import com.cloud.api.response.FirewallResponse;
import com.cloud.api.response.FirewallRuleResponse;
import com.cloud.api.response.GuestOSResponse;
import com.cloud.api.response.GuestOsMappingResponse;
import com.cloud.api.response.GuestVlanRangeResponse;
import com.cloud.api.response.HostForMigrationResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.HypervisorCapabilitiesResponse;
import com.cloud.api.response.IPAddressResponse;
import com.cloud.api.response.ImageStoreResponse;
import com.cloud.api.response.InstanceGroupResponse;
import com.cloud.api.response.IpForwardingRuleResponse;
import com.cloud.api.response.IsolationMethodResponse;
import com.cloud.api.response.LBHealthCheckResponse;
import com.cloud.api.response.LBStickinessResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.LoadBalancerResponse;
import com.cloud.api.response.NetworkACLItemResponse;
import com.cloud.api.response.NetworkACLResponse;
import com.cloud.api.response.NetworkOfferingResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.api.response.NicResponse;
import com.cloud.api.response.NicSecondaryIpResponse;
import com.cloud.api.response.PhysicalNetworkResponse;
import com.cloud.api.response.PodResponse;
import com.cloud.api.response.PrivateGatewayResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.ProviderResponse;
import com.cloud.api.response.RegionResponse;
import com.cloud.api.response.RemoteAccessVpnResponse;
import com.cloud.api.response.ResourceCountResponse;
import com.cloud.api.response.ResourceLimitResponse;
import com.cloud.api.response.ResourceTagResponse;
import com.cloud.api.response.SSHKeyPairResponse;
import com.cloud.api.response.ServiceOfferingResponse;
import com.cloud.api.response.ServiceResponse;
import com.cloud.api.response.Site2SiteCustomerGatewayResponse;
import com.cloud.api.response.Site2SiteVpnConnectionResponse;
import com.cloud.api.response.Site2SiteVpnGatewayResponse;
import com.cloud.api.response.SnapshotResponse;
import com.cloud.api.response.StaticRouteResponse;
import com.cloud.api.response.StorageNetworkIpRangeResponse;
import com.cloud.api.response.StoragePoolResponse;
import com.cloud.api.response.SystemVmResponse;
import com.cloud.api.response.TemplatePermissionsResponse;
import com.cloud.api.response.TemplateResponse;
import com.cloud.api.response.TrafficTypeResponse;
import com.cloud.api.response.UpgradeRouterTemplateResponse;
import com.cloud.api.response.UserResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.response.VMSnapshotResponse;
import com.cloud.api.response.VirtualRouterProviderResponse;
import com.cloud.api.response.VlanIpRangeResponse;
import com.cloud.api.response.VolumeResponse;
import com.cloud.api.response.VpcOfferingResponse;
import com.cloud.api.response.VpcResponse;
import com.cloud.api.response.VpnUsersResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.capacity.Capacity;
import com.cloud.config.Configuration;
import com.cloud.hypervisor.HypervisorCapabilities;
import com.cloud.legacymodel.configuration.ResourceCount;
import com.cloud.legacymodel.configuration.ResourceLimit;
import com.cloud.legacymodel.dc.Cluster;
import com.cloud.legacymodel.dc.DataCenter;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.Pod;
import com.cloud.legacymodel.dc.StorageNetworkIpRange;
import com.cloud.legacymodel.dc.Vlan;
import com.cloud.legacymodel.domain.Domain;
import com.cloud.legacymodel.network.FirewallRule;
import com.cloud.legacymodel.network.LoadBalancer;
import com.cloud.legacymodel.network.Network;
import com.cloud.legacymodel.network.Network.Service;
import com.cloud.legacymodel.network.Nic;
import com.cloud.legacymodel.network.PortForwardingRule;
import com.cloud.legacymodel.network.StaticNatRule;
import com.cloud.legacymodel.network.VirtualRouter;
import com.cloud.legacymodel.network.VpnUser;
import com.cloud.legacymodel.network.vpc.NetworkACL;
import com.cloud.legacymodel.network.vpc.NetworkACLItem;
import com.cloud.legacymodel.network.vpc.PrivateGateway;
import com.cloud.legacymodel.network.vpc.StaticRoute;
import com.cloud.legacymodel.network.vpc.Vpc;
import com.cloud.legacymodel.network.vpc.VpcOffering;
import com.cloud.legacymodel.storage.DiskOffering;
import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.legacymodel.storage.VMSnapshot;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.SSHKeyPair;
import com.cloud.legacymodel.user.User;
import com.cloud.legacymodel.user.UserAccount;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.network.GuestVlan;
import com.cloud.network.IpAddress;
import com.cloud.network.Networks.IsolationType;
import com.cloud.network.PhysicalNetwork;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.network.PhysicalNetworkTrafficType;
import com.cloud.network.RemoteAccessVpn;
import com.cloud.network.Site2SiteCustomerGateway;
import com.cloud.network.Site2SiteVpnConnection;
import com.cloud.network.Site2SiteVpnGateway;
import com.cloud.network.VirtualRouterProvider;
import com.cloud.network.rules.HealthCheckPolicy;
import com.cloud.network.rules.StickinessPolicy;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.projects.Project;
import com.cloud.region.Region;
import com.cloud.server.ResourceTag;
import com.cloud.storage.GuestOS;
import com.cloud.storage.GuestOSHypervisor;
import com.cloud.storage.ImageStore;
import com.cloud.storage.Snapshot;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.uservm.UserVm;
import com.cloud.vm.InstanceGroup;
import com.cloud.vm.NicSecondaryIp;

import java.text.DecimalFormat;
import java.util.EnumSet;
import java.util.List;

public interface ResponseGenerator {
    UserResponse createUserResponse(UserAccount user);

    AccountResponse createAccountResponse(ResponseView view, Account account);

    DomainResponse createDomainResponse(Domain domain);

    DiskOfferingResponse createDiskOfferingResponse(DiskOffering offering);

    ResourceLimitResponse createResourceLimitResponse(ResourceLimit limit);

    ResourceCountResponse createResourceCountResponse(ResourceCount resourceCount);

    ServiceOfferingResponse createServiceOfferingResponse(ServiceOffering offering);

    ConfigurationResponse createConfigurationResponse(Configuration cfg);

    SnapshotResponse createSnapshotResponse(Snapshot snapshot);

    List<UserVmResponse> createUserVmResponse(ResponseView view, String objectName, UserVm... userVms);

    List<UserVmResponse> createUserVmResponse(ResponseView view, String objectName, EnumSet<VMDetails> details, UserVm... userVms);

    SystemVmResponse createSystemVmResponse(VirtualMachine systemVM);

    DomainRouterResponse createDomainRouterResponse(VirtualRouter router);

    HostResponse createHostResponse(Host host, EnumSet<HostDetails> details);

    HostResponse createHostResponse(Host host);

    HostForMigrationResponse createHostForMigrationResponse(Host host);

    HostForMigrationResponse createHostForMigrationResponse(Host host, EnumSet<HostDetails> details);

    VlanIpRangeResponse createVlanIpRangeResponse(Vlan vlan);

    IPAddressResponse createIPAddressResponse(ResponseView view, IpAddress ipAddress);

    GuestVlanRangeResponse createDedicatedGuestVlanRangeResponse(GuestVlan result);

    LoadBalancerResponse createLoadBalancerResponse(LoadBalancer loadBalancer);

    LBStickinessResponse createLBStickinessPolicyResponse(List<? extends StickinessPolicy> stickinessPolicies, LoadBalancer lb);

    LBStickinessResponse createLBStickinessPolicyResponse(StickinessPolicy stickinessPolicy, LoadBalancer lb);

    LBHealthCheckResponse createLBHealthCheckPolicyResponse(List<? extends HealthCheckPolicy> healthcheckPolicies, LoadBalancer lb);

    LBHealthCheckResponse createLBHealthCheckPolicyResponse(HealthCheckPolicy healthcheckPolicy, LoadBalancer lb);

    PodResponse createPodResponse(Pod pod, Boolean showCapacities);

    ZoneResponse createZoneResponse(ResponseView view, DataCenter dataCenter, Boolean showCapacities);

    VolumeResponse createVolumeResponse(ResponseView view, Volume volume);

    InstanceGroupResponse createInstanceGroupResponse(InstanceGroup group);

    StoragePoolResponse createStoragePoolResponse(StoragePool pool);

    StoragePoolResponse createStoragePoolForMigrationResponse(StoragePool pool);

    ClusterResponse createClusterResponse(Cluster cluster, Boolean showCapacities);

    FirewallRuleResponse createPortForwardingRuleResponse(PortForwardingRule fwRule);

    IpForwardingRuleResponse createIpForwardingRuleResponse(StaticNatRule fwRule);

    User findUserById(Long userId);

    UserVm findUserVmById(Long vmId);

    Volume findVolumeById(Long volumeId);

    Account findAccountByNameDomain(String accountName, Long domainId);

    VirtualMachineTemplate findTemplateById(Long templateId);

    VpnUsersResponse createVpnUserResponse(VpnUser user);

    RemoteAccessVpnResponse createRemoteAccessVpnResponse(RemoteAccessVpn vpn);

    List<TemplateResponse> createTemplateResponses(ResponseView view, long templateId, Long zoneId, boolean readyOnly);

    List<TemplateResponse> createTemplateResponses(ResponseView view, long templateId, Long snapshotId, Long volumeId, boolean readyOnly);

    ExtractResponse createExtractResponse(Long id, Long zoneId, Long accountId, String mode, String url);

    String toSerializedString(CreateCmdResponse response, String responseType);

    TemplateResponse createTemplateUpdateResponse(ResponseView view, VirtualMachineTemplate result);

    List<TemplateResponse> createTemplateResponses(ResponseView view, VirtualMachineTemplate result, Long zoneId, boolean readyOnly);

    List<CapacityResponse> createCapacityResponse(List<? extends Capacity> result, DecimalFormat format);

    TemplatePermissionsResponse createTemplatePermissionsResponse(ResponseView view, List<String> accountNames, Long id);

    AsyncJobResponse queryJobResult(QueryAsyncJobResultCmd cmd);

    NetworkOfferingResponse createNetworkOfferingResponse(NetworkOffering offering);

    NetworkResponse createNetworkResponse(ResponseView view, Network network);

    UserResponse createUserResponse(User user);

    AccountResponse createUserAccountResponse(ResponseView view, UserAccount user);

    List<TemplateResponse> createIsoResponses(ResponseView view, VirtualMachineTemplate iso, Long zoneId, boolean readyOnly);

    ProjectResponse createProjectResponse(Project project);

    FirewallResponse createFirewallResponse(FirewallRule fwRule);

    HypervisorCapabilitiesResponse createHypervisorCapabilitiesResponse(HypervisorCapabilities hpvCapabilities);

    PhysicalNetworkResponse createPhysicalNetworkResponse(PhysicalNetwork result);

    ServiceResponse createNetworkServiceResponse(Service service);

    ProviderResponse createNetworkServiceProviderResponse(PhysicalNetworkServiceProvider result);

    TrafficTypeResponse createTrafficTypeResponse(PhysicalNetworkTrafficType result);

    VirtualRouterProviderResponse createVirtualRouterProviderResponse(VirtualRouterProvider result);

    StorageNetworkIpRangeResponse createStorageNetworkIpRangeResponse(StorageNetworkIpRange result);

    RegionResponse createRegionResponse(Region region);

    ImageStoreResponse createImageStoreResponse(ImageStore os);

    ResourceTagResponse createResourceTagResponse(ResourceTag resourceTag, boolean keyValueOnly);

    Site2SiteVpnGatewayResponse createSite2SiteVpnGatewayResponse(Site2SiteVpnGateway result);

    VpcOfferingResponse createVpcOfferingResponse(VpcOffering offering);

    VpcResponse createVpcResponse(ResponseView view, Vpc vpc);

    NetworkACLItemResponse createNetworkACLItemResponse(NetworkACLItem networkACLItem);

    NetworkACLResponse createNetworkACLResponse(NetworkACL networkACL);

    PrivateGatewayResponse createPrivateGatewayResponse(PrivateGateway result);

    StaticRouteResponse createStaticRouteResponse(StaticRoute result);

    Site2SiteCustomerGatewayResponse createSite2SiteCustomerGatewayResponse(Site2SiteCustomerGateway result);

    Site2SiteVpnConnectionResponse createSite2SiteVpnConnectionResponse(Site2SiteVpnConnection result);

    GuestOSResponse createGuestOSResponse(GuestOS os);

    GuestOsMappingResponse createGuestOSMappingResponse(GuestOSHypervisor osHypervisor);

    VMSnapshotResponse createVMSnapshotResponse(VMSnapshot vmSnapshot);

    NicSecondaryIpResponse createSecondaryIPToNicResponse(NicSecondaryIp result);

    NicResponse createNicResponse(Nic result);

    AffinityGroupResponse createAffinityGroupResponse(AffinityGroup group);

    Long getAffinityGroupId(String name, long entityOwnerId);

    IsolationMethodResponse createIsolationMethodResponse(IsolationType method);

    ListResponse<UpgradeRouterTemplateResponse> createUpgradeRouterTemplateResponse(List<Long> jobIds);

    SSHKeyPairResponse createSSHKeyPairResponse(SSHKeyPair sshkeyPair, boolean privatekey);
}
