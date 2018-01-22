package com.cloud.api.command.admin.vpc;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vpc.CreateVPCCmd;
import com.cloud.api.response.VpcResponse;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.vpc.Vpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createVPC", group = APICommandGroup.VPCService, description = "Creates a VPC", responseObject = VpcResponse.class, responseView = ResponseView.Full, entityType = {Vpc.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateVPCCmdByAdmin extends CreateVPCCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateVPCCmdByAdmin.class.getName());

    @Override
    public void execute() {
        Vpc vpc = null;
        try {
            if (isStart()) {
                _vpcService.startVpc(getEntityId(), true);
            } else {
                s_logger.debug("Not starting VPC as " + ApiConstants.START + "=false was passed to the API");
            }
            vpc = _entityMgr.findById(Vpc.class, getEntityId());
        } catch (final ResourceUnavailableException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.RESOURCE_UNAVAILABLE_ERROR, ex.getMessage());
        } catch (final ConcurrentOperationException ex) {
            s_logger.warn("Exception: ", ex);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, ex.getMessage());
        } catch (final InsufficientCapacityException ex) {
            s_logger.info(ex.toString());
            throw new ServerApiException(ApiErrorCode.INSUFFICIENT_CAPACITY_ERROR, ex.getMessage());
        }

        if (vpc != null) {
            final VpcResponse response = _responseGenerator.createVpcResponse(ResponseView.Full, vpc);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create VPC");
        }
    }
}
