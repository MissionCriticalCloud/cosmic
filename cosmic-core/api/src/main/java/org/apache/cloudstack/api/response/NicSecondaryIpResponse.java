package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.vm.NicSecondaryIp;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = NicSecondaryIp.class)
public class NicSecondaryIpResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the secondary private IP addr")
    private String id;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "Secondary IP address")
    private String ipAddr;

    @SerializedName(ApiConstants.NIC_ID)
    @Param(description = "the ID of the nic")
    private String nicId;

    @SerializedName(ApiConstants.NETWORK_ID)
    @Param(description = "the ID of the network")
    private String nwId;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_ID)
    @Param(description = "the ID of the vm")
    private String vmId;

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

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(final String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getNicId() {
        return nicId;
    }

    public void setNicId(final String string) {
        this.nicId = string;
    }

    public String getNwId() {
        return nwId;
    }

    public void setNwId(final String nwId) {
        this.nwId = nwId;
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(final String vmId) {
        this.vmId = vmId;
    }
}
