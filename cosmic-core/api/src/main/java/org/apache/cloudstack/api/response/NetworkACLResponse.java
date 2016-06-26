package org.apache.cloudstack.api.response;

import com.cloud.network.vpc.NetworkACL;
import com.cloud.serializer.Param;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = NetworkACL.class)
public class NetworkACLResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the ACL")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the Name of the ACL")
    private String name;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "Description of the ACL")
    private String description;

    @SerializedName(ApiConstants.VPC_ID)
    @Param(description = "Id of the VPC this ACL is associated with")
    private String vpcId;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is ACL for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setVpcId(final String vpcId) {
        this.vpcId = vpcId;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
