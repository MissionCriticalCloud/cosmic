package com.cloud.api.command.admin.user;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.UserResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.user.Account;
import com.cloud.legacymodel.user.User;
import com.cloud.legacymodel.user.UserAccount;
import com.cloud.region.RegionService;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "enableUser", group = APICommandGroup.UserService, description = "Enables a user account", responseObject = UserResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class EnableUserCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(EnableUserCmd.class.getName());
    private static final String s_name = "enableuserresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Inject
    RegionService _regionService;
    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = UserResponse.class, required = true, description = "Enables user by user ID.")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        CallContext.current().setEventDetails("UserId: " + getId());
        final UserAccount user = _regionService.enableUser(this);

        if (user != null) {
            final UserResponse response = _responseGenerator.createUserResponse(user);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to enable user");
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
        final User user = _entityMgr.findById(User.class, getId());
        if (user != null) {
            return user.getAccountId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are tracked
    }

    public Long getId() {
        return id;
    }
}
