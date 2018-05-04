package com.cloud.systemvm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import com.cloud.managementserver.ManagementServerService;
import com.cloud.legacymodel.utils.Pair;
import com.cloud.vm.AfterScanAction;

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

        @Override
        public String getScanHandlerName() {
            return null;
        }

        @Override
        public boolean canScan() {
            return false;
        }

        @Override
        public void onScanStart() {

        }

        @Override
        public Long[] getScannablePools() {
            return new Long[0];
        }

        @Override
        public boolean isPoolReadyForScan(final Long pool) {
            return false;
        }

        @Override
        public Pair<AfterScanAction, Object> scanPool(final Long pool) {
            return null;
        }

        @Override
        public void expandPool(final Long pool, final Object actionArgs) {

        }

        @Override
        public void shrinkPool(final Long pool, final Object actionArgs) {

        }

        @Override
        public void onScanEnd() {

        }
    }
}
