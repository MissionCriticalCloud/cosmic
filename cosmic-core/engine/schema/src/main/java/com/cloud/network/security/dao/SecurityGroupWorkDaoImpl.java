package com.cloud.network.security.dao;

import com.cloud.network.security.SecurityGroupWork;
import com.cloud.network.security.SecurityGroupWork.Step;
import com.cloud.network.security.SecurityGroupWorkVO;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.TransactionLegacy;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecurityGroupWorkDaoImpl extends GenericDaoBase<SecurityGroupWorkVO, Long> implements SecurityGroupWorkDao {
    private static final Logger s_logger = LoggerFactory.getLogger(SecurityGroupWorkDaoImpl.class);

    private final SearchBuilder<SecurityGroupWorkVO> VmIdTakenSearch;
    private final SearchBuilder<SecurityGroupWorkVO> VmIdSeqNumSearch;
    private final SearchBuilder<SecurityGroupWorkVO> VmIdUnTakenSearch;
    private final SearchBuilder<SecurityGroupWorkVO> UntakenWorkSearch;
    private final SearchBuilder<SecurityGroupWorkVO> VmIdStepSearch;
    private final SearchBuilder<SecurityGroupWorkVO> CleanupSearch;

    protected SecurityGroupWorkDaoImpl() {
        VmIdTakenSearch = createSearchBuilder();
        VmIdTakenSearch.and("vmId", VmIdTakenSearch.entity().getInstanceId(), SearchCriteria.Op.EQ);
        VmIdTakenSearch.and("taken", VmIdTakenSearch.entity().getDateTaken(), SearchCriteria.Op.NNULL);

        VmIdTakenSearch.done();

        VmIdUnTakenSearch = createSearchBuilder();
        VmIdUnTakenSearch.and("vmId", VmIdUnTakenSearch.entity().getInstanceId(), SearchCriteria.Op.EQ);
        VmIdUnTakenSearch.and("taken", VmIdUnTakenSearch.entity().getDateTaken(), SearchCriteria.Op.NULL);

        VmIdUnTakenSearch.done();

        UntakenWorkSearch = createSearchBuilder();
        UntakenWorkSearch.and("server", UntakenWorkSearch.entity().getServerId(), SearchCriteria.Op.NULL);
        UntakenWorkSearch.and("taken", UntakenWorkSearch.entity().getDateTaken(), SearchCriteria.Op.NULL);
        UntakenWorkSearch.and("step", UntakenWorkSearch.entity().getStep(), SearchCriteria.Op.EQ);

        UntakenWorkSearch.done();

        VmIdSeqNumSearch = createSearchBuilder();
        VmIdSeqNumSearch.and("vmId", VmIdSeqNumSearch.entity().getInstanceId(), SearchCriteria.Op.EQ);
        VmIdSeqNumSearch.and("seqno", VmIdSeqNumSearch.entity().getLogsequenceNumber(), SearchCriteria.Op.EQ);

        VmIdSeqNumSearch.done();

        VmIdStepSearch = createSearchBuilder();
        VmIdStepSearch.and("vmId", VmIdStepSearch.entity().getInstanceId(), SearchCriteria.Op.EQ);
        VmIdStepSearch.and("step", VmIdStepSearch.entity().getStep(), SearchCriteria.Op.EQ);

        VmIdStepSearch.done();

        CleanupSearch = createSearchBuilder();
        CleanupSearch.and("taken", CleanupSearch.entity().getDateTaken(), Op.LTEQ);
        CleanupSearch.and("step", CleanupSearch.entity().getStep(), SearchCriteria.Op.IN);

        CleanupSearch.done();
    }

    @Override
    public SecurityGroupWork findByVmId(final long vmId, final boolean taken) {
        final SearchCriteria<SecurityGroupWorkVO> sc = taken ? VmIdTakenSearch.create() : VmIdUnTakenSearch.create();
        sc.setParameters("vmId", vmId);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    public SecurityGroupWorkVO findByVmIdStep(final long vmId, final Step step) {
        final SearchCriteria<SecurityGroupWorkVO> sc = VmIdStepSearch.create();
        sc.setParameters("vmId", vmId);
        sc.setParameters("step", step);
        return findOneIncludingRemovedBy(sc);
    }

    @Override
    @DB
    public SecurityGroupWorkVO take(final long serverId) {
        final SecurityGroupWorkVO work;
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            final SearchCriteria<SecurityGroupWorkVO> sc = UntakenWorkSearch.create();
            sc.setParameters("step", Step.Scheduled);

            final Filter filter = new Filter(SecurityGroupWorkVO.class, null, true, 0l, 1l);//FIXME: order desc by update time?

            txn.start();
            final List<SecurityGroupWorkVO> vos = lockRows(sc, filter, true);
            if (vos.size() == 0) {
                txn.commit();
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("Security Group take: no work found");
                }
                return null;
            }
            work = vos.get(0);
            boolean processing = false;
            if (findByVmIdStep(work.getInstanceId(), Step.Processing) != null) {
                //ensure that there is no job in Processing state for the same VM
                processing = true;
                if (s_logger.isTraceEnabled()) {
                    s_logger.trace("Security Group work take: found a job in Scheduled and Processing  vmid=" + work.getInstanceId());
                }
            }
            work.setServerId(serverId);
            work.setDateTaken(new Date());
            if (processing) {
                //the caller to take() should check the step and schedule another work item to come back
                //and take a look.
                work.setStep(SecurityGroupWork.Step.Done);
            } else {
                work.setStep(SecurityGroupWork.Step.Processing);
            }

            update(work.getId(), work);

            txn.commit();
        }

        return work;
    }

    @Override
    @DB
    public void updateStep(final Long vmId, final Long logSequenceNumber, final Step step) {
        final TransactionLegacy txn = TransactionLegacy.currentTxn();
        txn.start();
        final SearchCriteria<SecurityGroupWorkVO> sc = VmIdSeqNumSearch.create();
        sc.setParameters("vmId", vmId);
        sc.setParameters("seqno", logSequenceNumber);

        final Filter filter = new Filter(SecurityGroupWorkVO.class, null, true, 0l, 1l);

        final List<SecurityGroupWorkVO> vos = lockRows(sc, filter, true);
        if (vos.size() == 0) {
            txn.commit();
            return;
        }
        final SecurityGroupWorkVO work = vos.get(0);
        work.setStep(step);
        update(work.getId(), work);

        txn.commit();
    }

    @Override
    @DB
    public void updateStep(final Long workId, final Step step) {
        try (final TransactionLegacy txn = TransactionLegacy.currentTxn()) {
            txn.start();

            final SecurityGroupWorkVO work = lockRow(workId, true);
            if (work == null) {
                txn.commit();
                return;
            }
            work.setStep(step);
            update(work.getId(), work);

            txn.commit();
        }
    }

    @Override
    public int deleteFinishedWork(final Date timeBefore) {
        final SearchCriteria<SecurityGroupWorkVO> sc = CleanupSearch.create();
        sc.setParameters("taken", timeBefore);
        sc.setParameters("step", Step.Done);

        return expunge(sc);
    }

    @Override
    public List<SecurityGroupWorkVO> findUnfinishedWork(final Date timeBefore) {
        final SearchCriteria<SecurityGroupWorkVO> sc = CleanupSearch.create();
        sc.setParameters("taken", timeBefore);
        sc.setParameters("step", Step.Processing);

        final List<SecurityGroupWorkVO> result = listIncludingRemovedBy(sc);

        return result;
    }

    @Override
    public List<SecurityGroupWorkVO> findAndCleanupUnfinishedWork(final Date timeBefore) {
        final SearchCriteria<SecurityGroupWorkVO> sc = CleanupSearch.create();
        sc.setParameters("taken", timeBefore);
        sc.setParameters("step", Step.Processing);

        final List<SecurityGroupWorkVO> result = listIncludingRemovedBy(sc);

        final SecurityGroupWorkVO work = createForUpdate();
        work.setStep(Step.Error);
        update(work, sc);

        return result;
    }

    @Override
    public List<SecurityGroupWorkVO> findScheduledWork() {
        final SearchCriteria<SecurityGroupWorkVO> sc = UntakenWorkSearch.create();
        sc.setParameters("step", Step.Scheduled);
        return listIncludingRemovedBy(sc);
    }
}
