package com.cloud.agent.manager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.cloud.utils.nio.Link;

import org.junit.Test;

public class ConnectedAgentAttacheTest {

    @Test
    public void testEquals() throws Exception {

        final Link link = mock(Link.class);

        final ConnectedAgentAttache agentAttache1 = new ConnectedAgentAttache(null, 0, null, link, false);
        final ConnectedAgentAttache agentAttache2 = new ConnectedAgentAttache(null, 0, null, link, false);

        assertTrue(agentAttache1.equals(agentAttache2));
    }

    @Test
    public void testEqualsFalseNull() throws Exception {

        final Link link = mock(Link.class);

        final ConnectedAgentAttache agentAttache1 = new ConnectedAgentAttache(null, 0, null, link, false);

        assertFalse(agentAttache1.equals(null));
    }

    @Test
    public void testEqualsFalseDiffLink() throws Exception {

        final Link link1 = mock(Link.class);
        final Link link2 = mock(Link.class);

        final ConnectedAgentAttache agentAttache1 = new ConnectedAgentAttache(null, 0, null, link1, false);
        final ConnectedAgentAttache agentAttache2 = new ConnectedAgentAttache(null, 0, null, link2, false);

        assertFalse(agentAttache1.equals(agentAttache2));
    }

    @Test
    public void testEqualsFalseDiffId() throws Exception {

        final Link link1 = mock(Link.class);

        final ConnectedAgentAttache agentAttache1 = new ConnectedAgentAttache(null, 1, null, link1, false);
        final ConnectedAgentAttache agentAttache2 = new ConnectedAgentAttache(null, 2, null, link1, false);

        assertFalse(agentAttache1.equals(agentAttache2));
    }

    @Test
    public void testEqualsFalseDiffClass() throws Exception {

        final Link link1 = mock(Link.class);

        final ConnectedAgentAttache agentAttache1 = new ConnectedAgentAttache(null, 1, null, link1, false);

        assertFalse(agentAttache1.equals("abc"));
    }
}
