package org.apache.cloudstack.api.response;

import com.cloud.network.GuestVlan;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = GuestVlan.class)
public class GuestVlanRangeResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the guest VLAN range")
    private String id;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account of the guest VLAN range")
    private String accountName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the guest VLAN range")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the guest VLAN range")
    private String domainName;

    @SerializedName(ApiConstants.GUEST_VLAN_RANGE)
    @Param(description = "the guest VLAN range")
    private String guestVlanRange;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the guest vlan range")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the guest vlan range")
    private String projectName;

    @SerializedName(ApiConstants.PHYSICAL_NETWORK_ID)
    @Param(description = "the physical network of the guest vlan range")
    private Long physicalNetworkId;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the zone of the guest vlan range")
    private Long zoneId;

    public void setId(final String id) {
        this.id = id;
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

    public void setGuestVlanRange(final String guestVlanRange) {
        this.guestVlanRange = guestVlanRange;
    }

    public void setPhysicalNetworkId(final Long physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public void setZoneId(final Long zoneId) {
        this.zoneId = zoneId;
    }
}
