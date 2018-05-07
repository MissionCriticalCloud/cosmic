package com.cloud.agent.resource.kvm.storage;

import com.cloud.legacymodel.storage.StorageProvisioningType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.StoragePoolType;
import com.cloud.utils.qemu.QemuImg.PhysicalDiskFormat;

import java.util.List;
import java.util.Map;

public class IscsiAdmStoragePool implements KvmStoragePool {
    private final String uuid;
    private final String sourceHost;
    private final int sourcePort;
    private final StoragePoolType storagePoolType;
    private final StorageAdaptor storageAdaptor;
    private String authUsername;
    private String authSecret;
    private String sourceDir;
    private String localPath;

    public IscsiAdmStoragePool(final String uuid, final String host, final int port, final StoragePoolType storagePoolType,
                               final StorageAdaptor storageAdaptor) {
        this.uuid = uuid;
        this.sourceHost = host;
        this.sourcePort = port;
        this.storagePoolType = storagePoolType;
        this.storageAdaptor = storageAdaptor;
    }

    // called from LibvirtComputingResource.copyPhysicalDisk(KVMPhysicalDisk, String, KVMStoragePool) and
    // from LibvirtComputingResource.createDiskFromTemplate(KVMPhysicalDisk, String, PhysicalDiskFormat, long,
    // KVMStoragePool)
    // does not apply for iScsiAdmStoragePool
    @Override
    public KvmPhysicalDisk createPhysicalDisk(final String name, final PhysicalDiskFormat format,
                                              final StorageProvisioningType provisioningType, final long size) {
        throw new UnsupportedOperationException("Creating a physical disk is not supported.");
    }

    // called from LibvirtComputingResource.execute(CreateCommand) and
    // from KVMStorageProcessor.createVolume(CreateObjectCommand)
    // does not apply for iScsiAdmStoragePool
    @Override
    public KvmPhysicalDisk createPhysicalDisk(final String name, final StorageProvisioningType provisioningType, final long size) {
        throw new UnsupportedOperationException("Creating a physical disk is not supported.");
    }

    @Override
    public boolean connectPhysicalDisk(final String name, final Map<String, String> details) {
        return this.storageAdaptor.connectPhysicalDisk(name, this, details);
    }

    @Override
    public KvmPhysicalDisk getPhysicalDisk(final String volumeUuid) {
        return this.storageAdaptor.getPhysicalDisk(volumeUuid, this);
    }

    @Override
    public boolean disconnectPhysicalDisk(final String volumeUuid) {
        return this.storageAdaptor.disconnectPhysicalDisk(volumeUuid, this);
    }

    @Override
    public boolean deletePhysicalDisk(final String volumeUuid, final ImageFormat format) {
        return this.storageAdaptor.deletePhysicalDisk(volumeUuid, this, format);
    }

    // does not apply for iScsiAdmStoragePool
    @Override
    public List<KvmPhysicalDisk> listPhysicalDisks() {
        return this.storageAdaptor.listPhysicalDisks(this.uuid, this);
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    @Override
    public long getCapacity() {
        return 0;
    }

    @Override
    public long getUsed() {
        return 0;
    }

    @Override
    public long getAvailable() {
        return 0;
    }

    // does not apply for iScsiAdmStoragePool
    @Override
    public boolean refresh() {
        return this.storageAdaptor.refresh(this);
    }

    @Override
    public boolean isExternalSnapshot() {
        return false;
    }

    @Override
    public String getLocalPath() {
        return this.localPath;
    }

    @Override
    public String getSourceHost() {
        return this.sourceHost;
    }

    @Override
    public String getSourceDir() {
        return this.sourceDir;
    }

    @Override
    public int getSourcePort() {
        return this.sourcePort;
    }

    @Override
    public String getAuthUserName() {
        return this.authUsername;
    }

    @Override
    public String getAuthSecret() {
        return this.authSecret;
    }

    @Override
    public StoragePoolType getType() {
        return this.storagePoolType;
    }

    @Override
    public boolean delete() {
        return this.storageAdaptor.deleteStoragePool(this);
    }

    @Override
    public PhysicalDiskFormat getDefaultFormat() {
        return PhysicalDiskFormat.RAW;
    }

    @Override
    public boolean createFolder(final String path) {
        return this.storageAdaptor.createFolder(this.uuid, path);
    }
}
