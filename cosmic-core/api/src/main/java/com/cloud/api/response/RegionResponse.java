package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.api.EntityReference;
import com.cloud.region.Region;
import com.cloud.serializer.Param;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Region.class)
public class RegionResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the region")
    private Integer id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the region")
    private String name;

    @SerializedName(ApiConstants.END_POINT)
    @Param(description = "the end point of the region")
    private String endPoint;

    @SerializedName("portableipserviceenabled")
    @Param(description = "true if security groups support is enabled, false otherwise")
    private boolean portableipServiceEnabled;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(final String endPoint) {
        this.endPoint = endPoint;
    }

    public void setPortableipServiceEnabled(final boolean portableipServiceEnabled) {
        this.portableipServiceEnabled = portableipServiceEnabled;
    }
}
