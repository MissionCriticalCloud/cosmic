package com.cloud.api.command.admin.host;

import com.cloud.api.APICommand;
import com.cloud.api.ApiCommandJobType;
import com.cloud.api.BaseListCmd;
import com.cloud.api.response.HostTagResponse;
import com.cloud.api.response.ListResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listHostTags", description = "Lists host tags", responseObject = HostTagResponse.class, requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListHostTagsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListHostTagsCmd.class.getName());

    private static final String s_name = "listhosttagsresponse";

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
        return ApiCommandJobType.Host;
    }

    @Override
    public void execute() {
        final ListResponse<HostTagResponse> response = _queryService.searchForHostTags(this);

        response.setResponseName(getCommandName());

        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
