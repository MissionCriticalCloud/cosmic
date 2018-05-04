package com.cloud.api.agent.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cloud.legacymodel.communication.command.CheckHealthCommand;

import org.junit.Test;

public class CheckHealthCommandTest {
    CheckHealthCommand chc = new CheckHealthCommand();

    @Test
    public void testGetWait() {
        final int wait = chc.getWait();
        assertTrue(wait == 50);
    }

    @Test
    public void testExecuteInSequence() {
        final boolean b = chc.executeInSequence();
        assertFalse(b);
    }
}
