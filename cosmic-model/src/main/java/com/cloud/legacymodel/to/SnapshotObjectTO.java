package com.cloud.legacymodel.to;

import com.cloud.model.enumeration.DataObjectType;
import com.cloud.model.enumeration.HypervisorType;

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

    public boolean isQuiescevm() {
        return quiescevm;
    }

    public void setParents(final String[] parents) {
        this.parents = parents;
    }

    @Override
    public String toString() {
        return new StringBuilder("SnapshotTO[datastore=").append(dataStore).append("|volume=").append(volume).append("|path").append(path).append("]").toString();
    }
}
