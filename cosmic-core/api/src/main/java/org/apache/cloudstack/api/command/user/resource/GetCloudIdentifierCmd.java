package org.apache.cloudstack.api.command.user.resource;

import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.CloudIdentifierResponse;
import org.apache.cloudstack.api.response.UserResponse;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "getCloudIdentifier", description = "Retrieves a cloud identifier.", responseObject = CloudIdentifierResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class GetCloudIdentifierCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(GetCloudIdentifierCmd.class.getName());
    private static final String s_name = "getcloudidentifierresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.USER_ID,
            type = CommandType.UUID,
            entityType = UserResponse.class,
            required = true,
            description = "the user ID for the cloud identifier")
    private Long userid;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getUserId() {
        return userid;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final ArrayList<String> result = _mgr.getCloudIdentifierResponse(userid);
        final CloudIdentifierResponse response = new CloudIdentifierResponse();
        if (result != null) {
            response.setCloudIdentifier(result.get(0));
            response.setSignature(result.get(1));
            response.setObjectName("cloudidentifier");
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to get cloud identifier");
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
}
