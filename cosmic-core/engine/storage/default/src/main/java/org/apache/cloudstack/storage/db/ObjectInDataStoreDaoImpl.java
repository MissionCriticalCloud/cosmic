package org.apache.cloudstack.storage.db;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.UpdateBuilder;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.Event;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine.State;

import javax.naming.ConfigurationException;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ObjectInDataStoreDaoImpl extends GenericDaoBase<ObjectInDataStoreVO, Long> implements ObjectInDataStoreDao {
    private static final Logger s_logger = LoggerFactory.getLogger(ObjectInDataStoreDaoImpl.class);
    private SearchBuilder<ObjectInDataStoreVO> updateStateSearch;

    @Override
    public boolean configure(final String name, final Map<String, Object> params) throws ConfigurationException {
        super.configure(name, params);

        updateStateSearch = this.createSearchBuilder();
        updateStateSearch.and("id", updateStateSearch.entity().getId(), Op.EQ);
        updateStateSearch.and("state", updateStateSearch.entity().getState(), Op.EQ);
        updateStateSearch.and("updatedCount", updateStateSearch.entity().getUpdatedCount(), Op.EQ);
        updateStateSearch.done();
        return true;
    }

    @Override
    public boolean updateState(final State currentState, final Event event, final State nextState, final DataObjectInStore dataObj, final Object data) {
        final ObjectInDataStoreVO vo = (ObjectInDataStoreVO) dataObj;
        final Long oldUpdated = vo.getUpdatedCount();
        final Date oldUpdatedTime = vo.getUpdated();

        final SearchCriteria<ObjectInDataStoreVO> sc = updateStateSearch.create();
        sc.setParameters("id", vo.getId());
        sc.setParameters("state", currentState);
        sc.setParameters("updatedCount", vo.getUpdatedCount());

        vo.incrUpdatedCount();

        final UpdateBuilder builder = getUpdateBuilder(vo);
        builder.set(vo, "state", nextState);
        builder.set(vo, "updated", new Date());

        final int rows = update(vo, sc);
        if (rows == 0 && s_logger.isDebugEnabled()) {
            final ObjectInDataStoreVO dbVol = findByIdIncludingRemoved(vo.getId());
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
                   .append(vo.getId())
                   .append("; state=")
                   .append(nextState)
                   .append("; event=")
                   .append(event)
                   .append("; updatecount=")
                   .append(vo.getUpdatedCount())
                   .append("; updatedTime=")
                   .append(vo.getUpdated());
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
                s_logger.debug("Unable to update objectIndatastore: id=" + vo.getId() + ", as there is no such object exists in the database anymore");
            }
        }
        return rows > 0;
    }
}
