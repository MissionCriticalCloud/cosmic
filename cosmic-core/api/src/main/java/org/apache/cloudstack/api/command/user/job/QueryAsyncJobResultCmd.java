package org.apache.cloudstack.api.command.user.job;

import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.AsyncJobResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "queryAsyncJobResult", description = "Retrieves the current status of asynchronous job.", responseObject = AsyncJobResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class QueryAsyncJobResultCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(QueryAsyncJobResultCmd.class.getName());

    private static final String s_name = "queryasyncjobresultresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.JOB_ID, type = CommandType.UUID, entityType = AsyncJobResponse.class, required = true, description = "the ID of the asychronous job")
    private Long id;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final AsyncJobResponse response = _responseGenerator.queryJobResult(this);
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
