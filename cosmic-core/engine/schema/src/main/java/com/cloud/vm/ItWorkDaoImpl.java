package com.cloud.vm;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.time.InaccurateClock;
import com.cloud.vm.ItWorkVO.Step;
import com.cloud.vm.VirtualMachine.State;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ItWorkDaoImpl extends GenericDaoBase<ItWorkVO, String> implements ItWorkDao {
    protected final SearchBuilder<ItWorkVO> AllFieldsSearch;
    protected final SearchBuilder<ItWorkVO> CleanupSearch;
    protected final SearchBuilder<ItWorkVO> OutstandingWorkSearch;
    protected final SearchBuilder<ItWorkVO> WorkInProgressSearch;

    protected ItWorkDaoImpl() {
        super();

        AllFieldsSearch = createSearchBuilder();
        AllFieldsSearch.and("instance", AllFieldsSearch.entity().getInstanceId(), Op.EQ);
        AllFieldsSearch.and("op", AllFieldsSearch.entity().getType(), Op.EQ);
        AllFieldsSearch.and("step", AllFieldsSearch.entity().getStep(), Op.EQ);
        AllFieldsSearch.done();

        CleanupSearch = createSearchBuilder();
        CleanupSearch.and("step", CleanupSearch.entity().getType(), Op.IN);
        CleanupSearch.and("time", CleanupSearch.entity().getUpdatedAt(), Op.LT);
        CleanupSearch.done();

        OutstandingWorkSearch = createSearchBuilder();
        OutstandingWorkSearch.and("instance", OutstandingWorkSearch.entity().getInstanceId(), Op.EQ);
        OutstandingWorkSearch.and("op", OutstandingWorkSearch.entity().getType(), Op.EQ);
        OutstandingWorkSearch.and("step", OutstandingWorkSearch.entity().getStep(), Op.NEQ);
        OutstandingWorkSearch.done();

        WorkInProgressSearch = createSearchBuilder();
        WorkInProgressSearch.and("server", WorkInProgressSearch.entity().getManagementServerId(), Op.EQ);
        WorkInProgressSearch.and("step", WorkInProgressSearch.entity().getStep(), Op.NIN);
        WorkInProgressSearch.done();
    }

    @Override
    public ItWorkVO findByOutstandingWork(final long instanceId, final State state) {
        final SearchCriteria<ItWorkVO> sc = OutstandingWorkSearch.create();
        sc.setParameters("instance", instanceId);
        sc.setParameters("op", state);
        sc.setParameters("step", Step.Done);

        return findOneBy(sc);
    }

    @Override
    public void cleanup(final long wait) {
        final SearchCriteria<ItWorkVO> sc = CleanupSearch.create();
        sc.setParameters("step", Step.Done);
        sc.setParameters("time", InaccurateClock.getTimeInSeconds() - wait);

        remove(sc);
    }

    @Override
    public boolean updateStep(final ItWorkVO work, final Step step) {
        work.setStep(step);
        return update(work.getId(), work);
    }

    @Override
    public boolean update(final String id, final ItWorkVO work) {
        work.setUpdatedAt(InaccurateClock.getTimeInSeconds());

        return super.update(id, work);
    }

    @Override
    public List<ItWorkVO> listWorkInProgressFor(final long nodeId) {
        final SearchCriteria<ItWorkVO> sc = WorkInProgressSearch.create();
        sc.setParameters("server", nodeId);
        sc.setParameters("step", Step.Done);

        return search(sc, null);
    }
}
