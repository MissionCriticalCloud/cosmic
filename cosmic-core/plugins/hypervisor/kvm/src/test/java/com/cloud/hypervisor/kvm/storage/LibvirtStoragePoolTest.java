package com.cloud.hypervisor.kvm.storage;

import com.cloud.storage.Storage.StoragePoolType;
import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

import junit.framework.TestCase;
import org.libvirt.StoragePool;
import org.mockito.Mockito;

public class LibvirtStoragePoolTest extends TestCase {

    public void testAttributes() {
        final String uuid = "4c4fb08b-373e-4f30-a120-3aa3a43f31da";
        final String name = "myfirstpool";

        final StoragePoolType type = StoragePoolType.NetworkFilesystem;

        final StorageAdaptor adapter = Mockito.mock(LibvirtStorageAdaptor.class);
        final StoragePool storage = Mockito.mock(StoragePool.class);

        final LibvirtStoragePool pool = new LibvirtStoragePool(uuid, name, type, adapter, storage);
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
        final String uuid = "f40cbf53-1f37-4c62-8912-801edf398f47";
        final String name = "myfirstpool";

        final StorageAdaptor adapter = Mockito.mock(LibvirtStorageAdaptor.class);
        final StoragePool storage = Mockito.mock(StoragePool.class);

        final LibvirtStoragePool nfsPool = new LibvirtStoragePool(uuid, name, StoragePoolType.NetworkFilesystem, adapter,
                storage);
        assertEquals(nfsPool.getDefaultFormat(), PhysicalDiskFormat.QCOW2);
        assertEquals(nfsPool.getStoragePoolType(), StoragePoolType.NetworkFilesystem);

        final LibvirtStoragePool rbdPool = new LibvirtStoragePool(uuid, name, StoragePoolType.RBD, adapter, storage);
        assertEquals(rbdPool.getDefaultFormat(), PhysicalDiskFormat.RAW);
        assertEquals(rbdPool.getStoragePoolType(), StoragePoolType.RBD);

        final LibvirtStoragePool clvmPool = new LibvirtStoragePool(uuid, name, StoragePoolType.CLVM, adapter, storage);
        assertEquals(clvmPool.getDefaultFormat(), PhysicalDiskFormat.RAW);
        assertEquals(clvmPool.getStoragePoolType(), StoragePoolType.CLVM);
    }

    public void testExternalSnapshot() {
        final String uuid = "60b46738-c5d0-40a9-a79e-9a4fe6295db7";
        final String name = "myfirstpool";

        final StorageAdaptor adapter = Mockito.mock(LibvirtStorageAdaptor.class);
        final StoragePool storage = Mockito.mock(StoragePool.class);

        final LibvirtStoragePool nfsPool = new LibvirtStoragePool(uuid, name, StoragePoolType.NetworkFilesystem, adapter,
                storage);
        assertFalse(nfsPool.isExternalSnapshot());

        final LibvirtStoragePool rbdPool = new LibvirtStoragePool(uuid, name, StoragePoolType.RBD, adapter, storage);
        assertTrue(rbdPool.isExternalSnapshot());

        final LibvirtStoragePool clvmPool = new LibvirtStoragePool(uuid, name, StoragePoolType.CLVM, adapter, storage);
        assertTrue(clvmPool.isExternalSnapshot());
    }
}
