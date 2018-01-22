package com.cloud.api.command.admin.vpc;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.ServerApiException;
import com.cloud.api.command.user.vpc.UpdateVPCCmd;
import com.cloud.api.response.VpcResponse;
import com.cloud.network.vpc.Vpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateVPC", group = APICommandGroup.VPCService, description = "Updates a VPC", responseObject = VpcResponse.class, responseView = ResponseView.Full, entityType = {Vpc.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateVPCCmdByAdmin extends UpdateVPCCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateVPCCmdByAdmin.class.getName());

    @Override
    public void execute() {
        final Vpc result = _vpcService.updateVpc(getId(), getVpcName(), getDisplayText(), getCustomId(), getDisplayVpc(), getVpcOfferingId(), getSourceNatList(), getSyslogServerList());
        if (result != null) {
            final VpcResponse response = _responseGenerator.createVpcResponse(ResponseView.Full, result);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update VPC");
        }
    }
}
