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
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.fsm.StateObject;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Date;

/**
 * Join table for image_data_store and templates
 */
@Entity
@Table(name = "template_store_ref")
public class TemplateDataStoreVO implements StateObject<ObjectInDataStoreStateMachine.State>, DataObjectInStore {
    private static final Logger s_logger = LoggerFactory.getLogger(TemplateDataStoreVO.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "store_id")
    private Long dataStoreId;

    @Column(name = "template_id")
    private long templateId;

    @Column(name = "store_role")
    @Enumerated(EnumType.STRING)
    private DataStoreRole dataStoreRole;

    @Column(name = GenericDaoBase.CREATED_COLUMN)
    private Date created = null;

    @Column(name = "last_updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastUpdated = null;

    @Column(name = "download_pct")
    private int downloadPercent;

    @Column(name = "size")
    private Long size;

    @Column(name = "physical_size")
    private long physicalSize;

    @Column(name = "download_state")
    @Enumerated(EnumType.STRING)
    private Status downloadState;

    @Column(name = "local_path")
    private String localDownloadPath;

    @Column(name = "error_str")
    private String errorString;

    @Column(name = "job_id")
    private String jobId;

    @Column(name = "install_path")
    private String installPath;

    @Column(name = "url", length = 2048)
    private String downloadUrl;

    @Column(name = "download_url", length = 2048)
    private String extractUrl;

    @Column(name = "download_url_created")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date extractUrlCreated = null;

    @Column(name = "is_copy")
    private boolean isCopy = false;

    @Column(name = "destroyed")
    boolean destroyed = false;

    @Column(name = "update_count", updatable = true, nullable = false)
    protected long updatedCount;

    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date updated;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    ObjectInDataStoreStateMachine.State state;

    @Column(name = "ref_cnt")
    Long refCnt = 0L;

    public TemplateDataStoreVO(final Long hostId, final long templateId) {
        super();
        dataStoreId = hostId;
        this.templateId = templateId;
        state = ObjectInDataStoreStateMachine.State.Allocated;
        refCnt = 0L;
    }

    public TemplateDataStoreVO(final Long hostId, final long templateId, final Date lastUpdated, final int downloadPercent, final Status downloadState, final String localDownloadPath, final String errorString,
                               final String jobId, final String installPath, final String downloadUrl) {
        super();
        dataStoreId = hostId;
        this.templateId = templateId;
        this.lastUpdated = lastUpdated;
        this.downloadPercent = downloadPercent;
        this.downloadState = downloadState;
        this.localDownloadPath = localDownloadPath;
        this.errorString = errorString;
        this.jobId = jobId;
        refCnt = 0L;
        this.installPath = installPath;
        setDownloadUrl(downloadUrl);
        switch (downloadState) {
            case DOWNLOADED:
                state = ObjectInDataStoreStateMachine.State.Ready;
                break;
            case CREATING:
            case DOWNLOAD_IN_PROGRESS:
            case UPLOAD_IN_PROGRESS:
                state = ObjectInDataStoreStateMachine.State.Creating2;
                break;
            case DOWNLOAD_ERROR:
            case UPLOAD_ERROR:
                state = ObjectInDataStoreStateMachine.State.Failed;
                break;
            case ABANDONED:
                state = ObjectInDataStoreStateMachine.State.Destroyed;
                break;
            default:
                state = ObjectInDataStoreStateMachine.State.Allocated;
                break;
        }
    }

    public TemplateDataStoreVO() {
        refCnt = 0L;
    }

    @Override
    public String getInstallPath() {
        return installPath;
    }

    @Override
    public long getDataStoreId() {
        return dataStoreId;
    }

    public void setDataStoreId(final long storeId) {
        dataStoreId = storeId;
    }

    public long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(final long templateId) {
        this.templateId = templateId;
    }

    public int getDownloadPercent() {
        return downloadPercent;
    }

    public void setDownloadPercent(final int downloadPercent) {
        this.downloadPercent = downloadPercent;
    }

    public void setDownloadState(final Status downloadState) {
        this.downloadState = downloadState;
    }

    public long getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(final Date date) {
        lastUpdated = date;
    }

    @Override
    public void setInstallPath(final String installPath) {
        this.installPath = installPath;
    }

    public Status getDownloadState() {
        return downloadState;
    }

    public void setLocalDownloadPath(final String localPath) {
        localDownloadPath = localPath;
    }

    public String getLocalDownloadPath() {
        return localDownloadPath;
    }

    public void setErrorString(final String errorString) {
        this.errorString = errorString;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public String getJobId() {
        return jobId;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof TemplateDataStoreVO) {
            final TemplateDataStoreVO other = (TemplateDataStoreVO) obj;
            return (templateId == other.getTemplateId() && dataStoreId == other.getDataStoreId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        final Long tid = new Long(templateId);
        final Long hid = new Long(dataStoreId);
        return tid.hashCode() + hid.hashCode();
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public void setPhysicalSize(final long physicalSize) {
        this.physicalSize = physicalSize;
    }

    public long getPhysicalSize() {
        return physicalSize;
    }

    public void setDestroyed(final boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean getDestroyed() {
        return destroyed;
    }

    public void setDownloadUrl(final String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setCopy(final boolean isCopy) {
        this.isCopy = isCopy;
    }

    public boolean isCopy() {
        return isCopy;
    }

    public long getTemplateSize() {
        return -1;
    }

    @Override
    public String toString() {
        return new StringBuilder("TmplDataStore[").append(id).append("-").append(templateId).append("-").append(dataStoreId).append(installPath).append("]").toString();
    }

    @Override
    public ObjectInDataStoreStateMachine.State getState() {
        // TODO Auto-generated method stub
        return state;
    }

    public void setState(final ObjectInDataStoreStateMachine.State state) {
        this.state = state;
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
    public long getObjectId() {
        return getTemplateId();
    }

    @Override
    public State getObjectInStoreState() {
        return state;
    }

    public DataStoreRole getDataStoreRole() {
        return dataStoreRole;
    }

    public void setDataStoreRole(final DataStoreRole dataStoreRole) {
        this.dataStoreRole = dataStoreRole;
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

    public String getExtractUrl() {
        return extractUrl;
    }

    public void setExtractUrl(final String extractUrl) {
        this.extractUrl = extractUrl;
    }

    public Date getExtractUrlCreated() {
        return extractUrlCreated;
    }

    public void setExtractUrlCreated(final Date extractUrlCreated) {
        this.extractUrlCreated = extractUrlCreated;
    }

}
