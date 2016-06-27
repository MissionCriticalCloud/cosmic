package com.cloud.hypervisor.kvm.storage;

import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;

import junit.framework.TestCase;
import org.mockito.Mockito;

public class KvmPhysicalDiskTest extends TestCase {

    public void testRBDStringBuilder() {
        assertEquals(KvmPhysicalDisk.rbdStringBuilder("ceph-monitor", 8000, "admin", "supersecret", "volume1"),
                "rbd:volume1:mon_host=ceph-monitor\\\\:8000:auth_supported=cephx:id=admin:key=supersecret:rbd_default_format=2:client_mount_timeout=30");
    }

    public void testAttributes() {
        final String name = "3bc186e0-6c29-45bf-b2b0-ddef6f91f5ef";
        final String path = "/" + name;

        final LibvirtStoragePool pool = Mockito.mock(LibvirtStoragePool.class);

        final KvmPhysicalDisk disk = new KvmPhysicalDisk(path, name, pool);
        assertEquals(disk.getName(), name);
        assertEquals(disk.getPath(), path);
        assertEquals(disk.getPool(), pool);
        assertEquals(disk.getSize(), 0);
        assertEquals(disk.getVirtualSize(), 0);

        disk.setSize(1024);
        disk.setVirtualSize(2048);
        assertEquals(disk.getSize(), 1024);
        assertEquals(disk.getVirtualSize(), 2048);

        disk.setFormat(PhysicalDiskFormat.RAW);
        assertEquals(disk.getFormat(), PhysicalDiskFormat.RAW);
    }
}
