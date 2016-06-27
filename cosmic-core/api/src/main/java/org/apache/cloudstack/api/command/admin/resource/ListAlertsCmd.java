package org.apache.cloudstack.api.command.admin.resource;

import com.cloud.alert.Alert;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.AlertResponse;
import org.apache.cloudstack.api.response.ListResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listAlerts", description = "Lists all alerts.", responseObject = AlertResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListAlertsCmd extends BaseListCmd {

    public static final Logger s_logger = LoggerFactory.getLogger(ListAlertsCmd.class.getName());

    private static final String s_name = "listalertsresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = AlertResponse.class, description = "the ID of the alert")
    private Long id;

    @Parameter(name = ApiConstants.TYPE, type = CommandType.STRING, description = "list by alert type")
    private String type;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "list by alert name", since = "4.3")
    private String name;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends Alert>, Integer> result = _mgr.searchForAlerts(this);
        final ListResponse<AlertResponse> response = new ListResponse<>();
        final List<AlertResponse> alertResponseList = new ArrayList<>();
        for (final Alert alert : result.first()) {
            final AlertResponse alertResponse = new AlertResponse();
            alertResponse.setId(alert.getUuid());
            alertResponse.setAlertType(alert.getType());
            alertResponse.setDescription(alert.getSubject());
            alertResponse.setLastSent(alert.getLastSent());
            alertResponse.setName(alert.getName());

            alertResponse.setObjectName("alert");
            alertResponseList.add(alertResponse);
        }

        response.setResponses(alertResponseList, result.second());
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
