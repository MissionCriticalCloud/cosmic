package com.cloud.projects.dao;

import com.cloud.projects.ProjectAccount;
import com.cloud.projects.ProjectAccountVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface ProjectAccountDao extends GenericDao<ProjectAccountVO, Long> {
    ProjectAccountVO getProjectOwner(long projectId);

    List<ProjectAccountVO> listByProjectId(long projectId);

    ProjectAccountVO findByProjectIdAccountId(long projectId, long accountId);

    boolean canAccessProjectAccount(long accountId, long projectAccountId);

    boolean canModifyProjectAccount(long accountId, long projectAccountId);

    List<Long> listPermittedAccountIds(long accountId);

    List<Long> listAdministratedProjectIds(long adminAccountId);

    Long countByAccountIdAndRole(long accountId, ProjectAccount.Role role);

    void removeAccountFromProjects(long accountId);
}
