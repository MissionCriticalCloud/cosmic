package com.cloud.hypervisor.kvm.storage;

import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.libvirt.StoragePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibvirtStoragePool implements KvmStoragePool {

    private final Logger logger = LoggerFactory.getLogger(LibvirtStoragePool.class);

    protected String uuid;
    protected long capacity;
    protected long used;
    protected long available;
    protected String name;
    protected String localPath;
    protected PhysicalDiskFormat defaultFormat;
    protected StoragePoolType type;
    protected StorageAdaptor storageAdaptor;
    protected StoragePool pool;
    protected String authUsername;
    protected String authSecret;
    protected String sourceHost;
    protected int sourcePort;
    protected String sourceDir;

    public LibvirtStoragePool(final String uuid, final String name, final StoragePoolType type, final StorageAdaptor adaptor, final StoragePool pool) {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
        storageAdaptor = adaptor;
        capacity = 0;
        used = 0;
        available = 0;
        this.pool = pool;
    }

    public String getName() {
        return name;
    }

    @Override
    public KvmPhysicalDisk createPhysicalDisk(final String name,
                                              final PhysicalDiskFormat format, final Storage.ProvisioningType provisioningType, final long size) {
        return storageAdaptor.createPhysicalDisk(name, this, format, provisioningType, size);
    }

    @Override
    public KvmPhysicalDisk createPhysicalDisk(final String name, final Storage.ProvisioningType provisioningType, final long size) {
        return storageAdaptor.createPhysicalDisk(name, this,
                getDefaultFormat(), provisioningType, size);
    }

    public StoragePoolType getStoragePoolType() {
        return type;
    }

    @Override
    public boolean connectPhysicalDisk(final String name, final Map<String, String> details) {
        return true;
    }

    @Override
    public KvmPhysicalDisk getPhysicalDisk(final String volumeUid) {
        KvmPhysicalDisk disk = null;
        String volumeUuid = volumeUid;
        if (volumeUid.contains("/")) {
            final String[] tokens = volumeUid.split("/");
            volumeUuid = tokens[tokens.length - 1];
        }
        try {
            disk = storageAdaptor.getPhysicalDisk(volumeUuid, this);
        } catch (final CloudRuntimeException e) {
            if (getStoragePoolType() != StoragePoolType.NetworkFilesystem
                    && getStoragePoolType() != StoragePoolType.Filesystem) {
                throw e;
            }
        }

        if (disk != null) {
            return disk;
        }
        logger.debug("find volume bypass libvirt");
        // For network file system or file system, try to use java file to find the volume, instead of through libvirt.
        // BUG:CLOUDSTACK-4459
        final String localPoolPath = getLocalPath();
        final File f = new File(localPoolPath + File.separator + volumeUuid);
        if (!f.exists()) {
            logger.debug("volume: " + volumeUuid + " not exist on storage pool");
            throw new CloudRuntimeException("Can't find volume:" + volumeUuid);
        }
        disk = new KvmPhysicalDisk(f.getPath(), volumeUuid, this);
        disk.setFormat(PhysicalDiskFormat.QCOW2);
        disk.setSize(f.length());
        disk.setVirtualSize(f.length());
        return disk;
    }

    @Override
    public boolean disconnectPhysicalDisk(final String uuid) {
        return true;
    }

    @Override
    public boolean deletePhysicalDisk(final String uuid, final Storage.ImageFormat format) {
        return storageAdaptor.deletePhysicalDisk(uuid, this, format);
    }

    @Override
    public List<KvmPhysicalDisk> listPhysicalDisks() {
        return storageAdaptor.listPhysicalDisks(uuid, this);
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(final long capacity) {
        this.capacity = capacity;
    }

    @Override
    public long getUsed() {
        return used;
    }

    public void setUsed(final long used) {
        this.used = used;
    }

    @Override
    public long getAvailable() {
        return available;
    }

    public void setAvailable(final long available) {
        this.available = available;
    }

    @Override
    public boolean refresh() {
        return storageAdaptor.refresh(this);
    }

    @Override
    public boolean isExternalSnapshot() {
        if (type == StoragePoolType.CLVM || type == StoragePoolType.RBD) {
            return true;
        }
        return false;
    }

    @Override
    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(final String localPath) {
        this.localPath = localPath;
    }

    @Override
    public String getSourceHost() {
        return sourceHost;
    }

    public void setSourceHost(final String host) {
        sourceHost = host;
    }

    @Override
    public String getSourceDir() {
        return sourceDir;
    }

    @Override
    public int getSourcePort() {
        return sourcePort;
    }

    @Override
    public String getAuthUserName() {
        return authUsername;
    }

    @Override
    public String getAuthSecret() {
        return authSecret;
    }

    public void setAuthSecret(final String authSecret) {
        this.authSecret = authSecret;
    }

    @Override
    public StoragePoolType getType() {
        return type;
    }

    @Override
    public boolean delete() {
        try {
            return storageAdaptor.deleteStoragePool(this);
        } catch (final Exception e) {
            logger.debug("Failed to delete storage pool", e);
        }
        return false;
    }

    @Override
    public PhysicalDiskFormat getDefaultFormat() {
        if (getStoragePoolType() == StoragePoolType.CLVM || getStoragePoolType() == StoragePoolType.RBD) {
            return PhysicalDiskFormat.RAW;
        } else {
            return PhysicalDiskFormat.QCOW2;
        }
    }

    @Override
    public boolean createFolder(final String path) {
        return storageAdaptor.createFolder(uuid, path);
    }

    public void setSourcePort(final int port) {
        sourcePort = port;
    }

    public void setSourceDir(final String dir) {
        sourceDir = dir;
    }

    public void setAuthUsername(final String authUsername) {
        this.authUsername = authUsername;
    }

    public StoragePool getPool() {
        return pool;
    }

    public void setPool(final StoragePool pool) {
        this.pool = pool;
    }
}
