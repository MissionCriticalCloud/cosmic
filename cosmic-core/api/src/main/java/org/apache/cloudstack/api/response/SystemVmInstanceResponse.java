package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

/*
 * This is the generic response for all types of System VMs (SSVM, consoleproxy, domain routers(router, LB, DHCP))
 */
public class SystemVmInstanceResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the ID of the system VM")
    private String id;

    @SerializedName("systemvmtype")
    @Param(description = "the system VM type")
    private String systemVmType;

    @SerializedName("name")
    @Param(description = "the name of the system VM")
    private String name;

    @SerializedName("hostid")
    @Param(description = "the host ID for the system VM")
    private String hostId;

    @SerializedName("state")
    @Param(description = "the state of the system VM")
    private String state;

    @SerializedName("role")
    @Param(description = "the role of the system VM")
    private String role;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getSystemVmType() {
        return systemVmType;
    }

    public void setSystemVmType(final String systemVmType) {
        this.systemVmType = systemVmType;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(final String hostId) {
        this.hostId = hostId;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }
}
