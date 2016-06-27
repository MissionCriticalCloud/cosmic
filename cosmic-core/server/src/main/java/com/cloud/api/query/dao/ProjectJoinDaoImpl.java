package com.cloud.api.query.dao;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.query.vo.AccountJoinVO;
import com.cloud.api.query.vo.ProjectJoinVO;
import com.cloud.api.query.vo.ResourceTagJoinVO;
import com.cloud.projects.Project;
import com.cloud.user.Account;
import com.cloud.user.dao.AccountDao;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.response.ProjectResponse;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProjectJoinDaoImpl extends GenericDaoBase<ProjectJoinVO, Long> implements ProjectJoinDao {
    public static final Logger s_logger = LoggerFactory.getLogger(ProjectJoinDaoImpl.class);
    private final SearchBuilder<ProjectJoinVO> prjSearch;
    private final SearchBuilder<ProjectJoinVO> prjIdSearch;
    @Inject
    private ConfigurationDao _configDao;
    @Inject
    private AccountJoinDao _accountJoinDao;
    @Inject
    private AccountDao _accountDao;

    protected ProjectJoinDaoImpl() {

        prjSearch = createSearchBuilder();
        prjSearch.and("idIN", prjSearch.entity().getId(), SearchCriteria.Op.IN);
        prjSearch.done();

        prjIdSearch = createSearchBuilder();
        prjIdSearch.and("id", prjIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        prjIdSearch.done();

        this._count = "select count(distinct id) from project_view WHERE ";
    }

    @Override
    public ProjectResponse newProjectResponse(final ProjectJoinVO proj) {
        final ProjectResponse response = new ProjectResponse();
        response.setId(proj.getUuid());
        response.setName(proj.getName());
        response.setDisplaytext(proj.getDisplayText());
        if (proj.getState() != null) {
            response.setState(proj.getState().toString());
        }
        response.setDomainId(proj.getDomainUuid());
        response.setDomain(proj.getDomainName());

        response.setOwner(proj.getOwner());

        // update tag information
        final Long tag_id = proj.getTagId();
        if (tag_id != null && tag_id.longValue() > 0) {
            final ResourceTagJoinVO vtag = ApiDBUtils.findResourceTagViewById(tag_id);
            if (vtag != null) {
                response.addTag(ApiDBUtils.newResourceTagResponse(vtag, false));
            }
        }

        //set resource limit/count information for the project (by getting the info of the project's account)
        final Account account = _accountDao.findByIdIncludingRemoved(proj.getProjectAccountId());
        final AccountJoinVO accountJn = ApiDBUtils.newAccountView(account);
        _accountJoinDao.setResourceLimits(accountJn, false, response);

        response.setObjectName("project");
        return response;
    }

    @Override
    public ProjectResponse setProjectResponse(final ProjectResponse rsp, final ProjectJoinVO proj) {
        // update tag information
        final Long tag_id = proj.getTagId();
        if (tag_id != null && tag_id.longValue() > 0) {
            final ResourceTagJoinVO vtag = ApiDBUtils.findResourceTagViewById(tag_id);
            if (vtag != null) {
                rsp.addTag(ApiDBUtils.newResourceTagResponse(vtag, false));
            }
        }
        return rsp;
    }

    @Override
    public List<ProjectJoinVO> newProjectView(final Project proj) {
        final SearchCriteria<ProjectJoinVO> sc = prjIdSearch.create();
        sc.setParameters("id", proj.getId());
        return searchIncludingRemoved(sc, null, null, false);
    }

    @Override
    public List<ProjectJoinVO> searchByIds(final Long... prjIds) {
        // set detail batch query size
        int DETAILS_BATCH_SIZE = 2000;
        final String batchCfg = _configDao.getValue("detail.batch.query.size");
        if (batchCfg != null) {
            DETAILS_BATCH_SIZE = Integer.parseInt(batchCfg);
        }
        // query details by batches
        final List<ProjectJoinVO> uvList = new ArrayList<>();
        // query details by batches
        int curr_index = 0;
        if (prjIds.length > DETAILS_BATCH_SIZE) {
            while ((curr_index + DETAILS_BATCH_SIZE) <= prjIds.length) {
                final Long[] ids = new Long[DETAILS_BATCH_SIZE];
                for (int k = 0, j = curr_index; j < curr_index + DETAILS_BATCH_SIZE; j++, k++) {
                    ids[k] = prjIds[j];
                }
                final SearchCriteria<ProjectJoinVO> sc = prjSearch.create();
                sc.setParameters("idIN", ids);
                final List<ProjectJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
                if (vms != null) {
                    uvList.addAll(vms);
                }
                curr_index += DETAILS_BATCH_SIZE;
            }
        }
        if (curr_index < prjIds.length) {
            final int batch_size = (prjIds.length - curr_index);
            // set the ids value
            final Long[] ids = new Long[batch_size];
            for (int k = 0, j = curr_index; j < curr_index + batch_size; j++, k++) {
                ids[k] = prjIds[j];
            }
            final SearchCriteria<ProjectJoinVO> sc = prjSearch.create();
            sc.setParameters("idIN", ids);
            final List<ProjectJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
            if (vms != null) {
                uvList.addAll(vms);
            }
        }
        return uvList;
    }
}
