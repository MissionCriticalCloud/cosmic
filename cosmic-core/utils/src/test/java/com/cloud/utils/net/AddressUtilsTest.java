package com.cloud.utils.net;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.net.UnknownHostException;

import org.junit.Test;

public class AddressUtilsTest {

    @Test(expected = UnknownHostException.class)
    public void isLocalAddress_unknownHost() throws Exception {
        AddressUtils.isLocalAddress("SomeUnknownAddress");
    }

    @Test
    public void isLocalAddress_localhost() throws Exception {
        assertThat(AddressUtils.isLocalAddress("localhost"), is(true));
        assertThat(AddressUtils.isLocalAddress("127.0.0.1"), is(true));
    }

    @Test
    public void isLocalAddress_linkLocal() throws Exception {
        assertThat(AddressUtils.isLocalAddress("169.254.0.1"), is(true));
    }

    @Test
    public void isLocalAddress_privateNetwork() throws Exception {
        assertThat(AddressUtils.isLocalAddress("10.0.0.1"), is(false));
        assertThat(AddressUtils.isLocalAddress("172.16.0.1"), is(false));
        assertThat(AddressUtils.isLocalAddress("192.168.0.1"), is(false));
    }

    @Test
    public void isPublicAddress_privateNetwork() throws Exception {
        assertThat(AddressUtils.isPublicAddress("10.0.0.1"), is(true));
        assertThat(AddressUtils.isPublicAddress("172.16.0.1"), is(true));
        assertThat(AddressUtils.isPublicAddress("192.168.0.1"), is(true));
    }
}
