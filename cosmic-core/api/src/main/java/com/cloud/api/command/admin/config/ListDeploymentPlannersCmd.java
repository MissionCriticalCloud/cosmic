package com.cloud.api.command.admin.config;

import com.cloud.api.response.DeploymentPlannersResponse;
import com.cloud.api.response.ListResponse;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListCmd;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@APICommand(name = "listDeploymentPlanners", description = "Lists all DeploymentPlanners available.", responseObject = DeploymentPlannersResponse.class,
        requestHasSensitiveInfo = false, responseHasSensitiveInfo = false)
public class ListDeploymentPlannersCmd extends BaseListCmd {
    public static final Logger s_logger = LoggerFactory.getLogger(ListDeploymentPlannersCmd.class.getName());

    private static final String s_name = "listdeploymentplannersresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        final List<String> planners = _mgr.listDeploymentPlanners();
        final ListResponse<DeploymentPlannersResponse> response = new ListResponse<>();
        final List<DeploymentPlannersResponse> plannerResponses = new ArrayList<>();

        for (final String planner : planners) {
            final DeploymentPlannersResponse plannerResponse = new DeploymentPlannersResponse();
            plannerResponse.setName(planner);
            plannerResponse.setObjectName("deploymentPlanner");
            plannerResponses.add(plannerResponse);
        }

        response.setResponses(plannerResponses);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return s_name;
    }
}
