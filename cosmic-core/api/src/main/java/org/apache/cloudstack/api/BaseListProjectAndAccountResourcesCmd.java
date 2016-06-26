package org.apache.cloudstack.api;

import org.apache.cloudstack.api.response.ProjectResponse;

public abstract class BaseListProjectAndAccountResourcesCmd extends BaseListAccountResourcesCmd implements IBaseListProjectAndAccountResourcesCmd {

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, description = "list objects by project")
    private Long projectId;

    @Override
    public Long getProjectId() {
        return projectId;
    }
}
