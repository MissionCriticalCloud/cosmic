package org.apache.cloudstack.api.command.user.tag;

import com.cloud.event.EventTypes;
import com.cloud.server.ResourceTag;
import com.cloud.server.ResourceTag.ResourceObjectType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.SuccessResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "deleteTags", description = "Deleting resource tag(s)", responseObject = SuccessResponse.class, since = "4.0.0", entityType = {ResourceTag.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class DeleteTagsCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(DeleteTagsCmd.class.getName());

    private static final String s_name = "deletetagsresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.TAGS, type = CommandType.MAP, description = "Delete tags matching key/value pairs")
    private Map tag;

    @Parameter(name = ApiConstants.RESOURCE_TYPE, type = CommandType.STRING, required = true, description = "Delete tag by resource type")
    private String resourceType;

    @Parameter(name = ApiConstants.RESOURCE_IDS,
            type = CommandType.LIST,
            required = true,
            collectionType = CommandType.STRING,
            description = "Delete tags for resource id(s)")
    private List<String> resourceIds;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final boolean success = _taggedResourceService.deleteTags(getResourceIds(), getResourceType(), getTags());

        if (success) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to delete tags");
        }
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public ResourceObjectType getResourceType() {
        return _taggedResourceService.getResourceType(resourceType);
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public Map<String, String> getTags() {
        Map<String, String> tagsMap = null;
        if (tag != null && !tag.isEmpty()) {
            tagsMap = new HashMap<>();
            final Collection<?> servicesCollection = tag.values();
            final Iterator<?> iter = servicesCollection.iterator();
            while (iter.hasNext()) {
                final HashMap<String, String> services = (HashMap<String, String>) iter.next();
                final String key = services.get("key");
                final String value = services.get("value");
                tagsMap.put(key, value);
            }
        }
        return tagsMap;
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

    @Override
    public String getEventType() {
        return EventTypes.EVENT_TAGS_DELETE;
    }

    @Override
    public String getEventDescription() {
        return "Deleting tags";
    }
}
