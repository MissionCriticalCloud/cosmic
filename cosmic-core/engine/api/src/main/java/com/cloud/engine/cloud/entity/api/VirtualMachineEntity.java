package com.cloud.engine.cloud.entity.api;

import com.cloud.deploy.DeploymentPlan;
import com.cloud.deploy.DeploymentPlanner;
import com.cloud.deploy.DeploymentPlanner.ExcludeList;
import com.cloud.engine.entity.api.CloudStackEntity;
import com.cloud.exception.CloudException;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.vm.VirtualMachineProfile;

import javax.ws.rs.BeanParam;
import java.util.Map;

public interface VirtualMachineEntity extends CloudStackEntity {

    String reserve(DeploymentPlanner plannerToUse, @BeanParam DeploymentPlan plan, ExcludeList exclude, String caller) throws InsufficientCapacityException, ResourceUnavailableException;

    void deploy(String reservationId, String caller, Map<VirtualMachineProfile.Param, Object> params, boolean deployOnGivenHost) throws InsufficientCapacityException, ResourceUnavailableException;

    boolean stop(String caller) throws CloudException;

    boolean stopForced(String caller) throws CloudException;

    boolean destroy(String caller) throws CloudException, ConcurrentOperationException;
}
