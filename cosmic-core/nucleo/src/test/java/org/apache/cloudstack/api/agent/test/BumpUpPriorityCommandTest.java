//

//

package org.apache.cloudstack.api.agent.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.cloud.agent.api.BumpUpPriorityCommand;
import com.cloud.agent.api.routing.NetworkElementCommand;

import org.junit.Test;

public class BumpUpPriorityCommandTest {

    BumpUpPriorityCommand bupc = new BumpUpPriorityCommand();

    // test super class
    @Test
    public void testSuperGetAccessDetail() {
        String value;
        bupc.setAccessDetail(NetworkElementCommand.ACCOUNT_ID, "accountID");
        value = bupc.getAccessDetail(NetworkElementCommand.ACCOUNT_ID);
        assertTrue(value.equals("accountID"));

        bupc.setAccessDetail(NetworkElementCommand.GUEST_NETWORK_CIDR, "GuestNetworkCIDR");
        value = bupc.getAccessDetail(NetworkElementCommand.GUEST_NETWORK_CIDR);
        assertTrue(value.equals("GuestNetworkCIDR"));

        bupc.setAccessDetail(NetworkElementCommand.GUEST_NETWORK_GATEWAY, "GuestNetworkGateway");
        value = bupc.getAccessDetail(NetworkElementCommand.GUEST_NETWORK_GATEWAY);
        assertTrue(value.equals("GuestNetworkGateway"));

        bupc.setAccessDetail(NetworkElementCommand.GUEST_VLAN_TAG, "GuestVlanTag");
        value = bupc.getAccessDetail(NetworkElementCommand.GUEST_VLAN_TAG);
        assertTrue(value.equals("GuestVlanTag"));

        bupc.setAccessDetail(NetworkElementCommand.ROUTER_NAME, "RouterName");
        value = bupc.getAccessDetail(NetworkElementCommand.ROUTER_NAME);
        assertTrue(value.equals("RouterName"));

        bupc.setAccessDetail(NetworkElementCommand.ROUTER_IP, "RouterIP");
        value = bupc.getAccessDetail(NetworkElementCommand.ROUTER_IP);
        assertTrue(value.equals("RouterIP"));

        bupc.setAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP, "RouterGuestIP");
        value = bupc.getAccessDetail(NetworkElementCommand.ROUTER_GUEST_IP);
        assertTrue(value.equals("RouterGuestIP"));

        bupc.setAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE, "ZoneNetworkType");
        value = bupc.getAccessDetail(NetworkElementCommand.ZONE_NETWORK_TYPE);
        assertTrue(value.equals("ZoneNetworkType"));

        bupc.setAccessDetail(NetworkElementCommand.GUEST_BRIDGE, "GuestBridge");
        value = bupc.getAccessDetail(NetworkElementCommand.GUEST_BRIDGE);
        assertTrue(value.equals("GuestBridge"));
    }

    @Test
    public void testExecuteInSequence() {
        final boolean b = bupc.executeInSequence();
        assertFalse(b);
    }
}
