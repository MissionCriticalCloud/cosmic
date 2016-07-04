package org.apache.cloudstack.storage.volume.db;

import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;

public interface TemplatePrimaryDataStoreDao extends GenericDao<TemplatePrimaryDataStoreVO, Long>,
        StateDao<ObjectInDataStoreStateMachine.State, ObjectInDataStoreStateMachine.Event, TemplatePrimaryDataStoreVO> {
    public TemplatePrimaryDataStoreVO findByTemplateIdAndPoolId(long templateId, long poolId);

    public TemplatePrimaryDataStoreVO findByTemplateIdAndPoolIdAndReady(long templateId, long poolId);
}
