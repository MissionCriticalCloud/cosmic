package com.cloud.agent;

import javax.naming.ConfigurationException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class AgentShellTest {
    @Test
    public void parseCommand() throws ConfigurationException {
        final AgentShell shell = new AgentShell();
        final UUID anyUuid = UUID.randomUUID();
        shell.parseCommand(new String[]{"port=55555", "threads=4", "host=localhost", "pod=pod1", "guid=" + anyUuid, "zone=zone1"});
        Assert.assertEquals(55555, shell.getPort());
        Assert.assertEquals(4, shell.getWorkers());
        Assert.assertEquals("localhost", shell.getHost());
        Assert.assertEquals(anyUuid.toString(), shell.getGuid());
        Assert.assertEquals("pod1", shell.getPod());
        Assert.assertEquals("zone1", shell.getZone());
    }

    @Test
    public void loadProperties() throws ConfigurationException {
        final AgentShell shell = new AgentShell();
        shell.loadProperties();
        Assert.assertNotNull(shell.getProperties());
        Assert.assertFalse(shell.getProperties().entrySet().isEmpty());
    }
}
