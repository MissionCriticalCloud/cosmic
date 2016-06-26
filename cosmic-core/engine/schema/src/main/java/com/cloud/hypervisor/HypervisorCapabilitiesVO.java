package com.cloud.hypervisor;

import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.NumbersUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "hypervisor_capabilities")
public class HypervisorCapabilitiesVO implements HypervisorCapabilities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "hypervisor_type")
    @Enumerated(value = EnumType.STRING)
    private HypervisorType hypervisorType;

    @Column(name = "hypervisor_version")
    private String hypervisorVersion;

    @Column(name = "max_guests_limit")
    private Long maxGuestsLimit;

    @Column(name = "security_group_enabled")
    private boolean securityGroupEnabled;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "max_data_volumes_limit")
    private Integer maxDataVolumesLimit;

    @Column(name = "max_hosts_per_cluster")
    private Integer maxHostsPerCluster;

    @Column(name = "vm_snapshot_enabled")
    private Boolean vmSnapshotEnabled;

    @Column(name = "storage_motion_supported")
    private boolean storageMotionSupported;

    protected HypervisorCapabilitiesVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public HypervisorCapabilitiesVO(final HypervisorType hypervisorType, final String hypervisorVersion, final Long maxGuestsLimit, final boolean securityGroupEnabled,
                                    final boolean storageMotionSupported) {
        this.hypervisorType = hypervisorType;
        this.hypervisorVersion = hypervisorVersion;
        this.maxGuestsLimit = maxGuestsLimit;
        this.securityGroupEnabled = securityGroupEnabled;
        this.storageMotionSupported = storageMotionSupported;
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     * @return the hypervisorType
     */
    @Override
    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    /**
     * @param hypervisorType the hypervisorType to set
     */
    public void setHypervisorType(final HypervisorType hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    /**
     * @return the hypervisorVersion
     */
    @Override
    public String getHypervisorVersion() {
        return hypervisorVersion;
    }

    /**
     * @param hypervisorVersion the hypervisorVersion to set
     */
    public void setHypervisorVersion(final String hypervisorVersion) {
        this.hypervisorVersion = hypervisorVersion;
    }

    /**
     * @return the securityGroupSupport
     */
    @Override
    public boolean isSecurityGroupEnabled() {
        return securityGroupEnabled;
    }

    public void setSecurityGroupEnabled(final Boolean securityGroupEnabled) {
        this.securityGroupEnabled = securityGroupEnabled;
    }

    /**
     * @return the maxGuests
     */
    @Override
    public Long getMaxGuestsLimit() {
        return maxGuestsLimit;
    }

    /**
     * @param maxGuests the maxGuests to set
     */
    public void setMaxGuestsLimit(final Long maxGuestsLimit) {
        this.maxGuestsLimit = maxGuestsLimit;
    }

    @Override
    public Integer getMaxDataVolumesLimit() {
        return maxDataVolumesLimit;
    }

    public void setMaxDataVolumesLimit(final Integer maxDataVolumesLimit) {
        this.maxDataVolumesLimit = maxDataVolumesLimit;
    }

    @Override
    public Integer getMaxHostsPerCluster() {
        return maxHostsPerCluster;
    }

    /**
     * @return if storage motion is supported
     */
    @Override
    public boolean isStorageMotionSupported() {
        return storageMotionSupported;
    }

    /**
     * @param storageMotionSupported
     */
    public void setStorageMotionSupported(final boolean storageMotionSupported) {
        this.storageMotionSupported = storageMotionSupported;
    }

    public void setMaxHostsPerCluster(final Integer maxHostsPerCluster) {
        this.maxHostsPerCluster = maxHostsPerCluster;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof HypervisorCapabilitiesVO) {
            return ((HypervisorCapabilitiesVO) obj).getId() == this.getId();
        } else {
            return false;
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public Boolean getVmSnapshotEnabled() {
        return vmSnapshotEnabled;
    }

    public void setVmSnapshotEnabled(final Boolean vmSnapshotEnabled) {
        this.vmSnapshotEnabled = vmSnapshotEnabled;
    }
}
