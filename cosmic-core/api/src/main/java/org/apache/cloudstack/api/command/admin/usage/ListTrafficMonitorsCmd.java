package org.apache.cloudstack.api.command.admin.usage;

import com.cloud.host.Host;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.command.user.offering.ListServiceOfferingsCmd;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.TrafficMonitorResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listTrafficMonitors", description = "List traffic monitor Hosts.", responseObject = TrafficMonitorResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListTrafficMonitorsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListServiceOfferingsCmd.class.getName());
    private static final String s_name = "listtrafficmonitorsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, required = true, description = "zone Id")
    private long zoneId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public long getZoneId() {
        return zoneId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final List<? extends Host> trafficMonitors = _networkUsageService.listTrafficMonitors(this);

        final ListResponse<TrafficMonitorResponse> listResponse = new ListResponse<>();
        final List<TrafficMonitorResponse> responses = new ArrayList<>();
        for (final Host trafficMonitor : trafficMonitors) {
            final TrafficMonitorResponse response = _responseGenerator.createTrafficMonitorResponse(trafficMonitor);
            response.setObjectName("trafficmonitor");
            response.setResponseName(getCommandName());
            responses.add(response);
        }

        listResponse.setResponses(responses);
        listResponse.setResponseName(getCommandName());
        this.setResponseObject(listResponse);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
