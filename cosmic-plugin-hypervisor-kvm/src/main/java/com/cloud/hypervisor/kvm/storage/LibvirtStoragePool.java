package com.cloud.hypervisor.kvm.storage;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.utils.exception.CloudRuntimeException;

import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;
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

  public LibvirtStoragePool(String uuid, String name, StoragePoolType type, StorageAdaptor adaptor, StoragePool pool) {
    this.uuid = uuid;
    this.name = name;
    this.type = type;
    storageAdaptor = adaptor;
    capacity = 0;
    used = 0;
    available = 0;
    this.pool = pool;
  }

  public void setCapacity(long capacity) {
    this.capacity = capacity;
  }

  @Override
  public long getCapacity() {
    return capacity;
  }

  public void setUsed(long used) {
    this.used = used;
  }

  public void setAvailable(long available) {
    this.available = available;
  }

  @Override
  public long getUsed() {
    return used;
  }

  @Override
  public long getAvailable() {
    return available;
  }

  public StoragePoolType getStoragePoolType() {
    return type;
  }

  public String getName() {
    return name;
  }

  @Override
  public String getUuid() {
    return uuid;
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
  public KvmPhysicalDisk createPhysicalDisk(String name,
      PhysicalDiskFormat format, Storage.ProvisioningType provisioningType, long size) {
    return storageAdaptor.createPhysicalDisk(name, this, format, provisioningType, size);
  }

  @Override
  public KvmPhysicalDisk createPhysicalDisk(String name, Storage.ProvisioningType provisioningType, long size) {
    return storageAdaptor.createPhysicalDisk(name, this,
        getDefaultFormat(), provisioningType, size);
  }

  @Override
  public KvmPhysicalDisk getPhysicalDisk(String volumeUid) {
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
  public boolean connectPhysicalDisk(String name, Map<String, String> details) {
    return true;
  }

  @Override
  public boolean disconnectPhysicalDisk(String uuid) {
    return true;
  }

  @Override
  public boolean deletePhysicalDisk(String uuid, Storage.ImageFormat format) {
    return storageAdaptor.deletePhysicalDisk(uuid, this, format);
  }

  @Override
  public List<KvmPhysicalDisk> listPhysicalDisks() {
    return storageAdaptor.listPhysicalDisks(uuid, this);
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

  public void setLocalPath(String localPath) {
    this.localPath = localPath;
  }

  @Override
  public String getAuthUserName() {
    return authUsername;
  }

  public void setAuthUsername(String authUsername) {
    this.authUsername = authUsername;
  }

  @Override
  public String getAuthSecret() {
    return authSecret;
  }

  public void setAuthSecret(String authSecret) {
    this.authSecret = authSecret;
  }

  @Override
  public String getSourceHost() {
    return sourceHost;
  }

  public void setSourceHost(String host) {
    sourceHost = host;
  }

  @Override
  public int getSourcePort() {
    return sourcePort;
  }

  public void setSourcePort(int port) {
    sourcePort = port;
  }

  @Override
  public String getSourceDir() {
    return sourceDir;
  }

  public void setSourceDir(String dir) {
    sourceDir = dir;
  }

  @Override
  public StoragePoolType getType() {
    return type;
  }

  public StoragePool getPool() {
    return pool;
  }

  public void setPool(StoragePool pool) {
    this.pool = pool;
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
  public boolean createFolder(String path) {
    return storageAdaptor.createFolder(uuid, path);
  }
}