package com.cloud.dc.dao;

import com.cloud.dc.HostPodVO;
import com.cloud.org.Grouping;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HostPodDaoImpl extends GenericDaoBase<HostPodVO, Long> implements HostPodDao {
    private static final Logger s_logger = LoggerFactory.getLogger(HostPodDaoImpl.class);

    protected SearchBuilder<HostPodVO> DataCenterAndNameSearch;
    protected SearchBuilder<HostPodVO> DataCenterIdSearch;
    protected GenericSearchBuilder<HostPodVO, Long> PodIdSearch;

    public HostPodDaoImpl() {
        DataCenterAndNameSearch = createSearchBuilder();
        DataCenterAndNameSearch.and("dc", DataCenterAndNameSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        DataCenterAndNameSearch.and("name", DataCenterAndNameSearch.entity().getName(), SearchCriteria.Op.EQ);
        DataCenterAndNameSearch.done();

        DataCenterIdSearch = createSearchBuilder();
        DataCenterIdSearch.and("dcId", DataCenterIdSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        DataCenterIdSearch.done();

        PodIdSearch = createSearchBuilder(Long.class);
        PodIdSearch.selectFields(PodIdSearch.entity().getId());
        PodIdSearch.and("dataCenterId", PodIdSearch.entity().getDataCenterId(), Op.EQ);
        PodIdSearch.and("allocationState", PodIdSearch.entity().getAllocationState(), Op.EQ);
        PodIdSearch.done();
    }

    @Override
    public List<HostPodVO> listByDataCenterId(final long id) {
        final SearchCriteria<HostPodVO> sc = DataCenterIdSearch.create();
        sc.setParameters("dcId", id);

        return listBy(sc);
    }

    @Override
    public HostPodVO findByName(final String name, final long dcId) {
        final SearchCriteria<HostPodVO> sc = DataCenterAndNameSearch.create();
        sc.setParameters("dc", dcId);
        sc.setParameters("name", name);

        return findOneBy(sc);
    }

    @Override
    public HashMap<Long, List<Object>> getCurrentPodCidrSubnets(final long zoneId, final long podIdToSkip) {
        final HashMap<Long, List<Object>> currentPodCidrSubnets = new HashMap<>();

        final String selectSql = "SELECT id, cidr_address, cidr_size FROM host_pod_ref WHERE data_center_id=" + zoneId + " and removed IS NULL";
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            final PreparedStatement stmt = txn.prepareAutoCloseStatement(selectSql);
            final ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                final Long podId = rs.getLong("id");
                if (podId.longValue() == podIdToSkip) {
                    continue;
                }
                final String cidrAddress = rs.getString("cidr_address");
                final long cidrSize = rs.getLong("cidr_size");
                final List<Object> cidrPair = new ArrayList<>();
                cidrPair.add(0, cidrAddress);
                cidrPair.add(1, new Long(cidrSize));
                currentPodCidrSubnets.put(podId, cidrPair);
            }
        } catch (final SQLException ex) {
            s_logger.warn("DB exception " + ex.getMessage(), ex);
            return null;
        }

        return currentPodCidrSubnets;
    }

    @Override
    public List<Long> listDisabledPods(final long zoneId) {
        final SearchCriteria<Long> sc = PodIdSearch.create();
        sc.addAnd("dataCenterId", SearchCriteria.Op.EQ, zoneId);
        sc.addAnd("allocationState", SearchCriteria.Op.EQ, Grouping.AllocationState.Disabled);
        return customSearch(sc, null);
    }

    @Override
    public List<Long> listAllPods(final long zoneId) {
        final SearchCriteria<Long> sc = PodIdSearch.create();
        sc.addAnd("dataCenterId", SearchCriteria.Op.EQ, zoneId);
        return customSearch(sc, null);
    }

    @Override
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final HostPodVO pod = createForUpdate();
        pod.setName(null);

        update(id, pod);

        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }
}
