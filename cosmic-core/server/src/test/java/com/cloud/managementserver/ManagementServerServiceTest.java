package com.cloud.managementserver;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import com.cloud.cluster.ManagementServerHostVO;
import com.cloud.cluster.dao.ManagementServerHostDao;
import com.cloud.utils.net.AddressUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AddressUtils.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ManagementServerServiceTest {
    @Mock
    private ManagementServerHostDao dao;

    @Test
    public void discoverManagementServerIps_whenBothIpsArePublic() throws Exception {
        PowerMockito.mockStatic(AddressUtils.class);
        given(AddressUtils.isPublicAddress("server1Ip")).willReturn(true);
        given(AddressUtils.isPublicAddress("server2Ip")).willReturn(true);

        final Date now = new Date();
        when(dao.listAll()).thenReturn(Arrays.asList(new ManagementServerHostVO(1l, 1l, "server1Ip", 1, now), new ManagementServerHostVO(2l, 2l, "server2Ip", 2, now)));

        final Collection<String> ips = new ManagementServerService(dao).discoverManagementServerIps().collect(toList());

        assertThat(ips, notNullValue());
        assertThat(ips, hasSize(2));
        assertThat(ips, containsInAnyOrder("server1Ip", "server2Ip"));
    }

    @Test
    public void discoverManagementServerIps_whenOneIpIsNotPublic() throws Exception {
        PowerMockito.mockStatic(AddressUtils.class);
        given(AddressUtils.isPublicAddress("server1Ip")).willReturn(false);
        given(AddressUtils.isPublicAddress("server2Ip")).willReturn(true);

        final Date now = new Date();
        when(dao.listAll()).thenReturn(Arrays.asList(new ManagementServerHostVO(1l, 1l, "server1Ip", 1, now), new ManagementServerHostVO(2l, 2l, "server2Ip", 2, now)));

        final Collection<String> ips = new ManagementServerService(dao).discoverManagementServerIps().collect(toList());

        assertThat(ips, notNullValue());
        assertThat(ips, containsInAnyOrder("localhost", "server2Ip"));
    }

    @Test
    public void discoverManagementServerIps_whenNoIpPublic() throws Exception {
        PowerMockito.mockStatic(AddressUtils.class);
        given(AddressUtils.isPublicAddress("server1Ip")).willReturn(false);
        given(AddressUtils.isPublicAddress("server2Ip")).willReturn(false);

        final Date now = new Date();
        when(dao.listAll()).thenReturn(Arrays.asList(new ManagementServerHostVO(1l, 1l, "server1Ip", 1, now), new ManagementServerHostVO(2l, 2l, "server2Ip", 2, now)));

        final Collection<String> ips = new ManagementServerService(dao).discoverManagementServerIps().collect(toList());

        assertThat(ips, notNullValue());
        assertThat(ips, hasSize(1));
        assertThat(ips, containsInAnyOrder("localhost"));
    }
}
