package org.apache.cloudstack.api.command.user.account;

import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListDomainResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.ListResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listAccounts", description = "Lists accounts and provides detailed account information for listed accounts", responseObject = AccountResponse.class,
        responseView = ResponseView.Restricted, entityType = {Account.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class ListAccountsCmd extends BaseListDomainResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListAccountsCmd.class.getName());
    private static final String s_name = "listaccountsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ACCOUNT_TYPE,
            type = CommandType.LONG,
            description = "list accounts by account type. Valid account types are 1 (admin), 2 (domain-admin), and 0 (user).")
    private Long accountType;

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = AccountResponse.class, description = "list account by account ID")
    private Long id;

    @Parameter(name = ApiConstants.IS_CLEANUP_REQUIRED, type = CommandType.BOOLEAN, description = "list accounts by cleanuprequired attribute (values are true or false)")
    private Boolean cleanupRequired;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "list account by account name")
    private String searchName;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "list accounts by state. Valid states are enabled, disabled, and locked.")
    private String state;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getAccountType() {
        return accountType;
    }

    public Long getId() {
        return id;
    }

    public Boolean isCleanupRequired() {
        return cleanupRequired;
    }

    public String getSearchName() {
        return searchName;
    }

    public String getState() {
        return state;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final ListResponse<AccountResponse> response = _queryService.searchForAccounts(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
