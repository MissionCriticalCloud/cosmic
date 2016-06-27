package com.cloud.projects.dao;

import com.cloud.projects.Project;
import com.cloud.projects.ProjectVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface ProjectDao extends GenericDao<ProjectVO, Long> {

    ProjectVO findByNameAndDomain(String name, long domainId);

    Long countProjectsForDomain(long domainId);

    ProjectVO findByProjectAccountId(long projectAccountId);

    List<ProjectVO> listByState(Project.State state);

    ProjectVO findByProjectAccountIdIncludingRemoved(long projectAccountId);
}
