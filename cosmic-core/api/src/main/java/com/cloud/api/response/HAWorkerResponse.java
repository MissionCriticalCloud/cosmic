package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.ha.HaWork;
import com.cloud.serializer.Param;
import com.cloud.vm.VirtualMachine.State;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class HAWorkerResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "ID of the HA worker")
    private Long id;

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "Type of the HA worker")
    private HaWork.HaWorkType type;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "Date when the HA worker was created")
    private Date created;

    @SerializedName(ApiConstants.STEP)
    @Param(description = "Current step the HA worker is at")
    private HaWork.HaWorkStep step;

    @SerializedName(ApiConstants.TAKEN)
    @Param(description = "Date of the last step this HA worker has taken")
    private Date taken;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "Previous state of the virtual machine this HA worker belongs too")
    private State state;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_ID)
    @Param(description = "ID of the virtual machine this HA worker belongs too")
    private String virtualMachineId;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_NAME)
    @Param(description = "Name of the virtual machine this HA worker belongs too")
    private String virtualMachineName;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_STATE)
    @Param(description = "State of the virtual machine this HA worker belongs too")
    private State virtualMachineState;

    @SerializedName(ApiConstants.HYPERVISOR)
    @Param(description = "Hypervisor this HA worker belongs too")
    private String hypervisor;

    @SerializedName(ApiConstants.MANAGEMENT_SERVER_NAME)
    @Param(description = "Name of the management server this HA worker belongs too")
    private String managementServerName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "ID of the domain this HA worker belongs too")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN_NAME)
    @Param(description = "Name of the domain this HA worker belongs too")
    private String domainName;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public HaWork.HaWorkType getType() {
        return type;
    }

    public void setType(final HaWork.HaWorkType type) {
        this.type = type;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public HaWork.HaWorkStep getStep() {
        return step;
    }

    public void setStep(final HaWork.HaWorkStep step) {
        this.step = step;
    }

    public Date getTaken() {
        return taken;
    }

    public void setTaken(final Date taken) {
        this.taken = taken;
    }

    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public String getVirtualMachineId() {
        return virtualMachineId;
    }

    public void setVirtualMachineId(final String virtualMachineId) {
        this.virtualMachineId = virtualMachineId;
    }

    public String getVirtualMachineName() {
        return virtualMachineName;
    }

    public void setVirtualMachineName(final String virtualMachineName) {
        this.virtualMachineName = virtualMachineName;
    }

    public State getVirtualMachineState() {
        return virtualMachineState;
    }

    public void setVirtualMachineState(final State virtualMachineState) {
        this.virtualMachineState = virtualMachineState;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(final String hypervisor) {
        this.hypervisor = hypervisor;
    }

    public String getManagementServerName() {
        return managementServerName;
    }

    public void setManagementServerName(final String managementServerName) {
        this.managementServerName = managementServerName;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }
}
