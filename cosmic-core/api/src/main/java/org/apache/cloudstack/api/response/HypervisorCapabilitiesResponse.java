package org.apache.cloudstack.api.response;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.hypervisor.HypervisorCapabilities;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = HypervisorCapabilities.class)
public class HypervisorCapabilitiesResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the hypervisor capabilities row")
    private String id;

    @SerializedName(ApiConstants.HYPERVISOR_VERSION)
    @Param(description = "the hypervisor version")
    private String hypervisorVersion;

    @SerializedName(ApiConstants.HYPERVISOR)
    @Param(description = "the hypervisor type")
    private HypervisorType hypervisor;

    @SerializedName(ApiConstants.MAX_GUESTS_LIMIT)
    @Param(description = "the maximum number of guest vms recommended for this hypervisor")
    private Long maxGuestsLimit;

    @SerializedName(ApiConstants.SECURITY_GROUP_EANBLED)
    @Param(description = "true if security group is supported")
    private boolean isSecurityGroupEnabled;

    @SerializedName(ApiConstants.MAX_DATA_VOLUMES_LIMIT)
    @Param(description = "the maximum number of Data Volumes that can be attached for this hypervisor")
    private Integer maxDataVolumesLimit;

    @SerializedName(ApiConstants.MAX_HOSTS_PER_CLUSTER)
    @Param(description = "the maximum number of Hosts per cluster for this hypervisor")
    private Integer maxHostsPerCluster;

    @SerializedName(ApiConstants.STORAGE_MOTION_ENABLED)
    @Param(description = "true if storage motion is supported")
    private boolean isStorageMotionSupported;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getHypervisorVersion() {
        return hypervisorVersion;
    }

    public void setHypervisorVersion(final String hypervisorVersion) {
        this.hypervisorVersion = hypervisorVersion;
    }

    public HypervisorType getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(final HypervisorType hypervisor) {
        this.hypervisor = hypervisor;
    }

    public Long getMaxGuestsLimit() {
        return maxGuestsLimit;
    }

    public void setMaxGuestsLimit(final Long maxGuestsLimit) {
        this.maxGuestsLimit = maxGuestsLimit;
    }

    public Boolean getIsSecurityGroupEnabled() {
        return this.isSecurityGroupEnabled;
    }

    public void setIsSecurityGroupEnabled(final Boolean sgEnabled) {
        this.isSecurityGroupEnabled = sgEnabled;
    }

    public Boolean getIsStorageMotionSupported() {
        return this.isStorageMotionSupported;
    }

    public void setIsStorageMotionSupported(final Boolean smSupported) {
        this.isStorageMotionSupported = smSupported;
    }

    public Integer getMaxDataVolumesLimit() {
        return maxDataVolumesLimit;
    }

    public void setMaxDataVolumesLimit(final Integer maxDataVolumesLimit) {
        this.maxDataVolumesLimit = maxDataVolumesLimit;
    }

    public Integer getMaxHostsPerCluster() {
        return maxHostsPerCluster;
    }

    public void setMaxHostsPerCluster(final Integer maxHostsPerCluster) {
        this.maxHostsPerCluster = maxHostsPerCluster;
    }
}
