package org.apache.cloudstack.engine.cloud.entity.api;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.engine.cloud.entity.api.db.VMEntityVO;

import java.util.Map;

public interface VMEntityManager {

    VMEntityVO loadVirtualMachine(String vmId);

    void saveVirtualMachine(VMEntityVO vmInstanceVO);

    String reserveVirtualMachine(VMEntityVO vmEntityVO, DeploymentPlanner plannerToUse, DeploymentPlan plan, ExcludeList exclude) throws InsufficientCapacityException,
            ResourceUnavailableException;

    void deployVirtualMachine(String reservationId, VMEntityVO vmEntityVO, String caller, Map<VirtualMachineProfile.Param, Object> params)
            throws InsufficientCapacityException, ResourceUnavailableException;

    boolean stopvirtualmachine(VMEntityVO vmEntityVO, String caller) throws ResourceUnavailableException;

    boolean destroyVirtualMachine(VMEntityVO vmEntityVO, String caller) throws AgentUnavailableException, OperationTimedoutException, ConcurrentOperationException;
}
