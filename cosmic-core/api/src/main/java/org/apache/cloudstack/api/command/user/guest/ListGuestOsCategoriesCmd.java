package org.apache.cloudstack.api.command.user.guest;

import com.cloud.storage.GuestOsCategory;
import com.cloud.utils.Pair;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.command.user.iso.ListIsosCmd;
import org.apache.cloudstack.api.response.GuestOSCategoryResponse;
import org.apache.cloudstack.api.response.ListResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listOsCategories", description = "Lists all supported OS categories for this cloud.", responseObject = GuestOSCategoryResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListGuestOsCategoriesCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListIsosCmd.class.getName());

    private static final String s_name = "listoscategoriesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = GuestOSCategoryResponse.class, description = "list Os category by id")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "list os category by name", since = "3.0.1")
    private String name;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends GuestOsCategory>, Integer> result = _mgr.listGuestOSCategoriesByCriteria(this);
        final ListResponse<GuestOSCategoryResponse> response = new ListResponse<>();
        final List<GuestOSCategoryResponse> osCatResponses = new ArrayList<>();
        for (final GuestOsCategory osCategory : result.first()) {
            final GuestOSCategoryResponse categoryResponse = new GuestOSCategoryResponse();
            categoryResponse.setId(osCategory.getUuid());
            categoryResponse.setName(osCategory.getName());

            categoryResponse.setObjectName("oscategory");
            osCatResponses.add(categoryResponse);
        }

        response.setResponses(osCatResponses, result.second());
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
