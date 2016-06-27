package com.cloud.network.vpc.dao;

import com.cloud.network.Network;
import com.cloud.network.vpc.Vpc;
import com.cloud.network.vpc.VpcServiceMapVO;
import com.cloud.network.vpc.VpcVO;
import com.cloud.server.ResourceTag.ResourceObjectType;
import com.cloud.tags.dao.ResourceTagDao;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
@DB()
public class VpcDaoImpl extends GenericDaoBase<VpcVO, Long> implements VpcDao {
    final GenericSearchBuilder<VpcVO, Integer> CountByOfferingId;
    final SearchBuilder<VpcVO> AllFieldsSearch;
    final GenericSearchBuilder<VpcVO, Long> CountByAccountId;

    @Inject
    ResourceTagDao _tagsDao;
    @Inject
    VpcServiceMapDao _vpcSvcMap;

    protected VpcDaoImpl() {
        super();

        CountByOfferingId = createSearchBuilder(Integer.class);
        CountByOfferingId.select(null, Func.COUNT, CountByOfferingId.entity().getId());
        CountByOfferingId.and("offeringId", CountByOfferingId.entity().getVpcOfferingId(), Op.EQ);
        CountByOfferingId.and("removed", CountByOfferingId.entity().getRemoved(), Op.NULL);
        CountByOfferingId.done();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("accountId", AllFieldsSearch.entity().getAccountId(), Op.EQ);
        AllFieldsSearch.done();

        CountByAccountId = createSearchBuilder(Long.class);
        CountByAccountId.select(null, Func.COUNT, CountByAccountId.entity().getId());
        CountByAccountId.and("accountId", CountByAccountId.entity().getAccountId(), Op.EQ);
        CountByAccountId.and("removed", CountByAccountId.entity().getRemoved(), Op.NULL);
        CountByAccountId.done();
    }

    @Override
    public int getVpcCountByOfferingId(final long offId) {
        final SearchCriteria<Integer> sc = CountByOfferingId.create();
        sc.setParameters("offeringId", offId);
        final List<Integer> results = customSearch(sc, null);
        return results.get(0);
    }

    @Override
    public Vpc getActiveVpcById(final long vpcId) {
        final SearchCriteria<VpcVO> sc = AllFieldsSearch.create();
        sc.setParameters("id", vpcId);
        sc.setParameters("state", Vpc.State.Enabled);
        return findOneBy(sc);
    }

    @Override
    public List<? extends Vpc> listByAccountId(final long accountId) {
        final SearchCriteria<VpcVO> sc = AllFieldsSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc, null);
    }

    @Override
    public List<VpcVO> listInactiveVpcs() {
        final SearchCriteria<VpcVO> sc = AllFieldsSearch.create();
        sc.setParameters("state", Vpc.State.Inactive);
        return listBy(sc, null);
    }

    @Override
    public long countByAccountId(final long accountId) {
        final SearchCriteria<Long> sc = CountByAccountId.create();
        sc.setParameters("accountId", accountId);
        final List<Long> results = customSearch(sc, null);
        return results.get(0);
    }

    @Override
    @DB
    public VpcVO persist(final VpcVO vpc, final Map<String, List<String>> serviceProviderMap) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final VpcVO newVpc = super.persist(vpc);
        persistVpcServiceProviders(vpc.getId(), serviceProviderMap);
        txn.commit();
        return newVpc;
    }

    @Override
    @DB
    public void persistVpcServiceProviders(final long vpcId, final Map<String, List<String>> serviceProviderMap) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        for (final String service : serviceProviderMap.keySet()) {
            for (final String provider : serviceProviderMap.get(service)) {
                final VpcServiceMapVO serviceMap = new VpcServiceMapVO(vpcId, Network.Service.getService(service), Network.Provider.getProvider(provider));
                _vpcSvcMap.persist(serviceMap);
            }
        }
        txn.commit();
    }

    @Override
    @DB
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final VpcVO entry = findById(id);
        if (entry != null) {
            _tagsDao.removeByIdAndType(id, ResourceObjectType.Vpc);
        }
        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }
}
