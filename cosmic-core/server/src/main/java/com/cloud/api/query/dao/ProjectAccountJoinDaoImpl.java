package com.cloud.api.query.dao;

import com.cloud.api.query.vo.ProjectAccountJoinVO;
import com.cloud.projects.ProjectAccount;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.response.ProjectAccountResponse;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProjectAccountJoinDaoImpl extends GenericDaoBase<ProjectAccountJoinVO, Long> implements ProjectAccountJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(ProjectAccountJoinDaoImpl.class);

    private final SearchBuilder<ProjectAccountJoinVO> paIdSearch;

    protected ProjectAccountJoinDaoImpl() {

        paIdSearch = createSearchBuilder();
        paIdSearch.and("accountId", paIdSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        paIdSearch.and("projectId", paIdSearch.entity().getProjectId(), SearchCriteria.Op.EQ);
        paIdSearch.done();

        this._count = "select count(distinct id) from project_account_view WHERE ";
    }

    @Override
    public ProjectAccountResponse newProjectAccountResponse(final ProjectAccountJoinVO proj) {
        final ProjectAccountResponse projectAccountResponse = new ProjectAccountResponse();

        projectAccountResponse.setProjectId(proj.getProjectUuid());
        projectAccountResponse.setProjectName(proj.getProjectName());

        projectAccountResponse.setAccountId(proj.getAccountUuid());
        projectAccountResponse.setAccountName(proj.getAccountName());
        projectAccountResponse.setAccountType(proj.getAccountType());
        projectAccountResponse.setRole(proj.getAccountRole().toString());
        projectAccountResponse.setDomainId(proj.getDomainUuid());
        projectAccountResponse.setDomainName(proj.getDomainName());

        projectAccountResponse.setObjectName("projectaccount");

        return projectAccountResponse;
    }

    @Override
    public ProjectAccountJoinVO newProjectAccountView(final ProjectAccount proj) {
        final SearchCriteria<ProjectAccountJoinVO> sc = paIdSearch.create();
        sc.setParameters("accountId", proj.getAccountId());
        sc.setParameters("projectId", proj.getProjectId());
        final List<ProjectAccountJoinVO> grps = searchIncludingRemoved(sc, null, null, false);
        assert grps != null && grps.size() == 1 : "No project account found for account id = " + proj.getAccountId() + " and project id = " + proj.getProjectId();
        return grps.get(0);
    }
}
