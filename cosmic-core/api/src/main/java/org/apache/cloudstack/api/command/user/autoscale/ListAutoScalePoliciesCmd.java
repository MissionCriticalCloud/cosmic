package org.apache.cloudstack.api.command.user.autoscale;

import com.cloud.network.as.AutoScalePolicy;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListAccountResourcesCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.AutoScalePolicyResponse;
import org.apache.cloudstack.api.response.AutoScaleVmGroupResponse;
import org.apache.cloudstack.api.response.ConditionResponse;
import org.apache.cloudstack.api.response.ListResponse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listAutoScalePolicies", description = "Lists autoscale policies.", responseObject = AutoScalePolicyResponse.class, entityType = {AutoScalePolicy.class},
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListAutoScalePoliciesCmd extends BaseListAccountResourcesCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListAutoScalePoliciesCmd.class.getName());

    private static final String s_name = "listautoscalepoliciesresponse";

    // ///////////////////////////////////////////////////
    // ////////////// API parameters /////////////////////
    // ///////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = AutoScalePolicyResponse.class, description = "the ID of the autoscale policy")
    private Long id;

    @Parameter(name = ApiConstants.CONDITION_ID, type = CommandType.UUID, entityType = ConditionResponse.class, description = "the ID of the condition of the policy")
    private Long conditionId;

    @Parameter(name = ApiConstants.ACTION,
            type = CommandType.STRING,
            description = "the action to be executed if all the conditions evaluate to true for the specified duration.")
    private String action;

    @Parameter(name = ApiConstants.VMGROUP_ID, type = CommandType.UUID, entityType = AutoScaleVmGroupResponse.class, description = "the ID of the autoscale vm group")
    private Long vmGroupId;

    // ///////////////////////////////////////////////////
    // ///////////////// Accessors ///////////////////////
    // ///////////////////////////////////////////////////

    public Long getId() {
        return id;
    }

    public Long getConditionId() {
        return conditionId;
    }

    public String getAction() {
        return action;
    }

    public Long getVmGroupId() {
        return vmGroupId;
    }

    // ///////////////////////////////////////////////////
    // ///////////// API Implementation///////////////////
    // ///////////////////////////////////////////////////

    @Override
    public void execute() {
        final List<? extends AutoScalePolicy> autoScalePolicies = _autoScaleService.listAutoScalePolicies(this);
        final ListResponse<AutoScalePolicyResponse> response = new ListResponse<>();
        final List<AutoScalePolicyResponse> responses = new ArrayList<>();
        if (autoScalePolicies != null) {
            for (final AutoScalePolicy autoScalePolicy : autoScalePolicies) {
                final AutoScalePolicyResponse autoScalePolicyResponse = _responseGenerator.createAutoScalePolicyResponse(autoScalePolicy);
                autoScalePolicyResponse.setObjectName("autoscalepolicy");
                responses.add(autoScalePolicyResponse);
            }
        }
        response.setResponses(responses);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
