package com.cloud.storage.dao;

import com.cloud.engine.subsystem.api.storage.DataObjectInStore;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.legacymodel.dc.HostStatus;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine.Event;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine.State;
import com.cloud.legacymodel.storage.VMTemplateStatus;
import com.cloud.model.enumeration.HostType;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.utils.DateUtil;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.db.UpdateBuilder;

import javax.inject.Inject;
import javax.naming.ConfigurationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VMTemplateHostDaoImpl extends GenericDaoBase<VMTemplateHostVO, Long> implements VMTemplateHostDao {
    public static final Logger s_logger = LoggerFactory.getLogger(VMTemplateHostDaoImpl.class.getName());
    protected static final String UPDATE_TEMPLATE_HOST_REF = "UPDATE template_host_ref SET download_state = ?, download_pct= ?, last_updated = ? "
            + ", error_str = ?, local_path = ?, job_id = ? " + "WHERE host_id = ? and type_id = ?";
    protected static final String DOWNLOADS_STATE_DC = "SELECT t.id, t.host_id, t.template_id, t.created, t.last_updated, t.job_id, "
            + "t.download_pct, t.size, t.physical_size, t.download_state, t.error_str, t.local_path, "
            + "t.install_path, t.url, t.destroyed, t.is_copy FROM template_host_ref t, host h " + "where t.host_id = h.id and h.data_center_id=? "
            + " and t.template_id=? and t.download_state = ?";
    protected static final String DOWNLOADS_STATE_DC_POD = "SELECT * FROM template_host_ref t, host h where t.host_id = h.id and h.data_center_id=? and h.pod_id=? "
            + " and t.template_id=? and t.download_state=?";
    protected static final String DOWNLOADS_STATE = "SELECT * FROM template_host_ref t " + " where t.template_id=? and t.download_state=?";
    protected final SearchBuilder<VMTemplateHostVO> HostSearch;
    protected final SearchBuilder<VMTemplateHostVO> TemplateSearch;
    protected final SearchBuilder<VMTemplateHostVO> HostTemplateSearch;
    protected final SearchBuilder<VMTemplateHostVO> HostTemplateStateSearch;
    protected final SearchBuilder<VMTemplateHostVO> HostDestroyedSearch;
    protected final SearchBuilder<VMTemplateHostVO> TemplateStatusSearch;
    protected final SearchBuilder<VMTemplateHostVO> TemplateStatesSearch;
    protected final SearchBuilder<VMTemplateHostVO> updateStateSearch;
    protected SearchBuilder<VMTemplateHostVO> ZoneTemplateSearch;
    protected SearchBuilder<VMTemplateHostVO> LocalSecondaryStorageSearch;
    @Inject
    HostDao _hostDao;

    public VMTemplateHostDaoImpl() {
        this.HostSearch = createSearchBuilder();
        this.HostSearch.and("host_id", this.HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        this.HostSearch.done();

        this.TemplateSearch = createSearchBuilder();
        this.TemplateSearch.and("template_id", this.TemplateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        this.TemplateSearch.and("destroyed", this.TemplateSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        this.TemplateSearch.done();

        this.HostTemplateSearch = createSearchBuilder();
        this.HostTemplateSearch.and("host_id", this.HostTemplateSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        this.HostTemplateSearch.and("template_id", this.HostTemplateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        this.HostTemplateSearch.and("destroyed", this.HostTemplateSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        this.HostTemplateSearch.done();

        this.HostDestroyedSearch = createSearchBuilder();
        this.HostDestroyedSearch.and("host_id", this.HostDestroyedSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        this.HostDestroyedSearch.and("destroyed", this.HostDestroyedSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        this.HostDestroyedSearch.done();

        this.TemplateStatusSearch = createSearchBuilder();
        this.TemplateStatusSearch.and("template_id", this.TemplateStatusSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        this.TemplateStatusSearch.and("download_state", this.TemplateStatusSearch.entity().getDownloadState(), SearchCriteria.Op.EQ);
        this.TemplateStatusSearch.and("destroyed", this.TemplateStatusSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        this.TemplateStatusSearch.done();

        this.TemplateStatesSearch = createSearchBuilder();
        this.TemplateStatesSearch.and("template_id", this.TemplateStatesSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        this.TemplateStatesSearch.and("states", this.TemplateStatesSearch.entity().getDownloadState(), SearchCriteria.Op.IN);
        this.TemplateStatesSearch.and("destroyed", this.TemplateStatesSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        this.TemplateStatesSearch.done();

        this.HostTemplateStateSearch = createSearchBuilder();
        this.HostTemplateStateSearch.and("template_id", this.HostTemplateStateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        this.HostTemplateStateSearch.and("host_id", this.HostTemplateStateSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        this.HostTemplateStateSearch.and("states", this.HostTemplateStateSearch.entity().getDownloadState(), SearchCriteria.Op.IN);
        this.HostTemplateStateSearch.and("destroyed", this.HostTemplateStateSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        this.HostTemplateStateSearch.done();

        this.updateStateSearch = this.createSearchBuilder();
        this.updateStateSearch.and("id", this.updateStateSearch.entity().getId(), Op.EQ);
        this.updateStateSearch.and("state", this.updateStateSearch.entity().getState(), Op.EQ);
        this.updateStateSearch.and("updatedCount", this.updateStateSearch.entity().getUpdatedCount(), Op.EQ);
        this.updateStateSearch.done();
    }

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        final boolean result = super.configure(name, params);
        this.ZoneTemplateSearch = createSearchBuilder();
        this.ZoneTemplateSearch.and("template_id", this.ZoneTemplateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        this.ZoneTemplateSearch.and("state", this.ZoneTemplateSearch.entity().getDownloadState(), SearchCriteria.Op.EQ);
        final SearchBuilder<HostVO> hostSearch = this._hostDao.createSearchBuilder();
        hostSearch.and("zone_id", hostSearch.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        this.ZoneTemplateSearch.join("tmplHost", hostSearch, hostSearch.entity().getId(), this.ZoneTemplateSearch.entity().getHostId(), JoinBuilder.JoinType.INNER);
        this.ZoneTemplateSearch.done();

        this.LocalSecondaryStorageSearch = createSearchBuilder();
        this.LocalSecondaryStorageSearch.and("template_id", this.LocalSecondaryStorageSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        this.LocalSecondaryStorageSearch.and("state", this.LocalSecondaryStorageSearch.entity().getDownloadState(), SearchCriteria.Op.EQ);
        final SearchBuilder<HostVO> localSecondaryHost = this._hostDao.createSearchBuilder();
        localSecondaryHost.and("private_ip_address", localSecondaryHost.entity().getPrivateIpAddress(), SearchCriteria.Op.EQ);
        localSecondaryHost.and("state", localSecondaryHost.entity().getStatus(), SearchCriteria.Op.EQ);
        localSecondaryHost.and("data_center_id", localSecondaryHost.entity().getDataCenterId(), SearchCriteria.Op.EQ);
        localSecondaryHost.and("type", localSecondaryHost.entity().getType(), SearchCriteria.Op.EQ);
        this.LocalSecondaryStorageSearch.join("host", localSecondaryHost, localSecondaryHost.entity().getId(), this.LocalSecondaryStorageSearch.entity().getHostId(),
                JoinBuilder.JoinType.INNER);
        this.LocalSecondaryStorageSearch.done();

        return result;
    }

    @Override
    public boolean updateState(final State currentState, final Event event, final State nextState, final DataObjectInStore vo, final Object data) {
        final VMTemplateHostVO templateHost = (VMTemplateHostVO) vo;
        final Long oldUpdated = templateHost.getUpdatedCount();
        final Date oldUpdatedTime = templateHost.getUpdated();

        final SearchCriteria<VMTemplateHostVO> sc = this.updateStateSearch.create();
        sc.setParameters("id", templateHost.getId());
        sc.setParameters("state", currentState);
        sc.setParameters("updatedCount", templateHost.getUpdatedCount());

        templateHost.incrUpdatedCount();

        final UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "updated", new Date());

        final int rows = update((VMTemplateHostVO) vo, sc);
        if (rows == 0 && s_logger.isDebugEnabled()) {
            final VMTemplateHostVO dbVol = findByIdIncludingRemoved(templateHost.getId());
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
                   .append(templateHost.getId())
                   .append("; state=")
                   .append(nextState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(templateHost.getUpdatedCount())
                   .append("; updatedTime=")
                   .append(templateHost.getUpdated());
                str.append(": stale Data={id=")
                   .append(templateHost.getId())
                   .append("; state=")
                   .append(currentState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(oldUpdated)
                   .append("; updatedTime=")
                   .append(oldUpdatedTime);
            } else {
                s_logger.debug("Unable to update objectIndatastore: id=" + templateHost.getId() + ", as there is no such object exists in the database anymore");
            }
        }
        return rows > 0;
    }

    @Override
    public void update(final VMTemplateHostVO instance) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        try {
            final Date now = new Date();
            final String sql = UPDATE_TEMPLATE_HOST_REF;
            pstmt = txn.prepareAutoCloseStatement(sql);
            pstmt.setString(1, instance.getDownloadState().toString());
            pstmt.setInt(2, instance.getDownloadPercent());
            pstmt.setString(3, DateUtil.getDateDisplayString(TimeZone.getTimeZone("GMT"), now));
            pstmt.setString(4, instance.getErrorString());
            pstmt.setString(5, instance.getLocalDownloadPath());
            pstmt.setString(6, instance.getJobId());
            pstmt.setLong(7, instance.getHostId());
            pstmt.setLong(8, instance.getTemplateId());
            pstmt.executeUpdate();
        } catch (final Exception e) {
            s_logger.warn("Exception: ", e);
        }
    }

    @Override
    public List<VMTemplateHostVO> listByHostId(final long id) {
        final SearchCriteria<VMTemplateHostVO> sc = this.HostSearch.create();
        sc.setParameters("host_id", id);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<VMTemplateHostVO> listByTemplateId(final long templateId) {
        final SearchCriteria<VMTemplateHostVO> sc = this.TemplateSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("destroyed", false);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<VMTemplateHostVO> listByOnlyTemplateId(final long templateId) {
        final SearchCriteria<VMTemplateHostVO> sc = this.TemplateSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("destroyed", false);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public VMTemplateHostVO findByHostTemplate(final long hostId, final long templateId) {
        final SearchCriteria<VMTemplateHostVO> sc = this.HostTemplateSearch.create();
        sc.setParameters("host_id", hostId);
        sc.setParameters("template_id", templateId);
        sc.setParameters("destroyed", false);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public VMTemplateHostVO findByTemplateId(final long templateId) {
        final SearchCriteria<VMTemplateHostVO> sc = this.HostTemplateSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("destroyed", false);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public List<VMTemplateHostVO> listByTemplateStatus(final long templateId, final VMTemplateStatus downloadState) {
        final SearchCriteria<VMTemplateHostVO> sc = this.TemplateStatusSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("download_state", downloadState.toString());
        sc.setParameters("destroyed", false);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<VMTemplateHostVO> listByTemplateStatus(final long templateId, final long datacenterId, final VMTemplateStatus downloadState) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        PreparedStatement pstmt = null;
        final List<VMTemplateHostVO> result = new ArrayList<>();
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
    public List<VMTemplateHostVO> listByTemplateHostStatus(final long templateId, final long hostId, final VMTemplateStatus... states) {
        final SearchCriteria<VMTemplateHostVO> sc = this.HostTemplateStateSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setParameters("host_id", hostId);
        sc.setParameters("states", (Object[]) states);
        return search(sc, null);
    }

    @Override
    public List<VMTemplateHostVO> listByTemplateStatus(final long templateId, final long datacenterId, final long podId, final VMTemplateStatus downloadState) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        final List<VMTemplateHostVO> result = new ArrayList<>();
        final String sql = DOWNLOADS_STATE_DC_POD;
        try (final PreparedStatement pstmt = txn.prepareStatement(sql)) {
            pstmt.setLong(1, datacenterId);
            pstmt.setLong(2, podId);
            pstmt.setLong(3, templateId);
            pstmt.setString(4, downloadState.toString());
            try (final ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // result.add(toEntityBean(rs, false)); TODO: this is buggy in
                    // GenericDaoBase for hand constructed queries
                    final long id = rs.getLong(1); // ID column
                    result.add(findById(id));
                }
            } catch (final SQLException e) {
                s_logger.warn("listByTemplateStatus:Exception: " + e.getMessage(), e);
            }
        } catch (final Exception e) {
            s_logger.warn("listByTemplateStatus:Exception: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public boolean templateAvailable(final long templateId, final long hostId) {
        final VMTemplateHostVO tmpltHost = findByHostTemplate(hostId, templateId);
        if (tmpltHost == null) {
            return false;
        }

        return tmpltHost.getDownloadState() == VMTemplateStatus.DOWNLOADED;
    }

    @Override
    public List<VMTemplateHostVO> listByTemplateStates(final long templateId, final VMTemplateStatus... states) {
        final SearchCriteria<VMTemplateHostVO> sc = this.TemplateStatesSearch.create();
        sc.setParameters("states", (Object[]) states);
        sc.setParameters("template_id", templateId);
        sc.setParameters("destroyed", false);
        return search(sc, null);
    }

    @Override
    public List<VMTemplateHostVO> listByState(final VMTemplateStatus state) {
        final SearchCriteria<VMTemplateHostVO> sc = createSearchCriteria();
        sc.addAnd("downloadState", SearchCriteria.Op.EQ, state);
        sc.addAnd("destroyed", SearchCriteria.Op.EQ, false);
        return search(sc, null);
    }

    @Override
    public List<VMTemplateHostVO> listByHostTemplate(final long hostId, final long templateId) {
        final SearchCriteria<VMTemplateHostVO> sc = this.HostTemplateSearch.create();
        sc.setParameters("host_id", hostId);
        sc.setParameters("template_id", templateId);
        sc.setParameters("destroyed", false);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public List<VMTemplateHostVO> listByZoneTemplate(final long dcId, final long templateId, final boolean readyOnly) {
        final SearchCriteria<VMTemplateHostVO> sc = this.ZoneTemplateSearch.create();
        sc.setParameters("template_id", templateId);
        sc.setJoinParameters("tmplHost", "zone_id", dcId);
        if (readyOnly) {
            sc.setParameters("state", VMTemplateStatus.DOWNLOADED);
        }
        return listBy(sc);
    }

    @Override
    public List<VMTemplateHostVO> listDestroyed(final long hostId) {
        final SearchCriteria<VMTemplateHostVO> sc = this.HostDestroyedSearch.create();
        sc.setParameters("host_id", hostId);
        sc.setParameters("destroyed", true);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public VMTemplateHostVO findByHostTemplate(final long hostId, final long templateId, final boolean lock) {
        final SearchCriteria<VMTemplateHostVO> sc = this.HostTemplateSearch.create();
        sc.setParameters("host_id", hostId);
        sc.setParameters("template_id", templateId);
        sc.setParameters("destroyed", false);
        if (!lock) {
            return findOneIncludingRemovedBy(sc);
        } else {
            return lockOneRandomRow(sc, true);
        }
    }

    // Based on computing node host id, and template id, find out the
    // corresponding template_host_ref, assuming local secondary storage and
    // computing node is in the same zone, and private ip
    @Override
    public VMTemplateHostVO findLocalSecondaryStorageByHostTemplate(final long hostId, final long templateId) {
        final HostVO computingHost = this._hostDao.findById(hostId);
        final SearchCriteria<VMTemplateHostVO> sc = this.LocalSecondaryStorageSearch.create();
        sc.setJoinParameters("host", "private_ip_address", computingHost.getPrivateIpAddress());
        sc.setJoinParameters("host", "state", HostStatus.Up);
        sc.setJoinParameters("host", "data_center_id", computingHost.getDataCenterId());
        sc.setJoinParameters("host", "type", HostType.LocalSecondaryStorage);
        sc.setParameters("template_id", templateId);
        sc.setParameters("state", VMTemplateStatus.DOWNLOADED);
        sc.setParameters("destroyed", false);
        return findOneBy(sc);
    }

    @Override
    public void deleteByHost(final Long hostId) {
        final List<VMTemplateHostVO> tmpltHosts = listByHostId(hostId);
        for (final VMTemplateHostVO tmpltHost : tmpltHosts) {
            remove(tmpltHost.getId());
        }
    }
}
