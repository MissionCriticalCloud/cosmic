package com.cloud.api.command.admin.vpc;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseAsyncCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.VpcOfferingResponse;
import com.cloud.event.EventTypes;
import com.cloud.network.vpc.VpcOffering;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateVPCOffering", group = APICommandGroup.VPCService, description = "Updates VPC offering", responseObject = VpcOfferingResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateVPCOfferingCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateVPCOfferingCmd.class.getName());
    private static final String s_name = "updatevpcofferingresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VpcOfferingResponse.class, required = true, description = "the id of the VPC offering")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "the name of the VPC offering")
    private String vpcOffName;

    @Parameter(name = ApiConstants.DISPLAY_TEXT, type = CommandType.STRING, description = "the display text of the VPC offering")
    private String displayText;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "update state for the VPC offering; " + "supported states - Enabled/Disabled")
    private String state;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final VpcOffering result = _vpcProvSvc.updateVpcOffering(getId(), getVpcOfferingName(), getDisplayText(), getState());
        if (result != null) {
            final VpcOfferingResponse response = _responseGenerator.createVpcOfferingResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update VPC offering");
        }
    }

    public Long getId() {
        return id;
    }

    public String getVpcOfferingName() {
        return vpcOffName;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getState() {
        return state;
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
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VPC_OFFERING_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Updating VPC offering id=" + getId();
    }
}
