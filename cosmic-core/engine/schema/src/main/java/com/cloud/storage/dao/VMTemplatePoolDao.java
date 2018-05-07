package com.cloud.storage.dao;

import com.cloud.engine.subsystem.api.storage.DataObjectInStore;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine;
import com.cloud.legacymodel.storage.VMTemplateStatus;
import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;

import java.util.List;

public interface VMTemplatePoolDao extends GenericDao<VMTemplateStoragePoolVO, Long>,
        StateDao<ObjectInDataStoreStateMachine.State, ObjectInDataStoreStateMachine.Event, DataObjectInStore> {
    public List<VMTemplateStoragePoolVO> listByPoolId(long id);

    public List<VMTemplateStoragePoolVO> listByTemplateId(long templateId);

    public VMTemplateStoragePoolVO findByPoolTemplate(long poolId, long templateId);

    public List<VMTemplateStoragePoolVO> listByTemplateStatus(long templateId, VMTemplateStatus downloadState);

    public List<VMTemplateStoragePoolVO> listByTemplateStatus(long templateId, VMTemplateStatus downloadState, long poolId);

    public List<VMTemplateStoragePoolVO> listByTemplateStatus(long templateId, long datacenterId, VMTemplateStatus downloadState);

    public List<VMTemplateStoragePoolVO> listByTemplateStatus(long templateId, long datacenterId, long podId, VMTemplateStatus downloadState);

    public List<VMTemplateStoragePoolVO> listByTemplateStates(long templateId, VMTemplateStatus... states);

    boolean templateAvailable(long templateId, long poolId);

    public VMTemplateStoragePoolVO findByHostTemplate(Long hostId, Long templateId);
}
