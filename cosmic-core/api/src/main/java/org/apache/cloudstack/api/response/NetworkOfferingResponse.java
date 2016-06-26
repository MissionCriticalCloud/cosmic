package org.apache.cloudstack.api.response;

import com.cloud.offering.NetworkOffering;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = NetworkOffering.class)
public class NetworkOfferingResponse extends BaseResponse {
    @SerializedName("id")
    @Param(description = "the id of the network offering")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the network offering")
    private String name;

    @SerializedName(ApiConstants.DISPLAY_TEXT)
    @Param(description = "an alternate display text of the network offering.")
    private String displayText;

    @SerializedName(ApiConstants.TAGS)
    @Param(description = "the tags for the network offering")
    private String tags;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this network offering was created")
    private Date created;

    @SerializedName(ApiConstants.TRAFFIC_TYPE)
    @Param(description = "the traffic type for the network offering, supported types are Public, Management, Control, Guest, Vlan or Storage.")
    private String trafficType;

    @SerializedName(ApiConstants.IS_DEFAULT)
    @Param(description = "true if network offering is default, false otherwise")
    private Boolean isDefault;

    @SerializedName(ApiConstants.SPECIFY_VLAN)
    @Param(description = "true if network offering supports vlans, false otherwise")
    private Boolean specifyVlan;

    @SerializedName(ApiConstants.CONSERVE_MODE)
    @Param(description = "true if network offering is ip conserve mode enabled")
    private Boolean conserveMode;

    @SerializedName(ApiConstants.SPECIFY_IP_RANGES)
    @Param(description = "true if network offering supports specifying ip ranges, false otherwise")
    private Boolean specifyIpRanges;

    @SerializedName(ApiConstants.AVAILABILITY)
    @Param(description = "availability of the network offering")
    private String availability;

    @SerializedName(ApiConstants.NETWORKRATE)
    @Param(description = "data transfer rate in megabits per second allowed.")
    private Integer networkRate;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "state of the network offering. Can be Disabled/Enabled/Inactive")
    private String state;

    @SerializedName(ApiConstants.GUEST_IP_TYPE)
    @Param(description = "guest type of the network offering, can be Shared or Isolated")
    private String guestIpType;

    @SerializedName(ApiConstants.SERVICE_OFFERING_ID)
    @Param(description = "the ID of the service offering used by virtual router provider")
    private String serviceOfferingId;

    @SerializedName(ApiConstants.SERVICE)
    @Param(description = "the list of supported services", responseObject = ServiceResponse.class)
    private List<ServiceResponse> services;

    @SerializedName(ApiConstants.FOR_VPC)
    @Param(description = "true if network offering can be used by VPC networks only")
    private Boolean forVpc;

    @SerializedName(ApiConstants.IS_PERSISTENT)
    @Param(description = "true if network offering supports persistent networks, false otherwise")
    private Boolean isPersistent;

    @SerializedName(ApiConstants.DETAILS)
    @Param(description = "additional key/value details tied with network offering", since = "4.2.0")
    private Map details;

    @SerializedName(ApiConstants.EGRESS_DEFAULT_POLICY)
    @Param(description = "true if guest network default egress policy is allow; false if default egress policy is deny")
    private Boolean egressDefaultPolicy;

    @SerializedName(ApiConstants.MAX_CONNECTIONS)
    @Param(description = "maximum number of concurrents connections to be handled by lb")
    private Integer concurrentConnections;

    @SerializedName(ApiConstants.SUPPORTS_STRECHED_L2_SUBNET)
    @Param(description = "true if network offering supports network that span multiple zones", since = "4.4")
    private Boolean supportsStrechedL2Subnet;

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDisplayText(final String displayText) {
        this.displayText = displayText;
    }

    public void setTags(final String tags) {
        this.tags = tags;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public void setTrafficType(final String trafficType) {
        this.trafficType = trafficType;
    }

    public void setIsDefault(final Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setSpecifyVlan(final Boolean specifyVlan) {
        this.specifyVlan = specifyVlan;
    }

    public void setConserveMode(final Boolean conserveMode) {
        this.conserveMode = conserveMode;
    }

    public void setAvailability(final String availability) {
        this.availability = availability;
    }

    public void setNetworkRate(final Integer networkRate) {
        this.networkRate = networkRate;
    }

    public void setServices(final List<ServiceResponse> services) {
        this.services = services;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public void setGuestIpType(final String type) {
        this.guestIpType = type;
    }

    public void setServiceOfferingId(final String serviceOfferingId) {
        this.serviceOfferingId = serviceOfferingId;
    }

    public void setSpecifyIpRanges(final Boolean specifyIpRanges) {
        this.specifyIpRanges = specifyIpRanges;
    }

    public void setForVpc(final Boolean forVpc) {
        this.forVpc = forVpc;
    }

    public void setIsPersistent(final Boolean isPersistent) {
        this.isPersistent = isPersistent;
    }

    public void setDetails(final Map details) {
        this.details = details;
    }

    public void setEgressDefaultPolicy(final Boolean egressDefaultPolicy) {
        this.egressDefaultPolicy = egressDefaultPolicy;
    }

    public void setConcurrentConnections(final Integer concurrentConnections) {
        this.concurrentConnections = concurrentConnections;
    }

    public void setSupportsStrechedL2Subnet(final Boolean supportsStrechedL2Subnet) {
        this.supportsStrechedL2Subnet = supportsStrechedL2Subnet;
    }
}
