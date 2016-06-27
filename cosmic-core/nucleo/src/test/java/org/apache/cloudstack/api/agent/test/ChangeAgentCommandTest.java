//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.api.ChangeAgentCommand;
import com.cloud.host.Status.Event;

import org.junit.Test;

public class ChangeAgentCommandTest {

    ChangeAgentCommand cac = new ChangeAgentCommand(123456789L, Event.AgentConnected);

    @Test
    public void testGetAgentId() {
        final Long aid = cac.getAgentId();
        assertTrue(123456789L == aid);
    }

    @Test
    public void testGetEvent() {
        final Event e = cac.getEvent();
        assertEquals(Event.AgentConnected, e);
    }

    @Test
    public void testExecuteInSequence() {
        final boolean b = cac.executeInSequence();
        assertFalse(b);
    }
}
