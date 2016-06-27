package org.apache.cloudstack.api.command.user.tag;

import com.cloud.server.ResourceTag;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListProjectAndAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.ResourceTagResponse;

@APICommand(name = "listTags", description = "List resource tag(s)", responseObject = ResourceTagResponse.class, since = "4.0.0", entityType = {ResourceTag.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListTagsCmd extends BaseListProjectAndAccountResourcesCmd {
    private static final String s_name = "listtagsresponse";

    @Parameter(name = ApiConstants.RESOURCE_TYPE, type = CommandType.STRING, description = "list by resource type")
    private String resourceType;

    @Parameter(name = ApiConstants.RESOURCE_ID, type = CommandType.STRING, description = "list by resource id")
    private String resourceId;

    @Parameter(name = ApiConstants.KEY, type = CommandType.STRING, description = "list by key")
    private String key;

    @Parameter(name = ApiConstants.VALUE, type = CommandType.STRING, description = "list by value")
    private String value;

    @Parameter(name = ApiConstants.CUSTOMER, type = CommandType.STRING, description = "list by customer name")
    private String customer;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {

        final ListResponse<ResourceTagResponse> response = _queryService.listTags(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getCustomer() {
        return customer;
    }
}
