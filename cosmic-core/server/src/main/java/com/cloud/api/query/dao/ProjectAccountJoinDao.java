package com.cloud.api.query.dao;

import com.cloud.api.query.vo.ProjectAccountJoinVO;
import com.cloud.projects.ProjectAccount;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.response.ProjectAccountResponse;

public interface ProjectAccountJoinDao extends GenericDao<ProjectAccountJoinVO, Long> {

    ProjectAccountResponse newProjectAccountResponse(ProjectAccountJoinVO proj);

    ProjectAccountJoinVO newProjectAccountView(ProjectAccount proj);
}
