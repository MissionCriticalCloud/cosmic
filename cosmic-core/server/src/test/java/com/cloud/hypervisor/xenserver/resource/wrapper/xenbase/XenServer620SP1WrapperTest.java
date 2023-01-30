package com.cloud.hypervisor.xenserver.resource.wrapper.xenbase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloud.hypervisor.xenserver.resource.XenServer620SP1Resource;
import com.cloud.legacymodel.communication.answer.Answer;
import com.cloud.legacymodel.communication.command.GetGPUStatsCommand;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;

import java.util.HashMap;

import com.xensource.xenapi.Connection;
import com.xensource.xenapi.Types.XenAPIException;
import org.apache.xmlrpc.XmlRpcException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class XenServer620SP1WrapperTest {

    @Mock
    private XenServer620SP1Resource xenServer620SP1Resource;

    @Test
    public void testGetGPUStatsCommand() {
        final String guuid = "246a5b75-05ed-4bbc-a171-2d1fe94a1b0e";

        final Connection conn = Mockito.mock(Connection.class);

        final GetGPUStatsCommand gpuStats = new GetGPUStatsCommand(guuid, "xen");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(xenServer620SP1Resource.getConnection()).thenReturn(conn);
        try {
            when(xenServer620SP1Resource.getGPUGroupDetails(conn)).thenReturn(new HashMap<>());
        } catch (final XenAPIException e) {
            fail(e.getMessage());
        } catch (final XmlRpcException e) {
            fail(e.getMessage());
        }

        final Answer answer = wrapper.execute(gpuStats, xenServer620SP1Resource);
        verify(xenServer620SP1Resource, times(1)).getConnection();
        try {
            verify(xenServer620SP1Resource, times(1)).getGPUGroupDetails(conn);
        } catch (final XenAPIException e) {
            fail(e.getMessage());
        } catch (final XmlRpcException e) {
            fail(e.getMessage());
        }

        assertTrue(answer.getResult());
    }

    @Test
    public void testGetGPUStatsCommandFailure() {
        final String guuid = "246a5b75-05ed-4bbc-a171-2d1fe94a1b0e";

        final Connection conn = Mockito.mock(Connection.class);

        final GetGPUStatsCommand gpuStats = new GetGPUStatsCommand(guuid, "xen");

        final CitrixRequestWrapper wrapper = CitrixRequestWrapper.getInstance();
        assertNotNull(wrapper);

        when(xenServer620SP1Resource.getConnection()).thenReturn(conn);
        try {
            when(xenServer620SP1Resource.getGPUGroupDetails(conn)).thenThrow(new CloudRuntimeException("Failed!"));
        } catch (final XenAPIException e) {
            fail(e.getMessage());
        } catch (final XmlRpcException e) {
            fail(e.getMessage());
        }

        final Answer answer = wrapper.execute(gpuStats, xenServer620SP1Resource);
        verify(xenServer620SP1Resource, times(1)).getConnection();
        try {
            verify(xenServer620SP1Resource, times(1)).getGPUGroupDetails(conn);
        } catch (final XenAPIException e) {
            fail(e.getMessage());
        } catch (final XmlRpcException e) {
            fail(e.getMessage());
        }

        assertFalse(answer.getResult());
    }
}
