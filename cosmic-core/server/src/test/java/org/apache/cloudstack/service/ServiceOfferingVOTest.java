package org.apache.cloudstack.service;

import com.cloud.service.ServiceOfferingVO;
import com.cloud.storage.Storage;
import com.cloud.vm.VirtualMachine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class ServiceOfferingVOTest {
    ServiceOfferingVO offeringCustom;
    ServiceOfferingVO offering;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        offeringCustom = new ServiceOfferingVO("custom", null, null, 500, 10, 10, false, "custom", Storage.ProvisioningType.THIN, false, false, "", false, VirtualMachine.Type
                .User, false);
        offering = new ServiceOfferingVO("normal", 1, 1000, 500, 10, 10, false, "normal", Storage.ProvisioningType.THIN, false, false, "", false, VirtualMachine.Type.User, false);
    }

    // Test restoreVm when VM state not in running/stopped case
    @Test
    public void isDynamic() {
        Assert.assertTrue(offeringCustom.isDynamic());
    }

    @Test
    public void notDynamic() {
        Assert.assertTrue(!offering.isDynamic());
    }
}
