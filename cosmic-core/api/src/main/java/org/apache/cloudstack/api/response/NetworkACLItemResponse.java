package org.apache.cloudstack.api.response;

import com.cloud.network.vpc.NetworkACLItem;
import com.cloud.serializer.Param;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = NetworkACLItem.class)
public class NetworkACLItemResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the ACL Item")
    private String id;

    @SerializedName(ApiConstants.PROTOCOL)
    @Param(description = "the protocol of the ACL")
    private String protocol;

    @SerializedName(ApiConstants.START_PORT)
    @Param(description = "the starting port of ACL's port range")
    private String startPort;

    @SerializedName(ApiConstants.END_PORT)
    @Param(description = "the ending port of ACL's port range")
    private String endPort;

    @SerializedName(ApiConstants.TRAFFIC_TYPE)
    @Param(description = "the traffic type for the ACL")
    private String trafficType;

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
    @Param(description = "the list of resource tags associated with the network ACLs", responseObject = ResourceTagResponse.class)
    private List<ResourceTagResponse> tags;

    @SerializedName(ApiConstants.ACL_ID)
    @Param(description = "the ID of the ACL this item belongs to")
    private String aclId;

    @SerializedName(ApiConstants.NUMBER)
    @Param(description = "Number of the ACL Item")
    private Integer number;

    @SerializedName(ApiConstants.ACTION)
    @Param(description = "Action of ACL Item. Allow/Deny")
    private String action;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is rule for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    public void setId(final String id) {
        this.id = id;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public void setStartPort(final String startPort) {
        this.startPort = startPort;
    }

    public void setEndPort(final String endPort) {
        this.endPort = endPort;
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

    public void setTrafficType(final String trafficType) {
        this.trafficType = trafficType;
    }

    public void setTags(final List<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void setAclId(final String aclId) {
        this.aclId = aclId;
    }

    public void setNumber(final Integer number) {
        this.number = number;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
