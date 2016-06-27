//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.api.CheckNetworkCommand;
import com.cloud.network.PhysicalNetworkSetupInfo;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CheckNetworkCommandTest {
    CheckNetworkCommand cnc;

    @Before
    public void setUp() {
        final
        List<PhysicalNetworkSetupInfo> net = Mockito.mock(List.class);
        cnc = new CheckNetworkCommand(net);
    }

    @Test
    public void testGetPhysicalNetworkInfoList() {
        final List<PhysicalNetworkSetupInfo> networkInfoList = cnc.getPhysicalNetworkInfoList();
        assertEquals(0, networkInfoList.size());
    }

    @Test
    public void testExecuteInSequence() {
        final boolean b = cnc.executeInSequence();
        assertTrue(b);
    }
}
