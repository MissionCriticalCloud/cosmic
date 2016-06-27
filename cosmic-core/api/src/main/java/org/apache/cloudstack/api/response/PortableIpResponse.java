package org.apache.cloudstack.api.response;

import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;
import org.apache.cloudstack.region.PortableIp;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = PortableIp.class)
public class PortableIpResponse extends BaseResponse {

    @SerializedName(ApiConstants.REGION_ID)
    @Param(description = "Region Id in which global load balancer is created")
    private Integer regionId;

    @SerializedName(ApiConstants.IP_ADDRESS)
    @Param(description = "public IP address")
    private String ipAddress;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the ID of the zone the public IP address belongs to")
    private String zoneId;

    @SerializedName(ApiConstants.NETWORK_ID)
    @Param(description = "the ID of the Network where ip belongs to")
    private String networkId;

    @SerializedName(ApiConstants.VPC_ID)
    @Param(description = "VPC the ip belongs to")
    private String vpcId;

    @SerializedName(ApiConstants.PHYSICAL_NETWORK_ID)
    @Param(description = "the physical network this belongs to")
    private String physicalNetworkId;

    @SerializedName(ApiConstants.ACCOUNT_ID)
    @Param(description = "the account ID the portable IP address is associated with")
    private String accountId;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID the portable IP address is associated with")
    private String domainId;

    @SerializedName("allocated")
    @Param(description = "date the portal IP address was acquired")
    private Date allocated;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "State of the ip address. Can be: Allocatin, Allocated and Releasing")
    private String state;

    public void setRegionId(final Integer regionId) {
        this.regionId = regionId;
    }

    public void setAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setAssociatedDataCenterId(final String zoneId) {
        this.zoneId = zoneId;
    }

    public void setAssociatedWithNetworkId(final String networkId) {
        this.networkId = networkId;
    }

    public void setAssociatedWithVpcId(final String vpcId) {
        this.vpcId = vpcId;
    }

    public void setPhysicalNetworkId(final String physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public void setAllocatedToAccountId(final String accountId) {
        this.accountId = accountId;
    }

    public void setAllocatedInDomainId(final String domainId) {
        this.domainId = domainId;
    }

    public void setAllocatedTime(final Date allocatedTimetime) {
        this.allocated = allocatedTimetime;
    }

    public void setState(final String state) {
        this.state = state;
    }
}
