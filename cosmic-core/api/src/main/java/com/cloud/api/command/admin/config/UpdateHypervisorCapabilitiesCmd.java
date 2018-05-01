package com.cloud.api.command.admin.config;

import com.cloud.api.APICommand;
import com.cloud.api.APICommandGroup;
import com.cloud.api.ApiConstants;
import com.cloud.api.ApiErrorCode;
import com.cloud.api.BaseCmd;
import com.cloud.api.Parameter;
import com.cloud.api.ServerApiException;
import com.cloud.api.response.HypervisorCapabilitiesResponse;
import com.cloud.hypervisor.HypervisorCapabilities;
import com.cloud.legacymodel.user.Account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "updateHypervisorCapabilities", group = APICommandGroup.HypervisorService,
        description = "Updates a hypervisor capabilities.",
        responseObject = HypervisorCapabilitiesResponse.class,
        since = "3.0.0",
        requestHasSensitiveInfo = false,
        responseHasSensitiveInfo = false)
public class UpdateHypervisorCapabilitiesCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpdateHypervisorCapabilitiesCmd.class.getName());
    private static final String s_name = "updatehypervisorcapabilitiesresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = HypervisorCapabilitiesResponse.class, description = "ID of the hypervisor capability")
    private Long id;

    @Parameter(name = ApiConstants.MAX_GUESTS_LIMIT, type = CommandType.LONG, description = "the max number of Guest VMs per host for this hypervisor.")
    private Long maxGuestsLimit;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final HypervisorCapabilities result = _mgr.updateHypervisorCapabilities(getId(), getMaxGuestsLimit());
        if (result != null) {
            final HypervisorCapabilitiesResponse response = _responseGenerator.createHypervisorCapabilitiesResponse(result);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to update hypervisor capabilities");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getMaxGuestsLimit() {
        return maxGuestsLimit;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
