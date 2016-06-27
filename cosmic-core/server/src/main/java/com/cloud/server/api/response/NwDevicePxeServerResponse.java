package com.cloud.server.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.response.NetworkDeviceResponse;

import com.google.gson.annotations.SerializedName;

public class NwDevicePxeServerResponse extends NetworkDeviceResponse {

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "Zone where to add PXE server")
    private String zoneId;

    @SerializedName(ApiConstants.POD_ID)
    @Param(description = "Pod where to add PXE server")
    private String podId;

    @SerializedName(ApiConstants.URL)
    @Param(description = "Ip of PXE server")
    private String url;

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "Type of add PXE server")
    private String type;

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public String getPodId() {
        return podId;
    }

    public void setPodId(final String podId) {
        this.podId = podId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
