package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class TrafficMonitorResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the external firewall")
    private String id;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the zone ID of the external firewall")
    private String zoneId;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "the management IP address of the external firewall")
    private String ipAddress;

    @SerializedName(ApiConstants.NUM_RETRIES)
    @Param(description = "the number of times to retry requests to the external firewall")
    private String numRetries;

    @SerializedName(ApiConstants.TIMEOUT)
    @Param(description = "the timeout (in seconds) for requests to the external firewall")
    private String timeout;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getNumRetries() {
        return numRetries;
    }

    public void setNumRetries(final String numRetries) {
        this.numRetries = numRetries;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(final String timeout) {
        this.timeout = timeout;
    }
}
