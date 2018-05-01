package com.cloud.api.command.admin.network;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCreateCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.PhysicalNetworkResponse;
import com.cloud.api.response.ZoneResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.user.Account;
import com.cloud.network.PhysicalNetwork;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createPhysicalNetwork", group = APICommandGroup.NetworkService, description = "Creates a physical network", responseObject = PhysicalNetworkResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreatePhysicalNetworkCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreatePhysicalNetworkCmd.class.getName());

    private static final String s_name = "createphysicalnetworkresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ZONE_ID,
            type = CommandType.UUID,
            entityType = ZoneResponse.class,
            required = true,
            description = "the Zone ID for the physical network")
    private Long zoneId;

    @Parameter(name = ApiConstants.VLAN, type = CommandType.STRING, description = "the VLAN for the physical network")
    private String vlan;

    @Parameter(name = ApiConstants.NETWORK_SPEED, type = CommandType.STRING, description = "the speed for the physical network[1G/10G]")
    private String speed;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            entityType = DomainResponse.class,
            description = "domain ID of the account owning a physical network")
    private Long domainId;

    @Parameter(name = ApiConstants.BROADCAST_DOMAIN_RANGE,
            type = CommandType.STRING,
            description = "the broadcast domain range for the physical network[Pod or Zone]. In Acton release it can be Zone only in Advance zone, and Pod in Basic")
    private String broadcastDomainRange;

    @Parameter(name = ApiConstants.TAGS, type = CommandType.LIST, collectionType = CommandType.STRING, description = "Tag the physical network")
    private List<String> tags;

    @Parameter(name = ApiConstants.ISOLATION_METHODS,
            type = CommandType.LIST,
            collectionType = CommandType.STRING,
            description = "the isolation method for the physical network[VLAN/L3/GRE]")
    private List<String> isolationMethods;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "the name of the physical network")
    private String networkName;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PHYSICAL_NETWORK_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "creating Physical Network. Id: " + getEntityId();
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.PhysicalNetwork;
    }

    @Override
    public void execute() {
        CallContext.current().setEventDetails("Physical Network Id: " + getEntityId());
        final PhysicalNetwork result = _networkService.getCreatedPhysicalNetwork(getEntityId());
        if (result != null) {
            final PhysicalNetworkResponse response = _responseGenerator.createPhysicalNetworkResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create physical network");
        }
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
    public void create() throws ResourceAllocationException {
        final PhysicalNetwork result =
                _networkService.createPhysicalNetwork(getZoneId(), getVlan(), getNetworkSpeed(), getIsolationMethods(), getBroadcastDomainRange(), getDomainId(), getTags(),
                        getNetworkName());
        if (result != null) {
            setEntityId(result.getId());
            setEntityUuid(result.getUuid());
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create physical network entity");
        }
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getVlan() {
        return vlan;
    }

    public String getNetworkSpeed() {
        return speed;
    }

    public List<String> getIsolationMethods() {
        return isolationMethods;
    }

    public String getBroadcastDomainRange() {
        return broadcastDomainRange;
    }

    public Long getDomainId() {
        return domainId;
    }

    public List<String> getTags() {
        return tags;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getNetworkName() {
        return networkName;
    }

    @Override
    public String getCreateEventType() {
        return EventTypes.EVENT_PHYSICAL_NETWORK_CREATE;
    }

    @Override
    public String getCreateEventDescription() {
        return "creating Physical Network";
    }
}
