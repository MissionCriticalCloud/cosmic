package com.cloud.affinity;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = AffinityGroup.class)
public class AffinityGroupTypeResponse extends BaseResponse {

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "the type of the affinity group")
    private String type;

    public AffinityGroupTypeResponse() {
    }

    public void setType(final String type) {
        this.type = type;
    }
}
