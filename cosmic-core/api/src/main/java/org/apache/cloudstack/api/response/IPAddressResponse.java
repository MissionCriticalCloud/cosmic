package org.apache.cloudstack.api.response;

import com.cloud.network.IpAddress;
import com.cloud.serializer.Param;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = IpAddress.class)
public class IPAddressResponse extends BaseResponse implements ControlledEntityResponse {
    @SerializedName(ApiConstants.ID)
    @Param(description = "public IP address id")
    private String id;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "public IP address")
    private String ipAddress;

    @SerializedName("allocated")
    @Param(description = "date the public IP address was acquired")
    private Date allocated;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the ID of the zone the public IP address belongs to")
    private String zoneId;

    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the name of the zone the public IP address belongs to")
    private String zoneName;

    @SerializedName("issourcenat")
    @Param(description = "true if the IP address is a source nat address, false otherwise")
    private Boolean sourceNat;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account the public IP address is associated with")
    private String accountName;

    @SerializedName(ApiConstants.PROJECT_ID)
    @Param(description = "the project id of the ipaddress")
    private String projectId;

    @SerializedName(ApiConstants.PROJECT)
    @Param(description = "the project name of the address")
    private String projectName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID the public IP address is associated with")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain the public IP address is associated with")
    private String domainName;

    @SerializedName(ApiConstants.FOR_VIRTUAL_NETWORK)
    @Param(description = "the virtual network for the IP address")
    private Boolean forVirtualNetwork;

    @SerializedName(ApiConstants.VLAN_ID)
    @Param(description = "the ID of the VLAN associated with the IP address." + " This parameter is visible to ROOT admins only")
    private String vlanId;

    @SerializedName("vlanname")
    @Param(description = "the VLAN associated with the IP address")
    private String vlanName;

    @SerializedName("isstaticnat")
    @Param(description = "true if this ip is for static nat, false otherwise")
    private Boolean staticNat;

    @SerializedName(ApiConstants.IS_SYSTEM)
    @Param(description = "true if this ip is system ip (was allocated as a part of deployVm or createLbRule)")
    private Boolean isSystem;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_ID)
    @Param(description = "virutal machine id the ip address is assigned to (not null only for static nat Ip)")
    private String virtualMachineId;

    @SerializedName("vmipaddress")
    @Param(description = "virutal machine (dnat) ip address (not null only for static nat Ip)")
    private String virtualMachineIp;

    @SerializedName("virtualmachinename")
    @Param(description = "virutal machine name the ip address is assigned to (not null only for static nat Ip)")
    private String virtualMachineName;

    @SerializedName("virtualmachinedisplayname")
    @Param(description = "virutal machine display name the ip address is assigned to (not null only for static nat Ip)")
    private String virtualMachineDisplayName;

    @SerializedName(ApiConstants.ASSOCIATED_NETWORK_ID)
    @Param(description = "the ID of the Network associated with the IP address")
    private String associatedNetworkId;

    @SerializedName(ApiConstants.ASSOCIATED_NETWORK_NAME)
    @Param(description = "the name of the Network associated with the IP address")
    private String associatedNetworkName;

    @SerializedName(ApiConstants.NETWORK_ID)
    @Param(description = "the ID of the Network where ip belongs to")
    private String networkId;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "State of the ip address. Can be: Allocatin, Allocated and Releasing")
    private String state;

    @SerializedName(ApiConstants.PHYSICAL_NETWORK_ID)
    @Param(description = "the physical network this belongs to")
    private String physicalNetworkId;

    @SerializedName(ApiConstants.PURPOSE)
    @Param(description = "purpose of the IP address. In Acton this value is not null for Ips with isSystem=true, and can have either StaticNat or LB value")
    private String purpose;

    @SerializedName(ApiConstants.VPC_ID)
    @Param(description = "VPC the ip belongs to")
    private String vpcId;
    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the list of resource tags associated with ip address", responseObject = ResourceTagResponse.class)
    private List<ResourceTagResponse> tags;

    @SerializedName(ApiConstants.IS_PORTABLE)
    @Param(description = "is public IP portable across the zones")
    private Boolean isPortable;

    @SerializedName(ApiConstants.FOR_DISPLAY)
    @Param(description = "is public ip for display to the regular user", since = "4.4", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    /*
        @SerializedName(ApiConstants.JOB_ID) @Param(description="shows the current pending asynchronous job ID. This tag is not returned if no current pending jobs are acting on
         the volume")
        private IdentityProxy jobId = new IdentityProxy("async_job");
    */

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
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

    public void setAllocated(final Date allocated) {
        this.allocated = allocated;
    }

    public void setZoneId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public void setZoneName(final String zoneName) {
        this.zoneName = zoneName;
    }

    public void setSourceNat(final Boolean sourceNat) {
        this.sourceNat = sourceNat;
    }

    @Override
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
    }

    @Override
    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    @Override
    public void setDomainId(final String domainId) {
        this.domainId = domainId;
    }

    @Override
    public void setDomainName(final String domainName) {
        this.domainName = domainName;
    }

    public void setForVirtualNetwork(final Boolean forVirtualNetwork) {
        this.forVirtualNetwork = forVirtualNetwork;
    }

    public void setVlanId(final String vlanId) {
        this.vlanId = vlanId;
    }

    public void setVlanName(final String vlanName) {
        this.vlanName = vlanName;
    }

    public void setStaticNat(final Boolean staticNat) {
        this.staticNat = staticNat;
    }

    public void setAssociatedNetworkId(final String networkId) {
        this.associatedNetworkId = networkId;
    }

    public void setNetworkId(final String networkId) {
        this.networkId = networkId;
    }

    public void setVirtualMachineId(final String virtualMachineId) {
        this.virtualMachineId = virtualMachineId;
    }

    public void setVirtualMachineIp(final String virtualMachineIp) {
        this.virtualMachineIp = virtualMachineIp;
    }

    public void setVirtualMachineName(final String virtualMachineName) {
        this.virtualMachineName = virtualMachineName;
    }

    public void setVirtualMachineDisplayName(final String virtualMachineDisplayName) {
        this.virtualMachineDisplayName = virtualMachineDisplayName;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setPhysicalNetworkId(final String physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public void setIsSystem(final Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    public void setVpcId(final String vpcId) {
        this.vpcId = vpcId;
    }

    public void setTags(final List<ResourceTagResponse> tags) {
        this.tags = tags;
    }

    public void setAssociatedNetworkName(final String associatedNetworkName) {
        this.associatedNetworkName = associatedNetworkName;
    }

    public void setPortable(final Boolean portable) {
        this.isPortable = portable;
    }

    public void setForDisplay(final Boolean forDisplay) {
        this.forDisplay = forDisplay;
    }
}
