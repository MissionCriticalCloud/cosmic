package com.cloud.api.command.admin.cloudops;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListDomainResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.WhoHasThisIpResponse;

@APICommand(name = "listWhoHasThisIp", description = "Lists all for this IP address", responseObject = WhoHasThisIpResponse.class)
public class ListWhoHasThisIpCmd extends BaseListDomainResourcesCmd {

    private static final String COMMAND_NAME = "listwhohasthisipresponse";
    @Parameter(name = ApiConstants.IP_ADDRESS, type = CommandType.STRING, required = true,
            description = "The IP Address that you are searching the owner of.")
    private String ipAddress;

    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public void execute() {
        final ListResponse<WhoHasThisIpResponse> response = _queryService.listWhoHasThisIp(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

}
