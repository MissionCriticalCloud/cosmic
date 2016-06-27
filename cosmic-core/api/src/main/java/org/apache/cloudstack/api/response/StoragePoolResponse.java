package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.storage.StoragePool;
import com.cloud.storage.StoragePoolStatus;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = StoragePool.class)
public class StoragePoolResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the ID of the storage pool")
    private String id;

    @SerializedName("zoneid")
    @Param(description = "the Zone ID of the storage pool")
    private String zoneId;

    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the Zone name of the storage pool")
    private String zoneName;

    @SerializedName("podid")
    @Param(description = "the Pod ID of the storage pool")
    private String podId;

    @SerializedName("podname")
    @Param(description = "the Pod name of the storage pool")
    private String podName;

    @SerializedName("name")
    @Param(description = "the name of the storage pool")
    private String name;

    @SerializedName("ipaddress")
    @Param(description = "the IP address of the storage pool")
    private String ipAddress;

    @SerializedName("path")
    @Param(description = "the storage pool path")
    private String path;

    @SerializedName("created")
    @Param(description = "the date and time the storage pool was created")
    private Date created;

    @SerializedName("type")
    @Param(description = "the storage pool type")
    private String type;

    @SerializedName("clusterid")
    @Param(description = "the ID of the cluster for the storage pool")
    private String clusterId;

    @SerializedName("clustername")
    @Param(description = "the name of the cluster for the storage pool")
    private String clusterName;

    @SerializedName("disksizetotal")
    @Param(description = "the total disk size of the storage pool")
    private Long diskSizeTotal;

    @SerializedName("disksizeallocated")
    @Param(description = "the host's currently allocated disk size")
    private Long diskSizeAllocated;

    @SerializedName("disksizeused")
    @Param(description = "the host's currently used disk size")
    private Long diskSizeUsed;

    @SerializedName("capacityiops")
    @Param(description = "IOPS CloudStack can provision from this storage pool")
    private Long capacityIops;

    @SerializedName("tags")
    @Param(description = "the tags for the storage pool")
    private String tags;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the storage pool")
    private StoragePoolStatus state;

    @SerializedName(ApiConstants.SCOPE)
    @Param(description = "the scope of the storage pool")
    private String scope;

    @SerializedName("overprovisionfactor")
    @Param(description = "the overprovisionfactor for the storage pool", since = "4.4")
    private String overProvisionFactor;

    @SerializedName(ApiConstants.HYPERVISOR)
    @Param(description = "the hypervisor type of the storage pool")
    private String hypervisor;

    @SerializedName("suitableformigration")
    @Param(description = "true if this pool is suitable to migrate a volume," + " false otherwise")
    private Boolean suitableForMigration;

    @SerializedName(ApiConstants.STORAGE_CAPABILITIES)
    @Param(description = "the storage pool capabilities")
    private Map<String, String> caps;

    public Map<String, String> getCaps() {
        return caps;
    }

    public void setCaps(final Map<String, String> cap) {
        this.caps = cap;
    }

    /**
     * @return the scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(final String hypervisor) {
        this.hypervisor = hypervisor;
    }

    @Override
    public String getObjectId() {
        return this.getId();
    }

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

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
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

    public Long getDiskSizeTotal() {
        return diskSizeTotal;
    }

    public void setDiskSizeTotal(final Long diskSizeTotal) {
        this.diskSizeTotal = diskSizeTotal;
    }

    public Long getDiskSizeAllocated() {
        return diskSizeAllocated;
    }

    public void setDiskSizeAllocated(final Long diskSizeAllocated) {
        this.diskSizeAllocated = diskSizeAllocated;
    }

    public Long getDiskSizeUsed() {
        return diskSizeUsed;
    }

    public void setDiskSizeUsed(final Long diskSizeUsed) {
        this.diskSizeUsed = diskSizeUsed;
    }

    public Long getCapacityIops() {
        return capacityIops;
    }

    public void setCapacityIops(final Long capacityIops) {
        this.capacityIops = capacityIops;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(final String tags) {
        this.tags = tags;
    }

    public StoragePoolStatus getState() {
        return state;
    }

    public void setState(final StoragePoolStatus state) {
        this.state = state;
    }

    public void setSuitableForMigration(final Boolean suitableForMigration) {
        this.suitableForMigration = suitableForMigration;
    }

    public void setOverProvisionFactor(final String overProvisionFactor) {
        this.overProvisionFactor = overProvisionFactor;
    }
}
