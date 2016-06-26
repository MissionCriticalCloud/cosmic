package org.apache.cloudstack.api.command.user.affinitygroup;

import com.cloud.user.Account;
import org.apache.cloudstack.affinity.AffinityGroupTypeResponse;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.response.ListResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listAffinityGroupTypes", description = "Lists affinity group types available", responseObject = AffinityGroupTypeResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListAffinityGroupTypesCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListAffinityGroupTypesCmd.class.getName());

    private static final String s_name = "listaffinitygrouptypesresponse";

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }

    @Override
    public void execute() {
        final List<String> result = _affinityGroupService.listAffinityGroupTypes();
        final ListResponse<AffinityGroupTypeResponse> response = new ListResponse<>();
        final ArrayList<AffinityGroupTypeResponse> responses = new ArrayList<>();
        if (result != null) {
            for (final String type : result) {
                final AffinityGroupTypeResponse affinityTypeResponse = new AffinityGroupTypeResponse();
                affinityTypeResponse.setType(type);
                affinityTypeResponse.setObjectName("affinityGroupType");
                responses.add(affinityTypeResponse);
            }
        }
        response.setResponses(responses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
