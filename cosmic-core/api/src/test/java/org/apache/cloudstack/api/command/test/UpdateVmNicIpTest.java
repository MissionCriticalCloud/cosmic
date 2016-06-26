package org.apache.cloudstack.api.command.test;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.uservm.UserVm;
import com.cloud.vm.UserVmService;
import org.apache.cloudstack.api.ResponseGenerator;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.vm.UpdateVmNicIpCmd;
import org.apache.cloudstack.api.response.UserVmResponse;

import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UpdateVmNicIpTest extends TestCase {

    private UpdateVmNicIpCmd updateVmNicIpCmd;
    private ResponseGenerator responseGenerator;

    @Override
    @Before
    public void setUp() {

    }

    @Test
    public void testSuccess() throws ResourceAllocationException, ResourceUnavailableException, ConcurrentOperationException, InsufficientCapacityException {

        final UserVmService userVmService = Mockito.mock(UserVmService.class);
        updateVmNicIpCmd = Mockito.mock(UpdateVmNicIpCmd.class);
        final UserVm userVm = Mockito.mock(UserVm.class);

        Mockito.when(userVmService.updateNicIpForVirtualMachine(Mockito.any(UpdateVmNicIpCmd.class))).thenReturn(userVm);

        updateVmNicIpCmd._userVmService = userVmService;
        responseGenerator = Mockito.mock(ResponseGenerator.class);

        final List<UserVmResponse> list = new LinkedList<>();
        final UserVmResponse userVmResponse = Mockito.mock(UserVmResponse.class);
        list.add(userVmResponse);
        Mockito.when(responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", userVm)).thenReturn(list);

        updateVmNicIpCmd._responseGenerator = responseGenerator;
        updateVmNicIpCmd.execute();
    }

    @Test
    public void testFailure() throws ResourceAllocationException, ResourceUnavailableException, ConcurrentOperationException, InsufficientCapacityException {
        final UserVmService userVmService = Mockito.mock(UserVmService.class);
        updateVmNicIpCmd = Mockito.mock(UpdateVmNicIpCmd.class);

        Mockito.when(userVmService.updateNicIpForVirtualMachine(Mockito.any(UpdateVmNicIpCmd.class))).thenReturn(null);

        updateVmNicIpCmd._userVmService = userVmService;

        updateVmNicIpCmd._responseGenerator = responseGenerator;
        try {
            updateVmNicIpCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to update ip address on vm NIC. Refer to server logs for details.", exception.getDescription());
        }
    }
}
