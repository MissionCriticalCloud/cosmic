package com.cloud.api.command.test;

import com.cloud.api.ResponseGenerator;
import com.cloud.api.command.user.vm.AddIpToVmNicCmd;
import com.cloud.api.command.user.vm.RemoveIpFromVmNicCmd;
import com.cloud.api.response.NicSecondaryIpResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientAddressCapacityException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.network.NetworkService;
import com.cloud.vm.NicSecondaryIp;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class AddIpToVmNicTest extends TestCase {

    private AddIpToVmNicCmd addIpToVmNicCmd;
    private RemoveIpFromVmNicCmd removeIpFromVmNicCmd;
    private ResponseGenerator responseGenerator;
    private SuccessResponse successResponseGenerator;

    @Override
    @Before
    public void setUp() {

    }

    @Test
    public void testCreateSuccess() throws ResourceAllocationException, ResourceUnavailableException, ConcurrentOperationException, InsufficientCapacityException {

        final NetworkService networkService = Mockito.mock(NetworkService.class);
        final AddIpToVmNicCmd ipTonicCmd = Mockito.mock(AddIpToVmNicCmd.class);
        final NicSecondaryIp secIp = Mockito.mock(NicSecondaryIp.class);

        Mockito.when(
                networkService.allocateSecondaryGuestIP(Matchers.anyLong(), Matchers.anyString()))
               .thenReturn(secIp);

        ipTonicCmd._networkService = networkService;
        responseGenerator = Mockito.mock(ResponseGenerator.class);

        final NicSecondaryIpResponse ipres = Mockito.mock(NicSecondaryIpResponse.class);
        Mockito.when(responseGenerator.createSecondaryIPToNicResponse(secIp)).thenReturn(ipres);

        ipTonicCmd._responseGenerator = responseGenerator;
        ipTonicCmd.execute();
    }

    @Test
    public void testCreateFailure() throws ResourceAllocationException, ResourceUnavailableException, ConcurrentOperationException, InsufficientCapacityException {

        final NetworkService networkService = Mockito.mock(NetworkService.class);
        final AddIpToVmNicCmd ipTonicCmd = Mockito.mock(AddIpToVmNicCmd.class);

        Mockito.when(
                networkService.allocateSecondaryGuestIP(Matchers.anyLong(), Matchers.anyString()))
               .thenReturn(null);

        ipTonicCmd._networkService = networkService;

        try {
            ipTonicCmd.execute();
        } catch (final InsufficientAddressCapacityException e) {
            throw new InvalidParameterValueException("Allocating guest ip for nic failed");
        }
    }

    @Test
    public void testRemoveIpFromVmNicSuccess() throws ResourceAllocationException, ResourceUnavailableException, ConcurrentOperationException,
            InsufficientCapacityException {

        final NetworkService networkService = Mockito.mock(NetworkService.class);
        final RemoveIpFromVmNicCmd removeIpFromNic = Mockito.mock(RemoveIpFromVmNicCmd.class);

        Mockito.when(networkService.releaseSecondaryIpFromNic(Matchers.anyInt())).thenReturn(true);

        removeIpFromNic._networkService = networkService;
        removeIpFromNic.execute();
    }

    @Test
    public void testRemoveIpFromVmNicFailure() throws InsufficientAddressCapacityException {
        final NetworkService networkService = Mockito.mock(NetworkService.class);
        final RemoveIpFromVmNicCmd removeIpFromNic = Mockito.mock(RemoveIpFromVmNicCmd.class);

        Mockito.when(networkService.releaseSecondaryIpFromNic(Matchers.anyInt())).thenReturn(false);

        removeIpFromNic._networkService = networkService;
        successResponseGenerator = Mockito.mock(SuccessResponse.class);

        try {
            removeIpFromNic.execute();
        } catch (final InvalidParameterValueException exception) {
            Assert.assertEquals("Failed to remove secondary  ip address for the nic", exception.getLocalizedMessage());
        }
    }
}
