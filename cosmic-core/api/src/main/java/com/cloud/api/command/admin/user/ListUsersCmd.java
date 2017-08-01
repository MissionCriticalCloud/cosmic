package com.cloud.api.command.admin.user;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListAccountResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.UserResponse;
import com.cloud.utils.StringUtils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listUsers", description = "Lists user accounts", responseObject = UserResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class ListUsersCmd extends BaseListAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListUsersCmd.class.getName());

    private static final String s_name = "listusersresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ACCOUNT_TYPE,
            type = CommandType.LONG,
            description = "List users by account type. Valid types include admin, domain-admin, read-only-admin, or user.")
    private Long accountType;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = UserResponse.class, description = "List user by ID.")
    private Long id;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "List users by state of the user account.")
    private String state;

    @Parameter(name = ApiConstants.USERNAME, type = CommandType.STRING, description = "List user by the username")
    private String username;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getAccountType() {
        return accountType;
    }

    public Long getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public String getUsername() {
        return username;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final ListResponse<UserResponse> response = _queryService.searchForUsers(this);
        response.setResponseName(getCommandName());
        List<UserResponse> responseList = response.getResponses();

        if (responseList != null && responseList.size() > 0) {
            for (UserResponse userResponse : responseList) {
                if (StringUtils.isNotBlank(userResponse.getSecretKey())) {
                    userResponse.setSecretKey("SecretKey only visible when generating a new key");
                }
            }
        }
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
