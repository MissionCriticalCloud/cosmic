package com.cloud.storage.dao;

import com.cloud.storage.SnapshotPolicyVO;
import com.cloud.utils.DateUtil.IntervalType;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SnapshotPolicyDaoImpl extends GenericDaoBase<SnapshotPolicyVO, Long> implements SnapshotPolicyDao {
    private final SearchBuilder<SnapshotPolicyVO> VolumeIdSearch;
    private final SearchBuilder<SnapshotPolicyVO> VolumeIdIntervalSearch;
    private final SearchBuilder<SnapshotPolicyVO> ActivePolicySearch;
    private final SearchBuilder<SnapshotPolicyVO> SnapshotPolicySearch;

    protected SnapshotPolicyDaoImpl() {
        VolumeIdSearch = createSearchBuilder();
        VolumeIdSearch.and("volumeId", VolumeIdSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        VolumeIdSearch.and("active", VolumeIdSearch.entity().isActive(), SearchCriteria.Op.EQ);
        VolumeIdSearch.and("display", VolumeIdSearch.entity().isDisplay(), SearchCriteria.Op.EQ);
        VolumeIdSearch.done();

        VolumeIdIntervalSearch = createSearchBuilder();
        VolumeIdIntervalSearch.and("volumeId", VolumeIdIntervalSearch.entity().getVolumeId(), SearchCriteria.Op.EQ);
        VolumeIdIntervalSearch.and("interval", VolumeIdIntervalSearch.entity().getInterval(), SearchCriteria.Op.EQ);
        VolumeIdIntervalSearch.done();

        ActivePolicySearch = createSearchBuilder();
        ActivePolicySearch.and("active", ActivePolicySearch.entity().isActive(), SearchCriteria.Op.EQ);
        ActivePolicySearch.done();

        SnapshotPolicySearch = createSearchBuilder();
        SnapshotPolicySearch.and("id", SnapshotPolicySearch.entity().getId(), SearchCriteria.Op.EQ);
        SnapshotPolicySearch.and("display", SnapshotPolicySearch.entity().isDisplay(), SearchCriteria.Op.EQ);
        SnapshotPolicySearch.done();
    }

    @Override
    public List<SnapshotPolicyVO> listByVolumeId(final long volumeId) {
        return listByVolumeId(volumeId, null);
    }

    @Override
    public List<SnapshotPolicyVO> listByVolumeId(final long volumeId, final Filter filter) {
        final SearchCriteria<SnapshotPolicyVO> sc = VolumeIdSearch.create();
        sc.setParameters("volumeId", volumeId);
        return listBy(sc, filter);
    }

    @Override
    public Pair<List<SnapshotPolicyVO>, Integer> listAndCountByVolumeId(final long volumeId, final boolean display) {
        return listAndCountByVolumeId(volumeId, display, null);
    }

    @Override
    public Pair<List<SnapshotPolicyVO>, Integer> listAndCountByVolumeId(final long volumeId, final boolean display, final Filter filter) {
        final SearchCriteria<SnapshotPolicyVO> sc = VolumeIdSearch.create();
        sc.setParameters("volumeId", volumeId);
        sc.setParameters("display", display);
        sc.setParameters("active", true);
        return searchAndCount(sc, filter);
    }

    @Override
    public SnapshotPolicyVO findOneByVolumeInterval(final long volumeId, final IntervalType intvType) {
        final SearchCriteria<SnapshotPolicyVO> sc = VolumeIdIntervalSearch.create();
        sc.setParameters("volumeId", volumeId);
        sc.setParameters("interval", intvType.ordinal());
        return findOneBy(sc);
    }

    @Override
    public List<SnapshotPolicyVO> listActivePolicies() {
        final SearchCriteria<SnapshotPolicyVO> sc = ActivePolicySearch.create();
        sc.setParameters("active", true);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public SnapshotPolicyVO findOneByVolume(final long volumeId) {
        final SearchCriteria<SnapshotPolicyVO> sc = VolumeIdSearch.create();
        sc.setParameters("volumeId", volumeId);
        sc.setParameters("active", true);
        return findOneBy(sc);
    }

    @Override
    public Pair<List<SnapshotPolicyVO>, Integer> listAndCountById(final long id, final boolean display, final Filter filter) {
        final SearchCriteria<SnapshotPolicyVO> sc = SnapshotPolicySearch.create();
        sc.setParameters("id", id);
        sc.setParameters("display", display);
        return searchAndCount(sc, filter);
    }
}
