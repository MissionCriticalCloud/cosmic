package com.cloud.api.command.user.securitygroup;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.DomainResponse;
import com.cloud.api.response.ProjectResponse;
import com.cloud.api.response.SecurityGroupResponse;
import com.cloud.context.CallContext;
import com.cloud.network.security.SecurityGroup;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createSecurityGroup", responseObject = SecurityGroupResponse.class, description = "Creates a security group", entityType = {SecurityGroup.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateSecurityGroupCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateSecurityGroupCmd.class.getName());

    private static final String s_name = "createsecuritygroupresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "an optional account for the security group. Must be used with domainId.")
    private String accountName;

    @Parameter(name = ApiConstants.DOMAIN_ID,
            type = CommandType.UUID,
            description = "an optional domainId for the security group. If the account parameter is used, domainId must also be used.",
            entityType = DomainResponse.class)
    private Long domainId;

    @Parameter(name = ApiConstants.DESCRIPTION, type = CommandType.STRING, description = "the description of the security group")
    private String description;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "name of the security group")
    private String securityGroupName;

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, description = "Create security group for project", entityType = ProjectResponse.class)
    private Long projectId;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public String getAccountName() {
        return accountName;
    }

    public String getDescription() {
        return description;
    }

    public Long getDomainId() {
        return domainId;
    }

    public String getSecurityGroupName() {
        return securityGroupName;
    }

    public Long getProjectId() {
        return projectId;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        final SecurityGroup group = _securityGroupService.createSecurityGroup(this);
        if (group != null) {
            final SecurityGroupResponse response = _responseGenerator.createSecurityGroupResponse(group);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create security group");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        final Account account = CallContext.current().getCallingAccount();
        if ((account == null) || _accountService.isAdmin(account.getId())) {
            if ((domainId != null) && (accountName != null)) {
                final Account userAccount = _responseGenerator.findAccountByNameDomain(accountName, domainId);
                if (userAccount != null) {
                    return userAccount.getId();
                }
            }
        }

        if (account != null) {
            return account.getId();
        }

        return Account.ACCOUNT_ID_SYSTEM; // no account info given, parent this command to SYSTEM so ERROR events are
        // tracked
    }
}
