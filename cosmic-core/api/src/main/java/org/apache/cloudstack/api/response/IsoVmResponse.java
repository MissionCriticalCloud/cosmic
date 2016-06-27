package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.template.VirtualMachineTemplate;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = VirtualMachineTemplate.class)
public class IsoVmResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the ISO ID")
    private String id;

    @SerializedName("name")
    @Param(description = "the ISO name")
    private String name;

    @SerializedName("displaytext")
    @Param(description = "the ISO display text")
    private String displayText;

    @SerializedName("bootable")
    @Param(description = "true if the ISO is bootable, false otherwise")
    private Boolean bootable;

    @SerializedName("isfeatured")
    @Param(description = "true if this template is a featured template, false otherwise")
    private Boolean featured;

    @SerializedName("ostypeid")
    @Param(description = "the ID of the OS type for this template.")
    private String osTypeId;

    @SerializedName("ostypename")
    @Param(description = "the name of the OS type for this template.")
    private String osTypeName;

    @SerializedName("virtualmachineid")
    @Param(description = "id of the virtual machine")
    private String virtualMachineId;

    @SerializedName("vmname")
    @Param(description = "name of the virtual machine")
    private String virtualMachineName;

    @SerializedName("vmdisplayname")
    @Param(description = "display name of the virtual machine")
    private String virtualMachineDisplayName;

    @SerializedName("vmstate")
    @Param(description = "state of the virtual machine")
    private String virtualMachineState;

    @Override
    public String getObjectId() {
        return this.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getOsTypeId() {
        return osTypeId;
    }

    public void setOsTypeId(final String osTypeId) {
        this.osTypeId = osTypeId;
    }

    public String getOsTypeName() {
        return osTypeName;
    }

    public void setOsTypeName(final String osTypeName) {
        this.osTypeName = osTypeName;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public Boolean isBootable() {
        return bootable;
    }

    public void setBootable(final Boolean bootable) {
        this.bootable = bootable;
    }

    public Boolean isFeatured() {
        return featured;
    }

    public void setFeatured(final Boolean featured) {
        this.featured = featured;
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

    public String getVirtualMachineDisplayName() {
        return virtualMachineDisplayName;
    }

    public void setVirtualMachineDisplayName(final String virtualMachineDisplayName) {
        this.virtualMachineDisplayName = virtualMachineDisplayName;
    }

    public String getVirtualMachineState() {
        return virtualMachineState;
    }

    public void setVirtualMachineState(final String virtualMachineState) {
        this.virtualMachineState = virtualMachineState;
    }
}
