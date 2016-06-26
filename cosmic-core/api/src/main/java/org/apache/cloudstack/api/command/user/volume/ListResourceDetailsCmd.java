package org.apache.cloudstack.api.command.user.volume;

import com.cloud.server.ResourceTag;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListProjectAndAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.ResourceDetailResponse;
import org.apache.cloudstack.api.response.ResourceTagResponse;

import java.util.List;

@APICommand(name = "listResourceDetails", description = "List resource detail(s)", responseObject = ResourceTagResponse.class, since = "4.2",
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListResourceDetailsCmd extends BaseListProjectAndAccountResourcesCmd {
    private static final String s_name = "listresourcedetailsresponse";

    @Parameter(name = ApiConstants.RESOURCE_TYPE, type = CommandType.STRING, description = "list by resource type", required = true)
    private String resourceType;

    @Parameter(name = ApiConstants.RESOURCE_ID, type = CommandType.STRING, description = "list by resource id")
    private String resourceId;

    @Parameter(name = ApiConstants.KEY, type = CommandType.STRING, description = "list by key")
    private String key;

    @Parameter(name = ApiConstants.VALUE, type = CommandType.STRING, description = "list by key, value. Needs to be passed only along with key",
            since = "4.4", authorized = {RoleType.Admin})
    private String value;

    @Parameter(name = ApiConstants.FOR_DISPLAY, type = CommandType.BOOLEAN, description = "if set to true, only details marked with display=true, are returned."
            + " False by default", since = "4.3", authorized = {RoleType.Admin})
    private Boolean forDisplay;

    public String getResourceId() {
        return resourceId;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public Boolean getDisplay() {
        if (forDisplay != null) {
            return forDisplay;
        }
        return super.getDisplay();
    }

    @Override
    public void execute() {

        final ListResponse<ResourceDetailResponse> response = new ListResponse<>();
        final List<ResourceDetailResponse> resourceDetailResponse = _queryService.listResourceDetails(this);
        response.setResponses(resourceDetailResponse);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    public ResourceTag.ResourceObjectType getResourceType() {
        return _taggedResourceService.getResourceType(resourceType);
    }
}
