package com.cloud.network.dao;

import com.cloud.network.VpnUser.State;
import com.cloud.network.VpnUserVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class VpnUserDaoImpl extends GenericDaoBase<VpnUserVO, Long> implements VpnUserDao {
    private final SearchBuilder<VpnUserVO> AccountSearch;
    private final SearchBuilder<VpnUserVO> AccountNameSearch;
    private final GenericSearchBuilder<VpnUserVO, Long> VpnUserCount;

    protected VpnUserDaoImpl() {

        AccountSearch = createSearchBuilder();
        AccountSearch.and("accountId", AccountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountSearch.done();

        AccountNameSearch = createSearchBuilder();
        AccountNameSearch.and("accountId", AccountNameSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountNameSearch.and("username", AccountNameSearch.entity().getUsername(), SearchCriteria.Op.EQ);
        AccountNameSearch.done();

        VpnUserCount = createSearchBuilder(Long.class);
        VpnUserCount.and("accountId", VpnUserCount.entity().getAccountId(), SearchCriteria.Op.EQ);
        VpnUserCount.and("state", VpnUserCount.entity().getState(), SearchCriteria.Op.NEQ);
        VpnUserCount.select(null, Func.COUNT, null);
        VpnUserCount.done();
    }

    @Override
    public List<VpnUserVO> listByAccount(final Long accountId) {
        final SearchCriteria<VpnUserVO> sc = AccountSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }

    @Override
    public VpnUserVO findByAccountAndUsername(final Long accountId, final String userName) {
        final SearchCriteria<VpnUserVO> sc = AccountNameSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("username", userName);

        return findOneBy(sc);
    }

    @Override
    public long getVpnUserCount(final Long accountId) {
        final SearchCriteria<Long> sc = VpnUserCount.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("state", State.Revoke);
        final List<Long> rs = customSearch(sc, null);
        if (rs.size() == 0) {
            return 0;
        }

        return rs.get(0);
    }
}
