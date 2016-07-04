package org.apache.cloudstack.storage.volume.db;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.QueryBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.UpdateBuilder;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TemplatePrimaryDataStoreDaoImpl extends GenericDaoBase<TemplatePrimaryDataStoreVO, Long> implements TemplatePrimaryDataStoreDao {
    private static final Logger s_logger = LoggerFactory.getLogger(TemplatePrimaryDataStoreDaoImpl.class);
    protected final SearchBuilder<TemplatePrimaryDataStoreVO> updateSearchBuilder;

    public TemplatePrimaryDataStoreDaoImpl() {
        updateSearchBuilder = createSearchBuilder();
        updateSearchBuilder.and("id", updateSearchBuilder.entity().getId(), Op.EQ);
        updateSearchBuilder.and("state", updateSearchBuilder.entity().getState(), Op.EQ);
        updateSearchBuilder.and("updatedCount", updateSearchBuilder.entity().getUpdatedCount(), Op.EQ);
        updateSearchBuilder.done();
    }

    @Override
    public TemplatePrimaryDataStoreVO findByTemplateIdAndPoolId(final long templateId, final long poolId) {
        final QueryBuilder<TemplatePrimaryDataStoreVO> sc = QueryBuilder.create(TemplatePrimaryDataStoreVO.class);
        sc.and(sc.entity().getTemplateId(), Op.EQ, templateId);
        sc.and(sc.entity().getPoolId(), Op.EQ, poolId);
        return sc.find();
    }

    @Override
    public TemplatePrimaryDataStoreVO findByTemplateIdAndPoolIdAndReady(final long templateId, final long poolId) {
        final QueryBuilder<TemplatePrimaryDataStoreVO> sc = QueryBuilder.create(TemplatePrimaryDataStoreVO.class);
        sc.and(sc.entity().getTemplateId(), Op.EQ, templateId);
        sc.and(sc.entity().getPoolId(), Op.EQ, poolId);
        sc.and(sc.entity().getState(), Op.EQ, ObjectInDataStoreStateMachine.State.Ready);
        return sc.find();
    }

    @Override
    public boolean updateState(final State currentState, final Event event, final State nextState, final TemplatePrimaryDataStoreVO vo, final Object data) {
        final Long oldUpdated = vo.getUpdatedCount();
        final Date oldUpdatedTime = vo.getLastUpdated();

        final SearchCriteria<TemplatePrimaryDataStoreVO> sc = updateSearchBuilder.create();
        sc.setParameters("id", vo.getId());
        sc.setParameters("state", currentState);
        sc.setParameters("updatedCount", vo.getUpdatedCount());

        vo.incrUpdatedCount();

        final UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "lastUpdated", new Date());

        final int rows = update(vo, sc);
        if (rows == 0 && s_logger.isDebugEnabled()) {
            final TemplatePrimaryDataStoreVO template = findByIdIncludingRemoved(vo.getId());
            if (template != null) {
                final StringBuilder str = new StringBuilder("Unable to update ").append(vo.toString());
                str.append(": DB Data={id=")
                   .append(template.getId())
                   .append("; state=")
                   .append(template.getState())
                   .append("; updatecount=")
                   .append(template.getUpdatedCount())
                   .append(";updatedTime=")
                   .append(template.getLastUpdated());
                str.append(": New Data={id=")
                   .append(vo.getId())
                   .append("; state=")
                   .append(nextState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(vo.getUpdatedCount())
                   .append("; updatedTime=")
                   .append(vo.getLastUpdated());
                str.append(": stale Data={id=")
                   .append(vo.getId())
                   .append("; state=")
                   .append(currentState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(oldUpdated)
                   .append("; updatedTime=")
                   .append(oldUpdatedTime);
            } else {
                s_logger.debug("Unable to update template: id=" + vo.getId() + ", as there is no such template exists in the database anymore");
            }
        }
        return rows > 0;
    }
}
