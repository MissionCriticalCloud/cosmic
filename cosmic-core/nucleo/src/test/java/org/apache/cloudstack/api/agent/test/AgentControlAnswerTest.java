//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertFalse;

import com.cloud.agent.api.AgentControlAnswer;
import com.cloud.agent.api.AgentControlCommand;

import org.junit.Test;

public class AgentControlAnswerTest {
    AgentControlCommand acc = new AgentControlCommand();
    AgentControlAnswer aca = new AgentControlAnswer(acc);

    @Test
    public void testExecuteInSequence() {
        final boolean b = acc.executeInSequence();
        assertFalse(b);
    }
}
