package org.apache.cloudstack.api.command.admin.resource;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.AlertResponse;
import org.apache.cloudstack.api.response.SuccessResponse;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "archiveAlerts", description = "Archive one or more alerts.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ArchiveAlertsCmd extends BaseCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(ArchiveAlertsCmd.class.getName());

    private static final String s_name = "archivealertsresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.IDS,
            type = CommandType.LIST,
            collectionType = CommandType.UUID,
            entityType = AlertResponse.class,
            description = "the IDs of the alerts")
    private List<Long> ids;

    @Parameter(name = ApiConstants.END_DATE, type = CommandType.DATE, description = "end date range to archive alerts"
            + " (including) this date (use format \"yyyy-MM-dd\" or the new format \"yyyy-MM-ddThh:mm:ss\")")
    private Date endDate;

    @Parameter(name = ApiConstants.START_DATE, type = CommandType.DATE, description = "start date range to archive alerts"
            + " (including) this date (use format \"yyyy-MM-dd\" or the new format \"yyyy-MM-ddThh:mm:ss\")")
    private Date startDate;

    @Parameter(name = ApiConstants.TYPE, type = CommandType.STRING, description = "archive by alert type")
    private String type;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public List<Long> getIds() {
        return ids;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getType() {
        return type;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        if (ids == null && type == null && endDate == null) {
            throw new InvalidParameterValueException("either ids, type, startdate or enddate must be specified");
        } else if (startDate != null && endDate == null) {
            throw new InvalidParameterValueException("enddate must be specified with startdate parameter");
        }
        final boolean result = _mgr.archiveAlerts(this);
        if (result) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Unable to archive Alerts, one or more parameters has invalid values");
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
