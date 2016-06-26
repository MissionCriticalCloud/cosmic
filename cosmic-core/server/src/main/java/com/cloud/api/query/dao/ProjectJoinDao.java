package com.cloud.api.query.dao;

import com.cloud.api.query.vo.ProjectJoinVO;
import com.cloud.projects.Project;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.response.ProjectResponse;

import java.util.List;

public interface ProjectJoinDao extends GenericDao<ProjectJoinVO, Long> {

    ProjectResponse newProjectResponse(ProjectJoinVO proj);

    ProjectResponse setProjectResponse(ProjectResponse rsp, ProjectJoinVO proj);

    List<ProjectJoinVO> newProjectView(Project proj);

    List<ProjectJoinVO> searchByIds(Long... ids);
}
