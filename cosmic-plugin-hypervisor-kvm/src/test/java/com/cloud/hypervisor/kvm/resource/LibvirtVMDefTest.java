package com.cloud.hypervisor.kvm.resource;

import com.cloud.hypervisor.kvm.resource.LibvirtVmDef.DiskDef;
import com.cloud.utils.Pair;

import junit.framework.TestCase;

public class LibvirtVMDefTest extends TestCase {

    public void testInterfaceEtehrnet() {
        final LibvirtVmDef.InterfaceDef ifDef = new LibvirtVmDef.InterfaceDef();
        ifDef.defEthernet("targetDeviceName", "00:11:22:aa:bb:dd", LibvirtVmDef.InterfaceDef.NicModel.VIRTIO);

        final String expected = "<interface type='ethernet'>\n" + "<target dev='targetDeviceName'/>\n"
                + "<mac address='00:11:22:aa:bb:dd'/>\n" + "<model type='virtio'/>\n"
                + "</interface>\n";

        assertEquals(expected, ifDef.toString());
    }

    public void testInterfaceDirectNet() {
        final LibvirtVmDef.InterfaceDef ifDef = new LibvirtVmDef.InterfaceDef();
        ifDef.defDirectNet("targetDeviceName", null, "00:11:22:aa:bb:dd", LibvirtVmDef.InterfaceDef.NicModel.VIRTIO,
                "private");

        final String expected = "<interface type='" + LibvirtVmDef.InterfaceDef.GuestNetType.DIRECT + "'>\n"
                + "<source dev='targetDeviceName' mode='private'/>\n" +
                "<mac address='00:11:22:aa:bb:dd'/>\n" + "<model type='virtio'/>\n" + "</interface>\n";

        assertEquals(expected, ifDef.toString());
    }

    public void testCpuModeDef() {
        final LibvirtVmDef.CpuModeDef cpuModeDef = new LibvirtVmDef.CpuModeDef();
        cpuModeDef.setMode("custom");
        cpuModeDef.setModel("Nehalem");

        final String expected1 = "<cpu mode='custom' match='exact'><model fallback='allow'>Nehalem</model></cpu>";

        assertEquals(expected1, cpuModeDef.toString());

        cpuModeDef.setMode("host-model");
        final String expected2 = "<cpu mode='host-model'><model fallback='allow'></model></cpu>";

        assertEquals(expected2, cpuModeDef.toString());

        cpuModeDef.setMode("host-passthrough");
        final String expected3 = "<cpu mode='host-passthrough'></cpu>";
        assertEquals(expected3, cpuModeDef.toString());
    }

    public void testDiskDef() {
        final String filePath = "/var/lib/libvirt/images/disk.qcow2";
        final String diskLabel = "vda";

        final DiskDef disk = new DiskDef();
        final DiskDef.DiskBus bus = DiskDef.DiskBus.VIRTIO;
        final DiskDef.DiskFmtType type = DiskDef.DiskFmtType.QCOW2;
        final DiskDef.DiskCacheMode cacheMode = DiskDef.DiskCacheMode.WRITEBACK;

        disk.defFileBasedDisk(filePath, diskLabel, bus, type);
        disk.setCacheMode(cacheMode);

        assertEquals(filePath, disk.getDiskPath());
        assertEquals(diskLabel, disk.getDiskLabel());
        assertEquals(bus, disk.getBusType());
        assertEquals(DiskDef.DeviceType.DISK, disk.getDeviceType());

        final String xmlDef = disk.toString();
        final String expectedXml = "<disk  device='disk' type='file'>\n<driver name='qemu' type='" + type.toString() + "' cache='"
                + cacheMode.toString() + "' />\n" +
                "<source file='" + filePath + "'/>\n<target dev='" + diskLabel + "' bus='" + bus.toString() + "'/>\n</disk>\n";

        assertEquals(xmlDef, expectedXml);
    }

    public void testHypervEnlightDef() {
        LibvirtVmDef.FeaturesDef featuresDef = new LibvirtVmDef.FeaturesDef();
        final LibvirtVmDef.HyperVEnlightenmentFeatureDef hyperVEnlightenmentFeatureDef = new LibvirtVmDef.HyperVEnlightenmentFeatureDef();
        hyperVEnlightenmentFeatureDef.setFeature("relaxed", true);
        hyperVEnlightenmentFeatureDef.setFeature("vapic", true);
        hyperVEnlightenmentFeatureDef.setFeature("spinlocks", true);
        hyperVEnlightenmentFeatureDef.setRetries(8096);
        featuresDef.addHyperVFeature(hyperVEnlightenmentFeatureDef);
        String defs = featuresDef.toString();
        assertTrue(defs.contains("relaxed"));
        assertTrue(defs.contains("vapic"));
        assertTrue(defs.contains("spinlocks"));

        featuresDef = new LibvirtVmDef.FeaturesDef();
        featuresDef.addFeatures("pae");
        defs = featuresDef.toString();
        assertFalse(defs.contains("relaxed"));
        assertFalse(defs.contains("vapic"));
        assertFalse(defs.contains("spinlocks"));
        assertTrue("Windows Server 2008 R2".contains("Windows Server 2008"));

        Pair<Integer, Integer> hostOsVersion = new Pair<>(6, 5);
        assertTrue((hostOsVersion.first() == 6 && hostOsVersion.second() >= 5) || (hostOsVersion.first() >= 7));
        hostOsVersion = new Pair<>(7, 1);
        assertTrue((hostOsVersion.first() == 6 && hostOsVersion.second() >= 5) || (hostOsVersion.first() >= 7));
    }

    public void testChannelDef() {
        final LibvirtVmDef.RngDef.RngBackendModel backendModel = LibvirtVmDef.RngDef.RngBackendModel.RANDOM;
        final String path = "/dev/random";

        final LibvirtVmDef.RngDef def = new LibvirtVmDef.RngDef(path, backendModel);
        assertEquals(def.getPath(), path);
        assertEquals(def.getRngBackendModel(), backendModel);
        assertEquals(def.getRngModel(), LibvirtVmDef.RngDef.RngModel.VIRTIO);
    }

    public void testWatchDogDef() {
        LibvirtVmDef.WatchDogDef def = null;

        def = new LibvirtVmDef.WatchDogDef(LibvirtVmDef.WatchDogDef.WatchDogAction.RESET,
                LibvirtVmDef.WatchDogDef.WatchDogModel.I6300ESB);
        assertEquals(def.getAction(), LibvirtVmDef.WatchDogDef.WatchDogAction.RESET);
        assertEquals(def.getModel(), LibvirtVmDef.WatchDogDef.WatchDogModel.I6300ESB);

        def = new LibvirtVmDef.WatchDogDef(LibvirtVmDef.WatchDogDef.WatchDogAction.POWEROFF,
                LibvirtVmDef.WatchDogDef.WatchDogModel.DIAG288);
        assertEquals(def.getAction(), LibvirtVmDef.WatchDogDef.WatchDogAction.POWEROFF);
        assertEquals(def.getModel(), LibvirtVmDef.WatchDogDef.WatchDogModel.DIAG288);
    }
}
