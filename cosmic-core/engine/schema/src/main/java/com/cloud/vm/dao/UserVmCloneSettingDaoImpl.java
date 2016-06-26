package com.cloud.vm.dao;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.vm.UserVmCloneSettingVO;

import javax.annotation.PostConstruct;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@DB()
public class UserVmCloneSettingDaoImpl extends GenericDaoBase<UserVmCloneSettingVO, Long> implements UserVmCloneSettingDao {
    public static final Logger s_logger = LoggerFactory.getLogger(UserVmCloneSettingDaoImpl.class);

    protected SearchBuilder<UserVmCloneSettingVO> vmIdSearch;
    protected SearchBuilder<UserVmCloneSettingVO> cloneTypeSearch;

    public UserVmCloneSettingDaoImpl() {
    }

    @PostConstruct
    public void init() {
        // Initialize the search builders.
        vmIdSearch = createSearchBuilder();
        vmIdSearch.and("vmId", vmIdSearch.entity().getCloneType(), Op.EQ);
        vmIdSearch.done();

        cloneTypeSearch = createSearchBuilder();
        cloneTypeSearch.and("cloneType", cloneTypeSearch.entity().getCloneType(), Op.EQ);
        cloneTypeSearch.done();
    }

    @Override
    public UserVmCloneSettingVO findByVmId(final long vmId) {
        final SearchCriteria<UserVmCloneSettingVO> sc = vmIdSearch.create();
        sc.setParameters("vmId", vmId);
        return findOneBy(sc);
    }

    @Override
    public List<UserVmCloneSettingVO> listByCloneType(final String cloneType) {
        final SearchCriteria<UserVmCloneSettingVO> sc = cloneTypeSearch.create();
        sc.setParameters("cloneType", cloneType);
        return search(sc, null);
    }
}
