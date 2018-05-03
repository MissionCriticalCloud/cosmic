package com.cloud.hypervisor.kvm.storage;

import com.cloud.legacymodel.storage.StorageProvisioningType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.StoragePoolType;
import com.cloud.utils.qemu.QemuImg.PhysicalDiskFormat;

import java.util.List;
import java.util.Map;

public interface KvmStoragePool {
    public KvmPhysicalDisk createPhysicalDisk(String volumeUuid, PhysicalDiskFormat format,
                                              StorageProvisioningType provisioningType, long size);

    public KvmPhysicalDisk createPhysicalDisk(String volumeUuid, StorageProvisioningType provisioningType, long size);

    public boolean connectPhysicalDisk(String volumeUuid, Map<String, String> details);

    public KvmPhysicalDisk getPhysicalDisk(String volumeUuid);

    public boolean disconnectPhysicalDisk(String volumeUuid);

    public boolean deletePhysicalDisk(String volumeUuid, ImageFormat format);

    public List<KvmPhysicalDisk> listPhysicalDisks();

    public String getUuid();

    public long getCapacity();

    public long getUsed();

    public long getAvailable();

    public boolean refresh();

    public boolean isExternalSnapshot();

    public String getLocalPath();

    public String getSourceHost();

    public String getSourceDir();

    public int getSourcePort();

    public String getAuthUserName();

    public String getAuthSecret();

    public StoragePoolType getType();

    public boolean delete();

    PhysicalDiskFormat getDefaultFormat();

    public boolean createFolder(String path);
}
