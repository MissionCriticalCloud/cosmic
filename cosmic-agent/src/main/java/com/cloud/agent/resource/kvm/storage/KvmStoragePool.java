package com.cloud.agent.resource.kvm.storage;

import com.cloud.legacymodel.storage.StorageProvisioningType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.StoragePoolType;
import com.cloud.utils.qemu.QemuImg.PhysicalDiskFormat;

import java.util.List;
import java.util.Map;

public interface KvmStoragePool {
    KvmPhysicalDisk createPhysicalDisk(String volumeUuid, PhysicalDiskFormat format,
                                       StorageProvisioningType provisioningType, long size);

    KvmPhysicalDisk createPhysicalDisk(String volumeUuid, StorageProvisioningType provisioningType, long size);

    boolean connectPhysicalDisk(String volumeUuid, Map<String, String> details);

    KvmPhysicalDisk getPhysicalDisk(String volumeUuid);

    boolean disconnectPhysicalDisk(String volumeUuid);

    boolean deletePhysicalDisk(String volumeUuid, ImageFormat format);

    List<KvmPhysicalDisk> listPhysicalDisks();

    String getUuid();

    long getCapacity();

    long getUsed();

    long getAvailable();

    boolean refresh();

    boolean isExternalSnapshot();

    String getLocalPath();

    String getSourceHost();

    String getSourceDir();

    int getSourcePort();

    String getAuthUserName();

    String getAuthSecret();

    StoragePoolType getType();

    boolean delete();

    PhysicalDiskFormat getDefaultFormat();

    boolean createFolder(String path);
}
