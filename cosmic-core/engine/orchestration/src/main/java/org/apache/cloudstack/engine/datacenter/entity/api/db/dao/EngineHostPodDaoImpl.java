package org.apache.cloudstack.engine.datacenter.entity.api.db.dao;

import com.cloud.org.Grouping;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.GenericSearchBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State;
import org.apache.cloudstack.engine.datacenter.entity.api.DataCenterResourceEntity.State.Event;
import org.apache.cloudstack.engine.datacenter.entity.api.db.EngineHostPodVO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component(value = "EngineHostPodDao")
public class EngineHostPodDaoImpl extends GenericDaoBase<EngineHostPodVO, Long> implements EngineHostPodDao {
    private static final Logger s_logger = LoggerFactory.getLogger(EngineHostPodDaoImpl.class);

    protected SearchBuilder<EngineHostPodVO> DataCenterAndNameSearch;
    protected SearchBuilder<EngineHostPodVO> DataCenterIdSearch;
    protected SearchBuilder<EngineHostPodVO> UUIDSearch;
    protected SearchBuilder<EngineHostPodVO> StateChangeSearch;

    protected EngineHostPodDaoImpl() {
        DataCenterAndNameSearch = createSearchBuilder();
        DataCenterAndNameSearch.and("dc", DataCenterAndNameSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        DataCenterAndNameSearch.and("name", DataCenterAndNameSearch.entity().getName(), SearchCriteria.Op.EQ);
        DataCenterAndNameSearch.done();

        DataCenterIdSearch = createSearchBuilder();
        DataCenterIdSearch.and("dcId", DataCenterIdSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        DataCenterIdSearch.done();

        UUIDSearch = createSearchBuilder();
        UUIDSearch.and("uuid", UUIDSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        UUIDSearch.done();

        StateChangeSearch = createSearchBuilder();
        StateChangeSearch.and("id", StateChangeSearch.entity().getId(), SearchCriteria.Op.EQ);
        StateChangeSearch.and("state", StateChangeSearch.entity().getState(), SearchCriteria.Op.EQ);
        StateChangeSearch.done();
    }

    @Override
    public List<EngineHostPodVO> listByDataCenterId(final long id) {
        final SearchCriteria<EngineHostPodVO> sc = DataCenterIdSearch.create();
        sc.setParameters("dcId", id);

        return listBy(sc);
    }

    @Override
    public EngineHostPodVO findByName(final String name, final long dcId) {
        final SearchCriteria<EngineHostPodVO> sc = DataCenterAndNameSearch.create();
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
        final GenericSearchBuilder<EngineHostPodVO, Long> podIdSearch = createSearchBuilder(Long.class);
        podIdSearch.selectFields(podIdSearch.entity().getId());
        podIdSearch.and("dataCenterId", podIdSearch.entity().getDataCenterId(), Op.EQ);
        podIdSearch.and("allocationState", podIdSearch.entity().getAllocationState(), Op.EQ);
        podIdSearch.done();

        final SearchCriteria<Long> sc = podIdSearch.create();
        sc.addAnd("dataCenterId", SearchCriteria.Op.EQ, zoneId);
        sc.addAnd("allocationState", SearchCriteria.Op.EQ, Grouping.AllocationState.Disabled);
        return customSearch(sc, null);
    }

    @Override
    public boolean remove(final Long id) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final EngineHostPodVO pod = createForUpdate();
        pod.setName(null);

        update(id, pod);

        final boolean result = super.remove(id);
        txn.commit();
        return result;
    }

    @Override
    public boolean updateState(final State currentState, final Event event, final State nextState, final DataCenterResourceEntity podEntity, final Object data) {

        final EngineHostPodVO vo = findById(podEntity.getId());

        final Date oldUpdatedTime = vo.getLastUpdated();

        final SearchCriteria<EngineHostPodVO> sc = StateChangeSearch.create();
        sc.setParameters("id", vo.getId());
        sc.setParameters("state", currentState);

        final UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "lastUpdated", new Date());

        final int rows = update(vo, sc);

        if (rows == 0 && s_logger.isDebugEnabled()) {
            final EngineHostPodVO dbDC = findByIdIncludingRemoved(vo.getId());
            if (dbDC != null) {
                final StringBuilder str = new StringBuilder("Unable to update ").append(vo.toString());
                str.append(": DB Data={id=").append(dbDC.getId()).append("; state=").append(dbDC.getState()).append(";updatedTime=").append(dbDC.getLastUpdated());
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
