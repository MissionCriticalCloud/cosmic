package com.cloud.projects.dao;

import com.cloud.projects.ProjectInvitation.State;
import com.cloud.projects.ProjectInvitationVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface ProjectInvitationDao extends GenericDao<ProjectInvitationVO, Long> {
    ProjectInvitationVO findByAccountIdProjectId(long accountId, long projectId, State... inviteState);

    List<ProjectInvitationVO> listExpiredInvitations();

    boolean expirePendingInvitations(long timeOut);

    boolean isActive(long id, long timeout);

    ProjectInvitationVO findByEmailAndProjectId(String email, long projectId, State... inviteState);

    ProjectInvitationVO findPendingByTokenAndProjectId(String token, long projectId, State... inviteState);

    void cleanupInvitations(long projectId);

    ProjectInvitationVO findPendingById(long id);

    List<ProjectInvitationVO> listInvitationsToExpire(long timeOut);
}
