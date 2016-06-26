package com.cloud.dc.dao;

import com.cloud.dc.ClusterVO;
import com.cloud.dc.HostPodVO;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.org.Grouping;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Func;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ClusterDaoImpl extends GenericDaoBase<ClusterVO, Long> implements ClusterDao {

    private static final String GET_POD_CLUSTER_MAP_PREFIX = "SELECT pod_id, id FROM cloud.cluster WHERE cluster.id IN( ";
    private static final String GET_POD_CLUSTER_MAP_SUFFIX = " )";
    protected final SearchBuilder<ClusterVO> PodSearch;
    protected final SearchBuilder<ClusterVO> HyTypeWithoutGuidSearch;
    protected final SearchBuilder<ClusterVO> AvailHyperSearch;
    protected final SearchBuilder<ClusterVO> ZoneSearch;
    protected final SearchBuilder<ClusterVO> ZoneHyTypeSearch;
    protected final SearchBuilder<ClusterVO> ZoneClusterSearch;
    protected GenericSearchBuilder<ClusterVO, Long> ClusterIdSearch;
    @Inject
    protected HostPodDao _hostPodDao;

    public ClusterDaoImpl() {
        super();

        HyTypeWithoutGuidSearch = createSearchBuilder();
        HyTypeWithoutGuidSearch.and("hypervisorType", HyTypeWithoutGuidSearch.entity().getHypervisorType(), SearchCriteria.Op.EQ);
        HyTypeWithoutGuidSearch.and("guid", HyTypeWithoutGuidSearch.entity().getGuid(), SearchCriteria.Op.NULL);
        HyTypeWithoutGuidSearch.done();

        ZoneHyTypeSearch = createSearchBuilder();
        ZoneHyTypeSearch.and("hypervisorType", ZoneHyTypeSearch.entity().getHypervisorType(), SearchCriteria.Op.EQ);
        ZoneHyTypeSearch.and("dataCenterId", ZoneHyTypeSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        ZoneHyTypeSearch.done();

        PodSearch = createSearchBuilder();
        PodSearch.and("pod", PodSearch.entity().getPodId(), SearchCriteria.Op.EQ);
        PodSearch.and("name", PodSearch.entity().getName(), SearchCriteria.Op.EQ);
        PodSearch.done();

        ZoneSearch = createSearchBuilder();
        ZoneSearch.and("dataCenterId", ZoneSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        ZoneSearch.groupBy(ZoneSearch.entity().getHypervisorType());
        ZoneSearch.done();

        AvailHyperSearch = createSearchBuilder();
        AvailHyperSearch.and("zoneId", AvailHyperSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        AvailHyperSearch.select(null, Func.DISTINCT, AvailHyperSearch.entity().getHypervisorType());
        AvailHyperSearch.done();

        ZoneClusterSearch = createSearchBuilder();
        ZoneClusterSearch.and("dataCenterId", ZoneClusterSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        ZoneClusterSearch.done();

        ClusterIdSearch = createSearchBuilder(Long.class);
        ClusterIdSearch.selectFields(ClusterIdSearch.entity().getId());
        ClusterIdSearch.and("dataCenterId", ClusterIdSearch.entity().getDataCenterId(), Op.EQ);
        ClusterIdSearch.done();
    }

    @Override
    public List<ClusterVO> listByPodId(final long podId) {
        final SearchCriteria<ClusterVO> sc = PodSearch.create();
        sc.setParameters("pod", podId);

        return listBy(sc);
    }

    @Override
    public ClusterVO findBy(final String name, final long podId) {
        final SearchCriteria<ClusterVO> sc = PodSearch.create();
        sc.setParameters("pod", podId);
        sc.setParameters("name", name);

        return findOneBy(sc);
    }

    @Override
    public List<ClusterVO> listByHyTypeWithoutGuid(final String hyType) {
        final SearchCriteria<ClusterVO> sc = HyTypeWithoutGuidSearch.create();
        sc.setParameters("hypervisorType", hyType);

        return listBy(sc);
    }

    @Override
    public List<ClusterVO> listByZoneId(final long zoneId) {
        final SearchCriteria<ClusterVO> sc = ZoneSearch.create();
        sc.setParameters("dataCenterId", zoneId);
        return listBy(sc);
    }

    @Override
    public List<HypervisorType> getAvailableHypervisorInZone(final Long zoneId) {
        final SearchCriteria<ClusterVO> sc = AvailHyperSearch.create();
        if (zoneId != null) {
            sc.setParameters("zoneId", zoneId);
        }
        final List<ClusterVO> clusters = listBy(sc);
        final List<HypervisorType> hypers = new ArrayList<>(4);
        for (final ClusterVO cluster : clusters) {
            hypers.add(cluster.getHypervisorType());
        }

        return hypers;
    }

    @Override
    public List<ClusterVO> listByDcHyType(final long dcId, final String hyType) {
        final SearchCriteria<ClusterVO> sc = ZoneHyTypeSearch.create();
        sc.setParameters("dataCenterId", dcId);
        sc.setParameters("hypervisorType", hyType);
        return listBy(sc);
    }

    @Override
    public Map<Long, List<Long>> getPodClusterIdMap(final List<Long> clusterIds) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final Map<Long, List<Long>> result = new HashMap<>();

        try {
            final StringBuilder sql = new StringBuilder(GET_POD_CLUSTER_MAP_PREFIX);
            if (clusterIds.size() > 0) {
                for (final Long clusterId : clusterIds) {
                    sql.append(clusterId).append(",");
                }
                sql.delete(sql.length() - 1, sql.length());
                sql.append(GET_POD_CLUSTER_MAP_SUFFIX);
            }

            pstmt = txn.prepareAutoCloseStatement(sql.toString());
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                final Long podId = rs.getLong(1);
                final Long clusterIdInPod = rs.getLong(2);
                if (result.containsKey(podId)) {
                    final List<Long> clusterList = result.get(podId);
                    clusterList.add(clusterIdInPod);
                    result.put(podId, clusterList);
                } else {
                    final List<Long> clusterList = new ArrayList<>();
                    clusterList.add(clusterIdInPod);
                    result.put(podId, clusterList);
                }
            }
            return result;
        } catch (final SQLException e) {
            throw new CloudRuntimeException("DB Exception on: " + GET_POD_CLUSTER_MAP_PREFIX, e);
        } catch (final Throwable e) {
            throw new CloudRuntimeException("Caught: " + GET_POD_CLUSTER_MAP_PREFIX, e);
        }
    }

    @Override
    public List<Long> listDisabledClusters(final long zoneId, final Long podId) {
        final GenericSearchBuilder<ClusterVO, Long> clusterIdSearch = createSearchBuilder(Long.class);
        clusterIdSearch.selectFields(clusterIdSearch.entity().getId());
        clusterIdSearch.and("dataCenterId", clusterIdSearch.entity().getDataCenterId(), Op.EQ);
        if (podId != null) {
            clusterIdSearch.and("podId", clusterIdSearch.entity().getPodId(), Op.EQ);
        }
        clusterIdSearch.and("allocationState", clusterIdSearch.entity().getAllocationState(), Op.EQ);
        clusterIdSearch.done();

        final SearchCriteria<Long> sc = clusterIdSearch.create();
        sc.addAnd("dataCenterId", SearchCriteria.Op.EQ, zoneId);
        if (podId != null) {
            sc.addAnd("podId", SearchCriteria.Op.EQ, podId);
        }
        sc.addAnd("allocationState", SearchCriteria.Op.EQ, Grouping.AllocationState.Disabled);
        return customSearch(sc, null);
    }

    @Override
    public List<Long> listClustersWithDisabledPods(final long zoneId) {

        final GenericSearchBuilder<HostPodVO, Long> disabledPodIdSearch = _hostPodDao.createSearchBuilder(Long.class);
        disabledPodIdSearch.selectFields(disabledPodIdSearch.entity().getId());
        disabledPodIdSearch.and("dataCenterId", disabledPodIdSearch.entity().getDataCenterId(), Op.EQ);
        disabledPodIdSearch.and("allocationState", disabledPodIdSearch.entity().getAllocationState(), Op.EQ);

        final GenericSearchBuilder<ClusterVO, Long> clusterIdSearch = createSearchBuilder(Long.class);
        clusterIdSearch.selectFields(clusterIdSearch.entity().getId());
        clusterIdSearch.join("disabledPodIdSearch", disabledPodIdSearch, clusterIdSearch.entity().getPodId(), disabledPodIdSearch.entity().getId(),
                JoinBuilder.JoinType.INNER);
        clusterIdSearch.done();

        final SearchCriteria<Long> sc = clusterIdSearch.create();
        sc.setJoinParameters("disabledPodIdSearch", "dataCenterId", zoneId);
        sc.setJoinParameters("disabledPodIdSearch", "allocationState", Grouping.AllocationState.Disabled);

        return customSearch(sc, null);
    }

    @Override
    public List<ClusterVO> listClustersByDcId(final long zoneId) {
        final SearchCriteria<ClusterVO> sc = ZoneClusterSearch.create();
        sc.setParameters("dataCenterId", zoneId);
        return listBy(sc);
    }

    @Override
    public List<Long> listAllCusters(final long zoneId) {
        final SearchCriteria<Long> sc = ClusterIdSearch.create();
        sc.setParameters("dataCenterId", zoneId);
        return customSearch(sc, null);
    }

    @Override
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final ClusterVO cluster = createForUpdate();
        cluster.setName(null);
        cluster.setGuid(null);

        update(id, cluster);

        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }
}
