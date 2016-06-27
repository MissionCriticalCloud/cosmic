package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class CapabilityResponse extends BaseResponse {

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the capability name")
    private String name;

    @SerializedName(ApiConstants.VALUE)
    @Param(description = "the capability value")
    private String value;

    @SerializedName(ApiConstants.CAN_CHOOSE_SERVICE_CAPABILITY)
    @Param(description = "can this service capability value can be choosable while creatine network offerings")
    private boolean canChoose;

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

    public boolean getCanChoose() {
        return canChoose;
    }

    public void setCanChoose(final boolean choosable) {
        this.canChoose = choosable;
    }
}
