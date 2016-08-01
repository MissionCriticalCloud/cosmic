package com.cloud.managementserver;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.when;

import com.cloud.cluster.ManagementServerHostVO;
import com.cloud.cluster.dao.ManagementServerHostDao;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ManagementServerServiceTest {
    @Mock
    private ManagementServerHostDao dao;

    @Test
    public void discoverManagementServerIps() throws Exception {
        final Date now = new Date();
        when(dao.listAll()).thenReturn(Arrays.asList(new ManagementServerHostVO(1l, 1l, "server1Ip", 1, now), new ManagementServerHostVO(2l, 2l, "server2Ip", 2, now)));

        final Collection<String> ips = new ManagementServerService(dao).discoverManagementServerIps().collect(toList());

        assertThat(ips, notNullValue());
        assertThat(ips, hasSize(2));
        assertThat(ips, containsInAnyOrder("server1Ip", "server2Ip"));
    }
}
