
package com.cloud.hypervisor.kvm.storage;

import com.cloud.storage.Storage.StoragePoolType;

import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;
import org.libvirt.StoragePool;
import org.mockito.Mockito;

import junit.framework.TestCase;

public class LibvirtStoragePoolTest extends TestCase {

  public void testAttributes() {
    String uuid = "4c4fb08b-373e-4f30-a120-3aa3a43f31da";
    String name = "myfirstpool";

    StoragePoolType type = StoragePoolType.NetworkFilesystem;

    StorageAdaptor adapter = Mockito.mock(LibvirtStorageAdaptor.class);
    StoragePool storage = Mockito.mock(StoragePool.class);

    LibvirtStoragePool pool = new LibvirtStoragePool(uuid, name, type, adapter, storage);
    assertEquals(pool.getCapacity(), 0);
    assertEquals(pool.getUsed(), 0);
    assertEquals(pool.getName(), name);
    assertEquals(pool.getUuid(), uuid);
    assertEquals(pool.getAvailable(), 0);
    assertEquals(pool.getStoragePoolType(), type);

    pool.setCapacity(2048);
    pool.setUsed(1024);
    pool.setAvailable(1023);

    assertEquals(pool.getCapacity(), 2048);
    assertEquals(pool.getUsed(), 1024);
    assertEquals(pool.getAvailable(), 1023);
  }

  public void testDefaultFormats() {
    String uuid = "f40cbf53-1f37-4c62-8912-801edf398f47";
    String name = "myfirstpool";

    StorageAdaptor adapter = Mockito.mock(LibvirtStorageAdaptor.class);
    StoragePool storage = Mockito.mock(StoragePool.class);

    LibvirtStoragePool nfsPool = new LibvirtStoragePool(uuid, name, StoragePoolType.NetworkFilesystem, adapter,
        storage);
    assertEquals(nfsPool.getDefaultFormat(), PhysicalDiskFormat.QCOW2);
    assertEquals(nfsPool.getStoragePoolType(), StoragePoolType.NetworkFilesystem);

    LibvirtStoragePool rbdPool = new LibvirtStoragePool(uuid, name, StoragePoolType.RBD, adapter, storage);
    assertEquals(rbdPool.getDefaultFormat(), PhysicalDiskFormat.RAW);
    assertEquals(rbdPool.getStoragePoolType(), StoragePoolType.RBD);

    LibvirtStoragePool clvmPool = new LibvirtStoragePool(uuid, name, StoragePoolType.CLVM, adapter, storage);
    assertEquals(clvmPool.getDefaultFormat(), PhysicalDiskFormat.RAW);
    assertEquals(clvmPool.getStoragePoolType(), StoragePoolType.CLVM);
  }

  public void testExternalSnapshot() {
    String uuid = "60b46738-c5d0-40a9-a79e-9a4fe6295db7";
    String name = "myfirstpool";

    StorageAdaptor adapter = Mockito.mock(LibvirtStorageAdaptor.class);
    StoragePool storage = Mockito.mock(StoragePool.class);

    LibvirtStoragePool nfsPool = new LibvirtStoragePool(uuid, name, StoragePoolType.NetworkFilesystem, adapter,
        storage);
    assertFalse(nfsPool.isExternalSnapshot());

    LibvirtStoragePool rbdPool = new LibvirtStoragePool(uuid, name, StoragePoolType.RBD, adapter, storage);
    assertTrue(rbdPool.isExternalSnapshot());

    LibvirtStoragePool clvmPool = new LibvirtStoragePool(uuid, name, StoragePoolType.CLVM, adapter, storage);
    assertTrue(clvmPool.isExternalSnapshot());
  }
}