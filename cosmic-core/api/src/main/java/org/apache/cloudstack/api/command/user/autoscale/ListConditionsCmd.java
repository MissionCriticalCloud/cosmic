package org.apache.cloudstack.api.command.user.autoscale;

import com.cloud.network.as.Condition;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.AutoScalePolicyResponse;
import org.apache.cloudstack.api.response.ConditionResponse;
import org.apache.cloudstack.api.response.CounterResponse;
import org.apache.cloudstack.api.response.ListResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listConditions", description = "List Conditions for the specific user", responseObject = ConditionResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListConditionsCmd extends BaseListAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListConditionsCmd.class.getName());
    private static final String s_name = "listconditionsresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = ConditionResponse.class, required = false, description = "ID of the Condition.")
    private Long id;

    @Parameter(name = ApiConstants.COUNTER_ID,
            type = CommandType.UUID,
            entityType = CounterResponse.class,
            required = false,
            description = "Counter-id of the condition.")
    private Long counterId;

    @Parameter(name = ApiConstants.POLICY_ID, type = CommandType.UUID, entityType = AutoScalePolicyResponse.class, description = "the ID of the policy")
    private Long policyId;

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        List<? extends Condition> conditions = null;
        conditions = _autoScaleService.listConditions(this);
        final ListResponse<ConditionResponse> response = new ListResponse<>();
        final List<ConditionResponse> cndnResponses = new ArrayList<>();
        for (final Condition cndn : conditions) {
            final ConditionResponse cndnResponse = _responseGenerator.createConditionResponse(cndn);
            cndnResponses.add(cndnResponse);
        }

        response.setResponses(cndnResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    // /////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    @Override
    public String getCommandName() {
        return s_name;
    }

    public Long getId() {
        return id;
    }

    public Long getCounterId() {
        return counterId;
    }

    public Long getPolicyId() {
        return policyId;
    }
}
