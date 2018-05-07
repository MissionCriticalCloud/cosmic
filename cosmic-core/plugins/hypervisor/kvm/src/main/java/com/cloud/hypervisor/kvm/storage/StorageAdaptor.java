package com.cloud.hypervisor.kvm.storage;

import com.cloud.legacymodel.storage.StorageProvisioningType;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.StoragePoolType;
import com.cloud.utils.qemu.QemuImg.PhysicalDiskFormat;

import java.util.List;
import java.util.Map;

public interface StorageAdaptor {

    KvmStoragePool getStoragePool(String uuid);

    // Get the storage pool from libvirt, but control if libvirt should refresh the pool (can take a long time)
    KvmStoragePool getStoragePool(String uuid, boolean refreshInfo);

    // given disk path (per database) and pool, create new KVMPhysicalDisk, populate
    // it with info from local disk, and return it
    KvmPhysicalDisk getPhysicalDisk(String volumeUuid, KvmStoragePool pool);

    KvmStoragePool createStoragePool(String name, String host, int port, String path, String userInfo,
                                     StoragePoolType type);

    boolean deleteStoragePool(String uuid);

    boolean deleteStoragePool(KvmStoragePool pool);

    KvmPhysicalDisk createPhysicalDisk(String name, KvmStoragePool pool,
                                       PhysicalDiskFormat format, StorageProvisioningType provisioningType, long size);

    // given disk path (per database) and pool, prepare disk on host
    boolean connectPhysicalDisk(String volumePath, KvmStoragePool pool, Map<String, String> details);

    // given disk path (per database) and pool, clean up disk on host
    boolean disconnectPhysicalDisk(String volumePath, KvmStoragePool pool);

    // given local path to file/device (per Libvirt XML), 1) check that device is
    // handled by your adaptor, return false if not. 2) clean up device, return true
    boolean disconnectPhysicalDiskByPath(String localPath);

    boolean deletePhysicalDisk(String uuid, KvmStoragePool pool, ImageFormat format);

    KvmPhysicalDisk createDiskFromTemplate(KvmPhysicalDisk template,
                                           String name, PhysicalDiskFormat format, StorageProvisioningType provisioningType, long size,
                                           KvmStoragePool destPool, int timeout);

    KvmPhysicalDisk createTemplateFromDisk(KvmPhysicalDisk disk, String name, PhysicalDiskFormat format, long size,
                                           KvmStoragePool destPool);

    List<KvmPhysicalDisk> listPhysicalDisks(String storagePoolUuid, KvmStoragePool pool);

    KvmPhysicalDisk copyPhysicalDisk(KvmPhysicalDisk disk, String name, KvmStoragePool destPools, int timeout);

    KvmPhysicalDisk createDiskFromSnapshot(KvmPhysicalDisk snapshot, String snapshotName, String name,
                                           KvmStoragePool destPool);

    boolean refresh(KvmStoragePool pool);

    boolean createFolder(String uuid, String path);
}
