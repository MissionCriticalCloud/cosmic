package com.cloud.storage.dao;

import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.storage.VMTemplateStorageResourceAssoc;
import com.cloud.storage.VMTemplateStorageResourceAssoc.Status;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VMTemplatePoolDaoImpl extends GenericDaoBase<VMTemplateStoragePoolVO, Long> implements VMTemplatePoolDao {
    public static final Logger s_logger = LoggerFactory.getLogger(VMTemplatePoolDaoImpl.class.getName());
    protected static final String UPDATE_TEMPLATE_HOST_REF = "UPDATE template_spool_ref SET download_state = ?, download_pct= ?, last_updated = ? "
            + ", error_str = ?, local_path = ?, job_id = ? " + "WHERE pool_id = ? and template_id = ?";
    protected static final String DOWNLOADS_STATE_DC = "SELECT * FROM template_spool_ref t, storage_pool p where t.pool_id = p.id and p.data_center_id=? "
            + " and t.template_id=? and t.download_state = ?";
    protected static final String DOWNLOADS_STATE_DC_POD =
            "SELECT * FROM template_spool_ref tp, storage_pool_host_ref ph, host h where tp.pool_id = ph.pool_id and ph.host_id = h.id and h.data_center_id=? and h.pod_id=? "
                    + " and tp.template_id=? and tp.download_state=?";
    protected static final String HOST_TEMPLATE_SEARCH =
            "SELECT * FROM template_spool_ref tp, storage_pool_host_ref ph, host h where tp.pool_id = ph.pool_id and ph.host_id = h.id and h.id=? "
                    + " and tp.template_id=? ";
    protected final SearchBuilder<VMTemplateStoragePoolVO> PoolSearch;
    protected final SearchBuilder<VMTemplateStoragePoolVO> TemplateSearch;
    protected final SearchBuilder<VMTemplateStoragePoolVO> PoolTemplateSearch;
    protected final SearchBuilder<VMTemplateStoragePoolVO> TemplateStatusSearch;
    protected final SearchBuilder<VMTemplateStoragePoolVO> TemplatePoolStatusSearch;
    protected final SearchBuilder<VMTemplateStoragePoolVO> TemplateStatesSearch;
    protected final SearchBuilder<VMTemplateStoragePoolVO> updateStateSearch;

    public VMTemplatePoolDaoImpl() {
        PoolSearch = createSearchBuilder();
        PoolSearch.and("pool_id", PoolSearch.entity().getPoolId(), SearchCriteria.Op.EQ);
        PoolSearch.done();

        TemplateSearch = createSearchBuilder();
        TemplateSearch.and("template_id", TemplateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        TemplateSearch.done();

        PoolTemplateSearch = createSearchBuilder();
        PoolTemplateSearch.and("pool_id", PoolTemplateSearch.entity().getPoolId(), SearchCriteria.Op.EQ);
        PoolTemplateSearch.and("template_id", PoolTemplateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        PoolTemplateSearch.done();

        TemplateStatusSearch = createSearchBuilder();
        TemplateStatusSearch.and("template_id", TemplateStatusSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        TemplateStatusSearch.and("download_state", TemplateStatusSearch.entity().getDownloadState(), SearchCriteria.Op.EQ);
        TemplateStatusSearch.done();

        TemplatePoolStatusSearch = createSearchBuilder();
        TemplatePoolStatusSearch.and("pool_id", TemplatePoolStatusSearch.entity().getPoolId(), SearchCriteria.Op.EQ);
        TemplatePoolStatusSearch.and("template_id", TemplatePoolStatusSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        TemplatePoolStatusSearch.and("download_state", TemplatePoolStatusSearch.entity().getDownloadState(), SearchCriteria.Op.EQ);
        TemplatePoolStatusSearch.done();

        TemplateStatesSearch = createSearchBuilder();
        TemplateStatesSearch.and("template_id", TemplateStatesSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        TemplateStatesSearch.and("states", TemplateStatesSearch.entity().getDownloadState(), SearchCriteria.Op.IN);
        TemplateStatesSearch.done();

        updateStateSearch = this.createSearchBuilder();
        updateStateSearch.and("id", updateStateSearch.entity().getId(), Op.EQ);
        updateStateSearch.and("state", updateStateSearch.entity().getState(), Op.EQ);
        updateStateSearch.and("updatedCount", updateStateSearch.entity().getUpdatedCount(), Op.EQ);
        updateStateSearch.done();
    }

    @Override
    public List<VMTemplateStoragePoolVO> listByPoolId(final long id) {
        final SearchCriteria<VMTemplateStoragePoolVO> sc = PoolSearch.create();
        sc.setParameters("pool_id", id);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<VMTemplateStoragePoolVO> listByTemplateId(final long templateId) {
        final SearchCriteria<VMTemplateStoragePoolVO> sc = TemplateSearch.create();
        sc.setParameters("template_id", templateId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public VMTemplateStoragePoolVO findByPoolTemplate(final long hostId, final long templateId) {
        final SearchCriteria<VMTemplateStoragePoolVO> sc = PoolTemplateSearch.create();
        sc.setParameters("pool_id", hostId);
        sc.setParameters("template_id", templateId);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public List<VMTemplateStoragePoolVO> listByTemplateStatus(final long templateId, final VMTemplateStoragePoolVO.Status downloadState) {
        final SearchCriteria<VMTemplateStoragePoolVO> sc = TemplateStatusSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("download_state", downloadState.toString());
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<VMTemplateStoragePoolVO> listByTemplateStatus(final long templateId, final VMTemplateStoragePoolVO.Status downloadState, final long poolId) {
        final SearchCriteria<VMTemplateStoragePoolVO> sc = TemplatePoolStatusSearch.create();
        sc.setParameters("pool_id", poolId);
        sc.setParameters("template_id", templateId);
        sc.setParameters("download_state", downloadState.toString());
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<VMTemplateStoragePoolVO> listByTemplateStatus(final long templateId, final long datacenterId, final VMTemplateStoragePoolVO.Status downloadState) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final List<VMTemplateStoragePoolVO> result = new ArrayList<>();
        try {
            final String sql = DOWNLOADS_STATE_DC;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setLong(1, datacenterId);
            pstmt.setLong(2, templateId);
            pstmt.setString(3, downloadState.toString());
            final ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(toEntityBean(rs, false));
            }
        } catch (final Exception e) {
            s_logger.warn("Exception: ", e);
        }
        return result;
    }

    @Override
    public List<VMTemplateStoragePoolVO> listByTemplateStatus(final long templateId, final long datacenterId, final long podId, final VMTemplateStoragePoolVO.Status
            downloadState) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        final List<VMTemplateStoragePoolVO> result = new ArrayList<>();
        final String sql = DOWNLOADS_STATE_DC_POD;
        try (PreparedStatement pstmt = txn.prepareStatement(sql)) {
            pstmt.setLong(1, datacenterId);
            pstmt.setLong(2, podId);
            pstmt.setLong(3, templateId);
            pstmt.setString(4, downloadState.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // result.add(toEntityBean(rs, false)); TODO: this is buggy in
                    // GenericDaoBase for hand constructed queries
                    final long id = rs.getLong(1); // ID column
                    result.add(findById(id));
                }
            } catch (final Exception e) {
                s_logger.warn("Exception: ", e);
            }
        } catch (final Exception e) {
            s_logger.warn("Exception: ", e);
        }
        return result;
    }

    @Override
    public List<VMTemplateStoragePoolVO> listByTemplateStates(final long templateId, final VMTemplateStoragePoolVO.Status... states) {
        final SearchCriteria<VMTemplateStoragePoolVO> sc = TemplateStatesSearch.create();
        sc.setParameters("states", (Object[]) states);
        sc.setParameters("template_id", templateId);

        return search(sc, null);
    }

    @Override
    public boolean templateAvailable(final long templateId, final long hostId) {
        final VMTemplateStorageResourceAssoc tmpltPool = findByPoolTemplate(hostId, templateId);
        if (tmpltPool == null) {
            return false;
        }

        return tmpltPool.getDownloadState() == Status.DOWNLOADED;
    }

    @Override
    public VMTemplateStoragePoolVO findByHostTemplate(final Long hostId, final Long templateId) {
        final List<VMTemplateStoragePoolVO> result = listByHostTemplate(hostId, templateId);
        return (result.size() == 0) ? null : result.get(1);
    }

    public List<VMTemplateStoragePoolVO> listByHostTemplate(final long hostId, final long templateId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        final List<VMTemplateStoragePoolVO> result = new ArrayList<>();
        final String sql = HOST_TEMPLATE_SEARCH;
        try (PreparedStatement pstmt = txn.prepareStatement(sql)) {
            pstmt.setLong(1, hostId);
            pstmt.setLong(2, templateId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // result.add(toEntityBean(rs, false)); TODO: this is buggy in
                    // GenericDaoBase for hand constructed queries
                    final long id = rs.getLong(1); // ID column
                    result.add(findById(id));
                }
            } catch (final Exception e) {
                s_logger.warn("Exception: ", e);
            }
        } catch (final Exception e) {
            s_logger.warn("Exception: ", e);
        }
        return result;
    }

    @Override
    public boolean updateState(final State currentState, final Event event, final State nextState, final DataObjectInStore vo, final Object data) {
        final VMTemplateStoragePoolVO templatePool = (VMTemplateStoragePoolVO) vo;
        final Long oldUpdated = templatePool.getUpdatedCount();
        final Date oldUpdatedTime = templatePool.getUpdated();

        final SearchCriteria<VMTemplateStoragePoolVO> sc = updateStateSearch.create();
        sc.setParameters("id", templatePool.getId());
        sc.setParameters("state", currentState);
        sc.setParameters("updatedCount", templatePool.getUpdatedCount());

        templatePool.incrUpdatedCount();

        final UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "updated", new Date());

        final int rows = update((VMTemplateStoragePoolVO) vo, sc);
        if (rows == 0 && s_logger.isDebugEnabled()) {
            final VMTemplateStoragePoolVO dbVol = findByIdIncludingRemoved(templatePool.getId());
            if (dbVol != null) {
                final StringBuilder str = new StringBuilder("Unable to update ").append(vo.toString());
                str.append(": DB Data={id=")
                   .append(dbVol.getId())
                   .append("; state=")
                   .append(dbVol.getState())
                   .append("; updatecount=")
                   .append(dbVol.getUpdatedCount())
                   .append(";updatedTime=")
                   .append(dbVol.getUpdated());
                str.append(": New Data={id=")
                   .append(templatePool.getId())
                   .append("; state=")
                   .append(nextState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(templatePool.getUpdatedCount())
                   .append("; updatedTime=")
                   .append(templatePool.getUpdated());
                str.append(": stale Data={id=")
                   .append(templatePool.getId())
                   .append("; state=")
                   .append(currentState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(oldUpdated)
                   .append("; updatedTime=")
                   .append(oldUpdatedTime);
            } else {
                s_logger.debug("Unable to update objectIndatastore: id=" + templatePool.getId() + ", as there is no such object exists in the database anymore");
            }
        }
        return rows > 0;
    }
}
