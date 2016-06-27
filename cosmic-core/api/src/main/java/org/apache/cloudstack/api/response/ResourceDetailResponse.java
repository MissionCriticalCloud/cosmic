package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class ResourceDetailResponse extends BaseResponse {
    @SerializedName(ApiConstants.RESOURCE_ID)
    @Param(description = "ID of the resource")
    private String resourceId;

    @SerializedName(ApiConstants.RESOURCE_TYPE)
    @Param(description = "ID of the resource")
    private String resourceType;

    @SerializedName(ApiConstants.KEY)
    @Param(description = "key of the resource detail")
    private String name;

    @SerializedName(ApiConstants.VALUE)
    @Param(description = "value of the resource detail")
    private String value;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "if detail is returned to the regular user", since = "4.3")
    private boolean forDisplay;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(final String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(final String resourceType) {
        this.resourceType = resourceType;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setForDisplay(final boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
