package org.apache.cloudstack.api.response;

import com.cloud.network.rules.FirewallRule;
import com.cloud.serializer.Param;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = FirewallRule.class)
public class FirewallRuleResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the port forwarding rule")
    private String id;

    @SerializedName(ApiConstants.PRIVATE_START_PORT)
    @Param(description = "the starting port of port forwarding rule's private port range")
    private String privateStartPort;

    @SerializedName(ApiConstants.PRIVATE_END_PORT)
    @Param(description = "the ending port of port forwarding rule's private port range")
    private String privateEndPort;

    @SerializedName(ApiConstants.PROTOCOL)
    @Param(description = "the protocol of the port forwarding rule")
    private String protocol;

    @SerializedName(ApiConstants.PUBLIC_START_PORT)
    @Param(description = "the starting port of port forwarding rule's public port range")
    private String publicStartPort;

    @SerializedName(ApiConstants.PUBLIC_END_PORT)
    @Param(description = "the ending port of port forwarding rule's private port range")
    private String publicEndPort;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_ID)
    @Param(description = "the VM ID for the port forwarding rule")
    private String virtualMachineId;

    @SerializedName("virtualmachinename")
    @Param(description = "the VM name for the port forwarding rule")
    private String virtualMachineName;

    @SerializedName("virtualmachinedisplayname")
    @Param(description = "the VM display name for the port forwarding rule")
    private String virtualMachineDisplayName;

    @SerializedName(ApiConstants.IP_ADDRESS_ID)
    @Param(description = "the public ip address id for the port forwarding rule")
    private String publicIpAddressId;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "the public ip address for the port forwarding rule")
    private String publicIpAddress;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the rule")
    private String state;

    @SerializedName(ApiConstants.CIDR_LIST)
    @Param(description = "the cidr list to forward traffic from")
    private String cidrList;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with the rule", responseObject = ResourceTagResponse.class)
    private List<ResourceTagResponse> tags;

    @SerializedName(ApiConstants.VM_GUEST_IP)
    @Param(description = "the vm ip address for the port forwarding rule")
    private String destNatVmIp;

    @SerializedName(ApiConstants.NETWORK_ID)
    @Param(description = "the id of the guest network the port forwarding rule belongs to")
    private String networkId;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is firewall for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    public String getDestNatVmIp() {
        return destNatVmIp;
    }

    public void setDestNatVmIp(final String destNatVmIp) {
        this.destNatVmIp = destNatVmIp;
    }

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

    public String getPrivateStartPort() {
        return privateStartPort;
    }

    public void setPrivateStartPort(final String privatePort) {
        this.privateStartPort = privatePort;
    }

    public String getPrivateEndPort() {
        return privateEndPort;
    }

    public void setPrivateEndPort(final String privatePort) {
        this.privateEndPort = privatePort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public String getPublicStartPort() {
        return publicStartPort;
    }

    public void setPublicStartPort(final String publicPort) {
        this.publicStartPort = publicPort;
    }

    public String getPublicEndPort() {
        return publicEndPort;
    }

    public void setPublicEndPort(final String publicPort) {
        this.publicEndPort = publicPort;
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

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(final String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getPublicIpAddressId() {
        return publicIpAddressId;
    }

    public void setPublicIpAddressId(final String publicIpAddressId) {
        this.publicIpAddressId = publicIpAddressId;
    }

    public String getCidrList() {
        return cidrList;
    }

    public void setCidrList(final String cidrs) {
        this.cidrList = cidrs;
    }

    public void setTags(final List<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void setNetworkId(final String networkId) {
        this.networkId = networkId;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
