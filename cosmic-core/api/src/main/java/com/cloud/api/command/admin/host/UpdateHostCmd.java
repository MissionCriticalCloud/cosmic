package com.cloud.api.command.admin.host;

import com.cloud.api.APICommand;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.GuestOSCategoryResponse;
import com.cloud.api.response.HostResponse;
import com.cloud.host.Host;
import com.cloud.user.Account;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateHost", description = "Updates a host.", responseObject = HostResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class UpdateHostCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateHostCmd.class.getName());
    private static final String s_name = "updatehostresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = HostResponse.class, required = true, description = "the ID of the host to update")
    private Long id;

    @Parameter(name = ApiConstants.OS_CATEGORY_ID,
            type = CommandType.UUID,
            entityType = GuestOSCategoryResponse.class,
            description = "the id of Os category to update the host with")
    private Long osCategoryId;

    @Parameter(name = ApiConstants.ALLOCATION_STATE,
            type = CommandType.STRING,
            description = "Change resource state of host, valid values are [Enable, Disable]. Operation may failed if host in states not allowing Enable/Disable")
    private String allocationState;

    @Parameter(name = ApiConstants.HOST_TAGS, type = CommandType.LIST, collectionType = CommandType.STRING, description = "list of tags to be added to the host")
    private List<String> hostTags;

    @Parameter(name = ApiConstants.URL, type = CommandType.STRING, description = "the new uri for the secondary storage: nfs://host/path")
    private String url;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public static String getResultObjectName() {
        return "updatehost";
    }

    public Long getOsCategoryId() {
        return osCategoryId;
    }

    public String getAllocationState() {
        return allocationState;
    }

    public List<String> getHostTags() {
        return hostTags;
    }

    public String getUrl() {
        return url;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Host result;
        try {
            result = _resourceService.updateHost(this);
            final HostResponse hostResponse = _responseGenerator.createHostResponse(result);
            hostResponse.setResponseName(getCommandName());
            this.setResponseObject(hostResponse);
        } catch (final Exception e) {
            s_logger.debug("Failed to update host:" + getId(), e);
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update host:" + getId() + "," + e.getMessage());
        }
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
