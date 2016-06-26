package com.cloud.vpc.dao;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.element.VpcVirtualRouterElement;
import com.cloud.network.vpc.Vpc;
import com.cloud.vm.ReservationContext;

public class MockVpcVirtualRouterElement extends VpcVirtualRouterElement {
    @Override
    public boolean shutdownVpc(final Vpc vpc, final ReservationContext context) throws ConcurrentOperationException, ResourceUnavailableException {
        return true;
    }
}
