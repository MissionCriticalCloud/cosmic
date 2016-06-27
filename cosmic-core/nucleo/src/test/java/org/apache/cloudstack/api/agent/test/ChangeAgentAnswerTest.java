//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.api.ChangeAgentAnswer;
import com.cloud.agent.api.ChangeAgentCommand;
import com.cloud.host.Status.Event;

import org.junit.Test;

public class ChangeAgentAnswerTest {
    ChangeAgentCommand cac = new ChangeAgentCommand(123456789L, Event.AgentConnected);
    ChangeAgentAnswer caa = new ChangeAgentAnswer(cac, true);

    @Test
    public void testGetResult() {
        final boolean b = caa.getResult();
        assertTrue(b);
    }

    @Test
    public void testExecuteInSequence() {
        final boolean b = caa.executeInSequence();
        assertFalse(b);
    }
}
