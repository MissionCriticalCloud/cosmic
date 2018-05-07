package com.cloud.storage.dao;

import com.cloud.engine.subsystem.api.storage.DataObjectInStore;
import com.cloud.legacymodel.storage.ObjectInDataStoreStateMachine;
import com.cloud.legacymodel.storage.VMTemplateStatus;
import com.cloud.storage.VMTemplateHostVO;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.fsm.StateDao;

import java.util.List;

public interface VMTemplateHostDao extends GenericDao<VMTemplateHostVO, Long>,
        StateDao<ObjectInDataStoreStateMachine.State, ObjectInDataStoreStateMachine.Event, DataObjectInStore> {
    List<VMTemplateHostVO> listByHostId(long id);

    List<VMTemplateHostVO> listByTemplateId(long templateId);

    List<VMTemplateHostVO> listByOnlyTemplateId(long templateId);

    VMTemplateHostVO findByHostTemplate(long hostId, long templateId);

    VMTemplateHostVO findByTemplateId(long templateId);

    VMTemplateHostVO findByHostTemplate(long hostId, long templateId, boolean lock);

    List<VMTemplateHostVO> listByHostTemplate(long hostId, long templateId);

    void update(VMTemplateHostVO instance);

    List<VMTemplateHostVO> listByTemplateStatus(long templateId, VMTemplateStatus downloadState);

    List<VMTemplateHostVO> listByTemplateStatus(long templateId, long datacenterId, VMTemplateStatus downloadState);

    List<VMTemplateHostVO> listByTemplateStatus(long templateId, long datacenterId, long podId, VMTemplateStatus downloadState);

    List<VMTemplateHostVO> listByTemplateStates(long templateId, VMTemplateStatus... states);

    List<VMTemplateHostVO> listDestroyed(long hostId);

    boolean templateAvailable(long templateId, long hostId);

    List<VMTemplateHostVO> listByZoneTemplate(long dcId, long templateId, boolean readyOnly);

    void deleteByHost(Long hostId);

    VMTemplateHostVO findLocalSecondaryStorageByHostTemplate(long hostId, long templateId);

    List<VMTemplateHostVO> listByTemplateHostStatus(long templateId, long hostId, VMTemplateStatus... states);

    List<VMTemplateHostVO> listByState(VMTemplateStatus state);
}
