package com.cloud.vm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import com.cloud.framework.jobs.impl.VmWorkJobVO;
import com.cloud.network.dao.NetworkVO;
import com.cloud.vm.VirtualMachineManagerImpl.JobHelper;

import org.junit.Test;

public class JobHelperTest {

    @Test
    public void test_jobIsForSameNetwork_whenJobIsForNetwork() throws Exception {
        final long networkId = 1L;
        final VmWorkAddVmToNetwork vmWorkAddVmToNetwork = new VmWorkAddVmToNetwork(0, 0, 0, "someHandler", networkId, null);
        final VmWorkJobVO vmWorkJobVO = new VmWorkJobVO("someContext");
        vmWorkJobVO.setCmdInfo(VmWorkSerializer.serialize(vmWorkAddVmToNetwork));
        final NetworkVO networkVO = new NetworkVO();
        networkVO.setId(networkId);

        assertThat(new JobHelper().jobIsForSameNetwork(vmWorkJobVO, networkVO), is(true));
    }

    @Test
    public void test_jobIsForSameNetwork_whenJobIsNotForNetwork() throws Exception {
        final long someNetworkId = 1L;
        final long anotherNetworkId = 2L;
        final VmWorkAddVmToNetwork vmWorkAddVmToNetwork = new VmWorkAddVmToNetwork(0, 0, 0, "someHandler", someNetworkId, null);
        final VmWorkJobVO vmWorkJobVO = new VmWorkJobVO("someContext");
        vmWorkJobVO.setCmdInfo(VmWorkSerializer.serialize(vmWorkAddVmToNetwork));
        final NetworkVO networkVO = new NetworkVO();
        networkVO.setId(anotherNetworkId);

        assertThat(new JobHelper().jobIsForSameNetwork(vmWorkJobVO, networkVO), is(false));
    }

    @Test
    public void test_jobIsForSameNetwork_whenJobCmdInfoNetworkIdIsNull() throws Exception {
        final long networkId = 1L;
        final VmWorkAddVmToNetwork vmWorkAddVmToNetwork = new VmWorkAddVmToNetwork(0, 0, 0, "someHandler", null, null);
        final VmWorkJobVO vmWorkJobVO = new VmWorkJobVO("someContext");
        vmWorkJobVO.setCmdInfo(VmWorkSerializer.serialize(vmWorkAddVmToNetwork));
        final NetworkVO networkVO = new NetworkVO();
        networkVO.setId(networkId);

        assertThat(new JobHelper().jobIsForSameNetwork(vmWorkJobVO, networkVO), is(false));
    }

    @Test
    public void test_jobIsForSameNetwork_whenJobCmdInfoIsNull() throws Exception {
        final long networkId = 1L;
        final VmWorkJobVO vmWorkJobVO = new VmWorkJobVO("someContext");
        final NetworkVO networkVO = new NetworkVO();
        networkVO.setId(networkId);

        assertThat(new JobHelper().jobIsForSameNetwork(vmWorkJobVO, networkVO), is(false));
    }
}
