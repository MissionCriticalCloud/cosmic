package org.apache.cloudstack.api.command.user.guest;

import com.cloud.storage.GuestOS;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.command.user.iso.ListIsosCmd;
import org.apache.cloudstack.api.response.GuestOSCategoryResponse;
import org.apache.cloudstack.api.response.GuestOSResponse;
import org.apache.cloudstack.api.response.ListResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listOsTypes", description = "Lists all supported OS types for this cloud.", responseObject = GuestOSResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListGuestOsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListIsosCmd.class.getName());

    private static final String s_name = "listostypesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = GuestOSResponse.class, description = "list by Os type Id")
    private Long id;

    @Parameter(name = ApiConstants.OS_CATEGORY_ID, type = CommandType.UUID, entityType = GuestOSCategoryResponse.class, description = "list by Os Category id")
    private Long osCategoryId;

    @Parameter(name = ApiConstants.DESCRIPTION, type = CommandType.STRING, description = "list os by description", since = "3.0.1")
    private String description;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Long getOsCategoryId() {
        return osCategoryId;
    }

    public String getDescription() {
        return description;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends GuestOS>, Integer> result = _mgr.listGuestOSByCriteria(this);
        final ListResponse<GuestOSResponse> response = new ListResponse<>();
        final List<GuestOSResponse> osResponses = new ArrayList<>();
        for (final GuestOS guestOS : result.first()) {
            final GuestOSResponse guestOSResponse = _responseGenerator.createGuestOSResponse(guestOS);
            osResponses.add(guestOSResponse);
        }

        response.setResponses(osResponses, result.second());
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
