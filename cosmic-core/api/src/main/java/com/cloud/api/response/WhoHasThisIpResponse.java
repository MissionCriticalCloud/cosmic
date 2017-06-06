package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.network.IpAddress;
import com.cloud.network.Networks;
import com.cloud.serializer.Param;
import com.cloud.utils.net.Ip;
import com.cloud.vm.Nic;

import java.net.URI;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class WhoHasThisIpResponse extends BaseResponse {

    @SerializedName(ApiConstants.VPC_NAME)
    @Param(description = "VPC name")
    private String vpcname;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "IP address")
    private String ipaddress;

    @SerializedName(ApiConstants.NETWORK_NAME)
    @Param(description = "Network name")
    private String networkname;

    @SerializedName(ApiConstants.ASSOCIATED_NETWORK_NAME)
    @Param(description = "Network name")
    private String associatedNetworkName;

    @SerializedName(ApiConstants.PRIVATE_MAC_ADDRESS)
    @Param(description = "Nics MAC Address")
    private String macAddress;

    @SerializedName(ApiConstants.NETMASK)
    @Param(description = "Netmask")
    private String netmask;

    @SerializedName(ApiConstants.BROADCAST_URI)
    @Param(description = "Broadcast uri")
    private URI broadcastUri;

    @SerializedName(ApiConstants.MODE)
    @Param(description = "Mode")
    private Networks.Mode mode;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "State")
    private String state;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "Created")
    private Date created;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_NAME)
    @Param(description = "VM Name")
    private String vmName;

    public String getNetworkname() {
        return networkname;
    }

    public void setNetworkname(final String networkname) {
        this.networkname = networkname;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(final String macAddress) {
        this.macAddress = macAddress;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(final String netmask) {
        this.netmask = netmask;
    }

    public URI getBroadcastUri() {
        return broadcastUri;
    }

    public void setBroadcastUri(final URI broadcastUri) {
        this.broadcastUri = broadcastUri;
    }

    public Networks.Mode getMode() {
        return mode;
    }

    public void setMode(final Networks.Mode mode) {
        this.mode = mode;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(final String vmName) {
        this.vmName = vmName;
    }

    public String getVpcname() {
        return vpcname;
    }

    public void setVpcname(final String vpcname) {
        this.vpcname = vpcname;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(final String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getAssociatedNetworkName() {
        return associatedNetworkName;
    }

    public void setAssociatedNetworkName(final String associatedNetworkName) {
        this.associatedNetworkName = associatedNetworkName;
    }

}
