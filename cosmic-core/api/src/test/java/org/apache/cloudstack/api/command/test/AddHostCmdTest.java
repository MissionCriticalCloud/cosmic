package org.apache.cloudstack.api.command.test;

import com.cloud.exception.DiscoveryException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.host.Host;
import com.cloud.resource.ResourceService;
import org.apache.cloudstack.api.ResponseGenerator;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.admin.host.AddHostCmd;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.ListResponse;

import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class AddHostCmdTest extends TestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private AddHostCmd addHostCmd;
    private ResourceService resourceService;
    private ResponseGenerator responseGenerator;

    @Override
    @Before
    public void setUp() {
        resourceService = Mockito.mock(ResourceService.class);
        responseGenerator = Mockito.mock(ResponseGenerator.class);
        addHostCmd = new AddHostCmd() {
        };
    }

    @Test
    public void testExecuteForEmptyResult() {
        addHostCmd._resourceService = resourceService;

        try {
            addHostCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to add host", exception.getDescription());
        }
    }

    @Test
    public void testExecuteForNullResult() {

        final ResourceService resourceService = Mockito.mock(ResourceService.class);
        addHostCmd._resourceService = resourceService;

        try {
            Mockito.when(resourceService.discoverHosts(addHostCmd)).thenReturn(null);
        } catch (final InvalidParameterValueException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final DiscoveryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            addHostCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to add host", exception.getDescription());
        }
    }

    /*
     * @Test public void testExecuteForResult() throws Exception {
     *
     * addHostCmd._resourceService = resourceService;
     * addHostCmd._responseGenerator = responseGenerator; MockHost mockInstance
     * = new MockHost(); MockHost[] mockArray = new MockHost[]{mockInstance};
     * HostResponse responseHost = new HostResponse();
     * responseHost.setName("Test");
     * Mockito.when(resourceService.discoverHosts(addHostCmd
     * )).thenReturn(Arrays.asList(mockArray));
     * Mockito.when(responseGenerator.createHostResponse
     * (mockInstance)).thenReturn(responseHost); addHostCmd.execute();
     * Mockito.verify(responseGenerator).createHostResponse(mockInstance);
     * ListResponse<HostResponse> actualResponse =
     * ((ListResponse<HostResponse>)addHostCmd.getResponseObject());
     * Assert.assertEquals(responseHost, actualResponse.getResponses().get(0));
     * Assert.assertEquals("addhostresponse", actualResponse.getResponseName());
     * }
     */
    @Test
    public void testExecuteForResult() throws Exception {

        addHostCmd._resourceService = resourceService;
        addHostCmd._responseGenerator = responseGenerator;
        final Host host = Mockito.mock(Host.class);
        final Host[] mockArray = new Host[]{host};

        final HostResponse responseHost = new HostResponse();
        responseHost.setName("Test");
        Mockito.doReturn(Arrays.asList(mockArray)).when(resourceService).discoverHosts(addHostCmd);
        Mockito.when(responseGenerator.createHostResponse(host)).thenReturn(responseHost);
        addHostCmd.execute();
        Mockito.verify(responseGenerator).createHostResponse(host);
        final
        ListResponse<HostResponse> actualResponse = ((ListResponse<HostResponse>) addHostCmd.getResponseObject());
        Assert.assertEquals(responseHost, actualResponse.getResponses().get(0));
        Assert.assertEquals("addhostresponse", actualResponse.getResponseName());
    }

    @Test
    public void testExecuteForDiscoveryException() {

        addHostCmd._resourceService = resourceService;

        try {
            Mockito.when(resourceService.discoverHosts(addHostCmd)).thenThrow(DiscoveryException.class);
        } catch (final InvalidParameterValueException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final DiscoveryException e) {
            e.printStackTrace();
        }

        try {
            addHostCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertNull(exception.getDescription());
        }
    }
}
