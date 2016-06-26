package org.apache.cloudstack.api.response;

import com.cloud.network.PhysicalNetworkTrafficType;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = PhysicalNetworkTrafficType.class)
public class TrafficTypeResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "id of the network provider")
    private String id;

    @SerializedName(ApiConstants.TRAFFIC_TYPE)
    @Param(description = "the trafficType to be added to the physical network")
    private String trafficType;

    @SerializedName(ApiConstants.PHYSICAL_NETWORK_ID)
    @Param(description = "the physical network this belongs to")
    private String physicalNetworkId;

    @SerializedName(ApiConstants.XENSERVER_NETWORK_LABEL)
    @Param(description = "The network name label of the physical device dedicated to this traffic on a XenServer host")
    private String xenNetworkLabel;

    @SerializedName(ApiConstants.KVM_NETWORK_LABEL)
    @Param(description = "The network name label of the physical device dedicated to this traffic on a KVM host")
    private String kvmNetworkLabel;

    @SerializedName(ApiConstants.OVM3_NETWORK_LABEL)
    @Param(description = "The network name of the physical device dedicated to this traffic on an OVM3 host")
    private String ovm3NetworkLabel;

    @Override
    public String getObjectId() {
        return id;
    }

    public void setPhysicalNetworkId(final String physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public String getphysicalNetworkId() {
        return physicalNetworkId;
    }

    public String getId() {
        return id;
    }

    public void setId(final String uuid) {
        id = uuid;
    }

    public String getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(final String trafficType) {
        this.trafficType = trafficType;
    }

    public String getXenLabel() {
        return xenNetworkLabel;
    }

    public void setXenLabel(final String xenLabel) {
        xenNetworkLabel = xenLabel;
    }

    public String getKvmLabel() {
        return kvmNetworkLabel;
    }

    public void setKvmLabel(final String kvmLabel) {
        kvmNetworkLabel = kvmLabel;
    }

    public String getOvm3Label() {
        return ovm3NetworkLabel;
    }

    public void setOvm3Label(final String ovm3Label) {
        ovm3NetworkLabel = ovm3Label;
    }
}
