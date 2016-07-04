package org.apache.cloudstack.storage.db;

import com.cloud.agent.api.to.DataObjectType;
import com.cloud.storage.DataStoreRole;
import com.cloud.storage.Storage;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
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

@Entity
@Table(name = "object_datastore_ref")
public class ObjectInDataStoreVO implements StateObject<ObjectInDataStoreStateMachine.State>, DataObjectInStore {
    @Column(name = "update_count", updatable = true, nullable = false)
    protected long updatedCount;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "object_id")
    long objectId;
    @Column(name = "object_type")
    @Enumerated(EnumType.STRING)
    DataObjectType objectType;
    @Column(name = GenericDaoBase.CREATED_COLUMN)
    Date created = null;
    @Column(name = "last_updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date lastUpdated = null;
    @Column(name = "download_pct")
    int downloadPercent;
    @Column(name = "download_state")
    @Enumerated(EnumType.STRING)
    Status downloadState;
    @Column(name = "local_path")
    String localDownloadPath;
    @Column(name = "error_str")
    String errorString;
    @Column(name = "job_id")
    String jobId;
    @Column(name = "install_path")
    String installPath;
    @Column(name = "size")
    Long size;
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    ObjectInDataStoreStateMachine.State state;
    @Column(name = "updated")
    @Temporal(value = TemporalType.TIMESTAMP)
    Date updated;
    @Column(name = "datastore_id")
    private long dataStoreId;
    @Column(name = "datastore_role")
    @Enumerated(EnumType.STRING)
    private DataStoreRole dataStoreRole;
    @Column(name = "url", length = 2048)
    private String downloadUrl;
    @Column(name = "format")
    private Storage.ImageFormat format;
    @Column(name = "checksum")
    private String checksum;

    public ObjectInDataStoreVO() {
        this.state = ObjectInDataStoreStateMachine.State.Allocated;
    }

    public long getId() {
        return this.id;
    }

    public DataStoreRole getDataStoreRole() {
        return this.dataStoreRole;
    }

    public void setDataStoreRole(final DataStoreRole role) {
        this.dataStoreRole = role;
    }

    public DataObjectType getObjectType() {
        return this.objectType;
    }

    public void setObjectType(final DataObjectType type) {
        this.objectType = type;
    }

    @Override
    public ObjectInDataStoreStateMachine.State getState() {
        return this.state;
    }

    @Override
    public String getInstallPath() {
        return this.installPath;
    }

    @Override
    public void setInstallPath(final String path) {
        this.installPath = path;
    }

    @Override
    public long getObjectId() {
        return this.objectId;
    }

    public void setObjectId(final long id) {
        this.objectId = id;
    }

    @Override
    public long getDataStoreId() {
        return dataStoreId;
    }

    public void setDataStoreId(final long dataStoreId) {
        this.dataStoreId = dataStoreId;
    }

    @Override
    public State getObjectInStoreState() {
        return this.state;
    }

    public Long getSize() {
        return this.size;
    }

    public void setSize(final Long size) {
        this.size = size;
    }

    public long getUpdatedCount() {
        return this.updatedCount;
    }

    public void incrUpdatedCount() {
        this.updatedCount++;
    }

    public void decrUpdatedCount() {
        this.updatedCount--;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(final Date updated) {
        this.updated = updated;
    }
}
