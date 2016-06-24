// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.storage;

import com.cloud.storage.Storage.ProvisioningType;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.utils.NumbersUtil;
import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "volumes")
public class VolumeVO implements Volume {
    @Column(name = "update_count", updatable = true, nullable = false)
    protected long updatedCount; // This field should be updated everytime the
    @Column(name = "display_volume", updatable = true, nullable = false)
    protected boolean displayVolume = true;
    @Id
    @TableGenerator(name = "volume_sq", table = "sequence", pkColumnName = "name", valueColumnName = "value", pkColumnValue = "volume_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    long id;
    @Column(name = "name")
    String name;
    @Column(name = "pool_id")
    Long poolId;
    @Column(name = "last_pool_id")
    Long lastPoolId;
    @Column(name = "account_id")
    long accountId;
    @Column(name = "domain_id")
    long domainId;
    @Column(name = "instance_id")
    Long instanceId = null;
    @Column(name = "device_id")
    Long deviceId = null;
    @Column(name = "size")
    Long size;
    @Column(name = "folder")
    String folder;

    @Column(name = "path")
    String path;

    @Column(name = "pod_id")
    Long podId;

    @Column(name = "created")
    Date created;

    @Column(name = "attached")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date attached;

    @Column(name = "data_center_id")
    long dataCenterId;

    @Column(name = "host_ip")
    String hostip;

    @Column(name = "disk_offering_id")
    long diskOfferingId;

    @Column(name = "template_id")
    Long templateId;

    @Column(name = "first_snapshot_backup_uuid")
    String firstSnapshotBackupUuid;

    @Column(name = "volume_type")
    @Enumerated(EnumType.STRING)
    Type volumeType = Volume.Type.UNKNOWN;

    @Column(name = "pool_type")
    @Enumerated(EnumType.STRING)
    StoragePoolType poolType;

    @Column(name = GenericDao.REMOVED_COLUMN)
    Date removed;

    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date updated;
    @Column(name = "recreatable")
    boolean recreatable;
    // state is updated. There's no set method in
    // the vo object because it is done with in the
    // dao code.
    @Column(name = "chain_info", length = 65535)
    String chainInfo;
    @Column(name = "uuid")
    String uuid;
    @Transient
    // @Column(name="reservation")
            String reservationId;
    @Column(name = "min_iops")
    private Long minIops;
    @Column(name = "max_iops")
    private Long maxIops;
    @Column(name = "state")
    @Enumerated(value = EnumType.STRING)
    private State state;
    @Column(name = "format")
    private Storage.ImageFormat format;
    @Column(name = "provisioning_type")
    private Storage.ProvisioningType provisioningType;
    @Column(name = "iscsi_name")
    private String _iScsiName;
    @Column(name = "vm_snapshot_chain_size")
    private Long vmSnapshotChainSize;
    @Column(name = "iso_id")
    private Long isoId;
    @Column(name = "hv_ss_reserve")
    private Integer hypervisorSnapshotReserve;

    // Real Constructor
    public VolumeVO(Type type, String name, long dcId, long domainId,
                    long accountId, long diskOfferingId, Storage.ProvisioningType provisioningType, long size,
                    Long minIops, Long maxIops, String iScsiName) {
        this.volumeType = type;
        this.name = name;
        dataCenterId = dcId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.provisioningType = provisioningType;
        this.size = size;
        this.minIops = minIops;
        this.maxIops = maxIops;
        _iScsiName = iScsiName;
        this.diskOfferingId = diskOfferingId;
        state = State.Allocated;
        uuid = UUID.randomUUID().toString();
    }

    public VolumeVO(String name, long dcId, long podId, long accountId,
                    long domainId, Long instanceId, String folder, String path, Storage.ProvisioningType provisioningType,
                    long size, Volume.Type vType) {
        this.name = name;
        this.accountId = accountId;
        this.domainId = domainId;
        this.instanceId = instanceId;
        this.folder = folder;
        this.path = path;
        this.provisioningType = provisioningType;
        this.size = size;
        minIops = null;
        maxIops = null;
        _iScsiName = null;
        this.podId = podId;
        dataCenterId = dcId;
        volumeType = vType;
        state = Volume.State.Allocated;
        recreatable = false;
        uuid = UUID.randomUUID().toString();
    }

    // Copy Constructor
    public VolumeVO(Volume that) {
        this(that.getName(),
                that.getDataCenterId(),
                that.getPodId(),
                that.getAccountId(),
                that.getDomainId(),
                that.getInstanceId(),
                that.getFolder(),
                that.getPath(),
                that.getProvisioningType(),
                that.getSize(),
                that.getMinIops(),
                that.getMaxIops(),
                that.get_iScsiName(),
                that.getVolumeType());
        recreatable = that.isRecreatable();
        state = State.Allocated; //This should be in Allocated state before going into Ready state
        size = that.getSize();
        minIops = that.getMinIops();
        maxIops = that.getMaxIops();
        _iScsiName = that.get_iScsiName();
        diskOfferingId = that.getDiskOfferingId();
        poolId = that.getPoolId();
        attached = that.getAttached();
        chainInfo = that.getChainInfo();
        templateId = that.getTemplateId();
        deviceId = that.getDeviceId();
        format = that.getFormat();
        provisioningType = that.getProvisioningType();
        uuid = UUID.randomUUID().toString();
    }

    public VolumeVO(String name, long dcId, Long podId, long accountId,
                    long domainId, Long instanceId, String folder, String path, Storage.ProvisioningType provisioningType,
                    long size, Long minIops, Long maxIops, String iScsiName,
                    Volume.Type vType) {
        this.name = name;
        this.accountId = accountId;
        this.domainId = domainId;
        this.instanceId = instanceId;
        this.folder = folder;
        this.path = path;
        this.provisioningType = provisioningType;
        this.size = size;
        this.minIops = minIops;
        this.maxIops = maxIops;
        _iScsiName = iScsiName;
        this.podId = podId;
        dataCenterId = dcId;
        volumeType = vType;
        state = Volume.State.Allocated;
        recreatable = false;
        uuid = UUID.randomUUID().toString();
    }

    protected VolumeVO() {
    }

    public void decrUpdatedCount() {
        updatedCount--;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public Long getMinIops() {
        return minIops;
    }

    public void setMinIops(Long minIops) {
        this.minIops = minIops;
    }

    @Override
    public Long getMaxIops() {
        return maxIops;
    }

    public void setMaxIops(Long maxIops) {
        this.maxIops = maxIops;
    }

    @Override
    public String get_iScsiName() {
        return _iScsiName;
    }

    @Override
    public Long getInstanceId() {
        return instanceId;
    }

    @Override
    public String getFolder() {
        return folder;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Long getPodId() {
        return podId;
    }

    @Override
    public long getDataCenterId() {
        return dataCenterId;
    }

    @Override
    public Type getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(Type type) {
        volumeType = type;
    }

    @Override
    public Long getPoolId() {
        return poolId;
    }

    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Date getAttached() {
        return attached;
    }

    @Override
    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public Long getDiskOfferingId() {
        return diskOfferingId;
    }

    public void setDiskOfferingId(long diskOfferingId) {
        this.diskOfferingId = diskOfferingId;
    }

    @Override
    public String getChainInfo() {
        return chainInfo;
    }

    @Override
    public boolean isRecreatable() {
        return recreatable;
    }

    @Override
    public long getUpdatedCount() {
        return updatedCount;
    }

    @Override
    public void incrUpdatedCount() {
        updatedCount++;
    }

    @Override
    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public String getReservationId() {
        return reservationId;
    }

    @Override
    public void setReservationId(String reserv) {
        reservationId = reserv;
    }

    @Override
    public Storage.ImageFormat getFormat() {
        return format;
    }

    public void setFormat(Storage.ImageFormat format) {
        this.format = format;
    }

    @Override
    public ProvisioningType getProvisioningType() {
        return provisioningType;
    }

    public void setProvisioningType(ProvisioningType provisioningType) {
        this.provisioningType = provisioningType;
    }

    @Override
    public Long getVmSnapshotChainSize() {
        return vmSnapshotChainSize;
    }

    public void setVmSnapshotChainSize(Long vmSnapshotChainSize) {
        this.vmSnapshotChainSize = vmSnapshotChainSize;
    }

    @Override
    public Integer getHypervisorSnapshotReserve() {
        return hypervisorSnapshotReserve;
    }

    @Override
    public boolean isDisplayVolume() {
        return displayVolume;
    }

    @Override
    public boolean isDisplay() {
        return displayVolume;
    }

    public void setDisplay(boolean display) {
        this.displayVolume = display;
    }

    public void setDisplayVolume(boolean displayVolume) {
        this.displayVolume = displayVolume;
    }

    public void setHypervisorSnapshotReserve(Integer hypervisorSnapshotReserve) {
        this.hypervisorSnapshotReserve = hypervisorSnapshotReserve;
    }

    public void setRecreatable(boolean recreatable) {
        this.recreatable = recreatable;
    }

    public void setChainInfo(String chainInfo) {
        this.chainInfo = chainInfo;
    }

    public void setAttached(Date attached) {
        this.attached = attached;
    }

    // don't use this directly, use volume state machine instead
    // This method is used by UpdateVolume as a part of "Better control over first class objects in CS"
    public void setState(State state) {
        this.state = state;
    }

    public void setDataCenterId(long dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public void setPodId(Long podId) {
        this.podId = podId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public void set_iScsiName(String iScsiName) {
        _iScsiName = iScsiName;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public StoragePoolType getPoolType() {
        return poolType;
    }

    public void setPoolType(StoragePoolType poolType) {
        this.poolType = poolType;
    }

    @Override
    public long getDomainId() {
        return domainId;
    }

    public void setDomainId(long domainId) {
        this.domainId = domainId;
    }

    public String getHostIp() {
        return hostip;
    }

    public void setHostIp(String hostip) {
        this.hostip = hostip;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(Date removed) {
        this.removed = removed;
    }

    @Override
    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getFirstSnapshotBackupUuid() {
        return firstSnapshotBackupUuid;
    }

    public void setFirstSnapshotBackupUuid(String firstSnapshotBackupUuid) {
        this.firstSnapshotBackupUuid = firstSnapshotBackupUuid;
    }

    public Long getLastPoolId() {
        return lastPoolId;
    }

    public void setLastPoolId(Long poolId) {
        lastPoolId = poolId;
    }

    @Override
    public int hashCode() {
        return NumbersUtil.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VolumeVO) {
            return id == ((VolumeVO) obj).id;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("Vol[").append(id).append("|vm=").append(instanceId).append("|").append(volumeType).append("]").toString();
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getIsoId() {
        return isoId;
    }

    public void setIsoId(Long isoId) {
        this.isoId = isoId;
    }

    @Override
    public Class<?> getEntityType() {
        return Volume.class;
    }
}
