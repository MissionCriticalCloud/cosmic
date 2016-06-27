package org.apache.cloudstack.network.lb;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.exception.StorageUnavailableException;
import com.cloud.network.router.VirtualRouter;
import com.cloud.user.Account;

public interface InternalLoadBalancerVMService {

    VirtualRouter startInternalLbVm(long internalLbVmId, Account caller, long callerUserId) throws StorageUnavailableException, InsufficientCapacityException,
            ConcurrentOperationException, ResourceUnavailableException;

    VirtualRouter stopInternalLbVm(long vmId, boolean forced, Account caller, long callerUserId) throws ConcurrentOperationException, ResourceUnavailableException;
}
