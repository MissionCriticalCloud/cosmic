package com.cloud.api.response;

import com.cloud.acl.RoleType;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.network.Network;
import com.cloud.projects.ProjectAccount;
import com.cloud.serializer.Param;

import java.util.List;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = {Network.class, ProjectAccount.class})
public class NetworkResponse extends BaseResponse implements ControlledEntityResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the id of the network")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the network")
    private String name;

    @SerializedName(ApiConstants.DISPLAY_TEXT)
    @Param(description = "the displaytext of the network")
    private String displaytext;

    @SerializedName("broadcastdomaintype")
    @Param(description = "Broadcast domain type of the network")
    private String broadcastDomainType;

    @SerializedName(ApiConstants.TRAFFIC_TYPE)
    @Param(description = "the traffic type of the network")
    private String trafficType;

    @SerializedName(ApiConstants.GATEWAY)
    @Param(description = "the network's gateway")
    private String gateway;

    @SerializedName(ApiConstants.NETMASK)
    @Param(description = "the network's netmask")
    private String netmask;

    @SerializedName(ApiConstants.CIDR)
    @Param(description = "Cloudstack managed address space, all CloudStack managed VMs get IP address from CIDR")
    private String cidr;

    @SerializedName(ApiConstants.NETWORK_CIDR)
    @Param(description = "the network CIDR of the guest network configured with IP reservation. It is the summation of CIDR and RESERVED_IP_RANGE")
    private String networkCidr;

    @SerializedName(ApiConstants.RESERVED_IP_RANGE)
    @Param(description = "the network's IP range not to be used by CloudStack guest VMs and can be used for non CloudStack purposes")
    private String reservedIpRange;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "zone id of the network")
    private String zoneId;

    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the name of the zone the network belongs to")
    private String zoneName;

    @SerializedName("networkofferingid")
    @Param(description = "network offering id the network is created from")
    private String networkOfferingId;

    @SerializedName("networkofferingname")
    @Param(description = "name of the network offering the network is created from")
    private String networkOfferingName;

    @SerializedName("networkofferingdisplaytext")
    @Param(description = "display text of the network offering the network is created from")
    private String networkOfferingDisplayText;

    @SerializedName("networkofferingconservemode")
    @Param(description = "true if network offering is ip conserve mode enabled")
    private Boolean networkOfferingConserveMode;

    @SerializedName("networkofferingavailability")
    @Param(description = "availability of the network offering the network is created from")
    private String networkOfferingAvailability;

    @SerializedName(ApiConstants.IS_SYSTEM)
    @Param(description = "true if network is system, false otherwise")
    private Boolean isSystem;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "state of the network")
    private String state;

    @SerializedName("related")
    @Param(description = "related to what other network configuration")
    private String related;

    @SerializedName("broadcasturi")
    @Param(description = "broadcast uri of the network. This parameter is visible to ROOT admins only")
    private String broadcastUri;

    @SerializedName(ApiConstants.DNS1)
    @Param(description = "the first DNS for the network")
    private String dns1;

    @SerializedName(ApiConstants.DNS2)
    @Param(description = "the second DNS for the network")
    private String dns2;

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "the type of the network")
    private String type;

    @SerializedName(ApiConstants.VLAN)
    @Param(description = "The vlan of the network. This parameter is visible to ROOT admins only")
    private String vlan;

    @SerializedName(ApiConstants.ACL_TYPE)
    @Param(description = "acl type - access type to the network")
    private String aclType;

    @SerializedName(ApiConstants.SUBDOMAIN_ACCESS)
    @Param(description = "true if users from subdomains can access the domain level network")
    private Boolean subdomainAccess;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the owner of the network")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the ipaddress")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the address")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain id of the network owner")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the network owner")
    private String domain;

    @SerializedName("isdefault")
    @Param(description = "true if network is default, false otherwise")
    private Boolean isDefault;

    @SerializedName("service")
    @Param(description = "the list of services", responseObject = ServiceResponse.class)
    private List<ServiceResponse> services;

    @SerializedName(ApiConstants.NETWORK_DOMAIN)
    @Param(description = "the network domain")
    private String networkDomain;

    @SerializedName(ApiConstants.PHYSICAL_NETWORK_ID)
    @Param(description = "the physical network id")
    private String physicalNetworkId;

    @SerializedName(ApiConstants.RESTART_REQUIRED)
    @Param(description = "true network requires restart")
    private Boolean restartRequired;

    @SerializedName(ApiConstants.SPECIFY_IP_RANGES)
    @Param(description = "true if network supports specifying ip ranges, false otherwise")
    private Boolean specifyIpRanges;

    @SerializedName(ApiConstants.VPC_ID)
    @Param(description = "VPC the network belongs to")
    private String vpcId;

    @SerializedName(ApiConstants.CAN_USE_FOR_DEPLOY)
    @Param(description = "list networks available for vm deployment")
    private Boolean canUseForDeploy;

    @SerializedName(ApiConstants.IS_PERSISTENT)
    @Param(description = "list networks that are persistent")
    private Boolean isPersistent;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with network", responseObject = ResourceTagResponse.class)
    private List<ResourceTagResponse> tags;

    @SerializedName(ApiConstants.IP6_GATEWAY)
    @Param(description = "the gateway of IPv6 network")
    private String ip6Gateway;

    @SerializedName(ApiConstants.IP6_CIDR)
    @Param(description = "the cidr of IPv6 network")
    private String ip6Cidr;

    @SerializedName(ApiConstants.IP_EXCLUSION_LIST)
    @Param(description = "list of ip addresses and/or ranges of addresses to be excluded from the network for assignment")
    private String ipExclusionList;

    @SerializedName(ApiConstants.DISPLAY_NETWORK)
    @Param(description = "an optional field, whether to the display the network to the end user or not.", authorized = {RoleType.Admin})
    private Boolean displayNetwork;

    @SerializedName(ApiConstants.ACL_ID)
    @Param(description = "ACL Id associated with the VPC network")
    private String aclId;

    @SerializedName(ApiConstants.STRECHED_L2_SUBNET)
    @Param(description = "true if network can span multiple zones", since = "4.4")
    private Boolean strechedL2Subnet;

    @SerializedName(ApiConstants.NETWORK_SPANNED_ZONES)
    @Param(description = "If a network is enabled for 'streched l2 subnet' then represents zones on which network currently spans", since = "4.4")
    private Set<String> networkSpannedZones;

    public Boolean getDisplayNetwork() {
        return displayNetwork;
    }

    public void setDisplayNetwork(final Boolean displayNetwork) {
        this.displayNetwork = displayNetwork;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setBroadcastDomainType(final String broadcastDomainType) {
        this.broadcastDomainType = broadcastDomainType;
    }

    public void setTrafficType(final String trafficType) {
        this.trafficType = trafficType;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public void setNetworkOfferingId(final String networkOfferingId) {
        this.networkOfferingId = networkOfferingId;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setRelated(final String related) {
        this.related = related;
    }

    public void setBroadcastUri(final String broadcastUri) {
        this.broadcastUri = broadcastUri;
    }

    public void setDns1(final String dns1) {
        this.dns1 = dns1;
    }

    public void setDns2(final String dns2) {
        this.dns2 = dns2;
    }

    public void setIpExclusionList(final String ipExclusionList) {
        this.ipExclusionList = ipExclusionList;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Override
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    @Override
    public void setDomainName(final String domain) {
        this.domain = domain;
    }

    public void setNetworkOfferingName(final String networkOfferingName) {
        this.networkOfferingName = networkOfferingName;
    }

    public void setNetworkOfferingDisplayText(final String networkOfferingDisplayText) {
        this.networkOfferingDisplayText = networkOfferingDisplayText;
    }

    public void setNetworkOfferingConserveMode(final Boolean networkOfferingConserveMode) {
        this.networkOfferingConserveMode = networkOfferingConserveMode;
    }

    public void setDisplaytext(final String displaytext) {
        this.displaytext = displaytext;
    }

    public void setVlan(final String vlan) {
        this.vlan = vlan;
    }

    public void setIsSystem(final Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public void setNetworkOfferingAvailability(final String networkOfferingAvailability) {
        this.networkOfferingAvailability = networkOfferingAvailability;
    }

    public void setServices(final List<ServiceResponse> services) {
        this.services = services;
    }

    public void setIsDefault(final Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setNetworkDomain(final String networkDomain) {
        this.networkDomain = networkDomain;
    }

    public void setPhysicalNetworkId(final String physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public void setAclType(final String aclType) {
        this.aclType = aclType;
    }

    public void setSubdomainAccess(final Boolean subdomainAccess) {
        this.subdomainAccess = subdomainAccess;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
    }

    public void setCidr(final String cidr) {
        this.cidr = cidr;
    }

    public void setNetworkCidr(final String networkCidr) {
        this.networkCidr = networkCidr;
    }

    public void setReservedIpRange(final String reservedIpRange) {
        this.reservedIpRange = reservedIpRange;
    }

    public void setRestartRequired(final Boolean restartRequired) {
        this.restartRequired = restartRequired;
    }

    public void setSpecifyIpRanges(final Boolean specifyIpRanges) {
        this.specifyIpRanges = specifyIpRanges;
    }

    public void setVpcId(final String vpcId) {
        this.vpcId = vpcId;
    }

    public void setCanUseForDeploy(final Boolean canUseForDeploy) {
        this.canUseForDeploy = canUseForDeploy;
    }

    public void setIsPersistent(final Boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    public void setTags(final List<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void setIp6Gateway(final String ip6Gateway) {
        this.ip6Gateway = ip6Gateway;
    }

    public void setIp6Cidr(final String ip6Cidr) {
        this.ip6Cidr = ip6Cidr;
    }

    public String getAclId() {
        return aclId;
    }

    public void setAclId(final String aclId) {
        this.aclId = aclId;
    }

    public void setStrechedL2Subnet(final Boolean strechedL2Subnet) {
        this.strechedL2Subnet = strechedL2Subnet;
    }

    public void setNetworkSpannedZones(final Set<String> networkSpannedZones) {
        this.networkSpannedZones = networkSpannedZones;
    }
}
