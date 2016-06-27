package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class CapacityResponse extends BaseResponse {
    @SerializedName(ApiConstants.TYPE)
    @Param(description = "the capacity type")
    private Short capacityType;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the Zone ID")
    private String zoneId;

    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the Zone name")
    private String zoneName;

    @SerializedName(ApiConstants.POD_ID)
    @Param(description = "the Pod ID")
    private String podId;

    @SerializedName("podname")
    @Param(description = "the Pod name")
    private String podName;

    @SerializedName(ApiConstants.CLUSTER_ID)
    @Param(description = "the Cluster ID")
    private String clusterId;

    @SerializedName("clustername")
    @Param(description = "the Cluster name")
    private String clusterName;

    @SerializedName("capacityused")
    @Param(description = "the capacity currently in use")
    private Long capacityUsed;

    @SerializedName("capacitytotal")
    @Param(description = "the total capacity available")
    private Long capacityTotal;

    @SerializedName("percentused")
    @Param(description = "the percentage of capacity currently in use")
    private String percentUsed;

    public Short getCapacityType() {
        return capacityType;
    }

    public void setCapacityType(final Short capacityType) {
        this.capacityType = capacityType;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
    }

    public String getPodId() {
        return podId;
    }

    public void setPodId(final String podId) {
        this.podId = podId;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(final String podName) {
        this.podName = podName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(final String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(final String clusterName) {
        this.clusterName = clusterName;
    }

    public Long getCapacityUsed() {
        return capacityUsed;
    }

    public void setCapacityUsed(final Long capacityUsed) {
        this.capacityUsed = capacityUsed;
    }

    public Long getCapacityTotal() {
        return capacityTotal;
    }

    public void setCapacityTotal(final Long capacityTotal) {
        this.capacityTotal = capacityTotal;
    }

    public String getPercentUsed() {
        return percentUsed;
    }

    public void setPercentUsed(final String percentUsed) {
        this.percentUsed = percentUsed;
    }
}
