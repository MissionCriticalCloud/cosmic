package org.apache.cloudstack.api.command.user.resource;

import com.cloud.user.Account;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.command.admin.router.UpgradeRouterCmd;
import org.apache.cloudstack.api.response.HypervisorResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listHypervisors", description = "List hypervisors", responseObject = HypervisorResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListHypervisorsCmd extends BaseCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(UpgradeRouterCmd.class.getName());
    private static final String s_name = "listhypervisorsresponse";
    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, entityType = ZoneResponse.class, description = "the zone id for listing hypervisors.")
    private Long zoneId;

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        final List<String> result = _mgr.getHypervisors(getZoneId());
        final ListResponse<HypervisorResponse> response = new ListResponse<>();
        final ArrayList<HypervisorResponse> responses = new ArrayList<>();
        if (result != null) {
            for (final String hypervisor : result) {
                final HypervisorResponse hypervisorResponse = new HypervisorResponse();
                hypervisorResponse.setName(hypervisor);
                hypervisorResponse.setObjectName("hypervisor");
                responses.add(hypervisorResponse);
            }
        }
        response.setResponses(responses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////
    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    public Long getZoneId() {
        return this.zoneId;
    }
}
