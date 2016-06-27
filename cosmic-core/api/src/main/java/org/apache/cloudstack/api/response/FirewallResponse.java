package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class FirewallResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the firewall rule")
    private String id;

    @SerializedName(ApiConstants.PROTOCOL)
    @Param(description = "the protocol of the firewall rule")
    private String protocol;

    @SerializedName(ApiConstants.START_PORT)
    @Param(description = "the starting port of firewall rule's port range")
    private Integer startPort;

    @SerializedName(ApiConstants.END_PORT)
    @Param(description = "the ending port of firewall rule's port range")
    private Integer endPort;

    @SerializedName(ApiConstants.IP_ADDRESS_ID)
    @Param(description = "the public ip address id for the firewall rule")
    private String publicIpAddressId;

    @SerializedName(ApiConstants.NETWORK_ID)
    @Param(description = "the network id of the firewall rule")
    private String networkId;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "the public ip address for the firewall rule")
    private String publicIpAddress;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the rule")
    private String state;

    @SerializedName(ApiConstants.CIDR_LIST)
    @Param(description = "the cidr list to forward traffic from")
    private String cidrList;

    @SerializedName(ApiConstants.ICMP_TYPE)
    @Param(description = "type of the icmp message being sent")
    private Integer icmpType;

    @SerializedName(ApiConstants.ICMP_CODE)
    @Param(description = "error code for this icmp message")
    private Integer icmpCode;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with the rule", responseObject = ResourceTagResponse.class)
    private List<ResourceTagResponse> tags;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is rule for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    public void setId(final String id) {
        this.id = id;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public void setStartPort(final Integer startPort) {
        this.startPort = startPort;
    }

    public void setEndPort(final Integer endPort) {
        this.endPort = endPort;
    }

    public void setPublicIpAddressId(final String publicIpAddressId) {
        this.publicIpAddressId = publicIpAddressId;
    }

    public void setPublicIpAddress(final String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public void setNetworkId(final String networkId) {
        this.networkId = networkId;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setCidrList(final String cidrList) {
        this.cidrList = cidrList;
    }

    public void setIcmpType(final Integer icmpType) {
        this.icmpType = icmpType;
    }

    public void setIcmpCode(final Integer icmpCode) {
        this.icmpCode = icmpCode;
    }

    public void setTags(final List<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
