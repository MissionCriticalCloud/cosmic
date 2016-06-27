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

@APICommand(name = "createTags", description = "Creates resource tag(s)", responseObject = SuccessResponse.class, since = "4.0.0", entityType = {ResourceTag.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class CreateTagsCmd extends BaseAsyncCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(CreateTagsCmd.class.getName());

    private static final String s_name = "createtagsresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.TAGS, type = CommandType.MAP, required = true, description = "Map of tags (key/value pairs)")
    private Map tag;

    @Parameter(name = ApiConstants.RESOURCE_TYPE, type = CommandType.STRING, required = true, description = "type of the resource")
    private String resourceType;

    @Parameter(name = ApiConstants.RESOURCE_IDS,
            type = CommandType.LIST,
            required = true,
            collectionType = CommandType.STRING,
            description = "list of resources to create the tags for")
    private List<String> resourceIds;

    @Parameter(name = ApiConstants.CUSTOMER, type = CommandType.STRING, description = "identifies client specific tag. "
            + "When the value is not null, the tag can't be used by cloudStack code internally")
    private String customer;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final List<ResourceTag> tags = _taggedResourceService.createTags(getResourceIds(), getResourceType(), getTags(), getCustomer());

        if (tags != null && !tags.isEmpty()) {
            final SuccessResponse response = new SuccessResponse(getCommandName());
            setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to create tags");
        }
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public ResourceObjectType getResourceType() {
        return _taggedResourceService.getResourceType(resourceType);
    }

    public Map<String, String> getTags() {
        Map<String, String> tagsMap = null;
        if (!tag.isEmpty()) {
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

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    public String getCustomer() {
        return customer;
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
        return EventTypes.EVENT_TAGS_CREATE;
    }

    @Override
    public String getEventDescription() {
        return "creating tags";
    }
}
