package com.cloud.agent.resource.kvm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloud.agent.resource.kvm.ha.KvmHaBase.NfsStoragePool;
import com.cloud.agent.resource.kvm.ha.KvmHaMonitor;
import com.cloud.agent.resource.kvm.storage.KvmPhysicalDisk;
import com.cloud.agent.resource.kvm.storage.KvmStoragePool;
import com.cloud.agent.resource.kvm.storage.KvmStoragePoolManager;
import com.cloud.agent.resource.kvm.vif.VifDriver;
import com.cloud.agent.resource.kvm.wrapper.LibvirtRequestWrapper;
import com.cloud.agent.resource.kvm.wrapper.LibvirtUtilitiesHelper;
import com.cloud.agent.resource.kvm.xml.LibvirtDiskDef;
import com.cloud.agent.resource.kvm.xml.LibvirtVmDef;
import com.cloud.agent.resource.kvm.xml.LibvirtVmDef.InterfaceDef;
import com.cloud.common.storageprocessor.Processor;
import com.cloud.common.storageprocessor.TemplateLocation;
import com.cloud.common.storageprocessor.resource.StorageSubsystemCommandHandler;
import com.cloud.common.virtualnetwork.VirtualRoutingResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.answer.AttachAnswer;
import com.cloud.legacymodel.communication.answer.CheckRouterAnswer;
import com.cloud.legacymodel.communication.command.AttachCommand;
import com.cloud.legacymodel.communication.command.AttachIsoCommand;
import com.cloud.legacymodel.communication.command.BackupSnapshotCommand;
import com.cloud.legacymodel.communication.command.CheckConsoleProxyLoadCommand;
import com.cloud.legacymodel.communication.command.CheckHealthCommand;
import com.cloud.legacymodel.communication.command.CheckNetworkCommand;
import com.cloud.legacymodel.communication.command.CheckOnHostCommand;
import com.cloud.legacymodel.communication.command.CheckRouterCommand;
import com.cloud.legacymodel.communication.command.CheckSshCommand;
import com.cloud.legacymodel.communication.command.CheckVirtualMachineCommand;
import com.cloud.legacymodel.communication.command.CopyVolumeCommand;
import com.cloud.legacymodel.communication.command.CreateCommand;
import com.cloud.legacymodel.communication.command.CreatePrivateTemplateFromSnapshotCommand;
import com.cloud.legacymodel.communication.command.CreatePrivateTemplateFromVolumeCommand;
import com.cloud.legacymodel.communication.command.CreateStoragePoolCommand;
import com.cloud.legacymodel.communication.command.CreateVolumeFromSnapshotCommand;
import com.cloud.legacymodel.communication.command.DeleteStoragePoolCommand;
import com.cloud.legacymodel.communication.command.DestroyCommand;
import com.cloud.legacymodel.communication.command.FenceCommand;
import com.cloud.legacymodel.communication.command.GetHostStatsCommand;
import com.cloud.legacymodel.communication.command.GetStorageStatsCommand;
import com.cloud.legacymodel.communication.command.GetVmDiskStatsCommand;
import com.cloud.legacymodel.communication.command.GetVmStatsCommand;
import com.cloud.legacymodel.communication.command.GetVncPortCommand;
import com.cloud.legacymodel.communication.command.MaintainCommand;
import com.cloud.legacymodel.communication.command.ManageSnapshotCommand;
import com.cloud.legacymodel.communication.command.MigrateCommand;
import com.cloud.legacymodel.communication.command.ModifySshKeysCommand;
import com.cloud.legacymodel.communication.command.ModifyStoragePoolCommand;
import com.cloud.legacymodel.communication.command.NetworkUsageCommand;
import com.cloud.legacymodel.communication.command.PingTestCommand;
import com.cloud.legacymodel.communication.command.PlugNicCommand;
import com.cloud.legacymodel.communication.command.PrepareForMigrationCommand;
import com.cloud.legacymodel.communication.command.PrimaryStorageDownloadCommand;
import com.cloud.legacymodel.communication.command.PvlanSetupCommand;
import com.cloud.legacymodel.communication.command.ReadyCommand;
import com.cloud.legacymodel.communication.command.RebootCommand;
import com.cloud.legacymodel.communication.command.RebootRouterCommand;
import com.cloud.legacymodel.communication.command.ResizeVolumeCommand;
import com.cloud.legacymodel.communication.command.StartCommand;
import com.cloud.legacymodel.communication.command.StopCommand;
import com.cloud.legacymodel.communication.command.UnPlugNicCommand;
import com.cloud.legacymodel.communication.command.UpdateHostPasswordCommand;
import com.cloud.legacymodel.communication.command.UpgradeSnapshotCommand;
import com.cloud.legacymodel.communication.command.WatchConsoleProxyLoadCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.InternalErrorException;
import com.cloud.legacymodel.network.PhysicalNetworkSetupInfo;
import com.cloud.legacymodel.storage.DiskProfile;
import com.cloud.legacymodel.storage.StoragePool;
import com.cloud.legacymodel.storage.TemplateFormatInfo;
import com.cloud.legacymodel.storage.Volume;
import com.cloud.legacymodel.to.DataStoreTO;
import com.cloud.legacymodel.to.DiskTO;
import com.cloud.legacymodel.to.NicTO;
import com.cloud.legacymodel.to.StorageFilerTO;
import com.cloud.legacymodel.to.VirtualMachineTO;
import com.cloud.legacymodel.to.VolumeTO;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.legacymodel.vm.BootloaderType;
import com.cloud.legacymodel.vm.VirtualMachine;
import com.cloud.legacymodel.vm.VirtualMachine.PowerState;
import com.cloud.legacymodel.vm.VmStatsEntry;
import com.cloud.model.enumeration.ImageFormat;
import com.cloud.model.enumeration.PhysicalDiskFormat;
import com.cloud.model.enumeration.StoragePoolType;
import com.cloud.model.enumeration.TrafficType;
import com.cloud.model.enumeration.VirtualMachineType;
import com.cloud.model.enumeration.VolumeType;
import com.cloud.utils.linux.CpuStat;
import com.cloud.utils.linux.MemStat;
import com.cloud.utils.script.Script;
import com.cloud.utils.storage.StorageLayer;

import javax.naming.ConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainBlockStats;
import org.libvirt.DomainInfo;
import org.libvirt.DomainInfo.DomainState;
import org.libvirt.DomainInterfaceStats;
import org.libvirt.LibvirtException;
import org.libvirt.NodeInfo;
import org.libvirt.StorageVol;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@RunWith(MockitoJUnitRunner.class)
public class LibvirtComputingResourceTest {

    private static final String VMNAME = "test";
    String _hyperVisorType = "kvm";
    Random _random = new Random();
    @Mock
    private LibvirtComputingResource libvirtComputingResource;

    /**
     * This test tests if the Agent can handle a vmSpec coming from a <=4.1 management server.
     */
    @Test
    public void testcreateVmFromSpecLegacy() {
        final int id = this._random.nextInt(65534);
        final String name = "test-instance-1";

        final int cpus = this._random.nextInt(2) + 1;
        final int minRam = 256 * 1024;
        final int maxRam = 512 * 1024;

        final String os = "Ubuntu";

        final String vncPassword = "mySuperSecretPassword";

        final LibvirtComputingResource lcr = new LibvirtComputingResource();
        final VirtualMachineTO to = new VirtualMachineTO(id, name, VirtualMachineType.User, cpus, minRam, maxRam,
                BootloaderType.HVM, os, false, false, vncPassword);
        to.setUuid("b0f0a72d-7efb-3cad-a8ff-70ebf30b3af9");

        final LibvirtVmDef vm = lcr.createVmFromSpec(to);
        vm.setHvsType(this._hyperVisorType);

        verifyVm(to, vm);
    }

    private void verifyVm(final VirtualMachineTO to, final LibvirtVmDef vm) {
        final Document domainDoc = parse(vm.toString());
        assertXpath(domainDoc, "/domain/@type", vm.getHvsType());
        assertXpath(domainDoc, "/domain/name/text()", to.getName());
        assertXpath(domainDoc, "/domain/uuid/text()", to.getUuid());
        assertXpath(domainDoc, "/domain/description/text()", "This VM is optimised for: " + to.getOptimiseFor().toString());
        assertXpath(domainDoc, "/domain/clock/@offset", "utc");
        assertNodeExists(domainDoc, "/domain/features/pae");
        assertNodeExists(domainDoc, "/domain/features/apic");
        assertNodeExists(domainDoc, "/domain/features/acpi");
        assertXpath(domainDoc, "/domain/devices/serial/@type", "pty");
        assertXpath(domainDoc, "/domain/devices/serial/target/@port", "0");
        assertXpath(domainDoc, "/domain/devices/graphics/@type", "vnc");
        assertXpath(domainDoc, "/domain/devices/graphics/@listen", "0.0.0.0");
        assertXpath(domainDoc, "/domain/devices/graphics/@autoport", "yes");
        assertXpath(domainDoc, "/domain/devices/graphics/@passwd", to.getVncPassword());

        assertXpath(domainDoc, "/domain/devices/console/@type", "pty");
        assertXpath(domainDoc, "/domain/devices/console/target/@port", "0");
        assertXpath(domainDoc, "/domain/devices/input/@type", "tablet");
        assertXpath(domainDoc, "/domain/devices/input/@bus", "usb");

        assertXpath(domainDoc, "/domain/memory/text()", String.valueOf(to.getMaxRam() / 1024));
        assertXpath(domainDoc, "/domain/currentMemory/text()", String.valueOf(to.getMinRam() / 1024));

        assertXpath(domainDoc, "/domain/devices/memballoon/@model", "virtio");
        assertXpath(domainDoc, "/domain/vcpu/text()", String.valueOf(to.getCpus()));

        assertXpath(domainDoc, "/domain/os/type/@machine", "pc");
        assertXpath(domainDoc, "/domain/os/type/text()", "hvm");

        assertNodeExists(domainDoc, "/domain/cpu");
        assertNodeExists(domainDoc, "/domain/os/boot[@dev='cdrom']");
        assertNodeExists(domainDoc, "/domain/os/boot[@dev='hd']");

        assertXpath(domainDoc, "/domain/on_reboot/text()", "destroy");
        assertXpath(domainDoc, "/domain/on_poweroff/text()", "destroy");
        assertXpath(domainDoc, "/domain/on_crash/text()", "destroy");

        assertXpath(domainDoc, "/domain/devices/watchdog/@model", "i6300esb");
        assertXpath(domainDoc, "/domain/devices/watchdog/@action", "none");
    }

    static Document parse(final String input) {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new ByteArrayInputStream(input.getBytes()));
        } catch (final SAXException | IOException | ParserConfigurationException e) {
            throw new IllegalArgumentException("Cloud not parse: " + input, e);
        }
    }

    static void assertXpath(final Document doc, final String xPathExpr,
                            final String expected) {
        try {
            Assert.assertEquals(expected, XPathFactory.newInstance().newXPath().evaluate(xPathExpr, doc));
        } catch (final XPathExpressionException e) {
            Assert.fail("Could not evaluate xpath" + xPathExpr + ":"
                    + e.getMessage());
        }
    }

    static void assertNodeExists(final Document doc, final String xPathExpr) {
        try {
            Assert.assertNotNull(XPathFactory.newInstance().newXPath().evaluate(xPathExpr, doc, XPathConstants.NODE));
        } catch (final XPathExpressionException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * This test verifies that CPU topology is properly set for hex-core
     */
    @Test
    public void testcreateVmFromSpecWithTopology6() {
        final int id = this._random.nextInt(65534);
        final String name = "test-instance-1";

        final int cpus = 12;
        final int minRam = 256 * 1024;
        final int maxRam = 512 * 1024;

        final String os = "Ubuntu";

        final String vncPassword = "mySuperSecretPassword";

        final LibvirtComputingResource lcr = new LibvirtComputingResource();
        final VirtualMachineTO to = new VirtualMachineTO(id, name, VirtualMachineType.User, cpus,
                minRam, maxRam, BootloaderType.HVM, os, false, false, vncPassword);
        to.setUuid("b0f0a72d-7efb-3cad-a8ff-70ebf30b3af9");

        final LibvirtVmDef vm = lcr.createVmFromSpec(to);
        vm.setHvsType(this._hyperVisorType);

        verifyVm(to, vm);
    }

    /**
     * This test verifies that CPU topology is properly set for quad-core
     */
    @Test
    public void testcreateVmFromSpecWithTopology4() {
        final int id = this._random.nextInt(65534);
        final String name = "test-instance-1";

        final int cpus = 8;
        final int minRam = 256 * 1024;
        final int maxRam = 512 * 1024;

        final String os = "Ubuntu";

        final String vncPassword = "mySuperSecretPassword";

        final LibvirtComputingResource lcr = new LibvirtComputingResource();
        final VirtualMachineTO to = new VirtualMachineTO(id, name, VirtualMachineType.User, cpus,
                minRam, maxRam, BootloaderType.HVM, os, false, false, vncPassword);
        to.setUuid("b0f0a72d-7efb-3cad-a8ff-70ebf30b3af9");

        final LibvirtVmDef vm = lcr.createVmFromSpec(to);
        vm.setHvsType(this._hyperVisorType);

        verifyVm(to, vm);
    }

    /**
     * This test tests if the Agent can handle a vmSpec coming from a >4.1 management server.
     */
    @Test
    public void testcreateVmFromSpec() {
        final int id = this._random.nextInt(65534);
        final String name = "test-instance-1";

        final int cpus = this._random.nextInt(2) + 1;
        final int minRam = 256 * 1024;
        final int maxRam = 512 * 1024;

        final String os = "Ubuntu";

        final String vncPassword = "mySuperSecretPassword";

        final LibvirtComputingResource lcr = new LibvirtComputingResource();
        final VirtualMachineTO to = new VirtualMachineTO(id, name, VirtualMachineType.User, cpus,
                minRam, maxRam, BootloaderType.HVM, os, false, false, vncPassword);
        to.setUuid("b0f0a72d-7efb-3cad-a8ff-70ebf30b3af9");

        final LibvirtVmDef vm = lcr.createVmFromSpec(to);
        vm.setHvsType(this._hyperVisorType);

        verifyVm(to, vm);
    }

    @Test
    public void testGetNicStats() {
        // this test is only working on linux because of the loopback interface name
        // also the tested code seems to work only on linux
        Assume.assumeTrue(SystemUtils.IS_OS_LINUX);
        final LibvirtComputingResource libvirtComputingResource = new LibvirtComputingResource();
        final Pair<Double, Double> stats = libvirtComputingResource.getNicStats("lo");
        assertNotNull(stats);
    }

    @Test
    public void diskUuidToSerialTest() {
        final String uuid = "38400000-8cf0-11bd-b24e-10b96e4ef00d";
        final String expected = "384000008cf011bdb24e";
        final LibvirtComputingResource lcr = new LibvirtComputingResource();
        Assert.assertEquals(expected, lcr.diskUuidToSerial(uuid));
    }

    @Test
    public void testUUID() {
        String uuid = "1";
        final LibvirtComputingResource lcr = new LibvirtComputingResource();
        uuid = lcr.getUuid(uuid);
        Assert.assertTrue(!uuid.equals("1"));

        final String oldUuid = UUID.randomUUID().toString();
        uuid = oldUuid;
        uuid = lcr.getUuid(uuid);
        Assert.assertTrue(uuid.equals(oldUuid));
    }

    @Test
    public void testGetVmStat() throws LibvirtException {
        final Connect connect = Mockito.mock(Connect.class);
        final Domain domain = Mockito.mock(Domain.class);
        final DomainInfo domainInfo = new DomainInfo();
        Mockito.when(domain.getInfo()).thenReturn(domainInfo);
        Mockito.when(connect.domainLookupByName(VMNAME)).thenReturn(domain);
        final NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.cpus = 8;
        nodeInfo.memory = 8 * 1024 * 1024;
        nodeInfo.sockets = 2;
        nodeInfo.threads = 2;
        nodeInfo.model = "Foo processor";
        Mockito.when(connect.nodeInfo()).thenReturn(nodeInfo);
        // this is testing the interface stats, returns an increasing number of sent and received bytes
        Mockito.when(domain.interfaceStats(Matchers.anyString())).thenAnswer(
                new org.mockito.stubbing.Answer<DomainInterfaceStats>() {
                    // increment with less than a KB, so this should be less than 1 KB
                    final static int increment = 1000;
                    int rxBytes = 1000;
                    int txBytes = 1000;

                    @Override
                    public DomainInterfaceStats answer(final InvocationOnMock invocation) throws Throwable {
                        final DomainInterfaceStats domainInterfaceStats = new DomainInterfaceStats();
                        domainInterfaceStats.rx_bytes = this.rxBytes += increment;
                        domainInterfaceStats.tx_bytes = this.txBytes += increment;
                        return domainInterfaceStats;
                    }
                });

        Mockito.when(domain.blockStats(Matchers.anyString())).thenAnswer(
                new org.mockito.stubbing.Answer<DomainBlockStats>() {
                    // a little less than a KB
                    final static int increment = 1000;

                    int rdBytes = 0;
                    int wrBytes = 1024;

                    @Override
                    public DomainBlockStats answer(final InvocationOnMock invocation) throws Throwable {
                        final DomainBlockStats domainBlockStats = new DomainBlockStats();

                        domainBlockStats.rd_bytes = this.rdBytes += increment;
                        domainBlockStats.wr_bytes = this.wrBytes += increment;
                        return domainBlockStats;
                    }
                });

        final LibvirtComputingResource libvirtComputingResource = new LibvirtComputingResource() {
            @Override
            public List<InterfaceDef> getInterfaces(final Connect conn, final String vmName) {
                final InterfaceDef interfaceDef = new InterfaceDef();
                return Arrays.asList(interfaceDef);
            }

            @Override
            public List<LibvirtDiskDef> getDisks(final Connect conn, final String vmName) {
                final LibvirtDiskDef diskDef = new LibvirtDiskDef();
                return Arrays.asList(diskDef);
            }
        };
        libvirtComputingResource.getVmStat(connect, VMNAME);
        final VmStatsEntry vmStat = libvirtComputingResource.getVmStat(connect, VMNAME);
        // network traffic as generated by the logic above, must be greater than zero
        Assert.assertTrue(vmStat.getNetworkReadKBs() > 0);
        Assert.assertTrue(vmStat.getNetworkWriteKBs() > 0);
        // IO traffic as generated by the logic above, must be greater than zero
        Assert.assertTrue(vmStat.getDiskReadKBs() > 0);
        Assert.assertTrue(vmStat.getDiskWriteKBs() > 0);
    }

    @Test
    public void getCpuSpeed() {
        Assume.assumeTrue(SystemUtils.IS_OS_LINUX);
        final NodeInfo nodeInfo = Mockito.mock(NodeInfo.class);
        LibvirtComputingResource.getCpuSpeed(nodeInfo);
    }

    /*
     * New Tests
     */

    @Test
    public void testStopCommandNoCheck() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final StopCommand command = new StopCommand(vmName, false, false);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);

        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testStopCommandCheckVmNOTRunning() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Domain vm = Mockito.mock(Domain.class);
        final DomainInfo info = Mockito.mock(DomainInfo.class);
        final DomainState state = DomainInfo.DomainState.VIR_DOMAIN_SHUTDOWN;
        info.state = state;

        final String vmName = "Test";
        final StopCommand command = new StopCommand(vmName, false, true);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
            when(conn.domainLookupByName(command.getVmName())).thenReturn(vm);

            when(vm.getInfo()).thenReturn(info);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);

        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(2)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testStopCommandCheckException1() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Domain vm = Mockito.mock(Domain.class);
        final DomainInfo info = Mockito.mock(DomainInfo.class);
        final DomainState state = DomainInfo.DomainState.VIR_DOMAIN_RUNNING;
        info.state = state;

        final String vmName = "Test";
        final StopCommand command = new StopCommand(vmName, false, true);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenThrow(LibvirtException.class);
            when(conn.domainLookupByName(command.getVmName())).thenReturn(vm);

            when(vm.getInfo()).thenReturn(info);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);

        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(2)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testStopCommandCheckVmRunning() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Domain vm = Mockito.mock(Domain.class);
        final DomainInfo info = Mockito.mock(DomainInfo.class);
        final DomainState state = DomainInfo.DomainState.VIR_DOMAIN_RUNNING;
        info.state = state;

        final String vmName = "Test";
        final StopCommand command = new StopCommand(vmName, false, true);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
            when(conn.domainLookupByName(command.getVmName())).thenReturn(vm);

            when(vm.getInfo()).thenReturn(info);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);

        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetVmStatsCommand() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final String uuid = "e8d6b4d0-bc6d-4613-b8bb-cb9e0600f3c6";
        final List<String> vms = new ArrayList<>();
        vms.add(vmName);

        final GetVmStatsCommand command = new GetVmStatsCommand(vms, uuid, "hostname");

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetVmDiskStatsCommand() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final String uuid = "e8d6b4d0-bc6d-4613-b8bb-cb9e0600f3c6";
        final List<String> vms = new ArrayList<>();
        vms.add(vmName);

        final GetVmDiskStatsCommand command = new GetVmDiskStatsCommand(vms, uuid, "hostname");

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnection()).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnection();
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetVmDiskStatsCommandException() {
        Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final String uuid = "e8d6b4d0-bc6d-4613-b8bb-cb9e0600f3c6";
        final List<String> vms = new ArrayList<>();
        vms.add(vmName);

        final GetVmDiskStatsCommand command = new GetVmDiskStatsCommand(vms, uuid, "hostname");

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnection()).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnection();
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRebootCommand() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final RebootCommand command = new RebootCommand(vmName, true);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRebootCommandException1() {
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final RebootCommand command = new RebootCommand(vmName, true);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRebootCommandError() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final RebootCommand command = new RebootCommand(vmName, true);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
            when(this.libvirtComputingResource.rebootVm(conn, command.getVmName())).thenReturn("error");
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRebootCommandException2() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final RebootCommand command = new RebootCommand(vmName, true);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
            when(this.libvirtComputingResource.rebootVm(conn, command.getVmName())).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRebootRouterCommand() {
        final VirtualRoutingResource routingResource = Mockito.mock(VirtualRoutingResource.class);
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final RebootRouterCommand command = new RebootRouterCommand(vmName, "127.0.0.1");

        when(this.libvirtComputingResource.getVirtRouterResource()).thenReturn(routingResource);
        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getVirtRouterResource();

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRebootRouterCommandConnect() {
        final VirtualRoutingResource routingResource = Mockito.mock(VirtualRoutingResource.class);
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final RebootRouterCommand command = new RebootRouterCommand(vmName, "127.0.0.1");

        when(this.libvirtComputingResource.getVirtRouterResource()).thenReturn(routingResource);
        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(routingResource.connect(command.getPrivateIpAddress())).thenReturn(true);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getVirtRouterResource();
        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetHostStatsCommand() {
        Mockito.mock(LibvirtUtilitiesHelper.class);
        final CpuStat cpuStat = Mockito.mock(CpuStat.class);
        final MemStat memStat = Mockito.mock(MemStat.class);

        final String uuid = "e8d6b4d0-bc6d-4613-b8bb-cb9e0600f3c6";
        final GetHostStatsCommand command = new GetHostStatsCommand(uuid, "summer", 1l);

        when(this.libvirtComputingResource.getCpuStat()).thenReturn(cpuStat);
        when(this.libvirtComputingResource.getMemStat()).thenReturn(memStat);
        when(this.libvirtComputingResource.getNicStats(Matchers.anyString())).thenReturn(new Pair<>(1.0d, 1.0d));
        when(cpuStat.getCpuUsedPercent()).thenReturn(0.5d);
        when(memStat.getAvailable()).thenReturn(1500L);
        when(memStat.getTotal()).thenReturn(15000L);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getCpuStat();
        verify(this.libvirtComputingResource, times(1)).getMemStat();
        verify(cpuStat, times(1)).getCpuUsedPercent();
        verify(memStat, times(1)).getAvailable();
        verify(memStat, times(1)).getTotal();
    }

    @Test
    public void testCheckHealthCommand() {
        final CheckHealthCommand command = new CheckHealthCommand();

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());
    }

    @Test
    public void testPrepareForMigrationCommand() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final VirtualMachineTO vm = Mockito.mock(VirtualMachineTO.class);
        final KvmStoragePoolManager storagePoolManager = Mockito.mock(KvmStoragePoolManager.class);
        final NicTO nicTO = Mockito.mock(NicTO.class);
        final DiskTO diskTO = Mockito.mock(DiskTO.class);
        final VifDriver vifDriver = Mockito.mock(VifDriver.class);

        final PrepareForMigrationCommand command = new PrepareForMigrationCommand(vm);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vm.getName())).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        when(vm.getNics()).thenReturn(new NicTO[]{nicTO});
        when(vm.getDisks()).thenReturn(new DiskTO[]{diskTO});

        when(nicTO.getType()).thenReturn(TrafficType.Guest);
        when(diskTO.getType()).thenReturn(VolumeType.ISO);

        when(this.libvirtComputingResource.getVifDriver(nicTO.getType())).thenReturn(vifDriver);
        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolManager);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vm.getName());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(vm, times(1)).getNics();
        verify(vm, times(1)).getDisks();
        verify(diskTO, times(1)).getType();
    }

    @Test
    public void testPrepareForMigrationCommandMigration() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final VirtualMachineTO vm = Mockito.mock(VirtualMachineTO.class);
        final KvmStoragePoolManager storagePoolManager = Mockito.mock(KvmStoragePoolManager.class);
        final NicTO nicTO = Mockito.mock(NicTO.class);
        final DiskTO diskTO = Mockito.mock(DiskTO.class);
        final VifDriver vifDriver = Mockito.mock(VifDriver.class);

        final PrepareForMigrationCommand command = new PrepareForMigrationCommand(vm);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vm.getName())).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        when(vm.getNics()).thenReturn(new NicTO[]{nicTO});
        when(vm.getDisks()).thenReturn(new DiskTO[]{diskTO});

        when(nicTO.getType()).thenReturn(TrafficType.Guest);
        when(diskTO.getType()).thenReturn(VolumeType.ISO);

        when(this.libvirtComputingResource.getVifDriver(nicTO.getType())).thenReturn(vifDriver);
        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolManager);
        when(storagePoolManager.connectPhysicalDisksViaVmSpec(vm)).thenReturn(true);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vm.getName());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(vm, times(1)).getNics();
        verify(vm, times(1)).getDisks();
        verify(diskTO, times(1)).getType();
    }

    @Test
    public void testPrepareForMigrationCommandLibvirtException() {
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final VirtualMachineTO vm = Mockito.mock(VirtualMachineTO.class);
        final KvmStoragePoolManager storagePoolManager = Mockito.mock(KvmStoragePoolManager.class);
        final NicTO nicTO = Mockito.mock(NicTO.class);
        final VifDriver vifDriver = Mockito.mock(VifDriver.class);

        final PrepareForMigrationCommand command = new PrepareForMigrationCommand(vm);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vm.getName())).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        when(vm.getNics()).thenReturn(new NicTO[]{nicTO});
        when(nicTO.getType()).thenReturn(TrafficType.Guest);

        when(this.libvirtComputingResource.getVifDriver(nicTO.getType())).thenReturn(vifDriver);
        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolManager);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vm.getName());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(vm, times(1)).getNics();
    }

    @Test
    public void testPrepareForMigrationCommandURISyntaxException() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final VirtualMachineTO vm = Mockito.mock(VirtualMachineTO.class);
        final KvmStoragePoolManager storagePoolManager = Mockito.mock(KvmStoragePoolManager.class);
        final NicTO nicTO = Mockito.mock(NicTO.class);
        final DiskTO volume = Mockito.mock(DiskTO.class);
        final VifDriver vifDriver = Mockito.mock(VifDriver.class);

        final PrepareForMigrationCommand command = new PrepareForMigrationCommand(vm);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vm.getName())).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        when(vm.getNics()).thenReturn(new NicTO[]{nicTO});
        when(vm.getDisks()).thenReturn(new DiskTO[]{volume});

        when(nicTO.getType()).thenReturn(TrafficType.Guest);
        when(volume.getType()).thenReturn(VolumeType.ISO);

        when(this.libvirtComputingResource.getVifDriver(nicTO.getType())).thenReturn(vifDriver);
        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolManager);
        try {
            when(this.libvirtComputingResource.getVolumePath(conn, volume)).thenThrow(URISyntaxException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final URISyntaxException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vm.getName());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(vm, times(1)).getNics();
        verify(vm, times(1)).getDisks();
        verify(volume, times(1)).getType();
    }

    @Test
    public void testPrepareForMigrationCommandInternalErrorException() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final VirtualMachineTO vm = Mockito.mock(VirtualMachineTO.class);
        final KvmStoragePoolManager storagePoolManager = Mockito.mock(KvmStoragePoolManager.class);
        final NicTO nicTO = Mockito.mock(NicTO.class);
        final DiskTO volume = Mockito.mock(DiskTO.class);

        final PrepareForMigrationCommand command = new PrepareForMigrationCommand(vm);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vm.getName())).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        when(vm.getNics()).thenReturn(new NicTO[]{nicTO});
        when(nicTO.getType()).thenReturn(TrafficType.Guest);

        when(this.libvirtComputingResource.getVifDriver(nicTO.getType())).thenThrow(InternalErrorException.class);
        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolManager);
        try {
            when(this.libvirtComputingResource.getVolumePath(conn, volume)).thenReturn("/path");
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final URISyntaxException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vm.getName());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(vm, times(1)).getNics();
    }

    @Test
    public void testMigrateCommand() {
        final Connect conn = Mockito.mock(Connect.class);
        final Connect dconn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final String destIp = "127.0.0.100";
        final boolean isWindows = false;
        final VirtualMachineTO vmTO = Mockito.mock(VirtualMachineTO.class);
        final boolean executeInSequence = false;

        final MigrateCommand command = new MigrateCommand(vmName, destIp, isWindows, vmTO, executeInSequence);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
            when(libvirtUtilitiesHelper.retrieveQemuConnection(
                    "qemu+tcp://" + command.getDestinationIp() + "/system")).thenReturn(dconn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final InterfaceDef interfaceDef = Mockito.mock(InterfaceDef.class);
        final List<InterfaceDef> ifaces = new ArrayList<>();
        ifaces.add(interfaceDef);

        when(this.libvirtComputingResource.getInterfaces(conn, vmName)).thenReturn(ifaces);

        final LibvirtDiskDef diskDef = Mockito.mock(LibvirtDiskDef.class);
        final List<LibvirtDiskDef> disks = new ArrayList<>();
        disks.add(diskDef);

        when(this.libvirtComputingResource.getDisks(conn, vmName)).thenReturn(disks);
        final Domain dm = Mockito.mock(Domain.class);
        try {
            when(conn.domainLookupByName(vmName)).thenReturn(dm);

            when(this.libvirtComputingResource.getPrivateIp()).thenReturn("127.0.0.1");
            when(dm.getXMLDesc(8)).thenReturn("host_domain");
            when(dm.getXMLDesc(1)).thenReturn("host_domain");
            when(dm.isPersistent()).thenReturn(1);
            doNothing().when(dm).undefine();
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final Exception e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
            verify(libvirtUtilitiesHelper, times(1)).retrieveQemuConnection(
                    "qemu+tcp://" + command.getDestinationIp() + "/system");
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        verify(this.libvirtComputingResource, times(1)).getInterfaces(conn, vmName);
        verify(this.libvirtComputingResource, times(1)).getDisks(conn, vmName);
        try {
            verify(conn, times(1)).domainLookupByName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        try {
            verify(dm, times(1)).getXMLDesc(8);
        } catch (final Throwable t) {
            try {
                verify(dm, times(1)).getXMLDesc(1);
            } catch (final LibvirtException e) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testPingTestHostIpCommand() {
        final PingTestCommand command = new PingTestCommand("127.0.0.1");

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());
    }

    @Test
    public void testPingTestPvtIpCommand() {
        final PingTestCommand command = new PingTestCommand("127.0.0.1", "127.0.0.1");

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());
    }

    @Test
    public void testPingOnlyOneIpCommand() {
        final PingTestCommand command = new PingTestCommand("127.0.0.1", null);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());
    }

    @Test
    public void testCheckVirtualMachineCommand() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final CheckVirtualMachineCommand command = new CheckVirtualMachineCommand(vmName);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        when(this.libvirtComputingResource.getVmState(conn, command.getVmName())).thenReturn(PowerState.PowerOn);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testExceptionCheckVirtualMachineCommand() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final CheckVirtualMachineCommand command = new CheckVirtualMachineCommand(vmName);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        when(this.libvirtComputingResource.getVmState(conn, command.getVmName())).thenReturn(PowerState.PowerOn);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testReadyCommand() {
        final ReadyCommand command = new ReadyCommand(1l);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());
    }

    @Test
    public void testAttachIsoCommand() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final AttachIsoCommand command = new AttachIsoCommand(vmName, "/path", true);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAttachIsoCommandLibvirtException() {
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final AttachIsoCommand command = new AttachIsoCommand(vmName, "/path", true);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAttachIsoCommandURISyntaxException() {
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final AttachIsoCommand command = new AttachIsoCommand(vmName, "/path", true);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenThrow(URISyntaxException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAttachIsoCommandInternalErrorException() {
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String vmName = "Test";
        final AttachIsoCommand command = new AttachIsoCommand(vmName, "/path", true);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenThrow(InternalErrorException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testWatchConsoleProxyLoadCommand() {
        final int interval = 0;
        final long proxyVmId = 0l;
        final String proxyVmName = "host";
        final String proxyManagementIp = "127.0.0.1";
        final int proxyCmdPort = 0;

        final WatchConsoleProxyLoadCommand command = new WatchConsoleProxyLoadCommand(interval, proxyVmId, proxyVmName,
                proxyManagementIp, proxyCmdPort);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());
    }

    @Test
    public void testCheckConsoleProxyLoadCommand() {
        final long proxyVmId = 0l;
        final String proxyVmName = "host";
        final String proxyManagementIp = "127.0.0.1";
        final int proxyCmdPort = 0;

        final CheckConsoleProxyLoadCommand command = new CheckConsoleProxyLoadCommand(proxyVmId, proxyVmName,
                proxyManagementIp, proxyCmdPort);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());
    }

    @Test
    public void testGetVncPortCommand() {
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final GetVncPortCommand command = new GetVncPortCommand(1l, "host");

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(command.getName())).thenReturn(conn);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(command.getName());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetVncPortCommandLibvirtException() {
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final GetVncPortCommand command = new GetVncPortCommand(1l, "host");

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(command.getName())).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(command.getName());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testModifySshKeysCommand() {
        final ModifySshKeysCommand command = new ModifySshKeysCommand("", "");

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);

        when(libvirtUtilitiesHelper.retrieveSshKeysPath()).thenReturn("/path/keys");
        when(libvirtUtilitiesHelper.retrieveSshPubKeyPath()).thenReturn("/path/pub/keys");
        when(libvirtUtilitiesHelper.retrieveSshPrvKeyPath()).thenReturn("/path/pvt/keys");

        when(this.libvirtComputingResource.getScriptsTimeout()).thenReturn(0);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getScriptsTimeout();
    }

    @Test
    public void testMaintainCommand() {
        final MaintainCommand command = new MaintainCommand();

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());
    }

    @Test
    public void testCreateCommandNoTemplate() {
        final DiskProfile diskCharacteristics = Mockito.mock(DiskProfile.class);
        final StorageFilerTO pool = Mockito.mock(StorageFilerTO.class);
        final boolean executeInSequence = false;

        final CreateCommand command = new CreateCommand(diskCharacteristics, pool, executeInSequence);

        final KvmStoragePoolManager poolManager = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool primary = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk vol = Mockito.mock(KvmPhysicalDisk.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(poolManager);
        when(poolManager.getStoragePool(pool.getType(), pool.getUuid())).thenReturn(primary);

        when(primary.createPhysicalDisk(diskCharacteristics.getPath(), diskCharacteristics.getProvisioningType(),
                diskCharacteristics.getSize())).thenReturn(vol);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(poolManager, times(1)).getStoragePool(pool.getType(), pool.getUuid());
    }

    @Test
    public void testCreateCommand() {
        final DiskProfile diskCharacteristics = Mockito.mock(DiskProfile.class);
        final StorageFilerTO pool = Mockito.mock(StorageFilerTO.class);
        final String templateUrl = "http://template";
        final boolean executeInSequence = false;

        final CreateCommand command = new CreateCommand(diskCharacteristics, templateUrl, pool, executeInSequence);

        final KvmStoragePoolManager poolManager = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool primary = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk vol = Mockito.mock(KvmPhysicalDisk.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(poolManager);
        when(poolManager.getStoragePool(pool.getType(), pool.getUuid())).thenReturn(primary);

        when(primary.getType()).thenReturn(StoragePoolType.CLVM);
        when(this.libvirtComputingResource.templateToPrimaryDownload(command.getTemplateUrl(), primary,
                diskCharacteristics.getPath())).thenReturn(vol);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(poolManager, times(1)).getStoragePool(pool.getType(), pool.getUuid());
    }

    @Test
    public void testCreateCommandCLVM() {
        final DiskProfile diskCharacteristics = Mockito.mock(DiskProfile.class);
        final StorageFilerTO pool = Mockito.mock(StorageFilerTO.class);
        final String templateUrl = "http://template";
        final boolean executeInSequence = false;

        final CreateCommand command = new CreateCommand(diskCharacteristics, templateUrl, pool, executeInSequence);

        final KvmStoragePoolManager poolManager = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool primary = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk vol = Mockito.mock(KvmPhysicalDisk.class);
        final KvmPhysicalDisk baseVol = Mockito.mock(KvmPhysicalDisk.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(poolManager);
        when(poolManager.getStoragePool(pool.getType(), pool.getUuid())).thenReturn(primary);

        when(primary.getPhysicalDisk(command.getTemplateUrl())).thenReturn(baseVol);
        when(poolManager.createDiskFromTemplate(baseVol,
                diskCharacteristics.getPath(), diskCharacteristics.getProvisioningType(), primary, 0)).thenReturn(vol);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(poolManager, times(1)).getStoragePool(pool.getType(), pool.getUuid());
    }

    @Test
    public void testDestroyCommand() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final Volume volume = Mockito.mock(Volume.class);
        final String vmName = "Test";

        final DestroyCommand command = new DestroyCommand(pool, volume, vmName);

        final KvmStoragePoolManager poolManager = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool primary = Mockito.mock(KvmStoragePool.class);

        final VolumeTO vol = command.getVolume();

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(poolManager);
        when(poolManager.getStoragePool(vol.getPoolType(), vol.getPoolUuid())).thenReturn(primary);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(poolManager, times(1)).getStoragePool(vol.getPoolType(), vol.getPoolUuid());
    }

    @Test
    public void testDestroyCommandError() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final Volume volume = Mockito.mock(Volume.class);
        final String vmName = "Test";

        final DestroyCommand command = new DestroyCommand(pool, volume, vmName);

        final KvmStoragePoolManager poolManager = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool primary = Mockito.mock(KvmStoragePool.class);

        final VolumeTO vol = command.getVolume();

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(poolManager);
        when(poolManager.getStoragePool(vol.getPoolType(), vol.getPoolUuid())).thenReturn(primary);

        when(primary.deletePhysicalDisk(vol.getPath(), null)).thenThrow(CloudRuntimeException.class);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(poolManager, times(1)).getStoragePool(vol.getPoolType(), vol.getPoolUuid());
    }

    @Test(expected = NullPointerException.class)
    public void testPrimaryStorageDownloadCommandNOTemplateDisk() {
        final StoragePool pool = Mockito.mock(StoragePool.class);

        final List<KvmPhysicalDisk> disks = new ArrayList<>();

        final String name = "Test";
        final String url = "http://template/";
        final ImageFormat format = ImageFormat.QCOW2;
        final long accountId = 1l;
        final int wait = 0;
        final PrimaryStorageDownloadCommand command = new PrimaryStorageDownloadCommand(name, url, format, accountId, pool,
                wait);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool primaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk tmplVol = Mockito.mock(KvmPhysicalDisk.class);
        final KvmPhysicalDisk primaryVol = Mockito.mock(KvmPhysicalDisk.class);

        final KvmPhysicalDisk disk = new KvmPhysicalDisk("/path", "disk.qcow2", primaryPool);
        disks.add(disk);

        final int index = url.lastIndexOf("/");
        final String mountpoint = url.substring(0, index);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePoolByUri(mountpoint)).thenReturn(secondaryPool);
        when(secondaryPool.listPhysicalDisks()).thenReturn(disks);
        when(storagePoolMgr.getStoragePool(command.getPool().getType(), command.getPoolUuid())).thenReturn(primaryPool);
        when(storagePoolMgr.copyPhysicalDisk(tmplVol, UUID.randomUUID().toString(), primaryPool, 0)).thenReturn(primaryVol);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
    }

    @Test
    public void testPrimaryStorageDownloadCommandNOTemplateNODisk() {
        final StoragePool pool = Mockito.mock(StoragePool.class);

        final List<KvmPhysicalDisk> disks = new ArrayList<>();

        final String name = "Test";
        final String url = "http://template/";
        final ImageFormat format = ImageFormat.QCOW2;
        final long accountId = 1l;
        final int wait = 0;
        final PrimaryStorageDownloadCommand command = new PrimaryStorageDownloadCommand(name, url, format, accountId, pool,
                wait);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool primaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk tmplVol = Mockito.mock(KvmPhysicalDisk.class);
        final KvmPhysicalDisk primaryVol = Mockito.mock(KvmPhysicalDisk.class);

        final int index = url.lastIndexOf("/");
        final String mountpoint = url.substring(0, index);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePoolByUri(mountpoint)).thenReturn(secondaryPool);
        when(secondaryPool.listPhysicalDisks()).thenReturn(disks);
        when(storagePoolMgr.getStoragePool(command.getPool().getType(), command.getPoolUuid())).thenReturn(primaryPool);
        when(storagePoolMgr.copyPhysicalDisk(tmplVol, UUID.randomUUID().toString(), primaryPool, 0)).thenReturn(primaryVol);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
    }

    @Test
    public void testPrimaryStorageDownloadCommandNOTemplateNOQcow2() {
        final StoragePool pool = Mockito.mock(StoragePool.class);

        final List<KvmPhysicalDisk> disks = new ArrayList<>();
        final List<KvmPhysicalDisk> spiedDisks = Mockito.spy(disks);

        final String name = "Test";
        final String url = "http://template/";
        final ImageFormat format = ImageFormat.QCOW2;
        final long accountId = 1l;
        final int wait = 0;
        final PrimaryStorageDownloadCommand command = new PrimaryStorageDownloadCommand(name, url, format, accountId, pool,
                wait);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool primaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk tmplVol = Mockito.mock(KvmPhysicalDisk.class);
        final KvmPhysicalDisk primaryVol = Mockito.mock(KvmPhysicalDisk.class);

        final int index = url.lastIndexOf("/");
        final String mountpoint = url.substring(0, index);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePoolByUri(mountpoint)).thenReturn(secondaryPool);
        when(secondaryPool.listPhysicalDisks()).thenReturn(spiedDisks);
        when(spiedDisks.isEmpty()).thenReturn(false);

        when(storagePoolMgr.getStoragePool(command.getPool().getType(), command.getPoolUuid())).thenReturn(primaryPool);
        when(storagePoolMgr.copyPhysicalDisk(tmplVol, UUID.randomUUID().toString(), primaryPool, 0)).thenReturn(primaryVol);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
    }

    @Test(expected = NullPointerException.class)
    public void testPrimaryStorageDownloadCommandTemplateNoDisk() {
        final StoragePool pool = Mockito.mock(StoragePool.class);

        final String name = "Test";
        final String url = "http://template/template.qcow2";
        final ImageFormat format = ImageFormat.VHD;
        final long accountId = 1l;
        final int wait = 0;
        final PrimaryStorageDownloadCommand command = new PrimaryStorageDownloadCommand(name, url, format, accountId, pool,
                wait);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool primaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk tmplVol = Mockito.mock(KvmPhysicalDisk.class);
        final KvmPhysicalDisk primaryVol = Mockito.mock(KvmPhysicalDisk.class);

        final int index = url.lastIndexOf("/");
        final String mountpoint = url.substring(0, index);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePoolByUri(mountpoint)).thenReturn(secondaryPool);
        when(secondaryPool.getPhysicalDisk("template.qcow2")).thenReturn(tmplVol);
        when(storagePoolMgr.getStoragePool(command.getPool().getType(), command.getPoolUuid())).thenReturn(primaryPool);
        when(storagePoolMgr.copyPhysicalDisk(tmplVol, UUID.randomUUID().toString(), primaryPool, 0)).thenReturn(primaryVol);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).getStoragePool(command.getPool().getType(), command.getPoolUuid());
    }

    @Test
    public void testGetStorageStatsCommand() {
        final DataStoreTO store = Mockito.mock(DataStoreTO.class);
        final GetStorageStatsCommand command = new GetStorageStatsCommand(store);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePool(command.getPooltype(), command.getStorageId(), true)).thenReturn(secondaryPool);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).getStoragePool(command.getPooltype(), command.getStorageId(), true);
    }

    @Test
    public void testGetStorageStatsCommandException() {
        final DataStoreTO store = Mockito.mock(DataStoreTO.class);
        final GetStorageStatsCommand command = new GetStorageStatsCommand(store);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenThrow(CloudRuntimeException.class);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
    }

    @Test
    public void testUpgradeSnapshotCommand() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "url";
        final Long dcId = 1l;
        final Long accountId = 1l;
        final Long volumeId = 1l;
        final Long templateId = 1l;
        final Long tmpltAccountId = 1l;
        final String volumePath = "/opt/path";
        final String snapshotUuid = "uuid:/8edb1156-a851-4914-afc6-468ee52ac861/";
        final String snapshotName = "uuid:/8edb1156-a851-4914-afc6-468ee52ac861/";
        final String version = "1";

        final UpgradeSnapshotCommand command = new UpgradeSnapshotCommand(pool, secondaryStoragePoolURL, dcId, accountId,
                volumeId, templateId, tmpltAccountId, volumePath, snapshotUuid, snapshotName, version);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());
    }

    @Test
    public void testDeleteStoragePoolCommand() {
        final StoragePool storagePool = Mockito.mock(StoragePool.class);
        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);

        final DeleteStoragePoolCommand command = new DeleteStoragePoolCommand(storagePool);

        final StorageFilerTO pool = command.getPool();
        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.deleteStoragePool(pool.getType(), pool.getUuid())).thenReturn(true);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).deleteStoragePool(pool.getType(), pool.getUuid());
    }

    @Test
    public void testDeleteStoragePoolCommandException() {
        final StoragePool storagePool = Mockito.mock(StoragePool.class);
        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);

        final DeleteStoragePoolCommand command = new DeleteStoragePoolCommand(storagePool);

        final StorageFilerTO pool = command.getPool();
        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.deleteStoragePool(pool.getType(), pool.getUuid())).thenThrow(CloudRuntimeException.class);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).deleteStoragePool(pool.getType(), pool.getUuid());
    }

    @Test
    public void testCreateStoragePoolCommand() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final CreateStoragePoolCommand command = new CreateStoragePoolCommand(true, pool);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());
    }

    @Test
    public void testModifyStoragePoolCommand() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final ModifyStoragePoolCommand command = new ModifyStoragePoolCommand(true, pool);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool kvmStoragePool = Mockito.mock(KvmStoragePool.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.createStoragePool(command.getPool().getUuid(), command.getPool().getHost(),
                command.getPool().getPort(), command.getPool().getPath(), command.getPool().getUserInfo(),
                command.getPool().getType())).thenReturn(kvmStoragePool);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).createStoragePool(command.getPool().getUuid(), command.getPool().getHost(),
                command.getPool().getPort(), command.getPool().getPath(), command.getPool().getUserInfo(),
                command.getPool().getType());
    }

    @Test
    public void testModifyStoragePoolCommandFailure() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final ModifyStoragePoolCommand command = new ModifyStoragePoolCommand(true, pool);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.createStoragePool(command.getPool().getUuid(), command.getPool().getHost(),
                command.getPool().getPort(), command.getPool().getPath(), command.getPool().getUserInfo(),
                command.getPool().getType())).thenReturn(null);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).createStoragePool(command.getPool().getUuid(), command.getPool().getHost(),
                command.getPool().getPort(), command.getPool().getPath(), command.getPool().getUserInfo(),
                command.getPool().getType());
    }

    @Test
    public void testCheckSshCommand() {
        final String instanceName = "Test";
        final String ip = "127.0.0.1";
        final int port = 22;

        final CheckSshCommand command = new CheckSshCommand(instanceName, ip, port);

        final VirtualRoutingResource virtRouterResource = Mockito.mock(VirtualRoutingResource.class);

        final String privateIp = command.getIp();
        final int cmdPort = command.getPort();

        when(this.libvirtComputingResource.getVirtRouterResource()).thenReturn(virtRouterResource);
        when(virtRouterResource.connect(privateIp, cmdPort)).thenReturn(true);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getVirtRouterResource();
        verify(virtRouterResource, times(1)).connect(privateIp, cmdPort);
    }

    @Test
    public void testCheckSshCommandFailure() {
        final String instanceName = "Test";
        final String ip = "127.0.0.1";
        final int port = 22;

        final CheckSshCommand command = new CheckSshCommand(instanceName, ip, port);

        final VirtualRoutingResource virtRouterResource = Mockito.mock(VirtualRoutingResource.class);

        final String privateIp = command.getIp();
        final int cmdPort = command.getPort();

        when(this.libvirtComputingResource.getVirtRouterResource()).thenReturn(virtRouterResource);
        when(virtRouterResource.connect(privateIp, cmdPort)).thenReturn(false);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getVirtRouterResource();
        verify(virtRouterResource, times(1)).connect(privateIp, cmdPort);
    }

    @Test
    public void testCheckNetworkCommand() {
        final List<PhysicalNetworkSetupInfo> networkInfoList = new ArrayList<>();

        final PhysicalNetworkSetupInfo nic = Mockito.mock(PhysicalNetworkSetupInfo.class);
        networkInfoList.add(nic);

        final CheckNetworkCommand command = new CheckNetworkCommand(networkInfoList);

        when(this.libvirtComputingResource.checkNetwork(nic.getGuestNetworkName())).thenReturn(true);
        when(this.libvirtComputingResource.checkNetwork(nic.getPrivateNetworkName())).thenReturn(true);
        when(this.libvirtComputingResource.checkNetwork(nic.getPublicNetworkName())).thenReturn(true);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(3)).checkNetwork(nic.getGuestNetworkName());
        verify(this.libvirtComputingResource, times(3)).checkNetwork(nic.getPrivateNetworkName());
        verify(this.libvirtComputingResource, times(3)).checkNetwork(nic.getPublicNetworkName());
    }

    @Test
    public void testCheckNetworkCommandFail1() {
        final List<PhysicalNetworkSetupInfo> networkInfoList = new ArrayList<>();

        final PhysicalNetworkSetupInfo networkSetupInfo = Mockito.mock(PhysicalNetworkSetupInfo.class);
        networkInfoList.add(networkSetupInfo);

        final CheckNetworkCommand command = new CheckNetworkCommand(networkInfoList);

        when(this.libvirtComputingResource.checkNetwork(networkSetupInfo.getGuestNetworkName())).thenReturn(false);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).checkNetwork(networkSetupInfo.getGuestNetworkName());
    }

    @Test
    public void testCheckNetworkCommandFail2() {
        final List<PhysicalNetworkSetupInfo> networkInfoList = new ArrayList<>();

        final PhysicalNetworkSetupInfo networkSetupInfo = Mockito.mock(PhysicalNetworkSetupInfo.class);
        networkInfoList.add(networkSetupInfo);

        final CheckNetworkCommand command = new CheckNetworkCommand(networkInfoList);

        when(this.libvirtComputingResource.checkNetwork(networkSetupInfo.getGuestNetworkName())).thenReturn(true);
        when(this.libvirtComputingResource.checkNetwork(networkSetupInfo.getPrivateNetworkName())).thenReturn(false);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).checkNetwork(networkSetupInfo.getGuestNetworkName());
        verify(this.libvirtComputingResource, times(1)).checkNetwork(networkSetupInfo.getPrivateNetworkName());
    }

    @Test
    public void testCheckNetworkCommandFail3() {
        final List<PhysicalNetworkSetupInfo> networkInfoList = new ArrayList<>();

        final PhysicalNetworkSetupInfo networkSetupInfo = Mockito.mock(PhysicalNetworkSetupInfo.class);
        networkInfoList.add(networkSetupInfo);

        final CheckNetworkCommand command = new CheckNetworkCommand(networkInfoList);

        when(this.libvirtComputingResource.checkNetwork(networkSetupInfo.getGuestNetworkName())).thenReturn(true);
        when(this.libvirtComputingResource.checkNetwork(networkSetupInfo.getPrivateNetworkName())).thenReturn(true);
        when(this.libvirtComputingResource.checkNetwork(networkSetupInfo.getPublicNetworkName())).thenReturn(false);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).checkNetwork(networkSetupInfo.getGuestNetworkName());
        verify(this.libvirtComputingResource, times(1)).checkNetwork(networkSetupInfo.getPrivateNetworkName());
    }

    @Test
    public void testCheckOnHostCommand() {
        final Host host = Mockito.mock(Host.class);

        final CheckOnHostCommand command = new CheckOnHostCommand(host);

        final KvmHaMonitor monitor = Mockito.mock(KvmHaMonitor.class);

        when(this.libvirtComputingResource.getMonitor()).thenReturn(monitor);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getMonitor();
    }

    @Test
    public void testCreateVolumeFromSnapshotCommand() {
        // This tests asserts to False because there will be a NPE due to UUID static method calls.

        final StoragePool pool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "/opt/storage/";
        final Long dcId = 1l;
        final Long accountId = 1l;
        final Long volumeId = 1l;
        final String backedUpSnapshotUuid = "uuid:/8edb1156-a851-4914-afc6-468ee52ac861/";
        final String backedUpSnapshotName = "uuid:/8edb1156-a851-4914-afc6-468ee52ac862/";
        final int wait = 0;

        final CreateVolumeFromSnapshotCommand command = new CreateVolumeFromSnapshotCommand(pool, secondaryStoragePoolURL,
                dcId, accountId, volumeId, backedUpSnapshotUuid, backedUpSnapshotName, wait);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk snapshot = Mockito.mock(KvmPhysicalDisk.class);
        final KvmStoragePool primaryPool = Mockito.mock(KvmStoragePool.class);

        String snapshotPath = command.getSnapshotUuid();
        final int index = snapshotPath.lastIndexOf("/");
        snapshotPath = snapshotPath.substring(0, index);

        final String primaryUuid = command.getPrimaryStoragePoolNameLabel();

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath)).thenReturn(secondaryPool);
        when(secondaryPool.getPhysicalDisk(command.getSnapshotName())).thenReturn(snapshot);
        when(storagePoolMgr.getStoragePool(command.getPool().getType(), primaryUuid)).thenReturn(primaryPool);

        // when(storagePoolMgr.copyPhysicalDisk(snapshot, volUuid, primaryPool, 0)).thenReturn(disk);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath);
        verify(secondaryPool, times(1)).getPhysicalDisk(command.getSnapshotName());
        verify(storagePoolMgr, times(1)).getStoragePool(command.getPool().getType(), primaryUuid);
        // verify(storagePoolMgr, times(1)).copyPhysicalDisk(snapshot, volUuid, primaryPool, 0);
    }

    @Test
    public void testCreateVolumeFromSnapshotCommandCloudException() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "/opt/storage/";
        final Long dcId = 1l;
        final Long accountId = 1l;
        final Long volumeId = 1l;
        final String backedUpSnapshotUuid = "uuid:/8edb1156-a851-4914-afc6-468ee52ac861/";
        final String backedUpSnapshotName = "uuid:/8edb1156-a851-4914-afc6-468ee52ac862/";
        final int wait = 0;

        final CreateVolumeFromSnapshotCommand command = new CreateVolumeFromSnapshotCommand(pool, secondaryStoragePoolURL,
                dcId, accountId, volumeId, backedUpSnapshotUuid, backedUpSnapshotName, wait);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk snapshot = Mockito.mock(KvmPhysicalDisk.class);

        String snapshotPath = command.getSnapshotUuid();
        final int index = snapshotPath.lastIndexOf("/");
        snapshotPath = snapshotPath.substring(0, index);

        final String primaryUuid = command.getPrimaryStoragePoolNameLabel();

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath)).thenReturn(secondaryPool);
        when(secondaryPool.getPhysicalDisk(command.getSnapshotName())).thenReturn(snapshot);
        when(storagePoolMgr.getStoragePool(command.getPool().getType(), primaryUuid)).thenThrow(
                CloudRuntimeException.class);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath);
        verify(secondaryPool, times(1)).getPhysicalDisk(command.getSnapshotName());
        verify(storagePoolMgr, times(1)).getStoragePool(command.getPool().getType(), primaryUuid);
    }

    @Test
    public void testFenceCommand() {
        final VirtualMachine vm = Mockito.mock(VirtualMachine.class);
        final Host host = Mockito.mock(Host.class);

        final FenceCommand command = new FenceCommand(vm, host);

        final KvmHaMonitor monitor = Mockito.mock(KvmHaMonitor.class);

        final NfsStoragePool storagePool = Mockito.mock(NfsStoragePool.class);
        final List<NfsStoragePool> pools = new ArrayList<>();
        pools.add(storagePool);

        when(this.libvirtComputingResource.getMonitor()).thenReturn(monitor);
        when(monitor.getStoragePools()).thenReturn(pools);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getMonitor();
        verify(monitor, times(1)).getStoragePools();
    }

    @Test
    public void testPlugNicCommandMatchMack() {
        final NicTO nic = Mockito.mock(NicTO.class);
        final String instanceName = "Test";
        final VirtualMachineType vmtype = VirtualMachineType.DomainRouter;

        final PlugNicCommand command = new PlugNicCommand(nic, instanceName, vmtype);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final Domain vm = Mockito.mock(Domain.class);

        final List<InterfaceDef> nics = new ArrayList<>();
        final InterfaceDef intDef = Mockito.mock(InterfaceDef.class);
        nics.add(intDef);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(this.libvirtComputingResource.getInterfaces(conn, command.getVmName())).thenReturn(nics);

        when(intDef.getDevName()).thenReturn("eth0");
        when(intDef.getBrName()).thenReturn("br0");
        when(intDef.getMacAddress()).thenReturn("00:00:00:00");

        when(nic.getMac()).thenReturn("00:00:00:00");

        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName())).thenReturn(conn);
            when(this.libvirtComputingResource.getDomain(conn, instanceName)).thenReturn(vm);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(command.getVmName());
            verify(this.libvirtComputingResource, times(1)).getDomain(conn, instanceName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPlugNicCommandNoMatchMack() {
        final NicTO nic = Mockito.mock(NicTO.class);
        final String instanceName = "Test";
        final VirtualMachineType vmtype = VirtualMachineType.DomainRouter;

        final PlugNicCommand command = new PlugNicCommand(nic, instanceName, vmtype);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final Domain vm = Mockito.mock(Domain.class);
        final VifDriver vifDriver = Mockito.mock(VifDriver.class);
        final InterfaceDef interfaceDef = Mockito.mock(InterfaceDef.class);

        final List<InterfaceDef> nics = new ArrayList<>();
        final InterfaceDef intDef = Mockito.mock(InterfaceDef.class);
        nics.add(intDef);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(this.libvirtComputingResource.getInterfaces(conn, command.getVmName())).thenReturn(nics);

        when(intDef.getDevName()).thenReturn("eth0");
        when(intDef.getBrName()).thenReturn("br0");
        when(intDef.getMacAddress()).thenReturn("00:00:00:00");

        when(nic.getMac()).thenReturn("00:00:00:01");

        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName())).thenReturn(conn);
            when(this.libvirtComputingResource.getDomain(conn, instanceName)).thenReturn(vm);

            when(this.libvirtComputingResource.getVifDriver(nic.getType())).thenReturn(vifDriver);

            when(vifDriver.plug(nic, "Default - VirtIO capable OS (64-bit)", "")).thenReturn(interfaceDef);
            when(interfaceDef.toString()).thenReturn("Interface");

            final String interfaceDefStr = interfaceDef.toString();
            doNothing().when(vm).attachDevice(interfaceDefStr);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(command.getVmName());
            verify(this.libvirtComputingResource, times(1)).getDomain(conn, instanceName);
            verify(this.libvirtComputingResource, times(1)).getVifDriver(nic.getType());
            verify(vifDriver, times(1)).plug(nic, "Default - VirtIO capable OS (64-bit)", "");
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPlugNicCommandLibvirtException() {
        final NicTO nic = Mockito.mock(NicTO.class);
        final String instanceName = "Test";
        final VirtualMachineType vmtype = VirtualMachineType.DomainRouter;

        final PlugNicCommand command = new PlugNicCommand(nic, instanceName, vmtype);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);

        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName())).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(command.getVmName());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPlugNicCommandInternalError() {
        final NicTO nic = Mockito.mock(NicTO.class);
        final String instanceName = "Test";
        final VirtualMachineType vmtype = VirtualMachineType.DomainRouter;

        final PlugNicCommand command = new PlugNicCommand(nic, instanceName, vmtype);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final Domain vm = Mockito.mock(Domain.class);
        final VifDriver vifDriver = Mockito.mock(VifDriver.class);

        final List<InterfaceDef> nics = new ArrayList<>();
        final InterfaceDef intDef = Mockito.mock(InterfaceDef.class);
        nics.add(intDef);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(this.libvirtComputingResource.getInterfaces(conn, command.getVmName())).thenReturn(nics);

        when(intDef.getDevName()).thenReturn("eth0");
        when(intDef.getBrName()).thenReturn("br0");
        when(intDef.getMacAddress()).thenReturn("00:00:00:00");

        when(nic.getMac()).thenReturn("00:00:00:01");

        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName())).thenReturn(conn);
            when(this.libvirtComputingResource.getDomain(conn, instanceName)).thenReturn(vm);

            when(this.libvirtComputingResource.getVifDriver(nic.getType())).thenReturn(vifDriver);

            when(vifDriver.plug(nic, "Default - VirtIO capable OS (64-bit)", "")).thenThrow(InternalErrorException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(command.getVmName());
            verify(this.libvirtComputingResource, times(1)).getDomain(conn, instanceName);
            verify(this.libvirtComputingResource, times(1)).getVifDriver(nic.getType());
            verify(vifDriver, times(1)).plug(nic, "Default - VirtIO capable OS (64-bit)", "");
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUnPlugNicCommandMatchMack() {
        final NicTO nic = Mockito.mock(NicTO.class);
        final String instanceName = "Test";

        final UnPlugNicCommand command = new UnPlugNicCommand(nic, instanceName);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final Domain vm = Mockito.mock(Domain.class);
        final InterfaceDef interfaceDef = Mockito.mock(InterfaceDef.class);

        final List<InterfaceDef> nics = new ArrayList<>();
        final InterfaceDef intDef = Mockito.mock(InterfaceDef.class);
        nics.add(intDef);

        final VifDriver vifDriver = Mockito.mock(VifDriver.class);
        final List<VifDriver> drivers = new ArrayList<>();
        drivers.add(vifDriver);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(this.libvirtComputingResource.getInterfaces(conn, command.getVmName())).thenReturn(nics);

        when(intDef.getDevName()).thenReturn("eth0");
        when(intDef.getBrName()).thenReturn("br0");
        when(intDef.getMacAddress()).thenReturn("00:00:00:00");

        when(nic.getMac()).thenReturn("00:00:00:00");

        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName())).thenReturn(conn);
            when(this.libvirtComputingResource.getDomain(conn, instanceName)).thenReturn(vm);

            when(interfaceDef.toString()).thenReturn("Interface");

            final String interfaceDefStr = interfaceDef.toString();
            doNothing().when(vm).detachDevice(interfaceDefStr);

            when(this.libvirtComputingResource.getAllVifDrivers()).thenReturn(drivers);

            doNothing().when(vifDriver).unplug(intDef);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(command.getVmName());
            verify(this.libvirtComputingResource, times(1)).getDomain(conn, instanceName);
            verify(this.libvirtComputingResource, times(1)).getAllVifDrivers();
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUnPlugNicCommandNoNics() {
        final NicTO nic = Mockito.mock(NicTO.class);
        final String instanceName = "Test";

        final UnPlugNicCommand command = new UnPlugNicCommand(nic, instanceName);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final Domain vm = Mockito.mock(Domain.class);

        final List<InterfaceDef> nics = new ArrayList<>();

        final VifDriver vifDriver = Mockito.mock(VifDriver.class);
        final List<VifDriver> drivers = new ArrayList<>();
        drivers.add(vifDriver);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(this.libvirtComputingResource.getInterfaces(conn, command.getVmName())).thenReturn(nics);

        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName())).thenReturn(conn);
            when(this.libvirtComputingResource.getDomain(conn, instanceName)).thenReturn(vm);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(command.getVmName());
            verify(this.libvirtComputingResource, times(1)).getDomain(conn, instanceName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUnPlugNicCommandLibvirtException() {
        final NicTO nic = Mockito.mock(NicTO.class);
        final String instanceName = "Test";

        final UnPlugNicCommand command = new UnPlugNicCommand(nic, instanceName);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);

        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName())).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(command.getVmName());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testNetworkUsageCommandNonVpc() {
        final String privateIP = "127.0.0.1";
        final String domRName = "domR";
        final boolean forVpc = false;
        final String gatewayIP = "127.0.0.1";

        final NetworkUsageCommand command = new NetworkUsageCommand(privateIP, domRName, forVpc, gatewayIP);

        this.libvirtComputingResource.getNetworkStats(command.getPrivateIP());

        when(this.libvirtComputingResource.getNetworkStats(command.getPrivateIP())).thenReturn(new long[]{10l, 10l});

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        // Being called twice, although I did not find the second place yet.
        verify(this.libvirtComputingResource, times(2)).getNetworkStats(command.getPrivateIP());
    }

    @Test
    public void testNetworkUsageCommandNonVpcCreate() {
        final String privateIP = "127.0.0.1";
        final String domRName = "domR";
        final boolean forVpc = false;

        final NetworkUsageCommand command = new NetworkUsageCommand(privateIP, domRName, "create", forVpc);

        this.libvirtComputingResource.getNetworkStats(command.getPrivateIP());

        when(this.libvirtComputingResource.networkUsage(command.getPrivateIP(), "create", null)).thenReturn("SUCCESS");

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).networkUsage(command.getPrivateIP(), "create", null);
    }

    @Test
    public void testNetworkUsageCommandVpcCreate() {
        final String privateIP = "127.0.0.1";
        final String domRName = "domR";
        final boolean forVpc = true;
        final String gatewayIP = "127.0.0.1";
        final String vpcCidr = "10.1.1.0/24";

        final NetworkUsageCommand command = new NetworkUsageCommand(privateIP, domRName, forVpc, gatewayIP, vpcCidr);

        this.libvirtComputingResource.getNetworkStats(command.getPrivateIP());

        when(this.libvirtComputingResource.configureVpcNetworkUsage(command.getPrivateIP(), command.getGatewayIP(), "create",
                command.getVpcCIDR())).thenReturn("SUCCESS");

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).configureVpcNetworkUsage(command.getPrivateIP(), command.getGatewayIP(),
                "create", command.getVpcCIDR());
    }

    @Test
    public void testNetworkUsageCommandVpcGet() {
        final String privateIP = "127.0.0.1";
        final String domRName = "domR";
        final boolean forVpc = true;
        final String gatewayIP = "127.0.0.1";

        final NetworkUsageCommand command = new NetworkUsageCommand(privateIP, domRName, forVpc, gatewayIP);

        this.libvirtComputingResource.getNetworkStats(command.getPrivateIP());

        when(this.libvirtComputingResource.getVpcNetworkStats(command.getPrivateIP(), command.getGatewayIP(),
                command.getOption())).thenReturn(new long[]{10l, 10l});

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getVpcNetworkStats(command.getPrivateIP(), command.getGatewayIP(),
                command.getOption());
    }

    @Test
    public void testNetworkUsageCommandVpcVpn() {
        final String privateIP = "127.0.0.1";
        final String domRName = "domR";
        final boolean forVpc = true;
        final String gatewayIP = "127.0.0.1";

        final NetworkUsageCommand command = new NetworkUsageCommand(privateIP, domRName, "vpn", forVpc, gatewayIP);

        this.libvirtComputingResource.getNetworkStats(command.getPrivateIP());

        when(this.libvirtComputingResource.getVpcNetworkStats(command.getPrivateIP(), command.getGatewayIP(),
                command.getOption())).thenReturn(new long[]{10l, 10l});

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getVpcNetworkStats(command.getPrivateIP(), command.getGatewayIP(),
                command.getOption());
    }

    @Test
    public void testNetworkUsageCommandVpcNoOption() {
        final String privateIP = "127.0.0.1";
        final String domRName = "domR";
        final boolean forVpc = true;
        final String gatewayIP = "127.0.0.1";

        final NetworkUsageCommand command = new NetworkUsageCommand(privateIP, domRName, null, forVpc, gatewayIP);

        this.libvirtComputingResource.getNetworkStats(command.getPrivateIP());

        when(this.libvirtComputingResource.configureVpcNetworkUsage(command.getPrivateIP(), command.getGatewayIP(),
                command.getOption(), command.getVpcCIDR())).thenReturn("FAILURE");

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).configureVpcNetworkUsage(command.getPrivateIP(), command.getGatewayIP(),
                command.getOption(), command.getVpcCIDR());
    }

    @Test
    public void testCreatePrivateTemplateFromVolumeCommand() {
        // Simple test used to make sure the flow (LibvirtComputingResource => Request => CommandWrapper) is working.
        // The code is way to big and complex. Will finish the refactor and come back to this to add more cases.

        final StoragePool pool = Mockito.mock(StoragePool.class);
        final String secondaryStorageUrl = "nfs:/127.0.0.1/storage/secondary";
        final long templateId = 1l;
        final long accountId = 1l;
        final String userSpecifiedName = "User";
        final String uniqueName = "Unique";
        final String volumePath = "/123/vol";
        final String vmName = "Test";
        final int wait = 0;

        final CreatePrivateTemplateFromVolumeCommand command = new CreatePrivateTemplateFromVolumeCommand(pool,
                secondaryStorageUrl, templateId, accountId, userSpecifiedName, uniqueName, volumePath, vmName, wait);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondaryStorage = Mockito.mock(KvmStoragePool.class);
        // final KVMStoragePool primary = Mockito.mock(KVMStoragePool.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePoolByUri(secondaryStorageUrl)).thenReturn(secondaryStorage);
        when(
                storagePoolMgr.getStoragePool(command.getPool().getType(), command.getPrimaryStoragePoolNameLabel())).thenThrow(
                new CloudRuntimeException("error"));

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(secondaryStorageUrl);
        verify(storagePoolMgr, times(1)).getStoragePool(command.getPool().getType(),
                command.getPrimaryStoragePoolNameLabel());
    }

    @Test
    public void testManageSnapshotCommandLibvirtException() {
        // Simple test used to make sure the flow (LibvirtComputingResource => Request => CommandWrapper) is working.
        // The code is way to big and complex. Will finish the refactor and come back to this to add more cases.

        final StoragePool pool = Mockito.mock(StoragePool.class);
        final String volumePath = "/123/vol";
        final String vmName = "Test";

        final long snapshotId = 1l;
        final String preSnapshotPath = "/snapshot/path";
        final String snapshotName = "snap";

        final ManageSnapshotCommand command = new ManageSnapshotCommand(snapshotId, volumePath, pool, preSnapshotPath,
                snapshotName, vmName);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        // final Connect conn = Mockito.mock(Connect.class);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);

        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName())).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(command.getVmName());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testManageSnapshotCommandLibvirt() {
        final StoragePool storagePool = Mockito.mock(StoragePool.class);
        final String volumePath = "/123/vol";
        final String vmName = "Test";
        final long snapshotId = 1l;
        final String preSnapshotPath = "/snapshot/path";
        final String snapshotName = "snap";

        final ManageSnapshotCommand command = new ManageSnapshotCommand(snapshotId, volumePath, storagePool,
                preSnapshotPath, snapshotName, vmName);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool primaryPool = Mockito.mock(KvmStoragePool.class);
        final Domain vm = Mockito.mock(Domain.class);
        final DomainInfo info = Mockito.mock(DomainInfo.class);
        final DomainState state = DomainInfo.DomainState.VIR_DOMAIN_RUNNING;
        info.state = state;

        final KvmPhysicalDisk disk = Mockito.mock(KvmPhysicalDisk.class);

        final StorageFilerTO pool = command.getPool();

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(vmName)).thenReturn(conn);
            when(this.libvirtComputingResource.getDomain(conn, command.getVmName())).thenReturn(vm);
            when(vm.getInfo()).thenReturn(info);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid())).thenReturn(primaryPool);
        when(primaryPool.getPhysicalDisk(command.getVolumePath())).thenReturn(disk);
        when(primaryPool.isExternalSnapshot()).thenReturn(false);

        try {
            when(vm.getUUIDString()).thenReturn("cdb18980-546d-4153-b916-70ee9edf0908");
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(vmName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testBackupSnapshotCommandLibvirtException() {
        // Simple test used to make sure the flow (LibvirtComputingResource => Request => CommandWrapper) is working.
        // The code is way to big and complex. Will finish the refactor and come back to this to add more cases.

        final StoragePool pool = Mockito.mock(StoragePool.class);
        final String secondaryStorageUrl = "nfs:/127.0.0.1/storage/secondary";
        final long accountId = 1l;
        final String volumePath = "/123/vol";
        final String vmName = "Test";
        final int wait = 0;

        final long snapshotId = 1l;
        final String snapshotName = "snap";

        final Long dcId = 1l;
        final Long volumeId = 1l;
        final Long secHostId = 1l;
        final String snapshotUuid = "9a0afe7c-26a7-4585-bf87-abf82ae106d9";
        final String prevBackupUuid = "003a0cc2-2e04-417a-bee0-534ef1724561";
        final boolean isVolumeInactive = false;
        final String prevSnapshotUuid = "1791efae-f22d-474b-87c6-92547d6c5877";

        final BackupSnapshotCommand command = new BackupSnapshotCommand(secondaryStorageUrl, dcId, accountId, volumeId,
                snapshotId, secHostId, volumePath, pool, snapshotUuid, snapshotName, prevSnapshotUuid, prevBackupUuid,
                isVolumeInactive, vmName, wait);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        // final Connect conn = Mockito.mock(Connect.class);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);

        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(command.getVmName())).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(command.getVmName());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreatePrivateTemplateFromSnapshotCommand() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "nfs:/127.0.0.1/storage/secondary";
        final Long dcId = 1l;
        final Long accountId = 1l;
        final Long volumeId = 1l;
        final String backedUpSnapshotUuid = "/run/9a0afe7c-26a7-4585-bf87-abf82ae106d9/";
        final String backedUpSnapshotName = "snap";
        final String origTemplateInstallPath = "/install/path/";
        final Long newTemplateId = 2l;
        final String templateName = "templ";
        final int wait = 0;

        final CreatePrivateTemplateFromSnapshotCommand command = new CreatePrivateTemplateFromSnapshotCommand(pool,
                secondaryStoragePoolURL, dcId, accountId, volumeId, backedUpSnapshotUuid, backedUpSnapshotName,
                origTemplateInstallPath, newTemplateId, templateName, wait);

        final String templatePath = "/template/path";
        final String localPath = "/mnt/local";
        final String tmplName = "ce97bbc1-34fe-4259-9202-74bbce2562ab";

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool snapshotPool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk snapshot = Mockito.mock(KvmPhysicalDisk.class);
        final StorageLayer storage = Mockito.mock(StorageLayer.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final TemplateLocation location = Mockito.mock(TemplateLocation.class);
        final Processor qcow2Processor = Mockito.mock(Processor.class);
        final TemplateFormatInfo info = Mockito.mock(TemplateFormatInfo.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);

        String snapshotPath = command.getSnapshotUuid();
        final int index = snapshotPath.lastIndexOf("/");
        snapshotPath = snapshotPath.substring(0, index);

        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath)).thenReturn(snapshotPool);
        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl())).thenReturn(secondaryPool);
        when(snapshotPool.getPhysicalDisk(command.getSnapshotName())).thenReturn(snapshot);
        when(secondaryPool.getLocalPath()).thenReturn(localPath);
        when(this.libvirtComputingResource.getStorage()).thenReturn(storage);

        when(this.libvirtComputingResource.createTmplPath()).thenReturn(templatePath);
        when(this.libvirtComputingResource.getCmdsTimeout()).thenReturn(1);

        final String templateFolder = command.getAccountId() + File.separator + command.getNewTemplateId();
        final String templateInstallFolder = "template/tmpl/" + templateFolder;
        final String tmplPath = secondaryPool.getLocalPath() + File.separator + templateInstallFolder;

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(libvirtUtilitiesHelper.buildTemplateLocation(storage, tmplPath)).thenReturn(location);
        when(libvirtUtilitiesHelper.generateUuidName()).thenReturn(tmplName);

        try {
            when(libvirtUtilitiesHelper.buildQcow2Processor(storage)).thenReturn(qcow2Processor);
            when(qcow2Processor.process(tmplPath, null, tmplName)).thenReturn(info);
        } catch (final ConfigurationException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath);
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl());
    }

    @Test
    public void testCreatePrivateTemplateFromSnapshotCommandConfigurationException() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "nfs:/127.0.0.1/storage/secondary";
        final Long dcId = 1l;
        final Long accountId = 1l;
        final Long volumeId = 1l;
        final String backedUpSnapshotUuid = "/run/9a0afe7c-26a7-4585-bf87-abf82ae106d9/";
        final String backedUpSnapshotName = "snap";
        final String origTemplateInstallPath = "/install/path/";
        final Long newTemplateId = 2l;
        final String templateName = "templ";
        final int wait = 0;

        final CreatePrivateTemplateFromSnapshotCommand command = new CreatePrivateTemplateFromSnapshotCommand(pool,
                secondaryStoragePoolURL, dcId, accountId, volumeId, backedUpSnapshotUuid, backedUpSnapshotName,
                origTemplateInstallPath, newTemplateId, templateName, wait);

        final String templatePath = "/template/path";
        final String localPath = "/mnt/local";
        final String tmplName = "ce97bbc1-34fe-4259-9202-74bbce2562ab";

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool snapshotPool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk snapshot = Mockito.mock(KvmPhysicalDisk.class);
        final StorageLayer storage = Mockito.mock(StorageLayer.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final TemplateLocation location = Mockito.mock(TemplateLocation.class);
        final Processor qcow2Processor = Mockito.mock(Processor.class);
        final TemplateFormatInfo info = Mockito.mock(TemplateFormatInfo.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);

        String snapshotPath = command.getSnapshotUuid();
        final int index = snapshotPath.lastIndexOf("/");
        snapshotPath = snapshotPath.substring(0, index);

        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath)).thenReturn(snapshotPool);
        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl())).thenReturn(secondaryPool);
        when(snapshotPool.getPhysicalDisk(command.getSnapshotName())).thenReturn(snapshot);
        when(secondaryPool.getLocalPath()).thenReturn(localPath);
        when(this.libvirtComputingResource.getStorage()).thenReturn(storage);

        when(this.libvirtComputingResource.createTmplPath()).thenReturn(templatePath);
        when(this.libvirtComputingResource.getCmdsTimeout()).thenReturn(1);

        final String templateFolder = command.getAccountId() + File.separator + command.getNewTemplateId();
        final String templateInstallFolder = "template/tmpl/" + templateFolder;
        final String tmplPath = secondaryPool.getLocalPath() + File.separator + templateInstallFolder;

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(libvirtUtilitiesHelper.buildTemplateLocation(storage, tmplPath)).thenReturn(location);
        when(libvirtUtilitiesHelper.generateUuidName()).thenReturn(tmplName);

        try {
            when(libvirtUtilitiesHelper.buildQcow2Processor(storage)).thenThrow(ConfigurationException.class);
            when(qcow2Processor.process(tmplPath, null, tmplName)).thenReturn(info);
        } catch (final ConfigurationException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath);
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl());
    }

    @Test
    public void testCreatePrivateTemplateFromSnapshotCommandInternalErrorException() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "nfs:/127.0.0.1/storage/secondary";
        final Long dcId = 1l;
        final Long accountId = 1l;
        final Long volumeId = 1l;
        final String backedUpSnapshotUuid = "/run/9a0afe7c-26a7-4585-bf87-abf82ae106d9/";
        final String backedUpSnapshotName = "snap";
        final String origTemplateInstallPath = "/install/path/";
        final Long newTemplateId = 2l;
        final String templateName = "templ";
        final int wait = 0;

        final CreatePrivateTemplateFromSnapshotCommand command = new CreatePrivateTemplateFromSnapshotCommand(pool,
                secondaryStoragePoolURL, dcId, accountId, volumeId, backedUpSnapshotUuid, backedUpSnapshotName,
                origTemplateInstallPath, newTemplateId, templateName, wait);

        final String templatePath = "/template/path";
        final String localPath = "/mnt/local";
        final String tmplName = "ce97bbc1-34fe-4259-9202-74bbce2562ab";

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool snapshotPool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk snapshot = Mockito.mock(KvmPhysicalDisk.class);
        final StorageLayer storage = Mockito.mock(StorageLayer.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final TemplateLocation location = Mockito.mock(TemplateLocation.class);
        final Processor qcow2Processor = Mockito.mock(Processor.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);

        String snapshotPath = command.getSnapshotUuid();
        final int index = snapshotPath.lastIndexOf("/");
        snapshotPath = snapshotPath.substring(0, index);

        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath)).thenReturn(snapshotPool);
        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl())).thenReturn(secondaryPool);
        when(snapshotPool.getPhysicalDisk(command.getSnapshotName())).thenReturn(snapshot);
        when(secondaryPool.getLocalPath()).thenReturn(localPath);
        when(this.libvirtComputingResource.getStorage()).thenReturn(storage);

        when(this.libvirtComputingResource.createTmplPath()).thenReturn(templatePath);
        when(this.libvirtComputingResource.getCmdsTimeout()).thenReturn(1);

        final String templateFolder = command.getAccountId() + File.separator + command.getNewTemplateId();
        final String templateInstallFolder = "template/tmpl/" + templateFolder;
        final String tmplPath = secondaryPool.getLocalPath() + File.separator + templateInstallFolder;

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(libvirtUtilitiesHelper.buildTemplateLocation(storage, tmplPath)).thenReturn(location);
        when(libvirtUtilitiesHelper.generateUuidName()).thenReturn(tmplName);

        try {
            when(libvirtUtilitiesHelper.buildQcow2Processor(storage)).thenReturn(qcow2Processor);
            when(qcow2Processor.process(tmplPath, null, tmplName)).thenThrow(InternalErrorException.class);
        } catch (final ConfigurationException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath);
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl());
    }

    @Test
    public void testCreatePrivateTemplateFromSnapshotCommandIOException() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "nfs:/127.0.0.1/storage/secondary";
        final Long dcId = 1l;
        final Long accountId = 1l;
        final Long volumeId = 1l;
        final String backedUpSnapshotUuid = "/run/9a0afe7c-26a7-4585-bf87-abf82ae106d9/";
        final String backedUpSnapshotName = "snap";
        final String origTemplateInstallPath = "/install/path/";
        final Long newTemplateId = 2l;
        final String templateName = "templ";
        final int wait = 0;

        final CreatePrivateTemplateFromSnapshotCommand command = new CreatePrivateTemplateFromSnapshotCommand(pool,
                secondaryStoragePoolURL, dcId, accountId, volumeId, backedUpSnapshotUuid, backedUpSnapshotName,
                origTemplateInstallPath, newTemplateId, templateName, wait);

        final String templatePath = "/template/path";
        final String localPath = "/mnt/local";
        final String tmplName = "ce97bbc1-34fe-4259-9202-74bbce2562ab";

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool snapshotPool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk snapshot = Mockito.mock(KvmPhysicalDisk.class);
        final StorageLayer storage = Mockito.mock(StorageLayer.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final TemplateLocation location = Mockito.mock(TemplateLocation.class);
        final Processor qcow2Processor = Mockito.mock(Processor.class);
        final TemplateFormatInfo info = Mockito.mock(TemplateFormatInfo.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);

        String snapshotPath = command.getSnapshotUuid();
        final int index = snapshotPath.lastIndexOf("/");
        snapshotPath = snapshotPath.substring(0, index);

        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath)).thenReturn(snapshotPool);
        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl())).thenReturn(secondaryPool);
        when(snapshotPool.getPhysicalDisk(command.getSnapshotName())).thenReturn(snapshot);
        when(secondaryPool.getLocalPath()).thenReturn(localPath);
        when(this.libvirtComputingResource.getStorage()).thenReturn(storage);

        when(this.libvirtComputingResource.createTmplPath()).thenReturn(templatePath);
        when(this.libvirtComputingResource.getCmdsTimeout()).thenReturn(1);

        final String templateFolder = command.getAccountId() + File.separator + command.getNewTemplateId();
        final String templateInstallFolder = "template/tmpl/" + templateFolder;
        final String tmplPath = secondaryPool.getLocalPath() + File.separator + templateInstallFolder;

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(libvirtUtilitiesHelper.buildTemplateLocation(storage, tmplPath)).thenReturn(location);
        when(libvirtUtilitiesHelper.generateUuidName()).thenReturn(tmplName);

        try {
            when(libvirtUtilitiesHelper.buildQcow2Processor(storage)).thenReturn(qcow2Processor);
            when(qcow2Processor.process(tmplPath, null, tmplName)).thenReturn(info);

            when(location.create(1, true, tmplName)).thenThrow(IOException.class);
        } catch (final ConfigurationException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        } catch (final IOException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath);
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl());
    }

    @Test
    public void testCreatePrivateTemplateFromSnapshotCommandCloudRuntime() {
        final StoragePool pool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "nfs:/127.0.0.1/storage/secondary";
        final Long dcId = 1l;
        final Long accountId = 1l;
        final Long volumeId = 1l;
        final String backedUpSnapshotUuid = "/run/9a0afe7c-26a7-4585-bf87-abf82ae106d9/";
        final String backedUpSnapshotName = "snap";
        final String origTemplateInstallPath = "/install/path/";
        final Long newTemplateId = 2l;
        final String templateName = "templ";
        final int wait = 0;

        final CreatePrivateTemplateFromSnapshotCommand command = new CreatePrivateTemplateFromSnapshotCommand(pool,
                secondaryStoragePoolURL, dcId, accountId, volumeId, backedUpSnapshotUuid, backedUpSnapshotName,
                origTemplateInstallPath, newTemplateId, templateName, wait);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondaryPool = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool snapshotPool = Mockito.mock(KvmStoragePool.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String tmplName = "ce97bbc1-34fe-4259-9202-74bbce2562ab";

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);

        String snapshotPath = command.getSnapshotUuid();
        final int index = snapshotPath.lastIndexOf("/");
        snapshotPath = snapshotPath.substring(0, index);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(libvirtUtilitiesHelper.generateUuidName()).thenReturn(tmplName);

        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath)).thenReturn(snapshotPool);
        when(storagePoolMgr.getStoragePoolByUri(command.getSecondaryStorageUrl())).thenReturn(secondaryPool);
        when(snapshotPool.getPhysicalDisk(command.getSnapshotName())).thenThrow(CloudRuntimeException.class);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl() + snapshotPath);
        verify(storagePoolMgr, times(1)).getStoragePoolByUri(command.getSecondaryStorageUrl());
    }

    @Test
    public void testCopyVolumeCommand() {
        final StoragePool storagePool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "nfs:/127.0.0.1/storage/secondary";
        final Long volumeId = 1l;
        final int wait = 0;
        final String volumePath = "/vol/path";
        final boolean toSecondaryStorage = true;
        final boolean executeInSequence = false;

        final CopyVolumeCommand command = new CopyVolumeCommand(volumeId, volumePath, storagePool, secondaryStoragePoolURL,
                toSecondaryStorage, wait, executeInSequence);

        final String destVolumeName = "ce97bbc1-34fe-4259-9202-74bbce2562ab";
        final String volumeDestPath = "/volumes/" + command.getVolumeId() + File.separator;

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondary = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool primary = Mockito.mock(KvmStoragePool.class);

        final KvmPhysicalDisk disk = Mockito.mock(KvmPhysicalDisk.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final StorageFilerTO pool = command.getPool();

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid())).thenReturn(primary);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(libvirtUtilitiesHelper.generateUuidName()).thenReturn(destVolumeName);
        when(primary.getPhysicalDisk(command.getVolumePath())).thenReturn(disk);
        when(storagePoolMgr.getStoragePoolByUri(secondaryStoragePoolURL)).thenReturn(secondary);
        when(secondary.getType()).thenReturn(StoragePoolType.ManagedNFS);
        when(secondary.getUuid()).thenReturn("60d979d8-d132-4181-8eca-8dfde50d7df6");
        when(secondary.createFolder(volumeDestPath)).thenReturn(true);
        when(storagePoolMgr.deleteStoragePool(secondary.getType(), secondary.getUuid())).thenReturn(true);
        when(storagePoolMgr.getStoragePoolByUri(secondaryStoragePoolURL + volumeDestPath)).thenReturn(secondary);
        when(storagePoolMgr.copyPhysicalDisk(disk, destVolumeName, secondary, 0)).thenReturn(disk);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
    }

    @Test
    public void testCopyVolumeCommandToSecFalse() {
        final StoragePool storagePool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "nfs:/127.0.0.1/storage/secondary";
        final Long volumeId = 1l;
        final int wait = 0;
        final String volumePath = "/vol/path";
        final boolean toSecondaryStorage = false;
        final boolean executeInSequence = false;

        final CopyVolumeCommand command = new CopyVolumeCommand(volumeId, volumePath, storagePool, secondaryStoragePoolURL,
                toSecondaryStorage, wait, executeInSequence);

        final String destVolumeName = "ce97bbc1-34fe-4259-9202-74bbce2562ab";
        final String volumeDestPath = "/volumes/" + command.getVolumeId() + File.separator;

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondary = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool primary = Mockito.mock(KvmStoragePool.class);

        final KvmPhysicalDisk disk = Mockito.mock(KvmPhysicalDisk.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final StorageFilerTO pool = command.getPool();

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid())).thenReturn(primary);
        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(libvirtUtilitiesHelper.generateUuidName()).thenReturn(destVolumeName);
        when(secondary.getType()).thenReturn(StoragePoolType.ManagedNFS);
        when(secondary.getUuid()).thenReturn("60d979d8-d132-4181-8eca-8dfde50d7df6");
        when(storagePoolMgr.getStoragePoolByUri(secondaryStoragePoolURL + volumeDestPath)).thenReturn(secondary);
        when(primary.getPhysicalDisk(command.getVolumePath() + ".qcow2")).thenReturn(disk);
        when(storagePoolMgr.copyPhysicalDisk(disk, destVolumeName, primary, 0)).thenReturn(disk);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
    }

    @Test
    public void testCopyVolumeCommandCloudRuntime() {
        final StoragePool storagePool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "nfs:/127.0.0.1/storage/secondary";
        final Long volumeId = 1l;
        final int wait = 0;
        final String volumePath = "/vol/path";
        final boolean toSecondaryStorage = false;
        final boolean executeInSequence = false;

        final CopyVolumeCommand command = new CopyVolumeCommand(volumeId, volumePath, storagePool, secondaryStoragePoolURL,
                toSecondaryStorage, wait, executeInSequence);

        final String destVolumeName = "ce97bbc1-34fe-4259-9202-74bbce2562ab";
        final String volumeDestPath = "/volumes/" + command.getVolumeId() + File.separator;

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondary = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool primary = Mockito.mock(KvmStoragePool.class);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final StorageFilerTO pool = command.getPool();

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid())).thenReturn(primary);
        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(libvirtUtilitiesHelper.generateUuidName()).thenReturn(destVolumeName);
        when(secondary.getType()).thenReturn(StoragePoolType.ManagedNFS);
        when(secondary.getUuid()).thenReturn("60d979d8-d132-4181-8eca-8dfde50d7df6");
        when(storagePoolMgr.getStoragePoolByUri(secondaryStoragePoolURL + volumeDestPath)).thenReturn(secondary);
        when(secondary.getPhysicalDisk(command.getVolumePath() + ".qcow2")).thenThrow(CloudRuntimeException.class);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
    }

    @Test
    public void testCopyVolumeCommandCloudRuntime2() {
        final StoragePool storagePool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "nfs:/127.0.0.1/storage/secondary";
        final Long volumeId = 1l;
        final int wait = 0;
        final String volumePath = "/vol/path";
        final boolean toSecondaryStorage = false;
        final boolean executeInSequence = false;

        final CopyVolumeCommand command = new CopyVolumeCommand(volumeId, volumePath, storagePool, secondaryStoragePoolURL,
                toSecondaryStorage, wait, executeInSequence);

        final StorageFilerTO pool = command.getPool();

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid())).thenThrow(new CloudRuntimeException("error"));

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
    }

    @Test
    public void testCopyVolumeCommandPrimaryNotFound() {
        final StoragePool storagePool = Mockito.mock(StoragePool.class);
        final String secondaryStoragePoolURL = "nfs:/127.0.0.1/storage/secondary";
        final Long volumeId = 1l;
        final int wait = 0;
        final String volumePath = "/vol/path";
        final boolean toSecondaryStorage = false;
        final boolean executeInSequence = false;

        final CopyVolumeCommand command = new CopyVolumeCommand(volumeId, volumePath, storagePool, secondaryStoragePoolURL,
                toSecondaryStorage, wait, executeInSequence);

        final String destVolumeName = "ce97bbc1-34fe-4259-9202-74bbce2562ab";
        final String volumeDestPath = "/volumes/" + command.getVolumeId() + File.separator;

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool secondary = Mockito.mock(KvmStoragePool.class);
        final KvmStoragePool primary = Mockito.mock(KvmStoragePool.class);

        final KvmPhysicalDisk disk = Mockito.mock(KvmPhysicalDisk.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final StorageFilerTO pool = command.getPool();

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid())).thenThrow(
                new CloudRuntimeException("not found"));

        when(storagePoolMgr.createStoragePool(pool.getUuid(), pool.getHost(), pool.getPort(), pool.getPath(),
                pool.getUserInfo(), pool.getType())).thenReturn(primary);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(libvirtUtilitiesHelper.generateUuidName()).thenReturn(destVolumeName);
        when(secondary.getType()).thenReturn(StoragePoolType.ManagedNFS);
        when(secondary.getUuid()).thenReturn("60d979d8-d132-4181-8eca-8dfde50d7df6");
        when(storagePoolMgr.getStoragePoolByUri(secondaryStoragePoolURL + volumeDestPath)).thenReturn(secondary);
        when(primary.getPhysicalDisk(command.getVolumePath() + ".qcow2")).thenReturn(disk);
        when(storagePoolMgr.copyPhysicalDisk(disk, destVolumeName, primary, 0)).thenReturn(disk);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
    }

    @Test
    public void testPvlanSetupCommandDhcpAdd() {
        final String op = "add";
        final URI uri = URI.create("http://localhost");
        final String networkTag = "/105";
        final String dhcpName = "dhcp";
        final String dhcpMac = "00:00:00:00";
        final String dhcpIp = "127.0.0.1";

        final PvlanSetupCommand command = PvlanSetupCommand.createDhcpSetup(op, uri, networkTag, dhcpName, dhcpMac, dhcpIp);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);

        final String guestBridgeName = "br0";
        when(this.libvirtComputingResource.getGuestBridgeName()).thenReturn(guestBridgeName);

        final int timeout = 0;
        when(this.libvirtComputingResource.getScriptsTimeout()).thenReturn(timeout);
        final String ovsPvlanDhcpHostPath = "/pvlan";
        when(this.libvirtComputingResource.getOvsPvlanDhcpHostPath()).thenReturn(ovsPvlanDhcpHostPath);
        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);

        final List<InterfaceDef> ifaces = new ArrayList<>();
        final InterfaceDef nic = Mockito.mock(InterfaceDef.class);
        ifaces.add(nic);

        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(dhcpName)).thenReturn(conn);
            when(this.libvirtComputingResource.getInterfaces(conn, dhcpName)).thenReturn(ifaces);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(dhcpName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPvlanSetupCommandVm() {
        final String op = "add";
        final URI uri = URI.create("http://localhost");
        final String networkTag = "/105";
        final String vmMac = "00:00:00:00";

        final PvlanSetupCommand command = PvlanSetupCommand.createVmSetup(op, uri, networkTag, vmMac);

        final String guestBridgeName = "br0";
        when(this.libvirtComputingResource.getGuestBridgeName()).thenReturn(guestBridgeName);
        final int timeout = 0;
        when(this.libvirtComputingResource.getScriptsTimeout()).thenReturn(timeout);

        final String ovsPvlanVmPath = "/pvlan";
        when(this.libvirtComputingResource.getOvsPvlanVmPath()).thenReturn(ovsPvlanVmPath);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());
    }

    @Test
    public void testPvlanSetupCommandDhcpException() {
        final String op = "add";
        final URI uri = URI.create("http://localhost");
        final String networkTag = "/105";
        final String dhcpName = "dhcp";
        final String dhcpMac = "00:00:00:00";
        final String dhcpIp = "127.0.0.1";

        final PvlanSetupCommand command = PvlanSetupCommand.createDhcpSetup(op, uri, networkTag, dhcpName, dhcpMac, dhcpIp);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String guestBridgeName = "br0";
        when(this.libvirtComputingResource.getGuestBridgeName()).thenReturn(guestBridgeName);

        final int timeout = 0;
        when(this.libvirtComputingResource.getScriptsTimeout()).thenReturn(timeout);
        final String ovsPvlanDhcpHostPath = "/pvlan";
        when(this.libvirtComputingResource.getOvsPvlanDhcpHostPath()).thenReturn(ovsPvlanDhcpHostPath);
        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);

        try {
            when(libvirtUtilitiesHelper.getConnectionByVmName(dhcpName)).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByVmName(dhcpName);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPvlanSetupCommandDhcpDelete() {
        final String op = "delete";
        final URI uri = URI.create("http://localhost");
        final String networkTag = "/105";
        final String dhcpName = "dhcp";
        final String dhcpMac = "00:00:00:00";
        final String dhcpIp = "127.0.0.1";

        final PvlanSetupCommand command = PvlanSetupCommand.createDhcpSetup(op, uri, networkTag, dhcpName, dhcpMac, dhcpIp);

        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        final String guestBridgeName = "br0";
        when(this.libvirtComputingResource.getGuestBridgeName()).thenReturn(guestBridgeName);

        final int timeout = 0;
        when(this.libvirtComputingResource.getScriptsTimeout()).thenReturn(timeout);
        final String ovsPvlanDhcpHostPath = "/pvlan";
        when(this.libvirtComputingResource.getOvsPvlanDhcpHostPath()).thenReturn(ovsPvlanDhcpHostPath);
        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());
    }

    @Test
    @Ignore
    public void testResizeVolumeCommand() {
        final String path = "nfs:/127.0.0.1/storage/secondary";
        final StorageFilerTO pool = Mockito.mock(StorageFilerTO.class);
        final Long currentSize = 100l;
        final Long newSize = 200l;
        final boolean shrinkOk = true;
        final String vmInstance = "Test";

        final ResizeVolumeCommand command = new ResizeVolumeCommand(path, pool, currentSize, newSize, shrinkOk, vmInstance);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool storagePool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk vol = Mockito.mock(KvmPhysicalDisk.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final StorageVol v = Mockito.mock(StorageVol.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid())).thenReturn(storagePool);
        when(storagePool.getPhysicalDisk(path)).thenReturn(vol);
        when(vol.getPath()).thenReturn(path);
        when(storagePool.getType()).thenReturn(StoragePoolType.RBD);
        when(vol.getFormat()).thenReturn(PhysicalDiskFormat.FILE);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnection()).thenReturn(conn);
            when(conn.storageVolLookupByPath(path)).thenReturn(v);

            when(conn.getLibVirVersion()).thenReturn(10010l);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnection();
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testResizeVolumeCommandSameSize() {
        final String path = "nfs:/127.0.0.1/storage/secondary";
        final StorageFilerTO pool = Mockito.mock(StorageFilerTO.class);
        final Long currentSize = 100l;
        final Long newSize = 100l;
        final boolean shrinkOk = false;
        final String vmInstance = "Test";

        final ResizeVolumeCommand command = new ResizeVolumeCommand(path, pool, currentSize, newSize, shrinkOk, vmInstance);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());
    }

    @Test
    @Ignore
    public void testResizeVolumeCommandShrink() {
        final String path = "nfs:/127.0.0.1/storage/secondary";
        final StorageFilerTO pool = Mockito.mock(StorageFilerTO.class);
        final Long currentSize = 100l;
        final Long newSize = 200l;
        final boolean shrinkOk = true;
        final String vmInstance = "Test";

        final ResizeVolumeCommand command = new ResizeVolumeCommand(path, pool, currentSize, newSize, shrinkOk, vmInstance);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool storagePool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk vol = Mockito.mock(KvmPhysicalDisk.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid())).thenReturn(storagePool);
        when(storagePool.getPhysicalDisk(path)).thenReturn(vol);
        when(vol.getPath()).thenReturn(path);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());
    }

    @Test
    @Ignore
    public void testResizeVolumeCommandException() {
        final String path = "nfs:/127.0.0.1/storage/secondary";
        final StorageFilerTO pool = Mockito.mock(StorageFilerTO.class);
        final Long currentSize = 100l;
        final Long newSize = 200l;
        final boolean shrinkOk = false;
        final String vmInstance = "Test";

        final ResizeVolumeCommand command = new ResizeVolumeCommand(path, pool, currentSize, newSize, shrinkOk, vmInstance);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool storagePool = Mockito.mock(KvmStoragePool.class);
        final KvmPhysicalDisk vol = Mockito.mock(KvmPhysicalDisk.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid())).thenReturn(storagePool);
        when(storagePool.getPhysicalDisk(path)).thenReturn(vol);
        when(vol.getPath()).thenReturn(path);
        when(storagePool.getType()).thenReturn(StoragePoolType.RBD);
        when(vol.getFormat()).thenReturn(PhysicalDiskFormat.FILE);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnection()).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();

        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnection();
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testResizeVolumeCommandException2() {
        final String path = "nfs:/127.0.0.1/storage/secondary";
        final StorageFilerTO pool = Mockito.mock(StorageFilerTO.class);
        final Long currentSize = 100l;
        final Long newSize = 200l;
        final boolean shrinkOk = false;
        final String vmInstance = "Test";

        final ResizeVolumeCommand command = new ResizeVolumeCommand(path, pool, currentSize, newSize, shrinkOk, vmInstance);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final KvmStoragePool storagePool = Mockito.mock(KvmStoragePool.class);

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(storagePoolMgr.getStoragePool(pool.getType(), pool.getUuid())).thenReturn(storagePool);
        when(storagePool.getPhysicalDisk(path)).thenThrow(CloudRuntimeException.class);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);
    }

    @Test
    public void testNetworkElementCommand() {
        final CheckRouterCommand command = new CheckRouterCommand();

        final VirtualRoutingResource virtRouterResource = Mockito.mock(VirtualRoutingResource.class);
        when(this.libvirtComputingResource.getVirtRouterResource()).thenReturn(virtRouterResource);

        when(virtRouterResource.executeRequest(command)).thenReturn(new CheckRouterAnswer(command, "mock_resource"));

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());
    }

    @Test
    public void testStorageSubSystemCommand() {
        final DiskTO disk = Mockito.mock(DiskTO.class);
        final String vmName = "Test";
        final AttachCommand command = new AttachCommand(disk, vmName);

        final StorageSubsystemCommandHandler handler = Mockito.mock(StorageSubsystemCommandHandler.class);
        when(this.libvirtComputingResource.getStorageHandler()).thenReturn(handler);

        when(handler.handleStorageCommands(command)).thenReturn(new AttachAnswer(disk));

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());
    }

    @Test
    public void testStartCommandFailedConnect() {
        final VirtualMachineTO vmSpec = Mockito.mock(VirtualMachineTO.class);
        final Host host = Mockito.mock(Host.class);
        final boolean executeInSequence = false;

        final StartCommand command = new StartCommand(vmSpec, host, executeInSequence);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtVmDef vmDef = Mockito.mock(LibvirtVmDef.class);

        final NicTO nic = Mockito.mock(NicTO.class);
        final NicTO[] nics = new NicTO[]{nic};

        final String vmName = "Test";

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(vmSpec.getNics()).thenReturn(nics);
        when(vmSpec.getType()).thenReturn(VirtualMachineType.DomainRouter);
        when(vmSpec.getName()).thenReturn(vmName);
        when(this.libvirtComputingResource.createVmFromSpec(vmSpec)).thenReturn(vmDef);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByType(vmDef.getHvsType())).thenReturn(conn);
            doNothing().when(this.libvirtComputingResource).createVbd(conn, vmSpec, vmName, vmDef);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        } catch (final URISyntaxException e) {
            fail(e.getMessage());
        }

        when(storagePoolMgr.connectPhysicalDisksViaVmSpec(vmSpec)).thenReturn(false);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByType(vmDef.getHvsType());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testStartCommandLibvirtException() {
        final VirtualMachineTO vmSpec = Mockito.mock(VirtualMachineTO.class);
        final Host host = Mockito.mock(Host.class);
        final boolean executeInSequence = false;

        final StartCommand command = new StartCommand(vmSpec, host, executeInSequence);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final LibvirtVmDef vmDef = Mockito.mock(LibvirtVmDef.class);

        final NicTO nic = Mockito.mock(NicTO.class);
        final NicTO[] nics = new NicTO[]{nic};

        final String vmName = "Test";

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(vmSpec.getNics()).thenReturn(nics);
        when(vmSpec.getType()).thenReturn(VirtualMachineType.DomainRouter);
        when(vmSpec.getName()).thenReturn(vmName);
        when(this.libvirtComputingResource.createVmFromSpec(vmSpec)).thenReturn(vmDef);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByType(vmDef.getHvsType())).thenThrow(LibvirtException.class);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByType(vmDef.getHvsType());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testStartCommandInternalError() {
        final VirtualMachineTO vmSpec = Mockito.mock(VirtualMachineTO.class);
        final Host host = Mockito.mock(Host.class);
        final boolean executeInSequence = false;

        final StartCommand command = new StartCommand(vmSpec, host, executeInSequence);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtVmDef vmDef = Mockito.mock(LibvirtVmDef.class);

        final NicTO nic = Mockito.mock(NicTO.class);
        final NicTO[] nics = new NicTO[]{nic};

        final String vmName = "Test";

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(vmSpec.getNics()).thenReturn(nics);
        when(vmSpec.getType()).thenReturn(VirtualMachineType.DomainRouter);
        when(vmSpec.getName()).thenReturn(vmName);
        when(this.libvirtComputingResource.createVmFromSpec(vmSpec)).thenReturn(vmDef);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByType(vmDef.getHvsType())).thenReturn(conn);
            doThrow(InternalErrorException.class).when(this.libvirtComputingResource).createVbd(conn, vmSpec, vmName, vmDef);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        } catch (final URISyntaxException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByType(vmDef.getHvsType());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testStartCommandUriException() {
        final VirtualMachineTO vmSpec = Mockito.mock(VirtualMachineTO.class);
        final Host host = Mockito.mock(Host.class);
        final boolean executeInSequence = false;

        final StartCommand command = new StartCommand(vmSpec, host, executeInSequence);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtVmDef vmDef = Mockito.mock(LibvirtVmDef.class);

        final NicTO nic = Mockito.mock(NicTO.class);
        final NicTO[] nics = new NicTO[]{nic};

        final String vmName = "Test";

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(vmSpec.getNics()).thenReturn(nics);
        when(vmSpec.getType()).thenReturn(VirtualMachineType.DomainRouter);
        when(vmSpec.getName()).thenReturn(vmName);
        when(this.libvirtComputingResource.createVmFromSpec(vmSpec)).thenReturn(vmDef);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByType(vmDef.getHvsType())).thenReturn(conn);
            doThrow(URISyntaxException.class).when(this.libvirtComputingResource).createVbd(conn, vmSpec, vmName, vmDef);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        } catch (final URISyntaxException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertFalse(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByType(vmDef.getHvsType());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testStartCommand() {
        final VirtualMachineTO vmSpec = Mockito.mock(VirtualMachineTO.class);
        final Host host = Mockito.mock(Host.class);
        final boolean executeInSequence = false;

        final StartCommand command = new StartCommand(vmSpec, host, executeInSequence);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtVmDef vmDef = Mockito.mock(LibvirtVmDef.class);
        final VirtualRoutingResource virtRouterResource = Mockito.mock(VirtualRoutingResource.class);

        final NicTO nic = Mockito.mock(NicTO.class);
        final NicTO[] nics = new NicTO[]{nic};
        final int[] vms = new int[0];

        final String vmName = "Test";
        final String controlIp = "127.0.0.1";

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(vmSpec.getNics()).thenReturn(nics);
        when(vmSpec.getType()).thenReturn(VirtualMachineType.DomainRouter);
        when(vmSpec.getName()).thenReturn(vmName);
        when(this.libvirtComputingResource.createVmFromSpec(vmSpec)).thenReturn(vmDef);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByType(vmDef.getHvsType())).thenReturn(conn);
            when(conn.listDomains()).thenReturn(vms);
            doNothing().when(this.libvirtComputingResource).createVbd(conn, vmSpec, vmName, vmDef);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        } catch (final URISyntaxException e) {
            fail(e.getMessage());
        }

        when(storagePoolMgr.connectPhysicalDisksViaVmSpec(vmSpec)).thenReturn(true);
        try {
            doNothing().when(this.libvirtComputingResource).createVifs(vmSpec, vmDef);

            when(this.libvirtComputingResource.startVm(conn, vmName, vmDef.toString())).thenReturn("SUCCESS");

            when(vmSpec.getBootArgs()).thenReturn("ls -lart");
            when(this.libvirtComputingResource.passCmdLine(vmName, vmSpec.getBootArgs())).thenReturn(true);

            when(nic.getIp()).thenReturn(controlIp);
            when(nic.getType()).thenReturn(TrafficType.Control);
            when(this.libvirtComputingResource.getVirtRouterResource()).thenReturn(virtRouterResource);
            when(virtRouterResource.connect(controlIp, 30, 5000)).thenReturn(true);
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByType(vmDef.getHvsType());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testStartCommandIsolationEc2() {
        final VirtualMachineTO vmSpec = Mockito.mock(VirtualMachineTO.class);
        final Host host = Mockito.mock(Host.class);
        final boolean executeInSequence = false;

        final StartCommand command = new StartCommand(vmSpec, host, executeInSequence);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtVmDef vmDef = Mockito.mock(LibvirtVmDef.class);
        final VirtualRoutingResource virtRouterResource = Mockito.mock(VirtualRoutingResource.class);

        final NicTO nic = Mockito.mock(NicTO.class);
        final NicTO[] nics = new NicTO[]{nic};
        final int[] vms = new int[0];

        final String vmName = "Test";
        final String controlIp = "127.0.0.1";

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(vmSpec.getNics()).thenReturn(nics);
        when(vmSpec.getType()).thenReturn(VirtualMachineType.DomainRouter);
        when(vmSpec.getName()).thenReturn(vmName);
        when(this.libvirtComputingResource.createVmFromSpec(vmSpec)).thenReturn(vmDef);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByType(vmDef.getHvsType())).thenReturn(conn);
            when(conn.listDomains()).thenReturn(vms);
            doNothing().when(this.libvirtComputingResource).createVbd(conn, vmSpec, vmName, vmDef);
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        } catch (final URISyntaxException e) {
            fail(e.getMessage());
        }

        when(storagePoolMgr.connectPhysicalDisksViaVmSpec(vmSpec)).thenReturn(true);
        try {
            doNothing().when(this.libvirtComputingResource).createVifs(vmSpec, vmDef);

            when(this.libvirtComputingResource.startVm(conn, vmName, vmDef.toString())).thenReturn("SUCCESS");

            when(nic.getIsolationUri()).thenReturn(new URI("ec2://test"));

            when(vmSpec.getBootArgs()).thenReturn("ls -lart");
            when(this.libvirtComputingResource.passCmdLine(vmName, vmSpec.getBootArgs())).thenReturn(true);

            when(nic.getIp()).thenReturn(controlIp);
            when(nic.getType()).thenReturn(TrafficType.Control);
            when(this.libvirtComputingResource.getVirtRouterResource()).thenReturn(virtRouterResource);
            when(virtRouterResource.connect(controlIp, 30, 5000)).thenReturn(true);
        } catch (final InternalErrorException e) {
            fail(e.getMessage());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        } catch (final URISyntaxException e) {
            fail(e.getMessage());
        }

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());

        verify(this.libvirtComputingResource, times(1)).getStoragePoolMgr();
        verify(this.libvirtComputingResource, times(1)).getLibvirtUtilitiesHelper();
        try {
            verify(libvirtUtilitiesHelper, times(1)).getConnectionByType(vmDef.getHvsType());
        } catch (final LibvirtException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testStartCommandHostMemory() {
        final VirtualMachineTO vmSpec = Mockito.mock(VirtualMachineTO.class);
        final Host host = Mockito.mock(Host.class);
        final boolean executeInSequence = false;

        final StartCommand command = new StartCommand(vmSpec, host, executeInSequence);

        final KvmStoragePoolManager storagePoolMgr = Mockito.mock(KvmStoragePoolManager.class);
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Connect conn = Mockito.mock(Connect.class);
        final LibvirtVmDef vmDef = Mockito.mock(LibvirtVmDef.class);

        final NicTO nic = Mockito.mock(NicTO.class);
        final NicTO[] nics = new NicTO[]{nic};
        final int vmId = 1;
        final int[] vms = new int[]{vmId};
        final Domain dm = Mockito.mock(Domain.class);

        final String vmName = "Test";

        when(this.libvirtComputingResource.getStoragePoolMgr()).thenReturn(storagePoolMgr);
        when(vmSpec.getNics()).thenReturn(nics);
        when(vmSpec.getType()).thenReturn(VirtualMachineType.User);
        when(vmSpec.getName()).thenReturn(vmName);
        when(vmSpec.getMaxRam()).thenReturn(512L);
        when(this.libvirtComputingResource.createVmFromSpec(vmSpec)).thenReturn(vmDef);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        try {
            when(libvirtUtilitiesHelper.getConnectionByType(vmDef.getHvsType())).thenReturn(conn);
            when(conn.listDomains()).thenReturn(vms);
            when(conn.domainLookupByID(vmId)).thenReturn(dm);
            when(dm.getMaxMemory()).thenReturn(1024L);
            when(dm.getName()).thenReturn(vmName);
            when(this.libvirtComputingResource.getTotalMemory()).thenReturn(2048 * 1024L);
            doNothing().when(this.libvirtComputingResource).createVbd(conn, vmSpec, vmName, vmDef);
        } catch (final LibvirtException | InternalErrorException | URISyntaxException e) {
            fail(e.getMessage());
        }

        when(storagePoolMgr.connectPhysicalDisksViaVmSpec(vmSpec)).thenReturn(true);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);
        assertTrue(answer.getResult());
    }

    @Test
    public void testUpdateHostPasswordCommand() {
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Script script = Mockito.mock(Script.class);

        final String hostIp = "127.0.0.1";
        final String username = "root";
        final String newPassword = "password";

        final UpdateHostPasswordCommand command = new UpdateHostPasswordCommand(username, newPassword, hostIp);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(this.libvirtComputingResource.getUpdateHostPasswdPath()).thenReturn("/tmp");
        when(libvirtUtilitiesHelper.buildScript(this.libvirtComputingResource.getUpdateHostPasswdPath())).thenReturn(script);

        when(script.execute()).thenReturn(null);

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);

        assertTrue(answer.getResult());
    }

    @Test
    public void testUpdateHostPasswordCommandFail() {
        final LibvirtUtilitiesHelper libvirtUtilitiesHelper = Mockito.mock(LibvirtUtilitiesHelper.class);
        final Script script = Mockito.mock(Script.class);

        final String hostIp = "127.0.0.1";
        final String username = "root";
        final String newPassword = "password";

        final UpdateHostPasswordCommand command = new UpdateHostPasswordCommand(username, newPassword, hostIp);

        when(this.libvirtComputingResource.getLibvirtUtilitiesHelper()).thenReturn(libvirtUtilitiesHelper);
        when(this.libvirtComputingResource.getUpdateHostPasswdPath()).thenReturn("/tmp");
        when(libvirtUtilitiesHelper.buildScript(this.libvirtComputingResource.getUpdateHostPasswdPath())).thenReturn(script);

        when(script.execute()).thenReturn("#FAIL");

        final LibvirtRequestWrapper wrapper = LibvirtRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, this.libvirtComputingResource);

        assertFalse(answer.getResult());
    }

    @Test
    public void testIsInterface() {
        final LibvirtComputingResource lvcr = new LibvirtComputingResource();
        assertFalse(lvcr.isInterface("bla"));
        assertTrue(lvcr.isInterface("p99p00"));
        for (final String ifNamePattern : lvcr.getIfNamePatterns()) {
            // excluding regexps as "\\\\d+" won't replace with String.replaceAll(String,String);
            if (!ifNamePattern.contains("\\")) {
                final String ifName = ifNamePattern.replaceFirst("\\^", "") + "0";
                assertTrue("The pattern '" + ifNamePattern + "' is expected to be valid for interface " + ifName, lvcr.isInterface(ifName));
            }
        }
    }
}
