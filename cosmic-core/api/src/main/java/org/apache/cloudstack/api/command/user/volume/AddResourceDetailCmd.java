package org.apache.cloudstack.api.command.user.volume;

import com.cloud.event.EventTypes;
import com.cloud.server.ResourceTag;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.SuccessResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "addResourceDetail", description = "Adds detail for the Resource.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class AddResourceDetailCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(AddResourceDetailCmd.class.getName());
    private static final String s_name = "addresourcedetailresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.DETAILS, type = CommandType.MAP, required = true, description = "Map of (key/value pairs)")
    private Map details;

    @Parameter(name = ApiConstants.RESOURCE_TYPE, type = CommandType.STRING, required = true, description = "type of the resource")
    private String resourceType;

    @Parameter(name = ApiConstants.RESOURCE_ID, type = CommandType.STRING, required = true, collectionType = CommandType.STRING, description = "resource id to create the details" +
            " for")
    private String resourceId;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "pass false if you want this detail to be disabled for the regular user. True by " +
            "default", since = "4.4")
    private Boolean display;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_RESOURCE_DETAILS_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "adding details to the resource ";
    }

    @Override
    public void execute() {
        _resourceMetaDataService.addResourceMetaData(getResourceId(), getResourceType(), getDetails(), forDisplay());
        setResponseObject(new SuccessResponse(getCommandName()));
    }

    public String getResourceId() {
        return resourceId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    public ResourceTag.ResourceObjectType getResourceType() {
        return _taggedResourceService.getResourceType(resourceType);
    }

    public Map getDetails() {
        Map<String, String> detailsMap = null;
        if (!details.isEmpty()) {
            detailsMap = new HashMap<>();
            final Collection<?> servicesCollection = details.values();
            final Iterator<?> iter = servicesCollection.iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> services = (HashMap<String, String>) iter.next();
                final String key = services.get("key");
                final String value = services.get("value");
                detailsMap.put(key, value);
            }
        }
        return detailsMap;
    }

    public boolean forDisplay() {
        if (display != null) {
            return display;
        } else {
            return true;
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        //FIXME - validate the owner here
        return 1;
    }
}
