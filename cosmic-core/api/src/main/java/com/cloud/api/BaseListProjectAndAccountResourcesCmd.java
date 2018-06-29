package com.cloud.api;

import com.cloud.api.response.ProjectResponse;
import com.cloud.context.CallContext;
import com.cloud.legacymodel.exceptions.PermissionDeniedException;
import com.cloud.legacymodel.user.Account;

public abstract class BaseListProjectAndAccountResourcesCmd extends BaseListAccountResourcesCmd implements IBaseListProjectAndAccountResourcesCmd {

    @Parameter(name = ApiConstants.PROJECT_ID, type = CommandType.UUID, entityType = ProjectResponse.class, description = "list objects by project")
    private Long projectId;

    @Override
    public Long getProjectId() {
        // Only allow root admin to see all project resources
        final Account caller = CallContext.current().getCallingAccount();
        if (projectId != null) {
            if (caller.getType() != Account.ACCOUNT_TYPE_ADMIN && projectId == -1L) {
                throw new PermissionDeniedException("Not allowed to access this project");
            }
        }
        return projectId;
    }
}
