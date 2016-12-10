package com.cloud.api.command.admin.usage;

import com.cloud.api.response.ListResponse;
import com.cloud.api.response.UsageTypeResponse;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listUsageTypes", description = "List Usage Types", responseObject = UsageTypeResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListUsageTypesCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListUsageTypesCmd.class.getName());
    private static final String s_name = "listusagetypesresponse";

    @Override
    public void execute() {
        final List<UsageTypeResponse> result = _usageService.listUsageTypes();
        final ListResponse<UsageTypeResponse> response = new ListResponse<>();
        response.setResponses(result);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
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
