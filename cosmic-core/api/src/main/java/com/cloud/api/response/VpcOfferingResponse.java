package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.network.vpc.VpcOffering;
import com.cloud.serializer.Param;

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

    @SerializedName(ApiConstants.SERVICE_OFFERING_ID)
    @Param(description = "The primary system compute offering id used for the virtual router")
    private String serviceOfferingId;

    @SerializedName(ApiConstants.SERVICE_OFFERING_NAME)
    @Param(description = "The primary system compute offering name used for the virtual router")
    private String serviceOfferingName;

    @SerializedName(ApiConstants.SECONDARY_SERVICE_OFFERING_ID)
    @Param(description = "The secondary system compute offering id used for the virtual router")
    private String secondaryServiceOfferingId;

    @SerializedName(ApiConstants.SECONDARY_SERVICE_OFFERING_NAME)
    @Param(description = "The secondary system compute offering name used for the virtual router")
    private String secondaryServiceOfferingName;

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

    public String getServiceOfferingId() {
        return serviceOfferingId;
    }

    public void setServiceOfferingId(final String serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    public String getServiceOfferingName() {
        return serviceOfferingName;
    }

    public void setServiceOfferingName(final String serviceOfferingName) {
        this.serviceOfferingName = serviceOfferingName;
    }

    public String getSecondaryServiceOfferingId() {
        return secondaryServiceOfferingId;
    }

    public void setSecondaryServiceOfferingId(final String secondaryServiceOfferingId) {
        this.secondaryServiceOfferingId = secondaryServiceOfferingId;
    }

    public String getSecondaryServiceOfferingName() {
        return secondaryServiceOfferingName;
    }

    public void setSecondaryServiceOfferingName(final String secondaryServiceOfferingName) {
        this.secondaryServiceOfferingName = secondaryServiceOfferingName;
    }
}
