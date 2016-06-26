package org.apache.cloudstack.api.response;

import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.serializer.Param;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

import java.util.List;

import com.google.gson.annotations.SerializedName;

@EntityReference(value = PhysicalNetworkServiceProvider.class)
public class ProviderResponse extends BaseResponse {

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the provider name")
    private String name;

    @SerializedName(ApiConstants.PHYSICAL_NETWORK_ID)
    @Param(description = "the physical network this belongs to")
    private String physicalNetworkId;

    @SerializedName(ApiConstants.DEST_PHYSICAL_NETWORK_ID)
    @Param(description = "the destination physical network")
    private String destinationPhysicalNetworkId;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "state of the network provider")
    private String state;

    @SerializedName(ApiConstants.ID)
    @Param(description = "uuid of the network provider")
    private String id;

    @SerializedName(ApiConstants.SERVICE_LIST)
    @Param(description = "services for this provider")
    private List<String> services;

    @SerializedName(ApiConstants.CAN_ENABLE_INDIVIDUAL_SERVICE)
    @Param(description = "true if individual services can be enabled/disabled")
    private Boolean canEnableIndividualServices;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPhysicalNetworkId(final String physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public String getphysicalNetworkId() {
        return physicalNetworkId;
    }

    public String getDestinationPhysicalNetworkId() {
        return destinationPhysicalNetworkId;
    }

    public void setDestinationPhysicalNetworkId(final String destPhysicalNetworkId) {
        this.destinationPhysicalNetworkId = destPhysicalNetworkId;
    }

    public String getState() {
        return this.state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String uuid) {
        this.id = uuid;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(final List<String> services) {
        this.services = services;
    }

    public Boolean getCanEnableIndividualServices() {
        return canEnableIndividualServices;
    }

    public void setCanEnableIndividualServices(final Boolean canEnableIndividualServices) {
        this.canEnableIndividualServices = canEnableIndividualServices;
    }
}
