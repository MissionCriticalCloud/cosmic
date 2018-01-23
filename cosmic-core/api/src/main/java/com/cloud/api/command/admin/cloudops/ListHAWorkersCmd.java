package com.cloud.api.command.admin.cloudops;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListDomainResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.HAWorkerResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.api.response.ListResponse;

@APICommand(name = "listHAWorkers", group = APICommandGroup.CloudOpsService, description = "Lists all HA workers", responseObject = HAWorkerResponse.class)
public class ListHAWorkersCmd extends BaseListDomainResourcesCmd {

    private static final String COMMAND_NAME = "listhaworkersresponse";

    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, entityType = HAWorkerResponse.class, description = "the ID of the HA worker")
    private Long id;

    @Override
    public void execute() {
        final ListResponse<HAWorkerResponse> response = _queryService.listHAWorkers(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    public Long getId() {
        return id;
    }
}
