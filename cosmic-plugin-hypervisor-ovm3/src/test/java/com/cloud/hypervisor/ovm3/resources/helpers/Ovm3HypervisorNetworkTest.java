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

package com.cloud.hypervisor.ovm3.resources.helpers;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.CheckNetworkCommand;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.hypervisor.ovm3.objects.CloudStackPluginTest;
import com.cloud.hypervisor.ovm3.objects.ConnectionTest;
import com.cloud.hypervisor.ovm3.objects.NetworkTest;
import com.cloud.hypervisor.ovm3.objects.XenTest;
import com.cloud.hypervisor.ovm3.objects.XmlTestResultTest;
import com.cloud.hypervisor.ovm3.resources.Ovm3HypervisorResource;
import com.cloud.hypervisor.ovm3.resources.Ovm3HypervisorResourceTest;
import com.cloud.hypervisor.ovm3.support.Ovm3SupportTest;
import com.cloud.network.PhysicalNetworkSetupInfo;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class Ovm3HypervisorNetworkTest {
    ConnectionTest con = new ConnectionTest();
    Ovm3ConfigurationTest configTest = new Ovm3ConfigurationTest();
    Ovm3SupportTest support = new Ovm3SupportTest();
    Ovm3HypervisorResource hypervisor = new Ovm3HypervisorResource();
    Ovm3HypervisorResourceTest hyperTest = new Ovm3HypervisorResourceTest();
    CloudStackPluginTest csp = new CloudStackPluginTest();
    XenTest xen = new XenTest();
    NetworkTest network = new NetworkTest();
    XmlTestResultTest results = new XmlTestResultTest();

    @Test
    public void CheckNetworkCommandTest() throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        final List<PhysicalNetworkSetupInfo> setups = new ArrayList<>();
        final PhysicalNetworkSetupInfo networkInfo = new PhysicalNetworkSetupInfo();
        setups.add(networkInfo);
        final CheckNetworkCommand cmd = new CheckNetworkCommand(setups);
        final Answer ra = hypervisor.executeRequest(cmd);
        results.basicBooleanTest(ra.getResult());
    }

    @Test
    public void CheckNetworkCommandGuestFailTest() throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        final List<PhysicalNetworkSetupInfo> setups = new ArrayList<>();
        final PhysicalNetworkSetupInfo networkInfo = new PhysicalNetworkSetupInfo();
        networkInfo.setGuestNetworkName(network.getInterface() + "." + 3000);
        setups.add(networkInfo);
        final CheckNetworkCommand cmd = new CheckNetworkCommand(setups);
        final Answer ra = hypervisor.executeRequest(cmd);
        results.basicBooleanTest(ra.getResult(), false);
    }

    @Test
    public void CheckNetworkCommandPublicFailTest() throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        final List<PhysicalNetworkSetupInfo> setups = new ArrayList<>();
        final PhysicalNetworkSetupInfo networkInfo = new PhysicalNetworkSetupInfo();
        networkInfo.setPublicNetworkName(network.getInterface() + "." + 3000);
        setups.add(networkInfo);
        final CheckNetworkCommand cmd = new CheckNetworkCommand(setups);
        final Answer ra = hypervisor.executeRequest(cmd);
        results.basicBooleanTest(ra.getResult(), false);
    }

    @Test
    public void CheckNetworkCommandPrivateFailTest() throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        final List<PhysicalNetworkSetupInfo> setups = new ArrayList<>();
        final PhysicalNetworkSetupInfo networkInfo = new PhysicalNetworkSetupInfo();
        networkInfo.setPrivateNetworkName(network.getInterface() + "." + 3000);
        setups.add(networkInfo);
        final CheckNetworkCommand cmd = new CheckNetworkCommand(setups);
        final Answer ra = hypervisor.executeRequest(cmd);
        results.basicBooleanTest(ra.getResult(), false);
    }

    @Test
    public void CheckNetworkCommandStorageFalseTest() throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        final List<PhysicalNetworkSetupInfo> setups = new ArrayList<>();
        final PhysicalNetworkSetupInfo networkInfo = new PhysicalNetworkSetupInfo();
        networkInfo.setStorageNetworkName(network.getInterface() + "." + 3000);
        setups.add(networkInfo);
        final CheckNetworkCommand cmd = new CheckNetworkCommand(setups);
        final Answer ra = hypervisor.executeRequest(cmd);
        results.basicBooleanTest(ra.getResult());
    }

    @Test
    public void PingTestCommandWeDontPingRouterTest() throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        final PingTestCommand cmd = new PingTestCommand(csp.getDom0Ip(), csp.getDomrIp());
        final Answer ra = hypervisor.executeRequest(cmd);
        results.basicBooleanTest(ra.getResult(), false);
    }

    @Test
    public void PingTestCommandComputeTest() throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        final PingTestCommand cmd = new PingTestCommand(csp.getDom0Ip());
        final Answer ra = hypervisor.executeRequest(cmd);
        results.basicBooleanTest(ra.getResult());
    }

    @Test
    public void PingTestCommandComputeFalseTest() throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        support.getConnection().setMethodResponse("ping", results.simpleResponseWrap("boolean", "0"));
        final PingTestCommand cmd = new PingTestCommand(csp.getDom0Ip());
        final Answer ra = hypervisor.executeRequest(cmd);
        results.basicBooleanTest(ra.getResult(), false);
    }
}
