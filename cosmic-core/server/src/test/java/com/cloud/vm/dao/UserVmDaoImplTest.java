package com.cloud.vm.dao;

import com.cloud.hypervisor.Hypervisor;
import com.cloud.vm.UserVmVO;
import com.cloud.vm.VirtualMachine;

import javax.inject.Inject;

import junit.framework.TestCase;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

public class UserVmDaoImplTest extends TestCase {
    @Inject
    UserVmDao dao;

    @Test
    public void testPersist() {
        final Long vmId = 2222l;
        makeAndVerifyEntry(vmId, "vm1", "vmdisp1", 1l, false, Hypervisor.HypervisorType.KVM, 1l, false, true, 1l, 1l, 1l, "uservm1", 1l);
        makeAndVerifyEntry(vmId, "vm1", "vmdisp1", 1l, true, Hypervisor.HypervisorType.KVM, 1l, false, true, 1l, 1l, 1l, "uservm1", 1l);
    }

    public void makeAndVerifyEntry(final Long vmId, final String instanceName, final String displayName, final long templateId, final boolean userdataFlag, final Hypervisor
            .HypervisorType hypervisor,
                                   final long guestOsId, final boolean haEnabled, final boolean limitCpuUse, final long domainId, final long accountId, final long
                                           serviceOfferingId, final String name, final Long
                                           diskOfferingId) {

        dao.expunge(vmId);
        final String userdata;

        if (userdataFlag) {
            // Generate large userdata to simulate 32k of random string data for userdata submitted through HTTP POST requests.
            userdata = RandomStringUtils.randomAlphanumeric(32 * 1024);
        } else {
            // Generate normal sized userdata to simulate 2k of random string data.
            userdata = RandomStringUtils.randomAlphanumeric(2 * 1024);
        }

        // Persist the data.
        UserVmVO vo =
                new UserVmVO(vmId, instanceName, displayName, templateId, hypervisor, guestOsId, haEnabled, limitCpuUse, domainId, accountId, 1, serviceOfferingId, userdata,
                        name, diskOfferingId);
        dao.persist(vo);

        vo = dao.findById(vmId);
        assert (vo.getType() == VirtualMachine.Type.User) : "Incorrect type " + vo.getType();

        // Check whether the userdata matches what we generated.
        assert (vo.getUserData().equals(userdata)) : "User data retrieved does not match userdata generated as input";
    }
}
