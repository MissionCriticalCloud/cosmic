package com.cloud.resource;

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dc.dao.HostPodDao;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.org.Grouping;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ResourceCheckerTest {

    @Mock
    private DataCenterDao dataCenterDao;

    @Mock
    private AccountManager accountManager;

    @Mock
    private HostPodDao hostPodDao;


    @Test(expected = InvalidParameterValueException.class)
    public void test_checkIfDataCenterExists_whenDataCenterDoesNotExist() throws Exception {
        when(dataCenterDao.findById(1L)).thenReturn(null);
        final ResourceChecker resourceChecker = buildResourceChecker();

        resourceChecker.checkIfDataCenterExists(1L);
    }

    @Test
    public void test_checkIfDataCenterExists_whenDataCenterExists() throws Exception {
        final DataCenterVO dataCenter = new DataCenterVO();
        when(dataCenterDao.findById(1L)).thenReturn(dataCenter);
        final ResourceChecker resourceChecker = buildResourceChecker();


        assertThat(resourceChecker.checkIfDataCenterExists(1L), is(dataCenter));
    }

    @Test(expected = PermissionDeniedException.class)
    public void test_checkIfDataCenterIsUsable_whenDataCenterIsDisabledAndAccountIsNotRoot() throws Exception {
        final DataCenterVO dataCenter = new DataCenterVO();
        dataCenter.setAllocationState(Grouping.AllocationState.Disabled);
        final AccountVO account = new AccountVO(1L);
        when(accountManager.isRootAdmin(1L)).thenReturn(false);
        final ResourceChecker resourceChecker = buildResourceChecker();

        resourceChecker.checkIfDataCenterIsUsable(dataCenter, account);
    }

    @Test
    public void test_checkIfDataCenterIsUsable_whenDataCenterIsDisabledAndAccountIsRoot() throws Exception {
        final DataCenterVO dataCenter = new DataCenterVO();
        dataCenter.setAllocationState(Grouping.AllocationState.Disabled);
        final AccountVO account = new AccountVO(1L);
        when(accountManager.isRootAdmin(1L)).thenReturn(true);
        final ResourceChecker resourceChecker = buildResourceChecker();

        try {
            resourceChecker.checkIfDataCenterIsUsable(dataCenter, account);
        } catch (final PermissionDeniedException e) {
            fail("No PermissionDeniedException should have be generated");
        }
    }

    @Test
    public void test_checkIfDataCenterIsUsable_whenDataCenterIsEnabledAndAccountIsNotRoot() throws Exception {
        final DataCenterVO dataCenter = new DataCenterVO();
        dataCenter.setAllocationState(Grouping.AllocationState.Enabled);
        final AccountVO account = new AccountVO(1L);
        when(accountManager.isRootAdmin(1L)).thenReturn(false);
        final ResourceChecker resourceChecker = buildResourceChecker();

        try {
            resourceChecker.checkIfDataCenterIsUsable(dataCenter, account);
        } catch (final PermissionDeniedException e) {
            fail("No PermissionDeniedException should have be generated");
        }
    }

    @Test
    public void test_checkIfDataCenterIsUsable_whenDataCenterIsEnabledAndAccountIsRoot() throws Exception {
        final DataCenterVO dataCenter = new DataCenterVO();
        dataCenter.setAllocationState(Grouping.AllocationState.Enabled);
        final AccountVO account = new AccountVO(1L);
        when(accountManager.isRootAdmin(1L)).thenReturn(true);
        final ResourceChecker resourceChecker = buildResourceChecker();

        try {
            resourceChecker.checkIfDataCenterIsUsable(dataCenter, account);
        } catch (final PermissionDeniedException e) {
            fail("No PermissionDeniedException should have be generated");
        }
    }

    @Test(expected = InvalidParameterValueException.class)
    public void test_checkIfPodExists_whenPodDoesNotExist() throws Exception {
        when(hostPodDao.findById(1L)).thenReturn(null);
        final ResourceChecker resourceChecker = buildResourceChecker();

        resourceChecker.checkIfPodExists(1L);
    }

    @Test
    public void test_checkIfPodExists_whenPodDoesExist() throws Exception {
        final HostPodVO hostPod = new HostPodVO();
        when(hostPodDao.findById(1L)).thenReturn(hostPod);
        final ResourceChecker resourceChecker = buildResourceChecker();

        assertThat(resourceChecker.checkIfPodExists(1L), is(hostPod));
    }

    @Test(expected = InvalidParameterValueException.class)
    public void test_checkIfPodIsUsable_whenPodDoesNotBelongToDataCenter() throws Exception {
        final DataCenterVO dataCenter = new DataCenterVO();
        dataCenter.setId(1L);
        dataCenter.setUuid("DataCenterUUID");
        final HostPodVO hostPod = new HostPodVO();
        hostPod.setDataCenterId(2L);
        hostPod.setUuid("HostPodUUID");
        final ResourceChecker resourceChecker = buildResourceChecker();

        resourceChecker.checkIfPodIsUsable(dataCenter, hostPod);
    }

    @Test
    public void test_checkIfPodIsUsable_whenPodBelongsToDataCenter() throws Exception {
        final DataCenterVO dataCenter = new DataCenterVO();
        dataCenter.setId(1L);
        dataCenter.setUuid("DataCenterUUID");
        final HostPodVO hostPod = new HostPodVO();
        hostPod.setDataCenterId(1L);
        hostPod.setUuid("HostPodUUID");
        final ResourceChecker resourceChecker = buildResourceChecker();

        try {
            resourceChecker.checkIfPodIsUsable(dataCenter, hostPod);
        } catch (final InvalidParameterValueException e) {
            fail("No InvalidParameterValueException should have been generated");
        }
    }

    private ResourceChecker buildResourceChecker() {
        return ResourceChecker.builder()
                .dataCenterDao(dataCenterDao)
                .accountManager(accountManager)
                .hostPodDao(hostPodDao)
                .build();
    }


}