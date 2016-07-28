package com.cloud.agent.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import javax.naming.ConfigurationException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class AgentShellTest {
    @Test
    public void parseCommand() throws ConfigurationException {
        final AgentShell shell = new AgentShell();
        final UUID anyUuid = UUID.randomUUID();
        shell.parseCommand(new String[]{"port=55555", "workers=4", "host=localhost", "pod=pod1", "guid=" + anyUuid, "zone=zone1"});

        assertThat(shell.getPort(), is(55555));
        assertThat(shell.getWorkers(), is(4));
        assertThat(shell.getHosts(), containsInAnyOrder("localhost"));
        assertThat(shell.getGuid(), is(anyUuid.toString()));
        assertThat(shell.getPod(), is("pod1"));
        assertThat(shell.getZone(), is("zone1"));
    }

    @Test
    public void loadProperties() throws ConfigurationException {
        final AgentShell shell = new AgentShell();
        shell.loadProperties();
        Assert.assertNotNull(shell);
    }
}
