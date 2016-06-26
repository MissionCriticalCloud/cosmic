package com.cloud.projects.dao;

import com.cloud.projects.Project;
import com.cloud.projects.ProjectVO;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.TransactionLegacy;

import javax.inject.Inject;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProjectDaoImpl extends GenericDaoBase<ProjectVO, Long> implements ProjectDao {
    private static final Logger s_logger = LoggerFactory.getLogger(ProjectDaoImpl.class);
    protected final SearchBuilder<ProjectVO> AllFieldsSearch;
    protected GenericSearchBuilder<ProjectVO, Long> CountByDomain;
    protected GenericSearchBuilder<ProjectVO, Long> ProjectAccountSearch;
    // ResourceTagsDaoImpl _tagsDao = ComponentLocator.inject(ResourceTagsDaoImpl.class);
    @Inject
    ResourceTagDao _tagsDao;

    protected ProjectDaoImpl() {
        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("name", AllFieldsSearch.entity().getName(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("domainId", AllFieldsSearch.entity().getDomainId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("projectAccountId", AllFieldsSearch.entity().getProjectAccountId(), SearchCriteria.Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), SearchCriteria.Op.EQ);
        AllFieldsSearch.done();

        CountByDomain = createSearchBuilder(Long.class);
        CountByDomain.select(null, Func.COUNT, null);
        CountByDomain.and("domainId", CountByDomain.entity().getDomainId(), SearchCriteria.Op.EQ);
        CountByDomain.done();
    }

    @Override
    public ProjectVO findByNameAndDomain(final String name, final long domainId) {
        final SearchCriteria<ProjectVO> sc = AllFieldsSearch.create();
        sc.setParameters("name", name);
        sc.setParameters("domainId", domainId);

        return findOneBy(sc);
    }

    @Override
    public Long countProjectsForDomain(final long domainId) {
        final SearchCriteria<Long> sc = CountByDomain.create();
        sc.setParameters("domainId", domainId);
        return customSearch(sc, null).get(0);
    }

    @Override
    public ProjectVO findByProjectAccountId(final long projectAccountId) {
        final SearchCriteria<ProjectVO> sc = AllFieldsSearch.create();
        sc.setParameters("projectAccountId", projectAccountId);

        return findOneBy(sc);
    }

    @Override
    public List<ProjectVO> listByState(final Project.State state) {
        final SearchCriteria<ProjectVO> sc = AllFieldsSearch.create();
        sc.setParameters("state", state);
        return listBy(sc);
    }

    @Override
    public ProjectVO findByProjectAccountIdIncludingRemoved(final long projectAccountId) {
        final SearchCriteria<ProjectVO> sc = AllFieldsSearch.create();
        sc.setParameters("projectAccountId", projectAccountId);

        return findOneIncludingRemovedBy(sc);
    }

    @Override
    @DB
    public boolean remove(final Long projectId) {
        boolean result = false;
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final ProjectVO projectToRemove = findById(projectId);
        projectToRemove.setName(null);
        if (!update(projectId, projectToRemove)) {
            s_logger.warn("Failed to reset name for the project id=" + projectId + " as a part of project remove");
            return false;
        }

        _tagsDao.removeByIdAndType(projectId, ResourceObjectType.Project);
        result = super.remove(projectId);
        txn.commit();

        return result;
    }
}
