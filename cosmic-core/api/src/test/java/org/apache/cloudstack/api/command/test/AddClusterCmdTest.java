package org.apache.cloudstack.api.command.test;

import com.cloud.exception.DiscoveryException;
import com.cloud.exception.ResourceInUseException;
import com.cloud.org.Cluster;
import com.cloud.resource.ResourceService;
import org.apache.cloudstack.api.ResponseGenerator;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.admin.cluster.AddClusterCmd;

import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class AddClusterCmdTest extends TestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private AddClusterCmd addClusterCmd;
    private ResourceService resourceService;
    private ResponseGenerator responseGenerator;

    @Override
    @Before
    public void setUp() {
        /*
         * resourceService = Mockito.mock(ResourceService.class);
         * responseGenerator = Mockito.mock(ResponseGenerator.class);
         */
        addClusterCmd = new AddClusterCmd() {
        };
    }

    @Test
    public void testExecuteForNullResult() {

        final ResourceService resourceService = Mockito.mock(ResourceService.class);

        try {
            Mockito.when(resourceService.discoverCluster(addClusterCmd)).thenReturn(null);
        } catch (final ResourceInUseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final DiscoveryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        addClusterCmd._resourceService = resourceService;

        try {
            addClusterCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to add cluster", exception.getDescription());
        }
    }

    @Test
    public void testExecuteForEmptyResult() {

        final ResourceService resourceService = Mockito.mock(ResourceService.class);
        addClusterCmd._resourceService = resourceService;

        try {
            addClusterCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to add cluster", exception.getDescription());
        }
    }

    @Test
    public void testExecuteForResult() throws Exception {

        resourceService = Mockito.mock(ResourceService.class);
        responseGenerator = Mockito.mock(ResponseGenerator.class);

        addClusterCmd._resourceService = resourceService;
        addClusterCmd._responseGenerator = responseGenerator;

        final Cluster cluster = Mockito.mock(Cluster.class);
        final Cluster[] clusterArray = new Cluster[]{cluster};

        Mockito.doReturn(Arrays.asList(clusterArray)).when(resourceService).discoverCluster(addClusterCmd);

        addClusterCmd.execute();
    }
}
