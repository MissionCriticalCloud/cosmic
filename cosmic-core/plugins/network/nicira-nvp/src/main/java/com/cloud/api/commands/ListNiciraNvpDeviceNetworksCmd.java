package com.cloud.api.commands;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.NetworkResponse;
import com.cloud.api.response.NiciraNvpDeviceResponse;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.element.NiciraNvpElementService;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.exception.InvalidParameterValueException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listNiciraNvpDeviceNetworks", group = APICommandGroup.NetworkService, responseObject = NetworkResponse.class, description = "lists network that are using a nicira nvp device",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListNiciraNvpDeviceNetworksCmd extends BaseListCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(ListNiciraNvpDeviceNetworksCmd.class.getName());
    private static final String s_name = "listniciranvpdevicenetworks";
    @Inject
    protected NiciraNvpElementService niciraNvpElementService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NICIRA_NVP_DEVICE_ID,
            type = CommandType.UUID,
            entityType = NiciraNvpDeviceResponse.class,
            required = true,
            description = "nicira nvp device ID")
    private Long niciraNvpDeviceId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getNiciraNvpDeviceId() {
        return niciraNvpDeviceId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        try {
            final List<? extends Network> networks = niciraNvpElementService.listNiciraNvpDeviceNetworks(this);
            final ListResponse<NetworkResponse> response = new ListResponse<>();
            final List<NetworkResponse> networkResponses = new ArrayList<>();

            if (networks != null && !networks.isEmpty()) {
                for (final Network network : networks) {
                    final NetworkResponse networkResponse = _responseGenerator.createNetworkResponse(ResponseView.Full, network);
                    networkResponses.add(networkResponse);
                }
            }

            response.setResponses(networkResponses);
            response.setResponseName(getCommandName());
            setResponseObject(response);
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
}
