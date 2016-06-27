package com.cloud.network.vpc.dao;

import com.cloud.network.vpc.VpcOfferingVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import org.springframework.stereotype.Component;

@Component
@DB()
public class VpcOfferingDaoImpl extends GenericDaoBase<VpcOfferingVO, Long> implements VpcOfferingDao {
    final SearchBuilder<VpcOfferingVO> AllFieldsSearch;

    protected VpcOfferingDaoImpl() {
        super();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("id", AllFieldsSearch.entity().getId(), Op.EQ);
        AllFieldsSearch.and("state", AllFieldsSearch.entity().getState(), Op.EQ);
        AllFieldsSearch.and("name", AllFieldsSearch.entity().getName(), Op.EQ);
        AllFieldsSearch.and("uName", AllFieldsSearch.entity().getUniqueName(), Op.EQ);
        AllFieldsSearch.and("displayText", AllFieldsSearch.entity().getDisplayText(), Op.EQ);
        AllFieldsSearch.and("svcOffId", AllFieldsSearch.entity().getServiceOfferingId(), Op.EQ);
        AllFieldsSearch.done();
    }

    @Override
    @DB
    public boolean remove(final Long vpcOffId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final VpcOfferingVO offering = findById(vpcOffId);
        offering.setUniqueName(null);
        update(vpcOffId, offering);
        final boolean result = super.remove(vpcOffId);
        txn.commit();
        return result;
    }

    @Override
    public VpcOfferingVO findByUniqueName(final String uniqueName) {
        final SearchCriteria<VpcOfferingVO> sc = AllFieldsSearch.create();
        sc.setParameters("uName", uniqueName);
        return findOneBy(sc);
    }
}
