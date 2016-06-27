package org.apache.cloudstack.api.commands;

import com.cloud.dc.DedicatedResourceVO;
import com.cloud.dc.DedicatedResources;
import com.cloud.utils.Pair;
import org.apache.cloudstack.affinity.AffinityGroupResponse;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DedicateHostResponse;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.HostResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.dedicated.DedicatedService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listDedicatedHosts", description = "Lists dedicated hosts.", responseObject = DedicateHostResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListDedicatedHostsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListDedicatedHostsCmd.class.getName());

    private static final String s_name = "listdedicatedhostsresponse";
    @Inject
    DedicatedService dedicatedService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.HOST_ID, type = CommandType.UUID, entityType = HostResponse.class, description = "the ID of the host")
    private Long hostId;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "the ID of the domain associated with the host")
    private Long domainId;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "the name of the account associated with the host. Must be used with domainId.")
    private String accountName;

    @Parameter(name = ApiConstants.AFFINITY_GROUP_ID,
            type = CommandType.UUID,
            entityType = AffinityGroupResponse.class,
            description = "list dedicated hosts by affinity group")
    private Long affinityGroupId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getHostId() {
        return hostId;
    }

    public Long getDomainId() {
        return domainId;
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getAffinityGroupId() {
        return affinityGroupId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////l
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends DedicatedResourceVO>, Integer> result = dedicatedService.listDedicatedHosts(this);
        final ListResponse<DedicateHostResponse> response = new ListResponse<>();
        final List<DedicateHostResponse> Responses = new ArrayList<>();
        if (result != null) {
            for (final DedicatedResources resource : result.first()) {
                final DedicateHostResponse hostResponse = dedicatedService.createDedicateHostResponse(resource);
                Responses.add(hostResponse);
            }
            response.setResponses(Responses, result.second());
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to list dedicated hosts");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
