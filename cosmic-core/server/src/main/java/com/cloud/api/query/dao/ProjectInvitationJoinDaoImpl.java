package com.cloud.api.query.dao;

import com.cloud.api.query.vo.ProjectInvitationJoinVO;
import com.cloud.projects.ProjectInvitation;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.response.ProjectInvitationResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProjectInvitationJoinDaoImpl extends GenericDaoBase<ProjectInvitationJoinVO, Long> implements ProjectInvitationJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(ProjectInvitationJoinDaoImpl.class);

    private final SearchBuilder<ProjectInvitationJoinVO> piIdSearch;

    protected ProjectInvitationJoinDaoImpl() {

        piIdSearch = createSearchBuilder();
        piIdSearch.and("id", piIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        piIdSearch.done();

        this._count = "select count(distinct id) from project_invitation_view WHERE ";
    }

    @Override
    public ProjectInvitationResponse newProjectInvitationResponse(final ProjectInvitationJoinVO invite) {
        final ProjectInvitationResponse response = new ProjectInvitationResponse();
        response.setId(invite.getUuid());
        response.setProjectId(invite.getProjectUuid());
        response.setProjectName(invite.getProjectName());
        if (invite.getState() != null) {
            response.setInvitationState(invite.getState().toString());
        }

        if (invite.getAccountName() != null) {
            response.setAccountName(invite.getAccountName());
        } else {
            response.setEmail(invite.getEmail());
        }

        response.setDomainId(invite.getDomainUuid());
        response.setDomainName(invite.getDomainName());

        response.setObjectName("projectinvitation");
        return response;
    }

    @Override
    public ProjectInvitationJoinVO newProjectInvitationView(final ProjectInvitation proj) {
        final SearchCriteria<ProjectInvitationJoinVO> sc = piIdSearch.create();
        sc.setParameters("id", proj.getId());
        final List<ProjectInvitationJoinVO> grps = searchIncludingRemoved(sc, null, null, false);
        assert grps != null && grps.size() == 1 : "No project invitation found for id  " + proj.getId();
        return grps.get(0);
    }
}
