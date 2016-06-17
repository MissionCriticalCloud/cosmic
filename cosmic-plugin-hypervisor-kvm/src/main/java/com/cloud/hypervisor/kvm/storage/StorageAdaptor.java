package com.cloud.hypervisor.kvm.storage;

import java.util.List;
import java.util.Map;

import com.cloud.storage.Storage;
import com.cloud.storage.Storage.StoragePoolType;

import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

public interface StorageAdaptor {

  public KvmStoragePool getStoragePool(String uuid);

  // Get the storage pool from libvirt, but control if libvirt should refresh the pool (can take a long time)
  public KvmStoragePool getStoragePool(String uuid, boolean refreshInfo);

  // given disk path (per database) and pool, create new KVMPhysicalDisk, populate
  // it with info from local disk, and return it
  public KvmPhysicalDisk getPhysicalDisk(String volumeUuid, KvmStoragePool pool);

  public KvmStoragePool createStoragePool(String name, String host, int port, String path, String userInfo,
      StoragePoolType type);

  public boolean deleteStoragePool(String uuid);

  public boolean deleteStoragePool(KvmStoragePool pool);

  public KvmPhysicalDisk createPhysicalDisk(String name, KvmStoragePool pool,
      PhysicalDiskFormat format, Storage.ProvisioningType provisioningType, long size);

  // given disk path (per database) and pool, prepare disk on host
  public boolean connectPhysicalDisk(String volumePath, KvmStoragePool pool, Map<String, String> details);

  // given disk path (per database) and pool, clean up disk on host
  public boolean disconnectPhysicalDisk(String volumePath, KvmStoragePool pool);

  // given local path to file/device (per Libvirt XML), 1) check that device is
  // handled by your adaptor, return false if not. 2) clean up device, return true
  public boolean disconnectPhysicalDiskByPath(String localPath);

  public boolean deletePhysicalDisk(String uuid, KvmStoragePool pool, Storage.ImageFormat format);

  public KvmPhysicalDisk createDiskFromTemplate(KvmPhysicalDisk template,
      String name, PhysicalDiskFormat format, Storage.ProvisioningType provisioningType, long size,
      KvmStoragePool destPool, int timeout);

  public KvmPhysicalDisk createTemplateFromDisk(KvmPhysicalDisk disk, String name, PhysicalDiskFormat format, long size,
      KvmStoragePool destPool);

  public List<KvmPhysicalDisk> listPhysicalDisks(String storagePoolUuid, KvmStoragePool pool);

  public KvmPhysicalDisk copyPhysicalDisk(KvmPhysicalDisk disk, String name, KvmStoragePool destPools, int timeout);

  public KvmPhysicalDisk createDiskFromSnapshot(KvmPhysicalDisk snapshot, String snapshotName, String name,
      KvmStoragePool destPool);

  public boolean refresh(KvmStoragePool pool);

  public boolean createFolder(String uuid, String path);
}