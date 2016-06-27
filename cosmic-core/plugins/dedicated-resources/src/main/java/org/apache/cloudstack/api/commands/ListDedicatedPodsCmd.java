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
import org.apache.cloudstack.api.response.DedicatePodResponse;
import org.apache.cloudstack.api.response.DomainResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.PodResponse;
import org.apache.cloudstack.dedicated.DedicatedService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listDedicatedPods", description = "Lists dedicated pods.", responseObject = DedicatePodResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListDedicatedPodsCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListDedicatedPodsCmd.class.getName());

    private static final String s_name = "listdedicatedpodsresponse";
    @Inject
    DedicatedService dedicatedService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name = ApiConstants.POD_ID, type = CommandType.UUID, entityType = PodResponse.class, description = "the ID of the pod")
    private Long podId;

    @Parameter(name = ApiConstants.DOMAIN_ID, type = CommandType.UUID, entityType = DomainResponse.class, description = "the ID of the domain associated with the pod")
    private Long domainId;

    @Parameter(name = ApiConstants.ACCOUNT, type = CommandType.STRING, description = "the name of the account associated with the pod. Must be used with domainId.")
    private String accountName;

    @Parameter(name = ApiConstants.AFFINITY_GROUP_ID,
            type = CommandType.UUID,
            entityType = AffinityGroupResponse.class,
            description = "list dedicated pods by affinity group")
    private Long affinityGroupId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getPodId() {
        return podId;
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
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final Pair<List<? extends DedicatedResourceVO>, Integer> result = dedicatedService.listDedicatedPods(this);
        final ListResponse<DedicatePodResponse> response = new ListResponse<>();
        final List<DedicatePodResponse> Responses = new ArrayList<>();
        if (result != null) {
            for (final DedicatedResources resource : result.first()) {
                final DedicatePodResponse podresponse = dedicatedService.createDedicatePodResponse(resource);
                Responses.add(podresponse);
            }
            response.setResponses(Responses, result.second());
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to list dedicated pods");
        }
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
