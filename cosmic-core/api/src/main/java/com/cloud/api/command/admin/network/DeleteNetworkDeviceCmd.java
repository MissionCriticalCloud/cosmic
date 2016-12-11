package com.cloud.api.command.admin.network;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.ExternalNetworkDeviceManager;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteNetworkDevice", description = "Deletes network device.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteNetworkDeviceCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteNetworkDeviceCmd.class);
    private static final String s_name = "deletenetworkdeviceresponse";

    @Inject
    ExternalNetworkDeviceManager nwDeviceMgr;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = HostResponse.class, required = true, description = "Id of network device to delete")
    private Long id;

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException,
            ResourceAllocationException {
        try {
            final boolean result = nwDeviceMgr.deleteNetworkDevice(this);
            if (result) {
                final SuccessResponse response = new SuccessResponse(getCommandName());
                response.setResponseName(getCommandName());
                this.setResponseObject(response);
            } else {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete network device:" + getId());
            }
        } catch (final InvalidParameterValueException ipve) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, ipve.getMessage());
        } catch (final CloudRuntimeException cre) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, cre.getMessage());
        }
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        // TODO Auto-generated method stub
        return 0;
    }
}
