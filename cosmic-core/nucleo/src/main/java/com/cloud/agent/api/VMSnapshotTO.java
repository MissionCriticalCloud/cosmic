//

//

package com.cloud.agent.api;

import com.cloud.vm.snapshot.VMSnapshot;
import org.apache.cloudstack.storage.to.VolumeObjectTO;

import java.util.List;

public class VMSnapshotTO {
    private Long id;
    private String snapshotName;
    private VMSnapshot.Type type;
    private Long createTime;
    private Boolean current;
    private String description;
    private VMSnapshotTO parent;
    private List<VolumeObjectTO> volumes;
    private boolean quiescevm;

    public VMSnapshotTO(final Long id, final String snapshotName, final VMSnapshot.Type type, final Long createTime, final String description, final Boolean current, final
    VMSnapshotTO parent, final boolean quiescevm) {
        super();
        this.id = id;
        this.snapshotName = snapshotName;
        this.type = type;
        this.createTime = createTime;
        this.current = current;
        this.description = description;
        this.parent = parent;
        this.quiescevm = quiescevm;
    }

    public VMSnapshotTO() {
        this.quiescevm = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Boolean getCurrent() {
        return current;
    }

    public void setCurrent(final Boolean current) {
        this.current = current;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(final Long createTime) {
        this.createTime = createTime;
    }

    public VMSnapshot.Type getType() {
        return type;
    }

    public void setType(final VMSnapshot.Type type) {
        this.type = type;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(final String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public VMSnapshotTO getParent() {
        return parent;
    }

    public void setParent(final VMSnapshotTO parent) {
        this.parent = parent;
    }

    public List<VolumeObjectTO> getVolumes() {
        return this.volumes;
    }

    public void setVolumes(final List<VolumeObjectTO> volumes) {
        this.volumes = volumes;
    }

    public boolean getQuiescevm() {
        return this.quiescevm;
    }

    public void setQuiescevm(final boolean quiescevm) {
        this.quiescevm = quiescevm;
    }
}
