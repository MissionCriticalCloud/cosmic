package com.cloud.api;

public enum APICommandGroup {
    AccountService("Account"),
    AffinityGroupService("Affinity Group"),
    AlertService("Alert"),
    AsyncjobService("Async job"),
    AuthenticationService("Authentication"),
    AutoScaleService("AutoScale"),
    BaseCmdTestService("BaseCmdTest"),
    CertificateService("Certificate"),
    CloudOpsService("CloudOps"),
    ClusterService("Cluster"),
    ConfigurationService("Configuration"),
    DiskOfferingService("Disk Offering"),
    DomainService("Domain"),
    EventService("Event"),
    FirewallService("Firewall"),
    GuestOSService("Guest OS"),
    HostService("Host"),
    HypervisorService("Hypervisor"),
    ImageStoreService("Image Store"),
    InternalLBService("Internal LB"),
    ISOService("ISO"),
    LimitService("Limit"),
    LoadBalancerService("Load Balancer"),
    NATService("NAT"),
    NetworkACLService("Network ACL"),
    NetworkDeviceService("Network Device"),
    NetworkOfferingService("Network Offering"),
    NetworkService("Network"),
    NiciraNVPService("Nicira NVP"),
    NicService("Nic"),
    PodService("Pod"),
    PortableIPService("Portable IP"),
    ProjectService("Project"),
    PublicIPAddressService("Public IP Address"),
    RegionService("Region"),
    ResourcemetadataService("Resource metadata"),
    ResourcetagsService("Resource tags"),
    RouterService("Router"),
    ServiceOfferingService("Service Offering"),
    SnapshotService("Snapshot"),
    SSHService("SSH"),
    StoragePoolService("Storage Pool"),
    SwiftService("Swift"),
    SystemService("System"),
    SystemVMService("System VM"),
    TemplateService("Template"),
    UsageService("Usage"),
    UserService("User"),
    VirtualMachineService("Virtual Machine"),
    VLANService("VLAN"),
    VMGroupService("VM Group"),
    VolumeService("Volume"),
    VPCService("VPC"),
    VPNService("VPN"),
    ZoneService("Zone");

    private String description;

    APICommandGroup(String description) {

        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}