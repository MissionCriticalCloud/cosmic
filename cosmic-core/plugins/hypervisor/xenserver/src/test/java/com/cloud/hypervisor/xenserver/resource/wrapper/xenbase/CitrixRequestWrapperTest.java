package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.AttachIsoCommand;
import com.cloud.agent.api.CheckHealthCommand;
import com.cloud.agent.api.CheckNetworkCommand;
import com.cloud.agent.api.CheckOnHostCommand;
import com.cloud.agent.api.CheckVirtualMachineCommand;
import com.cloud.agent.api.ClusterVMMetaDataSyncCommand;
import com.cloud.agent.api.Command;
import com.cloud.agent.api.CreateStoragePoolCommand;
import com.cloud.agent.api.CreateVMSnapshotCommand;
import com.cloud.agent.api.DeleteStoragePoolCommand;
import com.cloud.agent.api.DeleteVMSnapshotCommand;
import com.cloud.agent.api.GetHostStatsCommand;
import com.cloud.agent.api.GetStorageStatsCommand;
import com.cloud.agent.api.GetVmDiskStatsCommand;
import com.cloud.agent.api.GetVmIpAddressCommand;
import com.cloud.agent.api.GetVmStatsCommand;
import com.cloud.agent.api.GetVncPortCommand;
import com.cloud.agent.api.MaintainCommand;
import com.cloud.agent.api.MigrateCommand;
import com.cloud.agent.api.ModifySshKeysCommand;
import com.cloud.agent.api.ModifyStoragePoolCommand;
import com.cloud.agent.api.PerformanceMonitorCommand;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.agent.api.PlugNicCommand;
import com.cloud.agent.api.PrepareForMigrationCommand;
import com.cloud.agent.api.PvlanSetupCommand;
import com.cloud.agent.api.ReadyCommand;
import com.cloud.agent.api.RebootAnswer;
import com.cloud.agent.api.RebootCommand;
import com.cloud.agent.api.RebootRouterCommand;
import com.cloud.agent.api.RevertToVMSnapshotCommand;
import com.cloud.agent.api.ScaleVmCommand;
import com.cloud.agent.api.SetupCommand;
import com.cloud.agent.api.StartCommand;
import com.cloud.agent.api.StopCommand;
import com.cloud.agent.api.UnPlugNicCommand;
import com.cloud.agent.api.UpdateHostPasswordCommand;
import com.cloud.agent.api.UpgradeSnapshotCommand;
import com.cloud.agent.api.VMSnapshotTO;
import com.cloud.agent.api.check.CheckSshCommand;
import com.cloud.agent.api.proxy.CheckConsoleProxyLoadCommand;
import com.cloud.agent.api.proxy.WatchConsoleProxyLoadCommand;
import com.cloud.agent.api.storage.CreateAnswer;
import com.cloud.agent.api.storage.CreateCommand;
import com.cloud.agent.api.storage.DestroyCommand;
import com.cloud.agent.api.storage.PrimaryStorageDownloadCommand;
import com.cloud.agent.api.storage.ResizeVolumeCommand;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.DiskTO;
import com.cloud.agent.api.to.NicTO;
import com.cloud.agent.api.to.StorageFilerTO;
import com.cloud.agent.api.to.VirtualMachineTO;
import com.cloud.agent.resource.virtualnetwork.VRScripts;
import com.cloud.host.HostEnvironment;
import com.cloud.hypervisor.xenserver.resource.CitrixResourceBase;
import com.cloud.hypervisor.xenserver.resource.XsHost;
import com.cloud.hypervisor.xenserver.resource.XsLocalNetwork;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.PhysicalNetworkSetupInfo;
import com.cloud.storage.Storage.ImageFormat;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.command.AttachAnswer;
import com.cloud.storage.command.AttachCommand;
import com.cloud.storage.datastore.db.StoragePoolVO;
import com.cloud.storage.resource.StorageSubsystemCommandHandler;
import com.cloud.storage.to.VolumeObjectTO;
import com.cloud.utils.Pair;
import com.cloud.vm.DiskProfile;
import com.cloud.vm.VirtualMachine;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Host;
import com.xensource.xenapi.Marshalling;
import com.xensource.xenapi.Network;
import com.xensource.xenapi.Pool;
import com.xensource.xenapi.Types.XenAPIException;
import com.xensource.xenapi.VM;
import com.xensource.xenapi.VMGuestMetrics;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Pool.class)
public class CitrixRequestWrapperTest {

    @Mock
    private CitrixResourceBase citrixResourceBase;
    @Mock
    private RebootAnswer rebootAnswer;
    @Mock
    private CreateAnswer createAnswer;

    @Test
    public void testWrapperInstance() {
        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);
    }

    @Test
    public void testUnknownCommand() {
        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        try {
            wrapper.execute(new NotAValidCommand(), citrixResourceBase);
        } catch (final Exception e) {
            assertTrue(e instanceof NullPointerException);
        }
    }

    @Test
    public void testExecuteRebootRouterCommand() {
        final RebootRouterCommand rebootRouterCommand = new RebootRouterCommand("Test", "127.0.0.1");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(rebootRouterCommand, citrixResourceBase);

        verify(citrixResourceBase, times(2)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testExecuteCreateCommand() {
        final StoragePoolVO poolVO = Mockito.mock(StoragePoolVO.class);
        final DiskProfile diskProfile = Mockito.mock(DiskProfile.class);
        final CreateCommand createCommand = new CreateCommand(diskProfile, "", poolVO, false);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(createCommand, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testCheckConsoleProxyLoadCommand() {
        final CheckConsoleProxyLoadCommand consoleProxyCommand = new CheckConsoleProxyLoadCommand();

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(consoleProxyCommand, citrixResourceBase);

        assertFalse(answer.getResult());
    }

    @Test
    public void testWatchConsoleProxyLoadCommand() {
        final WatchConsoleProxyLoadCommand watchConsoleProxyCommand = new WatchConsoleProxyLoadCommand(0, 0, "", "", 0);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(watchConsoleProxyCommand, citrixResourceBase);

        assertFalse(answer.getResult());
    }

    @Test
    public void testReadyCommand() {
        final ReadyCommand readyCommand = new ReadyCommand();

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(readyCommand, citrixResourceBase);

        assertFalse(answer.getResult());
    }

    @Test
    public void testGetHostStatsCommand() {
        final GetHostStatsCommand statsCommand = new GetHostStatsCommand(null, null, 0);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(statsCommand, citrixResourceBase);

        assertTrue(answer.getResult());
    }

    @Test
    public void testGetVmStatsCommand() {
        final GetVmStatsCommand statsCommand = new GetVmStatsCommand(new ArrayList<>(), null, null);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(statsCommand, citrixResourceBase);

        assertTrue(answer.getResult());
    }

    @Test
    public void testGetVmDiskStatsCommand() {
        final GetVmDiskStatsCommand diskStatsCommand = new GetVmDiskStatsCommand(new ArrayList<>(), null, null);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(diskStatsCommand, citrixResourceBase);

        assertTrue(answer.getResult());
    }

    @Test
    public void testCheckHealthCommand() {
        final CheckHealthCommand checkHealthCommand = new CheckHealthCommand();

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(checkHealthCommand, citrixResourceBase);

        assertFalse(answer.getResult());
    }

    @Test
    public void testStopCommand() {
        final StopCommand stopCommand = new StopCommand("Test", false, false);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(stopCommand, citrixResourceBase);

        assertFalse(answer.getResult());
    }

    @Test
    public void testRebootCommand() {
        final RebootCommand rebootCommand = new RebootCommand("Test", true);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(rebootCommand, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testCheckVirtualMachineCommand() {
        final CheckVirtualMachineCommand virtualMachineCommand = new CheckVirtualMachineCommand("Test");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(virtualMachineCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testPrepareForMigrationCommand() {
        final VirtualMachineTO machineTO = Mockito.mock(VirtualMachineTO.class);
        final PrepareForMigrationCommand prepareCommand = new PrepareForMigrationCommand(machineTO);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(prepareCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testMigrateCommand() {
        final VirtualMachineTO machineTO = Mockito.mock(VirtualMachineTO.class);
        final MigrateCommand migrateCommand = new MigrateCommand("Test", "127.0.0.1", false, machineTO, false);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(migrateCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testDestroyCommand() {

        final VMTemplateStorageResourceAssoc templateStorage = Mockito.mock(VMTemplateStorageResourceAssoc.class);
        final StoragePoolVO poolVO = Mockito.mock(StoragePoolVO.class);

        final DestroyCommand destroyCommand = new DestroyCommand(poolVO, templateStorage);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(destroyCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testCreateStoragePoolCommand() {
        final StoragePoolVO poolVO = Mockito.mock(StoragePoolVO.class);
        final XsHost xsHost = Mockito.mock(XsHost.class);

        final CreateStoragePoolCommand createStorageCommand = new CreateStoragePoolCommand(false, poolVO);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getHost()).thenReturn(xsHost);

        final Answer answer = wrapper.execute(createStorageCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testModifyStoragePoolCommand() {
        final StoragePoolVO poolVO = Mockito.mock(StoragePoolVO.class);
        final XsHost xsHost = Mockito.mock(XsHost.class);

        final ModifyStoragePoolCommand modifyStorageCommand = new ModifyStoragePoolCommand(false, poolVO);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getHost()).thenReturn(xsHost);

        final Answer answer = wrapper.execute(modifyStorageCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testDeleteStoragePoolCommand() {
        final StoragePoolVO poolVO = Mockito.mock(StoragePoolVO.class);
        final XsHost xsHost = Mockito.mock(XsHost.class);

        final DeleteStoragePoolCommand deleteStorageCommand = new DeleteStoragePoolCommand(poolVO);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getHost()).thenReturn(xsHost);

        final Answer answer = wrapper.execute(deleteStorageCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testResizeVolumeCommand() {
        final StorageFilerTO pool = Mockito.mock(StorageFilerTO.class);

        final ResizeVolumeCommand resizeCommand = new ResizeVolumeCommand("Test", pool, 1l, 3l, false, "Tests-1");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(resizeCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testAttachIsoCommand() {
        final AttachIsoCommand attachCommand = new AttachIsoCommand("Test", "/", true);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(attachCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testUpgradeSnapshotCommand() {
        final StoragePoolVO poolVO = Mockito.mock(StoragePoolVO.class);

        final UpgradeSnapshotCommand upgradeSnapshotCommand = new UpgradeSnapshotCommand(poolVO, "http", 1l, 1l, 1l, 1l, 1l, "/", "58c5778b-7dd1-47cc-a7b5-f768541bf278", "Test",
                "2.1");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(upgradeSnapshotCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testUpgradeSnapshotCommandNo21() {
        final StoragePoolVO poolVO = Mockito.mock(StoragePoolVO.class);

        final UpgradeSnapshotCommand upgradeSnapshotCommand = new UpgradeSnapshotCommand(poolVO, "http", 1l, 1l, 1l, 1l, 1l, "/", "58c5778b-7dd1-47cc-a7b5-f768541bf278", "Test",
                "3.1");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(upgradeSnapshotCommand, citrixResourceBase);
        verify(citrixResourceBase, times(0)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testGetStorageStatsCommand() {
        final XsHost xsHost = Mockito.mock(XsHost.class);
        final DataStoreTO store = Mockito.mock(DataStoreTO.class);

        final GetStorageStatsCommand storageStatsCommand = new GetStorageStatsCommand(store);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getHost()).thenReturn(xsHost);

        final Answer answer = wrapper.execute(storageStatsCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testPrimaryStorageDownloadCommand() {
        final XsHost xsHost = Mockito.mock(XsHost.class);
        final StoragePoolVO poolVO = Mockito.mock(StoragePoolVO.class);

        final PrimaryStorageDownloadCommand storageDownloadCommand = new PrimaryStorageDownloadCommand("Test", "http://127.0.0.1", ImageFormat.VHD, 1l, poolVO, 200);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getHost()).thenReturn(xsHost);

        final Answer answer = wrapper.execute(storageDownloadCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testGetVncPortCommand() {
        final GetVncPortCommand vncPortCommand = new GetVncPortCommand(1l, "Test");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(vncPortCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testSetupCommand() {
        final XsHost xsHost = Mockito.mock(XsHost.class);
        final HostEnvironment env = Mockito.mock(HostEnvironment.class);

        final SetupCommand setupCommand = new SetupCommand(env);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getHost()).thenReturn(xsHost);

        final Answer answer = wrapper.execute(setupCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testMaintainCommand() {
        // This test needs further work.

        final String uuid = "befc4dcd-f5c6-4015-8791-3c18622b7c7f";

        final Connection conn = Mockito.mock(Connection.class);
        final XsHost xsHost = Mockito.mock(XsHost.class);
        final XmlRpcClient client = Mockito.mock(XmlRpcClient.class);

        // final Host.Record hr = PowerMockito.mock(Host.Record.class);
        // final Host host = PowerMockito.mock(Host.class);

        final MaintainCommand maintainCommand = new MaintainCommand();

        final Map<String, Object> map = new Hashtable<>();
        map.put("Value", "Xen");

        final Map<String, Object> spiedMap = spy(map);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);
        when(citrixResourceBase.getHost()).thenReturn(xsHost);
        when(xsHost.getUuid()).thenReturn(uuid);
        when(conn.getSessionReference()).thenReturn("befc4dcd");

        try {
            final Object[] params = {Marshalling.toXMLRPC("befc4dcd"), Marshalling.toXMLRPC(uuid)};
            when(client.execute("host.get_by_uuid", new Object[]{"befc4dcd", uuid})).thenReturn(spiedMap);
            PowerMockito.when(conn, "dispatch", "host.get_by_uuid", params).thenReturn(spiedMap);
        } catch (final Exception e) {
            fail(e.getMessage());
        }

        // try {
        // PowerMockito.mockStatic(Host.class);
        // //BDDMockito.given(Host.getByUuid(conn,
        // xsHost.getUuid())).willReturn(host);
        // PowerMockito.when(Host.getByUuid(conn,
        // xsHost.getUuid())).thenReturn(host);
        // PowerMockito.verifyStatic(times(1));
        // } catch (final BadServerResponse e) {
        // fail(e.getMessage());
        // } catch (final XenAPIException e) {
        // fail(e.getMessage());
        // } catch (final XmlRpcException e) {
        // fail(e.getMessage());
        // }
        //
        // PowerMockito.mockStatic(Types.class);
        // PowerMockito.when(Types.toHostRecord(spiedMap)).thenReturn(hr);
        // PowerMockito.verifyStatic(times(1));
        //
        // try {
        // PowerMockito.mockStatic(Host.Record.class);
        // when(host.getRecord(conn)).thenReturn(hr);
        // verify(host, times(1)).getRecord(conn);
        // } catch (final BadServerResponse e) {
        // fail(e.getMessage());
        // } catch (final XenAPIException e) {
        // fail(e.getMessage());
        // } catch (final XmlRpcException e) {
        // fail(e.getMessage());
        // }

        final Answer answer = wrapper.execute(maintainCommand, citrixResourceBase);

        assertFalse(answer.getResult());
    }

    @Test
    public void testPingTestCommandHostIp() {
        final PingTestCommand pingTestCommand = new PingTestCommand("127.0.0.1");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(pingTestCommand, citrixResourceBase);
        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testPingTestCommandRouterPvtIps() {
        final PingTestCommand pingTestCommand = new PingTestCommand("127.0.0.1", "127.0.0.1");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(pingTestCommand, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testCheckOnHostCommand() {
        final com.cloud.host.Host host = Mockito.mock(com.cloud.host.Host.class);
        final CheckOnHostCommand onHostCommand = new CheckOnHostCommand(host);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(onHostCommand, citrixResourceBase);

        assertFalse(answer.getResult());
    }

    @Test
    public void testModifySshKeysCommand() {
        final ModifySshKeysCommand sshKeysCommand = new ModifySshKeysCommand("", "");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(sshKeysCommand, citrixResourceBase);

        assertTrue(answer.getResult());
    }

    @Test
    public void testStartCommand() {
        final VirtualMachineTO vm = Mockito.mock(VirtualMachineTO.class);
        final com.cloud.host.Host host = Mockito.mock(com.cloud.host.Host.class);

        final StartCommand startCommand = new StartCommand(vm, host, false);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(startCommand, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testCheckSshCommand() {
        final CheckSshCommand sshCommand = new CheckSshCommand("Test", "127.0.0.1", 22);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(sshCommand, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testUpdateHostPasswordCommand() {
        final XenServerUtilitiesHelper xenServerUtilitiesHelper = Mockito.mock(XenServerUtilitiesHelper.class);
        final Pair<Boolean, String> result = Mockito.mock(Pair.class);

        final UpdateHostPasswordCommand updatePwd = new UpdateHostPasswordCommand("test", "123", "127.0.0.1");

        when(citrixResourceBase.getPwdFromQueue()).thenReturn("password");

        final String hostIp = updatePwd.getHostIp();
        final String username = updatePwd.getUsername();
        final String hostPasswd = citrixResourceBase.getPwdFromQueue();
        final String newPassword = updatePwd.getNewPassword();

        final StringBuilder cmdLine = new StringBuilder();
        cmdLine.append(XenServerUtilitiesHelper.SCRIPT_CMD_PATH).append(VRScripts.UPDATE_HOST_PASSWD).append(' ').append(username).append(' ').append(newPassword);

        when(citrixResourceBase.getXenServerUtilitiesHelper()).thenReturn(xenServerUtilitiesHelper);
        when(xenServerUtilitiesHelper.buildCommandLine(XenServerUtilitiesHelper.SCRIPT_CMD_PATH, VRScripts.UPDATE_HOST_PASSWD, username, newPassword)).thenReturn(
                cmdLine.toString());

        try {
            when(xenServerUtilitiesHelper.executeSshWrapper(hostIp, 22, username, null, hostPasswd, cmdLine.toString())).thenReturn(result);
            when(result.first()).thenReturn(true);
            when(result.second()).thenReturn("");
        } catch (final Exception e) {
            fail(e.getMessage());
        }

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(updatePwd, citrixResourceBase);

        verify(citrixResourceBase, times(2)).getPwdFromQueue();
        verify(citrixResourceBase, times(1)).getXenServerUtilitiesHelper();
        verify(xenServerUtilitiesHelper, times(1)).buildCommandLine(XenServerUtilitiesHelper.SCRIPT_CMD_PATH, VRScripts.UPDATE_HOST_PASSWD, username, newPassword);
        try {
            verify(xenServerUtilitiesHelper, times(1)).executeSshWrapper(hostIp, 22, username, null, hostPasswd, cmdLine.toString());
        } catch (final Exception e) {
            fail(e.getMessage());
        }
        verify(result, times(1)).first();
        verify(result, times(1)).second();

        assertTrue(answer.getResult());
    }

    @Test
    public void testUpdateHostPasswordCommandFail() {
        final XenServerUtilitiesHelper xenServerUtilitiesHelper = Mockito.mock(XenServerUtilitiesHelper.class);
        final Pair<Boolean, String> result = Mockito.mock(Pair.class);

        final UpdateHostPasswordCommand updatePwd = new UpdateHostPasswordCommand("test", "123", "127.0.0.1");

        when(citrixResourceBase.getPwdFromQueue()).thenReturn("password");

        final String hostIp = updatePwd.getHostIp();
        final String username = updatePwd.getUsername();
        final String hostPasswd = citrixResourceBase.getPwdFromQueue();
        final String newPassword = updatePwd.getNewPassword();

        final StringBuilder cmdLine = new StringBuilder();
        cmdLine.append(XenServerUtilitiesHelper.SCRIPT_CMD_PATH).append(VRScripts.UPDATE_HOST_PASSWD).append(' ').append(username).append(' ').append(newPassword);

        when(citrixResourceBase.getXenServerUtilitiesHelper()).thenReturn(xenServerUtilitiesHelper);
        when(xenServerUtilitiesHelper.buildCommandLine(XenServerUtilitiesHelper.SCRIPT_CMD_PATH, VRScripts.UPDATE_HOST_PASSWD, username, newPassword)).thenReturn(
                cmdLine.toString());

        try {
            when(xenServerUtilitiesHelper.executeSshWrapper(hostIp, 22, username, null, hostPasswd, cmdLine.toString())).thenReturn(result);
            when(result.first()).thenReturn(false);
            when(result.second()).thenReturn("");
        } catch (final Exception e) {
            fail(e.getMessage());
        }

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(updatePwd, citrixResourceBase);

        verify(citrixResourceBase, times(2)).getPwdFromQueue();
        verify(citrixResourceBase, times(1)).getXenServerUtilitiesHelper();
        verify(xenServerUtilitiesHelper, times(1)).buildCommandLine(XenServerUtilitiesHelper.SCRIPT_CMD_PATH, VRScripts.UPDATE_HOST_PASSWD, username, newPassword);
        try {
            verify(xenServerUtilitiesHelper, times(1)).executeSshWrapper(hostIp, 22, username, null, hostPasswd, cmdLine.toString());
        } catch (final Exception e) {
            fail(e.getMessage());
        }
        verify(result, times(1)).first();
        verify(result, times(1)).second();

        assertFalse(answer.getResult());
    }

    @Test
    public void testUpdateHostPasswordCommandException() {
        final XenServerUtilitiesHelper xenServerUtilitiesHelper = Mockito.mock(XenServerUtilitiesHelper.class);

        final UpdateHostPasswordCommand updatePwd = new UpdateHostPasswordCommand("test", "123", "127.0.0.1");

        when(citrixResourceBase.getPwdFromQueue()).thenReturn("password");

        final String hostIp = updatePwd.getHostIp();
        final String username = updatePwd.getUsername();
        final String hostPasswd = citrixResourceBase.getPwdFromQueue();
        final String newPassword = updatePwd.getNewPassword();

        final StringBuilder cmdLine = new StringBuilder();
        cmdLine.append(XenServerUtilitiesHelper.SCRIPT_CMD_PATH).append(VRScripts.UPDATE_HOST_PASSWD).append(' ').append(username).append(' ').append(newPassword);

        when(citrixResourceBase.getXenServerUtilitiesHelper()).thenReturn(xenServerUtilitiesHelper);
        when(xenServerUtilitiesHelper.buildCommandLine(XenServerUtilitiesHelper.SCRIPT_CMD_PATH, VRScripts.UPDATE_HOST_PASSWD, username, newPassword)).thenReturn(
                cmdLine.toString());

        try {
            when(xenServerUtilitiesHelper.executeSshWrapper(hostIp, 22, username, null, hostPasswd, cmdLine.toString())).thenThrow(new Exception("testing failure"));
        } catch (final Exception e) {
            fail(e.getMessage());
        }

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(updatePwd, citrixResourceBase);

        verify(citrixResourceBase, times(2)).getPwdFromQueue();
        verify(citrixResourceBase, times(1)).getXenServerUtilitiesHelper();
        verify(xenServerUtilitiesHelper, times(1)).buildCommandLine(XenServerUtilitiesHelper.SCRIPT_CMD_PATH, VRScripts.UPDATE_HOST_PASSWD, username, newPassword);
        try {
            verify(xenServerUtilitiesHelper, times(1)).executeSshWrapper(hostIp, 22, username, null, hostPasswd, cmdLine.toString());
        } catch (final Exception e) {
            fail(e.getMessage());
        }

        assertFalse(answer.getResult());
    }

    @Test
    public void testClusterVMMetaDataSyncCommand() {
        final String uuid = "6172d8b7-ba10-4a70-93f9-ecaf41f51d53";

        final Connection conn = Mockito.mock(Connection.class);
        final XsHost xsHost = Mockito.mock(XsHost.class);

        final Pool pool = PowerMockito.mock(Pool.class);
        final Pool.Record poolr = Mockito.mock(Pool.Record.class);
        final Host.Record hostr = Mockito.mock(Host.Record.class);
        final Host master = Mockito.mock(Host.class);

        final ClusterVMMetaDataSyncCommand vmDataSync = new ClusterVMMetaDataSyncCommand(10, 1l);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);
        try {
            when(citrixResourceBase.getHost()).thenReturn(xsHost);
            when(citrixResourceBase.getHost().getUuid()).thenReturn(uuid);
            when(xsHost.getPool()).thenReturn("pool");

            PowerMockito.mockStatic(Pool.class);
            when(Pool.getByUuid(conn, "pool")).thenReturn(pool);

            PowerMockito.mockStatic(Pool.Record.class);
            when(pool.getRecord(conn)).thenReturn(poolr);
            poolr.master = master;
            when(poolr.master.getRecord(conn)).thenReturn(hostr);
            hostr.uuid = uuid;
        } catch (final XenAPIException | XmlRpcException e) {
            fail(e.getMessage());
        }

        final Answer answer = wrapper.execute(vmDataSync, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testCheckNetworkCommandSuccess() {
        final List<PhysicalNetworkSetupInfo> setupInfos = new ArrayList<>();

        final CheckNetworkCommand checkNet = new CheckNetworkCommand(setupInfos);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(checkNet, citrixResourceBase);

        assertTrue(answer.getResult());
    }

    @Test
    public void testCheckNetworkCommandFailure() {
        final PhysicalNetworkSetupInfo info = new PhysicalNetworkSetupInfo();

        final List<PhysicalNetworkSetupInfo> setupInfos = new ArrayList<>();
        setupInfos.add(info);

        final CheckNetworkCommand checkNet = new CheckNetworkCommand(setupInfos);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(checkNet, citrixResourceBase);

        assertFalse(answer.getResult());
    }

    @Test
    public void testPlugNicCommand() {
        final NicTO nicTO = Mockito.mock(NicTO.class);
        final Connection conn = Mockito.mock(Connection.class);

        final PlugNicCommand plugNic = new PlugNicCommand(nicTO, "Test", VirtualMachine.Type.User);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);

        final Answer answer = wrapper.execute(plugNic, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testUnPlugNicCommand() {
        final NicTO nicTO = Mockito.mock(NicTO.class);
        final Connection conn = Mockito.mock(Connection.class);

        final UnPlugNicCommand unplugNic = new UnPlugNicCommand(nicTO, "Test");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);

        final Answer answer = wrapper.execute(unplugNic, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testCreateVMSnapshotCommand() {
        final Connection conn = Mockito.mock(Connection.class);

        final VMSnapshotTO snapshotTO = Mockito.mock(VMSnapshotTO.class);
        final List<VolumeObjectTO> volumeTOs = new ArrayList<>();

        final CreateVMSnapshotCommand vmSnapshot = new CreateVMSnapshotCommand("Test", "uuid", snapshotTO, volumeTOs, "Debian");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);

        final Answer answer = wrapper.execute(vmSnapshot, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testDeleteVMSnapshotCommand() {
        final Connection conn = Mockito.mock(Connection.class);

        final VMSnapshotTO snapshotTO = Mockito.mock(VMSnapshotTO.class);
        final List<VolumeObjectTO> volumeTOs = new ArrayList<>();

        final DeleteVMSnapshotCommand vmSnapshot = new DeleteVMSnapshotCommand("Test", snapshotTO, volumeTOs, "Debian");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);

        final Answer answer = wrapper.execute(vmSnapshot, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testRevertToVMSnapshotCommand() {
        final Connection conn = Mockito.mock(Connection.class);

        final VMSnapshotTO snapshotTO = Mockito.mock(VMSnapshotTO.class);
        final List<VolumeObjectTO> volumeTOs = new ArrayList<>();

        final RevertToVMSnapshotCommand vmSnapshot = new RevertToVMSnapshotCommand("Test", "uuid", snapshotTO, volumeTOs, "Debian");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);

        final Answer answer = wrapper.execute(vmSnapshot, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testScaleVmCommand() {
        final String uuid = "6172d8b7-ba10-4a70-93f9-ecaf41f51d53";

        final VirtualMachineTO machineTO = Mockito.mock(VirtualMachineTO.class);
        final Connection conn = Mockito.mock(Connection.class);
        final XsHost xsHost = Mockito.mock(XsHost.class);
        final Host host = Mockito.mock(Host.class);

        final ScaleVmCommand scaleVm = new ScaleVmCommand(machineTO);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);
        when(citrixResourceBase.getHost()).thenReturn(xsHost);
        when(citrixResourceBase.getHost().getUuid()).thenReturn(uuid);

        try {
            when(citrixResourceBase.isDmcEnabled(conn, host)).thenReturn(true);
        } catch (final XenAPIException e) {
            fail(e.getMessage());
        } catch (final XmlRpcException e) {
            fail(e.getMessage());
        }

        final Answer answer = wrapper.execute(scaleVm, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testPvlanSetupCommandDhcpSuccess() {
        final String label = "net";

        final Connection conn = Mockito.mock(Connection.class);
        final XsLocalNetwork network = Mockito.mock(XsLocalNetwork.class);
        final Network network2 = Mockito.mock(Network.class);

        final PvlanSetupCommand lanSetup = PvlanSetupCommand.createDhcpSetup("add", URI.create("http://127.0.0.1"), "tag", "dhcp", "0:0:0:0:0:0", "127.0.0.1");

        final String primaryPvlan = lanSetup.getPrimary();
        final String isolatedPvlan = lanSetup.getIsolated();
        final String op = lanSetup.getOp();
        final String dhcpName = lanSetup.getDhcpName();
        final String dhcpMac = lanSetup.getDhcpMac();
        final String dhcpIp = lanSetup.getDhcpIp();

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);
        try {
            when(citrixResourceBase.getNativeNetworkForTraffic(conn, TrafficType.Guest, "tag")).thenReturn(network);
            when(network.getNetwork()).thenReturn(network2);
            when(network2.getNameLabel(conn)).thenReturn(label);
        } catch (final XenAPIException e) {
            fail(e.getMessage());
        } catch (final XmlRpcException e) {
            fail(e.getMessage());
        }

        when(citrixResourceBase.callHostPlugin(conn, "ovs-pvlan", "setup-pvlan-dhcp", "op", op, "nw-label", label, "primary-pvlan", primaryPvlan, "isolated-pvlan",
                isolatedPvlan, "dhcp-name", dhcpName, "dhcp-ip", dhcpIp, "dhcp-mac", dhcpMac)).thenReturn("true");

        final Answer answer = wrapper.execute(lanSetup, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testPvlanSetupCommandDhcpFailure() {
        final String label = "net";

        final Connection conn = Mockito.mock(Connection.class);
        final XsLocalNetwork network = Mockito.mock(XsLocalNetwork.class);
        final Network network2 = Mockito.mock(Network.class);

        final PvlanSetupCommand lanSetup = PvlanSetupCommand.createDhcpSetup("add", URI.create("http://127.0.0.1"), "tag", "dhcp", "0:0:0:0:0:0", "127.0.0.1");

        final String primaryPvlan = lanSetup.getPrimary();
        final String isolatedPvlan = lanSetup.getIsolated();
        final String op = lanSetup.getOp();
        final String dhcpName = lanSetup.getDhcpName();
        final String dhcpMac = lanSetup.getDhcpMac();
        final String dhcpIp = lanSetup.getDhcpIp();

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);
        try {
            when(citrixResourceBase.getNativeNetworkForTraffic(conn, TrafficType.Guest, "tag")).thenReturn(network);
            when(network.getNetwork()).thenReturn(network2);
            when(network2.getNameLabel(conn)).thenReturn(label);
        } catch (final XenAPIException e) {
            fail(e.getMessage());
        } catch (final XmlRpcException e) {
            fail(e.getMessage());
        }

        when(citrixResourceBase.callHostPlugin(conn, "ovs-pvlan", "setup-pvlan-dhcp", "op", op, "nw-label", label, "primary-pvlan", primaryPvlan, "isolated-pvlan",
                isolatedPvlan, "dhcp-name", dhcpName, "dhcp-ip", dhcpIp, "dhcp-mac", dhcpMac)).thenReturn("false");

        final Answer answer = wrapper.execute(lanSetup, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testPvlanSetupCommandVmSuccess() {
        final String label = "net";

        final Connection conn = Mockito.mock(Connection.class);
        final XsLocalNetwork network = Mockito.mock(XsLocalNetwork.class);
        final Network network2 = Mockito.mock(Network.class);

        final PvlanSetupCommand lanSetup = PvlanSetupCommand.createVmSetup("add", URI.create("http://127.0.0.1"), "tag", "0:0:0:0:0:0");

        final String primaryPvlan = lanSetup.getPrimary();
        final String isolatedPvlan = lanSetup.getIsolated();
        final String op = lanSetup.getOp();
        final String vmMac = lanSetup.getVmMac();

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);
        try {
            when(citrixResourceBase.getNativeNetworkForTraffic(conn, TrafficType.Guest, "tag")).thenReturn(network);
            when(network.getNetwork()).thenReturn(network2);
            when(network2.getNameLabel(conn)).thenReturn(label);
        } catch (final XenAPIException e) {
            fail(e.getMessage());
        } catch (final XmlRpcException e) {
            fail(e.getMessage());
        }

        when(citrixResourceBase.callHostPlugin(conn, "ovs-pvlan", "setup-pvlan-vm", "op", op, "nw-label", label, "primary-pvlan", primaryPvlan, "isolated-pvlan",
                isolatedPvlan, "vm-mac", vmMac)).thenReturn("true");

        final Answer answer = wrapper.execute(lanSetup, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testPvlanSetupCommandVmFailure() {
        final String label = "net";

        final Connection conn = Mockito.mock(Connection.class);
        final XsLocalNetwork network = Mockito.mock(XsLocalNetwork.class);
        final Network network2 = Mockito.mock(Network.class);

        final PvlanSetupCommand lanSetup = PvlanSetupCommand.createVmSetup("add", URI.create("http://127.0.0.1"), "tag", "0:0:0:0:0:0");

        final String primaryPvlan = lanSetup.getPrimary();
        final String isolatedPvlan = lanSetup.getIsolated();
        final String op = lanSetup.getOp();
        final String vmMac = lanSetup.getVmMac();

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);
        try {
            when(citrixResourceBase.getNativeNetworkForTraffic(conn, TrafficType.Guest, "tag")).thenReturn(network);
            when(network.getNetwork()).thenReturn(network2);
            when(network2.getNameLabel(conn)).thenReturn(label);
        } catch (final XenAPIException e) {
            fail(e.getMessage());
        } catch (final XmlRpcException e) {
            fail(e.getMessage());
        }

        when(citrixResourceBase.callHostPlugin(conn, "ovs-pvlan", "setup-pvlan-vm", "op", op, "nw-label", label, "primary-pvlan", primaryPvlan, "isolated-pvlan",
                isolatedPvlan, "vm-mac", vmMac)).thenReturn("false");

        final Answer answer = wrapper.execute(lanSetup, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testPerformanceMonitorCommandSuccess() {
        final Connection conn = Mockito.mock(Connection.class);

        final PerformanceMonitorCommand performanceMonitor = new PerformanceMonitorCommand(new Hashtable<>(), 200);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);
        when(citrixResourceBase.getPerfMon(conn, performanceMonitor.getParams(), performanceMonitor.getWait())).thenReturn("performance");

        final Answer answer = wrapper.execute(performanceMonitor, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testPerformanceMonitorCommandFailure() {
        final Connection conn = Mockito.mock(Connection.class);

        final PerformanceMonitorCommand performanceMonitor = new PerformanceMonitorCommand(new Hashtable<>(), 200);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);
        when(citrixResourceBase.getPerfMon(conn, performanceMonitor.getParams(), performanceMonitor.getWait())).thenReturn(null);

        final Answer answer = wrapper.execute(performanceMonitor, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testStorageSubSystemCommand() {
        final DiskTO disk = Mockito.mock(DiskTO.class);
        final String vmName = "Test";
        final AttachCommand command = new AttachCommand(disk, vmName);

        final StorageSubsystemCommandHandler handler = Mockito.mock(StorageSubsystemCommandHandler.class);
        when(citrixResourceBase.getStorageHandler()).thenReturn(handler);

        when(handler.handleStorageCommands(command)).thenReturn(new AttachAnswer(disk));

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(command, citrixResourceBase);
        assertTrue(answer.getResult());
    }

    @Test
    public void testGetVmIpAddressCommand() throws XenAPIException, XmlRpcException {

        final Connection conn = Mockito.mock(Connection.class);
        final VM vm = Mockito.mock(VM.class);
        final VMGuestMetrics mtr = Mockito.mock(VMGuestMetrics.class);
        final VMGuestMetrics.Record rec = Mockito.mock(VMGuestMetrics.Record.class);

        final Map<String, String> vmIpsMap = new HashMap<>();
        vmIpsMap.put("Test", "127.0.0.1");
        rec.networks = vmIpsMap;

        final GetVmIpAddressCommand getVmIpAddrCmd = new GetVmIpAddressCommand("Test", "127.0.0.1/24", false);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(citrixResourceBase.getConnection()).thenReturn(conn);
        when(citrixResourceBase.getVM(conn, getVmIpAddrCmd.getVmName())).thenReturn(vm);
        when(vm.getGuestMetrics(conn)).thenReturn(mtr);
        when(mtr.getRecord(conn)).thenReturn(rec);

        final Answer answer = wrapper.execute(getVmIpAddrCmd, citrixResourceBase);

        verify(citrixResourceBase, times(1)).getConnection();

        assertTrue(answer.getResult());
    }
}

class NotAValidCommand extends Command {

    @Override
    public boolean executeInSequence() {
        return false;
    }
}
