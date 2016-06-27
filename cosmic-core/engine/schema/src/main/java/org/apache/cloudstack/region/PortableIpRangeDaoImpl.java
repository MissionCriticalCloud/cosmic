package org.apache.cloudstack.region;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PortableIpRangeDaoImpl extends GenericDaoBase<PortableIpRangeVO, Long> implements PortableIpRangeDao {

    private final SearchBuilder<PortableIpRangeVO> listByRegionIDSearch;

    public PortableIpRangeDaoImpl() {
        listByRegionIDSearch = createSearchBuilder();
        listByRegionIDSearch.and("regionId", listByRegionIDSearch.entity().getRegionId(), SearchCriteria.Op.EQ);
        listByRegionIDSearch.done();
    }

    @Override
    public List<PortableIpRangeVO> listByRegionId(final int regionIdId) {
        final SearchCriteria<PortableIpRangeVO> sc = listByRegionIDSearch.create();
        sc.setParameters("regionId", regionIdId);
        return listBy(sc);
    }
}
