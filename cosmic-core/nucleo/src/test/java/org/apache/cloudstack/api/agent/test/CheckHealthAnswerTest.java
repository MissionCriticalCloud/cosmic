//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.api.CheckHealthAnswer;
import com.cloud.agent.api.CheckHealthCommand;

import org.junit.Test;

public class CheckHealthAnswerTest {
    CheckHealthCommand chc = new CheckHealthCommand();
    CheckHealthAnswer cha = new CheckHealthAnswer(chc, true);

    @Test
    public void testGetResult() {
        final boolean r = cha.getResult();
        assertTrue(r);
    }

    @Test
    public void testGetDetails() {
        final String d = cha.getDetails();
        final boolean r = cha.getResult();
        assertTrue(d.equals("resource is " + (r ? "alive" : "not alive")));
    }

    @Test
    public void testExecuteInSequence() {
        final boolean b = cha.executeInSequence();
        assertFalse(b);
    }
}
