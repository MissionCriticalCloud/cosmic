package org.apache.cloudstack.api.command.admin.vpc;

import com.cloud.network.vpc.Vpc;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.user.vpc.UpdateVPCCmd;
import org.apache.cloudstack.api.response.VpcResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateVPC", description = "Updates a VPC", responseObject = VpcResponse.class, responseView = ResponseView.Full, entityType = {Vpc.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateVPCCmdByAdmin extends UpdateVPCCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateVPCCmdByAdmin.class.getName());

    @Override
    public void execute() {
        final Vpc result = _vpcService.updateVpc(getId(), getVpcName(), getDisplayText(), getCustomId(), getDisplayVpc());
        if (result != null) {
            final VpcResponse response = _responseGenerator.createVpcResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update VPC");
        }
    }
}
