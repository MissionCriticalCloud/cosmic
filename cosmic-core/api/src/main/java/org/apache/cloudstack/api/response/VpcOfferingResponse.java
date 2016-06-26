package org.apache.cloudstack.api.response;

import com.cloud.network.vpc.VpcOffering;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = VpcOffering.class)
public class VpcOfferingResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the id of the vpc offering")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the vpc offering")
    private String name;

    @SerializedName(ApiConstants.DISPLAY_TEXT)
    @Param(description = "an alternate display text of the vpc offering.")
    private String displayText;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this vpc offering was created")
    private Date created;

    @SerializedName(ApiConstants.IS_DEFAULT)
    @Param(description = "true if vpc offering is default, false otherwise")
    private Boolean isDefault;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "state of the vpc offering. Can be Disabled/Enabled")
    private String state;

    @SerializedName(ApiConstants.SERVICE)
    @Param(description = "the list of supported services", responseObject = ServiceResponse.class)
    private List<ServiceResponse> services;

    @SerializedName(ApiConstants.DISTRIBUTED_VPC_ROUTER)
    @Param(description = " indicates if the vpc offering supports distributed router for one-hop forwarding", since = "4.4")
    private Boolean supportsDistributedRouter;

    @SerializedName((ApiConstants.SUPPORTS_REGION_LEVEL_VPC))
    @Param(description = " indicated if the offering can support region level vpc", since = "4.4")
    private Boolean supportsRegionLevelVpc;

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setIsDefault(final Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setServices(final List<ServiceResponse> services) {
        this.services = services;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setSupportsDistributedRouter(final Boolean supportsDistributedRouter) {
        this.supportsDistributedRouter = supportsDistributedRouter;
    }

    public void setSupportsRegionLevelVpc(final Boolean supports) {
        this.supportsRegionLevelVpc = supports;
    }
}
