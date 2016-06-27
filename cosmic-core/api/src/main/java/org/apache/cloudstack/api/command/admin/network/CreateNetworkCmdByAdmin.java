package org.apache.cloudstack.api.command.admin.network;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.network.Network;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.network.CreateNetworkCmd;
import org.apache.cloudstack.api.response.NetworkResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createNetwork", description = "Creates a network", responseObject = NetworkResponse.class, responseView = ResponseView.Full, entityType = {Network.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateNetworkCmdByAdmin extends CreateNetworkCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateNetworkCmdByAdmin.class.getName());

    @Parameter(name = ApiConstants.VLAN, type = CommandType.STRING, description = "the ID or VID of the network")
    private String vlan;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getVlan() {
        return vlan;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    // an exception thrown by createNetwork() will be caught by the dispatcher.
    public void execute() throws InsufficientCapacityException, ConcurrentOperationException, ResourceAllocationException {
        final Network result = _networkService.createGuestNetwork(this);
        if (result != null) {
            final NetworkResponse response = _responseGenerator.createNetworkResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create network");
        }
    }
}
