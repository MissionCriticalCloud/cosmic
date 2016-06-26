package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;
import org.apache.cloudstack.region.Region;

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

    @SerializedName("gslbserviceenabled")
    @Param(description = "true if GSLB service is enabled in the region, false otherwise")
    private boolean gslbServiceEnabled;

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

    public void setGslbServiceEnabled(final boolean gslbServiceEnabled) {
        this.gslbServiceEnabled = gslbServiceEnabled;
    }

    public void setPortableipServiceEnabled(final boolean portableipServiceEnabled) {
        this.portableipServiceEnabled = portableipServiceEnabled;
    }
}
