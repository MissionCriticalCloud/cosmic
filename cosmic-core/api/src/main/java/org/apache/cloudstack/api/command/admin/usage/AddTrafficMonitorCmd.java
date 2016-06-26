package org.apache.cloudstack.api.command.admin.usage;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.host.Host;
import com.cloud.user.Account;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.TrafficMonitorResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addTrafficMonitor", description = "Adds Traffic Monitor Host for Direct Network Usage", responseObject = TrafficMonitorResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddTrafficMonitorCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddTrafficMonitorCmd.class.getName());
    private static final String s_name = "addtrafficmonitorresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ZONE_ID,
            type = CommandType.UUID,
            entityType = ZoneResponse.class,
            required = true,
            description = "Zone in which to add the external firewall appliance.")
    private Long zoneId;

    @Parameter(name = ApiConstants.URL, type = CommandType.STRING, required = true, description = "URL of the traffic monitor Host")
    private String url;

    @Parameter(name = ApiConstants.INCL_ZONES, type = CommandType.STRING, description = "Traffic going into the listed zones will be metered")
    private String inclZones;

    @Parameter(name = ApiConstants.EXCL_ZONES, type = CommandType.STRING, description = "Traffic going into the listed zones will not be metered")
    private String exclZones;

    ///////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getInclZones() {
        return inclZones;
    }

    public String getExclZones() {
        return exclZones;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public String getUrl() {
        return url;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        try {
            final Host trafficMonitor = _networkUsageService.addTrafficMonitor(this);
            final TrafficMonitorResponse response = _responseGenerator.createTrafficMonitorResponse(trafficMonitor);
            response.setObjectName("trafficmonitor");
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } catch (final InvalidParameterValueException ipve) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, ipve.getMessage());
        } catch (final CloudRuntimeException cre) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, cre.getMessage());
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
