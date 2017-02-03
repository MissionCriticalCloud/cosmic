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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

/**
 * VirtualMachineEntity represents a Virtual Machine in Cloud Orchestration
 * Platform.
 */
@Path("vm/{id}")
@Produces({"application/json", "application/xml"})
@XmlRootElement(name = "vm")
public interface VirtualMachineEntity extends CloudStackEntity {

    /**
     * @return List of uuids for volumes attached to this virtual machine.
     */
    @GET
    List<String> listVolumeIds();

    /**
     * @return List of volumes attached to this virtual machine.
     */
    List<VolumeEntity> listVolumes();

    /**
     * @return the template this virtual machine is based off.
     */
    TemplateEntity getTemplate();

    /**
     * Start the virtual machine with a given deployment plan
     *
     * @param plannerToUse the Deployment Planner that should be used
     * @param plan         plan to which to deploy the machine
     * @param exclude      list of areas to exclude
     * @return a reservation id
     */
    String reserve(DeploymentPlanner plannerToUse, @BeanParam DeploymentPlan plan, ExcludeList exclude, String caller) throws InsufficientCapacityException,
            ResourceUnavailableException;

    /**
     * Deploy this virtual machine according to the reservation from before.
     *
     * @param reservationId reservation id from reserve call.
     */
    void deploy(String reservationId, String caller, Map<VirtualMachineProfile.Param, Object> params, boolean deployOnGivenHost)
            throws InsufficientCapacityException, ResourceUnavailableException;

    /**
     * Stop the virtual machine
     */
    boolean stop(String caller) throws CloudException;

    /**
     * Stop the virtual machine, by force if necessary
     */
    boolean stopForced(String caller) throws ResourceUnavailableException, CloudException;

    /**
     * Cleans up after any botched starts.  CloudStack Orchestration Platform
     * will attempt a best effort to actually shutdown any resource but
     * even if it cannot, it releases the resource from its database.
     */
    void cleanup();

    /**
     * Destroys the VM.
     */
    boolean destroy(String caller) throws CloudException, ConcurrentOperationException;

    /**
     * Duplicate this VM in the database so that it will start new
     *
     * @param externalId
     * @return a new VirtualMachineEntity
     */
    VirtualMachineEntity duplicate(String externalId);

    /**
     * Attach volume to this VM
     *
     * @param volume   volume to attach
     * @param deviceId deviceId to use
     */
    void attach(VolumeEntity volume, short deviceId);

    /**
     * Detach the volume from this VM
     *
     * @param volume volume to detach
     */
    void detach(VolumeEntity volume);
}
