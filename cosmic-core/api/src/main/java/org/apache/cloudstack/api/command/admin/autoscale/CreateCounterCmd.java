package org.apache.cloudstack.api.command.admin.autoscale;

import com.cloud.event.EventTypes;
import com.cloud.network.as.Counter;
import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCreateCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.CounterResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "createCounter", description = "Adds metric counter", responseObject = CounterResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateCounterCmd extends BaseAsyncCreateCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateCounterCmd.class.getName());
    private static final String s_name = "counterresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, required = true, description = "Name of the counter.")
    private String name;

    @Parameter(name = ApiConstants.SOURCE, type = CommandType.STRING, required = true, description = "Source of the counter.")
    private String source;

    @Parameter(name = ApiConstants.VALUE, type = CommandType.STRING, required = true, description = "Value of the counter e.g. oid in case of snmp.")
    private String value;

    // /////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public String getSource() {
        return source;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void create() {
        Counter ctr = null;
        ctr = _autoScaleService.createCounter(this);

        if (ctr != null) {
            this.setEntityId(ctr.getId());
            this.setEntityUuid(ctr.getUuid());
            final CounterResponse response = _responseGenerator.createCounterResponse(ctr);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create Counter with name " + getName());
        }
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public String getName() {
        return name;
    }

    @Override
    public void execute() {
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_COUNTER_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "creating a new Counter";
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Counter;
    }
}
