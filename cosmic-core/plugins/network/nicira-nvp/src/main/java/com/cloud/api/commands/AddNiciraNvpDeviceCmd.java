package com.cloud.api.commands;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.NiciraNvpDeviceResponse;
import com.cloud.api.response.PhysicalNetworkResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.legacymodel.exceptions.CloudRuntimeException;
import com.cloud.legacymodel.exceptions.ConcurrentOperationException;
import com.cloud.legacymodel.exceptions.InsufficientCapacityException;
import com.cloud.legacymodel.exceptions.InvalidParameterValueException;
import com.cloud.legacymodel.exceptions.ResourceAllocationException;
import com.cloud.legacymodel.exceptions.ResourceUnavailableException;
import com.cloud.network.NiciraNvpDeviceVO;
import com.cloud.network.element.NiciraNvpElementService;

import javax.inject.Inject;

@APICommand(name = "addNiciraNvpDevice", group = APICommandGroup.NiciraNVPService, responseObject = NiciraNvpDeviceResponse.class, description = "Adds a Nicira NVP device",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddNiciraNvpDeviceCmd extends BaseAsyncCmd {
    private static final String s_name = "addniciranvpdeviceresponse";
    @Inject
    protected NiciraNvpElementService niciraNvpElementService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.PHYSICAL_NETWORK_ID,
            type = CommandType.UUID,
            entityType = PhysicalNetworkResponse.class,
            required = true,
            description = "the Physical Network ID")
    private Long physicalNetworkId;

    @Parameter(name = ApiConstants.HOST_NAME, type = CommandType.STRING, required = true, description = "Hostname of ip address of the Nicira NVP Controller.")
    private String host;

    @Parameter(name = ApiConstants.USERNAME, type = CommandType.STRING, required = true, description = "Credentials to access the Nicira Controller API")
    private String username;

    @Parameter(name = ApiConstants.PASSWORD, type = CommandType.STRING, required = true, description = "Credentials to access the Nicira Controller API")
    private String password;

    @Parameter(name = ApiConstants.NICIRA_NVP_TRANSPORT_ZONE_UUID,
            type = CommandType.STRING,
            required = true,
            description = "The Transportzone UUID configured on the Nicira Controller")
    private String transportzoneuuid;

    @Parameter(name = ApiConstants.NICIRA_NVP_GATEWAYSERVICE_UUID,
            type = CommandType.STRING,
            required = false,
            description = "The L3 Gateway Service UUID configured on the Nicira Controller")
    private String l3gatewayserviceuuid;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getTransportzoneUuid() {
        return transportzoneuuid;
    }

    public String getL3GatewayServiceUuid() {
        return l3gatewayserviceuuid;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        try {
            final NiciraNvpDeviceVO niciraNvpDeviceVO = niciraNvpElementService.addNiciraNvpDevice(this);
            if (niciraNvpDeviceVO != null) {
                final NiciraNvpDeviceResponse response = niciraNvpElementService.createNiciraNvpDeviceResponse(niciraNvpDeviceVO);
                response.setObjectName("niciranvpdevice");
                response.setResponseName(getCommandName());
                setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to add Nicira NVP device due to internal error.");
            }
        } catch (final InvalidParameterValueException invalidParamExcp) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, invalidParamExcp.getMessage());
        } catch (final CloudRuntimeException runtimeExcp) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, runtimeExcp.getMessage());
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return CallContext.current().getCallingAccount().getId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_EXTERNAL_NVP_CONTROLLER_ADD;
    }

    @Override
    public String getEventDescription() {
        return "Adding a Nicira Nvp Controller";
    }
}
