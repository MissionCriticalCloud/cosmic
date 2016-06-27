//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.api.AttachIsoCommand;

import org.junit.Test;

public class AttachIsoCommandTest {
    AttachIsoCommand aic = new AttachIsoCommand("vmname", "isopath", false);

    @Test
    public void testGetVmName() {
        final String vmName = aic.getVmName();
        assertTrue(vmName.equals("vmname"));
    }

    @Test
    public void testGetIsoPath() {
        final String isoPath = aic.getIsoPath();
        assertTrue(isoPath.equals("isopath"));
    }

    @Test
    public void testIsAttach() {
        final boolean b = aic.isAttach();
        assertFalse(b);
    }

    @Test
    public void testGetStoreUrl() {
        aic.setStoreUrl("http://incubator.apache.org/cloudstack/");
        final String url = aic.getStoreUrl();
        assertTrue(url.equals("http://incubator.apache.org/cloudstack/"));
    }

    @Test
    public void testExecuteInSequence() {
        final boolean b = aic.executeInSequence();
        assertTrue(b);
    }

    @Test
    public void testAllowCaching() {
        final boolean b = aic.allowCaching();
        assertTrue(b);
    }

    @Test
    public void testGetWait() {
        int b;
        aic.setWait(5);
        b = aic.getWait();
        assertEquals(b, 5);
        aic.setWait(-3);
        b = aic.getWait();
        assertEquals(b, -3);
        aic.setWait(0);
        b = aic.getWait();
        assertEquals(b, 0);
    }
}
