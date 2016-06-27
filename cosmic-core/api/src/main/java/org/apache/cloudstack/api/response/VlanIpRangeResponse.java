package org.apache.cloudstack.api.response;

import com.cloud.dc.Vlan;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Vlan.class)
public class VlanIpRangeResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the VLAN IP range")
    private String id;

    @SerializedName("forvirtualnetwork")
    @Param(description = "the virtual network for the VLAN IP range")
    private Boolean forVirtualNetwork;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the Zone ID of the VLAN IP range")
    private String zoneId;

    @SerializedName(ApiConstants.VLAN)
    @Param(description = "the ID or VID of the VLAN.")
    private String vlan;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account of the VLAN IP range")
    private String accountName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the VLAN IP range")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the VLAN IP range")
    private String domainName;

    @SerializedName(ApiConstants.POD_ID)
    @Param(description = "the Pod ID for the VLAN IP range")
    private String podId;

    @SerializedName("podname")
    @Param(description = "the Pod name for the VLAN IP range")
    private String podName;

    @SerializedName(ApiConstants.GATEWAY)
    @Param(description = "the gateway of the VLAN IP range")
    private String gateway;

    @SerializedName(ApiConstants.NETMASK)
    @Param(description = "the netmask of the VLAN IP range")
    private String netmask;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "the description of the VLAN IP range")
    private String description;

    @SerializedName(ApiConstants.START_IP)
    @Param(description = "the start ip of the VLAN IP range")
    private String startIp;

    @SerializedName(ApiConstants.END_IP)
    @Param(description = "the end ip of the VLAN IP range")
    private String endIp;

    @SerializedName(ApiConstants.NETWORK_ID)
    @Param(description = "the network id of vlan range")
    private String networkId;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the vlan range")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the vlan range")
    private String projectName;

    @SerializedName(ApiConstants.PHYSICAL_NETWORK_ID)
    @Param(description = "the physical network this belongs to")
    private String physicalNetworkId;

    @SerializedName(ApiConstants.START_IPV6)
    @Param(description = "the start ipv6 of the VLAN IP range")
    private String startIpv6;

    @SerializedName(ApiConstants.END_IPV6)
    @Param(description = "the end ipv6 of the VLAN IP range")
    private String endIpv6;

    @SerializedName(ApiConstants.IP6_GATEWAY)
    @Param(description = "the gateway of IPv6 network")
    private String ip6Gateway;

    @SerializedName(ApiConstants.IP6_CIDR)
    @Param(description = "the cidr of IPv6 network")
    private String ip6Cidr;

    public void setId(final String id) {
        this.id = id;
    }

    public void setForVirtualNetwork(final Boolean forVirtualNetwork) {
        this.forVirtualNetwork = forVirtualNetwork;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public void setVlan(final String vlan) {
        this.vlan = vlan;
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
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setPodId(final String podId) {
        this.podId = podId;
    }

    public void setPodName(final String podName) {
        this.podName = podName;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setStartIp(final String startIp) {
        this.startIp = startIp;
    }

    public void setEndIp(final String endIp) {
        this.endIp = endIp;
    }

    public void setNetworkId(final String networkId) {
        this.networkId = networkId;
    }

    public void setPhysicalNetworkId(final String physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public String getphysicalNetworkId() {
        return physicalNetworkId;
    }

    public String getStartIpv6() {
        return startIpv6;
    }

    public void setStartIpv6(final String startIpv6) {
        this.startIpv6 = startIpv6;
    }

    public void setEndIpv6(final String endIpv6) {
        this.endIpv6 = endIpv6;
    }

    public void setIp6Gateway(final String ip6Gateway) {
        this.ip6Gateway = ip6Gateway;
    }

    public void setIp6Cidr(final String ip6Cidr) {
        this.ip6Cidr = ip6Cidr;
    }
}
