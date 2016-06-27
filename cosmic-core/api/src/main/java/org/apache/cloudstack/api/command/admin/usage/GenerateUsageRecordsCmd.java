package org.apache.cloudstack.api.command.admin.usage;

import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.SuccessResponse;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "generateUsageRecords",
        description = "Generates usage records. This will generate records only if there any records to be generated, i.e if the scheduled usage job was not run or failed",
        responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class GenerateUsageRecordsCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(GenerateUsageRecordsCmd.class.getName());

    private static final String s_name = "generateusagerecordsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "List events for the specified domain.")
    private Long domainId;

    @Parameter(name = ApiConstants.END_DATE,
            type = CommandType.DATE,
            required = true,
            description = "End date range for usage record query. Use yyyy-MM-dd as the date format, e.g. startDate=2009-06-03.")
    private Date endDate;

    @Parameter(name = ApiConstants.START_DATE,
            type = CommandType.DATE,
            required = true,
            description = "Start date range for usage record query. Use yyyy-MM-dd as the date format, e.g. startDate=2009-06-01.")
    private Date startDate;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getDomainId() {
        return domainId;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean result = _usageService.generateUsageRecords(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to generate usage records");
        }
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
