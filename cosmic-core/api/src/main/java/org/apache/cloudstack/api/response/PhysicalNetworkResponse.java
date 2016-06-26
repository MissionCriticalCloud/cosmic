package org.apache.cloudstack.api.response;

import com.cloud.network.PhysicalNetwork;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = PhysicalNetwork.class)
public class PhysicalNetworkResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the uuid of the physical network")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "name of the physical network")
    private String name;

    @SerializedName(ApiConstants.BROADCAST_DOMAIN_RANGE)
    @Param(description = "Broadcast domain range of the physical network")
    private String broadcastDomainRange;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "zone id of the physical network")
    private String zoneId;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "state of the physical network")
    private String state;

    @SerializedName(ApiConstants.VLAN)
    @Param(description = "the vlan of the physical network")
    private String vlan;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain id of the physical network owner")
    private String domainId;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "comma separated tag")
    private String tags;

    @SerializedName(ApiConstants.ISOLATION_METHODS)
    @Param(description = "isolation methods")
    private String isolationMethods;

    @SerializedName(ApiConstants.NETWORK_SPEED)
    @Param(description = "the speed of the physical network")
    private String networkSpeed;

    @Override
    public String getObjectId() {
        return this.id;
    }

    public void setId(final String uuid) {
        this.id = uuid;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public void setVlan(final String vlan) {
        this.vlan = vlan;
    }

    public void setTags(final List<String> tags) {
        if (tags == null || tags.size() == 0) {
            return;
        }

        final StringBuilder buf = new StringBuilder();
        for (final String tag : tags) {
            buf.append(tag).append(",");
        }

        this.tags = buf.delete(buf.length() - 1, buf.length()).toString();
    }

    public void setBroadcastDomainRange(final String broadcastDomainRange) {
        this.broadcastDomainRange = broadcastDomainRange;
    }

    public void setNetworkSpeed(final String networkSpeed) {
        this.networkSpeed = networkSpeed;
    }

    public void setIsolationMethods(final List<String> isolationMethods) {
        if (isolationMethods == null || isolationMethods.size() == 0) {
            return;
        }

        final StringBuilder buf = new StringBuilder();
        for (final String isolationMethod : isolationMethods) {
            buf.append(isolationMethod).append(",");
        }

        this.isolationMethods = buf.delete(buf.length() - 1, buf.length()).toString();
    }

    public void setName(final String name) {
        this.name = name;
    }
}
