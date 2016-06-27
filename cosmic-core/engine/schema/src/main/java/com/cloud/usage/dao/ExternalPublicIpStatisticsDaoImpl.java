package com.cloud.usage.dao;

import com.cloud.usage.ExternalPublicIpStatisticsVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ExternalPublicIpStatisticsDaoImpl extends GenericDaoBase<ExternalPublicIpStatisticsVO, Long> implements ExternalPublicIpStatisticsDao {

    private final SearchBuilder<ExternalPublicIpStatisticsVO> AccountZoneSearch;
    private final SearchBuilder<ExternalPublicIpStatisticsVO> SingleRowSearch;

    public ExternalPublicIpStatisticsDaoImpl() {
        AccountZoneSearch = createSearchBuilder();
        AccountZoneSearch.and("accountId", AccountZoneSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountZoneSearch.and("zoneId", AccountZoneSearch.entity().getZoneId(), SearchCriteria.Op.EQ);
        AccountZoneSearch.done();

        SingleRowSearch = createSearchBuilder();
        SingleRowSearch.and("accountId", SingleRowSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        SingleRowSearch.and("zoneId", SingleRowSearch.entity().getZoneId(), SearchCriteria.Op.EQ);
        SingleRowSearch.and("publicIp", SingleRowSearch.entity().getPublicIpAddress(), SearchCriteria.Op.EQ);
        SingleRowSearch.done();
    }

    @Override
    public ExternalPublicIpStatisticsVO lock(final long accountId, final long zoneId, final String publicIpAddress) {
        final SearchCriteria<ExternalPublicIpStatisticsVO> sc = getSingleRowSc(accountId, zoneId, publicIpAddress);
        return lockOneRandomRow(sc, true);
    }

    @Override
    public ExternalPublicIpStatisticsVO findBy(final long accountId, final long zoneId, final String publicIpAddress) {
        final SearchCriteria<ExternalPublicIpStatisticsVO> sc = getSingleRowSc(accountId, zoneId, publicIpAddress);
        return findOneBy(sc);
    }

    @Override
    public List<ExternalPublicIpStatisticsVO> listBy(final long accountId, final long zoneId) {
        final SearchCriteria<ExternalPublicIpStatisticsVO> sc = AccountZoneSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("zoneId", zoneId);
        return search(sc, null);
    }

    private SearchCriteria<ExternalPublicIpStatisticsVO> getSingleRowSc(final long accountId, final long zoneId, final String publicIpAddress) {
        final SearchCriteria<ExternalPublicIpStatisticsVO> sc = SingleRowSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("zoneId", zoneId);
        sc.setParameters("publicIp", publicIpAddress);
        return sc;
    }
}
