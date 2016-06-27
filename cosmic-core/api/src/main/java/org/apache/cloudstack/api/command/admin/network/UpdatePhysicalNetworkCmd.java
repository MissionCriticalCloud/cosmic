package org.apache.cloudstack.api.command.admin.network;

import com.cloud.event.EventTypes;
import com.cloud.network.PhysicalNetwork;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.PhysicalNetworkResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updatePhysicalNetwork", description = "Updates a physical network", responseObject = PhysicalNetworkResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdatePhysicalNetworkCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdatePhysicalNetworkCmd.class.getName());

    private static final String s_name = "updatephysicalnetworkresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = PhysicalNetworkResponse.class, required = true, description = "physical network id")
    private Long id;

    @Parameter(name = ApiConstants.NETWORK_SPEED, type = CommandType.STRING, description = "the speed for the physical network[1G/10G]")
    private String speed;

    @Parameter(name = ApiConstants.TAGS, type = CommandType.LIST, collectionType = CommandType.STRING, description = "Tag the physical network")
    private List<String> tags;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "Enabled/Disabled")
    private String state;

    @Parameter(name = ApiConstants.VLAN, type = CommandType.STRING, description = "the VLAN for the physical network")
    private String vlan;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final PhysicalNetwork result = _networkService.updatePhysicalNetwork(getId(), getNetworkSpeed(), getTags(), getVlan(), getState());
        if (result != null) {
            final PhysicalNetworkResponse response = _responseGenerator.createPhysicalNetworkResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        }
    }

    public Long getId() {
        return id;
    }

    public String getNetworkSpeed() {
        return speed;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getVlan() {
        return vlan;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getState() {
        return state;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PHYSICAL_NETWORK_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Updating Physical network: " + getId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.PhysicalNetwork;
    }
}
