package com.cloud.api.query.dao;

import com.cloud.api.query.vo.ProjectAccountJoinVO;
import com.cloud.api.response.ProjectAccountResponse;
import com.cloud.projects.ProjectAccount;
import com.cloud.utils.db.GenericDao;

public interface ProjectAccountJoinDao extends GenericDao<ProjectAccountJoinVO, Long> {

    ProjectAccountResponse newProjectAccountResponse(ProjectAccountJoinVO proj);

    ProjectAccountJoinVO newProjectAccountView(ProjectAccount proj);
}
