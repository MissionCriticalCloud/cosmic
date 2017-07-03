package com.cloud.api.command.admin.cloudops;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListDomainResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.ListResponse;
import com.cloud.api.response.WhoHasThisIpResponse;

@APICommand(name = "listWhoHasThisMac", description = "Lists all for this MAC address", responseObject = WhoHasThisIpResponse.class)
public class ListWhoHasThisMacCmd extends BaseListDomainResourcesCmd {

    private static final String COMMAND_NAME = "listwhohasthismacresponse";
    @Parameter(name = ApiConstants.UUID, type = CommandType.STRING,
            description = "The UUID of nics row.")
    private String uuid;
    @Parameter(name = ApiConstants.MAC_ADDRESS, type = CommandType.STRING,
            description = "The MAC Address that you are searching the owner of.")
    private String macAddress;

    public String getUuid() {
        return uuid;
    }

    public String getMacAddress() {
        return macAddress;
    }

    @Override
    public void execute() {
        final ListResponse<WhoHasThisIpResponse> response = _queryService.listWhoHasThisMac(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

}
