package com.cloud.api.command.user.job;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.BaseListAccountResourcesCmd;
import com.cloud.api.Parameter;
import com.cloud.api.response.AsyncJobResponse;
import com.cloud.api.response.ListResponse;

import java.util.Date;

@APICommand(name = "listAsyncJobs", group = APICommandGroup.AsyncjobService, description = "Lists all pending asynchronous jobs for the account.", responseObject = AsyncJobResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListAsyncJobsCmd extends BaseListAccountResourcesCmd {
    private static final String s_name = "listasyncjobsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.START_DATE, type = CommandType.TZDATE, description = "the start date of the async job")
    private Date startDate;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Date getStartDate() {
        return startDate;
    }

    @Override
    public void execute() {

        final ListResponse<AsyncJobResponse> response = _queryService.searchForAsyncJobs(this);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }
}
