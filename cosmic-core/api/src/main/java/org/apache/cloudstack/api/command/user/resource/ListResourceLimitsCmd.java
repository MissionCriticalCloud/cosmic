package org.apache.cloudstack.api.command.user.resource;

import com.cloud.configuration.ResourceLimit;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListProjectAndAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.ResourceLimitResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listResourceLimits", description = "Lists resource limits.", responseObject = ResourceLimitResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListResourceLimitsCmd extends BaseListProjectAndAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListResourceLimitsCmd.class.getName());

    private static final String s_name = "listresourcelimitsresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.LONG, description = "Lists resource limits by ID.")
    private Long id;

    @Parameter(name = ApiConstants.RESOURCE_TYPE, type = CommandType.INTEGER, description = "Type of resource. Values are 0, 1, 2, 3, 4, 6, 7, 8, 9, 10 and 11. "
            + "0 - Instance. Number of instances a user can create. "
            + "1 - IP. Number of public IP addresses an account can own. "
            + "2 - Volume. Number of disk volumes an account can own. "
            + "3 - Snapshot. Number of snapshots an account can own. "
            + "4 - Template. Number of templates an account can register/create. "
            + "5 - Project. Number of projects an account can own. "
            + "6 - Network. Number of networks an account can own. "
            + "7 - VPC. Number of VPC an account can own. "
            + "8 - CPU. Number of CPU an account can allocate for his resources. "
            + "9 - Memory. Amount of RAM an account can allocate for his resources. "
            + "10 - PrimaryStorage. Total primary storage space (in GiB) a user can use. "
            + "11 - SecondaryStorage. Total secondary storage space (in GiB) a user can use. ")
    private Integer resourceType;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Integer getResourceType() {
        return resourceType;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final List<? extends ResourceLimit> result =
                _resourceLimitService.searchForLimits(id, _accountService.finalyzeAccountId(this.getAccountName(), this.getDomainId(), this.getProjectId(), false), this
                                .getDomainId(),
                        resourceType, this.getStartIndex(), this.getPageSizeVal());
        final ListResponse<ResourceLimitResponse> response = new ListResponse<>();
        final List<ResourceLimitResponse> limitResponses = new ArrayList<>();
        for (final ResourceLimit limit : result) {
            final ResourceLimitResponse resourceLimitResponse = _responseGenerator.createResourceLimitResponse(limit);
            resourceLimitResponse.setObjectName("resourcelimit");
            limitResponses.add(resourceLimitResponse);
        }

        response.setResponses(limitResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
