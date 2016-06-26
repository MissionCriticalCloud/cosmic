package com.cloud.api.query.dao;

import com.cloud.api.query.vo.ProjectInvitationJoinVO;
import com.cloud.projects.ProjectInvitation;
import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.api.response.ProjectInvitationResponse;

public interface ProjectInvitationJoinDao extends GenericDao<ProjectInvitationJoinVO, Long> {

    ProjectInvitationResponse newProjectInvitationResponse(ProjectInvitationJoinVO proj);

    ProjectInvitationJoinVO newProjectInvitationView(ProjectInvitation proj);
}
