package org.apache.cloudstack.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.apache.cloudstack.network.ExternalNetworkDeviceManager.NetworkDevice;

import org.junit.Test;

public class NetworkDeviceTest {

    @Test
    public void test_createNetworkDevice_canAddItSelfToDeviceList() throws Exception {
        final String deviceName = "someDevice";
        final NetworkDevice networkDevice = new NetworkDevice(deviceName);

        assertThat(NetworkDevice.getNetworkDevice(deviceName), is(networkDevice));
    }
}
