//

//

package org.apache.cloudstack.storage.to;

import com.cloud.agent.api.to.DataObjectType;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DataTO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import org.apache.cloudstack.engine.subsystem.api.storage.SnapshotInfo;
import org.apache.cloudstack.engine.subsystem.api.storage.VolumeInfo;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

public class SnapshotObjectTO implements DataTO {
    private String path;
    private VolumeObjectTO volume;
    private String parentSnapshotPath;
    private DataStoreTO dataStore;
    private String vmName;
    private String name;
    private HypervisorType hypervisorType;
    private long id;
    private boolean quiescevm;
    private String[] parents;
    private Long physicalSize = (long) 0;

    public SnapshotObjectTO() {

    }

    public SnapshotObjectTO(final SnapshotInfo snapshot) {
        this.path = snapshot.getPath();
        this.setId(snapshot.getId());
        final VolumeInfo vol = snapshot.getBaseVolume();
        if (vol != null) {
            this.volume = (VolumeObjectTO) vol.getTO();
            this.setVmName(vol.getAttachedVmName());
        }

        SnapshotInfo parentSnapshot = snapshot.getParent();
        final ArrayList<String> parentsArry = new ArrayList<>();
        if (parentSnapshot != null) {
            this.parentSnapshotPath = parentSnapshot.getPath();
            while (parentSnapshot != null) {
                parentsArry.add(parentSnapshot.getPath());
                parentSnapshot = parentSnapshot.getParent();
            }
            parents = parentsArry.toArray(new String[parentsArry.size()]);
            ArrayUtils.reverse(parents);
        }

        this.dataStore = snapshot.getDataStore().getTO();
        this.setName(snapshot.getName());
        this.hypervisorType = snapshot.getHypervisorType();
        this.quiescevm = false;
    }

    @Override
    public DataObjectType getObjectType() {
        return DataObjectType.SNAPSHOT;
    }

    @Override
    public DataStoreTO getDataStore() {
        return this.dataStore;
    }

    public void setDataStore(final DataStoreTO store) {
        this.dataStore = store;
    }

    @Override
    public HypervisorType getHypervisorType() {
        return hypervisorType;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setHypervisorType(final HypervisorType hypervisorType) {
        this.hypervisorType = hypervisorType;
    }

    public Long getPhysicalSize() {
        return this.physicalSize;
    }

    public void setPhysicalSize(final Long physicalSize) {
        this.physicalSize = physicalSize;
    }

    public VolumeObjectTO getVolume() {
        return volume;
    }

    public void setVolume(final VolumeObjectTO volume) {
        this.volume = volume;
    }

    public String getParentSnapshotPath() {
        return parentSnapshotPath;
    }

    public void setParentSnapshotPath(final String parentSnapshotPath) {
        this.parentSnapshotPath = parentSnapshotPath;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(final String vmName) {
        this.vmName = vmName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean getquiescevm() {
        return this.quiescevm;
    }

    public void setQuiescevm(final boolean quiescevm) {
        this.quiescevm = quiescevm;
    }

    public String[] getParents() {
        return parents;
    }

    @Override
    public String toString() {
        return new StringBuilder("SnapshotTO[datastore=").append(dataStore).append("|volume=").append(volume).append("|path").append(path).append("]").toString();
    }
}
