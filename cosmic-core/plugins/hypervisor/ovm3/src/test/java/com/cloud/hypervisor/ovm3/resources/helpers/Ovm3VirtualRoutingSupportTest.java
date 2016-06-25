package com.cloud.hypervisor.ovm3.resources.helpers;

import com.cloud.agent.api.Answer;
import com.cloud.agent.api.NetworkUsageCommand;
import com.cloud.agent.api.check.CheckSshCommand;
import com.cloud.hypervisor.ovm3.objects.CloudStackPluginTest;
import com.cloud.hypervisor.ovm3.objects.ConnectionTest;
import com.cloud.hypervisor.ovm3.objects.XmlTestResultTest;
import com.cloud.hypervisor.ovm3.resources.Ovm3HypervisorResource;
import com.cloud.hypervisor.ovm3.support.Ovm3SupportTest;

import javax.naming.ConfigurationException;

import org.junit.Test;

public class Ovm3VirtualRoutingSupportTest {
    ConnectionTest con = new ConnectionTest();
    Ovm3ConfigurationTest configTest = new Ovm3ConfigurationTest();
    Ovm3SupportTest support = new Ovm3SupportTest();
    Ovm3HypervisorResource hypervisor = new Ovm3HypervisorResource();
    CloudStackPluginTest csp = new CloudStackPluginTest();
    XmlTestResultTest results = new XmlTestResultTest();

    @Test
    public void NetworkUsageCommandTest() throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        final NetworkUsageCommand nuc = new NetworkUsageCommand(csp.getDomrIp(), "something", "", false);
        final Answer ra = hypervisor.executeRequest(nuc);
        results.basicBooleanTest(ra.getResult());
    }

    @Test
    public void NetworkUsageVpcCommandTest() throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        final NetworkUsageCommand nuc = new NetworkUsageCommand(csp.getDomrIp(), "something", "", true);
        final Answer ra = hypervisor.executeRequest(nuc);
        results.basicBooleanTest(ra.getResult());
    }

    @Test
    public void NetworkVpcGetCommandTest() throws ConfigurationException {
        NetworkVpcCommandTest("get");
    }

    public void NetworkVpcCommandTest(final String cmd) throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        final NetworkUsageCommand nuc = new NetworkUsageCommand(csp.getDomrIp(), "something", cmd, true);
        final Answer ra = hypervisor.executeRequest(nuc);
        results.basicBooleanTest(ra.getResult());
    }

    @Test
    public void NetworkVpcCreateCommandTest() throws ConfigurationException {
        NetworkVpcCommandTest("create");
    }

    @Test
    public void NetworkVpcResetCommandTest() throws ConfigurationException {
        NetworkVpcCommandTest("reset");
    }

    @Test
    public void NetworkVpcVpnCommandTest() throws ConfigurationException {
        NetworkVpcCommandTest("vpn");
    }

    @Test
    public void NetworkVpcRemoveCommandTest() throws ConfigurationException {
        NetworkVpcCommandTest("remove");
    }

    @Test
    public void CheckSshCommandTest() throws ConfigurationException {
        hypervisor = support.prepare(configTest.getParams());
        final CheckSshCommand ssh = new CheckSshCommand("name", csp.getDomrIp(), 8899);
        final Answer ra = hypervisor.executeRequest(ssh);
        results.basicBooleanTest(ra.getResult());
    }
}
