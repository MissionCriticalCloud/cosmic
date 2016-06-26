package org.apache.cloudstack.api.command.user.volume;

import com.cloud.event.EventTypes;
import com.cloud.server.ResourceTag;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandJobType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.SuccessResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "removeResourceDetail", description = "Removes detail for the Resource.", responseObject = SuccessResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class RemoveResourceDetailCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(RemoveResourceDetailCmd.class.getName());
    private static final String s_name = "removeresourcedetailresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.KEY, type = CommandType.STRING, description = "Delete details matching key/value pairs")
    private String key;

    @Parameter(name = ApiConstants.RESOURCE_TYPE, type = CommandType.STRING, required = true, description = "Delete detail by resource type")
    private String resourceType;

    @Parameter(name = ApiConstants.RESOURCE_ID,
            type = CommandType.STRING,
            required = true,
            collectionType = CommandType.STRING,
            description = "Delete details for resource id")
    private String resourceId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getEventType() {
        return EventTypes.EVENT_RESOURCE_DETAILS_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "Removing detail to the volume ";
    }

    @Override
    public ApiCommandJobType getInstanceType() {
        return ApiCommandJobType.Volume;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        _resourceMetaDataService.deleteResourceMetaData(getResourceId(), getResourceType(), getKey());
        this.setResponseObject(new SuccessResponse(getCommandName()));
    }

    public String getResourceId() {
        return resourceId;
    }

    public ResourceTag.ResourceObjectType getResourceType() {
        return _taggedResourceService.getResourceType(resourceType);
    }

    public String getKey() {
        return key;
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
