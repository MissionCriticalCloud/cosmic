package org.apache.cloudstack.api.command.admin.alert;

import com.cloud.event.EventTypes;
import org.apache.cloudstack.alert.AlertService;
import org.apache.cloudstack.alert.AlertService.AlertType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.PodResponse;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "generateAlert", description = "Generates an alert", responseObject = SuccessResponse.class, since = "4.3",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class GenerateAlertCmd extends BaseAsyncCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(GenerateAlertCmd.class.getName());

    private static final String s_name = "generatealertresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.TYPE, type = CommandType.SHORT, description = "Type of the alert", required = true)
    private Short type;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "Name of the alert", required = true)
    private String name;

    @Parameter(name = ApiConstants.DESCRIPTION, type = CommandType.STRING, description = "Alert description", required = true, length = 999)
    private String description;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "Zone id for which alert is generated")
    private Long zoneId;

    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class, description = "Pod id for which alert is generated")
    private Long podId;

    @Override
    public void execute() {
        final AlertType alertType = AlertService.AlertType.generateAlert(getType(), getName());
        if (_alertSvc.generateAlert(alertType, getZoneId(), getPodId(), getDescription())) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to generate an alert");
        }
    }

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return 0;
    }

    public Short getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public long getZoneId() {
        if (zoneId == null) {
            return 0L;
        }
        return zoneId;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public Long getPodId() {
        return podId;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getEventType() {
        return EventTypes.ALERT_GENERATE;
    }

    @Override
    public String getEventDescription() {
        return "Generating alert of type " + type + "; name " + name;
    }
}
