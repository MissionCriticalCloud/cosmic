package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloud.hypervisor.xenserver.resource.XenServer56Resource;
import com.cloud.hypervisor.xenserver.resource.XsHost;
import com.cloud.legacymodel.ExecutionResult;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.CheckOnHostCommand;
import com.cloud.legacymodel.communication.command.FenceCommand;
import com.cloud.legacymodel.communication.command.NetworkUsageCommand;
import com.cloud.legacymodel.communication.command.SetupCommand;
import com.cloud.legacymodel.dc.Host;
import com.cloud.legacymodel.dc.HostEnvironment;
import com.cloud.vm.VMInstanceVO;

import com.xensource.xenapi.Connection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class XenServer56WrapperTest {

    @Mock
    private XenServer56Resource xenServer56Resource;

    @Test
    public void testCheckOnHostCommand() {
        final Host host = Mockito.mock(Host.class);
        final CheckOnHostCommand onHostCommand = new CheckOnHostCommand(host);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final Answer answer = wrapper.execute(onHostCommand, this.xenServer56Resource);

        assertTrue(answer.getResult());
    }

    @Test
    public void testFenceCommand() {
        final VMInstanceVO vm = Mockito.mock(VMInstanceVO.class);
        final Host host = Mockito.mock(Host.class);

        final Connection conn = Mockito.mock(Connection.class);

        final FenceCommand fenceCommand = new FenceCommand(vm, host);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(this.xenServer56Resource.getConnection()).thenReturn(conn);

        final Answer answer = wrapper.execute(fenceCommand, this.xenServer56Resource);

        verify(this.xenServer56Resource, times(1)).getConnection();
        verify(this.xenServer56Resource, times(1)).checkHeartbeat(fenceCommand.getHostGuid());

        assertFalse(answer.getResult());
    }

    @Test
    public void testNetworkUsageCommandSuccess() {
        final Connection conn = Mockito.mock(Connection.class);

        final NetworkUsageCommand networkCommand = new NetworkUsageCommand("192.168.10.10", "domRName", false, "192.168.10.1");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(this.xenServer56Resource.getConnection()).thenReturn(conn);
        when(this.xenServer56Resource.getNetworkStats(conn, networkCommand.getPrivateIP())).thenReturn(new long[]{1, 1});

        final Answer answer = wrapper.execute(networkCommand, this.xenServer56Resource);

        verify(this.xenServer56Resource, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testNetworkUsageCommandFailure() {
        final Connection conn = Mockito.mock(Connection.class);

        final NetworkUsageCommand networkCommand = new NetworkUsageCommand("192.168.10.10", "domRName", false, "192.168.10.1");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(this.xenServer56Resource.getConnection()).thenReturn(conn);
        when(this.xenServer56Resource.getNetworkStats(conn, networkCommand.getPrivateIP())).thenReturn(new long[0]);

        final Answer answer = wrapper.execute(networkCommand, this.xenServer56Resource);

        verify(this.xenServer56Resource, times(1)).getConnection();

        assertFalse(answer.getResult());
    }

    @Test
    public void testNetworkUsageCommandCreateVpc() {
        final ExecutionResult executionResult = Mockito.mock(ExecutionResult.class);

        final NetworkUsageCommand networkCommand = new NetworkUsageCommand("192.168.10.10", "domRName", true, "192.168.10.1", "10.1.1.1/24");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final String args = " -l 192.168.10.1 -c -v 10.1.1.1/24";
        when(this.xenServer56Resource.executeInVR(networkCommand.getPrivateIP(), "vpc_netusage.sh", args)).thenReturn(executionResult);
        when(executionResult.isSuccess()).thenReturn(true);

        final Answer answer = wrapper.execute(networkCommand, this.xenServer56Resource);

        assertTrue(answer.getResult());
    }

    @Test
    public void testNetworkUsageCommandCreateVpcFailure() {
        final ExecutionResult executionResult = Mockito.mock(ExecutionResult.class);

        final NetworkUsageCommand networkCommand = new NetworkUsageCommand("192.168.10.10", "domRName", true, "192.168.10.1", "10.1.1.1/24");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        final String args = " -l 192.168.10.1 -c -v 10.1.1.1/24";
        when(this.xenServer56Resource.executeInVR(networkCommand.getPrivateIP(), "vpc_netusage.sh", args)).thenReturn(executionResult);
        when(executionResult.isSuccess()).thenReturn(false);

        final Answer answer = wrapper.execute(networkCommand, this.xenServer56Resource);

        assertFalse(answer.getResult());
    }

    @Test
    public void testSetupCommand() {
        final XsHost xsHost = Mockito.mock(XsHost.class);
        final HostEnvironment env = Mockito.mock(HostEnvironment.class);

        final SetupCommand setupCommand = new SetupCommand(env);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(this.xenServer56Resource.getHost()).thenReturn(xsHost);

        final Answer answer = wrapper.execute(setupCommand, this.xenServer56Resource);
        verify(this.xenServer56Resource, times(1)).getConnection();

        assertFalse(answer.getResult());
    }
}
