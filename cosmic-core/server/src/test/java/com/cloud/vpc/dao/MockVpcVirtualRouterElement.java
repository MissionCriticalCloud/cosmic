package com.cloud.vpc.dao;

import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.legacymodel.network.vpc.Vpc;
import com.cloud.network.element.VpcVirtualRouterElement;
import com.cloud.vm.ReservationContext;

public class MockVpcVirtualRouterElement extends VpcVirtualRouterElement {
    @Override
    public boolean shutdownVpc(final Vpc vpc, final ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        return true;
    }
}
