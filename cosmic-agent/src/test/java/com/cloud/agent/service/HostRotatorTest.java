package com.cloud.agent.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;

import org.junit.Test;

public class HostRotatorTest {

    public static final String HOST_1 = "host1";
    public static final String HOST_2 = "host2";

    @Test(expected = IllegalStateException.class)
    public void test_nextHost_whenHostsListIsEmpty() throws Exception {
        new HostRotator().nextHost();
    }

    @Test
    public void test_nextHost_whenHostsListHasOneEntry() throws Exception {
        final HostRotator hostRotator = new HostRotator();
        hostRotator.addAll(Arrays.asList(HOST_1));

        final String firstHost = hostRotator.nextHost();
        final String secondHost = hostRotator.nextHost();

        assertThat(firstHost, is(HOST_1));
        assertThat(secondHost, is(HOST_1));
    }

    @Test
    public void test_nextHost_whenHostsListHasMoreThanOneEntry() throws Exception {
        final HostRotator hostRotator = new HostRotator();
        hostRotator.addAll(Arrays.asList(HOST_1, HOST_2));

        final String firstHost = hostRotator.nextHost();
        final String secondHost = hostRotator.nextHost();
        final String thirdHost = hostRotator.nextHost();

        assertThat(firstHost, is(HOST_1));
        assertThat(secondHost, is(HOST_2));
        assertThat(thirdHost, is(HOST_1));
    }
}
