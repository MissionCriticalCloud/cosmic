package com.cloud.api.command.admin.swift;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListCmd;
import com.cloud.api.Parameter;
import com.cloud.api.command.admin.storage.ListImageStoresCmd;
import com.cloud.api.response.ImageStoreResponse;
import com.cloud.api.response.ListResponse;
import com.cloud.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listSwifts", description = "List Swift.", responseObject = ImageStoreResponse.class, since = "3.0.0",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListSwiftsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListSwiftsCmd.class.getName());
    private static final String s_name = "listswiftsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, description = "the id of the swift")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public void execute() {

        final ListImageStoresCmd cmd = new ListImageStoresCmd();
        cmd.setProvider("Swift");
        final ListResponse<ImageStoreResponse> response = _queryService.searchForImageStores(cmd);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
