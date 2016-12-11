package com.cloud.storage.volume.db;

import com.cloud.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;

public interface TemplatePrimaryDataStoreDao extends GenericDao<TemplatePrimaryDataStoreVO, Long>,
        StateDao<ObjectInDataStoreStateMachine.State, ObjectInDataStoreStateMachine.Event, TemplatePrimaryDataStoreVO> {
    public TemplatePrimaryDataStoreVO findByTemplateIdAndPoolId(long templateId, long poolId);

    public TemplatePrimaryDataStoreVO findByTemplateIdAndPoolIdAndReady(long templateId, long poolId);
}
