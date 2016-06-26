package com.cloud.storage.dao;

import com.cloud.storage.VolumeHostVO;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.UpdateBuilder;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VolumeHostDaoImpl extends GenericDaoBase<VolumeHostVO, Long> implements VolumeHostDao {
    private static final Logger s_logger = LoggerFactory.getLogger(VolumeHostDaoImpl.class);
    protected final SearchBuilder<VolumeHostVO> HostVolumeSearch;
    protected final SearchBuilder<VolumeHostVO> ZoneVolumeSearch;
    protected final SearchBuilder<VolumeHostVO> VolumeSearch;
    protected final SearchBuilder<VolumeHostVO> HostSearch;
    protected final SearchBuilder<VolumeHostVO> HostDestroyedSearch;
    protected final SearchBuilder<VolumeHostVO> updateStateSearch;

    public VolumeHostDaoImpl() {
        HostVolumeSearch = createSearchBuilder();
        HostVolumeSearch.and("host_id", HostVolumeSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostVolumeSearch.and("volume_id", HostVolumeSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        HostVolumeSearch.and("destroyed", HostVolumeSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        HostVolumeSearch.done();

        ZoneVolumeSearch = createSearchBuilder();
        ZoneVolumeSearch.and("zone_id", ZoneVolumeSearch.entity().getZoneId(), SearchCriteria.Op.EQ);
        ZoneVolumeSearch.and("volume_id", ZoneVolumeSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        ZoneVolumeSearch.and("destroyed", ZoneVolumeSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        ZoneVolumeSearch.done();

        HostSearch = createSearchBuilder();
        HostSearch.and("host_id", HostSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostSearch.and("destroyed", HostSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        HostSearch.done();

        VolumeSearch = createSearchBuilder();
        VolumeSearch.and("volume_id", VolumeSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        VolumeSearch.and("destroyed", VolumeSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        VolumeSearch.done();

        HostDestroyedSearch = createSearchBuilder();
        HostDestroyedSearch.and("host_id", HostDestroyedSearch.entity().getHostId(), SearchCriteria.Op.EQ);
        HostDestroyedSearch.and("destroyed", HostDestroyedSearch.entity().getDestroyed(), SearchCriteria.Op.EQ);
        HostDestroyedSearch.done();

        updateStateSearch = this.createSearchBuilder();
        updateStateSearch.and("id", updateStateSearch.entity().getId(), Op.EQ);
        updateStateSearch.and("state", updateStateSearch.entity().getState(), Op.EQ);
        updateStateSearch.and("updatedCount", updateStateSearch.entity().getUpdatedCount(), Op.EQ);
        updateStateSearch.done();
    }

    @Override
    public VolumeHostVO findByHostVolume(final long hostId, final long volumeId) {
        final SearchCriteria<VolumeHostVO> sc = HostVolumeSearch.create();
        sc.setParameters("host_id", hostId);
        sc.setParameters("volume_id", volumeId);
        sc.setParameters("destroyed", false);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public VolumeHostVO findByVolumeId(final long volumeId) {
        final SearchCriteria<VolumeHostVO> sc = VolumeSearch.create();
        sc.setParameters("volume_id", volumeId);
        sc.setParameters("destroyed", false);
        return findOneBy(sc);
    }

    @Override
    public List<VolumeHostVO> listBySecStorage(final long ssHostId) {
        final SearchCriteria<VolumeHostVO> sc = HostSearch.create();
        sc.setParameters("host_id", ssHostId);
        sc.setParameters("destroyed", false);
        return listAll();
    }

    @Override
    public List<VolumeHostVO> listDestroyed(final long hostId) {
        final SearchCriteria<VolumeHostVO> sc = HostDestroyedSearch.create();
        sc.setParameters("host_id", hostId);
        sc.setParameters("destroyed", true);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public VolumeHostVO findVolumeByZone(final long volumeId, final long zoneId) {
        final SearchCriteria<VolumeHostVO> sc = ZoneVolumeSearch.create();
        sc.setParameters("zone_id", zoneId);
        sc.setParameters("volume_id", volumeId);
        sc.setParameters("destroyed", false);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public boolean updateState(final State currentState, final Event event, final State nextState, final DataObjectInStore vo, final Object data) {
        final VolumeHostVO volHost = (VolumeHostVO) vo;
        final Long oldUpdated = volHost.getUpdatedCount();
        final Date oldUpdatedTime = volHost.getUpdated();

        final SearchCriteria<VolumeHostVO> sc = updateStateSearch.create();
        sc.setParameters("id", volHost.getId());
        sc.setParameters("state", currentState);
        sc.setParameters("updatedCount", volHost.getUpdatedCount());

        volHost.incrUpdatedCount();

        final UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "updated", new Date());

        final int rows = update((VolumeHostVO) vo, sc);
        if (rows == 0 && s_logger.isDebugEnabled()) {
            final VolumeHostVO dbVol = findByIdIncludingRemoved(volHost.getId());
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
                   .append(volHost.getId())
                   .append("; state=")
                   .append(nextState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(volHost.getUpdatedCount())
                   .append("; updatedTime=")
                   .append(volHost.getUpdated());
                str.append(": stale Data={id=")
                   .append(volHost.getId())
                   .append("; state=")
                   .append(currentState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(oldUpdated)
                   .append("; updatedTime=")
                   .append(oldUpdatedTime);
            } else {
                s_logger.debug("Unable to update objectIndatastore: id=" + volHost.getId() + ", as there is no such object exists in the database anymore");
            }
        }
        return rows > 0;
    }
}
