//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.api.CancelCommand;

import org.junit.Test;

public class CancelCommandTest {
    CancelCommand cc = new CancelCommand(123456789L, "goodreason");

    @Test
    public void testGetSequence() {
        final Long s = cc.getSequence();
        assertTrue(123456789L == s);
    }

    @Test
    public void testGetReason() {
        final String r = cc.getReason();
        assertTrue(r.equals("goodreason"));
    }

    @Test
    public void testExecuteInSequence() {
        final boolean b = cc.executeInSequence();
        assertFalse(b);
    }
}
