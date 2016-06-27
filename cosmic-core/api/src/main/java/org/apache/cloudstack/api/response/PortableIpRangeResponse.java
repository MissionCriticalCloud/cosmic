package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;
import org.apache.cloudstack.region.PortableIpRange;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = PortableIpRange.class)
public class PortableIpRangeResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "portable IP range ID")
    private String id;

    @SerializedName(ApiConstants.REGION_ID)
    @Param(description = "Region Id in which portable ip range is provisioned")
    private Integer regionId;

    @SerializedName(ApiConstants.GATEWAY)
    @Param(description = "the gateway of the VLAN IP range")
    private String gateway;

    @SerializedName(ApiConstants.NETMASK)
    @Param(description = "the netmask of the VLAN IP range")
    private String netmask;

    @SerializedName(ApiConstants.VLAN)
    @Param(description = "the ID or VID of the VLAN.")
    private String vlan;

    @SerializedName(ApiConstants.START_IP)
    @Param(description = "the start ip of the portable IP range")
    private String startIp;

    @SerializedName(ApiConstants.END_IP)
    @Param(description = "the end ip of the portable IP range")
    private String endIp;

    @SerializedName(ApiConstants.PORTABLE_IP_ADDRESS)
    @Param(description = "List of portable IP and association with zone/network/vpc details that are part of GSLB rule", responseObject = PortableIpResponse.class)
    private List<PortableIpResponse> portableIpResponses;

    public void setId(final String id) {
        this.id = id;
    }

    public void setRegionId(final int regionId) {
        this.regionId = regionId;
    }

    public void setStartIp(final String startIp) {
        this.startIp = startIp;
    }

    public void setEndIp(final String endIp) {
        this.endIp = endIp;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    public void setVlan(final String vlan) {
        this.vlan = vlan;
    }

    public void setPortableIpResponses(final List<PortableIpResponse> portableIpResponses) {
        this.portableIpResponses = portableIpResponses;
    }
}
