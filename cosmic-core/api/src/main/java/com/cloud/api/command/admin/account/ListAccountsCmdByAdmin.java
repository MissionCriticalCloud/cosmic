package com.cloud.api.command.admin.account;

import com.cloud.api.APICommand;
import com.cloud.api.ResponseObject.ResponseView;
import com.cloud.api.command.user.account.ListAccountsCmd;
import com.cloud.api.response.AccountResponse;
import com.cloud.user.Account;

@APICommand(name = "listAccounts", description = "Lists accounts and provides detailed account information for listed accounts", responseObject = AccountResponse.class,
        responseView = ResponseView.Full, entityType = {Account.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class ListAccountsCmdByAdmin extends ListAccountsCmd {
}
