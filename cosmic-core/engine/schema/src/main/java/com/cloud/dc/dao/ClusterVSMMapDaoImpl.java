package com.cloud.dc.dao;

import com.cloud.dc.ClusterVSMMapVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.TransactionLegacy;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@DB
public class ClusterVSMMapDaoImpl extends GenericDaoBase<ClusterVSMMapVO, Long> implements ClusterVSMMapDao {

    final SearchBuilder<ClusterVSMMapVO> ClusterSearch;
    final SearchBuilder<ClusterVSMMapVO> VsmSearch;

    public ClusterVSMMapDaoImpl() {
        //super();

        ClusterSearch = createSearchBuilder();
        ClusterSearch.and("clusterId", ClusterSearch.entity().getClusterId(), SearchCriteria.Op.EQ);
        ClusterSearch.done();

        VsmSearch = createSearchBuilder();
        VsmSearch.and("vsmId", VsmSearch.entity().getVsmId(), SearchCriteria.Op.EQ);
        VsmSearch.done();
    }

    @Override
    public ClusterVSMMapVO findByClusterId(final long clusterId) {
        final SearchCriteria<ClusterVSMMapVO> sc = ClusterSearch.create();
        sc.setParameters("clusterId", clusterId);
        return findOneBy(sc);
    }

    @Override
    public List<ClusterVSMMapVO> listByVSMId(final long vsmId) {
        final SearchCriteria<ClusterVSMMapVO> sc = VsmSearch.create();
        sc.setParameters("vsmId", vsmId);
        return listBy(sc);
    }

    @Override
    public boolean removeByVsmId(final long vsmId) {
        final SearchCriteria<ClusterVSMMapVO> sc = VsmSearch.create();
        sc.setParameters("vsmId", vsmId);
        this.remove(sc);
        return true;
    }

    @Override
    public boolean removeByClusterId(final long clusterId) {
        final SearchCriteria<ClusterVSMMapVO> sc = ClusterSearch.create();
        sc.setParameters("clusterId", clusterId);
        this.remove(sc);
        return true;
    }

    @Override
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final ClusterVSMMapVO cluster = createForUpdate();
        //cluster.setClusterId(null);
        //cluster.setVsmId(null);

        update(id, cluster);

        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }
}
