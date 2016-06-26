package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;

import com.google.gson.annotations.SerializedName;

public class ExternalLoadBalancerResponse extends NetworkDeviceResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the external load balancer")
    private String id;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the zone ID of the external load balancer")
    private String zoneId;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "the management IP address of the external load balancer")
    private String ipAddress;

    @SerializedName(ApiConstants.USERNAME)
    @Param(description = "the username that's used to log in to the external load balancer")
    private String username;

    @SerializedName(ApiConstants.PUBLIC_INTERFACE)
    @Param(description = "the public interface of the external load balancer")
    private String publicInterface;

    @SerializedName(ApiConstants.PRIVATE_INTERFACE)
    @Param(description = "the private interface of the external load balancer")
    private String privateInterface;

    @SerializedName(ApiConstants.NUM_RETRIES)
    @Param(description = "the number of times to retry requests to the external load balancer")
    private String numRetries;

    @Override
    public String getId() {
        return id;
    }

    @Override
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

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPublicInterface() {
        return publicInterface;
    }

    public void setPublicInterface(final String publicInterface) {
        this.publicInterface = publicInterface;
    }

    public String getPrivateInterface() {
        return privateInterface;
    }

    public void setPrivateInterface(final String privateInterface) {
        this.privateInterface = privateInterface;
    }

    public String getNumRetries() {
        return numRetries;
    }

    public void setNumRetries(final String numRetries) {
        this.numRetries = numRetries;
    }
}
