package com.cloud.projects.dao;

import com.cloud.projects.ProjectInvitation.State;
import com.cloud.projects.ProjectInvitationVO;
import com.cloud.utils.DateUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.sql.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProjectInvitationDaoImpl extends GenericDaoBase<ProjectInvitationVO, Long> implements ProjectInvitationDao {
    private static final Logger s_logger = LoggerFactory.getLogger(ProjectInvitationDaoImpl.class);
    protected final SearchBuilder<ProjectInvitationVO> AllFieldsSearch;
    protected final SearchBuilder<ProjectInvitationVO> InactiveSearch;

    protected ProjectInvitationDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("accountId", AllFieldsSearch.entity().getForAccountId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("projectId", AllFieldsSearch.entity().getProjectId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("created", AllFieldsSearch.entity().getCreated(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("projectAccountId", AllFieldsSearch.entity().getState(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), SearchCriteria.Op.IN);
        AllFieldsSearch.and("email", AllFieldsSearch.entity().getEmail(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("token", AllFieldsSearch.entity().getToken(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        InactiveSearch = createSearchBuilder();
        InactiveSearch.and("id", InactiveSearch.entity().getId(), SearchCriteria.Op.EQ);
        InactiveSearch.and("accountId", InactiveSearch.entity().getForAccountId(), SearchCriteria.Op.EQ);
        InactiveSearch.and("projectId", InactiveSearch.entity().getProjectId(), SearchCriteria.Op.EQ);
        InactiveSearch.and("created", InactiveSearch.entity().getCreated(), SearchCriteria.Op.LTEQ);
        InactiveSearch.and("state", InactiveSearch.entity().getState(), SearchCriteria.Op.EQ);
        InactiveSearch.done();
    }

    @Override
    public ProjectInvitationVO findByAccountIdProjectId(final long accountId, final long projectId, final State... inviteState) {
        final SearchCriteria<ProjectInvitationVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("projectId", projectId);
        if (inviteState != null && inviteState.length > 0) {
            sc.setParameters("state", (Object[]) inviteState);
        }

        return findOneBy(sc);
    }

    @Override
    public List<ProjectInvitationVO> listExpiredInvitations() {
        final SearchCriteria<ProjectInvitationVO> sc = AllFieldsSearch.create();
        sc.setParameters("state", State.Expired);

        return listBy(sc);
    }

    @Override
    public boolean expirePendingInvitations(final long timeout) {
        boolean success = true;

        final SearchCriteria<ProjectInvitationVO> sc = InactiveSearch.create();
        sc.setParameters("created", new Date((DateUtil.currentGMTTime().getTime() >> 10) - timeout));
        sc.setParameters("state", State.Pending);

        final List<ProjectInvitationVO> invitationsToExpire = listBy(sc);
        for (final ProjectInvitationVO invitationToExpire : invitationsToExpire) {
            invitationToExpire.setState(State.Expired);
            if (!update(invitationToExpire.getId(), invitationToExpire)) {
                s_logger.warn("Fail to expire invitation " + invitationToExpire.toString());
                success = false;
            }
        }
        return success;
    }

    @Override
    public boolean isActive(final long id, final long timeout) {
        final SearchCriteria<ProjectInvitationVO> sc = InactiveSearch.create();

        sc.setParameters("id", id);

        if (findOneBy(sc) == null) {
            s_logger.warn("Unable to find project invitation by id " + id);
            return false;
        }

        sc.setParameters("created", new Date((DateUtil.currentGMTTime().getTime()) - timeout));

        if (findOneBy(sc) == null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public ProjectInvitationVO findByEmailAndProjectId(final String email, final long projectId, final State... inviteState) {
        final SearchCriteria<ProjectInvitationVO> sc = AllFieldsSearch.create();
        sc.setParameters("email", email);
        sc.setParameters("projectId", projectId);
        if (inviteState != null && inviteState.length > 0) {
            sc.setParameters("state", (Object[]) inviteState);
        }

        return findOneBy(sc);
    }

    @Override
    public ProjectInvitationVO findPendingByTokenAndProjectId(final String token, final long projectId, final State... inviteState) {
        final SearchCriteria<ProjectInvitationVO> sc = AllFieldsSearch.create();
        sc.setParameters("token", token);
        sc.setParameters("projectId", projectId);
        if (inviteState != null && inviteState.length > 0) {
            sc.setParameters("state", (Object[]) inviteState);
        }

        return findOneBy(sc);
    }

    @Override
    public void cleanupInvitations(final long projectId) {
        final SearchCriteria<ProjectInvitationVO> sc = AllFieldsSearch.create();
        sc.setParameters("projectId", projectId);

        final int numberRemoved = remove(sc);
        s_logger.debug("Removed " + numberRemoved + " invitations for project id=" + projectId);
    }

    @Override
    public ProjectInvitationVO findPendingById(final long id) {
        final SearchCriteria<ProjectInvitationVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", id);
        sc.setParameters("state", State.Pending);

        return findOneBy(sc);
    }

    @Override
    public List<ProjectInvitationVO> listInvitationsToExpire(final long timeOut) {
        final SearchCriteria<ProjectInvitationVO> sc = InactiveSearch.create();
        sc.setParameters("created", new Date((DateUtil.currentGMTTime().getTime()) - timeOut));
        sc.setParameters("state", State.Pending);
        return listBy(sc);
    }
}
