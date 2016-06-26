package org.apache.cloudstack.affinity;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

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
