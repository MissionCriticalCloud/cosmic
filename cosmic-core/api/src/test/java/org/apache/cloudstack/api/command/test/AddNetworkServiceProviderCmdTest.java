package org.apache.cloudstack.api.command.test;

import com.cloud.exception.ResourceAllocationException;
import com.cloud.network.NetworkService;
import com.cloud.network.PhysicalNetworkServiceProvider;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.admin.network.AddNetworkServiceProviderCmd;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class AddNetworkServiceProviderCmdTest extends TestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private AddNetworkServiceProviderCmd addNetworkServiceProviderCmd;

    @Override
    @Before
    public void setUp() {
        addNetworkServiceProviderCmd = new AddNetworkServiceProviderCmd() {

            @Override
            public Long getPhysicalNetworkId() {
                return 2L;
            }

            @Override
            public String getProviderName() {
                return "ProviderName";
            }

            @Override
            public Long getDestinationPhysicalNetworkId() {
                return 2L;
            }

            @Override
            public List<String> getEnabledServices() {
                final List<String> lOfEnabledServices = new ArrayList<>();
                lOfEnabledServices.add("Enabled Services");
                return lOfEnabledServices;
            }

            @Override
            public Long getEntityId() {
                return 2L;
            }
        };
    }

    @Test
    public void testCreateProviderToPhysicalNetworkSuccess() {

        final NetworkService networkService = Mockito.mock(NetworkService.class);
        addNetworkServiceProviderCmd._networkService = networkService;

        final PhysicalNetworkServiceProvider physicalNetworkServiceProvider = Mockito.mock(PhysicalNetworkServiceProvider.class);
        Mockito.when(networkService.addProviderToPhysicalNetwork(Matchers.anyLong(), Matchers.anyString(), Matchers.anyLong(), Matchers.anyList())).thenReturn(
                physicalNetworkServiceProvider);

        try {
            addNetworkServiceProviderCmd.create();
        } catch (final ResourceAllocationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateProviderToPhysicalNetworkFailure() throws ResourceAllocationException {

        final NetworkService networkService = Mockito.mock(NetworkService.class);
        addNetworkServiceProviderCmd._networkService = networkService;

        Mockito.when(networkService.addProviderToPhysicalNetwork(Matchers.anyLong(), Matchers.anyString(), Matchers.anyLong(), Matchers.anyList())).thenReturn(null);

        try {
            addNetworkServiceProviderCmd.create();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to add service provider entity to physical network", exception.getDescription());
        }
    }
}
