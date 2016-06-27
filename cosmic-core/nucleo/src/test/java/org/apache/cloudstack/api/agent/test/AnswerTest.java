//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.api.AgentControlCommand;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.UnsupportedAnswer;

import org.junit.Test;

public class AnswerTest {
    AgentControlCommand acc = new AgentControlCommand();
    Answer a = new Answer(acc, true, "details");

    @Test
    public void testExecuteInSequence() {
        final boolean b = a.executeInSequence();
        assertFalse(b);
    }

    @Test
    public void testGetResult() {
        final boolean b = a.getResult();
        assertTrue(b);
    }

    @Test
    public void testGetDetails() {
        final String d = a.getDetails();
        assertTrue(d.equals("details"));
    }

    @Test
    public void testCreateUnsupportedCommandAnswer() {
        UnsupportedAnswer usa = Answer.createUnsupportedCommandAnswer(acc);
        boolean b = usa.executeInSequence();
        assertFalse(b);

        b = usa.getResult();
        assertFalse(b);

        String d = usa.getDetails();
        assertTrue(d.contains("Unsupported command issued: " + acc.toString() + ".  Are you sure you got the right type of server?"));

        usa = Answer.createUnsupportedVersionAnswer(acc);
        b = usa.executeInSequence();
        assertFalse(b);

        b = usa.getResult();
        assertFalse(b);

        d = usa.getDetails();
        assertTrue(d.equals("Unsuppored Version."));
    }
}
