//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertFalse;

import com.cloud.agent.api.AgentControlCommand;

import org.junit.Test;

public class AgentControlCommandTest {
    AgentControlCommand acc = new AgentControlCommand();

    @Test
    public void testExecuteInSequence() {
        final boolean b = acc.executeInSequence();
        assertFalse(b);
    }
}
