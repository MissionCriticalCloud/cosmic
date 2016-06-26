package org.apache.cloudstack.region.dao;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.region.RegionVO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RegionDaoImpl extends GenericDaoBase<RegionVO, Integer> implements RegionDao {
    private static final Logger s_logger = LoggerFactory.getLogger(RegionDaoImpl.class);
    protected SearchBuilder<RegionVO> NameSearch;
    protected SearchBuilder<RegionVO> AllFieldsSearch;

    public RegionDaoImpl() {
        NameSearch = createSearchBuilder();
        NameSearch.and("name", NameSearch.entity().getName(), SearchCriteria.Op.EQ);
        NameSearch.done();
    }

    @Override
    public RegionVO findByName(final String name) {
        final SearchCriteria<RegionVO> sc = NameSearch.create();
        sc.setParameters("name", name);
        return findOneBy(sc);
    }

    @Override
    public int getRegionId() {
        return 1;
    }
}
