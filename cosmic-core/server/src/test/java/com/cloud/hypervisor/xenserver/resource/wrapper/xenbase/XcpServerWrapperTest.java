package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloud.hypervisor.xenserver.resource.XcpServerResource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.NetworkUsageCommand;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;

import com.xensource.xenapi.Connection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class XcpServerWrapperTest {

    @Mock
    protected XcpServerResource XcpServerResource;

    @Test
    public void testNetworkUsageCommandCreate() {
        final Connection conn = Mockito.mock(Connection.class);

        final String privateIP = "192.168.0.10";
        final String domRName = "dom";
        final String option = "create";
        final boolean forVpc = true;

        final NetworkUsageCommand usageCommand = new NetworkUsageCommand(privateIP, domRName, option, forVpc);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(XcpServerResource.getConnection()).thenReturn(conn);
        when(XcpServerResource.networkUsage(conn, usageCommand.getPrivateIP(), "create", null)).thenReturn("success");

        final Answer answer = wrapper.execute(usageCommand, XcpServerResource);

        verify(XcpServerResource, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testNetworkUsageCommandGet() {
        final Connection conn = Mockito.mock(Connection.class);

        final String privateIP = "192.168.0.10";
        final String domRName = "dom";
        final boolean forVpc = true;
        final String gatewayIp = "172.16.0.10";

        final NetworkUsageCommand usageCommand = new NetworkUsageCommand(privateIP, domRName, forVpc, gatewayIp);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(XcpServerResource.getConnection()).thenReturn(conn);
        when(XcpServerResource.getNetworkStats(conn, usageCommand.getPrivateIP())).thenReturn(new long[]{1l, 1l});

        final Answer answer = wrapper.execute(usageCommand, XcpServerResource);

        verify(XcpServerResource, times(1)).getConnection();

        assertTrue(answer.getResult());
    }

    @Test
    public void testNetworkUsageCommandExceptiopn() {
        final Connection conn = Mockito.mock(Connection.class);

        final String privateIP = "192.168.0.10";
        final String domRName = "dom";
        final String option = null;
        final boolean forVpc = true;

        final NetworkUsageCommand usageCommand = new NetworkUsageCommand(privateIP, domRName, option, forVpc);

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(XcpServerResource.getConnection()).thenReturn(conn);
        when(XcpServerResource.networkUsage(conn, usageCommand.getPrivateIP(), "create", null)).thenThrow(new CloudRuntimeException("FAILED"));

        final Answer answer = wrapper.execute(usageCommand, XcpServerResource);

        verify(XcpServerResource, times(1)).getConnection();

        assertFalse(answer.getResult());
    }
}
