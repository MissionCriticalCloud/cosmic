package com.cloud.api.command.admin.vpc;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.PrivateGatewayResponse;
import com.cloud.api.response.SuccessResponse;
import com.cloud.context.CallContext;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.vpc.VpcGateway;
import com.cloud.utils.exception.InvalidParameterValueException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deletePrivateGateway", group = APICommandGroup.VPCService, description = "Deletes a Private gateway", responseObject = SuccessResponse.class, entityType = {VpcGateway.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeletePrivateGatewayCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeletePrivateGatewayCmd.class.getName());
    private static final String s_name = "deleteprivategatewayresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = PrivateGatewayResponse.class, required = true, description = "the ID of the private gateway")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PRIVATE_GATEWAY_DELETE;
    }

    @Override
    public String getEventDescription() {
        return ("Deleting private gateway id=" + id);
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.PrivateGateway;
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.vpcSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        final VpcGateway gateway = _vpcService.getVpcPrivateGateway(getId());
        if (gateway == null) {
            throw new InvalidParameterValueException("Invalid private gateway id");
        }
        return gateway.getVpcId();
    }

    public Long getId() {
        return id;
    }

    @Override
    public void execute() throws ResourceUnavailableException, ConcurrentOperationException {
        CallContext.current().setEventDetails("Network ACL Id: " + id);
        final boolean result = _vpcService.deleteVpcPrivateGateway(id);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete private gateway");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return CallContext.current().getCallingAccount().getId();
    }
}
