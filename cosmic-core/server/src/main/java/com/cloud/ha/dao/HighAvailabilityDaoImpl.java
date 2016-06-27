package com.cloud.ha.dao;

import com.cloud.ha.HaWorkVO;
import com.cloud.ha.HighAvailabilityManager.Step;
import com.cloud.ha.HighAvailabilityManager.WorkType;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;
import com.cloud.utils.exception.CloudRuntimeException;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HighAvailabilityDaoImpl extends GenericDaoBase<HaWorkVO, Long> implements HighAvailabilityDao {
    private static final Logger s_logger = LoggerFactory.getLogger(HighAvailabilityDaoImpl.class);

    private final SearchBuilder<HaWorkVO> TBASearch;
    private final SearchBuilder<HaWorkVO> PreviousInstanceSearch;
    private final SearchBuilder<HaWorkVO> UntakenMigrationSearch;
    private final SearchBuilder<HaWorkVO> CleanupSearch;
    private final SearchBuilder<HaWorkVO> PreviousWorkSearch;
    private final SearchBuilder<HaWorkVO> TakenWorkSearch;
    private final SearchBuilder<HaWorkVO> ReleaseSearch;
    private final SearchBuilder<HaWorkVO> FutureHaWorkSearch;
    private final SearchBuilder<HaWorkVO> RunningHaWorkSearch;
    private final SearchBuilder<HaWorkVO> PendingHaWorkSearch;

    protected HighAvailabilityDaoImpl() {
        super();

        CleanupSearch = createSearchBuilder();
        CleanupSearch.and("time", CleanupSearch.entity().getTimeToTry(), Op.LTEQ);
        CleanupSearch.and("step", CleanupSearch.entity().getStep(), Op.IN);
        CleanupSearch.done();

        TBASearch = createSearchBuilder();
        TBASearch.and("server", TBASearch.entity().getServerId(), Op.NULL);
        TBASearch.and("taken", TBASearch.entity().getDateTaken(), Op.NULL);
        TBASearch.and("time", TBASearch.entity().getTimeToTry(), Op.LTEQ);
        TBASearch.and("step", TBASearch.entity().getStep(), Op.NIN);
        TBASearch.done();

        PreviousInstanceSearch = createSearchBuilder();
        PreviousInstanceSearch.and("instance", PreviousInstanceSearch.entity().getInstanceId(), Op.EQ);
        PreviousInstanceSearch.done();

        UntakenMigrationSearch = createSearchBuilder();
        UntakenMigrationSearch.and("host", UntakenMigrationSearch.entity().getHostId(), Op.EQ);
        UntakenMigrationSearch.and("type", UntakenMigrationSearch.entity().getWorkType(), Op.EQ);
        UntakenMigrationSearch.and("server", UntakenMigrationSearch.entity().getServerId(), Op.NULL);
        UntakenMigrationSearch.and("taken", UntakenMigrationSearch.entity().getDateTaken(), Op.NULL);
        UntakenMigrationSearch.done();

        TakenWorkSearch = createSearchBuilder();
        TakenWorkSearch.and("type", TakenWorkSearch.entity().getWorkType(), Op.EQ);
        TakenWorkSearch.and("server", TakenWorkSearch.entity().getServerId(), Op.NNULL);
        TakenWorkSearch.and("taken", TakenWorkSearch.entity().getDateTaken(), Op.NNULL);
        TakenWorkSearch.and("step", TakenWorkSearch.entity().getStep(), Op.NIN);
        TakenWorkSearch.done();

        PreviousWorkSearch = createSearchBuilder();
        PreviousWorkSearch.and("instance", PreviousWorkSearch.entity().getInstanceId(), Op.EQ);
        PreviousWorkSearch.and("type", PreviousWorkSearch.entity().getWorkType(), Op.EQ);
        PreviousWorkSearch.and("taken", PreviousWorkSearch.entity().getDateTaken(), Op.NULL);
        PreviousWorkSearch.done();

        ReleaseSearch = createSearchBuilder();
        ReleaseSearch.and("server", ReleaseSearch.entity().getServerId(), Op.EQ);
        ReleaseSearch.and("step", ReleaseSearch.entity().getStep(), Op.NIN);
        ReleaseSearch.and("taken", ReleaseSearch.entity().getDateTaken(), Op.NNULL);
        ReleaseSearch.done();

        FutureHaWorkSearch = createSearchBuilder();
        FutureHaWorkSearch.and("instance", FutureHaWorkSearch.entity().getInstanceId(), Op.EQ);
        FutureHaWorkSearch.and("type", FutureHaWorkSearch.entity().getType(), Op.EQ);
        FutureHaWorkSearch.and("id", FutureHaWorkSearch.entity().getId(), Op.GT);
        FutureHaWorkSearch.done();

        RunningHaWorkSearch = createSearchBuilder();
        RunningHaWorkSearch.and("instance", RunningHaWorkSearch.entity().getInstanceId(), Op.EQ);
        RunningHaWorkSearch.and("type", RunningHaWorkSearch.entity().getType(), Op.EQ);
        RunningHaWorkSearch.and("taken", RunningHaWorkSearch.entity().getDateTaken(), Op.NNULL);
        RunningHaWorkSearch.and("step", RunningHaWorkSearch.entity().getStep(), Op.NIN);
        RunningHaWorkSearch.done();

        PendingHaWorkSearch = createSearchBuilder();
        PendingHaWorkSearch.and("instance", PendingHaWorkSearch.entity().getInstanceId(), Op.EQ);
        PendingHaWorkSearch.and("type", PendingHaWorkSearch.entity().getType(), Op.EQ);
        PendingHaWorkSearch.and("step", PendingHaWorkSearch.entity().getStep(), Op.NIN);
        PendingHaWorkSearch.done();
    }

    @Override
    public HaWorkVO take(final long serverId) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        try {
            final SearchCriteria<HaWorkVO> sc = TBASearch.create();
            sc.setParameters("time", System.currentTimeMillis() >> 10);
            sc.setParameters("step", Step.Done, Step.Cancelled);

            final Filter filter = new Filter(HaWorkVO.class, null, true, 0l, 1l);

            txn.start();
            final List<HaWorkVO> vos = lockRows(sc, filter, true);
            if (vos.size() == 0) {
                txn.commit();
                return null;
            }

            final HaWorkVO work = vos.get(0);
            work.setServerId(serverId);
            work.setDateTaken(new Date());

            update(work.getId(), work);

            txn.commit();

            return work;
        } catch (final Throwable e) {
            throw new CloudRuntimeException("Unable to execute take", e);
        }
    }

    @Override
    public List<HaWorkVO> findPreviousHA(final long instanceId) {
        final SearchCriteria<HaWorkVO> sc = PreviousInstanceSearch.create();
        sc.setParameters("instance", instanceId);
        return listIncludingRemovedBy(sc);
    }

    @Override
    public boolean delete(final long instanceId, final WorkType type) {
        final SearchCriteria<HaWorkVO> sc = PreviousWorkSearch.create();
        sc.setParameters("instance", instanceId);
        sc.setParameters("type", type);
        return expunge(sc) > 0;
    }

    @Override
    public void cleanup(final long time) {
        final SearchCriteria<HaWorkVO> sc = CleanupSearch.create();
        sc.setParameters("time", time);
        sc.setParameters("step", Step.Done, Step.Cancelled);
        expunge(sc);
    }

    @Override
    public void deleteMigrationWorkItems(final long hostId, final WorkType type, final long serverId) {
        final SearchCriteria<HaWorkVO> sc = UntakenMigrationSearch.create();
        sc.setParameters("host", hostId);
        sc.setParameters("type", type.toString());

        final HaWorkVO work = createForUpdate();
        final Date date = new Date();
        work.setDateTaken(date);
        work.setServerId(serverId);
        work.setStep(Step.Cancelled);

        update(work, sc);
    }

    @Override
    public List<HaWorkVO> findTakenWorkItems(final WorkType type) {
        final SearchCriteria<HaWorkVO> sc = TakenWorkSearch.create();
        sc.setParameters("type", type);
        sc.setParameters("step", Step.Done, Step.Cancelled, Step.Error);

        return listBy(sc);
    }

    @Override
    public boolean hasBeenScheduled(final long instanceId, final WorkType type) {
        final SearchCriteria<HaWorkVO> sc = PreviousWorkSearch.create();
        sc.setParameters("instance", instanceId);
        sc.setParameters("type", type);
        return listBy(sc, null).size() > 0;
    }

    @Override
    public int releaseWorkItems(final long nodeId) {
        final SearchCriteria<HaWorkVO> sc = ReleaseSearch.create();
        sc.setParameters("server", nodeId);
        sc.setParameters("step", Step.Done, Step.Cancelled, Step.Error);

        final HaWorkVO vo = createForUpdate();
        vo.setDateTaken(null);
        vo.setServerId(null);

        return update(vo, sc);
    }

    @Override
    public List<HaWorkVO> listFutureHaWorkForVm(final long vmId, final long workId) {
        final SearchCriteria<HaWorkVO> sc = FutureHaWorkSearch.create();
        sc.setParameters("instance", vmId);
        sc.setParameters("type", WorkType.HA);
        sc.setParameters("id", workId);

        return search(sc, null);
    }

    @Override
    public List<HaWorkVO> listRunningHaWorkForVm(final long vmId) {
        final SearchCriteria<HaWorkVO> sc = RunningHaWorkSearch.create();
        sc.setParameters("instance", vmId);
        sc.setParameters("type", WorkType.HA);
        sc.setParameters("step", Step.Done, Step.Error, Step.Cancelled);

        return search(sc, null);
    }

    @Override
    public List<HaWorkVO> listPendingHaWorkForVm(final long vmId) {
        final SearchCriteria<HaWorkVO> sc = PendingHaWorkSearch.create();
        sc.setParameters("instance", vmId);
        sc.setParameters("type", WorkType.HA);
        sc.setParameters("step", Step.Done, Step.Error, Step.Cancelled);

        return search(sc, null);
    }
}
