package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.google.gson.annotations.SerializedName;

public class AddIpToVmNicResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the secondary private IP addr")
    private Long id;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "Secondary IP address")
    private String ipAddr;

    @SerializedName(ApiConstants.NIC_ID)
    @Param(description = "the ID of the nic")
    private Long nicId;

    @SerializedName(ApiConstants.NETWORK_ID)
    @Param(description = "the ID of the network")
    private Long nwId;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_ID)
    @Param(description = "the ID of the vm")
    private Long vmId;

    public Long getId() {
        return id;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(final String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public Long getNicId() {
        return nicId;
    }

    public void setNicId(final Long nicId) {
        this.nicId = nicId;
    }

    public Long getNwId() {
        return nwId;
    }

    public void setNwId(final Long nwId) {
        this.nwId = nwId;
    }

    public Long getVmId() {
        return vmId;
    }

    public void setVmId(final Long vmId) {
        this.vmId = vmId;
    }

    public Long setId(final Long id) {
        return id;
    }
}
