package org.apache.cloudstack.api.command.user.event;

import com.cloud.event.Event;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListProjectAndAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.EventResponse;
import org.apache.cloudstack.api.response.ListResponse;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listEvents", description = "A command to list events.", responseObject = EventResponse.class, entityType = {Event.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListEventsCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListEventsCmd.class.getName());

    private static final String s_name = "listeventsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = EventResponse.class, description = "the ID of the event")
    private Long id;

    @Parameter(name = ApiConstants.DURATION, type = CommandType.INTEGER, description = "the duration of the event")
    private Integer duration;

    @Parameter(name = ApiConstants.END_DATE,
            type = CommandType.DATE,
            description = "the end date range of the list you want to retrieve (use format \"yyyy-MM-dd\" or the new format \"yyyy-MM-dd HH:mm:ss\")")
    private Date endDate;

    @Parameter(name = ApiConstants.ENTRY_TIME, type = CommandType.INTEGER, description = "the time the event was entered")
    private Integer entryTime;

    @Parameter(name = ApiConstants.LEVEL, type = CommandType.STRING, description = "the event level (INFO, WARN, ERROR)")
    private String level;

    @Parameter(name = ApiConstants.START_DATE,
            type = CommandType.DATE,
            description = "the start date range of the list you want to retrieve (use format \"yyyy-MM-dd\" or the new format \"yyyy-MM-dd HH:mm:ss\")")
    private Date startDate;

    @Parameter(name = ApiConstants.TYPE, type = CommandType.STRING, description = "the event type (see event types)")
    private String type;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Integer getDuration() {
        return duration;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Integer getEntryTime() {
        return entryTime;
    }

    public String getLevel() {
        return level;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getType() {
        return type;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {

        final ListResponse<EventResponse> response = _queryService.searchForEvents(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
