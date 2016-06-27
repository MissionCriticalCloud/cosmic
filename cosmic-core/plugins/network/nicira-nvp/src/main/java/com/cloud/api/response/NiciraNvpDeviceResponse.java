//

//

package com.cloud.api.response;

import com.cloud.network.NiciraNvpDeviceVO;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = NiciraNvpDeviceVO.class)
public class NiciraNvpDeviceResponse extends BaseResponse {
    @SerializedName(ApiConstants.NICIRA_NVP_DEVICE_ID)
    @Param(description = "device id of the Nicire Nvp")
    private String id;

    @SerializedName(ApiConstants.PHYSICAL_NETWORK_ID)
    @Param(description = "the physical network to which this Nirica Nvp belongs to")
    private String physicalNetworkId;

    @SerializedName(ApiConstants.PROVIDER)
    @Param(description = "name of the provider")
    private String providerName;

    @SerializedName(ApiConstants.NICIRA_NVP_DEVICE_NAME)
    @Param(description = "device name")
    private String deviceName;

    @SerializedName(ApiConstants.HOST_NAME)
    @Param(description = "the controller Ip address")
    private String hostName;

    @SerializedName(ApiConstants.NICIRA_NVP_TRANSPORT_ZONE_UUID)
    @Param(description = "the transport zone Uuid")
    private String transportZoneUuid;

    @SerializedName(ApiConstants.NICIRA_NVP_GATEWAYSERVICE_UUID)
    @Param(description = "this L3 gateway service Uuid")
    private String l3GatewayServiceUuid;

    public void setId(final String nvpDeviceId) {
        this.id = nvpDeviceId;
    }

    public void setPhysicalNetworkId(final String physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public void setTransportZoneUuid(final String transportZoneUuid) {
        this.transportZoneUuid = transportZoneUuid;
    }

    public void setL3GatewayServiceUuid(final String l3GatewayServiceUuid) {
        this.l3GatewayServiceUuid = l3GatewayServiceUuid;
    }
}
