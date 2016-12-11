package com.cloud.api.command.admin.storage;

import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.BaseListCmd;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.StorageTagResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listStorageTags", description = "Lists storage tags", responseObject = StorageTagResponse.class, requestHasSensitiveInfo = false, responseHasSensitiveInfo =
        false)
public class ListStorageTagsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListStorageTagsCmd.class.getName());

    private static final String s_name = "liststoragetagsresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.StoragePool;
    }

    @Override
    public void execute() {
        final ListResponse<StorageTagResponse> response = _queryService.searchForStorageTags(this);

        response.setResponseName(getCommandName());

        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
