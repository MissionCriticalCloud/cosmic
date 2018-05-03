package com.cloud.engine.cloud.entity.api;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.engine.cloud.entity.api.db.VMEntityVO;
import com.cloud.legacymodel.exceptions.AgentUnavailableException;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.OperationTimedoutException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.vm.VirtualMachineProfile;

import java.util.Map;

public interface VMEntityManager {

    VMEntityVO loadVirtualMachine(String vmId);

    void saveVirtualMachine(VMEntityVO vmInstanceVO);

    String reserveVirtualMachine(VMEntityVO vmEntityVO, DeploymentPlanner plannerToUse, DeploymentPlan plan, ExcludeList exclude) throws InsufficientCapacityException,
            ResourceUnavailableException;

    void deployVirtualMachine(String reservationId, VMEntityVO vmEntityVO, String caller, Map<VirtualMachineProfile.Param, Object> params, boolean deployOnGivenHost)
            throws InsufficientCapacityException, ResourceUnavailableException;

    boolean stopvirtualmachine(VMEntityVO vmEntityVO, String caller) throws ResourceUnavailableException;

    boolean stopvirtualmachineforced(VMEntityVO vmEntityVO, String caller) throws ResourceUnavailableException;

    boolean destroyVirtualMachine(VMEntityVO vmEntityVO, String caller) throws AgentUnavailableException, OperationTimedoutException, ConcurrentOperationException;
}
