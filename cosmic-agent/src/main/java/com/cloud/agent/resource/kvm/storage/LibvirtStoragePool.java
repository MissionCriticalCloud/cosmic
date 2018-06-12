package com.cloud.agent.resource.kvm.storage;

import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.PhysicalDiskFormat;
import com.cloud.model.enumeration.StoragePoolType;
import com.cloud.model.enumeration.StorageProvisioningType;

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
        this.storageAdaptor = adaptor;
        this.capacity = 0;
        this.used = 0;
        this.available = 0;
        this.pool = pool;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public KvmPhysicalDisk createPhysicalDisk(final String name, final PhysicalDiskFormat format, final StorageProvisioningType provisioningType, final long size) {
        return this.storageAdaptor.createPhysicalDisk(name, this, format, provisioningType, size);
    }

    @Override
    public KvmPhysicalDisk createPhysicalDisk(final String name, final StorageProvisioningType provisioningType, final long size) {
        return this.storageAdaptor.createPhysicalDisk(name, this, getDefaultFormat(), provisioningType, size);
    }

    public StoragePoolType getStoragePoolType() {
        return this.type;
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
            disk = this.storageAdaptor.getPhysicalDisk(volumeUuid, this);
        } catch (final CloudRuntimeException e) {
            if (getStoragePoolType() != StoragePoolType.NetworkFilesystem
                    && getStoragePoolType() != StoragePoolType.Filesystem) {
                throw e;
            }
        }

        if (disk != null) {
            return disk;
        }
        this.logger.debug("find volume bypass libvirt");
        // For network file system or file system, try to use java file to find the volume, instead of through libvirt.
        // BUG:CLOUDSTACK-4459
        final String localPoolPath = getLocalPath();
        final File f = new File(localPoolPath + File.separator + volumeUuid);
        if (!f.exists()) {
            this.logger.debug("volume: " + volumeUuid + " not exist on storage pool");
            throw new CloudRuntimeException("Can't find volume: " + volumeUuid);
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
    public boolean deletePhysicalDisk(final String uuid, final ImageFormat format) {
        return this.storageAdaptor.deletePhysicalDisk(uuid, this, format);
    }

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
        return this.capacity;
    }

    public void setCapacity(long capacity) {
        // Safe guard as the DB values are BIGINT unsigned so cannot be below zero
        if (capacity < 0) {
            capacity = 0;
        }
        this.capacity = capacity;
    }

    @Override
    public long getUsed() {
        return this.used;
    }

    public void setUsed(long used) {
        // Safe guard as the DB values are BIGINT unsigned so cannot be below zero
        if (used < 0) {
            used = 0;
        }
        this.used = used;
    }

    @Override
    public long getAvailable() {
        return this.available;
    }

    public void setAvailable(long available) {
        // Safe guard as the DB values are BIGINT unsigned so cannot be below zero
        if (available < 0) {
            available = 0;
        }
        this.available = available;
    }

    @Override
    public boolean refresh() {
        return this.storageAdaptor.refresh(this);
    }

    @Override
    public boolean isExternalSnapshot() {
        if (this.type == StoragePoolType.CLVM || this.type == StoragePoolType.RBD) {
            return true;
        }
        return false;
    }

    @Override
    public String getLocalPath() {
        return this.localPath;
    }

    public void setLocalPath(final String localPath) {
        this.localPath = localPath;
    }

    @Override
    public String getSourceHost() {
        return this.sourceHost;
    }

    public void setSourceHost(final String host) {
        this.sourceHost = host;
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

    public void setAuthSecret(final String authSecret) {
        this.authSecret = authSecret;
    }

    @Override
    public StoragePoolType getType() {
        return this.type;
    }

    @Override
    public boolean delete() {
        try {
            return this.storageAdaptor.deleteStoragePool(this);
        } catch (final Exception e) {
            this.logger.debug("Failed to delete storage pool", e);
        }
        return false;
    }

    @Override
    public PhysicalDiskFormat getDefaultFormat() {
        switch (getStoragePoolType()) {
            case LVM:
            case CLVM:
            case RBD:
                return PhysicalDiskFormat.RAW;
            default:
                return PhysicalDiskFormat.QCOW2;
        }
    }

    @Override
    public boolean createFolder(final String path) {
        return this.storageAdaptor.createFolder(this.uuid, path);
    }

    public void setSourcePort(final int port) {
        this.sourcePort = port;
    }

    public void setSourceDir(final String dir) {
        this.sourceDir = dir;
    }

    public void setAuthUsername(final String authUsername) {
        this.authUsername = authUsername;
    }

    public StoragePool getPool() {
        return this.pool;
    }

    public void setPool(final StoragePool pool) {
        this.pool = pool;
    }
}
