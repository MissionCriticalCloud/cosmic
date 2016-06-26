package org.apache.cloudstack.api.command.admin.user;

import com.cloud.user.Account;
import com.cloud.user.User;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.RegisterResponse;
import org.apache.cloudstack.api.response.UserResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "registerUserKeys",
        responseObject = RegisterResponse.class,
        description = "This command allows a user to register for the developer API, returning a secret key and an API key. This request is made through the integration API " +
                "port, so it is a privileged command and must be made on behalf of a user. It is up to the implementer just how the username and password are entered, and then " +
                "how that translates to an integration API request. Both secret key and API key should be returned to the user",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class RegisterCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RegisterCmd.class.getName());

    private static final String s_name = "registeruserkeysresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = UserResponse.class, required = true, description = "User id")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final String[] keys = _accountService.createApiKeyAndSecretKey(this);
        final RegisterResponse response = new RegisterResponse();
        if (keys != null) {
            response.setApiKey(keys[0]);
            response.setSecretKey(keys[1]);
        }
        response.setObjectName("userkeys");
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public long getEntityOwnerId() {
        final User user = _entityMgr.findById(User.class, getId());
        if (user != null) {
            return user.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}
