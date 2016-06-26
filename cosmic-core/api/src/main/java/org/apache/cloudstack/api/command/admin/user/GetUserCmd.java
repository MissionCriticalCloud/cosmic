package org.apache.cloudstack.api.command.admin.user;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.user.UserAccount;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.UserResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "getUser", description = "Find user account by API key", responseObject = UserResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class GetUserCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(GetUserCmd.class.getName());

    private static final String s_name = "getuserresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.USER_API_KEY, type = CommandType.STRING, required = true, description = "API key of the user")
    private String apiKey;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final UserAccount result = _accountService.getUserByApiKey(getApiKey());
        if (result != null) {
            final UserResponse response = _responseGenerator.createUserResponse(result);
            response.setResponseName(getCommandName());
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new InvalidParameterValueException("User with specified API key does not exist");
        }
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return 0;
    }
}
