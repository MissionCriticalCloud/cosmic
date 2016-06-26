// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cloudstack.api.command.test;

import com.cloud.uservm.UserVm;
import com.cloud.vm.UserVmService;
import org.apache.cloudstack.api.ResponseGenerator;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.vm.ScaleVMCmd;
import org.apache.cloudstack.api.response.UserVmResponse;

import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class ScaleVMCmdTest extends TestCase {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ScaleVMCmd scaleVMCmd;
    private ResponseGenerator responseGenerator;

    @Override
    @Before
    public void setUp() {

        scaleVMCmd = new ScaleVMCmd() {
            @Override
            public String getCommandName() {
                return "scalevirtualmachineresponse";
            }

            @Override
            public Long getId() {
                return 2L;
            }
        };
    }

    @Test
    public void testCreateSuccess() {

        final UserVmService userVmService = Mockito.mock(UserVmService.class);
        final UserVm userVm = Mockito.mock(UserVm.class);

        try {
            Mockito.when(userVmService.upgradeVirtualMachine(scaleVMCmd)).thenReturn(userVm);
        } catch (final Exception e) {
            Assert.fail("Received exception when success expected " + e.getMessage());
        }

        final ResponseGenerator responseGenerator = Mockito.mock(ResponseGenerator.class);
        scaleVMCmd._responseGenerator = responseGenerator;

        final UserVmResponse userVmResponse = Mockito.mock(UserVmResponse.class);
        //List<UserVmResponse> list = Mockito.mock(UserVmResponse.class);
        //list.add(userVmResponse);
        //LinkedList<UserVmResponse> mockedList = Mockito.mock(LinkedList.class);
        //Mockito.when(mockedList.get(0)).thenReturn(userVmResponse);

        final List<UserVmResponse> list = new LinkedList<>();
        list.add(userVmResponse);

        Mockito.when(responseGenerator.createUserVmResponse(ResponseView.Restricted, "virtualmachine", userVm)).thenReturn(
                list);

        scaleVMCmd._userVmService = userVmService;

        scaleVMCmd.execute();
    }

    @Test
    public void testCreateFailure() {

        final UserVmService userVmService = Mockito.mock(UserVmService.class);

        try {
            Mockito.when(userVmService.upgradeVirtualMachine(scaleVMCmd)).thenReturn(null);
        } catch (final Exception e) {
            Assert.fail("Received exception when success expected " + e.getMessage());
        }

        scaleVMCmd._userVmService = userVmService;

        try {
            scaleVMCmd.execute();
        } catch (final ServerApiException exception) {
            Assert.assertEquals("Failed to scale vm", exception.getDescription());
        }
    }
}
