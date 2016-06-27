package org.apache.cloudstack.framework.sampleserver;

import org.apache.cloudstack.framework.serializer.OnwireName;

@OnwireName(name = "SampleStoragePrepareCommand")
public class SampleStoragePrepareCommand {

    String storagePool;
    String volumeId;

    public SampleStoragePrepareCommand() {
    }

    public String getStoragePool() {
        return storagePool;
    }

    public void setStoragePool(final String storagePool) {
        this.storagePool = storagePool;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(final String volumeId) {
        this.volumeId = volumeId;
    }
}
