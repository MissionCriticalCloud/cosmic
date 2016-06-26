package org.apache.cloudstack.engine.subsystem.api.storage;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.storage.Storage.StoragePoolType;

import java.util.Map;

public class PrimaryDataStoreParameters {
    private Long zoneId;
    private Long podId;
    private Long clusterId;
    private String providerName;
    private Map<String, String> details;
    private String tags;
    private StoragePoolType type;
    private HypervisorType hypervisorType;
    private String host;
    private String path;
    private int port;
    private String uuid;
    private String name;
    private String userInfo;
    private long capacityBytes;
    private long usedBytes;
    private boolean managed;
    private Long capacityIops;

    /**
     * @return the userInfo
     */
    public String getUserInfo() {
        return userInfo;
    }

    /**
     * @param userInfo the userInfo to set
     */
    public void setUserInfo(final String userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(final String host) {
        this.host = host;
    }

    /**
     * @return the type
     */
    public StoragePoolType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(final StoragePoolType type) {
        this.type = type;
    }

    /**
     * @return the tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(final String tags) {
        this.tags = tags;
    }

    /**
     * @return the details
     */
    public Map<String, String> getDetails() {
        return details;
    }

    /**
     * @param details the details to set
     */
    public void setDetails(final Map<String, String> details) {
        this.details = details;
    }

    /**
     * @return the providerName
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * @param providerName the providerName to set
     */
    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    public boolean isManaged() {
        return managed;
    }

    public void setManaged(final boolean managed) {
        this.managed = managed;
    }

    public Long getCapacityIops() {
        return capacityIops;
    }

    public void setCapacityIops(final Long capacityIops) {
        this.capacityIops = capacityIops;
    }

    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    public void setHypervisorType(final HypervisorType hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    /**
     * @return the clusterId
     */
    public Long getClusterId() {
        return clusterId;
    }

    /**
     * @param clusterId the clusterId to set
     */
    public void setClusterId(final Long clusterId) {
        this.clusterId = clusterId;
    }

    /**
     * @return the podId
     */
    public Long getPodId() {
        return podId;
    }

    /**
     * @param podId the podId to set
     */
    public void setPodId(final Long podId) {
        this.podId = podId;
    }

    /**
     * @return the zoneId
     */
    public Long getZoneId() {
        return zoneId;
    }

    /**
     * @param zoneId the zoneId to set
     */
    public void setZoneId(final Long zoneId) {
        this.zoneId = zoneId;
    }

    public long getCapacityBytes() {
        return capacityBytes;
    }

    public void setCapacityBytes(final long capacityBytes) {
        this.capacityBytes = capacityBytes;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(final long usedBytes) {
        this.usedBytes = usedBytes;
    }
}
