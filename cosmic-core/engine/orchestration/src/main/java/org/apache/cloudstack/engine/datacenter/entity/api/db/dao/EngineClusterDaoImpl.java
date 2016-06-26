package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

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
import com.cloud.utils.db.UpdateBuilder;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineClusterVO;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostPodVO;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component(value = "EngineClusterDao")
public class EngineClusterDaoImpl extends GenericDaoBase<EngineClusterVO, Long> implements EngineClusterDao {
    private static final Logger s_logger = LoggerFactory.getLogger(EngineClusterDaoImpl.class);
    private static final String GET_POD_CLUSTER_MAP_PREFIX = "SELECT pod_id, id FROM cloud.cluster WHERE cluster.id IN( ";
    private static final String GET_POD_CLUSTER_MAP_SUFFIX = " )";
    protected final SearchBuilder<EngineClusterVO> PodSearch;
    protected final SearchBuilder<EngineClusterVO> HyTypeWithoutGuidSearch;
    protected final SearchBuilder<EngineClusterVO> AvailHyperSearch;
    protected final SearchBuilder<EngineClusterVO> ZoneSearch;
    protected final SearchBuilder<EngineClusterVO> ZoneHyTypeSearch;
    protected SearchBuilder<EngineClusterVO> StateChangeSearch;
    protected SearchBuilder<EngineClusterVO> UUIDSearch;
    @Inject
    protected EngineHostPodDao _hostPodDao;

    protected EngineClusterDaoImpl() {
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

        UUIDSearch = createSearchBuilder();
        UUIDSearch.and("uuid", UUIDSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        UUIDSearch.done();

        StateChangeSearch = createSearchBuilder();
        StateChangeSearch.and("id", StateChangeSearch.entity().getId(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("state", StateChangeSearch.entity().getState(), SearchCriteria.Op.EQ);
        StateChangeSearch.done();
    }

    @Override
    public List<EngineClusterVO> listByPodId(final long podId) {
        final SearchCriteria<EngineClusterVO> sc = PodSearch.create();
        sc.setParameters("pod", podId);

        return listBy(sc);
    }

    @Override
    public EngineClusterVO findBy(final String name, final long podId) {
        final SearchCriteria<EngineClusterVO> sc = PodSearch.create();
        sc.setParameters("pod", podId);
        sc.setParameters("name", name);

        return findOneBy(sc);
    }

    @Override
    public List<EngineClusterVO> listByHyTypeWithoutGuid(final String hyType) {
        final SearchCriteria<EngineClusterVO> sc = HyTypeWithoutGuidSearch.create();
        sc.setParameters("hypervisorType", hyType);

        return listBy(sc);
    }

    @Override
    public List<EngineClusterVO> listByZoneId(final long zoneId) {
        final SearchCriteria<EngineClusterVO> sc = ZoneSearch.create();
        sc.setParameters("dataCenterId", zoneId);
        return listBy(sc);
    }

    @Override
    public List<HypervisorType> getAvailableHypervisorInZone(final Long zoneId) {
        final SearchCriteria<EngineClusterVO> sc = AvailHyperSearch.create();
        if (zoneId != null) {
            sc.setParameters("zoneId", zoneId);
        }
        final List<EngineClusterVO> clusters = listBy(sc);
        final List<HypervisorType> hypers = new ArrayList<>(4);
        for (final EngineClusterVO cluster : clusters) {
            hypers.add(cluster.getHypervisorType());
        }

        return hypers;
    }

    @Override
    public List<EngineClusterVO> listByDcHyType(final long dcId, final String hyType) {
        final SearchCriteria<EngineClusterVO> sc = ZoneHyTypeSearch.create();
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
        final GenericSearchBuilder<EngineClusterVO, Long> clusterIdSearch = createSearchBuilder(Long.class);
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

        final GenericSearchBuilder<EngineHostPodVO, Long> disabledPodIdSearch = _hostPodDao.createSearchBuilder(Long.class);
        disabledPodIdSearch.selectFields(disabledPodIdSearch.entity().getId());
        disabledPodIdSearch.and("dataCenterId", disabledPodIdSearch.entity().getDataCenterId(), Op.EQ);
        disabledPodIdSearch.and("allocationState", disabledPodIdSearch.entity().getAllocationState(), Op.EQ);

        final GenericSearchBuilder<EngineClusterVO, Long> clusterIdSearch = createSearchBuilder(Long.class);
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
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final EngineClusterVO cluster = createForUpdate();
        cluster.setName(null);
        cluster.setGuid(null);

        update(id, cluster);

        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }

    @Override
    public boolean updateState(final State currentState, final Event event, final State nextState, final DataCenterResourceEntity clusterEntity, final Object data) {

        final EngineClusterVO vo = findById(clusterEntity.getId());

        final Date oldUpdatedTime = vo.getLastUpdated();

        final SearchCriteria<EngineClusterVO> sc = StateChangeSearch.create();
        sc.setParameters("id", vo.getId());
        sc.setParameters("state", currentState);

        final UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "lastUpdated", new Date());

        final int rows = update(vo, sc);

        if (rows == 0 && s_logger.isDebugEnabled()) {
            final EngineClusterVO dbCluster = findByIdIncludingRemoved(vo.getId());
            if (dbCluster != null) {
                final StringBuilder str = new StringBuilder("Unable to update ").append(vo.toString());
                str.append(": DB Data={id=")
                   .append(dbCluster.getId())
                   .append("; state=")
                   .append(dbCluster.getState())
                   .append(";updatedTime=")
                   .append(dbCluster.getLastUpdated());
                str.append(": New Data={id=")
                   .append(vo.getId())
                   .append("; state=")
                   .append(nextState)
                   .append("; event=")
                   .append(event)
                   .append("; updatedTime=")
                   .append(vo.getLastUpdated());
                str.append(": stale Data={id=")
                   .append(vo.getId())
                   .append("; state=")
                   .append(currentState)
                   .append("; event=")
                   .append(event)
                   .append("; updatedTime=")
                   .append(oldUpdatedTime);
            } else {
                s_logger.debug("Unable to update dataCenter: id=" + vo.getId() + ", as there is no such dataCenter exists in the database anymore");
            }
        }
        return rows > 0;
    }
}
