package com.cloud.hypervisor.kvm.storage;

import java.util.List;
import java.util.Map;

import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;

import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

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

  public IscsiAdmStoragePool(String uuid, String host, int port, StoragePoolType storagePoolType,
      StorageAdaptor storageAdaptor) {
    this.uuid = uuid;
    sourceHost = host;
    sourcePort = port;
    this.storagePoolType = storagePoolType;
    this.storageAdaptor = storageAdaptor;
  }

  @Override
  public String getUuid() {
    return uuid;
  }

  @Override
  public String getSourceHost() {
    return sourceHost;
  }

  @Override
  public int getSourcePort() {
    return sourcePort;
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

  @Override
  public StoragePoolType getType() {
    return storagePoolType;
  }

  @Override
  public PhysicalDiskFormat getDefaultFormat() {
    return PhysicalDiskFormat.RAW;
  }

  // called from LibvirtComputingResource.copyPhysicalDisk(KVMPhysicalDisk, String, KVMStoragePool) and
  // from LibvirtComputingResource.createDiskFromTemplate(KVMPhysicalDisk, String, PhysicalDiskFormat, long,
  // KVMStoragePool)
  // does not apply for iScsiAdmStoragePool
  @Override
  public KvmPhysicalDisk createPhysicalDisk(String name, PhysicalDiskFormat format,
      Storage.ProvisioningType provisioningType, long size) {
    throw new UnsupportedOperationException("Creating a physical disk is not supported.");
  }

  // called from LibvirtComputingResource.execute(CreateCommand) and
  // from KVMStorageProcessor.createVolume(CreateObjectCommand)
  // does not apply for iScsiAdmStoragePool
  @Override
  public KvmPhysicalDisk createPhysicalDisk(String name, Storage.ProvisioningType provisioningType, long size) {
    throw new UnsupportedOperationException("Creating a physical disk is not supported.");
  }

  @Override
  public boolean connectPhysicalDisk(String name, Map<String, String> details) {
    return storageAdaptor.connectPhysicalDisk(name, this, details);
  }

  @Override
  public KvmPhysicalDisk getPhysicalDisk(String volumeUuid) {
    return storageAdaptor.getPhysicalDisk(volumeUuid, this);
  }

  @Override
  public boolean disconnectPhysicalDisk(String volumeUuid) {
    return storageAdaptor.disconnectPhysicalDisk(volumeUuid, this);
  }

  @Override
  public boolean deletePhysicalDisk(String volumeUuid, Storage.ImageFormat format) {
    return storageAdaptor.deletePhysicalDisk(volumeUuid, this, format);
  }

  // does not apply for iScsiAdmStoragePool
  @Override
  public List<KvmPhysicalDisk> listPhysicalDisks() {
    return storageAdaptor.listPhysicalDisks(uuid, this);
  }

  // does not apply for iScsiAdmStoragePool
  @Override
  public boolean refresh() {
    return storageAdaptor.refresh(this);
  }

  @Override
  public boolean delete() {
    return storageAdaptor.deleteStoragePool(this);
  }

  @Override
  public boolean createFolder(String path) {
    return storageAdaptor.createFolder(uuid, path);
  }

  @Override
  public boolean isExternalSnapshot() {
    return false;
  }

  @Override
  public String getAuthUserName() {
    return authUsername;
  }

  @Override
  public String getAuthSecret() {
    return authSecret;
  }

  @Override
  public String getSourceDir() {
    return sourceDir;
  }

  @Override
  public String getLocalPath() {
    return localPath;
  }
}