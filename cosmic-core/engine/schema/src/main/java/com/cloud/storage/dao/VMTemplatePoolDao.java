package com.cloud.storage.dao;

import com.cloud.storage.VMTemplateStoragePoolVO;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;
import org.apache.cloudstack.engine.subsystem.api.storage.DataObjectInStore;
import org.apache.cloudstack.engine.subsystem.api.storage.ObjectInDataStoreStateMachine;

import java.util.List;

public interface VMTemplatePoolDao extends GenericDao<VMTemplateStoragePoolVO, Long>,
        StateDao<ObjectInDataStoreStateMachine.State, ObjectInDataStoreStateMachine.Event, DataObjectInStore> {
    public List<VMTemplateStoragePoolVO> listByPoolId(long id);

    public List<VMTemplateStoragePoolVO> listByTemplateId(long templateId);

    public VMTemplateStoragePoolVO findByPoolTemplate(long poolId, long templateId);

    public List<VMTemplateStoragePoolVO> listByTemplateStatus(long templateId, VMTemplateStoragePoolVO.Status downloadState);

    public List<VMTemplateStoragePoolVO> listByTemplateStatus(long templateId, VMTemplateStoragePoolVO.Status downloadState, long poolId);

    public List<VMTemplateStoragePoolVO> listByTemplateStatus(long templateId, long datacenterId, VMTemplateStoragePoolVO.Status downloadState);

    public List<VMTemplateStoragePoolVO> listByTemplateStatus(long templateId, long datacenterId, long podId, VMTemplateStoragePoolVO.Status downloadState);

    public List<VMTemplateStoragePoolVO> listByTemplateStates(long templateId, VMTemplateStoragePoolVO.Status... states);

    boolean templateAvailable(long templateId, long poolId);

    public VMTemplateStoragePoolVO findByHostTemplate(Long hostId, Long templateId);
}
