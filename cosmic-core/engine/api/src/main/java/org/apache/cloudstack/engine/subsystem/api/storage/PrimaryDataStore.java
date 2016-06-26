package org.apache.cloudstack.engine.subsystem.api.storage;

import org.apache.cloudstack.engine.subsystem.api.storage.disktype.DiskFormat;

import java.util.List;

public interface PrimaryDataStore extends DataStore, PrimaryDataStoreInfo {
    VolumeInfo getVolume(long id);

    List<VolumeInfo> getVolumes();

    boolean exists(DataObject data);

    TemplateInfo getTemplate(long templateId);

    SnapshotInfo getSnapshot(long snapshotId);

    DiskFormat getDefaultDiskType();
}
