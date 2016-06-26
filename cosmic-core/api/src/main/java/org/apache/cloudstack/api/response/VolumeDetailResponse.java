package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class VolumeDetailResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "ID of the volume")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "name of the volume detail")
    private String name;

    @SerializedName(ApiConstants.VALUE)
    @Param(description = "value of the volume detail")
    private String value;

    @SerializedName(ApiConstants.DISPLAY_VOLUME)
    @Param(description = "an optional field whether to the display the volume to the end user or not.")
    private Boolean displayVm;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getName() {

        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Boolean getDisplayVm() {
        return displayVm;
    }

    public void setDisplayVm(final Boolean displayVm) {
        this.displayVm = displayVm;
    }
}
