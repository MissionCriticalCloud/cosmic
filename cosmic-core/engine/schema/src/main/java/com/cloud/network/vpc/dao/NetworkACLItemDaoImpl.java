package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.NetworkACLItem.State;
import com.cloud.network.vpc.NetworkACLItemCidrsDao;
import com.cloud.network.vpc.NetworkACLItemDao;
import com.cloud.network.vpc.NetworkACLItemVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import javax.inject.Inject;
import java.util.List;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@DB()
public class NetworkACLItemDaoImpl extends GenericDaoBase<NetworkACLItemVO, Long> implements NetworkACLItemDao {
    private static final Logger s_logger = LoggerFactory.getLogger(NetworkACLItemDaoImpl.class);

    protected final SearchBuilder<NetworkACLItemVO> AllFieldsSearch;
    protected final SearchBuilder<NetworkACLItemVO> NotRevokedSearch;
    protected final SearchBuilder<NetworkACLItemVO> ReleaseSearch;
    protected final GenericSearchBuilder<NetworkACLItemVO, Integer> MaxNumberSearch;

    @Inject
    protected NetworkACLItemCidrsDao _networkACLItemCidrsDao;

    protected NetworkACLItemDaoImpl() {
        super();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("protocol", AllFieldsSearch.entity().getProtocol(), Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("aclId", AllFieldsSearch.entity().getAclId(), Op.EQ);
        AllFieldsSearch.and("trafficType", AllFieldsSearch.entity().getTrafficType(), Op.EQ);
        AllFieldsSearch.and("number", AllFieldsSearch.entity().getNumber(), Op.EQ);
        AllFieldsSearch.and("action", AllFieldsSearch.entity().getAction(), Op.EQ);
        AllFieldsSearch.done();

        NotRevokedSearch = createSearchBuilder();
        NotRevokedSearch.and("state", NotRevokedSearch.entity().getState(), Op.NEQ);
        NotRevokedSearch.and("protocol", NotRevokedSearch.entity().getProtocol(), Op.EQ);
        NotRevokedSearch.and("sourcePortStart", NotRevokedSearch.entity().getSourcePortStart(), Op.EQ);
        NotRevokedSearch.and("sourcePortEnd", NotRevokedSearch.entity().getSourcePortEnd(), Op.EQ);
        NotRevokedSearch.and("aclId", NotRevokedSearch.entity().getAclId(), Op.EQ);
        NotRevokedSearch.and("trafficType", NotRevokedSearch.entity().getTrafficType(), Op.EQ);
        NotRevokedSearch.done();

        ReleaseSearch = createSearchBuilder();
        ReleaseSearch.and("protocol", ReleaseSearch.entity().getProtocol(), Op.EQ);
        ReleaseSearch.and("ports", ReleaseSearch.entity().getSourcePortStart(), Op.IN);
        ReleaseSearch.done();

        MaxNumberSearch = createSearchBuilder(Integer.class);
        MaxNumberSearch.select(null, SearchCriteria.Func.MAX, MaxNumberSearch.entity().getNumber());
        MaxNumberSearch.and("aclId", MaxNumberSearch.entity().getAclId(), Op.EQ);
        MaxNumberSearch.done();
    }

    @Override
    public NetworkACLItemVO findById(final Long id) {
        final NetworkACLItemVO item = super.findById(id);
        loadCidrs(item);
        return item;
    }

    @Override
    public boolean update(final Long id, final NetworkACLItemVO item) {
        final boolean result = super.update(id, item);
        _networkACLItemCidrsDao.updateCidrs(item.getId(), item.getSourceCidrList());
        return result;
    }

    @Override
    @DB
    public NetworkACLItemVO persist(final NetworkACLItemVO networkAclItem) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();

        final NetworkACLItemVO dbNetworkACLItem = super.persist(networkAclItem);
        saveCidrs(networkAclItem, networkAclItem.getSourceCidrList());
        loadCidrs(dbNetworkACLItem);

        txn.commit();
        return dbNetworkACLItem;
    }

    public void saveCidrs(final NetworkACLItemVO networkACLItem, final List<String> cidrList) {
        if (cidrList == null) {
            return;
        }
        _networkACLItemCidrsDao.persist(networkACLItem.getId(), cidrList);
    }

    @Override
    public boolean setStateToAdd(final NetworkACLItemVO rule) {
        final SearchCriteria<NetworkACLItemVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", rule.getId());
        sc.setParameters("state", State.Staged);

        rule.setState(State.Add);

        return update(rule, sc) > 0;
    }

    @Override
    public boolean revoke(final NetworkACLItemVO rule) {
        rule.setState(State.Revoke);
        return update(rule.getId(), rule);
    }

    @Override
    public List<NetworkACLItemVO> listByACL(final Long aclId) {
        if (aclId == null) {
            return Lists.newArrayList();
        }

        final SearchCriteria<NetworkACLItemVO> sc = AllFieldsSearch.create();
        sc.setParameters("aclId", aclId);
        final List<NetworkACLItemVO> list = listBy(sc);
        for (final NetworkACLItemVO item : list) {
            loadCidrs(item);
        }
        return list;
    }

    @Override
    public int getMaxNumberByACL(final long aclId) {
        final SearchCriteria<Integer> sc = MaxNumberSearch.create();
        sc.setParameters("aclId", aclId);
        final Integer max = customSearch(sc, null).get(0);
        return (max == null) ? 0 : max;
    }

    @Override
    public NetworkACLItemVO findByAclAndNumber(final long aclId, final int number) {
        final SearchCriteria<NetworkACLItemVO> sc = AllFieldsSearch.create();
        sc.setParameters("aclId", aclId);
        sc.setParameters("number", number);
        final NetworkACLItemVO vo = findOneBy(sc);
        if (vo != null) {
            loadCidrs(vo);
        }
        return vo;
    }

    @Override
    public void loadCidrs(final NetworkACLItemVO item) {
        final List<String> cidrs = _networkACLItemCidrsDao.getCidrs(item.getId());
        item.setSourceCidrList(cidrs);
    }
}
