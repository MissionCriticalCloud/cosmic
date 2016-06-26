package org.apache.cloudstack.api.command.test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.resource.ResourceService;
import com.cloud.server.ManagementService;
import org.apache.cloudstack.api.ResponseGenerator;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.admin.host.UpdateHostPasswordCmd;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UpdateHostPasswordCmdTest extends TestCase {

    private UpdateHostPasswordCmd updateHostPasswordCmd;
    private ManagementService managementServer;
    private ResourceService resourceService;
    private ResponseGenerator responseGenerator;

    @Override
    @Before
    public void setUp() {
        responseGenerator = Mockito.mock(ResponseGenerator.class);
        managementServer = Mockito.mock(ManagementService.class);
        resourceService = Mockito.mock(ResourceService.class);
        updateHostPasswordCmd = new UpdateHostPasswordCmd();
    }

    @Test
    public void testExecuteForNullResult() {

        updateHostPasswordCmd._mgr = managementServer;
        updateHostPasswordCmd._resourceService = resourceService;

        try {
            Mockito.when(managementServer.updateHostPassword(updateHostPasswordCmd)).thenReturn(false);
        } catch (final InvalidParameterValueException e) {
            fail(e.getMessage());
        } catch (final IllegalArgumentException e) {
            fail(e.getMessage());
        }

        try {
            updateHostPasswordCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to update config", exception.getDescription());
        }

        assertFalse("The attribute updatePasswdOnHost should be false, but it isn't.", updateHostPasswordCmd.getUpdatePasswdOnHost());
        verify(managementServer, times(1)).updateHostPassword(updateHostPasswordCmd);
    }

    @Test
    public void testCreateSuccess() {

        updateHostPasswordCmd._mgr = managementServer;
        updateHostPasswordCmd._resourceService = resourceService;
        updateHostPasswordCmd._responseGenerator = responseGenerator;

        try {
            Mockito.when(managementServer.updateHostPassword(updateHostPasswordCmd)).thenReturn(true);
        } catch (final Exception e) {
            fail("Received exception when success expected " + e.getMessage());
        }

        try {
            updateHostPasswordCmd.execute();
        } catch (final ServerApiException exception) {
            assertEquals("Failed to update config", exception.getDescription());
        }

        assertFalse("The attribute updatePasswdOnHost should be false, but it isn't.", updateHostPasswordCmd.getUpdatePasswdOnHost());
        verify(managementServer, times(1)).updateHostPassword(updateHostPasswordCmd);
    }
}
