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
package org.apache.cloudstack.storage.datastore.db;

import com.cloud.storage.DataStoreRole;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.fsm.StateObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Join table for image_data_store and snapshots
 */
@Entity
@Table(name = "snapshot_store_ref")
public class SnapshotDataStoreVO implements StateObject<ObjectInDataStoreStateMachine.State>, DataObjectInStore {
    private static final Logger s_logger = LoggerFactory.getLogger(SnapshotDataStoreVO.class);
    @Column(name = "update_count", updatable = true, nullable = false)
    protected long updatedCount;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date updated;
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    ObjectInDataStoreStateMachine.State state;
    @Column(name = "ref_cnt")
    Long refCnt = 0L;
    @Column(name = "volume_id")
    Long volumeId;
    @Column(name = "store_id")
    private long dataStoreId;
    @Column(name = "store_role")
    @Enumerated(EnumType.STRING)
    private DataStoreRole role;
    @Column(name = "snapshot_id")
    private long snapshotId;
    @Column(name = GenericDaoBase.CREATED_COLUMN)
    private final Date created = null;
    @Column(name = "last_updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastUpdated = null;
    @Column(name = "size")
    private long size;
    @Column(name = "physical_size")
    private long physicalSize;
    @Column(name = "parent_snapshot_id")
    private long parentSnapshotId;
    @Column(name = "job_id")
    private String jobId;
    @Column(name = "install_path")
    private String installPath;

    public SnapshotDataStoreVO(final long hostId, final long snapshotId) {
        super();
        dataStoreId = hostId;
        this.snapshotId = snapshotId;
        state = ObjectInDataStoreStateMachine.State.Allocated;
    }

    public SnapshotDataStoreVO() {

    }

    @Override
    public String getInstallPath() {
        return installPath;
    }

    @Override
    public void setInstallPath(final String installPath) {
        this.installPath = installPath;
    }

    @Override
    public long getObjectId() {
        return getSnapshotId();
    }

    @Override
    public long getDataStoreId() {
        return dataStoreId;
    }

    public void setDataStoreId(final long storeId) {
        dataStoreId = storeId;
    }

    @Override
    public State getObjectInStoreState() {
        return state;
    }

    public long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(final long snapshotId) {
        this.snapshotId = snapshotId;
    }

    public long getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(final Date date) {
        lastUpdated = date;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    @Override
    public int hashCode() {
        final Long tid = new Long(snapshotId);
        final Long hid = new Long(dataStoreId);
        return tid.hashCode() + hid.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof SnapshotDataStoreVO) {
            final SnapshotDataStoreVO other = (SnapshotDataStoreVO) obj;
            return (snapshotId == other.getSnapshotId() && dataStoreId == other.getDataStoreId());
        }
        return false;
    }

    @Override
    public String toString() {
        return new StringBuilder("SnapshotDataStore[").append(id)
                                                      .append("-")
                                                      .append(snapshotId)
                                                      .append("-")
                                                      .append(dataStoreId)
                                                      .append(installPath)
                                                      .append("]")
                                                      .toString();
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public long getPhysicalSize() {
        return physicalSize;
    }

    public void setPhysicalSize(final long physicalSize) {
        this.physicalSize = physicalSize;
    }

    public long getVolumeSize() {
        return -1;
    }

    public long getUpdatedCount() {
        return updatedCount;
    }

    public void incrUpdatedCount() {
        updatedCount++;
    }

    public void decrUpdatedCount() {
        updatedCount--;
    }

    public Date getUpdated() {
        return updated;
    }

    @Override
    public ObjectInDataStoreStateMachine.State getState() {
        // TODO Auto-generated method stub
        return state;
    }

    public void setState(final ObjectInDataStoreStateMachine.State state) {
        this.state = state;
    }

    public DataStoreRole getRole() {
        return role;
    }

    public void setRole(final DataStoreRole role) {
        this.role = role;
    }

    public long getParentSnapshotId() {
        return parentSnapshotId;
    }

    public void setParentSnapshotId(final long parentSnapshotId) {
        this.parentSnapshotId = parentSnapshotId;
    }

    public Long getRefCnt() {
        return refCnt;
    }

    public void setRefCnt(final Long refCnt) {
        this.refCnt = refCnt;
    }

    public void incrRefCnt() {
        refCnt++;
    }

    public void decrRefCnt() {
        if (refCnt > 0) {
            refCnt--;
        } else {
            s_logger.warn("We should not try to decrement a zero reference count even though our code has guarded");
        }
    }

    public Long getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(final Long volumeId) {
        this.volumeId = volumeId;
    }
}
