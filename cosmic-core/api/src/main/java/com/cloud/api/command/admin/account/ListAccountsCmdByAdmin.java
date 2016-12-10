package com.cloud.api.command.admin.account;

import com.cloud.api.command.user.account.ListAccountsCmd;
import com.cloud.api.response.AccountResponse;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ResponseObject.ResponseView;

@APICommand(name = "listAccounts", description = "Lists accounts and provides detailed account information for listed accounts", responseObject = AccountResponse.class,
        responseView = ResponseView.Full, entityType = {Account.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = true)
public class ListAccountsCmdByAdmin extends ListAccountsCmd {
}
