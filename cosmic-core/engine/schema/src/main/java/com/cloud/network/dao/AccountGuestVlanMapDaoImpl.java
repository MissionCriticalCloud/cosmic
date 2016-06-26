package com.cloud.network.dao;

import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB
public class AccountGuestVlanMapDaoImpl extends GenericDaoBase<AccountGuestVlanMapVO, Long> implements AccountGuestVlanMapDao {

    protected SearchBuilder<AccountGuestVlanMapVO> AccountSearch;
    protected SearchBuilder<AccountGuestVlanMapVO> GuestVlanSearch;
    protected SearchBuilder<AccountGuestVlanMapVO> PhysicalNetworkSearch;

    public AccountGuestVlanMapDaoImpl() {
        super();
        AccountSearch = createSearchBuilder();
        AccountSearch.and("accountId", AccountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountSearch.done();

        GuestVlanSearch = createSearchBuilder();
        GuestVlanSearch.and("guestVlanId", GuestVlanSearch.entity().getId(), SearchCriteria.Op.EQ);
        GuestVlanSearch.done();

        PhysicalNetworkSearch = createSearchBuilder();
        PhysicalNetworkSearch.and("physicalNetworkId", PhysicalNetworkSearch.entity().getId(), SearchCriteria.Op.EQ);
        PhysicalNetworkSearch.done();
    }

    @Override
    public List<AccountGuestVlanMapVO> listAccountGuestVlanMapsByAccount(final long accountId) {
        final SearchCriteria<AccountGuestVlanMapVO> sc = AccountSearch.create();
        sc.setParameters("accountId", accountId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<AccountGuestVlanMapVO> listAccountGuestVlanMapsByVlan(final long guestVlanId) {
        final SearchCriteria<AccountGuestVlanMapVO> sc = GuestVlanSearch.create();
        sc.setParameters("guestVlanId", guestVlanId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<AccountGuestVlanMapVO> listAccountGuestVlanMapsByPhysicalNetwork(final long physicalNetworkId) {
        final SearchCriteria<AccountGuestVlanMapVO> sc = GuestVlanSearch.create();
        sc.setParameters("physicalNetworkId", physicalNetworkId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public int removeByAccountId(final long accountId) {
        final SearchCriteria<AccountGuestVlanMapVO> sc = AccountSearch.create();
        sc.setParameters("accountId", accountId);
        return expunge(sc);
    }
}
