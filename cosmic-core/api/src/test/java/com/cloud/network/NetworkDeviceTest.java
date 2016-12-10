package com.cloud.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

public class NetworkDeviceTest {

    @Test
    public void test_createNetworkDevice_canAddItSelfToDeviceList() throws Exception {
        final String deviceName = "someDevice";
        final ExternalNetworkDeviceManager.NetworkDevice networkDevice = new ExternalNetworkDeviceManager.NetworkDevice(deviceName);

        assertThat(ExternalNetworkDeviceManager.NetworkDevice.getNetworkDevice(deviceName), is(networkDevice));
    }
}
