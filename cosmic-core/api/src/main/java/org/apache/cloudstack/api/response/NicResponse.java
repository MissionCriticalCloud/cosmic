package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.vm.Nic;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = Nic.class)
public class NicResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "the ID of the nic")
    private String id;

    @SerializedName("networkid")
    @Param(description = "the ID of the corresponding network")
    private String networkId;

    @SerializedName("networkname")
    @Param(description = "the name of the corresponding network")
    private String networkName;

    @SerializedName(ApiConstants.NETMASK)
    @Param(description = "the netmask of the nic")
    private String netmask;

    @SerializedName(ApiConstants.GATEWAY)
    @Param(description = "the gateway of the nic")
    private String gateway;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "the ip address of the nic")
    private String ipaddress;

    @SerializedName("isolationuri")
    @Param(description = "the isolation uri of the nic")
    private String isolationUri;

    @SerializedName("broadcasturi")
    @Param(description = "the broadcast uri of the nic")
    private String broadcastUri;

    @SerializedName(ApiConstants.TRAFFIC_TYPE)
    @Param(description = "the traffic type of the nic")
    private String trafficType;

    @SerializedName(ApiConstants.TYPE)
    @Param(description = "the type of the nic")
    private String type;

    @SerializedName(ApiConstants.IS_DEFAULT)
    @Param(description = "true if nic is default, false otherwise")
    private Boolean isDefault;

    @SerializedName("macaddress")
    @Param(description = "true if nic is default, false otherwise")
    private String macAddress;

    @SerializedName(ApiConstants.IP6_GATEWAY)
    @Param(description = "the gateway of IPv6 network")
    private String ip6Gateway;

    @SerializedName(ApiConstants.IP6_CIDR)
    @Param(description = "the cidr of IPv6 network")
    private String ip6Cidr;

    @SerializedName(ApiConstants.IP6_ADDRESS)
    @Param(description = "the IPv6 address of network")
    private String ip6Address;

    @SerializedName("secondaryip")
    @Param(description = "the Secondary ipv4 addr of nic")
    private List<NicSecondaryIpResponse> secondaryIps;

    @SerializedName(ApiConstants.DEVICE_ID)
    @Param(description = "device id for the network when plugged into the virtual machine", since = "4.4")
    private String deviceId;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_ID)
    @Param(description = "Id of the vm to which the nic belongs")
    private String vmId;

    public void setVmId(final String vmId) {
        this.vmId = vmId;
    }

    public void setNetworkid(final String networkid) {
        this.networkId = networkid;
    }

    public void setNetworkName(final String networkname) {
        this.networkName = networkname;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    public void setIpaddress(final String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public void setIsolationUri(final String isolationUri) {
        this.isolationUri = isolationUri;
    }

    public void setBroadcastUri(final String broadcastUri) {
        this.broadcastUri = broadcastUri;
    }

    public void setTrafficType(final String trafficType) {
        this.trafficType = trafficType;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setIsDefault(final Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public void setIp6Gateway(final String ip6Gateway) {
        this.ip6Gateway = ip6Gateway;
    }

    public void setIp6Cidr(final String ip6Cidr) {
        this.ip6Cidr = ip6Cidr;
    }

    public void setIp6Address(final String ip6Address) {
        this.ip6Address = ip6Address;
    }

    public void setDeviceId(final String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        final String oid = this.getId();
        result = prime * result + ((oid == null) ? 0 : oid.hashCode());
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NicResponse other = (NicResponse) obj;
        final String oid = this.getId();
        if (oid == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!oid.equals(other.getId())) {
            return false;
        }
        return true;
    }

    public void setSecondaryIps(final List<NicSecondaryIpResponse> ipList) {
        this.secondaryIps = ipList;
    }
}
