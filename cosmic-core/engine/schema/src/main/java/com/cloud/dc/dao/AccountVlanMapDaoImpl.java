package com.cloud.dc.dao;

import com.cloud.dc.AccountVlanMapVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AccountVlanMapDaoImpl extends GenericDaoBase<AccountVlanMapVO, Long> implements AccountVlanMapDao {

    protected SearchBuilder<AccountVlanMapVO> AccountSearch;
    protected SearchBuilder<AccountVlanMapVO> VlanSearch;
    protected SearchBuilder<AccountVlanMapVO> AccountVlanSearch;

    public AccountVlanMapDaoImpl() {
        AccountSearch = createSearchBuilder();
        AccountSearch.and("accountId", AccountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountSearch.done();

        VlanSearch = createSearchBuilder();
        VlanSearch.and("vlanDbId", VlanSearch.entity().getVlanDbId(), SearchCriteria.Op.EQ);
        VlanSearch.done();

        AccountVlanSearch = createSearchBuilder();
        AccountVlanSearch.and("accountId", AccountVlanSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountVlanSearch.and("vlanDbId", AccountVlanSearch.entity().getVlanDbId(), SearchCriteria.Op.EQ);
        AccountVlanSearch.done();
    }

    @Override
    public List<AccountVlanMapVO> listAccountVlanMapsByAccount(final long accountId) {
        final SearchCriteria<AccountVlanMapVO> sc = AccountSearch.create();
        sc.setParameters("accountId", accountId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<AccountVlanMapVO> listAccountVlanMapsByVlan(final long vlanDbId) {
        final SearchCriteria<AccountVlanMapVO> sc = VlanSearch.create();
        sc.setParameters("vlanDbId", vlanDbId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public AccountVlanMapVO findAccountVlanMap(final long accountId, final long vlanDbId) {
        final SearchCriteria<AccountVlanMapVO> sc = AccountVlanSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("vlanDbId", vlanDbId);
        return findOneIncludingRemovedBy(sc);
    }
}
