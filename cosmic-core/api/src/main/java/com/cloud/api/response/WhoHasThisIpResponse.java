package com.cloud.api.response;

import com.cloud.api.ApiConstants;
import com.cloud.api.BaseResponse;
import com.cloud.network.Networks;
import com.cloud.serializer.Param;
import com.cloud.vm.VirtualMachine;

import java.net.URI;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class WhoHasThisIpResponse extends BaseResponse {

    @SerializedName(ApiConstants.VPC_NAME)
    @Param(description = "VPC name")
    private String vpcName;

    @SerializedName(ApiConstants.UUID)
    @Param(description = "UUID of the nic or users_ip_address")
    private String uuid;

    @SerializedName(ApiConstants.VPC_UUID)
    @Param(description = "VPC uuid")
    private String vpcUuid;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "IP address")
    private String ipAddress;

    @SerializedName(ApiConstants.NETWORK_NAME)
    @Param(description = "Network name")
    private String networkName;

    @SerializedName(ApiConstants.NETWORK_UUID)
    @Param(description = "Network uuid")
    private String networkUuid;

    @SerializedName(ApiConstants.ASSOCIATED_NETWORK_NAME)
    @Param(description = "Associated network name")
    private String associatedNetworkName;

    @SerializedName(ApiConstants.ASSOCIATED_NETWORK_UUID)
    @Param(description = "Associated network uuid")
    private String associatedNetworkUuid;

    @SerializedName(ApiConstants.MAC_ADDRESS)
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

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_UUID)
    @Param(description = "VM uuid")
    private String vmUuid;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_TYPE)
    @Param(description = "VM type")
    private VirtualMachine.Type vmType;

    @SerializedName(ApiConstants.DOMAIN_NAME)
    @Param(description = "Domain name")
    private String domainName;

    @SerializedName(ApiConstants.DOMAIN_UUID)
    @Param(description = "Domain uuid")
    private String domainUuid;

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(final String networkName) {
        this.networkName = networkName;
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

    public String getVpcName() {
        return vpcName;
    }

    public void setVpcName(final String vpcName) {
        this.vpcName = vpcName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getAssociatedNetworkName() {
        return associatedNetworkName;
    }

    public void setAssociatedNetworkName(final String associatedNetworkName) {
        this.associatedNetworkName = associatedNetworkName;
    }

    public String getVpcUuid() {
        return vpcUuid;
    }

    public void setVpcUuid(final String vpcUuid) {
        this.vpcUuid = vpcUuid;
    }

    public String getNetworkUuid() {
        return networkUuid;
    }

    public void setNetworkUuid(final String networkUuid) {
        this.networkUuid = networkUuid;
    }

    public String getAssociatedNetworkUuid() {
        return associatedNetworkUuid;
    }

    public void setAssociatedNetworkUuid(final String associatedNetworkUuid) {
        this.associatedNetworkUuid = associatedNetworkUuid;
    }

    public String getVmUuid() {
        return vmUuid;
    }

    public void setVmUuid(final String vmUuid) {
        this.vmUuid = vmUuid;
    }

    public void setVmType(final VirtualMachine.Type vmType) {
        this.vmType = vmType;
    }

    public VirtualMachine.Type getVmType() {
        return vmType;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public String getDomainUuid() {
        return domainUuid;
    }

    public void setDomainUuid(final String domainUuid) {
        this.domainUuid = domainUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

}
