package org.apache.cloudstack.api.response;

import com.cloud.org.Cluster;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Cluster.class)
public class ClusterResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the cluster ID")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the cluster name")
    private String name;

    @SerializedName(ApiConstants.POD_ID)
    @Param(description = "the Pod ID of the cluster")
    private String podId;

    @SerializedName("podname")
    @Param(description = "the Pod name of the cluster")
    private String podName;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the Zone ID of the cluster")
    private String zoneId;

    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the Zone name of the cluster")
    private String zoneName;

    @SerializedName("hypervisortype")
    @Param(description = "the hypervisor type of the cluster")
    private String hypervisorType;

    @SerializedName("clustertype")
    @Param(description = "the type of the cluster")
    private String clusterType;

    @SerializedName("allocationstate")
    @Param(description = "the allocation state of the cluster")
    private String allocationState;

    @SerializedName("managedstate")
    @Param(description = "whether this cluster is managed by cloudstack")
    private String managedState;

    @SerializedName("capacity")
    @Param(description = "the capacity of the Cluster", responseObject = CapacityResponse.class)
    private List<CapacityResponse> capacitites;

    @SerializedName("cpuovercommitratio")
    @Param(description = "The cpu overcommit ratio of the cluster")
    private String cpuovercommitratio;

    @SerializedName("memoryovercommitratio")
    @Param(description = "The memory overcommit ratio of the cluster")
    private String memoryovercommitratio;

    @SerializedName("ovm3vip")
    @Param(description = "Ovm3 VIP to use for pooling and/or clustering")
    private String ovm3vip;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
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

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(final String clusterType) {
        this.clusterType = clusterType;
    }

    public String getHypervisorType() {
        return this.hypervisorType;
    }

    public void setHypervisorType(final String hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public String getAllocationState() {
        return allocationState;
    }

    public void setAllocationState(final String allocationState) {
        this.allocationState = allocationState;
    }

    public String getManagedState() {
        return managedState;
    }

    public void setManagedState(final String managedState) {
        this.managedState = managedState;
    }

    public List<CapacityResponse> getCapacitites() {
        return capacitites;
    }

    public void setCapacitites(final ArrayList<CapacityResponse> arrayList) {
        this.capacitites = arrayList;
    }

    public String getCpuOvercommitRatio() {
        return cpuovercommitratio;
    }

    public void setCpuOvercommitRatio(final String cpuovercommitratio) {
        this.cpuovercommitratio = cpuovercommitratio;
    }

    public String getMemoryOvercommitRatio() {
        return memoryovercommitratio;
    }

    public void setMemoryOvercommitRatio(final String memoryovercommitratio) {
        this.memoryovercommitratio = memoryovercommitratio;
    }

    public String getOvm3Vip() {
        return ovm3vip;
    }

    public void setOvm3Vip(final String ovm3vip) {
        this.ovm3vip = ovm3vip;
    }
}
