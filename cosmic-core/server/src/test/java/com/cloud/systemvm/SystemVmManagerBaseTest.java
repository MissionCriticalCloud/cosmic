package com.cloud.systemvm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import com.cloud.managementserver.ManagementServerService;

import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SystemVmManagerBaseTest {
    @Mock
    private ManagementServerService managementServerService;

    @Test
    public void computeManagementServerIpList() throws Exception {
        when(managementServerService.discoverManagementServerIps()).thenReturn(Stream.of("server1Ip", "server2Ip"));

        final String ipList = new SystemVmManagerBaseImpl().computeManagementServerIpList(managementServerService);

        assertThat(ipList, is("server1Ip,server2Ip"));
    }

    class SystemVmManagerBaseImpl extends SystemVmManagerBase {
        @Override
        public String computeManagementServerIpList(final ManagementServerService managementServerService) {
            return super.computeManagementServerIpList(managementServerService);
        }
    }
}
