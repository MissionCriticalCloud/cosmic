package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import com.cloud.vm.VirtualMachine;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = VirtualMachine.class)
public class SystemVmResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the ID of the system VM")
    private String id;

    @SerializedName("systemvmtype")
    @Param(description = "the system VM type")
    private String systemVmType;

    @SerializedName("jobid")
    @Param(description = "the job ID associated with the system VM. This is only displayed if the router listed is part of a currently running asynchronous job.")
    private String jobId;

    @SerializedName("jobstatus")
    @Param(description = "the job status associated with the system VM.  This is only displayed if the router listed is part of a currently running asynchronous job.")
    private Integer jobStatus;

    @SerializedName("zoneid")
    @Param(description = "the Zone ID for the system VM")
    private String zoneId;

    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the Zone name for the system VM")
    private String zoneName;

    @SerializedName("dns1")
    @Param(description = "the first DNS for the system VM")
    private String dns1;

    @SerializedName("dns2")
    @Param(description = "the second DNS for the system VM")
    private String dns2;

    @SerializedName("networkdomain")
    @Param(description = "the network domain for the system VM")
    private String networkDomain;

    @SerializedName("gateway")
    @Param(description = "the gateway for the system VM")
    private String gateway;

    @SerializedName("name")
    @Param(description = "the name of the system VM")
    private String name;

    @SerializedName("podid")
    @Param(description = "the Pod ID for the system VM")
    private String podId;

    @SerializedName("hostid")
    @Param(description = "the host ID for the system VM")
    private String hostId;

    @SerializedName("hostname")
    @Param(description = "the hostname for the system VM")
    private String hostName;

    @SerializedName("hypervisor")
    @Param(description = "the hypervisor on which the template runs")
    private String hypervisor;

    @SerializedName(ApiConstants.PRIVATE_IP)
    @Param(description = "the private IP address for the system VM")
    private String privateIp;

    @SerializedName(ApiConstants.PRIVATE_MAC_ADDRESS)
    @Param(description = "the private MAC address for the system VM")
    private String privateMacAddress;

    @SerializedName(ApiConstants.PRIVATE_NETMASK)
    @Param(description = "the private netmask for the system VM")
    private String privateNetmask;

    @SerializedName(ApiConstants.LINK_LOCAL_IP)
    @Param(description = "the link local IP address for the system vm")
    private String linkLocalIp;

    @SerializedName(ApiConstants.LINK_LOCAL_MAC_ADDRESS)
    @Param(description = "the link local MAC address for the system vm")
    private String linkLocalMacAddress;

    @SerializedName(ApiConstants.LINK_LOCAL_MAC_NETMASK)
    @Param(description = "the link local netmask for the system vm")
    private String linkLocalNetmask;

    @SerializedName("publicip")
    @Param(description = "the public IP address for the system VM")
    private String publicIp;

    @SerializedName("publicmacaddress")
    @Param(description = "the public MAC address for the system VM")
    private String publicMacAddress;

    @SerializedName("publicnetmask")
    @Param(description = "the public netmask for the system VM")
    private String publicNetmask;

    @SerializedName("templateid")
    @Param(description = "the template ID for the system VM")
    private String templateId;

    @SerializedName("created")
    @Param(description = "the date and time the system VM was created")
    private Date created;

    @SerializedName("state")
    @Param(description = "the state of the system VM")
    private String state;

    @SerializedName("activeviewersessions")
    @Param(description = "the number of active console sessions for the console proxy system vm")
    private Integer activeViewerSessions;

    // private Long objectId;

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

    public String getSystemVmType() {
        return systemVmType;
    }

    public void setSystemVmType(final String systemVmType) {
        this.systemVmType = systemVmType;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
    }

    public String getDns1() {
        return dns1;
    }

    public void setDns1(final String dns1) {
        this.dns1 = dns1;
    }

    public String getDns2() {
        return dns2;
    }

    public void setDns2(final String dns2) {
        this.dns2 = dns2;
    }

    public String getNetworkDomain() {
        return networkDomain;
    }

    public void setNetworkDomain(final String networkDomain) {
        this.networkDomain = networkDomain;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getPodId() {
        return podId;
    }

    public void setPodId(final String podId) {
        this.podId = podId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(final String hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public String getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(final String hypervisor) {
        this.hypervisor = hypervisor;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(final String privateIp) {
        this.privateIp = privateIp;
    }

    public String getPrivateMacAddress() {
        return privateMacAddress;
    }

    public void setPrivateMacAddress(final String privateMacAddress) {
        this.privateMacAddress = privateMacAddress;
    }

    public String getPrivateNetmask() {
        return privateNetmask;
    }

    public void setPrivateNetmask(final String privateNetmask) {
        this.privateNetmask = privateNetmask;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(final String publicIp) {
        this.publicIp = publicIp;
    }

    public String getPublicMacAddress() {
        return publicMacAddress;
    }

    public void setPublicMacAddress(final String publicMacAddress) {
        this.publicMacAddress = publicMacAddress;
    }

    public String getPublicNetmask() {
        return publicNetmask;
    }

    public void setPublicNetmask(final String publicNetmask) {
        this.publicNetmask = publicNetmask;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(final String templateId) {
        this.templateId = templateId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public Integer getActiveViewerSessions() {
        return activeViewerSessions;
    }

    public void setActiveViewerSessions(final Integer activeViewerSessions) {
        this.activeViewerSessions = activeViewerSessions;
    }

    public String getLinkLocalIp() {
        return linkLocalIp;
    }

    public void setLinkLocalIp(final String linkLocalIp) {
        this.linkLocalIp = linkLocalIp;
    }

    public String getLinkLocalMacAddress() {
        return linkLocalMacAddress;
    }

    public void setLinkLocalMacAddress(final String linkLocalMacAddress) {
        this.linkLocalMacAddress = linkLocalMacAddress;
    }

    public String getLinkLocalNetmask() {
        return linkLocalNetmask;
    }

    public void setLinkLocalNetmask(final String linkLocalNetmask) {
        this.linkLocalNetmask = linkLocalNetmask;
    }
}
